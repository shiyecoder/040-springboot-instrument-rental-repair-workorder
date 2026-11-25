package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.rental.common.BizException;
import com.music.rental.common.CurrentUser;
import com.music.rental.common.R;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 维修工单：接单 → 检测报价 → 零件领用 → 维修 → 试奏检验 → 完工验收 */
@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairOrderMapper repairMapper;
    private final RepairPartMapper partMapper;
    private final InstrumentMapper instrumentMapper;
    private final RentalOrderMapper orderMapper;
    private final SysMessageMapper messageMapper;

    public RepairController(RepairOrderMapper repairMapper, RepairPartMapper partMapper, InstrumentMapper instrumentMapper,
                            RentalOrderMapper orderMapper, SysMessageMapper messageMapper) {
        this.repairMapper = repairMapper;
        this.partMapper = partMapper;
        this.instrumentMapper = instrumentMapper;
        this.orderMapper = orderMapper;
        this.messageMapper = messageMapper;
    }

    @GetMapping("/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String keyword) {
        QueryWrapper<RepairOrder> qw = new QueryWrapper<>();
        if (status != null && !status.isBlank() && !"全部".equals(status)) qw.eq("status", status);
        if (keyword != null && !keyword.isBlank()) qw.and(w -> w.like("repair_no", keyword).or().like("fault_desc", keyword));
        // 待接单优先，其次更新时间倒序
        qw.last("order by field(status,'PENDING','DIAGNOSING','REPAIRING','TESTING','COMPLETED','CLOSED'), create_time desc");
        Page<RepairOrder> p = repairMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (RepairOrder r : p.getRecords()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("repair", r);
            Instrument i = instrumentMapper.selectById(r.getInstrumentId());
            m.put("instrumentNo", i == null ? "-" : i.getInstrumentNo());
            m.put("instrumentName", i == null ? "-" : i.getName());
            m.put("category", i == null ? "-" : i.getCategory());
            rows.add(m);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", rows);
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return R.ok(res);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        RepairOrder r = repairMapper.selectById(id);
        if (r == null) throw new BizException("工单不存在");
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("repair", r);
        res.put("instrument", instrumentMapper.selectById(r.getInstrumentId()));
        res.put("parts", partMapper.selectList(new QueryWrapper<RepairPart>().eq("repair_id", id)));
        res.put("order", r.getOrderId() == null ? null : orderMapper.selectById(r.getOrderId()));
        return R.ok(res);
    }

    @PostMapping
    public R<Long> create(@RequestBody Map<String, Object> body) {
        RepairOrder r = new RepairOrder();
        r.setRepairNo("RP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", repairMapper.selectCount(new QueryWrapper<>()) + 1));
        r.setInstrumentId(Long.valueOf(String.valueOf(body.get("instrumentId"))));
        String fault = body.get("faultDesc") == null ? "" : String.valueOf(body.get("faultDesc"));
        if (fault.length() < 5) throw new BizException("故障描述不少于 5 个字");
        r.setFaultDesc(fault);
        r.setFaultImages(body.get("images") == null ? null : String.valueOf(body.get("images")));
        r.setStatus("PENDING");
        r.setSource("STORE");
        r.setEstimatedCost(BigDecimal.ZERO);
        r.setActualCost(BigDecimal.ZERO);
        r.setCreateTime(LocalDateTime.now());
        repairMapper.insert(r);
        return R.ok(r.getId());
    }

    /** 技师接单：绑定本人，乐器进入维修中 */
    @PostMapping("/{id}/accept")
    public R<Void> accept(@PathVariable Long id) {
        RepairOrder r = must(id);
        if (!"PENDING".equals(r.getStatus())) throw new BizException("该工单已被接单");
        r.setStatus("DIAGNOSING");
        r.setTechnicianId(CurrentUser.id());
        r.setTechnicianName(CurrentUser.get().getRealName());
        repairMapper.updateById(r);
        Instrument i = instrumentMapper.selectById(r.getInstrumentId());
        if (i != null) {
            i.setStatus("REPAIRING");
            i.setUpdateTime(LocalDateTime.now());
            instrumentMapper.updateById(i);
        }
        return R.ok();
    }

    @PostMapping("/{id}/diagnose")
    public R<Void> diagnose(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RepairOrder r = must(id);
        if (!"DIAGNOSING".equals(r.getStatus())) throw new BizException("当前状态不可检测");
        String diagnosis = str(body.get("diagnosis"));
        if (diagnosis == null || diagnosis.length() < 3) throw new BizException("请填写检测结论");
        r.setDiagnosis(diagnosis);
        r.setEstimatedCost(dec(body.get("estimatedCost")));
        if (r.getEstimatedCost() == null) throw new BizException("请填写预估费用");
        r.setStatus("REPAIRING");
        repairMapper.updateById(r);
        return R.ok();
    }

    /** 零件领用：累计到实际费用 */
    @PostMapping("/{id}/part")
    public R<Void> addPart(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RepairOrder r = must(id);
        if (!List.of("REPAIRING", "DIAGNOSING").contains(r.getStatus())) throw new BizException("当前状态不可领用零件");
        RepairPart p = new RepairPart();
        p.setRepairId(id);
        p.setPartName(str(body.get("partName")));
        p.setQuantity(body.get("quantity") == null ? 1 : Integer.parseInt(str(body.get("quantity"))));
        p.setUnitPrice(dec(body.get("unitPrice")) == null ? BigDecimal.ZERO : dec(body.get("unitPrice")));
        p.setAmount(p.getUnitPrice().multiply(BigDecimal.valueOf(p.getQuantity())));
        p.setCreateTime(LocalDateTime.now());
        partMapper.insert(p);
        return R.ok();
    }

    /** 维修完成：填写实际处理与试奏结果，转入待验收 */
    @PostMapping("/{id}/finish")
    public R<Void> finish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RepairOrder r = must(id);
        if (!"REPAIRING".equals(r.getStatus())) throw new BizException("当前状态不可完工");
        String handle = str(body.get("handleDesc"));
        if (handle == null || handle.length() < 5) throw new BizException("实际处理说明不少于 5 个字");
        r.setHandleDesc(handle);
        r.setTestResult(str(body.get("testResult")));
        BigDecimal partsCost = partMapper.selectList(new QueryWrapper<RepairPart>().eq("repair_id", id)).stream()
                .map(RepairPart::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal actual = dec(body.get("actualCost"));
        r.setActualCost(actual == null ? partsCost : actual);
        r.setStatus("TESTING");
        r.setFinishTime(LocalDateTime.now());
        repairMapper.updateById(r);
        return R.ok();
    }

    /** 完工验收：工单关闭，乐器回到可租，累加维修次数 */
    @PostMapping("/{id}/close")
    public R<Void> close(@PathVariable Long id) {
        RepairOrder r = must(id);
        if (!List.of("TESTING", "COMPLETED").contains(r.getStatus())) throw new BizException("请先完成维修与试奏");
        r.setStatus("CLOSED");
        r.setAcceptTime(LocalDateTime.now());
        repairMapper.updateById(r);
        Instrument i = instrumentMapper.selectById(r.getInstrumentId());
        if (i != null) {
            i.setStatus("AVAILABLE");
            i.setRepairCount((i.getRepairCount() == null ? 0 : i.getRepairCount()) + 1);
            i.setUpdateTime(LocalDateTime.now());
            instrumentMapper.updateById(i);
        }
        if (r.getOrderId() != null) {
            RentalOrder o = orderMapper.selectById(r.getOrderId());
            if (o != null) {
                SysMessage m = new SysMessage();
                m.setUserId(o.getCustomerId());
                m.setTitle("维修已完成");
                m.setContent("您的乐器已完成维修并通过试奏检验：" + r.getTestResult());
                m.setBizNo(o.getOrderNo());
                m.setBizType("REPAIR");
                m.setLink("/my-repair");
                m.setIsRead(0);
                m.setCreateTime(LocalDateTime.now());
                messageMapper.insert(m);
            }
        }
        return R.ok();
    }

    private RepairOrder must(Long id) {
        RepairOrder r = repairMapper.selectById(id);
        if (r == null) throw new BizException("工单不存在");
        return r;
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static BigDecimal dec(Object o) {
        if (o == null || String.valueOf(o).isBlank()) return null;
        return new BigDecimal(String.valueOf(o));
    }
}

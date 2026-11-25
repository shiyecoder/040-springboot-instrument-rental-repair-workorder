package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.rental.common.BizException;
import com.music.rental.common.CurrentUser;
import com.music.rental.common.R;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import com.music.rental.service.RentalService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 租赁订单台：预约 → 押金 → 交接 → 租用中 → 归还验收，含续租、报修、赔偿、评价 */
@RestController
@RequestMapping("/api/rental")
public class RentalController {

    private final RentalOrderMapper orderMapper;
    private final RentalService rentalService;
    private final RentalHandoverMapper handoverMapper;
    private final RenewalApplicationMapper renewalMapper;
    private final ReturnInspectionMapper inspectionMapper;
    private final DamageClaimMapper damageMapper;
    private final RepairOrderMapper repairMapper;
    private final RepairPartMapper partMapper;
    private final InstrumentMapper instrumentMapper;
    private final CustomerFeedbackMapper feedbackMapper;
    private final SysMessageMapper messageMapper;
    private final MaintainPlanMapper maintainPlanMapper;
    private final SysUserMapper userMapper;

    public RentalController(RentalOrderMapper orderMapper, RentalService rentalService, RentalHandoverMapper handoverMapper,
                            RenewalApplicationMapper renewalMapper, ReturnInspectionMapper inspectionMapper,
                            DamageClaimMapper damageMapper, RepairOrderMapper repairMapper, RepairPartMapper partMapper,
                            InstrumentMapper instrumentMapper, CustomerFeedbackMapper feedbackMapper,
                            SysMessageMapper messageMapper, MaintainPlanMapper maintainPlanMapper, SysUserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.rentalService = rentalService;
        this.handoverMapper = handoverMapper;
        this.renewalMapper = renewalMapper;
        this.inspectionMapper = inspectionMapper;
        this.damageMapper = damageMapper;
        this.repairMapper = repairMapper;
        this.partMapper = partMapper;
        this.instrumentMapper = instrumentMapper;
        this.feedbackMapper = feedbackMapper;
        this.messageMapper = messageMapper;
        this.maintainPlanMapper = maintainPlanMapper;
        this.userMapper = userMapper;
    }

    /** 订单台分页：待办优先 + 更新时间倒序；客户只看本人 */
    @GetMapping("/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String status,
                                       @RequestParam(required = false) String tab,
                                       @RequestParam(required = false) String dateFrom,
                                       @RequestParam(required = false) String dateTo) {
        QueryWrapper<RentalOrder> qw = new QueryWrapper<>();
        SysUser me = CurrentUser.get();
        if ("CUSTOMER".equals(me.getRoleCode())) qw.eq("customer_id", me.getId());
        if (notBlank(keyword)) qw.and(w -> w.like("order_no", keyword).or().like("customer_name", keyword));
        if (notBlank(status) && !"全部".equals(status)) qw.eq("status", status);
        if (notBlank(dateFrom)) qw.ge("create_time", dateFrom + " 00:00:00");
        if (notBlank(dateTo)) qw.le("create_time", dateTo + " 23:59:59");
        if ("TODO".equals(tab)) qw.in("status", List.of("PENDING_AUDIT", "PENDING_DEPOSIT", "PENDING_HANDOVER", "PENDING_RETURN"));
        if ("DOING".equals(tab)) qw.eq("status", "RENTING");
        if ("DONE".equals(tab)) qw.eq("status", "COMPLETED");
        if ("ABNORMAL".equals(tab)) qw.in("status", List.of("REJECTED", "CANCELLED"));
        // 待办优先：用 CASE 排序保证处理中的单据排在最前
        qw.last("order by field(status,'PENDING_AUDIT','PENDING_DEPOSIT','PENDING_HANDOVER','PENDING_RETURN','RENTING','COMPLETED','REJECTED','CANCELLED'), update_time desc");
        Page<RentalOrder> p = orderMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        long todo = orderMapper.selectCount(new QueryWrapper<RentalOrder>()
                .in("status", List.of("PENDING_AUDIT", "PENDING_DEPOSIT", "PENDING_HANDOVER", "PENDING_RETURN")));
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", p.getRecords());
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        res.put("todo", todo);
        return R.ok(res);
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        RentalOrder o = orderMapper.selectById(id);
        if (o == null) throw new BizException("订单不存在");
        SysUser me = CurrentUser.get();
        if ("CUSTOMER".equals(me.getRoleCode()) && !me.getId().equals(o.getCustomerId())) {
            throw new BizException("只能查看本人订单");
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("order", o);
        res.put("instrument", instrumentMapper.selectById(o.getInstrumentId()));
        res.put("handovers", handoverMapper.selectList(new QueryWrapper<RentalHandover>().eq("order_id", id).orderByAsc("handover_time")));
        res.put("renewals", renewalMapper.selectList(new QueryWrapper<RenewalApplication>().eq("order_id", id).orderByDesc("apply_time")));
        res.put("inspections", inspectionMapper.selectList(new QueryWrapper<ReturnInspection>().eq("order_id", id)));
        res.put("damages", damageMapper.selectList(new QueryWrapper<DamageClaim>().eq("order_id", id)));
        res.put("repairs", repairMapper.selectList(new QueryWrapper<RepairOrder>().eq("order_id", id)));
        res.put("feedbacks", feedbackMapper.selectList(new QueryWrapper<CustomerFeedback>().eq("order_id", id)));
        return R.ok(res);
    }

    /** 客户提交预约：后端计算租金押金并锁定乐器时段 */
    @PostMapping
    public R<Long> create(@RequestBody RentalOrder body) {
        SysUser me = CurrentUser.get();
        Instrument inst = rentalService.instrumentOf(body.getInstrumentId());
        rentalService.checkRentable(inst);
        int days = rentalService.days(body.getStartDate(), body.getEndDate());
        RentalPrice price = rentalService.priceOf(inst.getCategory());
        if (days < price.getMinDays()) throw new BizException("该类别最短租期为 " + price.getMinDays() + " 天");
        if (days > price.getMaxDays()) throw new BizException("该类别最长租期为 " + price.getMaxDays() + " 天");
        String conflict = rentalService.conflict(inst.getId(), body.getStartDate(), body.getEndDate(), null);
        if (!conflict.isEmpty()) throw new BizException(conflict);

        RentalOrder o = new RentalOrder();
        o.setOrderNo("RO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", orderMapper.selectCount(new QueryWrapper<>()) + 1));
        o.setInstrumentId(inst.getId());
        o.setCustomerId("CUSTOMER".equals(me.getRoleCode()) ? me.getId() : (body.getCustomerId() == null ? me.getId() : body.getCustomerId()));
        o.setCustomerName("CUSTOMER".equals(me.getRoleCode()) ? me.getRealName() : body.getCustomerName());
        o.setStartDate(body.getStartDate());
        o.setEndDate(body.getEndDate());
        o.setPurpose(body.getPurpose());
        o.setDeliveryType(body.getDeliveryType());
        o.setNeedAccessory(body.getNeedAccessory());
        o.setIdProof(body.getIdProof());
        o.setIdProofImage(body.getIdProofImage());
        o.setRentAmount(rentalService.rentOf(price, days));
        o.setDepositAmount(price.getDeposit());
        o.setOverdueFee(BigDecimal.ZERO);
        o.setCompensateAmount(BigDecimal.ZERO);
        o.setDepositStatus("UNPAID");
        o.setStatus("PENDING_AUDIT");
        o.setImage(inst.getImage());
        o.setApplyTime(LocalDateTime.now());
        o.setDeleted(0);
        o.setVersion(0);
        o.setCreateTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.insert(o);

        inst.setStatus("RESERVED");
        inst.setUpdateTime(LocalDateTime.now());
        instrumentMapper.updateById(inst);

        pushToClerk("待审批：新租赁预约", inst.getInstrumentNo() + " " + inst.getName() + " 申请 " + o.getStartDate() + " 起租", o.getOrderNo());
        return R.ok(o.getId());
    }

    /** 审核：通过进入待押金，退回则释放乐器 */
    @PostMapping("/{id}/audit")
    public R<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String reason = str(body.get("reason"));
        if (!pass && (reason == null || reason.length() < 5)) throw new BizException("退回原因不少于 5 个字");
        if (!"PENDING_AUDIT".equals(o.getStatus())) throw new BizException("当前状态不可审核");
        rentalService.assertTransition(o.getStatus(), pass ? "PENDING_DEPOSIT" : "REJECTED");
        o.setStatus(pass ? "PENDING_DEPOSIT" : "REJECTED");
        o.setHandlerId(CurrentUser.id());
        o.setHandlerName(CurrentUser.get().getRealName());
        if (reason != null) o.setRemark(reason);
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        if (!pass) {
            releaseInstrument(o.getInstrumentId(), "AVAILABLE");
            push(o.getCustomerId(), "预约被退回", reason, o.getOrderNo());
        } else {
            push(o.getCustomerId(), "预约已通过", "请到店登记押金 ¥" + o.getDepositAmount(), o.getOrderNo());
        }
        return R.ok();
    }

    /** 线下押金登记：只记录状态与凭证，不接线上支付 */
    @PostMapping("/{id}/deposit")
    public R<Void> deposit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!"PENDING_DEPOSIT".equals(o.getStatus())) throw new BizException("当前状态不可登记押金");
        BigDecimal amount = dec(body.get("amount"));
        if (amount == null || amount.compareTo(o.getDepositAmount()) < 0) {
            throw new BizException("实收押金不能低于应收 ¥" + o.getDepositAmount());
        }
        rentalService.assertTransition(o.getStatus(), "PENDING_HANDOVER");
        o.setDepositStatus("PAID");
        o.setStatus("PENDING_HANDOVER");
        o.setHandlerId(CurrentUser.id());
        o.setHandlerName(CurrentUser.get().getRealName());
        o.setRemark(str(body.get("method")) + "；凭证：" + Optional.ofNullable(str(body.get("proof"))).orElse("无"));
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        push(o.getCustomerId(), "押金已登记", "请到店办理出库交接，携带身份证件", o.getOrderNo());
        return R.ok();
    }

    /** 出库交接：记录外观/功能/编号/配件/划痕/照片，双方确认后进入租用中 */
    @PostMapping("/{id}/handover")
    public R<Void> handover(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!"PENDING_HANDOVER".equals(o.getStatus())) throw new BizException("当前状态不可交接");
        if (!"PAID".equals(o.getDepositStatus())) throw new BizException("押金未登记，不得出库");
        rentalService.assertTransition(o.getStatus(), "RENTING");

        RentalHandover h = new RentalHandover();
        h.setOrderId(id);
        h.setHandoverType("OUT");
        h.setAppearanceCheck(str(body.get("appearance")));
        h.setFunctionCheck(str(body.get("function")));
        h.setInstrumentNoCheck(str(body.get("instrumentNoCheck")));
        h.setAccessoryList(str(body.get("accessories")));
        h.setScratches(str(body.get("scratches")));
        h.setPhotos(str(body.get("photos")));
        h.setCustomerConfirm(1);
        h.setStaffConfirm(1);
        h.setOperator(CurrentUser.get().getRealName());
        h.setHandoverTime(LocalDateTime.now());
        h.setRemark(str(body.get("remark")));
        handoverMapper.insert(h);

        o.setStatus("RENTING");
        o.setHandoverTime(LocalDateTime.now());
        o.setHandlerId(CurrentUser.id());
        o.setHandlerName(CurrentUser.get().getRealName());
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);

        Instrument inst = instrumentMapper.selectById(o.getInstrumentId());
        if (inst != null) {
            inst.setStatus("RENTING");
            inst.setUpdateTime(LocalDateTime.now());
            instrumentMapper.updateById(inst);
        }
        push(o.getCustomerId(), "已出库", "乐器已交接，请按时归还（" + o.getEndDate() + "）", o.getOrderNo());
        return R.ok();
    }

    /** 客户申请续租：检查后续预约、保养、未处理故障 */
    @PostMapping("/{id}/renew")
    public R<Long> renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!"RENTING".equals(o.getStatus())) throw new BizException("只有租用中的订单可以续租");
        LocalDate newEnd = LocalDate.parse(str(body.get("newEndDate")));
        if (!newEnd.isAfter(o.getEndDate())) throw new BizException("新归还日必须晚于当前归还日");
        String reason = str(body.get("reason"));
        if (reason == null || reason.length() < 5) throw new BizException("续租说明不少于 5 个字");

        String conflict = rentalService.conflict(o.getInstrumentId(), o.getEndDate().plusDays(1), newEnd, o.getId());
        long openFault = repairMapper.selectCount(new QueryWrapper<RepairOrder>()
                .eq("order_id", id).in("status", List.of("PENDING", "DIAGNOSING", "REPAIRING", "TESTING")));
        if (openFault > 0 && conflict.isEmpty()) conflict = "存在未处理故障报修，需先处理完成";

        int extraDays = rentalService.days(o.getEndDate().plusDays(1), newEnd);
        RentalPrice price = rentalService.priceOf(rentalService.instrumentOf(o.getInstrumentId()).getCategory());
        BigDecimal extra = rentalService.rentOf(price, extraDays);

        RenewalApplication r = new RenewalApplication();
        r.setOrderId(id);
        r.setOldEndDate(o.getEndDate());
        r.setNewEndDate(newEnd);
        r.setExtraRent(extra);
        r.setConflictFlag(conflict.isEmpty() ? 0 : 1);
        r.setConflictReason(conflict);
        r.setStatus("PENDING");
        r.setApplyReason(reason);
        r.setApplyTime(LocalDateTime.now());
        renewalMapper.insert(r);
        return R.ok(r.getId());
    }

    @PostMapping("/renew/{id}/audit")
    public R<Void> auditRenew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RenewalApplication r = renewalMapper.selectById(id);
        if (r == null) throw new BizException("续租申请不存在");
        if (!"PENDING".equals(r.getStatus())) throw new BizException("该申请已处理");
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String remark = str(body.get("remark"));
        if (!pass && (remark == null || remark.length() < 5)) throw new BizException("驳回原因不少于 5 个字");
        if (pass && r.getConflictFlag() != null && r.getConflictFlag() == 1) {
            throw new BizException("存在冲突，不能通过：" + r.getConflictReason());
        }
        r.setStatus(pass ? "APPROVED" : "REJECTED");
        r.setAuditTime(LocalDateTime.now());
        r.setAuditor(CurrentUser.get().getRealName());
        r.setAuditRemark(remark);
        renewalMapper.updateById(r);
        if (pass) {
            RentalOrder o = orderMapper.selectById(r.getOrderId());
            o.setEndDate(r.getNewEndDate());
            o.setRentAmount(o.getRentAmount().add(r.getExtraRent()));
            o.setRemark("续租至 " + r.getNewEndDate() + "，补租金 ¥" + r.getExtraRent());
            o.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(o);
            push(o.getCustomerId(), "续租已通过", "归还日延长至 " + r.getNewEndDate() + "，补租金 ¥" + r.getExtraRent(), o.getOrderNo());
        }
        return R.ok();
    }

    /** 归还验收：后端计算逾期费，按结论决定是否转赔偿/维修 */
    @PostMapping("/{id}/return")
    public R<Map<String, Object>> returnCheck(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!List.of("RENTING", "PENDING_RETURN").contains(o.getStatus())) throw new BizException("当前状态不可归还");
        Instrument inst = rentalService.instrumentOf(o.getInstrumentId());
        RentalPrice price = rentalService.priceOf(inst.getCategory());

        LocalDate actual = body.get("returnDate") == null ? LocalDate.now() : LocalDate.parse(str(body.get("returnDate")));
        int overdueDays = actual.isAfter(o.getEndDate()) ? (int) (actual.toEpochDay() - o.getEndDate().toEpochDay()) : 0;
        BigDecimal fee = rentalService.overdueFee(price, o.getEndDate(), actual);
        String result = Optional.ofNullable(str(body.get("result"))).orElse("PASS");

        ReturnInspection ri = new ReturnInspection();
        ri.setOrderId(id);
        ri.setFunctionCheck(str(body.get("functionCheck")));
        ri.setAppearanceCheck(str(body.get("appearanceCheck")));
        ri.setAccessoryCheck(str(body.get("accessoryCheck")));
        ri.setCleanCheck(str(body.get("cleanCheck")));
        ri.setOverdueDays(overdueDays);
        ri.setOverdueFee(fee);
        ri.setResult(result);
        ri.setDamageDesc(str(body.get("damageDesc")));
        ri.setPhotos(str(body.get("photos")));
        ri.setInspector(CurrentUser.get().getRealName());
        ri.setInspectTime(LocalDateTime.now());
        inspectionMapper.insert(ri);

        o.setOverdueFee(fee);
        o.setActualReturnTime(LocalDateTime.now());
        o.setUpdateTime(LocalDateTime.now());
        if ("PASS".equals(result)) {
            o.setStatus("COMPLETED");
            // 押金只有在验收、逾期、赔偿都处理完后才可退
            o.setDepositStatus("REFUNDED");
            inst.setStatus("AVAILABLE");
            inst.setTotalRentDays((inst.getTotalRentDays() == null ? 0 : inst.getTotalRentDays()) + rentalService.days(o.getStartDate(), actual));
        } else {
            o.setStatus("PENDING_RETURN");
            o.setDepositStatus("PAID"); // 待赔偿处理
            inst.setStatus("REPAIRING".equals(result) ? "REPAIRING" : "MAINTAINING");
        }
        inst.setUpdateTime(LocalDateTime.now());
        instrumentMapper.updateById(inst);
        orderMapper.updateById(o);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("overdueDays", overdueDays);
        res.put("overdueFee", fee);
        res.put("depositRefund", o.getDepositAmount().subtract(fee).subtract(o.getCompensateAmount()));
        return R.ok(res);
    }

    /** 损坏赔偿：登记损坏点、缺失配件、责任与线下赔偿 */
    @PostMapping("/{id}/damage")
    public R<Void> damage(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        DamageClaim d = new DamageClaim();
        d.setOrderId(id);
        d.setDamagePoint(str(body.get("damagePoint")));
        d.setMissingAccessory(str(body.get("missingAccessory")));
        BigDecimal cost = dec(body.get("repairCost"));
        d.setRepairCost(cost == null ? BigDecimal.ZERO : cost);
        d.setLiability(str(body.get("liability")));
        d.setConclusion(str(body.get("conclusion")));
        BigDecimal amount = "自然损耗".equals(d.getLiability()) ? BigDecimal.ZERO : d.getRepairCost();
        d.setAmount(amount);
        d.setPayStatus("PAID");
        d.setPayProof(str(body.get("payProof")));
        d.setOperator(CurrentUser.get().getRealName());
        d.setCreateTime(LocalDateTime.now());
        damageMapper.insert(d);

        o.setCompensateAmount(o.getCompensateAmount().add(amount));
        o.setDepositStatus("REFUNDED");
        o.setStatus("COMPLETED");
        o.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(o);
        push(o.getCustomerId(), "损坏赔偿已登记", "赔偿金额 ¥" + amount + "，押金已退回", o.getOrderNo());
        return R.ok();
    }

    /** 租期报修：确认故障后生成维修工单 */
    @PostMapping("/{id}/repair")
    public R<Long> reportFault(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!List.of("RENTING", "PENDING_RETURN").contains(o.getStatus())) throw new BizException("租期外不能报修");
        String fault = str(body.get("faultDesc"));
        if (fault == null || fault.length() < 5) throw new BizException("故障描述不少于 5 个字");
        RepairOrder r = new RepairOrder();
        r.setRepairNo("RP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", repairMapper.selectCount(new QueryWrapper<>()) + 1));
        r.setInstrumentId(o.getInstrumentId());
        r.setOrderId(id);
        r.setFaultDesc(fault);
        r.setFaultImages(str(body.get("images")));
        r.setStatus("PENDING");
        r.setSource("RENTAL");
        r.setEstimatedCost(BigDecimal.ZERO);
        r.setActualCost(BigDecimal.ZERO);
        r.setCreateTime(LocalDateTime.now());
        repairMapper.insert(r);
        pushToTechnician("待接单：" + rentalService.instrumentOf(o.getInstrumentId()).getName() + " 维修", fault, r.getRepairNo());
        return R.ok(r.getId());
    }

    @PostMapping("/{id}/feedback")
    public R<Void> feedback(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        RentalOrder o = mustOrder(id);
        if (!"COMPLETED".equals(o.getStatus())) throw new BizException("订单完成后才能评价");
        CustomerFeedback f = new CustomerFeedback();
        f.setOrderId(id);
        f.setInstrumentId(o.getInstrumentId());
        f.setCustomerId(o.getCustomerId());
        f.setRating(body.get("rating") == null ? 5 : Integer.parseInt(str(body.get("rating"))));
        f.setContent(str(body.get("content")));
        f.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(f);
        return R.ok();
    }

    /** 可租日历：返回该乐器所有占用区间 */
    @GetMapping("/calendar")
    public R<Map<String, Object>> calendar(@RequestParam Long instrumentId,
                                          @RequestParam(required = false) String month) {
        Instrument inst = rentalService.instrumentOf(instrumentId);
        LocalDate first = month == null ? LocalDate.now().withDayOfMonth(1)
                : LocalDate.parse(month + "-01");
        LocalDate last = first.plusMonths(1).minusDays(1);
        List<Map<String, Object>> events = new ArrayList<>();
        for (RentalOrder o : orderMapper.selectList(new QueryWrapper<RentalOrder>()
                .eq("instrument_id", instrumentId)
                .in("status", List.of("PENDING_DEPOSIT", "PENDING_HANDOVER", "RENTING", "PENDING_RETURN"))
                .le("start_date", last).ge("end_date", first))) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "RENTAL");
            e.put("from", o.getStartDate());
            e.put("to", o.getEndDate());
            e.put("no", o.getOrderNo());
            e.put("status", o.getStatus());
            events.add(e);
        }
        for (RepairOrder r : repairMapper.selectList(new QueryWrapper<RepairOrder>()
                .eq("instrument_id", instrumentId)
                .in("status", List.of("PENDING", "DIAGNOSING", "REPAIRING", "TESTING")))) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "REPAIR");
            e.put("from", r.getCreateTime().toLocalDate());
            e.put("to", r.getCreateTime().toLocalDate().plusDays(7));
            e.put("no", r.getRepairNo());
            e.put("status", r.getStatus());
            events.add(e);
        }
        for (MaintainPlan m : rentalControllerHelperMaintains(instrumentId, first, last)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("type", "MAINTAIN");
            e.put("from", m.getPlanDate());
            e.put("to", m.getPlanDate());
            e.put("no", "MP-" + m.getId());
            e.put("status", m.getStatus());
            events.add(e);
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("instrument", inst);
        res.put("events", events);
        res.put("price", rentalService.priceOf(inst.getCategory()));
        return R.ok(res);
    }

    private List<MaintainPlan> rentalControllerHelperMaintains(Long instrumentId, LocalDate first, LocalDate last) {
        return maintainPlanMapper.selectList(new QueryWrapper<MaintainPlan>()
                .eq("instrument_id", instrumentId).between("plan_date", first, last));
    }

    /** 客户费用台账：租金/押金/逾期/赔偿，全部线下收取 */
    @GetMapping("/ledger")
    public R<Map<String, Object>> ledger() {
        SysUser me = CurrentUser.get();
        QueryWrapper<RentalOrder> qw = new QueryWrapper<>();
        if ("CUSTOMER".equals(me.getRoleCode())) qw.eq("customer_id", me.getId());
        qw.in("status", List.of("PENDING_DEPOSIT", "PENDING_HANDOVER", "RENTING", "PENDING_RETURN", "COMPLETED"));
        List<RentalOrder> orders = orderMapper.selectList(qw);
        BigDecimal rent = BigDecimal.ZERO, deposit = BigDecimal.ZERO, overdue = BigDecimal.ZERO, comp = BigDecimal.ZERO;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (RentalOrder o : orders) {
            rent = rent.add(o.getRentAmount() == null ? BigDecimal.ZERO : o.getRentAmount());
            if (!"REFUNDED".equals(o.getDepositStatus())) {
                deposit = deposit.add(o.getDepositAmount() == null ? BigDecimal.ZERO : o.getDepositAmount());
            }
            overdue = overdue.add(o.getOverdueFee() == null ? BigDecimal.ZERO : o.getOverdueFee());
            comp = comp.add(o.getCompensateAmount() == null ? BigDecimal.ZERO : o.getCompensateAmount());
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("orderNo", o.getOrderNo());
            r.put("time", o.getCreateTime());
            r.put("type", "租金");
            r.put("amount", o.getRentAmount());
            r.put("status", "PAID");
            r.put("orderId", o.getId());
            rows.add(r);
            if (o.getDepositAmount() != null && o.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("orderNo", o.getOrderNo());
                d.put("time", o.getHandoverTime());
                d.put("type", "押金");
                d.put("amount", o.getDepositAmount());
                d.put("status", "REFUNDED".equals(o.getDepositStatus()) ? "REFUNDED" : "PAID");
                d.put("orderId", o.getId());
                rows.add(d);
            }
            if (o.getOverdueFee() != null && o.getOverdueFee().compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("orderNo", o.getOrderNo());
                d.put("time", o.getActualReturnTime());
                d.put("type", "逾期费");
                d.put("amount", o.getOverdueFee());
                d.put("status", "PAID");
                d.put("orderId", o.getId());
                rows.add(d);
            }
            if (o.getCompensateAmount() != null && o.getCompensateAmount().compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> d = new LinkedHashMap<>();
                d.put("orderNo", o.getOrderNo());
                d.put("time", o.getActualReturnTime());
                d.put("type", "赔偿金");
                d.put("amount", o.getCompensateAmount());
                d.put("status", "PAID");
                d.put("orderId", o.getId());
                rows.add(d);
            }
        }
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("rent", rent);
        res.put("deposit", deposit);
        res.put("overdue", overdue);
        res.put("compensate", comp);
        res.put("rows", rows);
        return R.ok(res);
    }

    @GetMapping("/renew/page")
    public R<List<Map<String, Object>>> renewPage() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RenewalApplication r : renewalMapper.selectList(new QueryWrapper<RenewalApplication>().orderByDesc("apply_time"))) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("renew", r);
            RentalOrder o = orderMapper.selectById(r.getOrderId());
            m.put("orderNo", o == null ? "-" : o.getOrderNo());
            m.put("customerName", o == null ? "-" : o.getCustomerName());
            list.add(m);
        }
        return R.ok(list);
    }

    private RentalOrder mustOrder(Long id) {
        RentalOrder o = orderMapper.selectById(id);
        if (o == null) throw new BizException("订单不存在");
        return o;
    }

    private void releaseInstrument(Long id, String status) {
        Instrument i = instrumentMapper.selectById(id);
        if (i != null) {
            i.setStatus(status);
            i.setUpdateTime(LocalDateTime.now());
            instrumentMapper.updateById(i);
        }
    }

    private void push(Long userId, String title, String content, String bizNo) {
        SysMessage m = new SysMessage();
        m.setUserId(userId);
        m.setTitle(title);
        m.setContent(content);
        m.setBizNo(bizNo);
        m.setBizType("RENTAL");
        m.setLink("/my-orders");
        m.setIsRead(0);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
    }

    private void pushToClerk(String title, String content, String bizNo) {
        for (SysUser u : clerkAndAdmin()) push(u.getId(), title, content, bizNo);
    }

    private void pushToTechnician(String title, String content, String bizNo) {
        for (SysUser u : clerkAndAdmin()) {
            if ("TECHNICIAN".equals(u.getRoleCode())) push(u.getId(), title, content, bizNo);
        }
    }

    private List<SysUser> clerkAndAdmin() {
        return userMapper.selectList(new QueryWrapper<SysUser>().in("role_code", List.of("CLERK", "TECHNICIAN", "ADMIN")));
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static BigDecimal dec(Object o) {
        if (o == null || String.valueOf(o).isBlank()) return null;
        return new BigDecimal(String.valueOf(o));
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}

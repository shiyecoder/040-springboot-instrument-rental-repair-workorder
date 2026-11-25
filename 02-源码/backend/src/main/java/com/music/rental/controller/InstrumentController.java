package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.rental.common.BizException;
import com.music.rental.common.CurrentUser;
import com.music.rental.common.R;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import com.music.rental.service.UploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** 乐器台账、配件、价格方案、保养计划、文件上传 */
@RestController
@RequestMapping("/api")
public class InstrumentController {

    private final InstrumentMapper instrumentMapper;
    private final InstrumentAccessoryMapper accessoryMapper;
    private final RentalPriceMapper priceMapper;
    private final MaintainPlanMapper maintainMapper;
    private final RentalOrderMapper orderMapper;
    private final RepairOrderMapper repairMapper;
    private final SysFileMapper fileMapper;
    private final UploadService uploadService;

    public InstrumentController(InstrumentMapper instrumentMapper, InstrumentAccessoryMapper accessoryMapper,
                                RentalPriceMapper priceMapper, MaintainPlanMapper maintainMapper,
                                RentalOrderMapper orderMapper, RepairOrderMapper repairMapper,
                                SysFileMapper fileMapper, UploadService uploadService) {
        this.instrumentMapper = instrumentMapper;
        this.accessoryMapper = accessoryMapper;
        this.priceMapper = priceMapper;
        this.maintainMapper = maintainMapper;
        this.orderMapper = orderMapper;
        this.repairMapper = repairMapper;
        this.fileMapper = fileMapper;
        this.uploadService = uploadService;
    }

    @GetMapping("/instrument/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String brand,
                                      @RequestParam(required = false) String condition,
                                      @RequestParam(required = false) String location,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String repairStatus) {
        QueryWrapper<Instrument> qw = new QueryWrapper<>();
        if (notBlank(keyword)) qw.and(w -> w.like("instrument_no", keyword).or().like("name", keyword).or().like("model", keyword));
        if (notBlank(category) && !"全部".equals(category)) qw.eq("category", category);
        if (notBlank(brand)) qw.like("brand", brand);
        if (notBlank(condition) && !"全部".equals(condition)) qw.eq("condition_level", condition);
        if (notBlank(location) && !"全部".equals(location)) qw.like("location", location);
        if (notBlank(status) && !"全部".equals(status)) qw.eq("status", status);
        if (notBlank(repairStatus) && !"全部".equals(repairStatus)) {
            if ("REPAIRING".equals(repairStatus)) qw.eq("status", "REPAIRING");
            else qw.ne("status", "REPAIRING");
        }
        qw.orderByAsc("instrument_no");
        Page<Instrument> p = instrumentMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        Map<String, Object> res = pageOf(p.getRecords(), p.getTotal(), pageNum, pageSize);
        return R.ok(res);
    }

    @GetMapping("/instrument/options")
    public R<List<Map<String, Object>>> options(@RequestParam(required = false) String status) {
        QueryWrapper<Instrument> qw = new QueryWrapper<>();
        if (notBlank(status)) qw.eq("status", status);
        qw.orderByAsc("instrument_no");
        List<Map<String, Object>> list = new ArrayList<>();
        for (Instrument i : instrumentMapper.selectList(qw)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", i.getId());
            m.put("label", i.getInstrumentNo() + " " + i.getName());
            m.put("status", i.getStatus());
            m.put("category", i.getCategory());
            list.add(m);
        }
        return R.ok(list);
    }

    /** 详情：主档 + 配件 + 关联单据 + 最近评分 */
    @GetMapping("/instrument/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Instrument i = instrumentMapper.selectById(id);
        if (i == null) throw new BizException("乐器不存在");
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("instrument", i);
        res.put("accessories", accessoryMapper.selectList(new QueryWrapper<InstrumentAccessory>().eq("instrument_id", id)));
        res.put("orders", orderMapper.selectList(new QueryWrapper<RentalOrder>().eq("instrument_id", id)
                .orderByDesc("create_time").last("limit 5")));
        res.put("repairs", repairMapper.selectList(new QueryWrapper<RepairOrder>().eq("instrument_id", id)
                .orderByDesc("create_time").last("limit 5")));
        res.put("maintains", maintainMapper.selectList(new QueryWrapper<MaintainPlan>().eq("instrument_id", id)
                .orderByAsc("plan_date")));
        return R.ok(res);
    }

    @PostMapping("/instrument")
    public R<Long> create(@RequestBody Instrument i) {
        validate(i);
        if (instrumentMapper.selectCount(new QueryWrapper<Instrument>().eq("instrument_no", i.getInstrumentNo().trim())) > 0) {
            throw new BizException("乐器编号已存在");
        }
        i.setInstrumentNo(i.getInstrumentNo().trim());
        i.setStatus(i.getStatus() == null ? "AVAILABLE" : i.getStatus());
        i.setDeleted(0);
        i.setVersion(0);
        i.setTotalRentDays(i.getTotalRentDays() == null ? 0 : i.getTotalRentDays());
        i.setRepairCount(i.getRepairCount() == null ? 0 : i.getRepairCount());
        i.setCreateTime(LocalDateTime.now());
        i.setUpdateTime(LocalDateTime.now());
        instrumentMapper.insert(i);
        return R.ok(i.getId());
    }

    @PutMapping("/instrument/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Instrument i) {
        Instrument old = instrumentMapper.selectById(id);
        if (old == null) throw new BizException("乐器不存在");
        // 编号被业务单据引用后不允许改，避免历史单据对不上
        if (notBlank(i.getInstrumentNo()) && !i.getInstrumentNo().trim().equals(old.getInstrumentNo())) {
            long used = orderMapper.selectCount(new QueryWrapper<RentalOrder>().eq("instrument_id", id));
            if (used > 0) throw new BizException("该乐器已有租赁单据，编号不可修改");
        }
        validate(i);
        i.setId(id);
        i.setUpdateTime(LocalDateTime.now());
        instrumentMapper.updateById(i);
        return R.ok();
    }

    /** 逻辑删除前检查引用 */
    @DeleteMapping("/instrument/{id}")
    public R<Void> delete(@PathVariable Long id) {
        long used = orderMapper.selectCount(new QueryWrapper<RentalOrder>().eq("instrument_id", id))
                + repairMapper.selectCount(new QueryWrapper<RepairOrder>().eq("instrument_id", id));
        if (used > 0) throw new BizException("该乐器已被业务单据引用，不能删除");
        instrumentMapper.deleteById(id);
        return R.ok();
    }

    private void validate(Instrument i) {
        if (!notBlank(i.getInstrumentNo())) throw new BizException("乐器编号必填");
        if (i.getInstrumentNo().trim().length() > 50) throw new BizException("乐器编号不能超过 50 个字符");
        if (!notBlank(i.getName())) throw new BizException("乐器名称必填");
        if (!notBlank(i.getCategory())) throw new BizException("乐器类别必选");
        if (!notBlank(i.getBrand()) && !notBlank(i.getModel())) throw new BizException("品牌型号必填");
        if (i.getImage() != null && i.getImage().startsWith("http")) throw new BizException("乐器图片必须本地上传，禁止外链");
    }

    // ---------------- 价格方案 ----------------
    @GetMapping("/price/list")
    public R<List<RentalPrice>> priceList() {
        return R.ok(priceMapper.selectList(new QueryWrapper<RentalPrice>().orderByAsc("id")));
    }

    @PostMapping("/price")
    public R<Long> priceSave(@RequestBody RentalPrice p) {
        if (!notBlank(p.getPlanName())) throw new BizException("方案名称必填");
        if (p.getDayPrice() == null || p.getDayPrice().compareTo(BigDecimal.ZERO) <= 0) throw new BizException("日租金必须大于 0");
        if (p.getMinDays() != null && p.getMaxDays() != null && p.getMaxDays() < p.getMinDays()) {
            throw new BizException("最长租期不能小于最短租期");
        }
        p.setCreateTime(LocalDateTime.now());
        priceMapper.insert(p);
        return R.ok(p.getId());
    }

    @PutMapping("/price/{id}")
    public R<Void> priceUpdate(@PathVariable Long id, @RequestBody RentalPrice p) {
        p.setId(id);
        priceMapper.updateById(p);
        return R.ok();
    }

    // ---------------- 保养计划 ----------------
    @GetMapping("/maintain/page")
    public R<Map<String, Object>> maintainPage(@RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize,
                                               @RequestParam(required = false) String status) {
        QueryWrapper<MaintainPlan> qw = new QueryWrapper<>();
        if (notBlank(status)) qw.eq("status", status);
        qw.orderByAsc("plan_date");
        Page<MaintainPlan> p = maintainMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (MaintainPlan m : p.getRecords()) {
            Map<String, Object> one = new LinkedHashMap<>();
            one.put("plan", m);
            Instrument i = instrumentMapper.selectById(m.getInstrumentId());
            one.put("instrumentNo", i == null ? "-" : i.getInstrumentNo());
            one.put("instrumentName", i == null ? "-" : i.getName());
            one.put("location", i == null ? "-" : i.getLocation());
            one.put("lastMaintain", i == null ? null : i.getLastMaintainDate());
            rows.add(one);
        }
        return R.ok(pageOf(rows, p.getTotal(), pageNum, pageSize));
    }

    /** 执行保养：写入完成日期，并把乐器置回可租 */
    @PostMapping("/maintain/{id}/execute")
    public R<Void> executeMaintain(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        MaintainPlan m = maintainMapper.selectById(id);
        if (m == null) throw new BizException("保养计划不存在");
        if ("DONE".equals(m.getStatus())) throw new BizException("该保养计划已完成");
        m.setStatus("DONE");
        m.setFinishDate(LocalDate.now());
        m.setExecutor(body != null && body.get("executor") != null ? body.get("executor") : CurrentUser.get().getRealName());
        maintainMapper.updateById(m);

        Instrument i = instrumentMapper.selectById(m.getInstrumentId());
        if (i != null) {
            i.setLastMaintainDate(LocalDate.now());
            if ("MAINTAINING".equals(i.getStatus())) i.setStatus("AVAILABLE");
            i.setUpdateTime(LocalDateTime.now());
            instrumentMapper.updateById(i);
        }
        return R.ok();
    }

    @PostMapping("/maintain")
    public R<Long> createMaintain(@RequestBody MaintainPlan m) {
        if (m.getInstrumentId() == null) throw new BizException("请选择乐器");
        if (m.getPlanDate() == null) throw new BizException("计划日期必填");
        m.setStatus("PLAN");
        maintainMapper.insert(m);
        return R.ok(m.getId());
    }

    // ---------------- 上传 ----------------
    @PostMapping("/upload")
    public R<Map<String, String>> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(defaultValue = "common") String type) {
        Map<String, String> saved = uploadService.save(file, type);
        SysFile f = new SysFile();
        f.setBizType(type);
        f.setFileName(saved.get("originName"));
        f.setFilePath(saved.get("url"));
        f.setFileSize(Long.parseLong(saved.get("size")));
        f.setUploader(CurrentUser.get().getRealName());
        f.setCreateTime(LocalDateTime.now());
        fileMapper.insert(f);
        return R.ok(saved);
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    static Map<String, Object> pageOf(List<?> records, long total, int pageNum, int pageSize) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", records);
        res.put("total", total);
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return res;
    }
}

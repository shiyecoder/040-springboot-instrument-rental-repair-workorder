package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.rental.common.BizException;
import com.music.rental.common.CurrentUser;
import com.music.rental.common.R;
import com.music.rental.entity.ScoreRecord;
import com.music.rental.entity.ScoreRule;
import com.music.rental.mapper.ScoreRecordMapper;
import com.music.rental.mapper.ScoreRuleMapper;
import com.music.rental.service.ScoreService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/** 创新点：规则配置、评分计算留痕、结果解释、人工修正、效果统计 */
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    private final ScoreService scoreService;
    private final ScoreRuleMapper ruleMapper;
    private final ScoreRecordMapper recordMapper;

    public ScoreController(ScoreService scoreService, ScoreRuleMapper ruleMapper, ScoreRecordMapper recordMapper) {
        this.scoreService = scoreService;
        this.ruleMapper = ruleMapper;
        this.recordMapper = recordMapper;
    }

    @GetMapping("/rule/list")
    public R<List<ScoreRule>> rules() {
        return R.ok(ruleMapper.selectList(new QueryWrapper<ScoreRule>().orderByDesc("id")));
    }

    @GetMapping("/rule/current")
    public R<ScoreRule> current() {
        return R.ok(scoreService.currentRule());
    }

    /** 发布新版本：权重合计必须 100%，记录版本号与生效时间 */
    @PostMapping("/rule")
    public R<Long> publish(@RequestBody ScoreRule r) {
        int sum = n(r.getWeightAppearance()) + n(r.getWeightPitch()) + n(r.getWeightPart())
                + n(r.getWeightRentDays()) + n(r.getWeightRepairCount()) + n(r.getWeightMaintain());
        if (sum != 100) throw new BizException("权重合计必须等于 100%，当前为 " + sum + "%");
        if (r.getThreshold() == null || r.getThreshold() < 0 || r.getThreshold() > 100) throw new BizException("出租阈值须在 0-100 之间");
        if (r.getRemark() == null || r.getRemark().length() < 5) throw new BizException("生效说明不少于 5 个字");
        if (r.getVersionNo() == null || r.getVersionNo().isBlank()) {
            long cnt = ruleMapper.selectCount(new QueryWrapper<>());
            r.setVersionNo("v" + (cnt + 1));
        }
        // 旧版本全部归档，只保留一个启用版本
        for (ScoreRule old : ruleMapper.selectList(new QueryWrapper<ScoreRule>().eq("enabled", 1))) {
            old.setEnabled(0);
            ruleMapper.updateById(old);
        }
        r.setId(null);
        r.setEnabled(1);
        r.setEffectTime(LocalDateTime.now());
        r.setCreateTime(LocalDateTime.now());
        ruleMapper.insert(r);
        return R.ok(r.getId());
    }

    @PostMapping("/evaluate/{instrumentId}")
    public R<Map<String, Object>> evaluate(@PathVariable Long instrumentId,
                                           @RequestParam(required = false) Long ruleId) {
        return R.ok(scoreService.evaluate(instrumentId, ruleId));
    }

    @GetMapping("/page")
    public R<Map<String, Object>> page(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) Long instrumentId,
                                       @RequestParam(required = false) String level) {
        QueryWrapper<ScoreRecord> qw = new QueryWrapper<>();
        if (instrumentId != null) qw.eq("instrument_id", instrumentId);
        if (level != null && !level.isBlank()) qw.eq("level", level);
        qw.orderByDesc("create_time");
        Page<ScoreRecord> p = recordMapper.selectPage(new Page<>(pageNum, pageSize), qw);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("records", p.getRecords());
        res.put("total", p.getTotal());
        res.put("pageNum", pageNum);
        res.put("pageSize", pageSize);
        return R.ok(res);
    }

    @GetMapping("/{id}")
    public R<ScoreRecord> detail(@PathVariable Long id) {
        ScoreRecord r = recordMapper.selectById(id);
        if (r == null) throw new BizException("评分记录不存在");
        return R.ok(r);
    }

    @GetMapping("/by-instrument/{instrumentId}")
    public R<ScoreRecord> latest(@PathVariable Long instrumentId) {
        return R.ok(recordMapper.selectOne(new QueryWrapper<ScoreRecord>()
                .eq("instrument_id", instrumentId).orderByDesc("create_time").last("limit 1")));
    }

    @PostMapping("/{id}/adjust")
    public R<Void> adjust(@PathVariable Long id, @RequestBody Map<String, String> body) {
        scoreService.adjust(id, body.get("manualResult"), body.get("reason"), CurrentUser.get().getRealName());
        return R.ok();
    }

    @GetMapping("/effect")
    public R<Map<String, Object>> effect() {
        return R.ok(scoreService.effect());
    }

    private int n(Integer v) {
        return v == null ? 0 : v;
    }
}

package com.music.rental.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.rental.common.BizException;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 创新点：乐器状态评分与维护周期。
 * 输入外观、音准、部件、累计租期、维修次数、保养间隔，按可配置权重加权，
 * 结果是「可解释的业务辅助」：保留每项输入与得分快照，允许人工修正但不覆盖原始结果。
 */
@Service
public class ScoreService {

    private final ScoreRuleMapper ruleMapper;
    private final ScoreRecordMapper recordMapper;
    private final InstrumentMapper instrumentMapper;
    private final RepairOrderMapper repairMapper;
    private final MaintainPlanMapper maintainMapper;

    public ScoreService(ScoreRuleMapper ruleMapper, ScoreRecordMapper recordMapper, InstrumentMapper instrumentMapper,
                        RepairOrderMapper repairMapper, MaintainPlanMapper maintainMapper) {
        this.ruleMapper = ruleMapper;
        this.recordMapper = recordMapper;
        this.instrumentMapper = instrumentMapper;
        this.repairMapper = repairMapper;
        this.maintainMapper = maintainMapper;
    }

    public ScoreRule currentRule() {
        ScoreRule r = ruleMapper.selectOne(new QueryWrapper<ScoreRule>().eq("enabled", 1).orderByDesc("id").last("limit 1"));
        if (r == null) throw new BizException("尚未启用评分规则");
        return r;
    }

    /** 计算：权重合计必须 100，缺失输入不参与计算并提示补录 */
    public Map<String, Object> evaluate(Long instrumentId, Long ruleId) {
        Instrument ins = instrumentMapper.selectById(instrumentId);
        if (ins == null) throw new BizException("乐器不存在");
        ScoreRule rule = ruleId == null ? currentRule() : ruleMapper.selectById(ruleId);
        if (rule == null) throw new BizException("规则版本不存在");

        int wSum = rule.getWeightAppearance() + rule.getWeightPitch() + rule.getWeightPart()
                + rule.getWeightRentDays() + rule.getWeightRepairCount() + rule.getWeightMaintain();
        if (wSum != 100) throw new BizException("权重合计必须等于 100%，当前为 " + wSum + "%");

        int appearance = ins.getAppearanceScore() == null ? -1 : ins.getAppearanceScore();
        int pitch = ins.getPitchScore() == null ? -1 : ins.getPitchScore();
        int part = ins.getPartScore() == null ? -1 : ins.getPartScore();
        int rentDays = ins.getTotalRentDays() == null ? 0 : ins.getTotalRentDays();
        int repairCount = ins.getRepairCount() == null ? 0 : ins.getRepairCount();
        int gap = ins.getLastMaintainDate() == null ? 365
                : (int) ChronoUnit.DAYS.between(ins.getLastMaintainDate(), LocalDate.now());

        List<String> missing = new ArrayList<>();
        if (appearance < 0) missing.add("外观评分");
        if (pitch < 0) missing.add("音准评分");
        if (part < 0) missing.add("部件状态");
        if (ins.getLastMaintainDate() == null) missing.add("最近保养日期");

        // 各因素归一到 0-100
        double sAppearance = Math.max(0, appearance);
        double sPitch = Math.max(0, pitch);
        double sPart = Math.max(0, part);
        double sRentDays = Math.max(0, 100 - rentDays / 6.0);
        double sRepair = Math.max(0, 100 - repairCount * 15.0);
        double sMaintain = Math.max(0, 100 - gap / 2.0);

        Map<String, Double> weights = new LinkedHashMap<>();
        weights.put("外观", rule.getWeightAppearance() / 100.0);
        weights.put("音准", rule.getWeightPitch() / 100.0);
        weights.put("部件", rule.getWeightPart() / 100.0);
        weights.put("累计租期", rule.getWeightRentDays() / 100.0);
        weights.put("维修次数", rule.getWeightRepairCount() / 100.0);
        weights.put("保养间隔", rule.getWeightMaintain() / 100.0);

        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("外观", sAppearance);
        scores.put("音准", sPitch);
        scores.put("部件", sPart);
        scores.put("累计租期", sRentDays);
        scores.put("维修次数", sRepair);
        scores.put("保养间隔", sMaintain);

        double total = 0;
        for (String k : weights.keySet()) total += scores.get(k) * weights.get(k);
        BigDecimal totalScore = BigDecimal.valueOf(total).setScale(1, RoundingMode.HALF_UP);

        String level = totalScore.doubleValue() >= 85 ? "RECOMMEND"
                : totalScore.doubleValue() >= 75 ? "OBSERVE"
                : totalScore.doubleValue() >= rule.getThreshold() ? "MAINTAIN" : "FORBID";
        String recommend = switch (level) {
            case "RECOMMEND" -> "建议出租";
            case "OBSERVE" -> "可出租，建议关注";
            case "MAINTAIN" -> "建议保养后再租";
            default -> "禁止出租";
        };

        // 主要影响因素：加权后损失最大的两项
        List<Map.Entry<String, Double>> loss = new ArrayList<>();
        for (String k : weights.keySet()) loss.add(Map.entry(k, (100 - scores.get(k)) * weights.get(k)));
        loss.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        String factors = loss.get(0).getKey() + "、" + loss.get(1).getKey();

        List<String> unmet = new ArrayList<>();
        if (totalScore.doubleValue() < rule.getThreshold()) unmet.add("总分低于出租阈值 " + rule.getThreshold());
        if (repairCount >= 4) unmet.add("累计维修 " + repairCount + " 次，超过 3 次上限");
        if (gap > 180) unmet.add("保养间隔 " + gap + " 天，超过 180 天");
        if (!missing.isEmpty()) unmet.add("缺少 " + String.join("、", missing) + "，结果仅供参考");

        List<String> improve = new ArrayList<>();
        if (gap > 60) improve.add("安排一次全面保养，可提升约 " + Math.min(8, gap / 30) + " 分");
        if (sPart < 80) improve.add("更换易损件（琴弦/皮垫/网皮）可提升部件得分");
        if (sRentDays < 70) improve.add("该乐器累计出租 " + rentDays + " 天，建议安排轮休或大保养");
        if (repairCount >= 2) improve.add("复核高频故障点，原因见维修工单");
        if (improve.isEmpty()) improve.add("保持当前保养节奏即可");

        // 明细快照：保留每一项输入、权重、得分，结果可复算
        StringBuilder json = new StringBuilder("{");
        String[] keys = {"appearance", "pitch", "part", "rentDays", "repairCount", "maintainGap"};
        String[] names = {"外观", "音准", "部件", "累计租期", "维修次数", "保养间隔"};
        int[] inputs = {appearance, pitch, part, rentDays, repairCount, gap};
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(keys[i]).append("\":{")
                    .append("\"name\":\"").append(names[i]).append("\",")
                    .append("\"input\":").append(inputs[i]).append(",")
                    .append("\"weight\":").append(String.format("%.2f", weights.get(names[i]))).append(",")
                    .append("\"score\":").append(String.format("%.1f", scores.get(names[i]))).append(",")
                    .append("\"weighted\":").append(String.format("%.1f", scores.get(names[i]) * weights.get(names[i])))
                    .append("}");
        }
        json.append("}");

        ScoreRecord rec = new ScoreRecord();
        rec.setInstrumentId(instrumentId);
        rec.setRuleId(rule.getId());
        rec.setRuleVersion(rule.getVersionNo());
        rec.setAppearanceInput(appearance < 0 ? null : appearance);
        rec.setPitchInput(pitch < 0 ? null : pitch);
        rec.setPartInput(part < 0 ? null : part);
        rec.setRentDaysInput(rentDays);
        rec.setRepairCountInput(repairCount);
        rec.setMaintainDaysInput(gap);
        rec.setDetailJson(json.toString());
        rec.setTotalScore(totalScore);
        rec.setLevel(level);
        rec.setRecommend(recommend);
        rec.setMainFactors(factors);
        rec.setUnmet(String.join("；", unmet));
        rec.setImprove(String.join("；", improve));
        rec.setCreateTime(LocalDateTime.now());
        recordMapper.insert(rec);

        // 回写主档，出租时按最新分判断
        ins.setScoreValue(totalScore);
        ins.setScoreLevel(level);
        ins.setUpdateTime(LocalDateTime.now());
        instrumentMapper.updateById(ins);

        // 低于阈值自动生成保养建议并停租
        if ("FORBID".equals(level)) {
            ins.setStatus("MAINTAINING");
            instrumentMapper.updateById(ins);
            MaintainPlan mp = new MaintainPlan();
            mp.setInstrumentId(instrumentId);
            mp.setPlanDate(LocalDate.now().plusDays(3));
            mp.setCycleDays(90);
            mp.setContent("状态分低于阈值，安排全面保养并复检");
            mp.setStatus("PLAN");
            mp.setRemark("系统按评分规则自动生成");
            maintainMapper.insert(mp);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("record", rec);
        res.put("detail", json.toString());
        res.put("missing", missing);
        res.put("rule", rule);
        return res;
    }

    /** 人工修正：原始结果不覆盖，只记录修正值与原因 */
    public void adjust(Long recordId, String manualResult, String reason, String operator) {
        if (reason == null || reason.length() < 5) throw new BizException("修正原因不少于 5 个字");
        ScoreRecord rec = recordMapper.selectById(recordId);
        if (rec == null) throw new BizException("评分记录不存在");
        rec.setManualResult(manualResult);
        rec.setAdjustReason(reason);
        rec.setAdjustBy(operator);
        rec.setAdjustTime(LocalDateTime.now());
        recordMapper.updateById(rec);
    }

    /** 看板效果：采纳率与调整率 */
    public Map<String, Object> effect() {
        List<ScoreRecord> all = recordMapper.selectList(new QueryWrapper<>());
        long adjusted = all.stream().filter(r -> r.getManualResult() != null && !r.getManualResult().isBlank()).count();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", all.size());
        res.put("adjusted", adjusted);
        res.put("adjustRate", all.isEmpty() ? 0 : Math.round(adjusted * 1000.0 / all.size()) / 10.0);
        res.put("adoptRate", all.isEmpty() ? 0 : Math.round((all.size() - adjusted) * 1000.0 / all.size()) / 10.0);
        Map<String, Long> dist = new LinkedHashMap<>();
        for (String lv : List.of("RECOMMEND", "OBSERVE", "MAINTAIN", "FORBID")) {
            dist.put(lv, all.stream().filter(r -> lv.equals(r.getLevel())).count());
        }
        res.put("dist", dist);
        return res;
    }
}

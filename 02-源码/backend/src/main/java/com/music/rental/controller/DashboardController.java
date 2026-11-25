package com.music.rental.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.rental.common.R;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** 资产经营看板：6 个核心指标 + 6 个图表，全部后端聚合，前端不再二次统计 */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final InstrumentMapper instrumentMapper;
    private final RentalOrderMapper orderMapper;
    private final RepairOrderMapper repairMapper;
    private final ScoreRecordMapper scoreMapper;
    private final RentalPriceMapper priceMapper;
    private final MaintainPlanMapper maintainMapper;

    public DashboardController(InstrumentMapper instrumentMapper, RentalOrderMapper orderMapper,
                               RepairOrderMapper repairMapper, ScoreRecordMapper scoreMapper,
                               RentalPriceMapper priceMapper, MaintainPlanMapper maintainMapper) {
        this.instrumentMapper = instrumentMapper;
        this.orderMapper = orderMapper;
        this.repairMapper = repairMapper;
        this.scoreMapper = scoreMapper;
        this.priceMapper = priceMapper;
        this.maintainMapper = maintainMapper;
    }

    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard(@RequestParam(required = false, defaultValue = "30") String range) {
        List<Instrument> instruments = instrumentMapper.selectList(new QueryWrapper<>());
        List<RentalOrder> orders = orderMapper.selectList(new QueryWrapper<>());
        LocalDateTime from = switch (range) {
            case "quarter" -> LocalDate.now().minusMonths(3).atStartOfDay();
            case "year" -> LocalDate.now().withDayOfYear(1).atStartOfDay();
            case "month" -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
            default -> LocalDateTime.now().minusDays(30);
        };
        List<RentalOrder> recent = orders.stream().filter(o -> o.getCreateTime() != null && o.getCreateTime().isAfter(from)).toList();

        long available = instruments.stream().filter(i -> "AVAILABLE".equals(i.getStatus())).count();
        long renting = instruments.stream().filter(i -> "RENTING".equals(i.getStatus())).count();
        long repairing = repairMapper.selectCount(new QueryWrapper<RepairOrder>()
                .in("status", List.of("PENDING", "DIAGNOSING", "REPAIRING", "TESTING")));
        long overdue = orders.stream().filter(o -> "RENTING".equals(o.getStatus()) && o.getEndDate() != null
                && o.getEndDate().isBefore(LocalDate.now())).count();
        double avgScore = instruments.stream().filter(i -> i.getScoreValue() != null)
                .mapToDouble(i -> i.getScoreValue().doubleValue()).average().orElse(0);
        BigDecimal income = recent.stream().filter(o -> !"REJECTED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .map(o -> o.getRentAmount() == null ? BigDecimal.ZERO : o.getRentAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("availableRate", instruments.isEmpty() ? 0 : Math.round(available * 1000.0 / instruments.size()) / 10.0);
        kpi.put("rentCount", recent.stream().filter(o -> !"REJECTED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus())).count());
        kpi.put("overdue", overdue);
        kpi.put("repairing", repairing);
        kpi.put("avgScore", Math.round(avgScore * 10) / 10.0);
        kpi.put("income", income.setScale(2, RoundingMode.HALF_UP));

        // 趋势：近 10 天的租赁单量
        Map<String, Long> trendMap = new LinkedHashMap<>();
        for (int i = 9; i >= 0; i--) trendMap.put(LocalDate.now().minusDays(i).toString().substring(5), 0L);
        for (RentalOrder o : orders) {
            if (o.getCreateTime() == null) continue;
            String k = o.getCreateTime().toLocalDate().toString().substring(5);
            if (trendMap.containsKey(k)) trendMap.put(k, trendMap.get(k) + 1);
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        trendMap.forEach((k, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", k);
            m.put("value", v);
            trend.add(m);
        });

        // 状态分布
        List<Map<String, Object>> statusDist = new ArrayList<>();
        String[][] st = {{"AVAILABLE", "可租"}, {"RENTING", "租用中"}, {"REPAIRING", "维修中"},
                {"MAINTAINING", "保养中"}, {"RESERVED", "已预留"}, {"SCRAPPED", "已报废"}};
        for (String[] s : st) {
            long c = instruments.stream().filter(i -> s[0].equals(i.getStatus())).count();
            if (c == 0) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s[1]);
            m.put("value", c);
            statusDist.add(m);
        }

        // 类别租赁次数
        Map<String, Long> catMap = new LinkedHashMap<>();
        for (RentalOrder o : orders) {
            Instrument i = instruments.stream().filter(x -> x.getId().equals(o.getInstrumentId())).findFirst().orElse(null);
            if (i == null) continue;
            catMap.merge(i.getCategory(), 1L, Long::sum);
        }
        List<Map<String, Object>> byCategory = toList(catMap);
        byCategory.sort((a, b) -> Long.compare((Long) b.get("value"), (Long) a.get("value")));

        // 责任人排行
        Map<String, Long> staffMap = new LinkedHashMap<>();
        for (RentalOrder o : orders) {
            if (o.getHandlerName() == null) continue;
            staffMap.merge(o.getHandlerName(), 1L, Long::sum);
        }
        List<Map<String, Object>> byStaff = toList(staffMap);

        // 逾期率与维修费用按故障类型
        Map<String, Long> faultMap = new LinkedHashMap<>();
        for (RepairOrder r : repairMapper.selectList(new QueryWrapper<>())) {
            String key = r.getFaultDesc() == null ? "其他" : r.getFaultDesc().substring(0, Math.min(4, r.getFaultDesc().length()));
            faultMap.merge(key, 1L, Long::sum);
        }
        List<Map<String, Object>> byFault = toList(faultMap);

        // 评分结果分布
        Map<String, Long> scoreMap = new LinkedHashMap<>();
        String[][] lv = {{"RECOMMEND", "推荐出租"}, {"OBSERVE", "观察"}, {"MAINTAIN", "待保养"}, {"FORBID", "禁止出租"}};
        List<ScoreRecord> records = scoreMapper.selectList(new QueryWrapper<>());
        for (String[] l : lv) {
            long c = records.stream().filter(r -> l[0].equals(r.getLevel())).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", l[1]);
            m.put("value", c);
            scoreMap.put(l[1], c);
        }
        List<Map<String, Object>> byScore = toList(scoreMap);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("kpi", kpi);
        res.put("trend", trend);
        res.put("statusDist", statusDist);
        res.put("byCategory", byCategory);
        res.put("byStaff", byStaff);
        res.put("byFault", byFault);
        res.put("byScore", byScore);
        res.put("maintainDue", maintainMapper.selectCount(new QueryWrapper<MaintainPlan>().ne("status", "DONE")));
        return R.ok(res);
    }

    private List<Map<String, Object>> toList(Map<String, Long> map) {
        List<Map<String, Object>> list = new ArrayList<>();
        map.forEach((k, v) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", k);
            m.put("value", v);
            list.add(m);
        });
        return list;
    }
}

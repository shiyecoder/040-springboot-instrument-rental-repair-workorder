package com.music.rental.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.rental.common.BizException;
import com.music.rental.entity.*;
import com.music.rental.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 租赁核心规则：租金/逾期费精确计算、可用时段冲突校验、状态机校验。
 * 所有金额一律后端计算，前端只展示，不允许前端传金额覆盖。
 */
@Service
public class RentalService {

    private final RentalPriceMapper priceMapper;
    private final RentalOrderMapper orderMapper;
    private final RepairOrderMapper repairMapper;
    private final MaintainPlanMapper maintainMapper;
    private final InstrumentMapper instrumentMapper;

    public RentalService(RentalPriceMapper priceMapper, RentalOrderMapper orderMapper, RepairOrderMapper repairMapper,
                         MaintainPlanMapper maintainMapper, InstrumentMapper instrumentMapper) {
        this.priceMapper = priceMapper;
        this.orderMapper = orderMapper;
        this.repairMapper = repairMapper;
        this.maintainMapper = maintainMapper;
        this.instrumentMapper = instrumentMapper;
    }

    public RentalPrice priceOf(String category) {
        RentalPrice p = priceMapper.selectOne(new QueryWrapper<RentalPrice>()
                .eq("category", category).eq("status", 1).last("limit 1"));
        if (p == null) p = priceMapper.selectOne(new QueryWrapper<RentalPrice>().eq("status", 1).last("limit 1"));
        if (p == null) throw new BizException("未配置 " + category + " 类别的价格方案");
        return p;
    }

    /** 租期天数包含起止两天，最短/最长租期按价格方案校验 */
    public int days(LocalDate start, LocalDate end) {
        if (start == null || end == null) throw new BizException("起租日与归还日必填");
        long d = ChronoUnit.DAYS.between(start, end) + 1;
        if (d <= 0) throw new BizException("归还日必须晚于起租日");
        return (int) d;
    }

    public BigDecimal rentOf(RentalPrice p, int days) {
        BigDecimal rent;
        if (days >= 30) {
            rent = p.getMonthPrice().multiply(BigDecimal.valueOf(days / 30));
            rent = rent.add(p.getDayPrice().multiply(BigDecimal.valueOf(days % 30)));
            rent = rent.multiply(new BigDecimal("0.95")); // 长约折扣
        } else if (days >= 7) {
            rent = p.getWeekPrice().multiply(BigDecimal.valueOf(days / 7))
                    .add(p.getDayPrice().multiply(BigDecimal.valueOf(days % 7)));
        } else {
            rent = p.getDayPrice().multiply(BigDecimal.valueOf(days));
        }
        return rent.setScale(2, RoundingMode.HALF_UP);
    }

    /** 逾期费 = 逾期天数 × 日租金 × 逾期费率 */
    public BigDecimal overdueFee(RentalPrice p, LocalDate end, LocalDate actual) {
        if (actual == null || !actual.isAfter(end)) return BigDecimal.ZERO;
        long od = ChronoUnit.DAYS.between(end, actual);
        return p.getDayPrice().multiply(BigDecimal.valueOf(od)).multiply(p.getOverdueRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 可用时段冲突校验：已租/预留、维修、保养三类占用不能重叠。
     * 返回空串表示无冲突，否则返回中文原因。
     */
    public String conflict(Long instrumentId, LocalDate start, LocalDate end, Long excludeOrderId) {
        List<RentalOrder> orders = orderMapper.selectList(new QueryWrapper<RentalOrder>()
                .eq("instrument_id", instrumentId)
                .in("status", List.of("PENDING_DEPOSIT", "PENDING_HANDOVER", "RENTING", "PENDING_RETURN")));
        for (RentalOrder o : orders) {
            if (excludeOrderId != null && excludeOrderId.equals(o.getId())) continue;
            if (!end.isBefore(o.getStartDate()) && !start.isAfter(o.getEndDate())) {
                return "该乐器在 " + o.getStartDate() + " ~ " + o.getEndDate() + " 已被单据 " + o.getOrderNo() + " 占用";
            }
        }
        List<RepairOrder> repairs = repairMapper.selectList(new QueryWrapper<RepairOrder>()
                .eq("instrument_id", instrumentId)
                .in("status", List.of("PENDING", "DIAGNOSING", "REPAIRING", "TESTING")));
        if (!repairs.isEmpty()) return "该乐器存在未完成的维修工单 " + repairs.get(0).getRepairNo() + "，暂不可租";

        List<MaintainPlan> plans = maintainMapper.selectList(new QueryWrapper<MaintainPlan>()
                .eq("instrument_id", instrumentId).ne("status", "DONE"));
        for (MaintainPlan m : plans) {
            if (m.getPlanDate() != null && !m.getPlanDate().isBefore(start) && !m.getPlanDate().isAfter(end)) {
                return "该乐器 " + m.getPlanDate() + " 已安排保养，时段冲突";
            }
        }
        return "";
    }

    /** 出租前置校验：状态分低于阈值禁止出租（创新点落地） */
    public void checkRentable(Instrument i) {
        if (i == null) throw new BizException("乐器不存在");
        if ("SCRAPPED".equals(i.getStatus())) throw new BizException("该乐器已报废，不可出租");
        if ("REPAIRING".equals(i.getStatus())) throw new BizException("该乐器正在维修，不可出租");
        if ("MAINTAINING".equals(i.getStatus())) throw new BizException("该乐器正在保养，不可出租");
        if (i.getScoreValue() != null && i.getScoreValue().compareTo(new BigDecimal("60")) < 0) {
            throw new BizException("该乐器状态分 " + i.getScoreValue() + " 低于出租阈值 60，需先保养");
        }
    }

    /** 状态机：只允许文档规定的正向流转 */
    public void assertTransition(String from, String to) {
        Map<String, List<String>> flow = Map.of(
                "PENDING_AUDIT", List.of("PENDING_DEPOSIT", "REJECTED", "CANCELLED"),
                "PENDING_DEPOSIT", List.of("PENDING_HANDOVER", "REJECTED", "CANCELLED"),
                "PENDING_HANDOVER", List.of("RENTING", "CANCELLED"),
                "RENTING", List.of("PENDING_RETURN"),
                "PENDING_RETURN", List.of("COMPLETED")
        );
        List<String> allow = flow.getOrDefault(from, List.of());
        if (!allow.contains(to)) throw new BizException("不允许从「" + from + "」流转到「" + to + "」");
    }

    public Instrument instrumentOf(Long id) {
        Instrument i = instrumentMapper.selectById(id);
        if (i == null) throw new BizException("乐器不存在");
        return i;
    }
}

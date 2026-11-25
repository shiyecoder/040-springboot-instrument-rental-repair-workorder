package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 维修工单 */
@Data
@TableName("repair_order")
public class RepairOrder {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 工单号 */
    private String repairNo;
    /** 乐器 */
    private Long instrumentId;
    /** 关联租赁订单 */
    private Long orderId;
    /** 故障描述 */
    private String faultDesc;
    /** 故障照片 */
    private String faultImages;
    /** 检测结论 */
    private String diagnosis;
    /** 预估费用 */
    private BigDecimal estimatedCost;
    /** 实际费用 */
    private BigDecimal actualCost;
    /** 实际处理 */
    private String handleDesc;
    /** 试奏检验 */
    private String testResult;
    /** 工单状态 */
    private String status;
    /** 技师 */
    private Long technicianId;
    /** 技师姓名 */
    private String technicianName;
    /** RENTAL 租期报修 / STORE 门店送修 */
    private String source;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 完工时间 */
    private LocalDateTime finishTime;
    /** 验收时间 */
    private LocalDateTime acceptTime;
}

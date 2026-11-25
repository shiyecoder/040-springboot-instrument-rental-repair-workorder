package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 归还验收 */
@Data
@TableName("return_inspection")
public class ReturnInspection {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 */
    private Long orderId;
    /** 功能检查 */
    private String functionCheck;
    /** 外观检查 */
    private String appearanceCheck;
    /** 配件检查 */
    private String accessoryCheck;
    /** 清洁情况 */
    private String cleanCheck;
    /** 逾期天数 */
    private Integer overdueDays;
    /** 逾期费 */
    private BigDecimal overdueFee;
    /** PASS/DAMAGE/REPAIR */
    private String result;
    /** 损坏描述 */
    private String damageDesc;
    /** 归还照片 */
    private String photos;
    /** 验收人 */
    private String inspector;
    /** 验收时间 */
    private LocalDateTime inspectTime;
}

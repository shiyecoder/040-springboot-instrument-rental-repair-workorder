package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 损坏赔偿 */
@Data
@TableName("damage_claim")
public class DamageClaim {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 */
    private Long orderId;
    /** 损坏点 */
    private String damagePoint;
    /** 缺失配件 */
    private String missingAccessory;
    /** 维修成本 */
    private BigDecimal repairCost;
    /** 责任结论 */
    private String liability;
    /** 处理结论 */
    private String conclusion;
    /** 赔偿金额 */
    private BigDecimal amount;
    /** PAID/UNPAID */
    private String payStatus;
    /** 收款凭证 */
    private String payProof;
    /** 经办人 */
    private String operator;
    /** 创建时间 */
    private LocalDateTime createTime;
}

package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 续租申请 */
@Data
@TableName("renewal_application")
public class RenewalApplication {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 */
    private Long orderId;
    /** 原归还日 */
    private LocalDate oldEndDate;
    /** 新归还日 */
    private LocalDate newEndDate;
    /** 补租金 */
    private BigDecimal extraRent;
    /** 1 存在冲突 */
    private Integer conflictFlag;
    /** 冲突原因 */
    private String conflictReason;
    /** PENDING/APPROVED/REJECTED */
    private String status;
    /** 申请说明 */
    private String applyReason;
    /** 申请时间 */
    private LocalDateTime applyTime;
    /** 审核时间 */
    private LocalDateTime auditTime;
    /** 审核人 */
    private String auditor;
    /** 审核意见 */
    private String auditRemark;
}

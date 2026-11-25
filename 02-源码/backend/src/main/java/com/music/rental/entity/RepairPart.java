package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 维修零件领用 */
@Data
@TableName("repair_part")
public class RepairPart {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 工单 */
    private Long repairId;
    /** 零件名称 */
    private String partName;
    /** 数量 */
    private Integer quantity;
    /** 单价 */
    private BigDecimal unitPrice;
    /** 金额 */
    private BigDecimal amount;
    /** 领用时间 */
    private LocalDateTime createTime;
}

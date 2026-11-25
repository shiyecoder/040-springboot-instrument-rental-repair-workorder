package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 租赁价格方案 */
@Data
@TableName("rental_price")
public class RentalPrice {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 方案名称 */
    private String planName;
    /** 适用类别 */
    private String category;
    /** 日租金 */
    private BigDecimal dayPrice;
    /** 周租金 */
    private BigDecimal weekPrice;
    /** 月租金 */
    private BigDecimal monthPrice;
    /** 押金标准 */
    private BigDecimal deposit;
    /** 最短租期 */
    private Integer minDays;
    /** 最长租期 */
    private Integer maxDays;
    /** 逾期费率倍数 */
    private BigDecimal overdueRate;
    /** 赠送配件 */
    private String giftAccessory;
    /** 1 启用 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
}

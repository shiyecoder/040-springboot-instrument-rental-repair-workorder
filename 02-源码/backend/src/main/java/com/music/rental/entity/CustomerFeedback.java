package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 客户评价 */
@Data
@TableName("customer_feedback")
public class CustomerFeedback {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 */
    private Long orderId;
    /** 乐器 */
    private Long instrumentId;
    /** 客户 */
    private Long customerId;
    /** 评分 1-5 */
    private Integer rating;
    /** 评价内容 */
    private String content;
    /** 评价时间 */
    private LocalDateTime createTime;
}

package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 租赁与维修工单 */
@Data
@TableName("rental_order")
public class RentalOrder {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单号 */
    private String orderNo;
    /** 乐器 */
    private Long instrumentId;
    /** 客户 */
    private Long customerId;
    /** 客户姓名 */
    private String customerName;
    /** 起租日 */
    private LocalDate startDate;
    /** 应归还日 */
    private LocalDate endDate;
    /** 用途 */
    private String purpose;
    /** 自提/配送 */
    private String deliveryType;
    /** 需要配件 */
    private String needAccessory;
    /** 身份证明类型 */
    private String idProof;
    /** 身份证明照片 */
    private String idProofImage;
    /** 押金 */
    private BigDecimal depositAmount;
    /** 租金 */
    private BigDecimal rentAmount;
    /** 逾期费 */
    private BigDecimal overdueFee;
    /** 赔偿金 */
    private BigDecimal compensateAmount;
    /** 押金状态 */
    private String depositStatus;
    /** 订单状态 */
    private String status;
    /** 受理营业员 */
    private Long handlerId;
    /** 受理营业员姓名 */
    private String handlerName;
    /** 维修技师 */
    private Long technicianId;
    /** 维修技师姓名 */
    private String technicianName;
    /** 备注 */
    private String remark;
    /** 乐器快照 */
    private String image;
    /** 提交时间 */
    private LocalDateTime applyTime;
    /** 出库时间 */
    private LocalDateTime handoverTime;
    /** 实际归还时间 */
    private LocalDateTime actualReturnTime;
    /** 逻辑删除 */
    private Integer deleted;
    /** 版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}

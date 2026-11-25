package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 出库/归还交接单 */
@Data
@TableName("rental_handover")
public class RentalHandover {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 订单 */
    private Long orderId;
    /** OUT 出库 / RETURN 归还 */
    private String handoverType;
    /** 外观检查 */
    private String appearanceCheck;
    /** 功能检查 */
    private String functionCheck;
    /** 编号核对 */
    private String instrumentNoCheck;
    /** 配件清单 */
    private String accessoryList;
    /** 已有划痕 */
    private String scratches;
    /** 交接照片 */
    private String photos;
    /** 客户确认 */
    private Integer customerConfirm;
    /** 店员确认 */
    private Integer staffConfirm;
    /** 经办人 */
    private String operator;
    /** 交接时间 */
    private LocalDateTime handoverTime;
    /** 备注 */
    private String remark;
}

package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/** 乐器配件 */
@Data
@TableName("instrument_accessory")
public class InstrumentAccessory {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 乐器 */
    private Long instrumentId;
    /** 配件名称 */
    private String accessoryName;
    /** 规格 */
    private String spec;
    /** 数量 */
    private Integer quantity;
    /** 状态 */
    private String status;
    /** 备注 */
    private String remark;
}

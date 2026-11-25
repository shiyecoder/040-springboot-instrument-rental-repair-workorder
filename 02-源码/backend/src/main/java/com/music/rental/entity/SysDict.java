package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/** 字典 */
@Data
@TableName("sys_dict")
public class SysDict {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 字典类型 */
    private String dictType;
    /** 显示名 */
    private String dictLabel;
    /** 值 */
    private String dictValue;
    /** 排序 */
    private Integer sort;
}

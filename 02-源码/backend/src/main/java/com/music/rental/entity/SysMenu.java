package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/** 菜单 */
@Data
@TableName("sys_menu")
public class SysMenu {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 父级 */
    private Long parentId;
    /** 菜单名称 */
    private String menuName;
    /** 路由地址 */
    private String path;
    /** 图标 */
    private String icon;
    /** 排序 */
    private Integer sort;
}

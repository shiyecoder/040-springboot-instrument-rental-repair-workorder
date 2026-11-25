package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/** 角色菜单 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色 */
    private Long roleId;
    /** 菜单 */
    private Long menuId;
}

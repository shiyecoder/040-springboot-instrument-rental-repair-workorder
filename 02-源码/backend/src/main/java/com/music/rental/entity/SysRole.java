package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


/** 角色 */
@Data
@TableName("sys_role")
public class SysRole {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;
    /** ALL 全部 / SELF 本人 */
    private String dataScope;
    /** 说明 */
    private String remark;
}

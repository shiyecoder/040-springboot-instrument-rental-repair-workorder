package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户 */
@Data
@TableName("sys_user")
public class SysUser {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 登录账号 */
    private String username;
    /** BCrypt 密码 */
    private String password;
    /** 姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像 */
    private String avatar;
    /** 角色编码 */
    private String roleCode;
    /** 所属门店 */
    private String storeName;
    /** 1 启用 0 停用 */
    private Integer status;
    /** 最近登录 */
    private LocalDateTime lastLogin;
    /** 创建时间 */
    private LocalDateTime createTime;
}

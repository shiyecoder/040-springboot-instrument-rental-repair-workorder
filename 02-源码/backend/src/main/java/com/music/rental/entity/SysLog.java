package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 操作日志 */
@Data
@TableName("sys_log")
public class SysLog {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作人 */
    private Long userId;
    /** 登录名 */
    private String username;
    /** 模块 */
    private String module;
    /** 动作 */
    private String action;
    /** 业务编号 */
    private String bizNo;
    /** 内容 */
    private String content;
    /** 时间 */
    private LocalDateTime createTime;
}

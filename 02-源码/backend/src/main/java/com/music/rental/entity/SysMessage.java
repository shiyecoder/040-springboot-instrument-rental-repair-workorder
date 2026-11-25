package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 站内消息 */
@Data
@TableName("sys_message")
public class SysMessage {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 接收人 */
    private Long userId;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 业务编号 */
    private String bizNo;
    /** 业务类型 */
    private String bizType;
    /** 跳转地址 */
    private String link;
    /** 0 未读 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createTime;
}

package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 附件 */
@Data
@TableName("sys_file")
public class SysFile {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 业务类型 */
    private String bizType;
    /** 业务主键 */
    private Long bizId;
    /** 原始文件名 */
    private String fileName;
    /** 相对路径 */
    private String filePath;
    /** 大小 */
    private Long fileSize;
    /** 上传人 */
    private String uploader;
    /** 上传时间 */
    private LocalDateTime createTime;
}

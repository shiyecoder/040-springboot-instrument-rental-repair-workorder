package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

/** 保养计划 */
@Data
@TableName("maintain_plan")
public class MaintainPlan {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 乐器 */
    private Long instrumentId;
    /** 计划日期 */
    private LocalDate planDate;
    /** 保养周期 */
    private Integer cycleDays;
    /** 保养内容 */
    private String content;
    /** PLAN/DUE/DONE */
    private String status;
    /** 完成日期 */
    private LocalDate finishDate;
    /** 执行人 */
    private String executor;
    /** 备注 */
    private String remark;
}

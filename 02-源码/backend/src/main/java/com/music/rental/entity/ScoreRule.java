package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 评分规则 */
@Data
@TableName("score_rule")
public class ScoreRule {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 规则版本 */
    private String versionNo;
    /** 外观权重 */
    private Integer weightAppearance;
    /** 音准权重 */
    private Integer weightPitch;
    /** 部件权重 */
    private Integer weightPart;
    /** 累计租期权重 */
    private Integer weightRentDays;
    /** 维修次数权重 */
    private Integer weightRepairCount;
    /** 保养间隔权重 */
    private Integer weightMaintain;
    /** 出租阈值 */
    private Integer threshold;
    /** 1 启用 */
    private Integer enabled;
    /** 生效时间 */
    private LocalDateTime effectTime;
    /** 说明 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
}

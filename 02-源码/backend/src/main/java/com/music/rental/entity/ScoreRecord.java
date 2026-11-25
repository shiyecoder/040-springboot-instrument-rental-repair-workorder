package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 评分计算留痕 */
@Data
@TableName("score_record")
public class ScoreRecord {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 乐器 */
    private Long instrumentId;
    /** 规则 */
    private Long ruleId;
    /** 规则版本 */
    private String ruleVersion;
    /** 外观输入 */
    private Integer appearanceInput;
    /** 音准输入 */
    private Integer pitchInput;
    /** 部件输入 */
    private Integer partInput;
    /** 累计租期输入 */
    private Integer rentDaysInput;
    /** 维修次数输入 */
    private Integer repairCountInput;
    /** 保养间隔输入 */
    private Integer maintainDaysInput;
    /** 明细快照 JSON */
    private String detailJson;
    /** 总分 */
    private BigDecimal totalScore;
    /** 等级 */
    private String level;
    /** 推荐结果 */
    private String recommend;
    /** 主要影响因素 */
    private String mainFactors;
    /** 未满足条件 */
    private String unmet;
    /** 可改进项 */
    private String improve;
    /** 人工修正结果 */
    private String manualResult;
    /** 修正原因 */
    private String adjustReason;
    /** 修正人 */
    private String adjustBy;
    /** 修正时间 */
    private LocalDateTime adjustTime;
    /** 计算时间 */
    private LocalDateTime createTime;
}

package com.music.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 乐器主档 */
@Data
@TableName("instrument")
public class Instrument {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 乐器编号（一物一码） */
    private String instrumentNo;
    /** 乐器名称 */
    private String name;
    /** 类别 */
    private String category;
    /** 品牌 */
    private String brand;
    /** 型号 */
    private String model;
    /** 级别 */
    private String level;
    /** 尺寸规格 */
    private String sizeSpec;
    /** 购置价 */
    private BigDecimal purchasePrice;
    /** 当前成色 */
    private String conditionLevel;
    /** 随附配件 */
    private String accessoryDesc;
    /** 存放仓位 */
    private String location;
    /** 租赁/维修状态 */
    private String status;
    /** 乐器图片 */
    private String image;
    /** 购置日期 */
    private LocalDate purchaseDate;
    /** 累计租期天数 */
    private Integer totalRentDays;
    /** 维修次数 */
    private Integer repairCount;
    /** 最近保养日期 */
    private LocalDate lastMaintainDate;
    /** 外观评分 */
    private Integer appearanceScore;
    /** 音准评分 */
    private Integer pitchScore;
    /** 部件状态评分 */
    private Integer partScore;
    /** 当前状态分 */
    private BigDecimal scoreValue;
    /** 当前评分等级 */
    private String scoreLevel;
    /** 逻辑删除 */
    private Integer deleted;
    /** 版本号 */
    private Integer version;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}

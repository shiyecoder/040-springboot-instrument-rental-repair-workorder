package com.music.rental.dto;

import lombok.Data;

/** 状态流转参数：目标动作 + 原因（退回/驳回/调整必填，不少于 5 字） */
@Data
public class TransitionDTO {
    private String action;
    private String reason;
    private String remark;
}

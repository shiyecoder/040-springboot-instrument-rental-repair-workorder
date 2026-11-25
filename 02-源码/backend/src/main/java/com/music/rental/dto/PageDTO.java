package com.music.rental.dto;

import lombok.Data;

/** 通用分页查询参数：服务端分页，页码从 1 开始 */
@Data
public class PageDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String category;
    private String status;
    private String location;
    private String suitPeople;
    private String dateFrom;
    private String dateTo;
    private Long receiverId;
}

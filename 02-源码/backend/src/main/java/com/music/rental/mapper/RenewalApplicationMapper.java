package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RenewalApplication;
import org.apache.ibatis.annotations.Mapper;

/** 续租申请 Mapper */
@Mapper
public interface RenewalApplicationMapper extends BaseMapper<RenewalApplication> {
}

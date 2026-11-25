package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.CustomerFeedback;
import org.apache.ibatis.annotations.Mapper;

/** 客户评价 Mapper */
@Mapper
public interface CustomerFeedbackMapper extends BaseMapper<CustomerFeedback> {
}

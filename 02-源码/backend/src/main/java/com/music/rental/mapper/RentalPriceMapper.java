package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RentalPrice;
import org.apache.ibatis.annotations.Mapper;

/** 租赁价格方案 Mapper */
@Mapper
public interface RentalPriceMapper extends BaseMapper<RentalPrice> {
}

package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RentalOrder;
import org.apache.ibatis.annotations.Mapper;

/** 租赁与维修工单 Mapper */
@Mapper
public interface RentalOrderMapper extends BaseMapper<RentalOrder> {
}

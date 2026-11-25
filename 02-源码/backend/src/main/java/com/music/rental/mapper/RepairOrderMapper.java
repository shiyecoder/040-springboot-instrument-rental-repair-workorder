package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;

/** 维修工单 Mapper */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
}

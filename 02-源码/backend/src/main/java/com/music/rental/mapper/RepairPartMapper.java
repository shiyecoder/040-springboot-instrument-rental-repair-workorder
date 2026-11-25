package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RepairPart;
import org.apache.ibatis.annotations.Mapper;

/** 维修零件领用 Mapper */
@Mapper
public interface RepairPartMapper extends BaseMapper<RepairPart> {
}

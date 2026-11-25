package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.RentalHandover;
import org.apache.ibatis.annotations.Mapper;

/** 出库/归还交接单 Mapper */
@Mapper
public interface RentalHandoverMapper extends BaseMapper<RentalHandover> {
}

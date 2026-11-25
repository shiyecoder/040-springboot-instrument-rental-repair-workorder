package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.Instrument;
import org.apache.ibatis.annotations.Mapper;

/** 乐器主档 Mapper */
@Mapper
public interface InstrumentMapper extends BaseMapper<Instrument> {
}

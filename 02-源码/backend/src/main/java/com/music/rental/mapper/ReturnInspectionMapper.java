package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.ReturnInspection;
import org.apache.ibatis.annotations.Mapper;

/** 归还验收 Mapper */
@Mapper
public interface ReturnInspectionMapper extends BaseMapper<ReturnInspection> {
}

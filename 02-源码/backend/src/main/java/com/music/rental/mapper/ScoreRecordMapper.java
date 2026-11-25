package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.ScoreRecord;
import org.apache.ibatis.annotations.Mapper;

/** 评分计算留痕 Mapper */
@Mapper
public interface ScoreRecordMapper extends BaseMapper<ScoreRecord> {
}

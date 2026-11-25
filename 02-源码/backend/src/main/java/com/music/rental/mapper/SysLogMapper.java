package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/** 操作日志 Mapper */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}

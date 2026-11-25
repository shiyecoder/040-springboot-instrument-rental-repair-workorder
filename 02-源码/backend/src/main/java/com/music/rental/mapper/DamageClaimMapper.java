package com.music.rental.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.rental.entity.DamageClaim;
import org.apache.ibatis.annotations.Mapper;

/** 损坏赔偿 Mapper */
@Mapper
public interface DamageClaimMapper extends BaseMapper<DamageClaim> {
}

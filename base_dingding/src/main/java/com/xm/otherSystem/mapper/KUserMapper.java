package com.xm.otherSystem.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.otherSystem.domain.entity.KUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("hr")
public interface KUserMapper extends BaseMapper<KUser> {
}
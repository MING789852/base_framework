package com.xm.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.auth.domain.entity.TcUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TcUserMapper extends BaseMapper<TcUser> {
}
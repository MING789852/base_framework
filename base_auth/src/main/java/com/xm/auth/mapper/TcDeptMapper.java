package com.xm.auth.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.auth.domain.entity.TcDept;
import com.xm.auth.domain.vo.TcDeptVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TcDeptMapper extends BaseMapper<TcDept> {
    List<TcDeptVo> selectByList(@Param("ew") QueryWrapper queryWrapper);

    TcDeptVo findById(@Param("id") String id);
}
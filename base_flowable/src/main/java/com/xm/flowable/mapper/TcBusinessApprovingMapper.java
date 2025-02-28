package com.xm.flowable.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.flowable.domain.entity.TcBusinessApproving;
import com.xm.flowable.domain.query.ActInst;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TcBusinessApprovingMapper extends BaseMapper<TcBusinessApproving> {

    List<ActInst> selectHisActInst(@Param("ew") Wrapper<Object> wrapper);

    List<ActInst> selectRuActInst(@Param("ew") Wrapper<Object> wrapper);

    int deleteHisTask(@Param("ew") Wrapper<Object> wrapper);

    int deleteHisVar(@Param("ew") Wrapper<Object> wrapper);

    int deleteHisIdentity(@Param("ew") Wrapper<Object> wrapper);

    int deleteHisActInst(@Param("ew") Wrapper<Object> wrapper);

    int deleteRuExecution(@Param("ew") Wrapper<Object> wrapper);

    int deleteRuTask(@Param("ew") Wrapper<Object> wrapper);

    int deleteRuVar(@Param("ew") Wrapper<Object> wrapper);

    int deleteRuActInst(@Param("ew") Wrapper<Object> wrapper);
}
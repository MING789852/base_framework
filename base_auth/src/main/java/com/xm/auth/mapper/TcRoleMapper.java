package com.xm.auth.mapper;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.vo.RoleRelUserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TcRoleMapper extends BaseMapper<TcRole> {
    List<RoleRelUserVo> selectUserByRoleId(@Param("roleId") String roleId, @Param("ew") Wrapper wrapper,Page<RoleRelUserVo> page);
}
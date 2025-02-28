package com.xm.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.mapper.TcRoleMapper;
import com.xm.auth.service.TcRoleRouterRelService;
import com.xm.auth.service.TcRoleService;
import com.xm.auth.service.TcRouterActionService;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.module.core.params.QueryData;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationResult;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcRoleServiceImpl implements TcRoleService {

    private final TcRoleMapper tcRoleMapper;

    private final TcRouterActionService routerActionService;

    private final TcUserRoleRelService userRoleRelService;

    private final TcRoleRouterRelService roleRouterRelService;

    @Override
    public List<TcRole> getRoleList() {
        LambdaQueryWrapper<TcRole> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(TcRole::getJudgeEnable,1);
        return tcRoleMapper.selectList(queryWrapper);
    }

    @Override
    public Page<TcRole> selectByPage(QueryData<TcRole> queryData) {
        queryData.getQueryParams().put("judgeEnable_$_eq",1);
        Page<TcRole> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        return tcRoleMapper.selectPage(page, queryData.generateQueryWrapper());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcRole> tcRoleList) {
        List<String> idList=tcRoleList.stream().map(TcRole::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(idList)) {
            //删除角色主表
            LambdaQueryWrapper<TcRole> tcRoleLambdaQueryWrapper=new LambdaQueryWrapper<>();
            tcRoleLambdaQueryWrapper.in(TcRole::getId,idList);
            tcRoleMapper.delete(tcRoleLambdaQueryWrapper);

            //删除路由角色表
            roleRouterRelService.deleteByRoleIdList(idList);
            //删除角色路由操作关联表
            routerActionService.deleteRouterActionRelByRoleIdList(idList);
            //删除角色用户关联表
            userRoleRelService.unRelByRoleIdList(idList);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcRole> tcRoleList) {
        for (int i = 0; i < tcRoleList.size(); i++) {
            TcRole tcRole=tcRoleList.get(i);
            ValidationResult validationResult= ValidationUtils.validateEntityWithResultIgnore(tcRole, Arrays.asList("id","judgeEnable"));
            if (!validationResult.getIsLegal()){
                throw new CommonException(StrUtil.format("第{}行数据错误=>{}",(i+1),validationResult.getMsg()));
            }
            tcRole.setJudgeEnable(1);
            if (StrUtil.isBlank(tcRole.getId())){
                LambdaQueryWrapper<TcRole> roleLambdaQueryWrapper=new LambdaQueryWrapper<>();
                roleLambdaQueryWrapper.eq(TcRole::getRoleCode,tcRole.getRoleCode());
                TcRole dataBase = tcRoleMapper.selectOne(roleLambdaQueryWrapper);
                if (dataBase==null){
                    tcRole.setCreateDate(new Date());
                    tcRole.setId(SnowIdUtil.getSnowId());
                    tcRoleMapper.insert(tcRole);
                }else {
                    tcRole.setId(dataBase.getId());
                    tcRole.setUpdateDate(new Date());
                    tcRoleMapper.updateById(tcRole);
                }
            }else {
                tcRole.setUpdateDate(new Date());
                tcRoleMapper.updateById(tcRole);
            }
        }
        return "操作成功";
    }

    @Override
    public Page<RoleRelUserVo> selectUserAndDeptPageByRoleId(String roleId, QueryData<RoleRelUserVo> queryData) {
        Page<RoleRelUserVo> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        QueryWrapper<RoleRelUserVo> wrapper = queryData.generateQueryWrapperDisUnderlineCase();
        List<RoleRelUserVo> records = tcRoleMapper.selectUserByRoleId(roleId, wrapper, page);
        page.setRecords(records);
        return page;
    }

    @Override
    public TcRoleMapper getMapper() {
        return tcRoleMapper;
    }
}

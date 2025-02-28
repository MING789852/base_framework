package com.xm.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.dto.RouterActionSaveDataDto;
import com.xm.auth.domain.dto.RouterActionSaveRefDto;
import com.xm.auth.domain.entity.*;
import com.xm.auth.mapper.TcRoleRouterActionRelMapper;
import com.xm.auth.mapper.TcRouterActionMapper;
import com.xm.auth.service.TcRouterActionService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcRouterActionServiceImpl implements TcRouterActionService {

    private final TcRouterActionMapper routerActionMapper;

    private final TcRoleRouterActionRelMapper roleRouterActionRelMapper;


    @Override
    public List<TcRouterAction> getRouterActionDataByRouter(TcRouter router) {
        String routerId = router.getId();
        if (StrUtil.isBlank(routerId)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcRouterAction> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcRouterAction::getJudgeEnable,1)
                .eq(TcRouterAction::getRouterId,routerId);
        return routerActionMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveRouterActionData(RouterActionSaveDataDto saveDto) {
        Date now = new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        TcRouter router = saveDto.getRouter();
        if (router==null){
            throw new CommonException("Router为空");
        }
        if (StrUtil.isBlank(router.getId())){
            throw new CommonException("RouterId为空");
        }
        List<TcRouterAction> routerActionList = saveDto.getRouterActionList();
        if (CollectionUtil.isNotEmpty(routerActionList)){
            ValidationUtils.validateEntityListIgnore(routerActionList, Arrays.asList("id","routerId","judgeEnable"));
            for (TcRouterAction action:routerActionList){
                String id = action.getId();
                action.setRouterId(router.getId());
                if (StrUtil.isBlank(action.getActionName())){
                    action.setActionName(" ");
                }
                if (StrUtil.isBlank(id)){
                    action.setId(SnowIdUtil.getSnowId());
                    action.setCreateDate(now);
                    action.setCreateUser(currentLoginUserBySession.getNickName());
                    routerActionMapper.insert(action);
                }else {
                    action.setUpdateDate(now);
                    action.setUpdateUser(currentLoginUserBySession.getNickName());
                    routerActionMapper.updateById(action);
                }
            }
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteRouterActionData(List<TcRouterAction> routerActionList) {
        if (CollectionUtil.isEmpty(routerActionList)){
            throw new CommonException("数据为空");
        }
        List<String> idList = routerActionList.stream().map(TcRouterAction::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(idList)){
            throw new CommonException("数据ID为空");
        }
        //删除主表
        routerActionMapper.deleteByIds(idList);
        //删除对应的关联表
        LambdaQueryWrapper<TcRoleRouterActionRel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(TcRoleRouterActionRel::getRouterActionId,idList);
        roleRouterActionRelMapper.delete(lambdaQueryWrapper);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveRouterActionRef(RouterActionSaveRefDto saveRefDto) {
        Date now = new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        TcRole role = saveRefDto.getRole();
        if (role==null||StrUtil.isBlank(role.getId())){
            throw new CommonException("角色为空");
        }
        List<String> routerActionList = saveRefDto.getRouterActionIdList();
        //先删除
        LambdaQueryWrapper<TcRoleRouterActionRel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcRoleRouterActionRel::getRoleId,role.getId());
        roleRouterActionRelMapper.delete(lambdaQueryWrapper);
        //后新增
        if (CollectionUtil.isNotEmpty(routerActionList)){
            List<TcRoleRouterActionRel> relList=new ArrayList<>();
            for (String routerActionId:routerActionList){
                TcRouterAction action = routerActionMapper.selectById(routerActionId);
                if (action!=null){
                    TcRoleRouterActionRel rel=new TcRoleRouterActionRel();
                    rel.setCreateDate(now);
                    rel.setCreateUser(currentLoginUserBySession.getNickName());
                    rel.setRouterActionId(action.getId());
                    rel.setRoleId(role.getId());
                    rel.setRouterId(action.getRouterId());
                    rel.setId(SnowIdUtil.getSnowId());
                    relList.add(rel);
                }
            }
            roleRouterActionRelMapper.insert(relList);
        }
        return "操作成功";
    }

    @Override
    public List<String> getRouterActionRefByRole(TcRole role) {
        LambdaQueryWrapper<TcRoleRouterActionRel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcRoleRouterActionRel::getRoleId,role.getId())
                .eq(TcRoleRouterActionRel::getJudgeEnable,1);
        List<TcRoleRouterActionRel> relList = roleRouterActionRelMapper.selectList(lambdaQueryWrapper);
        if (CollectionUtil.isEmpty(relList)){
            return new ArrayList<>();
        }else {
            return relList.stream().map(TcRoleRouterActionRel::getRouterActionId).collect(Collectors.toList());
        }
    }

    @Override
    public List<TcRouterAction> getRouterActionByRoleAndRouter(List<String> roleIdList, List<String> routerIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(routerIdList)){
            return new ArrayList<>();
        }
        List<TcRoleRouterActionRel> refByRoleAndRouter = getRefByRoleAndRouter(roleIdList, routerIdList);
        if (CollectionUtil.isEmpty(refByRoleAndRouter)){
            return new ArrayList<>();
        }
        List<String> routerActionIdList= refByRoleAndRouter.stream().map(TcRoleRouterActionRel::getRouterActionId).collect(Collectors.toList());
        LambdaQueryWrapper<TcRouterAction> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .in(TcRouterAction::getId,routerActionIdList)
                .eq(TcRouterAction::getJudgeEnable,1);
        return routerActionMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<TcRouterAction> getRouterActionDataByIdList(List<String> idList) {
        LambdaQueryWrapper<TcRouterAction> routerActionLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerActionLambdaQueryWrapper
                .in(TcRouterAction::getId,idList)
                .eq(TcRouterAction::getJudgeEnable,1);
        return routerActionMapper.selectList(routerActionLambdaQueryWrapper);
    }

    @Override
    public List<TcRoleRouterActionRel> getRefByRoleAndRouter(List<String> roleIdList, List<String> routerIdList) {
        LambdaQueryWrapper<TcRoleRouterActionRel> relLambdaQueryWrapper=new LambdaQueryWrapper<>();
        relLambdaQueryWrapper
                .in(TcRoleRouterActionRel::getRouterId,routerIdList)
                .in(TcRoleRouterActionRel::getRoleId,roleIdList)
                .eq(TcRoleRouterActionRel::getJudgeEnable,1);
        return roleRouterActionRelMapper.selectList(relLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouterActionDataAndRelByRouterIdList(List<String> routerIdList) {
        if (CollectionUtil.isEmpty(routerIdList)){
            return;
        }
        //先删除主表
        LambdaQueryWrapper<TcRouterAction> routerActionLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerActionLambdaQueryWrapper.in(TcRouterAction::getRouterId,routerIdList);
        routerActionMapper.delete(routerActionLambdaQueryWrapper);

        //再删除关联
        LambdaQueryWrapper<TcRoleRouterActionRel> relLambdaQueryWrapper=new LambdaQueryWrapper<>();
        relLambdaQueryWrapper.in(TcRoleRouterActionRel::getRouterId,routerIdList);
        roleRouterActionRelMapper.delete(relLambdaQueryWrapper);
    }

    @Override
    public void deleteRouterActionRelByRoleIdList(List<String> roleIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return;
        }
        LambdaQueryWrapper<TcRoleRouterActionRel> relLambdaQueryWrapper=new LambdaQueryWrapper<>();
        relLambdaQueryWrapper.in(TcRoleRouterActionRel::getRoleId,roleIdList);
        roleRouterActionRelMapper.delete(relLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRouterActionRelByRoleIdAndRouterIdList(String roleId, List<String> routerIdList) {
        if (StrUtil.isBlank(roleId)){
            return;
        }
        if (CollectionUtil.isEmpty(routerIdList)){
            return;
        }
        LambdaQueryWrapper<TcRoleRouterActionRel> relLambdaQueryWrapper=new LambdaQueryWrapper<>();
        relLambdaQueryWrapper
                .eq(TcRoleRouterActionRel::getRoleId,roleId)
                .in(TcRoleRouterActionRel::getRouterId,routerIdList);
        roleRouterActionRelMapper.delete(relLambdaQueryWrapper);
    }
}

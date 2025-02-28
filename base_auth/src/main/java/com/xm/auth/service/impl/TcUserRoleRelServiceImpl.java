package com.xm.auth.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.entity.TcUserRoleRel;
import com.xm.auth.domain.vo.RoleRelUserVo;
import com.xm.auth.domain.vo.UserRoleRelVo;
import com.xm.auth.mapper.TcRoleMapper;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.auth.mapper.TcUserRoleRelMapper;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.module.core.params.QueryData;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcUserRoleRelServiceImpl implements TcUserRoleRelService {

    private final TcUserRoleRelMapper userRoleRelMapper;

    private final TcUserMapper userMapper;

    private final TcRoleMapper roleMapper;

    @Override
    public List<TcUserRoleRel> selectByUserId(String userId) {
        if (StrUtil.isBlank(userId)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleRelLambdaQueryWrapper.eq(TcUserRoleRel::getUserId,userId).eq(TcUserRoleRel::getJudgeEnable,1);
        return userRoleRelMapper.selectList(userRoleRelLambdaQueryWrapper);
    }


    @Override
    public List<String> getRoleIdListByUser(String userId) {
        List<String> roleList=new ArrayList<>();
        if (StrUtil.isBlank(userId)){
            return roleList;
        }
        TcUser tcUser = userMapper.selectById(userId);
        if (tcUser==null){
            return roleList;
        }
        if (StrUtil.isBlank(tcUser.getId())){
            return roleList;
        }
        LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleRelLambdaQueryWrapper.eq(TcUserRoleRel::getJudgeEnable,1).eq(TcUserRoleRel::getUserId,tcUser.getId());
        List<TcUserRoleRel> userRoleRelList=userRoleRelMapper.selectList(userRoleRelLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(userRoleRelList)){
            return new ArrayList<>();
        }
        return userRoleRelList.stream().map(TcUserRoleRel::getRoleId).collect(Collectors.toList());
    }

    @Override
    public List<TcRole> getRoleListByUser(String userId) {
        List<String> roleIdListByUser = getRoleIdListByUser(userId);
        if (CollectionUtil.isEmpty(roleIdListByUser)){
            return new ArrayList<>();
        }else {
            if (CollectionUtil.isEmpty(roleIdListByUser)){
                return new ArrayList<>();
            }
            LambdaQueryWrapper<TcRole> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.in(TcRole::getId,roleIdListByUser);
            return roleMapper.selectList(lambdaQueryWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String roleAddUser(String roleId, List<String> userIdList) {
        if (StrUtil.isBlank(roleId)){
            throw new CommonException("角色不能为空");
        }
        if (CollectionUtil.isEmpty(userIdList)){
            throw new CommonException("添加用户不能为空");
        }
        //先删除(防止重复关联)
        LambdaQueryWrapper<TcUserRoleRel> roleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roleRelLambdaQueryWrapper
                .eq(TcUserRoleRel::getRoleId,roleId)
                .in(TcUserRoleRel::getUserId,userIdList);
        userRoleRelMapper.delete(roleRelLambdaQueryWrapper);
        Date now=new Date();
        //再添加
        for (String userId:userIdList){
            TcUserRoleRel tcUserRoleRel=new TcUserRoleRel();
            tcUserRoleRel.setRoleId(roleId);
            tcUserRoleRel.setUserId(userId);
            tcUserRoleRel.setId(SnowIdUtil.getSnowId());
            tcUserRoleRel.setCreateDate(now);
            tcUserRoleRel.setJudgeEnable(1);
            userRoleRelMapper.insert(tcUserRoleRel);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String roleAddUserByRoleCode(String roleCode, List<String> userIdList) {
        LambdaQueryWrapper<TcRole> roleLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roleLambdaQueryWrapper.eq(TcRole::getRoleCode,roleCode);
        TcRole tcRole = roleMapper.selectOne(roleLambdaQueryWrapper);
        if (tcRole==null){
            throw new CommonException("角色编码不存在");
        }
        return roleAddUser(tcRole.getId(),userIdList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveUserRoleRel(UserRoleRelVo userRoleRelVo) {
        String userId = userRoleRelVo.getUserId();
        if (StrUtil.isBlank(userId)){
            throw new CommonException("用户id为空");
        }
        TcUser tcUser = userMapper.selectById(userId);
        if (tcUser==null){
            throw new CommonException("用户数据为空");
        }
        if (StrUtil.isBlank(tcUser.getId())){
            throw new CommonException("用户ID为空");
        }
        List<String> roleIdList=userRoleRelVo.getRoleIdList();
        //先删除所有关联
        Date now=new Date();
        LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleRelLambdaQueryWrapper.clear();
        userRoleRelLambdaQueryWrapper.eq(TcUserRoleRel::getUserId,tcUser.getId());
        userRoleRelMapper.delete(userRoleRelLambdaQueryWrapper);

        //再关联
        if (CollectionUtil.isNotEmpty(roleIdList)){
            for (String roleId:roleIdList){
                TcUserRoleRel tcUserRoleRel=new TcUserRoleRel();
                tcUserRoleRel.setRoleId(roleId);
                tcUserRoleRel.setUserId(tcUser.getId());
                tcUserRoleRel.setId(SnowIdUtil.getSnowId());
                tcUserRoleRel.setCreateDate(now);
                tcUserRoleRel.setJudgeEnable(1);

                userRoleRelMapper.insert(tcUserRoleRel);
            }
        }
        return "操作成功";
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unRelUserAndRoleAll(String roleId, QueryData<RoleRelUserVo> queryData) {
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        QueryWrapper<RoleRelUserVo> wrapper = queryData.generateQueryWrapperDisUnderlineCase();
        List<RoleRelUserVo> userAndDeptVos = roleMapper.selectUserByRoleId(roleId, wrapper,null);
        if (CollectionUtil.isNotEmpty(userAndDeptVos)){
            List<String> usrIdList = userAndDeptVos.stream().map(RoleRelUserVo::getUserId).collect(Collectors.toList());
            LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
            userRoleRelLambdaQueryWrapper.eq(TcUserRoleRel::getRoleId,roleId).in(TcUserRoleRel::getUserId,usrIdList);
            userRoleRelMapper.delete(userRoleRelLambdaQueryWrapper);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String unRelUserAndRole(String roleId, String userId) {
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }

        LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleRelLambdaQueryWrapper.eq(TcUserRoleRel::getRoleId,roleId).eq(TcUserRoleRel::getUserId,userId);
        userRoleRelMapper.delete(userRoleRelLambdaQueryWrapper);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unRelByRoleIdList(List<String> roleIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return;
        }
        LambdaQueryWrapper<TcUserRoleRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userRoleRelLambdaQueryWrapper.in(TcUserRoleRel::getRoleId,roleIdList);
        userRoleRelMapper.delete(userRoleRelLambdaQueryWrapper);
    }
}

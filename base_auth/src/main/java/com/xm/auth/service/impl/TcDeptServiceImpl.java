package com.xm.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.*;
import com.xm.auth.domain.vo.TcDeptVo;
import com.xm.auth.domain.vo.UserAndDeptVo;
import com.xm.auth.mapper.TcDeptMapper;
import com.xm.auth.mapper.TcUserRoleRelMapper;
import com.xm.auth.service.*;
import com.xm.module.core.params.QueryData;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TcDeptServiceImpl implements TcDeptService {

    private final TcDeptMapper tcDeptMapper;

    private final TcUserService userService;

    private final UserDeptRelService userDeptRelService;

    private final TcRoleService roleService;

    private final TcUserRoleRelMapper userRoleRelMapper;

    @Override
    public String initDept() {
        ExternalDeptService externalDeptService=SpringBeanUtil.getBeanByClass(ExternalDeptService.class);
        return externalDeptService.initDept();
    }

    @Override
    public List<TcDeptVo> selectByList(QueryData<TcDeptVo> queryData) {
        QueryWrapper<TcDeptVo> queryWrapper = queryData.generateQueryWrapper();
        queryWrapper.eq("level",1);
        TcDeptService beanByClass = SpringBeanUtil.getBeanByClass(TcDeptService.class);
        List<TcDeptVo> tcDeptVoList = beanByClass.selectAllByList(queryData);

        if (CollectionUtil.isNotEmpty(tcDeptVoList)) {
            for (TcDeptVo node : tcDeptVoList) {
                List<TcDeptVo> children=recursionFindChildren(node);
                if (CollectionUtil.isNotEmpty(children)){
                    node.setIsLeaf(false);
                    node.setChildren(children);
                }else {
                    node.setIsLeaf(true);
                }
            }
        }

        return tcDeptVoList;
    }

    @Override
    public List<TcDeptVo> selectAllByList(QueryData<TcDeptVo> queryData) {
        QueryWrapper<TcDeptVo> queryWrapper = queryData.getWrapper();
        queryWrapper.eq("judge_enable",1);
        return tcDeptMapper.selectByList(queryWrapper);
    }

    @Override
    public TcDeptVo selectOne(String id) {
        if (StrUtil.isBlank(id)){
            throw new CommonException("部门id不能为空");
        }
        QueryWrapper<TcDeptVo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("id",id);
        List<TcDeptVo> tcDeptVoList=tcDeptMapper.selectByList(queryWrapper);
        if (CollectionUtil.isNotEmpty(tcDeptVoList)){
            TcDeptVo tcDeptVo = tcDeptVoList.get(0);
            List<TcDeptVo> children=recursionFindChildren(tcDeptVo);
            if (CollectionUtil.isNotEmpty(children)){
                tcDeptVo.setIsLeaf(false);
                tcDeptVo.setChildren(children);
            }else {
                tcDeptVo.setIsLeaf(true);
            }
            return tcDeptVo;
        }else {
            return null;
        }
    }

    private List<TcDeptVo> recursionFindChildren(TcDeptVo parent){
        QueryWrapper<TcDeptVo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("parent_id",parent.getId());
        List<TcDeptVo> tcDeptVoList=tcDeptMapper.selectByList(queryWrapper);
        if (CollectionUtil.isNotEmpty(tcDeptVoList)){
            for (TcDeptVo node:tcDeptVoList){
                List<TcDeptVo> children=recursionFindChildren(node);
                if (CollectionUtil.isNotEmpty(children)){
                    node.setIsLeaf(false);
                    node.setChildren(children);
                }else {
                    node.setIsLeaf(true);
                }
            }
        }
        return tcDeptVoList;
    }

    @Override
    public List<TcDeptVo> findChildrenList(TcDeptVo parent){
        QueryWrapper<TcDeptVo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("parent_id",parent.getId());
        List<TcDeptVo> tcDeptVoList=tcDeptMapper.selectByList(queryWrapper);
        if (CollectionUtil.isNotEmpty(tcDeptVoList)){
            tcDeptVoList.forEach(item->{
                item.setIsLeaf(false);
                item.setChildren(new ArrayList<>());
            });
        }
        return tcDeptVoList;
    }

    @Override
    public List<UserAndDeptVo> findUserAndDeptRefByDeptId(String deptId) {
        QueryWrapper<TcDeptVo> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id",deptId);
        TcDeptService beanByClass = SpringBeanUtil.getBeanByClass(TcDeptService.class);
        TcDeptVo tcDeptVo = beanByClass.selectOne(deptId);
        if (tcDeptVo==null){
            return new ArrayList<>();
        }
        List<UserAndDeptVo> userAndDeptVoList=new ArrayList<>();
        recursionUserAndDeptRef(userAndDeptVoList, Collections.singletonList(tcDeptVo));
        return userAndDeptVoList;
    }

    @Override
    public List<TcDept> selectDeptByDeptNameList(List<String> deptNameList) {
        LambdaQueryWrapper<TcDept> deptLambdaQueryWrapper=new LambdaQueryWrapper<>();
        deptLambdaQueryWrapper.in(TcDept::getName,deptNameList);
        return tcDeptMapper.selectList(deptLambdaQueryWrapper);
    }

    private void recursionUserAndDeptRef(List<UserAndDeptVo> userAndDeptVoList,List<TcDeptVo> deptVoList){
        LambdaQueryWrapper<TcUserDeptRel> userDeptRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TcUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(deptVoList)){
            for (TcDeptVo deptVo:deptVoList){
                userDeptRelLambdaQueryWrapper.clear();
                userDeptRelLambdaQueryWrapper.eq(TcUserDeptRel::getDeptId,deptVo.getId())
                        .eq(TcUserDeptRel::getJudgeEnable,1);
                List<TcUserDeptRel> userDeptRelList=userDeptRelService.getMapper().selectList(userDeptRelLambdaQueryWrapper);

                if (CollectionUtil.isNotEmpty(userDeptRelList)){
                    List<String> userIdList = userDeptRelList.stream().map(TcUserDeptRel::getUserId).collect(Collectors.toList());
                    userLambdaQueryWrapper.clear();
                    userLambdaQueryWrapper.in(TcUser::getId,userIdList).eq(TcUser::getJudgeEnable,1);
                    List<TcUser> tcUserList = userService.getMapper().selectList(userLambdaQueryWrapper);
                    if (CollectionUtil.isNotEmpty(tcUserList)){
                        for (TcUser user:tcUserList){
                            UserAndDeptVo userAndDeptVo=new UserAndDeptVo();
                            userAndDeptVo.setDeptName(deptVo.getName());
                            userAndDeptVo.setDeptId(deptVo.getId());
                            userAndDeptVo.setUserName(user.getNickName());
                            userAndDeptVo.setUserId(user.getId());

                            userAndDeptVoList.add(userAndDeptVo);
                        }
                    }
                }
                if (CollectionUtil.isNotEmpty(deptVo.getChildren())){
                    recursionUserAndDeptRef(userAndDeptVoList,deptVo.getChildren());
                }
            }
        }
    }


    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public List<TcUser> createTcUserWithDeptId(List<String> deptIdList) {
        ExternalDeptService externalDeptService=SpringBeanUtil.getBeanByClass(ExternalDeptService.class);
        return externalDeptService.createTcUserWithDeptId(deptIdList);
    }

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public String allocateRoleToDept(String deptId, List<String> roleIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return "角色为空";
        }
        TcDeptService beanByClass = SpringBeanUtil.getBeanByClass(TcDeptService.class);
        TcDeptVo tcDeptVo = beanByClass.selectOne(deptId);
        List<TcUser> userList=new ArrayList<>();
        recursionGetDeptUser(userList, Collections.singletonList(tcDeptVo));
        if (CollectionUtil.isNotEmpty(userList)){
            LambdaQueryWrapper<TcRole> roleLambdaQueryWrapper=new LambdaQueryWrapper<>();
            LambdaQueryWrapper<TcUserRoleRel> relLambdaQueryWrapper=new LambdaQueryWrapper<>();
            Date now = new Date();
            for (String roleId:roleIdList){
                roleLambdaQueryWrapper.clear();
                roleLambdaQueryWrapper.eq(TcRole::getId,roleId).eq(TcRole::getJudgeEnable,1);
                TcRole role = roleService.getMapper().selectOne(roleLambdaQueryWrapper);
                if (role==null){
                    continue;
                }
                for (TcUser user:userList){
                    relLambdaQueryWrapper.clear();
                    relLambdaQueryWrapper
                            .eq(TcUserRoleRel::getUserId,user.getId())
                            .eq(TcUserRoleRel::getRoleId,role.getId())
                            .eq(TcUserRoleRel::getJudgeEnable,1);
                    Long count = userRoleRelMapper.selectCount(relLambdaQueryWrapper);
                    if (count.intValue()==0){
                        TcUserRoleRel userRoleRel=new TcUserRoleRel();
                        userRoleRel.setUserId(user.getId());
                        userRoleRel.setRoleId(role.getId());
                        userRoleRel.setCreateUser("system");
                        userRoleRel.setCreateDate(now);
                        userRoleRel.setId(SnowIdUtil.getSnowId());
                        userRoleRel.setJudgeEnable(1);

                        userRoleRelMapper.insert(userRoleRel);
                    }
                }
            }
        }

        return "操作成功";
    }


    private void recursionGetDeptUser(List<TcUser> userList,List<TcDeptVo> deptVoList){
        LambdaQueryWrapper<TcUserDeptRel> userDeptRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TcUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(deptVoList)){
            for (TcDeptVo deptVo:deptVoList){
                userDeptRelLambdaQueryWrapper.clear();
                userDeptRelLambdaQueryWrapper.eq(TcUserDeptRel::getDeptId,deptVo.getId())
                        .eq(TcUserDeptRel::getJudgeEnable,1);
                List<TcUserDeptRel> userDeptRelList=userDeptRelService.getMapper().selectList(userDeptRelLambdaQueryWrapper);

                if (CollectionUtil.isNotEmpty(userDeptRelList)){
                    List<String> userIdList = userDeptRelList.stream().map(TcUserDeptRel::getUserId).collect(Collectors.toList());
                    userLambdaQueryWrapper.clear();
                    userLambdaQueryWrapper.in(TcUser::getId,userIdList).eq(TcUser::getJudgeEnable,1);
                    List<TcUser> tcUserList = userService.getMapper().selectList(userLambdaQueryWrapper);
                    if (CollectionUtil.isNotEmpty(tcUserList)){
                        userList.addAll(tcUserList);
                    }
                }
                if (CollectionUtil.isNotEmpty(deptVo.getChildren())){
                    recursionGetDeptUser(userList,deptVo.getChildren());
                }
            }
        }
    }

}

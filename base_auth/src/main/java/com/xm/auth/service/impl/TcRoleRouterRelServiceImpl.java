package com.xm.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRoleRouterRel;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.vo.RoleRouterVo;
import com.xm.auth.mapper.TcRoleRouterRelMapper;
import com.xm.auth.service.TcRoleRouterRelService;
import com.xm.auth.service.TcRouterActionService;
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
public class TcRoleRouterRelServiceImpl implements TcRoleRouterRelService {

    private final TcRoleRouterRelMapper roleRouterRelMapper;

    private final TcRouterActionService routerActionService;

    @Override
    public List<TcRoleRouterRel> selectByRoleIdList(List<String> roleIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcRoleRouterRel> roleRouterRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roleRouterRelLambdaQueryWrapper
                .in(TcRoleRouterRel::getRoleId,roleIdList)
                .eq(TcRoleRouterRel::getJudgeEnable,1);
        return roleRouterRelMapper.selectList(roleRouterRelLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRouterIdList(List<String> routerIdList) {
        if (CollectionUtil.isEmpty(routerIdList)){
            return;
        }
        LambdaQueryWrapper<TcRoleRouterRel> routerRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerRelLambdaQueryWrapper.in(TcRoleRouterRel::getRouterId,routerIdList);
        roleRouterRelMapper.delete(routerRelLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleIdList(List<String> roleIdList) {
        if (CollectionUtil.isEmpty(roleIdList)){
            return;
        }
        LambdaQueryWrapper<TcRoleRouterRel> routerRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerRelLambdaQueryWrapper.in(TcRoleRouterRel::getRoleId,roleIdList);
        roleRouterRelMapper.delete(routerRelLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveRoleRouterRel(RoleRouterVo roleRouterVo) {
        TcRole tcRole=roleRouterVo.getTcRole();
        if (tcRole==null){
            throw new CommonException("角色数据为空");
        }
        if (StrUtil.isBlank(tcRole.getId())){
            throw new CommonException("角色ID为空");
        }
        List<TcRouter> tcRouterList=roleRouterVo.getRouterList();
        Date now=new Date();
        LambdaQueryWrapper<TcRoleRouterRel> userRoleRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        //路由菜单为空，则删除全部
        if (CollectionUtil.isEmpty(tcRouterList)){
            userRoleRelLambdaQueryWrapper.eq(TcRoleRouterRel::getRoleId,tcRole.getId());
            roleRouterRelMapper.delete(userRoleRelLambdaQueryWrapper);
        }else {
            //新的routerIdList
            List<String> newRouterIdList = tcRouterList.stream().map(TcRouter::getId).collect(Collectors.toList());

            //旧的routerIdList
            userRoleRelLambdaQueryWrapper
                    .eq(TcRoleRouterRel::getRoleId,tcRole.getId())
                    .eq(TcRoleRouterRel::getJudgeEnable,1);
            List<TcRoleRouterRel> roleRouterRelList = roleRouterRelMapper.selectList(userRoleRelLambdaQueryWrapper);
            if (CollectionUtil.isEmpty(roleRouterRelList)){
                roleRouterRelList=new ArrayList<>();
            }
            List<String> oldRouterIdList=roleRouterRelList.stream().map(TcRoleRouterRel::getRouterId).collect(Collectors.toList());

            //旧对新取差集是删除
            List<String> deleteRouterIdList = oldRouterIdList.stream().filter(item -> !newRouterIdList.contains(item)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(deleteRouterIdList)){
                List<String> deleteIdList =
                        roleRouterRelList.stream()
                                .filter(item -> deleteRouterIdList.contains(item.getRouterId()))
                                .map(TcRoleRouterRel::getId).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(deleteRouterIdList)){
                    roleRouterRelMapper.deleteByIds(deleteIdList);
                }
                routerActionService.deleteRouterActionRelByRoleIdAndRouterIdList(tcRole.getId(),deleteRouterIdList);
            }
            //新对旧取差集是新增
            List<String> addRouterIdList = newRouterIdList.stream().filter(item -> !oldRouterIdList.contains(item)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(addRouterIdList)){
                for (String addRouterId:addRouterIdList){
                    TcRoleRouterRel tcRoleRouterRel=new TcRoleRouterRel();
                    tcRoleRouterRel.setRoleId(tcRole.getId());
                    tcRoleRouterRel.setRouterId(addRouterId);
                    tcRoleRouterRel.setId(SnowIdUtil.getSnowId());
                    tcRoleRouterRel.setCreateDate(now);
                    tcRoleRouterRel.setJudgeEnable(1);

                    roleRouterRelMapper.insert(tcRoleRouterRel);
                }
            }
        }
        return "操作成功";
    }


    @Override
    public List<String> getRouterIdListByRole(String roleId) {
        if (StrUtil.isBlank(roleId)){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcRoleRouterRel> roleRouterRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        roleRouterRelLambdaQueryWrapper.eq(TcRoleRouterRel::getRoleId,roleId).eq(TcRoleRouterRel::getJudgeEnable,1);
        List<TcRoleRouterRel> tcRoleRouterRelList=roleRouterRelMapper.selectList(roleRouterRelLambdaQueryWrapper);
        return tcRoleRouterRelList.stream().map(TcRoleRouterRel::getRouterId).collect(Collectors.toList());
    }
}

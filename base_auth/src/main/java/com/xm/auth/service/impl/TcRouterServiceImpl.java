package com.xm.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.auth.domain.entity.*;
import com.xm.auth.domain.vo.*;
import com.xm.auth.mapper.TcRouterMapper;
import com.xm.auth.service.TcRoleRouterRelService;
import com.xm.auth.service.TcRouterActionService;
import com.xm.auth.service.TcRouterService;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationResult;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcRouterServiceImpl implements TcRouterService {

    private final TcRouterMapper tcRouterMapper;

    private final TcUserRoleRelService userRoleRelService;

    private final TcRoleRouterRelService roleRouterRelService;

    private final TcRouterActionService routerActionService;


    //外层key是路由编码(名称),内层key是路由操作编码，value是路由操作名称
    private Map<String, Map<String, String>> handleRouterActionMapping(List<TcRouter> routerList,List<TcRouterAction> routerActionList) {
        Map<String,Map<String, String>> mapMap=new HashMap<>();
        if (CollectionUtil.isEmpty(routerList)){
            return mapMap;
        }
        Map<String, Map<String, String>> routerIdActionMap = routerActionList.stream()
                .collect(Collectors.groupingBy(TcRouterAction::getRouterId, Collectors.toMap(TcRouterAction::getActionCode, TcRouterAction::getActionName)));
        for (TcRouter router:routerList){
            Map<String, String> atctionMap = routerIdActionMap.get(router.getId());
            if (CollectionUtil.isNotEmpty(atctionMap)){
                mapMap.put(router.getName(), atctionMap);
            }
        }
        return mapMap;
    }

    private RouterVo tcRouterToRouterVo(TcRouter tcRouter){
        RouterVo routerVo=new RouterVo();
        routerVo.setPath(tcRouter.getPath());
        routerVo.setName(tcRouter.getName());
        routerVo.setComponent(tcRouter.getComponent());
        routerVo.setRedirect(tcRouter.getRedirect());
        routerVo.setFullscreen(tcRouter.getFullscreen());
        RouteMeta routeMeta=new RouteMeta();
        routeMeta.setRank(tcRouter.getRankInt());
        routeMeta.setKeepAlive(tcRouter.getKeepAlive());
        routeMeta.setIcon(tcRouter.getIcon());
        routeMeta.setShowLink(tcRouter.getShowLink());
        routeMeta.setTitle(tcRouter.getTitle());
        routeMeta.setHiddenTag(tcRouter.getHiddenTag());
        routerVo.setMeta(routeMeta);
        return routerVo;
    }

    private List<RouterVo> initRouterList(List<TcRouter> tcRouterList){
        List<RouterVo> routerVoList=new ArrayList<>();
        if (CollectionUtil.isEmpty(tcRouterList)){
            return routerVoList;
        }
        Map<String,RouterVo> parentMap=new HashMap<>();
        for (TcRouter tcRouter:tcRouterList){
            String parentId=tcRouter.getParentId();
            if (StrUtil.isBlank(parentId)){
                continue;
            }
            RouterVo parentRouterVo=parentMap.get(parentId);
            if (parentRouterVo==null){
                TcRouter parentTcRouter=tcRouterMapper.selectById(parentId);
                if (parentTcRouter!=null){
                    parentRouterVo=tcRouterToRouterVo(parentTcRouter);
                    parentRouterVo.setChildren(new ArrayList<>());
                    parentMap.put(parentId,parentRouterVo);
                }else {
                    continue;
                }
            }
            RouterVo childrenRouterVo = tcRouterToRouterVo(tcRouter);
            childrenRouterVo.getMeta().setShowParent(true);
            parentRouterVo.getChildren().add(childrenRouterVo);
        }

        routerVoList.addAll(parentMap.values());
        return routerVoList;
    }

    @Override
    public List<TcRouter> getPublicRouter(){
        LambdaQueryWrapper<TcRouter> routerLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerLambdaQueryWrapper.ne(TcRouter::getLevel,0);
        routerLambdaQueryWrapper.in(TcRouter::getJudgePublic,true);
        routerLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1);

        List<TcRouter> tcRouterList=tcRouterMapper.selectList(routerLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcRouterList)){
            return new ArrayList<>();
        }else {
            return tcRouterList;
        }
    }

    @Override
    public TcRouter getTcRouterByRouterName(String routerName) {
        if(routerName==null){
            return null;
        }
        LambdaQueryWrapper<TcRouter> routerLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1);
        routerLambdaQueryWrapper.eq(TcRouter::getName,routerName);
        return tcRouterMapper.selectOne(routerLambdaQueryWrapper);
    }

    @Override
    public List<TcRouter> getPrivateRouterByRole(TcRole role) {
        if (role==null||StrUtil.isBlank(role.getId())){
            return new ArrayList<>();
        }
        List<TcRoleRouterRel> tcRoleRouterRelList = roleRouterRelService.selectByRoleIdList(Collections.singletonList(role.getId()));
        //角色对应的路由id为空则直接返回
        if (CollectionUtil.isEmpty(tcRoleRouterRelList)){
            return new ArrayList<>();
        }
        List<String> routerIdList=tcRoleRouterRelList.stream().map(TcRoleRouterRel::getRouterId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<TcRouter> routerLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerLambdaQueryWrapper.ne(TcRouter::getLevel,0);
        routerLambdaQueryWrapper.in(TcRouter::getId,routerIdList);
        routerLambdaQueryWrapper.eq(TcRouter::getJudgePublic,false);
        routerLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1);

        List<TcRouter> tcRouterList=tcRouterMapper.selectList(routerLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcRouterList)){
            return new ArrayList<>();
        }else {
            return tcRouterList;
        }
    }

    @Override
    public List<TcRouter> getPrivateRouter(List<TcRole> roleList){
        //角色为空直接返回
        if (CollectionUtil.isEmpty(roleList)){
            return new ArrayList<>();
        }
        List<String> roleIdList=roleList.stream().map(TcRole::getId).collect(Collectors.toList());
        List<TcRoleRouterRel> tcRoleRouterRelList = roleRouterRelService.selectByRoleIdList(roleIdList);
        //角色对应的路由id为空则直接返回
        if (CollectionUtil.isEmpty(tcRoleRouterRelList)){
            return new ArrayList<>();
        }
        List<String> routerIdList=tcRoleRouterRelList.stream().map(TcRoleRouterRel::getRouterId).distinct().collect(Collectors.toList());
        LambdaQueryWrapper<TcRouter> routerLambdaQueryWrapper=new LambdaQueryWrapper<>();
        routerLambdaQueryWrapper.ne(TcRouter::getLevel,0);
        routerLambdaQueryWrapper.in(TcRouter::getId,routerIdList);
        routerLambdaQueryWrapper.eq(TcRouter::getJudgePublic,false);
        routerLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1);

        List<TcRouter> tcRouterList=tcRouterMapper.selectList(routerLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcRouterList)){
            return new ArrayList<>();
        }else {
            return tcRouterList;
        }
    }

    @Override
    public JSONObject getCurrentRouter() {
        TcUser tcUser= UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (tcUser==null){
            throw new UnAuthException("未登录");
        }
        JSONObject jsonObject=new JSONObject();
        if (UserInfoUtil.isSuperPrivilege()){
            LambdaQueryWrapper<TcRouter> routerLambdaQueryWrapper=new LambdaQueryWrapper<>();
            routerLambdaQueryWrapper.ne(TcRouter::getLevel,0);
            routerLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1);

            List<TcRouter> tcRouterList=tcRouterMapper.selectList(routerLambdaQueryWrapper);

            jsonObject.set("router",initRouterList(tcRouterList));
            return jsonObject;
        }

        List<TcRouter> allRouter=new ArrayList<>();
        List<TcRouter> publicRouter = getPublicRouter();
        if (CollectionUtil.isNotEmpty(publicRouter)){
            allRouter.addAll(publicRouter);
        }

        List<TcRole> roleList = userRoleRelService.getRoleListByUser(tcUser.getId());
        List<TcRouter> privateRouter=getPrivateRouter(roleList);
        if (CollectionUtil.isNotEmpty(privateRouter)){
            allRouter.addAll(privateRouter);
        }


        List<String> roleCodeList = roleList.stream().map(TcRole::getRoleCode).collect(Collectors.toList());
        List<String> routerIdList = allRouter.stream().map(TcRouter::getId).collect(Collectors.toList());
        List<TcRouterAction> routerActionByUser = routerActionService.getRouterActionByUser(tcUser.getId(), routerIdList);
        Map<String, Map<String, String>> currentRouterActionMapping = handleRouterActionMapping(allRouter,routerActionByUser);
        UserInfoUtil.initCurrentRouterActionMapping(currentRouterActionMapping);
        UserInfoUtil.initCurrentRole(roleList);
        UserInfoUtil.initCurrentRouter(allRouter);
        jsonObject.set("routerActionMapping",currentRouterActionMapping);
        jsonObject.set("router",initRouterList(allRouter));
        jsonObject.set("roles",roleCodeList);
        return jsonObject;
    }


    @Override
    public List<TcRouterVo> getRouterList() {
        //获取顶层
        LambdaQueryWrapper<TcRouter> tcRouterLambdaQueryWrapper=new LambdaQueryWrapper<>();
        tcRouterLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1)
                .eq(TcRouter::getLevel,0)
                .orderByAsc(TcRouter::getRankInt);
        List<TcRouter> tcRouterList=tcRouterMapper.selectList(tcRouterLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcRouterList)){
            return new ArrayList<>();
        }
        List<TcRouterVo> tcRouterVoList= BeanUtil.copyToList(tcRouterList,TcRouterVo.class);

        for (TcRouterVo tcRouterVo:tcRouterVoList){
            tcRouterVo.setIsLeaf(false);
            tcRouterLambdaQueryWrapper.clear();
            tcRouterLambdaQueryWrapper.eq(TcRouter::getJudgeEnable,1).eq(TcRouter::getParentId,tcRouterVo.getId());
            List<TcRouter> tcRouterChildrenList=tcRouterMapper.selectList(tcRouterLambdaQueryWrapper);
            if (CollectionUtil.isEmpty(tcRouterChildrenList)){
                tcRouterChildrenList=new ArrayList<>();
            }

            List<TcRouterVo> childrenList=BeanUtil.copyToList(tcRouterChildrenList,TcRouterVo.class);
            childrenList.forEach(item->item.setIsLeaf(true));
            tcRouterVo.setChildren(childrenList);
        }

        return tcRouterVoList;
    }

    @Override
    public List<TcRouterVo> getPrivateRouterList() {
        //获取顶层
        LambdaQueryWrapper<TcRouter> tcRouterLambdaQueryWrapper=new LambdaQueryWrapper<>();
        tcRouterLambdaQueryWrapper
                .eq(TcRouter::getJudgeEnable,1)
                .eq(TcRouter::getJudgePublic,false)
                .eq(TcRouter::getLevel,0);
        List<TcRouter> tcRouterList=tcRouterMapper.selectList(tcRouterLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcRouterList)){
            return new ArrayList<>();
        }
        List<TcRouterVo> tcRouterVoList= BeanUtil.copyToList(tcRouterList,TcRouterVo.class);

        for (TcRouterVo tcRouterVo:tcRouterVoList){
            tcRouterVo.setIsLeaf(false);
            tcRouterLambdaQueryWrapper.clear();
            tcRouterLambdaQueryWrapper
                    .eq(TcRouter::getJudgeEnable,1)
                    .eq(TcRouter::getJudgePublic,false)
                    .eq(TcRouter::getParentId,tcRouterVo.getId());
            List<TcRouter> tcRouterChildrenList=tcRouterMapper.selectList(tcRouterLambdaQueryWrapper);
            if (CollectionUtil.isEmpty(tcRouterChildrenList)){
                tcRouterChildrenList=new ArrayList<>();
            }

            List<TcRouterVo> childrenList=BeanUtil.copyToList(tcRouterChildrenList,TcRouterVo.class);
            childrenList.forEach(item->item.setIsLeaf(true));
            tcRouterVo.setChildren(childrenList);
        }
        //过滤掉无子菜单的菜单
        return tcRouterVoList.stream().filter(item->CollectionUtil.isNotEmpty(item.getChildren())).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addRouter(AddRouterVo addRouterVo) {
        TcRouter parentRouter=addRouterVo.getParentRouter();
        TcRouter addRouter=addRouterVo.getAddRouter();
        if (parentRouter!=null){
            addRouter.setParentId(parentRouter.getId());
            addRouter.setLevel(1);
        }else {
            addRouter.setLevel(0);
            if (StrUtil.isBlank(addRouter.getIcon())){
                addRouter.setIcon("ri:information-line");
            }
        }
        if(addRouter.getJudgePublic()==null){
            addRouter.setJudgePublic(false);
        }
        ValidationResult validationResult= ValidationUtils.validateEntityWithResultIgnore(addRouter, Arrays.asList("id","judgeEnable"));
        if (!validationResult.getIsLegal()){
            throw new CommonException(validationResult.getMsg());
        }

        addRouter.setJudgeEnable(1);
        addRouter.setId(SnowIdUtil.getSnowId());
        if (addRouter.getShowLink()==null){
            addRouter.setShowLink(true);
        }
        if (addRouter.getKeepAlive()==null){
            addRouter.setKeepAlive(true);
        }
        if (addRouter.getHiddenTag()==null){
            addRouter.setHiddenTag(false);
        }

        addRouter.setCreateDate(new Date());
        tcRouterMapper.insert(addRouter);

        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateRouter(TcRouter router) {
        if (router.getLevel()==0){
            if (StrUtil.isBlank(router.getIcon())){
                router.setIcon("ri:information-line");
            }
        }
        router.setUpdateDate(new Date());
        tcRouterMapper.updateById(router);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteRouter(List<TcRouter> tcRouterList) {
        LambdaQueryWrapper<TcRouter> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if (CollectionUtil.isNotEmpty(tcRouterList)){
            for (TcRouter router:tcRouterList){
                if (StrUtil.isBlank(router.getId())){
                    continue;
                }
                //删除父项角色路由关联
                roleRouterRelService.deleteByRouterIdList(Collections.singletonList(router.getId()));
                //删除父项角色路由操作关联表
                routerActionService.deleteRouterActionDataAndRelByRouterIdList(Collections.singletonList(router.getId()));
                //删除父项
                tcRouterMapper.deleteById(router.getId());
                if (router.getLevel()==0){
                    //查询子项
                    lambdaQueryWrapper.clear();
                    lambdaQueryWrapper.eq(TcRouter::getParentId,router.getId());
                    List<TcRouter> childrenList=tcRouterMapper.selectList(lambdaQueryWrapper);
                    if (CollectionUtil.isNotEmpty(childrenList)){
                        List<String> childrenRouterIdList=childrenList.stream().map(TcRouter::getId).collect(Collectors.toList());
                        //删除子项角色路由关联
                        roleRouterRelService.deleteByRouterIdList(childrenRouterIdList);
                        //删除子项角色路由操作关联表
                        routerActionService.deleteRouterActionDataAndRelByRouterIdList(childrenRouterIdList);
                        //删除子项
                        tcRouterMapper.delete(lambdaQueryWrapper);
                    }
                }
            }
        }
        return "操作成功";
    }


    @Override
    public List<RouterWithActionTreeNodeVo> getRouterWithAction(TcRole role) {
        //获取当前角色
        List<TcRouter> publicRouter = getPublicRouter();
        List<TcRouter> privateRouter = getPrivateRouterByRole(role);
        List<TcRouter> allRouter=new ArrayList<>();
        if (CollectionUtil.isNotEmpty(publicRouter)){
            allRouter.addAll(publicRouter);
        }
        if (CollectionUtil.isNotEmpty(privateRouter)){
            allRouter.addAll(privateRouter);
        }
        List<RouterWithActionTreeNodeVo> result=new ArrayList<>();
        if (CollectionUtil.isEmpty(allRouter)){
            return result;
        }
        for (TcRouter router:allRouter){
            RouterWithActionTreeNodeVo vo=new RouterWithActionTreeNodeVo();
            BeanUtil.copyProperties(router,vo);
            List<TcRouterAction> routerActionList = routerActionService.getRouterActionDataByRouter(router);
            List<TcRouterActionTreeNodeVo> routerActionTreeNodeVoList=new ArrayList<>();
            if (CollectionUtil.isNotEmpty(routerActionList)){
                for (TcRouterAction routerAction:routerActionList){
                    TcRouterActionTreeNodeVo routerActionTreeNodeVo=new TcRouterActionTreeNodeVo();
                    BeanUtil.copyProperties(routerAction,routerActionTreeNodeVo);
                    routerActionTreeNodeVo.setIsLeaf(true);
                    routerActionTreeNodeVo.setTitle(StrUtil.format("{}【{}】"
                            ,routerAction.getActionName()
                            ,routerAction.getActionCode()));
                    routerActionTreeNodeVoList.add(routerActionTreeNodeVo);
                }
            }
            vo.setChildren(routerActionTreeNodeVoList);
            result.add(vo);
        }
        //过滤掉无子项操作的菜单
        return result.stream().filter(item->CollectionUtil.isNotEmpty(item.getChildren())).collect(Collectors.toList());
    }
}

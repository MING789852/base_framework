package com.xm.dd.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcDept;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.TcDeptVo;
import com.xm.auth.mapper.TcDeptMapper;
import com.xm.auth.service.ExternalDeptService;
import com.xm.auth.service.TcDeptService;
import com.xm.auth.service.UserDeptRelService;
import com.xm.dd.service.DingDingUserService;
import com.xm.otherSystem.domain.entity.DDeptNum;
import com.xm.otherSystem.domain.entity.KUser;
import com.xm.otherSystem.mapper.DDeptNumMapper;
import com.xm.otherSystem.mapper.KUserMapper;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.lock.LockUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DingDingDeptServiceImpl implements ExternalDeptService {

    private final DDeptNumMapper dDeptNumMapper;

    private final KUserMapper kUserMapper;

    private final TcDeptMapper tcDeptMapper;

    private final UserDeptRelService userDeptRelService;

    private final DingDingUserService dingDingUserService;

    @Override
    public List<TcUser> createTcUserWithDeptId(String deptId) {
        //防止部门人员同步重复进行
        return LockUtil.tryLock("DD_DEPT_USER_INIT","部门和人员同步正在执行，无法重复操作",()->{
            TcDeptService beanByClass = SpringBeanUtil.getBeanByClass(TcDeptService.class);
            TcDeptVo tcDeptVo = beanByClass.selectOne(deptId);
            if (tcDeptVo==null){
                return null;
            }
            List<TcUser> userList=new ArrayList<>();
            recursionRefreshDeptUser(userList, Collections.singletonList(tcDeptVo));
            return userList;
        },(e)->{
            throw new CommonException(StrUtil.format("同步部门人员失败->{}", ExceptionUtil.stacktraceToString(e)));
        });
    }


    private void recursionRefreshDeptUser(List<TcUser> userList,List<TcDeptVo> deptVoList){
        if (CollectionUtil.isNotEmpty(deptVoList)){
            for (TcDeptVo deptVo:deptVoList){
                //先删除部门用户关系
                userDeptRelService.delRelatedByDeptId(deptVo.getId());
                //再进行关联
                String deptId = deptVo.getId();
                LambdaQueryWrapper<KUser> kUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
                kUserLambdaQueryWrapper.eq(KUser::getDeptId,deptId)
                        .eq(KUser::getLeaveType,0)
                        .eq(KUser::getSite,0);
                List<KUser> kUsers = kUserMapper.selectList(kUserLambdaQueryWrapper);
                if (CollectionUtil.isNotEmpty(kUsers)){
                    for (KUser kUser:kUsers){
                        //创建用户
                        TcUser tcUser = dingDingUserService.createTcUserByKUser(kUser);
                        if (tcUser==null){
                            continue;
                        }
                        userList.add(tcUser);
                        //关联部门
                        userDeptRelService.relatedByUserIdAndDeptId(kUser.getId(),deptVo.getId());
                    }
                }
                //判断是否还存在子部门
                if (CollectionUtil.isNotEmpty(deptVo.getChildren())){
                    recursionRefreshDeptUser(userList,deptVo.getChildren());
                }
            }
        }
    }

    @Override
    public String initDept() {
        //防止部门同步重复进行
        return LockUtil.tryLock("DD_DEPT_USER_INIT", "部门和人员同步正在执行，无法重复操作", () -> {
            //先删除
            tcDeptMapper.delete(new QueryWrapper<>());
            //再同步
            List<DDeptNum> dDeptNumList = dDeptNumMapper.selectList(new QueryWrapper<>());
            if (CollectionUtil.isEmpty(dDeptNumList)) {
                throw new CommonException("外部系统部门表查询为空");
            }
            for (DDeptNum dDeptNum : dDeptNumList) {
                TcDept insertTcDept = new TcDept();
                BeanUtils.copyProperties(dDeptNum, insertTcDept);
                TcDept tcDept = tcDeptMapper.selectById(insertTcDept.getId());
                if (tcDept == null) {
                    tcDeptMapper.insert(insertTcDept);
                }
            }
            return "操作成功";
        }, (e) -> {
            throw new CommonException(StrUtil.format("同步部门失败->{}", ExceptionUtil.stacktraceToString(e)));
        });
    }
}

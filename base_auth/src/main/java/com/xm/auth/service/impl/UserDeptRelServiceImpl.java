package com.xm.auth.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.auth.domain.entity.TcUserDeptRel;
import com.xm.auth.mapper.TcUserDeptRelMapper;
import com.xm.auth.service.UserDeptRelService;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserDeptRelServiceImpl implements UserDeptRelService {

    private final TcUserDeptRelMapper userDeptRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void relatedDepartments(String userId, List<Integer> deptIdList) {
        if (CollectionUtil.isEmpty(deptIdList)||StrUtil.isBlank(userId)){
            return;
        }
        //关联部门
        if (CollectionUtil.isNotEmpty(deptIdList)){
            for (Integer deptId:deptIdList){
                //先删除
                delRelatedByUserId(userId);

                //再关联
                TcUserDeptRel tcUserDeptRel=new TcUserDeptRel();
                tcUserDeptRel.setId(SnowIdUtil.getSnowId());
                tcUserDeptRel.setDeptId(String.valueOf(deptId));
                tcUserDeptRel.setUserId(userId);
                tcUserDeptRel.setCreateUser("system");
                userDeptRelMapper.insert(tcUserDeptRel);
            }
        }
    }

    @Override
    public void relatedByUserIdAndDeptId(String userId, String deptId) {
        TcUserDeptRel tcUserDeptRel=new TcUserDeptRel();
        tcUserDeptRel.setId(SnowIdUtil.getSnowId());
        tcUserDeptRel.setDeptId(deptId);
        tcUserDeptRel.setUserId(userId);
        tcUserDeptRel.setCreateUser("system");
        userDeptRelMapper.insert(tcUserDeptRel);
    }

    @Override
    public void delRelatedByUserId(String userId) {
        LambdaQueryWrapper<TcUserDeptRel> tcUserDeptRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        tcUserDeptRelLambdaQueryWrapper.eq(TcUserDeptRel::getUserId,userId);
//        TcUserDeptRel updateUserDeptRel=new TcUserDeptRel();
//        updateUserDeptRel.setJudgeEnable(0);
//        userDeptRelMapper.update(updateUserDeptRel,tcUserDeptRelLambdaQueryWrapper);
        userDeptRelMapper.delete(tcUserDeptRelLambdaQueryWrapper);
    }

    @Override
    public void delRelatedByDeptId(String deptId) {
        LambdaQueryWrapper<TcUserDeptRel> tcUserDeptRelLambdaQueryWrapper=new LambdaQueryWrapper<>();
        tcUserDeptRelLambdaQueryWrapper.eq(TcUserDeptRel::getDeptId,deptId);
//        TcUserDeptRel updateUserDeptRel=new TcUserDeptRel();
//        updateUserDeptRel.setJudgeEnable(0);
//        userDeptRelMapper.update(updateUserDeptRel,tcUserDeptRelLambdaQueryWrapper);
        userDeptRelMapper.delete(tcUserDeptRelLambdaQueryWrapper);
    }

    @Override
    public TcUserDeptRelMapper getMapper() {
        return userDeptRelMapper;
    }
}

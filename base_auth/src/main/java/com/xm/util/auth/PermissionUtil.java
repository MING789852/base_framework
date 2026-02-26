package com.xm.util.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.xm.auth.domain.entity.TcDept;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.service.TcDeptService;
import com.xm.auth.service.TcRoleService;
import com.xm.util.bean.SpringBeanUtil;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class PermissionUtil {
    private static final TcDeptService deptService;
    private static final TcRoleService roleService;

    static {
        deptService= SpringBeanUtil.getBeanByClass(TcDeptService.class);
        roleService= SpringBeanUtil.getBeanByClass(TcRoleService.class);
    }


    public static List<TcUser> allocateRoleToDept(List<String> deptNameList,List<String> roleCodeList){
        if (CollectionUtil.isEmpty(deptNameList)){
            log.error("[部门权限分配]部门名称为空");
            return new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(roleCodeList)){
            log.error("[部门权限分配]角色编码为空");
            return new ArrayList<>();
        }
        List<TcDept> deptList = deptService.selectDeptByDeptNameList(deptNameList);
        if (CollectionUtil.isEmpty(deptList)){
            log.error("[部门权限分配]部门名称->{}不存在",deptNameList);
            return new ArrayList<>();
        }
        List<TcRole> roleList = roleService.selectRoleByRoleCodeList(roleCodeList);
        if (CollectionUtil.isEmpty(roleList)){
            log.error("[部门权限分配]角色编码->{}不存在",roleCodeList);
            return new ArrayList<>();
        }
        List<String> deptIdList = deptList.stream().map(TcDept::getId).collect(Collectors.toList());
        List<String> roleIdList = roleList.stream().map(TcRole::getId).collect(Collectors.toList());
        //初始化部门人员
        List<TcUser> tcUserWithDeptId = deptService.createTcUserWithDeptId(deptIdList);
        //添加角色
        for (String deptId:deptIdList){
            deptService.allocateRoleToDept(deptId,roleIdList);
        }
        List<String> nameList = tcUserWithDeptId.stream().map(TcUser::getNickName).collect(Collectors.toList());
        log.info("[部门权限分配]为部门->{},分配权限->{},人员->{}",deptNameList,roleCodeList,nameList);
        return tcUserWithDeptId;
    }
}

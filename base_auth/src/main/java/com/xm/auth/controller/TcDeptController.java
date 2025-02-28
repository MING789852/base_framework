package com.xm.auth.controller;

import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.TcDeptVo;
import com.xm.auth.domain.vo.UserAndDeptVo;
import com.xm.auth.service.TcDeptService;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("dept")
@RequiredArgsConstructor
public class TcDeptController {
    private final TcDeptService tcDeptService;

    @PostMapping("selectByList")
    public Result<List<TcDeptVo>> selectByList(@RequestBody QueryData queryData){
        return Result.successForData(tcDeptService.selectByList(queryData));
    }

    @PostMapping("findChildrenList")
    public Result<List<TcDeptVo>> findChildrenList(@RequestBody TcDeptVo parent){
        return Result.successForData(tcDeptService.findChildrenList(parent));
    }

    @GetMapping("findUserAndDeptRefByDeptId")
    public Result<List<UserAndDeptVo>> findUserAndDeptRefByDeptId(String deptId){
        return Result.successForData(tcDeptService.findUserAndDeptRefByDeptId(deptId));
    }

    @GetMapping("initDept")
    public Result<String> initDept(){
        //同步部门
        return Result.successForData(tcDeptService.initDept());
    }

    @GetMapping("createTcUserWithDeptId")
    public Result<List<TcUser>> createTcUserWithDeptId(String deptId){
        return Result.successForData(tcDeptService.createTcUserWithDeptId(deptId));
    }

    @PostMapping("allocateRoleToDept")
    public Result<String> allocateRoleToDept(String deptId,@RequestBody List<String> roleCodeList){
        return Result.successForData(tcDeptService.allocateRoleToDept(deptId, roleCodeList));
    }
}

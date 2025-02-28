package com.xm.dd.controller;

import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.UserVo;
import com.xm.core.params.Result;
import com.xm.dd.service.DingDingUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ddUser")
@RequiredArgsConstructor
public class DDUserController {

    private final DingDingUserService dingDingUserService;


    @GetMapping("createTcUserByDingDingUserId")
    public Result<TcUser> createTcUserByDingDingUserId(String userId){
        return Result.successForData(dingDingUserService.createTcUserByDingDingUserId(userId));
    }

    @GetMapping("createTcUserWithLeaderByDingDingUserId")
    public Result<TcUser> createTcUserWithLeaderByDingDingUserId(String userId){
        return Result.successForData(dingDingUserService.createTcUserWithLeaderByDingDingUserId(userId));
    }

    @GetMapping("createTcUserByDingDingName")
    public Result<TcUser> createTcUserByDingDingName(String name){
        return Result.successForData(dingDingUserService.createTcUserByDingDingName(name));
    }

    @GetMapping("createTcUserByDingDingWorkNum")
    public Result<TcUser> createTcUserByDingDingWorkNum(String workNum){
        return Result.successForData(dingDingUserService.createTcUserByDingDingWorkNum(workNum));
    }

    @GetMapping("loginByAuthCode")
    @IgnoreAuth
    public Result<UserVo> loginByAuthCode(String authCode, @RequestParam(required = false) String configKey){
        return Result.successForData(dingDingUserService.loginByDingDingAuthCode(authCode,configKey));
    }
}

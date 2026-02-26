package com.xm.ddUser.controller;

import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.vo.UserVo;
import com.xm.core.params.Result;
import com.xm.ddUser.service.DingDingUserService;
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

    @GetMapping("loginByAuthCode")
    @IgnoreAuth
    public Result<UserVo> loginByAuthCode(String authCode, @RequestParam(required = false) String configKey){
        return Result.successForData(dingDingUserService.loginByDingDingAuthCode(authCode,configKey));
    }
}

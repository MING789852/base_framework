package com.xm.auth.controller;

import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.dto.LoginDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.CaptchaVo;
import com.xm.auth.domain.vo.RefreshTokenVo;
import com.xm.auth.domain.vo.UserVo;
import com.xm.auth.service.AuthService;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("login")
    @IgnoreAuth
    public Result<UserVo> login(@RequestBody LoginDto loginDto){
        return Result.successForData(authService.login(loginDto));
    }

    @GetMapping("createCaptcha")
    @IgnoreAuth
    public Result<CaptchaVo> createCaptcha(){
        return Result.successForData(authService.createCaptchaVo());
    }

    @GetMapping("loginByToken")
    @IgnoreAuth
    public Result<UserVo> loginByToken(String token){
        return Result.successForData(authService.loginByToken(token));
    }


    @PostMapping("doRefreshToken")
    public Result<RefreshTokenVo> doRefreshToken(@RequestBody RefreshTokenVo refreshTokenVo){
        return Result.successForData(authService.doRefreshToken(refreshTokenVo));
    }

    @GetMapping("logout")
    @IgnoreAuth
    public Result<String> logout(){
        return Result.successForData(authService.logout());
    }

    @GetMapping("getCurrentUser")
    @IgnoreAuth
    public Result<TcUser> getCurrentUser(){
        return Result.successForData(authService.getCurrentUser());
    }

}

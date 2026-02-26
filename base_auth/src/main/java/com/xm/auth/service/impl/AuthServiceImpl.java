package com.xm.auth.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWTPayload;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.advice.exception.exception.UnAuthException;
import com.xm.auth.consts.ErrorCode;
import com.xm.auth.domain.dto.LoginDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.CaptchaVo;
import com.xm.auth.domain.vo.RefreshTokenVo;
import com.xm.auth.domain.vo.TokenVo;
import com.xm.auth.domain.vo.UserVo;
import com.xm.auth.service.AuthService;
import com.xm.auth.service.TcUserService;
import com.xm.util.auth.*;
import com.xm.util.auth.enums.TokenKeyEnum;
import com.xm.util.auth.enums.TokenTypeEnum;
import com.xm.util.crypto.aes.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final TcUserService tcUserService;


    @Override
    public CaptchaVo createCaptchaVo() {
        return CaptchaCodeUtil.createCaptcha();
    }

    @Override
    public UserVo login(LoginDto loginDto) {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        String captchaCode = loginDto.getCaptchaCode();
        if (StrUtil.isBlank(username)){
            throw new CommonException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)){
            throw new CommonException("密码不能为空");
        }
        if (StrUtil.isBlank(captchaCode)){
            throw new CommonException("验证码非空");
        }
        //校验尝试失败次数
        if (!LoginUtil.allowTry(username)){
            throw new CommonException(ErrorCode.LoginTryCountExceeded,
                    StrUtil.format("登录尝试次数过多,请{}分钟后尝试",LoginUtil.LoginTryCountTime));
        }
        //先判断验证码是否正确
        if (!CaptchaCodeUtil.verifyCaptcha(captchaCode)){
            //刷新验证码
            throw new CommonException(ErrorCode.CaptchaCodeError,"验证码有误");
        }
        LambdaQueryWrapper<TcUser> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcUser::getUsername,username);
        TcUser tcUser=tcUserService.getMapper().selectOne(lambdaQueryWrapper);
        if (tcUser==null){
            log.error("用户不存在");
            throw new CommonException(ErrorCode.CaptchaCodeError,"用户或密码不正确");
        }
        String userPassword=tcUser.getPassword();
        if (userPassword.equals(AESUtil.encrypt(password))){
            LoginSessionUtil.initSession(tcUser);
            //刷新重试次数
            LoginUtil.resetTryCount(username);
            return LoginSessionUtil.convertToUserVo(tcUser);
        }else {
            log.error("密码错误");
            //添加尝试次数
            LoginUtil.addTryCount(username);
            throw new CommonException(ErrorCode.CaptchaCodeError,"用户或密码不正确");
        }
    }

    @Override
    public UserVo loginByToken(String token) {
        JWTPayload jwtPayload = TokenGenerateUtil.getPayloadByToken(token);
        if (jwtPayload==null){
            log.error("token解析失败");
            throw new CommonException("无效token");
        }
        JSONObject jsonObject=jwtPayload.getClaimsJson();
        String type=jsonObject.getStr(TokenKeyEnum.TYPE.name());
        if (!TokenTypeEnum.LOGIN_TOKEN.name().equals(type)){
            log.error("token类型错误");
            throw new CommonException("无效token");
        }
        String username=jsonObject.getStr(TokenKeyEnum.USER.name());

        LambdaQueryWrapper<TcUser> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcUser::getUsername,username);
        TcUser tcUser=tcUserService.getMapper().selectOne(lambdaQueryWrapper);
        if (tcUser==null){
            log.error("用户不存在");
            throw new CommonException("用户不存在");
        }
        LoginSessionUtil.initSession(tcUser);
        return LoginSessionUtil.convertToUserVo(tcUser);
    }

    @Override
    public RefreshTokenVo doRefreshToken(RefreshTokenVo refreshTokenVo) {
        try {
            Date doRefreshTokenDate= TokenGenerateUtil.getDoRefreshTokenDate();
            RefreshTokenVo newRefreshTokenVo=new RefreshTokenVo();

            TokenVo tokenVo= TokenGenerateUtil.doRefreshToken(refreshTokenVo.getRefreshToken(),doRefreshTokenDate);
            if (tokenVo==null){
                if (!UserInfoUtil.isLogin()){
                    throw new UnAuthException("未登录");
                }else {
                    throw new CommonException("刷新token失败");
                }
            }
            newRefreshTokenVo.setExpires(doRefreshTokenDate);
            newRefreshTokenVo.setRefreshToken(tokenVo.getRefreshToken());
            newRefreshTokenVo.setAccessToken(tokenVo.getAccessToken());
            return newRefreshTokenVo;
        }catch (Exception e){
            log.error("刷新token失败",e);
            throw new CommonException("刷新token失败");
        }
    }

    @Override
    public String logout() {
        LoginSessionUtil.removeCurrentLoginUserSession();
        return "操作成功";
    }

    @Override
    public TcUser getCurrentUser() {
        return UserInfoUtil.getCurrentLoginUserBySessionOrToken();
    }
}

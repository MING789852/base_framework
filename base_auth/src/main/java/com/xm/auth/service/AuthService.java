package com.xm.auth.service;

import com.xm.auth.domain.dto.LoginDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.CaptchaVo;
import com.xm.auth.domain.vo.RefreshTokenVo;
import com.xm.auth.domain.vo.UserVo;

public interface AuthService {

    CaptchaVo createCaptchaVo();

    UserVo login(LoginDto loginDto);

    UserVo loginByToken(String token);

    RefreshTokenVo doRefreshToken(RefreshTokenVo refreshTokenVo);

    String logout();

    TcUser getCurrentUser();
}

package com.xm.util.auth;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.consts.ErrorCode;
import com.xm.auth.domain.vo.CaptchaVo;
import com.xm.util.cache.CacheUtil;

import java.util.concurrent.TimeUnit;

public class CaptchaCodeUtil {
    /**
     * 获验证码rediskey
     */
    private static String getCaptchaRedisKey(){
        String sessionId = HttpRequestUtil.getCurrentHttpSessionId();
        return StrUtil.format("{}:{}","captcha",sessionId);
    }

    /**
     * 生成验证码
     */
    public static CaptchaVo createCaptcha() {
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40,4,20);
        CaptchaVo captchaVo=new CaptchaVo();
        String code = lineCaptcha.getCode();
        String imageBase64Data = lineCaptcha.getImageBase64Data();

        CacheUtil.set(getCaptchaRedisKey(),code,5, TimeUnit.MINUTES);

        captchaVo.setBase64(imageBase64Data);
        return captchaVo;
    }

    /**
     * 校验验证码
     */
    public static boolean verifyCaptcha(String codeVerify) {
        if (StrUtil.isBlank(codeVerify)){
            throw new CommonException("验证码为空");
        }
        String code = (String) CacheUtil.get(getCaptchaRedisKey());
        if (StrUtil.isBlank(code)){
            throw new CommonException(ErrorCode.CaptchaCodeError,"验证码已过期");
        }
        return codeVerify.equalsIgnoreCase(code);
    }
}

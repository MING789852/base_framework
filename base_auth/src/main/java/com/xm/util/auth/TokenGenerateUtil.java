package com.xm.util.auth;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.RegisteredPayload;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.vo.TokenVo;
import com.xm.consts.SessionAttr;
import com.xm.util.auth.enums.TokenKeyEnum;
import com.xm.util.auth.enums.TokenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.session.Session;

import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
public class TokenGenerateUtil {

    private static final byte[] KEY;

    static {
        try {
            //jar打包之后不能用new file
            //fileReader = FileReader.create(new File(ResourceUtil.getResource("privateKey").toURI()));
            KEY= IoUtil.readBytes(ResourceUtil.getStream("privateKey"));
        } catch (Exception e) {
            throw new RuntimeException("密钥不存在");
        }
    }

    //前端刷新token的时间（每1小时刷新一次）
    public static Date getDoRefreshTokenDate(){
        return DateUtil.offset(new Date(), DateField.HOUR,1);
    }


    /**
     * 初始化token
     */
    public static TokenVo initToken(String username, Date doRefreshDate){
        TokenVo tokenVo= TokenGenerateUtil.createTokenVo(username,doRefreshDate);
        HttpSession httpSession=HttpRequestUtil.getCurrentHttpSession();
        if (httpSession!=null){
            httpSession.setAttribute(SessionAttr.attr_tokenKey,tokenVo);
        }else {
            log.error("生成token失败->未登录");
            throw new CommonException("未登录");
        }
        return tokenVo;
    }

    /**
     * 刷新token
     */
    public static TokenVo doRefreshToken(String refreshToken,Date doRefreshDate){
        if (StrUtil.isEmpty(refreshToken)){
            return null;
        }
        JWTPayload jwtPayload= TokenGenerateUtil.getPayloadByToken(refreshToken);
        if (jwtPayload==null){
            log.error("执行刷新token失败->token已过期或token错误");
            return null;
        }
        Object usernameObj= jwtPayload.getClaim(TokenKeyEnum.USER.name());
        if (usernameObj==null){
            log.error("执行刷新token失败->无法从token中解析用户信息");
            return null;
        }
        String type=jwtPayload.getClaim(TokenKeyEnum.TYPE.name()).toString();
        if (!TokenTypeEnum.REFRESH_TOKEN.name().equals(type)){
            log.error("执行刷新token失败->类型非刷新token");
            return null;
        }
        TokenVo tokenVo=initToken(usernameObj.toString(),doRefreshDate);
        log.info("用户->{},刷新accessToken->{}",usernameObj,tokenVo.getAccessToken());
        return tokenVo;
    }

    public static boolean verifyAccessToken(String accessToken){
        Session session=TokenSessionUtil.getCurrentSessionByAccessToken();
        if (session==null){
            log.error("校验accessToken失败->未登录");
            return false;
        }
        TokenVo tokenVo=session.getAttribute(SessionAttr.attr_tokenKey);
        if (tokenVo==null){
            log.error("校验accessToken失败->session中不存在tokenVo");
            return false;
        }
        if (tokenVo.getAccessToken()==null){
            log.error("校验accessToken失败->session中不存在accessToken");
            return false;
        }
        return tokenVo.getAccessToken().equals(accessToken);
    }


    /**
     * 小程序访问token
     * @param openId
     * @param expire
     * @return
     */
    public static String createMiniAccessToken(String openId,Date expire){
        JWTPayload jwtPayload=new JWTPayload();
        jwtPayload
                .setPayload(TokenKeyEnum.openId.name(), openId)
                .setPayload(TokenKeyEnum.expire.name(), expire.getTime());
        return JWTUtil.createToken(jwtPayload.getClaimsJson(),KEY);
    }

    /**
     * 刷新accessToken的token
     * @param username
     * @param expire
     * @return
     */
    public static String createRefreshToken(String username,Date expire){
        expire= DateUtil.offset(expire, DateField.HOUR,1);
        JWTPayload jwtPayload=new JWTPayload();
        jwtPayload
                .setPayload(TokenKeyEnum.USER.name(),username)
                .setPayload(TokenKeyEnum.TYPE.name(),TokenTypeEnum.REFRESH_TOKEN.name())
                .setExpiresAt(expire);
        return JWTUtil.createToken(jwtPayload.getClaimsJson(),KEY);
    }


    /**
     * 访问token
     * @param username
     * @param expire
     * @return
     */
    public static String createAccessToken(String username,Date expire){
        JWTPayload jwtPayload=new JWTPayload();
        jwtPayload
                .setPayload(TokenKeyEnum.USER.name(),username)
                .setPayload(TokenKeyEnum.TYPE.name(), TokenTypeEnum.ACCESS_TOKEN.name())
                .setExpiresAt(expire);
        return JWTUtil.createToken(jwtPayload.getClaimsJson(),KEY);
    }

    public static TokenVo createTokenVo(String username, Date doRefreshDate){
        //accessToken、refreshToken过期时间要比前端执行刷新时间长一小时
        //refreshToken比accessToken过期时间长一个小时
        Date expire = DateUtil.offset(doRefreshDate, DateField.HOUR,1);
        String accessToken= TokenGenerateUtil.createAccessToken(username,expire);
        String refreshToken= TokenGenerateUtil.createRefreshToken(username,expire);
        TokenVo tokenVo=new TokenVo();
        tokenVo.setAccessToken(accessToken);
        tokenVo.setRefreshToken(refreshToken);
        return tokenVo;
    }

    /**
     * 用于免密登录
     * @param username
     * @return
     */
    public static String createLoginToken(String username){
        Date expire= DateUtil.offset(new Date(), DateField.MINUTE,30);
        JWTPayload jwtPayload=new JWTPayload();
        jwtPayload
                .setPayload(TokenKeyEnum.USER.name(),username)
                .setPayload(TokenKeyEnum.TYPE.name(),TokenTypeEnum.LOGIN_TOKEN.name())
                .setExpiresAt(expire);
        return JWTUtil.createToken(jwtPayload.getClaimsJson(),KEY);
    }

    public static Boolean verify(String token){
        return JWTUtil.verify(token,KEY);
    }

    public static JWTPayload getPayloadByToken(String token){
        if (verify(token)){
            JWTPayload jwtPayload = JWTUtil.parseToken(token).getPayload();
            //判断token是否过期
            Object expObj = jwtPayload.getClaim(RegisteredPayload.EXPIRES_AT);
            if (expObj!=null){
                long exp = Long.parseLong(expObj.toString());
                if (System.currentTimeMillis() >= exp){
                    return jwtPayload;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
//        String accessToken = TokenUtil.createAccessToken("admin", DateUtil.offsetMinute(new Date(), 2));
//        System.out.println(accessToken);
        boolean flag2=verify("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJVU0VSIjoiYWRtaW4iLCJUWVBFIjoiQUNDRVNTX1RPS0VOIiwiZXhwIjoxNzM3MzYzNjQ1fQ.QreK9rSjBVozZz9wihxq0zFcDsMtfodFOMJe1lvSHUM");
        System.out.println(flag2);
    }
}

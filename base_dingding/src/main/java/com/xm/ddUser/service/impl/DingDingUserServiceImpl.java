package com.xm.ddUser.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.UserVo;
import com.xm.auth.enums.UserTypeEnum;
import com.xm.auth.service.TcUserService;
import com.xm.auth.service.UserDeptRelService;
import com.xm.configuration.dingding.DingdingConfigContextHolder;
import com.xm.ddUser.service.DingDingUserService;
import com.xm.util.auth.LoginSessionUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.crypto.aes.AESUtil;
import com.xm.util.dingding.user.DingDingUserUtil;
import com.xm.util.dingding.user.requestRes.DDUserDetail;
import com.xm.util.dingding.user.requestRes.DDUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class DingDingUserServiceImpl implements DingDingUserService {

    private final TcUserService  userService;

    private final UserDeptRelService userDeptRelService;

    @Override
    public UserVo loginByDingDingAuthCode(String authCode, String configKey) {
        if (StrUtil.isNotBlank(configKey)){
            DingdingConfigContextHolder.setConfigKey(configKey);
        }
        TcUser tcUser= UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (tcUser==null){
            DDUserInfo dingDingUserInfo= DingDingUserUtil.getUserInfo(authCode);
            String userId = dingDingUserInfo.getUserid();
            log.info("钉钉授权登录根据authCode->{},configKey->{},获取到userId->{}",authCode,configKey,userId);
            tcUser = userService.getMapper().selectById(userId);
            if (tcUser==null){
                //创建用户
                tcUser=createTcUserWithLeaderByDingDingUserId(userId);
            }
            //初始化session
            LoginSessionUtil.initSession(tcUser);
        }
        return LoginSessionUtil.convertToUserVo(tcUser);
    }


    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public TcUser createTcUserByDingDingUserId(String userId){
        if (StrUtil.isBlank(userId)){
            return null;
        }
        TcUser tcUser=userService.selectById(userId);
        if (tcUser==null){
            DDUserDetail dingDingUserDetail= DingDingUserUtil.getUserDetail(userId);
            if (dingDingUserDetail==null){
                return null;
            }
            //创建用户账号
            tcUser=new TcUser();
            tcUser.setId(userId);
            String username= PinyinUtil.getPinyin(dingDingUserDetail.getName(),"");
            tcUser.setUsername(username);
            tcUser.setPassword(AESUtil.encrypt(username+"123"));
            tcUser.setNickName(dingDingUserDetail.getName());
            tcUser.setCreateUser("system");
            tcUser.setUserType(UserTypeEnum.DINGDING.getValue());
            tcUser.setJobNumber(dingDingUserDetail.getJob_number());
            tcUser.setManagerUserid(dingDingUserDetail.getManager_userid());
            try {
                userService.getMapper().insert(tcUser);
            }catch (Exception e){
                log.error("创建用户失败，尝试为用户名添加时间戳再新增",e);
                tcUser.setUsername(tcUser.getUsername()+System.currentTimeMillis());
                userService.getMapper().insert(tcUser);
            }
            //关联部门
            userDeptRelService.relatedDepartments(userId,dingDingUserDetail.getDept_id_list());
        }

        return tcUser;
    }


    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public TcUser createTcUserWithLeaderByDingDingUserId(String userId) {
        //创建账号
        TcUser tcUser=null;
        if (StrUtil.isNotBlank(userId)){
            tcUser=createTcUserByDingDingUserId(userId);
        }
        if (tcUser==null){
            throw new CommonException(StrUtil.format("创建用户失败,userid->{}不存在",userId));
        }
        //创建对应领导人账号
        String manager_userid=tcUser.getManagerUserid();
        if (StrUtil.isNotBlank(manager_userid)){
            createTcUserByDingDingUserId(manager_userid);
        }
        return tcUser;
    }
}

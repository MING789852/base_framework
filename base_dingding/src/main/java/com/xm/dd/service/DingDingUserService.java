package com.xm.dd.service;

import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.UserVo;
import com.xm.otherSystem.domain.entity.KUser;

public interface DingDingUserService {
    UserVo loginByDingDingAuthCode(String authCode, String configKey);

    /**
     * 根据钉钉userid创建账号
     * @param userId
     * @return
     */
    TcUser createTcUserByDingDingUserId(String userId);

    /**
     * 根据外部系统的用户创建账号
     * @param kUser
     * @return
     */
    TcUser createTcUserByKUser(KUser kUser);

    /**
     * 根据钉钉userid创建账号以及其领导人账号
     * @param userId
     * @return
     */
    TcUser createTcUserWithLeaderByDingDingUserId(String userId);


    TcUser createTcUserByDingDingName(String name);


    TcUser createTcUserByDingDingWorkNum(String workNum);
}

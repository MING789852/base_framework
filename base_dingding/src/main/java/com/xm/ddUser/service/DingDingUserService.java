package com.xm.ddUser.service;

import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.UserVo;

public interface DingDingUserService {
    UserVo loginByDingDingAuthCode(String authCode, String configKey);

    /**
     * 根据钉钉userid创建账号
     * @param userId
     * @return
     */
    TcUser createTcUserByDingDingUserId(String userId);

    /**
     * 根据钉钉userid创建账号以及其领导人账号
     * @param userId
     * @return
     */
    TcUser createTcUserWithLeaderByDingDingUserId(String userId);
}

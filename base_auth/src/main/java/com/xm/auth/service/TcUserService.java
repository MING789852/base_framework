package com.xm.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.xm.auth.domain.dto.UpdatePasswordDto;
import com.xm.auth.domain.dto.UpdateUserInfoDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.LoginUserVo;
import com.xm.auth.domain.vo.UserEnableVo;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.module.core.params.QueryData;
import com.xm.module.core.service.BaseService;
import com.xm.util.auth.params.RequestAction;

import java.util.List;
import java.util.Map;

public interface TcUserService extends BaseService<TcUserMapper,TcUser> {
    Page<TcUser> selectByPage(QueryData<TcUser> queryData);

    String changeEnable(UserEnableVo userEnableVo);

    String addSystemUser(List<TcUser> list);

    String saveOrUpdateData(List<TcUser> tcUserList, Boolean updateUser);

    Map<Integer,String> getUserTypeMapping();

    Map<String,String> getUserIdAndNickNameMapping();

    String updatePassword(UpdatePasswordDto updatePasswordDto);

    String updateUserInfo(UpdateUserInfoDto updateUserInfoDto);

    List<TcUser> getUserList();

    List<LoginUserVo> getAllLoginUser();

    String removeLoginUser(List<LoginUserVo> loginUserVoList);

    List<RequestAction> getRequestAction(LoginUserVo loginUserVo);

    TcUser selectById(String id);

    TcUser getUserByUserName(String userName);

    String resetLoginTry(List<TcUser> list);
}

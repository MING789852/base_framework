package com.xm.auth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.dto.UpdatePasswordDto;
import com.xm.auth.domain.dto.UpdateUserInfoDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.result.UserTypeResult;
import com.xm.auth.domain.vo.LoginUserVo;
import com.xm.auth.domain.vo.UserEnableVo;
import com.xm.auth.enums.UserTypeEnum;
import com.xm.auth.mapper.TcUserMapper;
import com.xm.auth.service.ExternalUserService;
import com.xm.auth.service.TcUserRoleRelService;
import com.xm.auth.service.TcUserService;
import com.xm.auth.service.UserDeptRelService;
import com.xm.module.core.params.QueryData;
import com.xm.util.auth.LoginSessionUtil;
import com.xm.util.auth.LoginUtil;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.auth.params.RequestAction;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.crypto.aes.AESUtil;
import com.xm.util.valid.ValidationResult;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TcUserServiceImpl implements TcUserService {

    private final TcUserMapper tcUserMapper;

    private final List<ExternalUserService> externalUserServiceList;

    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    //密码正则（密码格式应为8-18位数字、字母、符号的任意两种组合）
    private final Pattern passwordPattern=Pattern.compile("^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?!([^(0-9a-zA-Z)]|[()])+$)(?!^.*[\\u4E00-\\u9FA5].*$)([^(0-9a-zA-Z)]|[()]|[a-z]|[A-Z]|[0-9]){8,18}$");

    @Override
    public Page<TcUser> selectByPage(QueryData<TcUser> queryData) {
//        queryParams.getQueryParams().put("userType_$_in",Arrays.asList(0,1));
        Page<TcUser> page=new Page<>(queryData.getCurrent(), queryData.getSize());
        page=tcUserMapper.selectPage(page, queryData.generateQueryWrapper());
        page.getRecords().forEach(item->item.setPassword(null));
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String changeEnable(UserEnableVo userEnableVo) {
        List<TcUser> tcUserList=userEnableVo.getTcUserList();
        Integer judgeEnable = userEnableVo.getJudgeEnable();
        if (CollectionUtil.isNotEmpty(tcUserList)){
            List<String> idList=tcUserList.stream().map(TcUser::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
            LambdaQueryWrapper<TcUser> tcUserLambdaQueryWrapper=new LambdaQueryWrapper<>();
            tcUserLambdaQueryWrapper.in(TcUser::getId,idList);

            TcUser temp=new TcUser();
            temp.setJudgeEnable(judgeEnable);
            tcUserMapper.update(temp,tcUserLambdaQueryWrapper);
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addSystemUser(List<TcUser> list) {
        for (TcUser tcUser : list){
            if (tcUser.getUserType()==null){
                tcUser.setUserType(UserTypeEnum.SYSTEM.getValue());
            }
        }
        return saveOrUpdateData(list,true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcUser> tcUserList,Boolean updateUser) {
        if (CollectionUtil.isNotEmpty(tcUserList)){
            for (int i = 0; i < tcUserList.size(); i++) {
                TcUser tcUser=tcUserList.get(i);
                ValidationResult validationResult= ValidationUtils.validateEntityWithResultIgnore(tcUser, Arrays.asList("id","password","judgeEnable"));
                if (!validationResult.getIsLegal()){
                    throw new CommonException(StrUtil.format("第{}行数据错误=>{}",(i+1),validationResult.getMsg()));
                }
                if (StrUtil.isBlank(tcUser.getId())){
                    String username = tcUser.getUsername();
                    TcUser userByUserName = getUserByUserName(username);
                    if (userByUserName==null){
                        if (StrUtil.isBlank(tcUser.getPassword())){
                            throw new CommonException("密码不能为空");
                        }else{
                            if (!passwordPattern.matcher(tcUser.getPassword()).matches()){
                                throw new CommonException("密码格式应为8-18位数字、字母、符号的任意两种组合");
                            }
                        }
                        //加密密码
                        tcUser.setPassword(AESUtil.encrypt(tcUser.getPassword()));
                        //如果用户类型为空，默认系统类型
                        if (tcUser.getUserType()==null){
                            tcUser.setUserType(UserTypeEnum.SYSTEM.getValue());
                        }
                        //设置创建日期
                        tcUser.setCreateDate(new Date());
                        //设置是否禁用
                        tcUser.setJudgeEnable(1);
                        //设置id
                        tcUser.setId(SnowIdUtil.getSnowId());
                        tcUserMapper.insert(tcUser);
                    }
                }else {
                    if (updateUser){
                        if (StrUtil.isNotBlank(tcUser.getPassword())){
                            tcUser.setPassword(AESUtil.encrypt(tcUser.getPassword()));
                        }
                        tcUser.setUpdateDate(new Date());
                        tcUserMapper.updateById(tcUser);
                    }
                }
            }
        }
        return "操作成功";
    }

    @Override
    public Map<Integer, String> getUserTypeMapping() {
        Map<Integer, String> userTypeMapping = Arrays.stream(UserTypeEnum.values())
                .collect(Collectors.toMap(UserTypeEnum::getValue, UserTypeEnum::getLabel));
        if (CollectionUtil.isNotEmpty(externalUserServiceList)){
            Map<Integer, String> externalUserTypeMapping = externalUserServiceList.stream()
                    .map(ExternalUserService::getUserTypeMapping)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(UserTypeResult::getValue, UserTypeResult::getLabel));
            userTypeMapping.putAll(externalUserTypeMapping);
        }
        return userTypeMapping;
    }

    @Override
    public Map<String, String> getUserIdAndNickNameMapping() {
        LambdaQueryWrapper<TcUser> userLambdaQueryWrapper=new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(TcUser::getJudgeEnable,1);
        List<TcUser> tcUserList = tcUserMapper.selectList(userLambdaQueryWrapper);
        if (CollectionUtil.isEmpty(tcUserList)){
            return new HashMap<>();
        }
        return tcUserList.stream().collect(Collectors.toMap(TcUser::getId,TcUser::getNickName));
    }

    @Override
    public String updatePassword(UpdatePasswordDto updatePasswordDto) {
        TcUser userBySession = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        String newPassword = updatePasswordDto.getNewPassword();
        String oldPassword = updatePasswordDto.getOldPassword();
        if (StrUtil.isBlank(oldPassword)){
            throw new CommonException("旧密码不能为空");
        }
        if (StrUtil.isBlank(newPassword)){
            throw new CommonException("新密码不能为空");
        }
        TcUser databaseUser=tcUserMapper.selectById(userBySession.getId());
        if (databaseUser==null){
            throw new CommonException("用户已被删除");
        }
        String databaseUserPassword = databaseUser.getPassword();
        if (!databaseUserPassword.equals(AESUtil.encrypt(oldPassword))){
            throw new CommonException("旧密码错误");
        }

        if (!passwordPattern.matcher(newPassword).matches()){
            throw new CommonException("密码格式应为8-18位数字、字母、符号的任意两种组合");
        }

        TcUser update=new TcUser();
        update.setId(databaseUser.getId());
        update.setPassword(AESUtil.encrypt(newPassword));
        update.setUpdateDate(new Date());
        tcUserMapper.updateById(update);

        return "操作成功";
    }

    @Override
    public String updateUserInfo(UpdateUserInfoDto updateUserInfoDto) {
        TcUser userBySession = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        String email = updateUserInfoDto.getEmail();
        if (StrUtil.isBlank(email)){
            throw new CommonException("邮箱不能为空");
        }
        email=email.trim();
        if (!emailPattern.matcher(email).matches()){
            throw new CommonException("邮箱格式错误");
        }
        TcUser databaseUser=tcUserMapper.selectById(userBySession.getId());
        if (databaseUser==null){
            throw new CommonException("用户已被删除");
        }

        TcUser update=new TcUser();
        update.setId(databaseUser.getId());
        update.setEmail(email);
        update.setUpdateDate(new Date());
        tcUserMapper.updateById(update);

        return "操作成功";
    }

    @Override
    public List<TcUser> getUserList() {
        LambdaQueryWrapper<TcUser> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcUser::getUserType,UserTypeEnum.DINGDING.getValue());
        List<TcUser> tcUserList=tcUserMapper.selectList(lambdaQueryWrapper);
        tcUserList.forEach(item->{
            item.setPassword("");
            item.setPhoneNumber("");
            item.setEmail("");
        });
        return tcUserList;
    }

    @Override
    public List<LoginUserVo> getAllLoginUser() {
        Map<String, LoginUserVo> allLoginUserBySession = LoginSessionUtil.getAllLoginUserBySession();
        return new ArrayList<>(allLoginUserBySession.values());
    }

    @Override
    public String removeLoginUser(List<LoginUserVo> loginUserVoList) {
        if (CollectionUtil.isEmpty(loginUserVoList)){
            throw new CommonException("操作用户不能为空");
        }
        for (LoginUserVo loginUserVo:loginUserVoList){
            LoginSessionUtil.removeSessionByUserName(loginUserVo.getUsername());
        }
        return "操作成功";
    }

    @Override
    public List<RequestAction> getRequestAction(LoginUserVo loginUserVo) {
        return UserInfoUtil.getRequestActionByUserName(loginUserVo.getUsername());
    }

    @Override
    public TcUserMapper getMapper() {
        return tcUserMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcUser> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        list=list.stream().filter(item->StrUtil.isNotBlank(item.getId())).collect(Collectors.toList());

        TcUserRoleRelService tcUserRoleRelService = SpringBeanUtil.getBeanByClass(TcUserRoleRelService.class);
        UserDeptRelService userDeptRelService = SpringBeanUtil.getBeanByClass(UserDeptRelService.class);
        for (TcUser tcUser:list){
            //删除用户
            String id = tcUser.getId();
            tcUserMapper.deleteById(id);
            //删除用户角色关联
            tcUserRoleRelService.unRelByUserIdList(Collections.singletonList(id));
            //删除用户部门关联
            userDeptRelService.delRelatedByUserId(id);
            //强制退出登录
            LoginUserVo loginUserVo = new LoginUserVo();
            BeanUtil.copyProperties(tcUser,loginUserVo);
            removeLoginUser(Collections.singletonList(loginUserVo));
        }
        return "操作成功";
    }

    @Override
    public TcUser selectById(String id) {
        if (StrUtil.isBlank(id)){
            return null;
        }
        return tcUserMapper.selectById(id);
    }

    @Override
    public TcUser getUserByUserName(String userName) {
        LambdaQueryWrapper<TcUser> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcUser::getUsername,userName);
        return tcUserMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public String resetLoginTry(List<TcUser> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空,无法操作");
        }
        List<String> usernameList=
                list.stream().map(TcUser::getUsername)
                        .filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(usernameList)){
            throw new CommonException("数据为空,无法操作");
        }
        for (String username:usernameList){
            LoginUtil.resetTryCount(username);
        }
        return "操作成功";
    }
}

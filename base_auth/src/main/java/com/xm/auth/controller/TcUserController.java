package com.xm.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.auth.domain.dto.UpdatePasswordDto;
import com.xm.auth.domain.dto.UpdateUserInfoDto;
import com.xm.auth.domain.entity.TcUser;
import com.xm.auth.domain.vo.LoginUserVo;
import com.xm.auth.domain.vo.UserEnableVo;
import com.xm.auth.service.TcUserService;
import com.xm.module.core.params.QueryData;
import com.xm.core.params.Result;
import com.xm.util.auth.params.RequestAction;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class TcUserController {

    private final TcUserService tcUserService;


    @PostMapping("selectByPage")
    public Result<Page<TcUser>> selectByPage(@RequestBody QueryData<TcUser> queryData){
        return Result.successForData(tcUserService.selectByPage(queryData));
    }

    @PostMapping("updatePassword")
    public Result<String> updatePassword(@RequestBody UpdatePasswordDto updatePasswordDto){
        return Result.successForData(tcUserService.updatePassword(updatePasswordDto));
    }

    @PostMapping("updateUserInfo")
    public Result<String> updatePassword(@RequestBody UpdateUserInfoDto updateUserInfoDto){
        return Result.successForData(tcUserService.updateUserInfo(updateUserInfoDto));
    }

    @PostMapping("changeEnable")
    public Result<String> changeEnable(@RequestBody UserEnableVo userEnableVo){
        return Result.successForData(tcUserService.changeEnable(userEnableVo));
    }

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<TcUser> tcUserList){
        return Result.successForData(tcUserService.addSystemUser(tcUserList));
    }

    @GetMapping("getUserIdAndNickNameMapping")
    public Result<Map<String, String>>  getUserIdAndNickNameMapping(){
        return Result.successForData(tcUserService.getUserIdAndNickNameMapping());
    }

    @GetMapping("/getUserList")
    public Result<List<TcUser>> getUserList(){
        return Result.successForData(tcUserService.getUserList());
    }

    @GetMapping("/getAllLoginUser")
    public Result<List<LoginUserVo>> getAllLoginUser(){
        return Result.successForData(tcUserService.getAllLoginUser());
    }

    @PostMapping("/removeLoginUser")
    public Result<String> removeLoginUser(@RequestBody List<LoginUserVo> loginUserVoList){
        return Result.successForData(tcUserService.removeLoginUser(loginUserVoList));
    }

    @PostMapping("/getRequestAction")
    public Result<List<RequestAction>> getRequestAction(@RequestBody LoginUserVo loginUserVo){
        return Result.successForData(tcUserService.getRequestAction(loginUserVo));
    }

    @GetMapping("/getUserTypeMapping")
    public Result<Map<Integer, String>> getUserTypeMapping(){
        return Result.successForData(tcUserService.getUserTypeMapping());
    }

    @PostMapping("/deleteData")
    public Result<String> deleteData(@RequestBody List<TcUser> list){
        return Result.successForData(tcUserService.deleteData(list));
    }

    @PostMapping("/resetLoginTry")
    public Result<String> resetLoginTry(@RequestBody List<TcUser> list){
        return Result.successForData(tcUserService.resetLoginTry(list));
    }
}

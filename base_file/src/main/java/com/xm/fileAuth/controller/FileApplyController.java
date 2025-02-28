package com.xm.fileAuth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.params.Result;
import com.xm.fileAuth.domain.dto.ApplyFileDto;
import com.xm.fileAuth.domain.dto.AuthApplyFileDto;
import com.xm.fileAuth.domain.entity.TcFileApply;
import com.xm.fileAuth.domain.result.FileAuthStatusResult;
import com.xm.fileAuth.domain.vo.TcFileApplyVo;
import com.xm.fileAuth.service.TcFileApplyService;
import com.xm.module.core.params.QueryData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("fileApply")
@RequiredArgsConstructor
public class FileApplyController {
    private final TcFileApplyService fileApplyService;

    @PostMapping("fileApply")
    public Result<String> fileApply(@RequestBody ApplyFileDto applyFileDto){
        return Result.successForData(fileApplyService.fileApply(applyFileDto));
    }

    @PostMapping("fileAuth")
    public Result<String> fileAuth(@RequestBody AuthApplyFileDto authApplyFileDto){
        return Result.successForData(fileApplyService.fileAuth(authApplyFileDto));
    }

    @PostMapping("checkFileAuthStatus")
    public Result<FileAuthStatusResult> checkFileAuthStatus(@RequestBody List<String> fileIdList){
        return Result.successForData(fileApplyService.checkFileAuthStatus(fileIdList));
    }

    @PostMapping("selectByPage")
    public Result<Page<TcFileApply>> selectByPage(@RequestBody QueryData<TcFileApply> queryData){
        return Result.successForData(fileApplyService.selectByPage(queryData));
    }

    @PostMapping("fillData")
    public Result<TcFileApplyVo> fillData(@RequestBody TcFileApply fileApply){
        return Result.successForData(fileApplyService.fillData(fileApply));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcFileApply> list){
        return Result.successForData(fileApplyService.deleteData(list));
    }
}

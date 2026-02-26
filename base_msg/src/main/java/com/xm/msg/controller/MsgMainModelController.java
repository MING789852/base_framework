package com.xm.msg.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.msg.params.Msg;
import com.xm.core.params.Result;
import com.xm.module.core.params.QueryData;
import com.xm.msg.domain.dto.MsgDto;
import com.xm.msg.domain.entity.TcMsg;
import com.xm.msg.service.MsgMainModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("msgMainModel")
public class MsgMainModelController {
    private final MsgMainModelService msgMainModelService;

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<TcMsg> list) {
        return Result.successForData(msgMainModelService.deleteData(list));
    }

    @PostMapping("saveOrUpdateMsg")
    public Result<String> saveOrUpdateMsg(@RequestBody List<MsgDto> list) {
        return Result.successForData(msgMainModelService.saveOrUpdateMsg(list));
    }

    @PostMapping("finishMsg")
    public Result<String> finishMsg(@RequestBody List<Msg> list) {
        return Result.successForData(msgMainModelService.finishMsg(list));
    }


    @PostMapping("selectByPage")
    public Result<Page<TcMsg>> selectByPage(@RequestBody QueryData<TcMsg> queryData) {
        return Result.successForData(msgMainModelService.selectByPage(queryData));
    }

    @GetMapping("getDictMapping")
    public Result<Map<String,Object>> getDictMapping() {
        return Result.successForData(msgMainModelService.getDictMapping());
    }
}

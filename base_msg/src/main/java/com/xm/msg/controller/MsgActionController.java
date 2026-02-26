package com.xm.msg.controller;

import com.xm.core.params.Result;
import com.xm.core.msg.MsgActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("msgAction")
public class MsgActionController {
    private final MsgActionService msgActionService;

    @GetMapping("jumpAfterFinishMsg")
    public Result<String> jumpAfterFinishMsg(String businessType, String businessKey, boolean all) {
        return Result.successForData(msgActionService.jumpAfterFinishMsg(businessType, businessKey, all));
    }
}

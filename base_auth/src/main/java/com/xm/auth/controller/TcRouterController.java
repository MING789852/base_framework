package com.xm.auth.controller;

import cn.hutool.json.JSONObject;
import com.xm.annotation.IgnoreAuth;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.vo.AddRouterVo;
import com.xm.auth.domain.vo.RouterWithActionTreeNodeVo;
import com.xm.auth.domain.vo.TcRouterVo;
import com.xm.auth.service.TcRouterService;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("router")
@RequiredArgsConstructor
public class TcRouterController {

    private final TcRouterService tcRouterService;

    @GetMapping("getCurrentRouter")
    @IgnoreAuth
    public Result<JSONObject> getCurrentRouter(){
        return Result.successForData(tcRouterService.getCurrentRouter());
    }


    @GetMapping("getRouterList")
    public Result<List<TcRouterVo>> getRouterList(){
        return Result.successForData(tcRouterService.getRouterList());
    }

    @GetMapping("getPrivateRouterList")
    public Result<List<TcRouterVo>> getPrivateRouterList(){
        return Result.successForData(tcRouterService.getPrivateRouterList());
    }

    @PostMapping("addRouter")
    public Result<String> addRouter(@RequestBody AddRouterVo addRouterVo){
        return Result.successForData(tcRouterService.addRouter(addRouterVo));
    }


    @PostMapping("updateRouter")
    public Result<String> updateRouter(@RequestBody TcRouter router){
        return Result.successForData(tcRouterService.updateRouter(router));
    }


    @PostMapping("deleteRouter")
    public Result<String> deleteRouter(@RequestBody List<TcRouter> tcRouterList){
        return Result.successForData(tcRouterService.deleteRouter(tcRouterList));
    }

    @PostMapping("getRouterWithAction")
    public Result<List<RouterWithActionTreeNodeVo>> getRouterWithAction(@RequestBody TcRole role){
        return Result.successForData(tcRouterService.getRouterWithAction(role));
    }
}

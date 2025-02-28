package com.xm.auth.controller;

import com.xm.auth.domain.dto.RouterActionSaveDataDto;
import com.xm.auth.domain.dto.RouterActionSaveRefDto;
import com.xm.auth.domain.entity.TcRole;
import com.xm.auth.domain.entity.TcRouter;
import com.xm.auth.domain.entity.TcRouterAction;
import com.xm.auth.service.TcRouterActionService;
import com.xm.core.params.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("routerAction")
@RequiredArgsConstructor
public class TcRouterActionController {

    private final TcRouterActionService routerActionService;

    @PostMapping("getRouterActionDataByRouter")
    public Result<List<TcRouterAction>> getRouterActionDataByRouter(@RequestBody TcRouter router){
        return Result.successForData(routerActionService.getRouterActionDataByRouter(router));
    }

    @PostMapping("saveRouterActionData")
    public Result<String> saveRouterActionData(@RequestBody RouterActionSaveDataDto saveDto){
        return Result.successForData(routerActionService.saveRouterActionData(saveDto));
    }

    @PostMapping("saveRouterActionRef")
    public Result<String> saveRouterActionRef(@RequestBody RouterActionSaveRefDto saveRefDto){
        return Result.successForData(routerActionService.saveRouterActionRef(saveRefDto));
    }

    @PostMapping("getRouterActionRefByRole")
    public Result<List<String>> getRouterActionRefByRole(@RequestBody TcRole role){
        return Result.successForData(routerActionService.getRouterActionRefByRole(role));
    }

    @PostMapping("deleteRouterActionData")
    public Result<String> deleteRouterActionData(@RequestBody List<TcRouterAction> routerActionList){
        return Result.successForData(routerActionService.deleteRouterActionData(routerActionList));
    }
}

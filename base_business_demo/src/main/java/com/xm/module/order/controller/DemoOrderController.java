package com.xm.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.params.Result;
import com.xm.module.core.params.QueryData;
import com.xm.module.order.domain.entity.DemoOrder;
import com.xm.module.order.domain.params.OrderExportParams;
import com.xm.module.order.service.DemoOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("order")
@RestController
@RequiredArgsConstructor
public class DemoOrderController {
    private final DemoOrderService demoOrderService;

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody List<DemoOrder> list) {
        return Result.successForData(demoOrderService.saveOrUpdateData(list));
    }

    @PostMapping("deleteData")
    public Result<String> deleteData(@RequestBody List<DemoOrder> list) {
        return Result.successForData(demoOrderService.deleteData(list));
    }

    @PostMapping("selectByPage")
    public Result<Page<DemoOrder>> selectByPage(@RequestBody QueryData<DemoOrder> queryData) {
        return Result.successForData(demoOrderService.selectByPage(queryData));
    }

    @PostMapping("exportExcel")
    public void exportExcel(@RequestBody OrderExportParams params, HttpServletResponse response) {
        demoOrderService.exportExcel(params, response);
    }
}

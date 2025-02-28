package com.xm.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.module.core.params.QueryData;
import com.xm.module.core.service.BaseService;
import com.xm.module.order.domain.entity.DemoOrder;
import com.xm.module.order.domain.params.OrderExportParams;
import com.xm.module.order.mapper.DemoOrderMapper;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DemoOrderService extends BaseService<DemoOrderMapper, DemoOrder> {
    @Override
    DemoOrderMapper getMapper();

    @Override
    default List<DemoOrder> selectByList(QueryData<DemoOrder> queryData) {
        return BaseService.super.selectByList(queryData);
    }

    @Override
    default Page<DemoOrder> selectByPage(QueryData<DemoOrder> queryData) {
        return BaseService.super.selectByPage(queryData);
    }

    @Override
    default String deleteData(List<DemoOrder> list) {
        return BaseService.super.deleteData(list);
    }

    @Override
    default String saveOrUpdateData(List<DemoOrder> list) {
        return BaseService.super.saveOrUpdateData(list);
    }

    void exportExcel(OrderExportParams params, HttpServletResponse response);
}

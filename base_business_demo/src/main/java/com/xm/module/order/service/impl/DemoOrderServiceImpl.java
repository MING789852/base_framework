package com.xm.module.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.core.params.ColumnProps;
import com.xm.module.core.params.QueryData;
import com.xm.module.order.domain.entity.DemoOrder;
import com.xm.module.order.domain.params.OrderExportParams;
import com.xm.module.order.mapper.DemoOrderMapper;
import com.xm.module.order.service.DemoOrderService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.common.CommonUtil;
import com.xm.util.excel.ExcelUtils;
import com.xm.util.excel.params.DynamicExcelWithMerge;
import com.xm.util.id.SnowIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoOrderServiceImpl implements DemoOrderService {

    private final DemoOrderMapper demoOrderMapper;

    @Override
    public DemoOrderMapper getMapper() {
        return demoOrderMapper;
    }

    @Override
    public List<DemoOrder> selectByList(QueryData<DemoOrder> queryData) {
        //默认按创建日期降序
        queryData.getWrapper().orderByDesc("create_date");
        return DemoOrderService.super.selectByList(queryData);
    }

    @Override
    public Page<DemoOrder> selectByPage(QueryData<DemoOrder> queryData) {
        return DemoOrderService.super.selectByPage(queryData);
    }

    @Override
    public String deleteData(List<DemoOrder> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("请选中数据后操作");
        }
        List<String> deleteIdList = list.stream().map(DemoOrder::getId).filter(StrUtil::isNotBlank).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(deleteIdList)){
            LambdaQueryWrapper<DemoOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(DemoOrder::getId,deleteIdList);
            getMapper().delete(queryWrapper);
        }
        return "操作成功";
    }

    @Override
    public String saveOrUpdateData(List<DemoOrder> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空，无法保存");
        }
        Date now=new Date();
        TcUser currentLoginUser = UserInfoUtil.getCurrentLoginUserBySessionOrTokenNotNull();
        for (DemoOrder order:list) {
            if (StrUtil.isBlank(order.getId())){
                order.setId(SnowIdUtil.getSnowId());
                order.setCreateUser(currentLoginUser.getNickName());
                order.setCreateDate(now);
                getMapper().insert(order);
            }else {
                order.setUpdateUser(currentLoginUser.getNickName());
                order.setUpdateDate(now);
                getMapper().updateById(order);
            }
        }
        return "操作成功";
    }

    @Override
    public void exportExcel(OrderExportParams params, HttpServletResponse response) {
        try {
            QueryData<DemoOrder> queryData = params.getQueryData();
            if (queryData==null){
                throw new CommonException("查询条件为空，无法导出excel");
            }
            List<ColumnProps> mainColumns = params.getMainColumns();
            List<DemoOrder> demoOrders = selectByList(queryData);
            DynamicExcelWithMerge<Object> dynamicExcelWithMerge=new DynamicExcelWithMerge<>();
            dynamicExcelWithMerge.setMainTableData(CommonUtil.toMapList(demoOrders));
            dynamicExcelWithMerge.setMainColumns(mainColumns);
            Workbook workbook = ExcelUtils.exportDynamicDataWithMergeToExcel(dynamicExcelWithMerge);
            ExcelUtils.exportExcel(workbook,response);
        } catch (Exception e) {
            String msg=StrUtil.format("导出excel失败->{}", ExceptionUtil.stacktraceToString(e));
            log.error(msg);
            throw new CommonException(msg);
        }
    }
}

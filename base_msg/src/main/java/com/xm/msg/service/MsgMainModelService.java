package com.xm.msg.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.core.msg.params.Msg;
import com.xm.module.core.params.QueryData;
import com.xm.module.core.service.BaseService;
import com.xm.msg.domain.entity.TcMsg;
import com.xm.msg.mapper.TcMsgMapper;

import java.util.List;
import java.util.Map;

public interface MsgMainModelService extends BaseService<TcMsgMapper, TcMsg> {
    @Override
    TcMsgMapper getMapper();

    @Override
    default List<TcMsg> selectByList(QueryData<TcMsg> queryData) {
        return BaseService.super.selectByList(queryData);
    }

    @Override
    default Page<TcMsg> selectByPage(QueryData<TcMsg> queryData) {
        return BaseService.super.selectByPage(queryData);
    }

    @Override
    String deleteData(List<TcMsg> list);


    String saveOrUpdateMsg(List<Msg> list);

    Map<String,Object> getDictMapping();
}

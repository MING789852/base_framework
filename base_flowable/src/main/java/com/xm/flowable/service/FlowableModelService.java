package com.xm.flowable.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.flowable.domain.dto.ChangeModel;
import com.xm.module.core.params.QueryData;
import com.xm.module.core.service.BaseService;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.query.ProcessInstancePageQuery;
import com.xm.flowable.domain.vo.FlowableModelVo;
import com.xm.flowable.domain.vo.ProcessInstanceVo;
import com.xm.flowable.mapper.TcFlowableModelMapper;

import java.util.List;
import java.util.Map;

public interface FlowableModelService extends BaseService<TcFlowableModelMapper,TcFlowableModel> {

    String initUnRecordModel();

    void recordModel(String processDefinitionKey, String processDefinitionName);

    String saveModel(FlowableModelVo flowableModelVo);

    String changeModelNode(ChangeModel changeModel);

    String changeModelEdge(ChangeModel changeModel);

    String reSendApproveMsg(List<ProcessInstanceVo> processInstanceVoList);

    FlowableModelVo fillModel(TcFlowableModel flowableModel);

    @Override
    TcFlowableModelMapper getMapper();

    @Override
    default List<TcFlowableModel> selectByList(QueryData<TcFlowableModel> queryData) {
        return BaseService.super.selectByList(queryData);
    }

    @Override
    default Page<TcFlowableModel> selectByPage(QueryData<TcFlowableModel> queryData) {
        return BaseService.super.selectByPage(queryData);
    }

    @Override
    String deleteData(List<TcFlowableModel> list);


    String forceDeleteData(List<TcFlowableModel> list);

    @Override
    String saveOrUpdateData(List<TcFlowableModel> list);

    Map<String,Object> getDictList();


    Page<ProcessInstanceVo> getProcInsPageByProcDefKey(ProcessInstancePageQuery query);


    String deleteProcessInstance(List<ProcessInstanceVo>  processInstanceVoList);
}

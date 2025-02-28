package com.xm.flowable.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.flowable.domain.dto.SaveProcessBindData;
import com.xm.flowable.domain.entity.TcFlowableModel;
import com.xm.flowable.domain.vo.ProcessBindData;
import com.xm.flowable.service.FlowableModelService;
import com.xm.flowable.service.ProcessBindService;
import com.xm.module.config.domain.entity.TcConfig;
import com.xm.util.config.ConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessBindServiceImpl implements ProcessBindService {

    private final String groupCode = "ProcessBind";

    private final FlowableModelService flowableModelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveProcessBindData(SaveProcessBindData saveProcessBindData) {
        List<ProcessBindData> processBindDataList = saveProcessBindData.getProcessBindDataList();
        String groupName = saveProcessBindData.getGroupName();
        if (CollectionUtil.isEmpty(processBindDataList)){
            throw new CommonException("流程绑定数据不能为空");
        }
        if (StrUtil.isBlank(groupName)){
            throw new CommonException("流程绑定数据groupName不能为空");
        }

        int index = 0;
        Set<String> duplicateCheck = new HashSet<>();
        for (ProcessBindData processBindData : processBindDataList) {
            if (StrUtil.isBlank(processBindData.getProcessModel())){
                throw new CommonException(StrUtil.format("第{}行，绑定模型为空",(index+1)));
            }
            if (StrUtil.isBlank(processBindData.getValue())){
                throw new CommonException(StrUtil.format("第{}行，绑定值为空",(index+1)));
            }
            if (duplicateCheck.contains(processBindData.getValue())){
                throw new CommonException(StrUtil.format("第{}行，绑定值重复",(index+1)));
            }else {
                duplicateCheck.add(processBindData.getValue());
            }
            index++;
        }
        ConfigUtil.saveConfig(groupCode, groupName, JSONUtil.toJsonStr(processBindDataList));
        return "操作成功";
    }

    @Override
    public List<ProcessBindData> getProcessBindData(String groupName) {
        TcConfig config = ConfigUtil.getConfig(groupCode, groupName);
        List<ProcessBindData> processBindDataList=new ArrayList<>();
        if (config==null){
            return processBindDataList;
        }
        String configValue = config.getConfigValue();
        if (StrUtil.isNotBlank(configValue)){
            processBindDataList = JSONUtil.toList(JSONUtil.parseArray(configValue), ProcessBindData.class);
        }
        return processBindDataList;
    }

    @Override
    public Map<String, Object> getDictMapping() {
        Map<String, String> processModel = new HashMap<>();
        List<TcFlowableModel> tcFlowableModels = flowableModelService.getMapper().selectList(new QueryWrapper<>());
        if (CollectionUtil.isNotEmpty(tcFlowableModels)){
            processModel=tcFlowableModels.stream()
                    .collect(Collectors.toMap(TcFlowableModel::getProcessDefinitionKey,TcFlowableModel::getProcessDefinitionName));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("processModel",processModel);
        return result;
    }

    @Override
    public String getProcessBindModel(String groupName, String value) {
        if (StrUtil.isBlank(groupName)){
            throw new CommonException("groupName为空，无法获取绑定模型");
        }
        if (StrUtil.isBlank(value)){
            throw new CommonException("value为空，无法获取绑定模型");
        }
        List<ProcessBindData> processBindData = getProcessBindData(groupName);
        List<ProcessBindData> filterProcessBindData = processBindData.stream().filter(item -> item.getValue().equals(value)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(filterProcessBindData)){
            return null;
        }else {
            return filterProcessBindData.get(0).getProcessModel();
        }
    }
}

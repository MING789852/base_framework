package com.xm.form.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.module.dict.domain.query.DictQuery;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.dto.FormModelDeleteDto;
import com.xm.form.domain.dto.FormModelSaveDto;
import com.xm.form.domain.entity.TcFormDetailModel;
import com.xm.form.domain.entity.TcFormMainInstance;
import com.xm.form.domain.entity.TcFormMainModel;
import com.xm.form.domain.params.DyFormColumn;
import com.xm.form.domain.query.FormModelQuery;
import com.xm.form.domain.vo.FormModelResult;
import com.xm.form.enums.FormFieldTypeEnum;
import com.xm.form.enums.FormModelFileUploadType;
import com.xm.form.handler.ExternalFieldTypeHandler;
import com.xm.form.mapper.TcFormMainModelMapper;
import com.xm.form.service.TcFormDetailModelService;
import com.xm.form.service.TcFormMainInstanceService;
import com.xm.form.service.TcFormMainModelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcFormMainModelServiceImpl implements TcFormMainModelService {

    private final TcFormMainModelMapper mainModelMapper;

    private final TcFormMainInstanceService mainInstanceService;

    private final TcFormDetailModelService detailModelService;

    private final List<ExternalFieldTypeHandler> externalFieldTypeHandlerList;

    @Override
    public TcFormMainModelMapper getMapper() {
        return mainModelMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcFormMainModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        for (TcFormMainModel mainModel:list){
            //判断是否存在表单实例
            String id = mainModel.getId();
            if (mainInstanceService.existById(id)){
                throw new CommonException("表单模型存在实例，无法删除");
            }
            mainModelMapper.deleteById(id);
        }
        //删除表单明细
        List<String> idList = list.stream().map(TcFormMainModel::getId).collect(Collectors.toList());
        detailModelService.deleteByMainIdList(idList);
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcFormMainModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        ValidationUtils.validateEntityListIgnore(list, Collections.singletonList("id"));
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        for (TcFormMainModel mainModel:list){
            if (StrUtil.isBlank(mainModel.getId())){
                mainModel.setId(SnowIdUtil.getSnowId());
                mainModel.setCreateDate(now);
                mainModel.setCreateUser(currentLoginUserBySession.getNickName());
                mainModelMapper.insert(mainModel);
            }else {
                mainModel.setUpdateDate(now);
                mainModel.setUpdateUser(currentLoginUserBySession.getNickName());
                mainModelMapper.updateById(mainModel);
            }
        }
        return "操作成功";
    }

    @Override
    public Map<String,String> getFormMainModelIdAndNameMapping() {
        List<TcFormMainModel> tcFormMainModels = mainModelMapper.selectList(new QueryWrapper<>());
        Map<String,String> dict=new HashMap<>();
        if (CollectionUtil.isEmpty(tcFormMainModels)){
            return dict;
        }
        return tcFormMainModels.stream().collect(Collectors.toMap(TcFormMainModel::getId,TcFormMainModel::getName));
    }

    @Override
    public FormModelResult getFormModelResultById(FormModelQuery query) {
        String mainModelId = query.getMainModelId();
        String formInsId = query.getFormInsId();
        FormModelResult formModelResult=new FormModelResult();
        List<DyFormColumn> formDefines=new ArrayList<>();
        List<DictQuery>  dictQueryType=new ArrayList<>();
        formModelResult.setFormDefines(formDefines);
        formModelResult.setDictQueryType(dictQueryType);
        if (StrUtil.isBlank(mainModelId)){
            return formModelResult;
        }
        List<TcFormDetailModel> detailModelList = detailModelService.getShowDetailDataListByMainModelId(mainModelId);
        for (TcFormDetailModel detailModel:detailModelList){
            DyFormColumn dyFormColumn=new DyFormColumn();
            dyFormColumn.setLabel(detailModel.getFieldName());
            dyFormColumn.setType(detailModel.getFieldType());
            dyFormColumn.setProp(detailModel.getFieldCode());
            dyFormColumn.setRequired(detailModel.getRequired());
            dyFormColumn.setPlaceholder(detailModel.getPlaceholder());
            formDefines.add(dyFormColumn);
            //获取数据字典
            if (Arrays.asList(FormFieldTypeEnum.TREE_SELECT.getValue(),FormFieldTypeEnum.COMMON_SELECT.getValue())
                    .contains(detailModel.getFieldType())){
                if (StrUtil.isBlank(detailModel.getOther1())||StrUtil.isBlank(detailModel.getOther2())){
                    continue;
                }
                DictQuery dictQuery=new DictQuery();
                dictQuery.setDictMappingKey(detailModel.getFieldCode());
                dictQuery.setDictQueryType(detailModel.getOther1());
                dictQuery.setDictGroupCode(detailModel.getOther2());
                dictQueryType.add(dictQuery);
            }
        }
        Map<String, Object> dynamicData = getDynamicData(mainModelId, formInsId);
        formModelResult.setDataMap(dynamicData);
        return formModelResult;
    }

    private Map<String, Object> getDynamicData(String mainModelId,String formInsId){
        Map<String,Object> data=new HashMap<>();
        if (StrUtil.isBlank(formInsId)){
            return data;
        }
        List<TcFormDetailModel> detailModelList = detailModelService.getShowDetailDataListByMainModelId(mainModelId);
        if (CollectionUtil.isEmpty(detailModelList)){
            return data;
        }
        TcFormMainInstance instance = mainInstanceService.getInstanceById(formInsId);
        if (instance==null){
            return data;
        }
        Map<String, ExternalFieldTypeHandler> fieldTypeHandlerMap = externalFieldTypeHandlerList.stream().collect(Collectors.toMap(ExternalFieldTypeHandler::getFieldType, Function.identity()));
        for (TcFormDetailModel detailModel:detailModelList){
            String fieldType = detailModel.getFieldType();
            ExternalFieldTypeHandler externalFieldTypeHandler = fieldTypeHandlerMap.get(fieldType);
            if (externalFieldTypeHandler!=null){
                externalFieldTypeHandler.getData(detailModel,instance.getId(),instance.getRefId(),instance.getRefType(),data);
            }
        }
        return data;
    }

    private void saveOrUpdateDynamicData(String mainModelId,String formInsId,
                                         String refId,String refType,
                                         Map<String, Object> data,
                                         FormModelFileUploadType formModelFileUploadType,
                                         Map<String, UploadFileWithStream> uploadFileWithStreamMap,Map<String,String> tempIdAndFileIdMap){
        if (CollectionUtil.isEmpty(externalFieldTypeHandlerList)){
            throw new CommonException("处理器为空");
        }
        Map<String, ExternalFieldTypeHandler> fieldTypeHandlerMap = externalFieldTypeHandlerList.stream().collect(Collectors.toMap(ExternalFieldTypeHandler::getFieldType, Function.identity()));
        if (StrUtil.isBlank(mainModelId)){
            throw new CommonException("主表单模型ID为空");
        }
        List<TcFormDetailModel> detailModelList = detailModelService.getShowDetailDataListByMainModelId(mainModelId);
        if (CollectionUtil.isEmpty(detailModelList)){
            return;
        }
        for (TcFormDetailModel detailModel:detailModelList){
            String fieldType = detailModel.getFieldType();
            ExternalFieldTypeHandler externalFieldTypeHandler = fieldTypeHandlerMap.get(fieldType);
            if (externalFieldTypeHandler!=null){
                externalFieldTypeHandler.saveData(detailModel,formInsId,refId,refType,data,formModelFileUploadType,uploadFileWithStreamMap,tempIdAndFileIdMap);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TcFormMainInstance saveFormModelData(FormModelSaveDto saveDto) {
        String refType = saveDto.getRefType();
        String refId = saveDto.getRefId();
        String mainModelId = saveDto.getMainModelId();
        String formInsName = saveDto.getFormInsName();
        String formInsCode = saveDto.getFormInsCode();
        String formInsId = saveDto.getFormInsId();
        Map<String, Object> data = saveDto.getData();
        Map<String, String> tempIdAndFileIdMap = saveDto.getTempIdAndFileIdMap();
        FormModelFileUploadType uploadType = saveDto.getUploadType();

        Map<String, UploadFileWithStream> uploadFileWithStreamMap = saveDto.getUploadFileWithStreamMap();
        TcFormMainInstance instance;
        if (StrUtil.isBlank(formInsId)){
            //创建实例
            instance = mainInstanceService.createInstance(formInsCode, formInsName, mainModelId, refId, refType);
            formInsId=instance.getId();
        }else {
            instance = mainInstanceService.getInstanceById(formInsId);
        }
        //保存动态字段
        saveOrUpdateDynamicData(mainModelId,formInsId,refId,refType,data,uploadType,uploadFileWithStreamMap,tempIdAndFileIdMap);
        return instance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFormModelData(FormModelDeleteDto deleteDto) {
        String formInsId = deleteDto.getFormInsId();
        if (StrUtil.isBlank(formInsId)){
            return;
        }
        TcFormMainInstance instance = mainInstanceService.getInstanceById(formInsId);
        if (instance==null){
            return;
        }
        String mainModelId = instance.getFormMainModelId();
        if (StrUtil.isBlank(mainModelId)){
            return;
        }
        List<TcFormDetailModel> detailModelList = detailModelService.getShowDetailDataListByMainModelId(mainModelId);
        if (CollectionUtil.isEmpty(detailModelList)){
            return;
        }
        //删除表单特殊字段
        Map<String, ExternalFieldTypeHandler> fieldTypeHandlerMap = externalFieldTypeHandlerList.stream().collect(Collectors.toMap(ExternalFieldTypeHandler::getFieldType, Function.identity()));
        for (TcFormDetailModel detailModel:detailModelList){
            String fieldType = detailModel.getFieldType();
            ExternalFieldTypeHandler externalFieldTypeHandler = fieldTypeHandlerMap.get(fieldType);
            if (externalFieldTypeHandler!=null){
                externalFieldTypeHandler.deleteData(detailModel,instance.getId(),instance.getRefId(),instance.getRefType());
            }
        }
        //动态表单实例和动态字段
        mainInstanceService.deleteData(Collections.singletonList(instance));
    }

    @Override
    public TcFormMainModel getModelByCode(String code) {
        if (StrUtil.isBlank(code)){
            return null;
        }
        LambdaQueryWrapper<TcFormMainModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TcFormMainModel::getCode,code);
        return mainModelMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public TcFormMainModel getModelById(String id) {
        if (StrUtil.isBlank(id)){
            return null;
        }
        return mainModelMapper.selectById(id);
    }

    @Override
    public List<TcFormDetailModel> getDetailModelByCode(String code) {
        if (StrUtil.isBlank(code)){
            return new ArrayList<>();
        }
        TcFormMainModel modelByCode = getModelByCode(code);
        if (modelByCode==null){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcFormDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormDetailModel::getFormMainModelId,modelByCode.getId())
                .eq(TcFormDetailModel::getJudgeShow,true);
        return detailModelService.getMapper().selectList(lambdaQueryWrapper);
    }

    @Override
    public List<TcFormDetailModel> getDetailModelByMainId(String mainId) {
        if (StrUtil.isBlank(mainId)){
            return new ArrayList<>();
        }
        TcFormMainModel modelById = getModelById(mainId);
        if (modelById==null){
            return new ArrayList<>();
        }
        LambdaQueryWrapper<TcFormDetailModel> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TcFormDetailModel::getFormMainModelId,mainId)
                .eq(TcFormDetailModel::getJudgeShow,true);
        return detailModelService.getMapper().selectList(lambdaQueryWrapper);
    }


}

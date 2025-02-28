package com.xm.status.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.advice.exception.exception.CommonException;
import com.xm.auth.domain.entity.TcUser;
import com.xm.file.domain.dto.UploadFileWithStream;
import com.xm.form.domain.dto.FormModelSaveDto;
import com.xm.form.domain.entity.TcFormMainInstance;
import com.xm.form.service.TcFormMainModelService;
import com.xm.status.domain.dto.StatusChangeAddDto;
import com.xm.status.domain.dto.StatusModelSaveDto;
import com.xm.status.domain.dto.TcStatusDetailInstanceDto;
import com.xm.status.domain.entity.TcStatusDetailInstance;
import com.xm.status.domain.entity.TcStatusDetailModel;
import com.xm.status.domain.entity.TcStatusMainInstance;
import com.xm.status.domain.entity.TcStatusMainModel;
import com.xm.status.domain.query.StatusModelQuery;
import com.xm.status.domain.vo.StatusModelDataResult;
import com.xm.status.domain.vo.StatusModelSaveOrUpdateResult;
import com.xm.status.mapper.TcStatusMainModelMapper;
import com.xm.status.service.TcStatusDetailInstanceService;
import com.xm.status.service.TcStatusDetailModelService;
import com.xm.status.service.TcStatusMainInstanceService;
import com.xm.status.service.TcStatusMainModelService;
import com.xm.util.auth.UserInfoUtil;
import com.xm.util.id.SnowIdUtil;
import com.xm.util.valid.ValidationUtils;
import groovy.util.logging.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcStatusMainModelServiceImpl implements TcStatusMainModelService {

    private final TcStatusMainModelMapper mainModelMapper;
    private final TcStatusDetailModelService detailModelService;
    private final TcStatusMainInstanceService mainInstanceService;
    private final TcFormMainModelService formMainModelService;
    private final TcStatusDetailInstanceService detailInstanceService;

    @Override
    public TcStatusMainModelMapper getMapper() {
        return mainModelMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deleteData(List<TcStatusMainModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        for (TcStatusMainModel mainModel:list){
            if (StrUtil.isBlank(mainModel.getId())){
                continue;
            }
            if (mainInstanceService.existInstanceByMainModelId(mainModel.getId())){
                String msg=StrUtil.format("名称->{},编码->{},存在实例无法删除",mainModel.getName(),mainModel.getCode());
                throw new CommonException(msg);
            }
            //删除主表
            mainModelMapper.deleteById(mainModel.getId());
            //删除明细表
            detailModelService.deleteByMainModelId(mainModel.getId());
        }
        return "操作成功";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveOrUpdateData(List<TcStatusMainModel> list) {
        if (CollectionUtil.isEmpty(list)){
            throw new CommonException("数据为空");
        }
        Date now=new Date();
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        ValidationUtils.validateEntityListIgnore(list, Collections.singletonList("id"));
        for (TcStatusMainModel mainModel:list){
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
    public Map<String, String> getStatusMainModelIdAndNameMapping() {
        List<TcStatusMainModel> tcStatusMainModels = mainModelMapper.selectList(new QueryWrapper<>());
        Map<String,String> dictMapping=new HashMap<>();
        if (CollectionUtil.isEmpty(tcStatusMainModels)){
            return dictMapping;
        }
        return tcStatusMainModels.stream().collect(Collectors.toMap(TcStatusMainModel::getId,TcStatusMainModel::getName));
    }

    @Override
    public StatusModelSaveOrUpdateResult saveStatusModelData(StatusModelSaveDto saveOrUpdateDto) {
        String refId = saveOrUpdateDto.getRefId();
        String refType = saveOrUpdateDto.getRefType();
        String statusMainModelId = saveOrUpdateDto.getStatusMainModelId();
        String statusMainInstanceId = saveOrUpdateDto.getStatusMainInstanceId();
        String statusInsCode = saveOrUpdateDto.getStatusInsCode();
        String statusInsName = saveOrUpdateDto.getStatusInsName();
        List<TcStatusDetailInstanceDto> detailInstanceDtoList = saveOrUpdateDto.getDetailInstanceDtoList();
        Map<String, UploadFileWithStream> uploadFileWithStreamMap = saveOrUpdateDto.getUploadFileWithStreamMap();
        TcStatusMainInstance instance;
        if (StrUtil.isBlank(statusMainInstanceId)){
            instance=mainInstanceService.createInstance(statusInsCode,statusInsName,statusMainModelId,refId,refType);
        }else {
            instance=mainInstanceService.getInstanceById(statusMainInstanceId);
        }
        StatusModelSaveOrUpdateResult saveOrUpdateResult=new StatusModelSaveOrUpdateResult();
        saveOrUpdateResult.setInstance(instance);

        if (CollectionUtil.isNotEmpty(detailInstanceDtoList)){
            List<TcStatusDetailInstanceDto> activeList = detailInstanceDtoList.stream()
                    .filter(TcStatusDetailInstance::getActive).collect(Collectors.toList());
            if (activeList.size()!=1){
                throw new CommonException("激活状态只能存在一个");
            }
            String activeStatus = activeList.get(0).getCode();
            saveOrUpdateResult.setActiveStatus(activeStatus);

            for (TcStatusDetailInstanceDto dto:detailInstanceDtoList){
                //保存表单
                TcStatusDetailModel statusDetailModel = detailModelService.getStatusDetailModelById(dto.getStatusDetailModelId());
                if (statusDetailModel==null){
                    throw new CommonException(StrUtil.format("ID->{},状态明细模型不存在",dto.getStatusDetailModelId()));
                }
                if (StrUtil.isBlank(statusDetailModel.getFormMainModelId())){
                    continue;
                }
                FormModelSaveDto formModelSaveDto=new FormModelSaveDto();
                formModelSaveDto.setFormInsCode(statusDetailModel.getCode());
                formModelSaveDto.setFormInsName(statusDetailModel.getName());
                formModelSaveDto.setData(dto.getFormData());
                formModelSaveDto.setRefType(refType);
                formModelSaveDto.setRefId(refId+"_"+statusDetailModel.getCode());
                formModelSaveDto.setMainModelId(dto.getFormMainModelId());
                formModelSaveDto.setFormInsId(dto.getFormMainInstanceId());
                formModelSaveDto.setUploadFileWithStreamMap(uploadFileWithStreamMap);
                TcFormMainInstance formMainInstance = formMainModelService.saveFormModelData(formModelSaveDto);
                if (StrUtil.isBlank(dto.getFormMainInstanceId())){
                    dto.setFormMainInstanceId(formMainInstance.getId());
                }
                //保存状态实例
                dto.setStatusMainInstanceId(instance.getId());
                //保存关联
                dto.setRefId(refId);
                dto.setRefType(refType);
                //设置id
                dto.setId(SnowIdUtil.getSnowId());
            }
            //保存明细实例
            detailInstanceService.saveDtoData(statusMainInstanceId,detailInstanceDtoList);
        }

        return saveOrUpdateResult;
    }

    @Override
    public StatusModelDataResult getStatusModelResult(StatusModelQuery query) {
        StatusModelDataResult result=new StatusModelDataResult();
        List<TcStatusDetailInstanceDto> detailInstanceDtoList=new ArrayList<>();
        result.setDetailInstanceDtoList(detailInstanceDtoList);

        String statusMainInstanceId = query.getStatusMainInstanceId();
        if (StrUtil.isBlank(statusMainInstanceId)){
            return result;
        }
        List<TcStatusDetailInstance> detailInstanceList = detailInstanceService.getStatusDetailInstanceByMainInsId(statusMainInstanceId);
        if (CollectionUtil.isNotEmpty(detailInstanceList)){
            for (TcStatusDetailInstance instance:detailInstanceList){
                TcStatusDetailInstanceDto dto=new TcStatusDetailInstanceDto();
                BeanUtil.copyProperties(instance,dto);
                TcStatusDetailModel statusDetailModel = detailModelService.getStatusDetailModelById(instance.getStatusDetailModelId());
                if (statusDetailModel==null){
                    throw new CommonException(StrUtil.format("状态明细模型ID->{}已被删除",instance.getStatusDetailModelId()));
                }
                dto.setName(statusDetailModel.getName());
                dto.setCode(statusDetailModel.getCode());
                detailInstanceDtoList.add(dto);
            }
        }
        return result;
    }

    @Override
    public List<TcStatusDetailInstanceDto> statusModelChangeAdd(StatusChangeAddDto statusChangeAddDto) {
        TcUser currentLoginUserBySession = UserInfoUtil.getCurrentLoginUserBySessionOrToken();
        if (currentLoginUserBySession==null){
            throw new CommonException("未登录，无权限操作");
        }
        String statusMainModelId = statusChangeAddDto.getStatusMainModelId();
        String statusDetailModelId = statusChangeAddDto.getStatusDetailModelId();
        if (StrUtil.isBlank(statusDetailModelId)){
            throw new CommonException("状态ID为空,无法推进状态");
        }
        //判断状态是否已被删除
        TcStatusDetailModel statusDetailModel = detailModelService.getStatusDetailModelById(statusDetailModelId);
        if (statusDetailModel==null){
            throw new CommonException(StrUtil.format("状态已在模型中删除"));
        }

        List<TcStatusDetailInstanceDto> statusList = statusChangeAddDto.getStatusList();
        //判断状态是否已存在
        if (CollectionUtil.isNotEmpty(statusList)){
            long count = statusList.stream().filter(item -> statusDetailModelId.equals(item.getStatusDetailModelId())).count();
            if (count!=0){
                throw new CommonException(StrUtil.format("状态已存在"));
            }
        }else {
            statusList=new ArrayList<>();
        }

        //之前状态设置为未激活
        for (TcStatusDetailInstanceDto dto:statusList){
            dto.setActive(false);
        }
        //创建新状态
        TcStatusDetailInstanceDto statusDetailInstanceDto=new TcStatusDetailInstanceDto();
        statusDetailInstanceDto.setName(statusDetailModel.getName());
        statusDetailInstanceDto.setCode(statusDetailModel.getCode());
        statusDetailInstanceDto.setFormMainModelId(statusDetailModel.getFormMainModelId());
        statusDetailInstanceDto.setStatusMainModelId(statusMainModelId);
        statusDetailInstanceDto.setStatusDetailModelId(statusDetailModel.getId());
        statusDetailInstanceDto.setActive(true);
        statusDetailInstanceDto.setCreateDate(new Date());
        statusDetailInstanceDto.setCreateUser(currentLoginUserBySession.getNickName());
        statusList.add(statusDetailInstanceDto);
        //再判断该状态是否存在
        return statusList;
    }
}

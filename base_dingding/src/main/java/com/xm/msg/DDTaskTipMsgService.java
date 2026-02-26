package com.xm.msg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgSaveService;
import com.xm.core.msg.MsgService;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.ErrorSendResult;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.core.msg.params.Msg;
import com.xm.core.msg.params.MsgSendResult;
import com.xm.msg.enums.DDTaskTodoTypeEnum;
import com.xm.util.dingding.jumpUrl.DingDingJumpUrlUtil;
import com.xm.util.dingding.jumpUrl.params.DDJumpUrlParamGenerate;
import com.xm.util.dingding.task.DingDingTaskUtil;
import com.xm.util.dingding.task.commonParams.DDTaskContentFieldListDTO;
import com.xm.util.dingding.task.commonParams.DetailUrlDTO;
import com.xm.util.dingding.task.commonParams.NotifyConfigsDTO;
import com.xm.util.dingding.task.requestParams.DDTaskCreateParam;
import com.xm.util.dingding.task.requestParams.DDTaskUpdateParam;
import com.xm.util.dingding.task.requestRes.DDTaskCreateRes;
import com.xm.util.dingding.task.requestRes.DDTaskDeleteRes;
import com.xm.util.dingding.task.requestRes.DDTaskUpdateRes;
import com.xm.util.http.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service(MsgTypeConst.ddTaskTip)
@Slf4j
@RequiredArgsConstructor
public class DDTaskTipMsgService implements MsgService {

    private final MsgSaveService msgSaveService;

    private final DDUnionIdMappingService mappingService;

    //优先级，取值：
    //10：较低
    //20：普通
    //30：较高
    //40：紧急
    private final int priority=20;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MsgSendResult createOrUpdateMsg(List<Msg> msgList) {
        MsgSendResult result = new MsgSendResult();
        if (CollectionUtil.isEmpty(msgList)){
            return result;
        }
        for (Msg msg:msgList){
            List<String> userIdList = msg.getToUserIdList();
            if (CollectionUtil.isEmpty(userIdList)){
                throw new CommonException("【钉钉待办消息】用户为空，无法创建或更新待办");
            }
            //判断是否使用直接链接
            String subject = msg.getTitle();
            String description = msg.getContent();
            String businessType = msg.getBusinessType();
            String businessKey = msg.getBusinessKey();
            boolean done = msg.isJudgeFinish();
            Map<String, Object> otherMap = msg.getOtherMap();
            String creatorId = msg.getCreatorId();
            JumpUrlParam jumpUrlParam = msg.getJumpUrlParam();

            String jumpUrl=null;
            String todoType=DDTaskTodoTypeEnum.TODO.name();

            if (jumpUrlParam!=null){
                jumpUrl = jumpUrlParam.generate(DDJumpUrlParamGenerate.class);
                //添加额外参数
                Boolean jumpAfterFinish = msg.getJumpAfterFinish();
                if (jumpAfterFinish!=null&&jumpAfterFinish){
                    Map<String, String> queryMap = new HashMap<>();
                    queryMap.put("businessKey", businessKey);
                    queryMap.put("businessType", businessType);
                    queryMap.put("jumpAfterFinish", String.valueOf(jumpAfterFinish));
                    jumpUrl=HttpUtils.buildUrl(jumpUrl,queryMap);
                    todoType = DDTaskTodoTypeEnum.READ.name();
                }
                jumpUrl = DingDingJumpUrlUtil.getJumpDDWorkbenchUrl(jumpUrl);
            }


            String creatorUnionId = null;
            if (StrUtil.isNotBlank(creatorId)){
                creatorUnionId = mappingService.getUnionIdByUserId(creatorId);
            }
            for (String userId:userIdList){
                Msg database=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
                String unionId = mappingService.getUnionIdByUserId(userId);
                if (database==null){
                    if (StrUtil.isBlank(unionId)){
                        log.error("【钉钉待办消息】转换unionId后，待办执行人为空，无法创建或更新待办");
                        result.addFailSendMsg(msg, new ErrorSendResult(null,"转换unionId后，待办执行人为空，无法创建或更新待办",null));
                        continue;
                    }
                    if (StrUtil.isEmpty(subject)){
                        log.error("【钉钉待办消息】待办主题为空，无法创建或更新待办");
                        result.addFailSendMsg(msg, new ErrorSendResult(null,"待办主题为空，无法创建或更新待",null));
                        continue;
                    }
                    DDTaskCreateParam ddTaskCreateParam=new DDTaskCreateParam();
                    if (StrUtil.isNotBlank(jumpUrl)){
                        DetailUrlDTO detailUrl = new DetailUrlDTO();
                        detailUrl.setPcUrl(jumpUrl);
                        detailUrl.setAppUrl(jumpUrl);
                        ddTaskCreateParam.setDetailUrl(detailUrl);
                    }
                    if (creatorUnionId!=null){
                        ddTaskCreateParam.setCreatorId(creatorUnionId);
                    }else {
                        ddTaskCreateParam.setCreatorId(unionId);
                    }
                    ddTaskCreateParam.setDescription(description);
                    ddTaskCreateParam.setSubject(subject);
                    ddTaskCreateParam.setPriority(priority);
                    ddTaskCreateParam.setTodoType(todoType);
                    ddTaskCreateParam.setExecutorIds(Collections.singletonList(unionId));
                    //设置主体字段
                    if (CollectionUtil.isNotEmpty(otherMap)){
                        List<DDTaskContentFieldListDTO> contentFieldList = new ArrayList<>();
                        for (Map.Entry<String,Object> entry:otherMap.entrySet()){
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (value==null){
                                continue;
                            }
                            DDTaskContentFieldListDTO contentFieldListDTO=new DDTaskContentFieldListDTO();
                            contentFieldListDTO.setFieldKey(key);
                            contentFieldListDTO.setFieldValue(value.toString());
                            contentFieldList.add(contentFieldListDTO);
                        }
                        ddTaskCreateParam.setContentFieldList(contentFieldList);
                    }
                    //设置提醒
                    NotifyConfigsDTO notifyConfigs=new NotifyConfigsDTO();
                    notifyConfigs.setDingNotify("1");
                    notifyConfigs.setSendTodoApn("true");
                    notifyConfigs.setSendAssistantChat("true");
                    ddTaskCreateParam.setNotifyConfigs(notifyConfigs);

                    DDTaskCreateRes ddTaskCreateRes = DingDingTaskUtil.taskCreate(ddTaskCreateParam);

                    if (ddTaskCreateRes.isResult()){
                        Msg temp=Msg.builder()
                                .id(ddTaskCreateRes.getId())
                                .businessType(businessType)
                                .businessKey(businessKey)
                                .type(getMsgType())
                                .title(subject)
                                .content(description)
                                .info(JSONUtil.toJsonStr(ddTaskCreateRes))
                                .judgeFinish(false)
                                .toUserIdList(Collections.singletonList(userId)).build();
                        msgSaveService.saveMsg(temp);
                        //添加成功结果返回
                        result.addSuccessSendMsg(temp);
                    }else {
                        //添加失败结果返回
                        result.addFailSendMsg(msg, new ErrorSendResult(ddTaskCreateRes.getCode(), ddTaskCreateRes.getMessage(),null));
                    }
                }else {
                    DDTaskUpdateParam param=new DDTaskUpdateParam();
                    param.setDone(done);
                    param.setSubject(subject);
                    param.setDescription(description);
                    param.setExecutorIds(Collections.singletonList(unionId));
                    //设置提醒
                    NotifyConfigsDTO notifyConfigs=new NotifyConfigsDTO();
                    notifyConfigs.setDingNotify("1");
                    notifyConfigs.setSendTodoApn("true");
                    notifyConfigs.setSendAssistantChat("true");
                    param.setNotifyConfigs(notifyConfigs);
                    //设置主体字段
                    if (CollectionUtil.isNotEmpty(otherMap)){
                        List<DDTaskContentFieldListDTO> contentFieldList = new ArrayList<>();
                        for (Map.Entry<String,Object> entry:otherMap.entrySet()){
                            String key = entry.getKey();
                            Object value = entry.getValue();
                            if (value==null){
                                continue;
                            }
                            DDTaskContentFieldListDTO contentFieldListDTO=new DDTaskContentFieldListDTO();
                            contentFieldListDTO.setFieldKey(key);
                            contentFieldListDTO.setFieldValue(value.toString());
                            contentFieldList.add(contentFieldListDTO);
                        }
                        param.setContentFieldList(contentFieldList);
                    }

                    //更新请求结果信息
                    DDTaskCreateRes info=JSONUtil.toBean(database.getInfo(),DDTaskCreateRes.class);
                    info.setDone(done);
                    info.setSubject(subject);
                    info.setDescription(description);
                    info.setExecutorIds(Collections.singletonList(unionId));
                    info.setContentFieldList(param.getContentFieldList());
                    info.setNotifyConfigs(param.getNotifyConfigs());
                    info.setPriority(priority);
                    try {
                        DDTaskUpdateRes updateRes = DingDingTaskUtil.taskUpdate(param, database.getId());
                        if (updateRes.isResult()){
                            Msg temp=Msg.builder()
                                    .id(database.getId())
                                    .businessType(businessType)
                                    .businessKey(businessKey)
                                    .type(getMsgType())
                                    .title(subject)
                                    .content(description)
                                    .info(JSONUtil.toJsonStr(info))
                                    .judgeFinish(done)
                                    .toUserIdList(Collections.singletonList(userId)).build();
                            msgSaveService.saveMsg(temp);
                            //添加成功结果返回
                            result.addSuccessSendMsg(temp);
                        }else {
                            String message = updateRes.getMessage();
                            if (StrUtil.isNotBlank(message)&&message.contains("task not exist")){
                                log.info("【钉钉待办消息】钉钉待办->{}不存在,直接删除,并重新创建待办",JSONUtil.toJsonStr(msg));
                                //task不存在
                                msgSaveService.deleteMsg(database.getId());
                                //再执行一次发送代办逻辑
                                createOrUpdateMsg(Collections.singletonList(msg));
                            }else {
                                //添加失败结果返回
                                result.addFailSendMsg(msg, new ErrorSendResult(updateRes.getCode(), updateRes.getMessage(),null));
                            }
                        }
                    }catch (Exception e){
                        throw new CommonException(e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    private void deleteMsg(Msg msg) {
        DDTaskCreateRes bean = JSONUtil.toBean(msg.getInfo(), DDTaskCreateRes.class);
        DDTaskDeleteRes ddTaskDeleteRes = DingDingTaskUtil.taskDelete(bean.getId());
        if (ddTaskDeleteRes.isResult()){
            log.info("【钉钉待办消息】删除钉钉待办成功->{}", JSONUtil.toJsonStr(ddTaskDeleteRes));
            msgSaveService.deleteMsg(msg.getId());
        }else {
            String message = ddTaskDeleteRes.getMessage();
            if (StrUtil.isNotBlank(message)&&message.contains("task not exist")){
                log.info("【钉钉待办消息】删除钉钉待办失败，钉钉待办不存在，直接删除->{}", JSONUtil.toJsonStr(msg));
                msgSaveService.deleteMsg(msg.getId());
            }else {
                throw new CommonException(StrUtil.format("【钉钉待办消息】钉钉待办删除失败->{}", message));
            }
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMsgList(String businessType, String businessKey) {
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey,getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                deleteMsg(msg);
            }
        }
        return true;
    }

    @Override
    public boolean deleteMsg(String businessType, String businessKey, String userId) {
        Msg msg=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
        if (msg!=null){
            deleteMsg(msg);
        }
        return true;
    }

    private void finishMsg(Msg msg) {
        DDTaskCreateRes info=JSONUtil.toBean(msg.getInfo(),DDTaskCreateRes.class);
        if (info!=null){
            DDTaskUpdateParam param=new DDTaskUpdateParam();
            param.setDone(true);
            DDTaskUpdateRes ddTaskUpdateRes = DingDingTaskUtil.taskUpdate(param, info.getId());
            if (ddTaskUpdateRes.isResult()){
                log.info("【钉钉待办消息】完结待办成功->{}", JSONUtil.toJsonStr(ddTaskUpdateRes));
                msg.setJudgeFinish(true);
                msg.setType(getMsgType());
                msgSaveService.saveMsg(msg);
            }else {
                String message = ddTaskUpdateRes.getMessage();
                if (StrUtil.isNotBlank(message)&&message.contains("task not exist")){
                    log.info("【钉钉待办消息】完结待办失败，钉钉待办不存在，直接删除->{}",JSONUtil.toJsonStr(msg));
                    //task不存在,直接删除
                    msgSaveService.deleteMsg(msg.getId());
                }else {
                    throw new CommonException(StrUtil.format("【钉钉待办消息】钉钉完结待办失败->{}", message));
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishMsgList(String businessType, String businessKey){
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey,getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                finishMsg(msg);
            }
        }
        return true;
    }

    @Override
    public boolean finishMsg(String businessType, String businessKey, String userId) {
        Msg msg=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
        if (msg!=null){
            finishMsg(msg);
        }
        return true;
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.ddTaskTip;
    }
}

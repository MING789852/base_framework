package com.xm.msg;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.core.msg.MsgSaveService;
import com.xm.core.msg.MsgService;
import com.xm.core.msg.consts.MsgTypeConst;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.core.msg.params.Msg;
import com.xm.util.dingding.DingdingUtil;
import com.xm.util.dingding.requestParams.DDTaskCreateParam;
import com.xm.util.dingding.requestParams.DDTaskUpdateParam;
import com.xm.util.dingding.requestRes.DDTaskCreateRes;
import com.xm.util.dingding.requestRes.DDTaskDeleteRes;
import com.xm.util.dingding.requestRes.DDTaskUpdateRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service(MsgTypeConst.ddTaskTip)
@Slf4j
@RequiredArgsConstructor
public class DDTaskTipMsgService implements MsgService {

    private final MsgSaveService msgSaveService;

    private final DDUnionIdMappingService mappingService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createOrUpdateMsg(List<Msg> msgList) {
        if (CollectionUtil.isEmpty(msgList)){
            throw new CommonException("【钉钉待办消息】消息为空，无法创建或更新待办");
        }
        for (Msg msg:msgList){
            List<String> userIdList = msg.getToUserIdList();
            if (CollectionUtil.isEmpty(userIdList)){
                throw new CommonException("【钉钉待办消息】用户为空，待办审批操作失败");
            }
            JumpUrlParam jumpUrlParam = msg.getJumpUrlParam();
            String jumpUrl=null;
            if (jumpUrlParam!=null){
                jumpUrl = DingdingUtil.getJumpDDWorkbenchUrl(jumpUrlParam);
            }
            String subject = msg.getTitle();
            String description = msg.getContent();
            String businessType = msg.getBusinessType();
            String businessKey = msg.getBusinessKey();
            Boolean done = msg.getJudgeFinish();

            for (String userId:userIdList){
                Msg database=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
                String unionId = mappingService.getUnionIdByUserId(userId);
                if (database==null){
                    if (StrUtil.isBlank(unionId)){
                        throw new CommonException("【钉钉待办消息】待办执行人为空，无法创建待办");
                    }
                    if (StrUtil.isEmpty(subject)){
                        throw new CommonException("【钉钉待办消息】待办主题为空，无法创建待办");
                    }
                    DDTaskCreateParam ddTaskCreateParam=new DDTaskCreateParam();
                    if (StrUtil.isNotBlank(jumpUrl)){
                        DDTaskCreateParam.DetailUrlDTO detailUrl = new DDTaskCreateParam.DetailUrlDTO();
                        detailUrl.setPcUrl(jumpUrl);
                        detailUrl.setAppUrl(jumpUrl);
                        ddTaskCreateParam.setDetailUrl(detailUrl);
                    }
                    ddTaskCreateParam.setCreatorId(unionId);
                    ddTaskCreateParam.setDescription(description);
                    ddTaskCreateParam.setSubject(subject);
                    ddTaskCreateParam.setPriority(20);
                    ddTaskCreateParam.setOnlyShowExecutor(true);
                    ddTaskCreateParam.setExecutorIds(Collections.singletonList(unionId));
                    DDTaskCreateRes ddTaskCreateRes = DingdingUtil.taskCreate(ddTaskCreateParam);

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
                }else {
                    DDTaskCreateRes ddTaskCreateRes=JSONUtil.toBean(database.getInfo(),DDTaskCreateRes.class);
                    ddTaskCreateRes.setDone(done);
                    ddTaskCreateRes.setSubject(subject);
                    ddTaskCreateRes.setDescription(description);
                    ddTaskCreateRes.setExecutorIds(Collections.singletonList(unionId));

                    DDTaskUpdateParam param=new DDTaskUpdateParam();
                    param.setDone(done);
                    param.setSubject(subject);
                    param.setDescription(description);
                    param.setExecutorIds(Collections.singletonList(unionId));
                    try {
                        DDTaskUpdateRes ddTaskUpdateRes = DingdingUtil.taskUpdate(param, database.getId());
                        if (ddTaskUpdateRes.isResult()){
                            Msg temp=Msg.builder()
                                    .id(database.getId())
                                    .businessType(businessType)
                                    .businessKey(businessKey)
                                    .type(getMsgType())
                                    .title(subject)
                                    .content(description)
                                    .info(JSONUtil.toJsonStr(ddTaskCreateRes))
                                    .judgeFinish(done)
                                    .toUserIdList(Collections.singletonList(userId)).build();
                            msgSaveService.saveMsg(temp);
                        }
                    }catch (Exception e){
                        if (e.getMessage().contains("task not exist")){
                            log.info("【钉钉待办消息】钉钉待办->{}不存在,直接删除,并重新创建待办",JSONUtil.toJsonStr(msg));
                            //task不存在
                            msgSaveService.deleteMsg(database.getId());
                            //再执行一次发送代办逻辑
                            createOrUpdateMsg(Collections.singletonList(msg));
                        }else {
                            throw new CommonException(e.getMessage());
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMsgList(String businessType, String businessKey) {
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey,getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                try {
                    DDTaskCreateRes bean = JSONUtil.toBean(msg.getInfo(), DDTaskCreateRes.class);
                    DDTaskDeleteRes ddTaskDeleteRes = DingdingUtil.taskDelete(bean.getId());
                    log.info("【钉钉待办消息】钉钉待办->{}，删除成功", JSONUtil.toJsonStr(ddTaskDeleteRes));
                    msgSaveService.deleteMsg(msg.getId());
                }catch (Exception e){
                    if (e.getMessage().contains("task not exist")) {
                        log.info("【钉钉待办消息】钉钉待办->{}不存在,跳过删除", JSONUtil.toJsonStr(msg));
                        msgSaveService.deleteMsg(msg.getId());
                    }else {
                        throw new CommonException(e.getMessage());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteMsg(String businessType, String businessKey, String userId) {
        Msg msg=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
        if (msg!=null){
            try {
                DDTaskCreateRes bean = JSONUtil.toBean(msg.getInfo(), DDTaskCreateRes.class);
                DDTaskDeleteRes ddTaskDeleteRes = DingdingUtil.taskDelete(bean.getId());
                log.info("【钉钉待办消息】钉钉待办->{}，删除成功", JSONUtil.toJsonStr(ddTaskDeleteRes));
                msgSaveService.deleteMsg(msg.getId());
                return true;
            }catch (Exception e){
                if (e.getMessage().contains("task not exist")) {
                    log.info("【钉钉待办消息】钉钉待办->{}不存在,直接删除", JSONUtil.toJsonStr(msg));
                    msgSaveService.deleteMsg(msg.getId());
                }else {
                    throw new CommonException(e.getMessage());
                }
            }
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishMsgList(String businessType, String businessKey){
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey,getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                DDTaskCreateRes ddTaskCreateRes=JSONUtil.toBean(msg.getInfo(),DDTaskCreateRes.class);
                if (ddTaskCreateRes!=null){
                    try {
                        DDTaskUpdateParam param=new DDTaskUpdateParam();
                        param.setDone(true);
                        DingdingUtil.taskUpdate(param, ddTaskCreateRes.getId());
                        msg.setJudgeFinish(true);
                        msg.setType(getMsgType());
                        msgSaveService.saveMsg(msg);
                    }catch (Exception e){
                        if (e.getMessage().contains("task not exist")){
                            log.info("【钉钉待办消息】钉钉待办->{}不存在,直接删除",JSONUtil.toJsonStr(msg));
                            //task不存在,直接删除
                            msgSaveService.deleteMsg(msg.getId());
                        }else {
                            throw new CommonException(e.getMessage());
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean finishMsg(String businessType, String businessKey, String userId) {
        Msg msg=msgSaveService.getMsg(businessType,businessKey,getMsgType(),userId);
        if (msg==null){
            throw new CommonException(StrUtil.format("【钉钉待办消息】{},{},{}不存在待办信息",businessType,businessKey,userId));
        }
        DDTaskCreateRes ddTaskCreateRes=JSONUtil.toBean(msg.getInfo(),DDTaskCreateRes.class);
        if (ddTaskCreateRes!=null){
            try {
                DDTaskUpdateParam param=new DDTaskUpdateParam();
                param.setDone(true);
                DingdingUtil.taskUpdate(param, ddTaskCreateRes.getId());
                msg.setJudgeFinish(true);
                msg.setType(getMsgType());
                msgSaveService.saveMsg(msg);
            }catch (Exception e){
                if (e.getMessage().contains("task not exist")){
                    log.info("【钉钉待办消息】钉钉待办->{}不存在,直接删除",JSONUtil.toJsonStr(msg));
                    //task不存在,直接删除
                    msgSaveService.deleteMsg(msg.getId());
                }else {
                    throw new CommonException(e.getMessage());
                }
            }
        }
        return true;
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.ddTaskTip;
    }
}

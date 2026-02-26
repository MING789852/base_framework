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
import com.xm.util.dingding.jumpUrl.DingDingJumpUrlUtil;
import com.xm.util.dingding.jumpUrl.params.DDJumpUrlParamGenerate;
import com.xm.util.dingding.robot.DingDingRobotUtil;
import com.xm.util.dingding.robot.requestRes.DDRecallRobotToOneMsgRes;
import com.xm.util.dingding.robot.requestRes.DDSendRobotToOneMsgRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service(MsgTypeConst.ddRobotMsg)
@Slf4j
@RequiredArgsConstructor
public class DDRobotMsgService implements MsgService {

    private final MsgSaveService msgSaveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MsgSendResult createOrUpdateMsg(List<Msg> msgList) {
        MsgSendResult msgSendResult = new MsgSendResult();
        if (CollectionUtil.isEmpty(msgList)){
            throw new CommonException("【钉钉机器人消息】消息为空，机器人消息创建失败");
        }
        for (Msg msg:msgList){
            List<String> userIdList = msg.getToUserIdList();
            if (CollectionUtil.isEmpty(userIdList)){
                throw new CommonException("【钉钉机器人消息】用户为空，机器人消息创建失败");
            }
            for (String userId:userIdList){
                String title = msg.getTitle();
                String content = msg.getContent();
                String businessType = msg.getBusinessType();
                String businessKey = msg.getBusinessKey();
                String jumpUrl=null;
                JumpUrlParam jumpUrlParam = msg.getJumpUrlParam();
                if (jumpUrlParam!=null){
                    jumpUrl = jumpUrlParam.generate(DDJumpUrlParamGenerate.class);
                    jumpUrl = DingDingJumpUrlUtil.getJumpDDWorkbenchUrl(jumpUrl);
                }
                if (StrUtil.isBlank(title)){
                    throw new CommonException("【钉钉机器人消息】消息主题为空，机器人消息创建失败");
                }
                if (StrUtil.isBlank(content)){
                    throw new CommonException("【钉钉机器人消息】消息内容为空，机器人消息创建失败");
                }
                if (StrUtil.isBlank(businessType)){
                    throw new CommonException("【钉钉机器人消息】消息业务类型为空，机器人消息创建失败");
                }
                if (StrUtil.isBlank(businessKey)){
                    throw new CommonException("【钉钉机器人消息】消息业务主键为空，机器人消息创建失败");
                }
                DDSendRobotToOneMsgRes ddSendRobotToOneMsgRes = DingDingRobotUtil.sendRobotToOneSampleActionCardMsg(title, content,
                        Collections.singletonList(userId), jumpUrl);
                if (ddSendRobotToOneMsgRes.isResult()){
                    //被限流的userId列表
                    List<String> flowControlledStaffIdList = ddSendRobotToOneMsgRes.getFlowControlledStaffIdList();
                    if (CollectionUtil.isNotEmpty(flowControlledStaffIdList)){
                        log.info("【钉钉机器人消息】钉钉机器人消息->{}，被限流userId列表->{}", JSONUtil.toJsonStr(ddSendRobotToOneMsgRes), JSONUtil.toJsonStr(flowControlledStaffIdList));
                    }
                    //无效的用户userId列表
                    List<String> invalidStaffIdList = ddSendRobotToOneMsgRes.getInvalidStaffIdList();
                    if (CollectionUtil.isNotEmpty(invalidStaffIdList)){
                        log.info("【钉钉机器人消息】钉钉机器人消息->{}，无效的userId列表->{}", JSONUtil.toJsonStr(ddSendRobotToOneMsgRes), JSONUtil.toJsonStr(invalidStaffIdList));
                    }
                    msg.setInfo(JSONUtil.toJsonStr(ddSendRobotToOneMsgRes));
                    msg.setType(getMsgType());
                    msg.setJudgeFinish(true);
                    msg.setToUserIdList(Collections.singletonList(userId));
                    msg.setId(ddSendRobotToOneMsgRes.getProcessQueryKey());
                    msgSaveService.saveMsg(msg);
                    //添加成功发送消息结果
                    msgSendResult.addSuccessSendMsg(msg);
                }else {
                    //添加失败发送消息结果
                    msgSendResult.addFailSendMsg(msg, new ErrorSendResult(ddSendRobotToOneMsgRes.getCode(),
                            ddSendRobotToOneMsgRes.getMessage(),null));
                }
            }
        }
        return msgSendResult;
    }

    private void deleteRobotMsg(Msg msg) {
        String info = msg.getInfo();
        if (StrUtil.isBlank(info)){
            return;
        }
        DDSendRobotToOneMsgRes sendMsgRes= JSONUtil.toBean(info, DDSendRobotToOneMsgRes.class);
        if (sendMsgRes==null){
            return;
        }
        String processQueryKey = sendMsgRes.getProcessQueryKey();
        DDRecallRobotToOneMsgRes ddRecallRobotToOneMsgRes = DingDingRobotUtil.recallRobotToOneMsg(Collections.singletonList(processQueryKey));
        if (ddRecallRobotToOneMsgRes !=null){
            //成功被撤回的消息
            List<String> successResult = ddRecallRobotToOneMsgRes.getSuccessResult();
            if (CollectionUtil.isNotEmpty(successResult)){
                msgSaveService.deleteMsg(msg.getId());
            }
        }
    }

    @Override
    public boolean deleteMsgList(String businessType, String businessKey) {
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey, getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                deleteRobotMsg(msg);
            }
        }
        return true;
    }

    @Override
    public boolean deleteMsg(String businessType, String businessKey, String userId) {
        Msg msg = msgSaveService.getMsg(businessType, businessKey, getMsgType(), userId);
        if (msg!=null){
            deleteRobotMsg(msg);
        }
        return true;
    }

    @Override
    public boolean finishMsgList(String businessType, String businessKey) {
        log.error("【钉钉机器人消息】businessType->{},businessKey->{},无法完结",businessType,businessKey);
        return false;
    }

    @Override
    public boolean finishMsg(String businessType, String businessKey, String userId) {
        log.error("【钉钉机器人消息】businessType->{},businessKey->{},userId->{},无法完结",businessType,businessKey,userId);
        return false;
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.ddRobotMsg;
    }
}

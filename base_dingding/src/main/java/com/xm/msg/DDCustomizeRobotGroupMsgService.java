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
import com.xm.util.dingding.robot.params.RobotSendToGroupTarget;
import com.xm.util.dingding.robot.requestRes.DDSendRobotToGroupMsgRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(MsgTypeConst.ddCustomizeRobotGroupMsg)
@Slf4j
@RequiredArgsConstructor
public class DDCustomizeRobotGroupMsgService implements MsgService {

    private final MsgSaveService msgSaveService;

    @Override
    public MsgSendResult createOrUpdateMsg(List<Msg> msgList) {
        MsgSendResult msgSendResult = new MsgSendResult();
        if (CollectionUtil.isEmpty(msgList)){
            return msgSendResult;
        }
        for (Msg msg:msgList){
            String groupId = msg.getGroupId();
            String title = msg.getTitle();
            String content = msg.getContent();
            String businessType = msg.getBusinessType();
            String businessKey = msg.getBusinessKey();
            Boolean groupAtAll = msg.getGroupAtAll();
            //判断是否使用直接链接
            String jumpUrl=null;
            JumpUrlParam jumpUrlParam = msg.getJumpUrlParam();
            if (jumpUrlParam!=null){
                jumpUrl = jumpUrlParam.generate(DDJumpUrlParamGenerate.class);
                jumpUrl = DingDingJumpUrlUtil.getJumpDDWorkbenchUrl(jumpUrl);
            }
            List<String> toUserIdList = msg.getToUserIdList();
            if (StrUtil.isBlank(groupId)){
                throw new CommonException("【钉钉自定义机器人群消息】groupId为空,机器人消息创建失败");
            }
            if (StrUtil.isBlank(title)){
                throw new CommonException("【钉钉自定义机器人群消息】消息主题为空，机器人消息创建失败");
            }
            if (StrUtil.isBlank(content)){
                throw new CommonException("【钉钉自定义机器人群消息】消息内容为空，机器人消息创建失败");
            }
            if (StrUtil.isBlank(businessType)){
                throw new CommonException("【钉钉自定义机器人群消息】消息业务类型为空，机器人消息创建失败");
            }
            if (StrUtil.isBlank(businessKey)){
                throw new CommonException("【钉钉自定义机器人群消息】消息业务主键为空，机器人消息创建失败");
            }
            RobotSendToGroupTarget robotSendToGroupTarget = new RobotSendToGroupTarget();
            robotSendToGroupTarget.setGroupAccessToken(groupId);
            robotSendToGroupTarget.setUserIdList(toUserIdList);
            robotSendToGroupTarget.setIsAtAll(Boolean.TRUE.equals(groupAtAll)?"1":"0");
            robotSendToGroupTarget.setButtonParamList(msg.getButtonParamList());
            List<DDSendRobotToGroupMsgRes> ddSendRobotToGroupMsgRes = DingDingRobotUtil
                    .sendCustomizeRobotToGroupSampleActionCardMsg(title, content, Collections.singletonList(robotSendToGroupTarget), jumpUrl);
            if (CollectionUtil.isNotEmpty(ddSendRobotToGroupMsgRes)){
                DDSendRobotToGroupMsgRes res = ddSendRobotToGroupMsgRes.get(0);
                if ("0".equals(res.getErrcode())){
                    Map<String,Object> request=new HashMap<>();
                    request.put("title",title);
                    request.put("content",content);
                    request.put("jumpUrl",jumpUrl);
                    request.put("targetList",Collections.singletonList(robotSendToGroupTarget));
                    Map<String,Object> result=new HashMap<>();
                    result.put("request",request);
                    result.put("response",ddSendRobotToGroupMsgRes);
                    if (CollectionUtil.isEmpty(toUserIdList)){
                        if (Boolean.TRUE.equals(groupAtAll)){
                            toUserIdList= Collections.singletonList("@all");
                        }else {
                            toUserIdList= Collections.singletonList("group");
                        }
                    }
                    Msg temp=Msg.builder()
                            .businessType(businessType)
                            .businessKey(businessKey)
                            .type(getMsgType())
                            .title(title)
                            .content(content)
                            .info(JSONUtil.toJsonStr(result))
                            .judgeFinish(true)
                            .toUserIdList(toUserIdList)
                            .groupId(groupId)
                            .groupAtAll(groupAtAll)
                            .build();
                    msgSaveService.saveMsg(temp);
                    //添加成功发送消息结果
                    msgSendResult.addSuccessSendMsg(msg);
                }else {
                    msg.setErrorSendResult(new ErrorSendResult(res.getErrcode(),res.getErrmsg(),null));
                }
            }
        }
        return msgSendResult;
    }


    private void deleteRobotMsg(Msg msg) {
        msgSaveService.deleteMsg(msg.getId());
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
        List<Msg> msgList = msgSaveService.getMsgList(businessType, businessKey, getMsgType());
        if (CollectionUtil.isNotEmpty(msgList)){
            for (Msg msg:msgList){
                deleteRobotMsg(msg);
            }
        }
        return true;
    }

    @Override
    public boolean finishMsgList(String businessType, String businessKey) {
        log.error("【钉钉自定义机器人群消息】businessType->{},businessKey->{},无法完结",businessType,businessKey);
        return false;
    }

    @Override
    public boolean finishMsg(String businessType, String businessKey, String userId) {
        log.error("【钉钉自定义机器人群消息】businessType->{},businessKey->{},userId->{},无法完结",businessType,businessKey,userId);
        return false;
    }

    @Override
    public String getMsgType() {
        return MsgTypeConst.ddCustomizeRobotGroupMsg;
    }
}

package com.xm.util.dingding.robot;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.dingding.DingdingProperty;
import com.xm.core.msg.params.ButtonParam;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.robot.params.RobotSendToGroupTarget;
import com.xm.util.dingding.robot.requestParams.*;
import com.xm.util.dingding.robot.requestRes.*;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DingDingRobotUtil {

    //===================================交互卡片=======================================//

    /**
     * 创建和发送互动卡片
     */
    public static DDSendRobotInteractiveCardRes sendRobotInteractiveCard(DDSendRobotInteractiveCardParam createRobotCardParam) {
        String url = "https://api.dingtalk.com/v1.0/im/interactiveCards/send";
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(createRobotCardParam));
        DDSendRobotInteractiveCardRes res = JSONUtil.toBean(resStr, DDSendRobotInteractiveCardRes.class);
        if (res.isSuccess()) {
            return res;
        } else {
            throw new CommonException(StrUtil.format("【机器人发送钉钉互动卡片失败】{}", res));
        }
    }

    /**
     * 更新互动卡片
     */
    public static DDUpdateRobotInteractiveCardRes updateRobotInteractiveCard(DDUpdateRobotInteractiveCardParam updateRobotCardParam) {
        String url = "https://api.dingtalk.com/v1.0/im/interactiveCards";
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.PUT, header, JSONUtil.toJsonStr(updateRobotCardParam));
        DDUpdateRobotInteractiveCardRes res = JSONUtil.toBean(resStr, DDUpdateRobotInteractiveCardRes.class);
        if (res.isSuccess()) {
            return res;
        } else {
            throw new CommonException(StrUtil.format("【更新钉钉机器人互动卡片失败】{}", res));
        }
    }


    /**
     * 注册互动卡片回调接口
     */
    public static DDRegisterRobotInteractiveCardCallBackRes registerRobotInteractiveCardCallBack(
            DDRegisterRobotInteractiveCardCallBackParam registerParam) {
        String url = StrUtil.format("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/interactivecard/callback/register?access_token={}", DingDingUtil.getAccessToken());
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(registerParam));
        DDRegisterRobotInteractiveCardCallBackRes res = JSONUtil.toBean(resStr, DDRegisterRobotInteractiveCardCallBackRes.class);
        if (res.isSuccess() && res.getErrcode() == 0) {
            return res;
        } else {
            throw new CommonException(StrUtil.format("【注册机器人互动卡片回调地址失败】{}", res));
        }
    }

    //===================================发送个人消息=======================================//


    /**
     * 发送机器人普通卡片
     *
     * @param title      列表显示标题
     * @param text       markdown版本为2
     * @param userIdList 发送人
     */
    public static DDSendRobotToOneMsgRes sendRobotToOneSampleActionCardMsg(String title, String text, List<String> userIdList, String jumpUrl) {
        DingdingProperty dingdingProperty = DingDingUtil.getCurrentDingdingProperty();
        DDSendRobotToOneMsgParam ddSendMsgParam = new DDSendRobotToOneMsgParam();
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("title", title);
        jsonObject.set("text", text);
        jsonObject.set("singleTitle", "查看详情");
        jsonObject.set("singleURL", jumpUrl);
        ddSendMsgParam.setMsgParam(jsonObject.toString());
        ddSendMsgParam.setMsgKey("sampleActionCard");
        ddSendMsgParam.setUserIds(userIdList);
        ddSendMsgParam.setRobotCode(dingdingProperty.getRobotCode());
        return sendRobotToOneMsg(ddSendMsgParam);
    }


    /**
     * 发送机器人普通卡片
     */
    public static DDSendRobotToOneMsgRes sendRobotToOneMsg(DDSendRobotToOneMsgParam ddSendMsgParam) {
        String url = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(ddSendMsgParam));
        DDSendRobotToOneMsgRes bean = JSONUtil.toBean(resStr, DDSendRobotToOneMsgRes.class);
        bean.setResult(!StrUtil.isBlank(bean.getProcessQueryKey()));
        return bean;
    }

    /**
     * 撤回机器人消息
     *
     * @param processQueryKeys 发送机器人消息返回结果中获取
     */
    public static DDRecallRobotToOneMsgRes recallRobotToOneMsg(List<String> processQueryKeys) {
        if (CollectionUtil.isEmpty(processQueryKeys)) {
            throw new CommonException("撤回机器人消息失败，参数为空");
        }
        DDRecallRobotToOneMsgParam ddRecallMsgParam = new DDRecallRobotToOneMsgParam();
        ddRecallMsgParam.setProcessQueryKeys(processQueryKeys);
        ddRecallMsgParam.setRobotCode(DingDingUtil.getCurrentDingdingProperty().getRobotCode());
        return recallRobotToOneMsg(ddRecallMsgParam);
    }

    /**
     * 撤回机器人消息
     */
    public static DDRecallRobotToOneMsgRes recallRobotToOneMsg(DDRecallRobotToOneMsgParam ddRecallMsgParam) {
        String url = "https://api.dingtalk.com/v1.0/robot/otoMessages/batchRecall";
        Map<String, String> header = new HashMap<>();
        header.put("x-acs-dingtalk-access-token", DingDingUtil.getAccessToken());
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(ddRecallMsgParam));
        return JSONUtil.toBean(resStr, DDRecallRobotToOneMsgRes.class);
    }


    //===================================发送群消息=======================================//

    /**
     *
     * @param targetList at目标, 当有跳转url时无效
     * @param jumpUrl  跳转url
     */
    public static List<DDSendRobotToGroupMsgRes> sendCustomizeRobotToGroupSampleActionCardMsg(String title, String text,
                                                                                              List<RobotSendToGroupTarget> targetList,
                                                                                              String jumpUrl){
        if (CollectionUtil.isNotEmpty(targetList)){
            List<DDSendRobotToGroupMsgRes> resList=new ArrayList<>();
            for (RobotSendToGroupTarget target : targetList) {
                DDSendRobotToGroupMsgParam ddSendRobotToGroupMsgParam=new DDSendRobotToGroupMsgParam();
                DDSendRobotToGroupMsgParam.AtDTO atDTO = new DDSendRobotToGroupMsgParam.AtDTO();
                if (target.getIsAtAll()!=null){
                    if ("1".equals(target.getIsAtAll())){
                        atDTO.setIsAtAll(true);
                        //注意：每个markdown格式操作之后都需要\n换行
                        text= "@所有人" +"\n<br>\n"+text;
                    }else if ("0".equals(target.getIsAtAll())){
                        atDTO.setIsAtAll(false);
                        List<String> userIdList = target.getUserIdList();
                        StringBuilder atUser= new StringBuilder();
                        if (CollectionUtil.isNotEmpty(userIdList)){
                            for (String userId : userIdList){
                                atUser.append("@").append(userId).append(" ");
                            }
                            //注意：每个markdown格式操作之后都需要\n换行
                            text= atUser +"\n<br>\n"+text;
                        }
                    }
                }
                List<ButtonParam> buttonParamList = target.getButtonParamList();
                //跳转链接为空或者按钮列表为空则发送普通markdown消息
                if (StrUtil.isNotBlank(jumpUrl)||CollectionUtil.isNotEmpty(buttonParamList)){
                    ddSendRobotToGroupMsgParam.setMsgtype("actionCard");
                    DDSendRobotToGroupMsgParam.ActionCardDTO actionCardDTO = new DDSendRobotToGroupMsgParam.ActionCardDTO();
                    actionCardDTO.setText(text);
                    actionCardDTO.setTitle(title);
                    if (CollectionUtil.isEmpty(buttonParamList)){
                        actionCardDTO.setSingleTitle("查看详情");
                        actionCardDTO.setSingleURL(jumpUrl);
                    }else {
                        //按钮竖直排序
                        actionCardDTO.setBtnOrientation("0");
                        List<DDSendRobotToGroupMsgParam.ActionCardDTO.BtnsDTO> btnsDTOList = new ArrayList<>();
                        for (ButtonParam buttonParam : buttonParamList){
                            DDSendRobotToGroupMsgParam.ActionCardDTO.BtnsDTO btnsDTO = new DDSendRobotToGroupMsgParam.ActionCardDTO.BtnsDTO();
                            btnsDTO.setTitle(buttonParam.getTitle());
                            btnsDTO.setActionURL(buttonParam.getActionUrL());
                            btnsDTOList.add(btnsDTO);
                        }
                        actionCardDTO.setBtns(btnsDTOList);
                    }
                    ddSendRobotToGroupMsgParam.setActionCard(actionCardDTO);
                }else {
                    ddSendRobotToGroupMsgParam.setMsgtype("markdown");
                    DDSendRobotToGroupMsgParam.MarkdownDTO markdownDTO = new DDSendRobotToGroupMsgParam.MarkdownDTO();
                    markdownDTO.setTitle(title);
                    ddSendRobotToGroupMsgParam.setMarkdown(markdownDTO);
                    markdownDTO.setText(text);
                }
                atDTO.setAtUserIds(target.getUserIdList());
                ddSendRobotToGroupMsgParam.setAt(atDTO);

                DDSendRobotToGroupMsgRes ddSendRobotToGroupMsgRes =
                        DingDingRobotUtil.sendCustomizeRobotToGroupMsg(target.getGroupAccessToken(), null, null, ddSendRobotToGroupMsgParam);
                resList.add(ddSendRobotToGroupMsgRes);
            }
            return resList;
        }else {
            return new ArrayList<>();
        }
    }


    /**
     * @param groupAccessToken  群机器人webhook地址中的access_token值
     * @param timestamp 自定义机器人必须
     * @param sign  自定义机器人必须
     */
    public static DDSendRobotToGroupMsgRes sendCustomizeRobotToGroupMsg(String groupAccessToken,String timestamp,String sign,
                                                               DDSendRobotToGroupMsgParam ddSendRobotToGroupMsgParam) {
        if (StrUtil.isBlank(groupAccessToken)){
            throw new CommonException("群机器人webhook地址中的access_token值不能为空");
        }
        String url = "https://oapi.dingtalk.com/robot/send";
        Map<String,String> paramsMap=new HashMap<>();
        paramsMap.put("access_token", groupAccessToken);
        if (StrUtil.isNotBlank(timestamp)){
            paramsMap.put("timestamp", timestamp);
        }
        if (StrUtil.isNotBlank(sign)){
            paramsMap.put("sign", sign);
        }
        url = HttpUtils.buildUrl(url, paramsMap);
        Map<String, String> header = new HashMap<>();
        String resStr = HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(ddSendRobotToGroupMsgParam));
        return JSONUtil.toBean(resStr, DDSendRobotToGroupMsgRes.class);
    }
}

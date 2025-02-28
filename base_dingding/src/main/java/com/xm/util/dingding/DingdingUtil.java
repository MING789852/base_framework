package com.xm.util.dingding;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.xm.advice.exception.exception.CommonException;
import com.xm.configuration.baseUrl.BaseUrlProperty;
import com.xm.configuration.dingding.DingdingConfigContextHolder;
import com.xm.configuration.dingding.DingdingProperty;
import com.xm.configuration.dingding.KeyDingdingProperty;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.JumpUrlParamGenerate;
import com.xm.msg.params.DDJumpUrlParamGenerate;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.cache.CacheUtil;
import com.xm.util.dingding.params.DDApprovedCard;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.util.dingding.requestParams.*;
import com.xm.util.dingding.requestRes.*;
import com.xm.util.http.HttpUtils;
import com.xm.util.http.enums.HttpMethodEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DingdingUtil {

    private final static BaseUrlProperty baseUrlProperty;

    private final static JumpUrlParamGenerate jumpUrlParamGenerate;

    private final static String redisKey="DD_ACCESSTOKEN";

    static {
        baseUrlProperty = SpringBeanUtil.getBeanByClass(BaseUrlProperty.class);
        jumpUrlParamGenerate = SpringBeanUtil.getBeanByClass(DDJumpUrlParamGenerate.class);
    }

    private static KeyDingdingProperty  getCurrentKeyAndDingdingProperty(){
        return DingdingConfigContextHolder.getCurrentKeyAndDingdingProperty();
    }

    private static DingdingProperty getCurrentDingdingProperty(){
        KeyDingdingProperty currentKeyAndDingdingProperty = getCurrentKeyAndDingdingProperty();
        return currentKeyAndDingdingProperty.getProperty();
    }

    private static String getRedisKey(){
        KeyDingdingProperty keyDingdingProperty = getCurrentKeyAndDingdingProperty();
        return redisKey+"_"+keyDingdingProperty.getKey();
    }

    /**
     * 存储redis，一小时刷新一次
     */
    public static String getAccessToken()  {
        String accessToken;
        accessToken= (String) CacheUtil.get(getRedisKey());
        if (accessToken!=null){
            return accessToken;
        }else {
            JSONObject dataMap=new JSONObject();
            DingdingProperty dingdingProperty = getCurrentDingdingProperty();
            dataMap.set("appKey",dingdingProperty.getAppKey());
            dataMap.set("appSecret",dingdingProperty.getAppSecret());
            String resStr= HttpUtils.doJsonRequestReturnString("https://api.dingtalk.com/v1.0/oauth2/accessToken", HttpMethodEnum.POST, null, dataMap.toString());
            JSONObject jsonObject=JSONUtil.parseObj(resStr);
            accessToken=jsonObject.getStr("accessToken");
            //缓存一小时
            CacheUtil.set(getRedisKey(),accessToken,1, TimeUnit.HOURS);
            return accessToken;
        }
    }

    /**
     * 获取用户详情
     */
    public static DDUserDetail getUserDetail(String accessToken, String userId){
        String url = "https://oapi.dingtalk.com/topapi/v2/user/get?access_token="+accessToken;
        JSONObject dataMap=new JSONObject();
        dataMap.set("userid",userId);
        Map<String,String> headers=new HashMap<>();
        headers.put("Content-Type","application/json");
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, headers, dataMap.toString());
        JSONObject res=JSONUtil.parseObj(resStr);
        Integer errcode=res.getInt("errcode");
        if (errcode==0){
            JSONObject result=res.getJSONObject("result");
            return result.toBean(DDUserDetail.class);
        }else {
            String errmsg=res.getStr("errmsg");
            log.error(StrUtil.format("【请求钉钉获取用户详情失败】 {}",errmsg));
            return null;
        }
    }


    /**
     * 发送工作通知
     */
    public static DDSendMsgRes sendMsg(DDSendMsgParam param){
        DingdingProperty dingdingProperty = getCurrentDingdingProperty();
        param.setAgent_id(dingdingProperty.getAgentId());
        String url = StrUtil.format("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token={}",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(param));
        DDSendMsgRes res=JSONUtil.toBean(resStr, DDSendMsgRes.class);
        int errcode=res.getErrcode();
        if (errcode==0){
            return res;
        }else {
            throw new CommonException(StrUtil.format("【发送钉钉消息失败】{}",res));
        }
    }

    /**
     * 发送机器人互动审批卡片
     * @param ddApprovedCard
     * @return
     */
    public static DDApprovedCard sendApprovalCard(DDApprovedCard ddApprovedCard){
        DDCreateRobotCardParam robotCardSendParam=new DDCreateRobotCardParam();
        DDCreateRobotCardParam.CardDataDTO cardDataDTO=new DDCreateRobotCardParam.CardDataDTO();
        cardDataDTO.setCardParamMap(ddApprovedCard.toLegalParameters());
        robotCardSendParam.setCardData(cardDataDTO);
        robotCardSendParam.setCardTemplateId("77b8d00e-168b-4c11-8efb-b388a0e3c485.schema");

        robotCardSendParam.setReceiverUserIdList(ddApprovedCard.getReceiverUserIdList());
        robotCardSendParam.setOutTrackId(ddApprovedCard.getOutTrackId());
        robotCardSendParam.setUserIdType(1);
        robotCardSendParam.setConversationType(0);
        robotCardSendParam.setCallbackRouteKey(ddApprovedCard.getCallbackRouteKey());

        DingdingUtil.robotCardCreateAndSend(robotCardSendParam);
        return ddApprovedCard;
    }

    /**
     * 更新机器人互动审批卡片
     */
    public static DDApprovedCard updateApprovedCard(DDApprovedCard ddApprovedCard){
        DDUpdateRobotCardParam updateRobotCardParam=new DDUpdateRobotCardParam();
        DDUpdateRobotCardParam.CardDataDTO cardDataDTO=new DDUpdateRobotCardParam.CardDataDTO();
        cardDataDTO.setCardParamMap(ddApprovedCard.toLegalParameters());
        updateRobotCardParam.setCardData(cardDataDTO);
        updateRobotCardParam.setOutTrackId(ddApprovedCard.getOutTrackId());
        updateRobotCardParam.setUserIdType(1);
        DDUpdateRobotCardParam.CardOptionsDTO cardOptionsDTO = new DDUpdateRobotCardParam.CardOptionsDTO();
        cardOptionsDTO.setUpdateCardDataByKey(true);
        updateRobotCardParam.setCardOptions(cardOptionsDTO);

        DingdingUtil.robotCardUpdate(updateRobotCardParam);
        return ddApprovedCard;
    }

    /**
     * 发送机器人普通卡片
     * @param title 列表显示标题
     * @param text markdown版本为2
     * @param userIdList 发送人
     */
    public static DDRobotSendMsgRes robotSendMsg(String title,String text, List<String> userIdList,String jumpUrl){
        DingdingProperty dingdingProperty = getCurrentDingdingProperty();
        DDRobotSendMsgParam ddSendMsgParam=new DDRobotSendMsgParam();
        JSONObject jsonObject=new JSONObject();
        jsonObject.set("title",title);
        jsonObject.set("text",text);
        jsonObject.set("singleTitle","查看详情");
        jsonObject.set("singleURL",jumpUrl);
        ddSendMsgParam.setMsgParam(jsonObject.toString());
        ddSendMsgParam.setMsgKey("sampleActionCard");
        ddSendMsgParam.setUserIds(userIdList);
        ddSendMsgParam.setRobotCode(dingdingProperty.getRobotCode());
        return robotSendMsg(ddSendMsgParam);
    }

    /**
     * 撤回机器人消息
     * @param processQueryKeys 发送机器人消息返回结果中获取
     */
    public static DDRobotRecallMsgRes robotRecallMsg(List<String> processQueryKeys){
        if (CollectionUtil.isEmpty(processQueryKeys)){
            throw new CommonException("撤回机器人消息失败，参数为空");
        }
        DDRobotRecallMsgParam ddRecallMsgParam=new DDRobotRecallMsgParam();
        ddRecallMsgParam.setProcessQueryKeys(processQueryKeys);
        ddRecallMsgParam.setRobotCode(getCurrentDingdingProperty().getRobotCode());
        return robotRecallMsg(ddRecallMsgParam);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * 发送机器人普通卡片
     */
    public static DDRobotSendMsgRes robotSendMsg(DDRobotSendMsgParam ddSendMsgParam){
        String url = "https://api.dingtalk.com/v1.0/robot/oToMessages/batchSend";
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(ddSendMsgParam));
        return JSONUtil.toBean(resStr, DDRobotSendMsgRes.class);
    }

    /**
     * 撤回机器人消息
     */
    public static DDRobotRecallMsgRes robotRecallMsg(DDRobotRecallMsgParam ddRecallMsgParam){
        String url = "https://api.dingtalk.com/v1.0/robot/otoMessages/batchRecall";
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(ddRecallMsgParam));
        return JSONUtil.toBean(resStr, DDRobotRecallMsgRes.class);
    }


    /**
     * 注册互动卡片回调接口
     * @param registerParam
     * @return
     */
    public static DDRobotCardCallbackRegisterRes robotCardRegisterCallBackUrl(DDRobotCardCallbackRegisterParam registerParam){
        String url = StrUtil.format("https://oapi.dingtalk.com/topapi/im/chat/scencegroup/interactivecard/callback/register?access_token={}",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(registerParam));
        DDRobotCardCallbackRegisterRes res=JSONUtil.toBean(resStr, DDRobotCardCallbackRegisterRes.class);
        if (res.isSuccess()&&res.getErrcode()==0){
            return res;
        }else {
            throw new CommonException(StrUtil.format("【注册机器人互动卡片回调地址失败】{}",res));
        }
    }

    /**
     * 更新互动卡片
     * @param updateRobotCardParam
     * @return
     */
    public static DDUpdateRobotCardRes robotCardUpdate(DDUpdateRobotCardParam updateRobotCardParam){
        String url = "https://api.dingtalk.com/v1.0/im/interactiveCards";
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url,HttpMethodEnum.PUT,header, JSONUtil.toJsonStr(updateRobotCardParam));
        DDUpdateRobotCardRes res=JSONUtil.toBean(resStr, DDUpdateRobotCardRes.class);
        if (res.isSuccess()){
            return res;
        }else {
            throw new CommonException(StrUtil.format("【更新钉钉机器人互动卡片失败】{}",res));
        }
    }


    /**
     * 创建和发送互动卡片
     * @param createRobotCardParam
     * @return
     */
    public static DDCreateRobotCardRes robotCardCreateAndSend(DDCreateRobotCardParam createRobotCardParam){
        String url = "https://api.dingtalk.com/v1.0/im/interactiveCards/send";
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(createRobotCardParam));
        DDCreateRobotCardRes res=JSONUtil.toBean(resStr, DDCreateRobotCardRes.class);
        if (res.isSuccess()){
            return res;
        }else {
            throw new CommonException(StrUtil.format("【机器人发送钉钉互动卡片失败】{}",res));
        }
    }

    /**
     * 待办更新
     */
    public static DDTaskUpdateRes taskUpdate(DDTaskUpdateParam param, String taskId){
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/MhC5jXZMkKwoVvxt9srSkwiEiE/tasks/{}",taskId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url,HttpMethodEnum.PUT,header, JSONUtil.toJsonStr(param));
        return JSONUtil.toBean(resStr, DDTaskUpdateRes.class);
    }

    /**
     * 待办删除
     */
    public static DDTaskDeleteRes taskDelete(String taskId){
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/MhC5jXZMkKwoVvxt9srSkwiEiE/tasks/{}",taskId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url,HttpMethodEnum.DELETE,header, null);
        return JSONUtil.toBean(resStr, DDTaskDeleteRes.class);
    }

    /**
     * 待办创建
     */
    public static DDTaskCreateRes taskCreate(DDTaskCreateParam param){
        //使用创建者
        String creatorId = param.getCreatorId();
        String url = StrUtil.format("https://api.dingtalk.com/v1.0/todo/users/{}/tasks",creatorId);
        Map<String,String> header=new HashMap<>();
        header.put("x-acs-dingtalk-access-token",getAccessToken());
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, header, JSONUtil.toJsonStr(param));
        return JSONUtil.toBean(resStr, DDTaskCreateRes.class);
    }

    /**
     * 免登码获取用户信息
     */
    public static DDUserInfo getUserInfo(String accessToken, String authCode){
        String url = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo?access_token="+accessToken;
        JSONObject dataMap=new JSONObject();
        dataMap.set("code",authCode);
        String resStr=HttpUtils.doJsonRequestReturnString(url, HttpMethodEnum.POST, null, JSONUtil.toJsonStr(dataMap));
        JSONObject res=JSONUtil.parseObj(resStr);
        Integer errcode=res.getInt("errcode");
        if (errcode==0){
            JSONObject result=res.getJSONObject("result");
            return result.toBean(DDUserInfo.class);
        }else {
            String errmsg=res.getStr("errmsg");
            throw new CommonException(StrUtil.format("【请求钉钉获取用户的userid失败】 {}",errmsg));
        }
    }




    /**
     * 获取授权跳转url
     */
    public static String getAuthRedirectUrl(String path){
        KeyDingdingProperty currentKeyAndDingdingProperty = getCurrentKeyAndDingdingProperty();
        DingdingProperty dingdingProperty = currentKeyAndDingdingProperty.getProperty();
        return dingdingProperty.getAuthUrl() + "&redirectUrl=" + URLUtil.encodeAll(path)
                + "&configKey="+currentKeyAndDingdingProperty.getKey()+ "&actionType="+ MsgActionTypeEnum.redirectUrl.name();
    }

    /**
     * 获取钉钉回调url
     */
    public static String getCallBackUrl(String path){
        return baseUrlProperty.getCallBackUrl()+path;
    }

    /**
     * 获取授权跳转url
     */
    public static String getAuthOpenDialog(String json){
        KeyDingdingProperty currentKeyAndDingdingProperty = getCurrentKeyAndDingdingProperty();
        DingdingProperty dingdingProperty = currentKeyAndDingdingProperty.getProperty();
        return dingdingProperty.getAuthUrl() + "&openDialog=" + URLUtil.encodeAll(json)
                + "&configKey="+currentKeyAndDingdingProperty.getKey()+ "&actionType="+ MsgActionTypeEnum.openDialog.name();
    }

    /**
     * 获取跳转到工作台的url
     */
    public static String getJumpDDWorkbenchUrl(JumpUrlParam jumpUrlParam) {
        String url=jumpUrlParamGenerate.generate(jumpUrlParam);
        DingdingProperty dingdingProperty = getCurrentDingdingProperty();
        return StrUtil.format("dingtalk://dingtalkclient/action/openapp?corpid={}&container_type=work_platform&app_id={}&redirect_type=jump&redirect_url={}"
                ,dingdingProperty.getCorpId(),dingdingProperty.getAgentId(), URLUtil.encodeAll(url));
    }

    /**
     * 获取跳转到侧边栏url
     */
    public static String getJumpDDSidebarUrl(JumpUrlParam jumpUrlParam){
        String url=jumpUrlParamGenerate.generate(jumpUrlParam);
        return StrUtil.format("dingtalk://dingtalkclient/page/link?url={}&pc_slide=true"
                , URLUtil.encodeAll(url));
    }
}

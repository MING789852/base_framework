package com.xm.util.dingding.jumpUrl.params;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.configuration.baseUrl.BaseUrlProperty;
import com.xm.configuration.dingding.DingdingConfig;
import com.xm.configuration.dingding.KeyDingdingProperty;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.JumpUrlParamGenerate;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.http.HttpUtils;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class DDJumpUrlParamGenerate implements JumpUrlParamGenerate {

    private final DingdingConfig dingdingConfig;

    private final BaseUrlProperty baseUrlProperty;

    /**
     * 获取授权跳转url
     */
    public  String getAuthRedirectUrl(String path,boolean useConfigUrl,boolean useConfigOutUrl,String customUrl){
        KeyDingdingProperty currentKeyAndDingdingProperty = DingDingUtil.getCurrentKeyAndDingdingProperty();
        String authUrl;
        if (useConfigUrl){
            if (useConfigOutUrl){
                authUrl = baseUrlProperty.getFgOutUrl()+dingdingConfig.getAuthPath();
            }else {
                authUrl = baseUrlProperty.getFgUrl()+dingdingConfig.getAuthPath();
            }
        }else {
            if (StrUtil.isBlank(customUrl)){
                throw new RuntimeException("自定义url不能为空");
            }
            authUrl = customUrl+dingdingConfig.getAuthPath();
        }
        return authUrl + "&redirectUrl=" + URLUtil.encodeAll(path)
                + "&configKey="+currentKeyAndDingdingProperty.getKey()+ "&actionType="+ MsgActionTypeEnum.redirectUrl.name();
    }

    /**
     * 获取授权跳转url
     */
    public  String getAuthOpenDialog(String json,boolean useConfigUrl,boolean useConfigOutUrl,String customUrl){
        KeyDingdingProperty currentKeyAndDingdingProperty = DingDingUtil.getCurrentKeyAndDingdingProperty();
        String authUrl;
        if (useConfigUrl){
            if (useConfigOutUrl){
                authUrl = baseUrlProperty.getFgOutUrl()+dingdingConfig.getAuthPath();
            }else {
                authUrl = baseUrlProperty.getFgUrl()+dingdingConfig.getAuthPath();
            }
        }else {
            if (StrUtil.isBlank(customUrl)){
                throw new RuntimeException("自定义url不能为空");
            }
            authUrl = customUrl+dingdingConfig.getAuthPath();
        }

        return authUrl + "&openDialog=" + URLUtil.encodeAll(json)
                + "&configKey="+currentKeyAndDingdingProperty.getKey()+ "&actionType="+ MsgActionTypeEnum.openDialog.name();
    }

    @Override
    public String generate(JumpUrlParam jumpUrlParam) {
        JSONObject props = jumpUrlParam.getProps();
        MsgActionTypeEnum msgActionTypeEnum = jumpUrlParam.getMsgActionTypeEnum();
        String urlPath = jumpUrlParam.getUrlPath();
        String componentPath = jumpUrlParam.getComponentPath();
        if (props==null){
            props=new JSONObject();
        }
        String customUrl = jumpUrlParam.getCustomUrl();
        boolean useConfigUrl = jumpUrlParam.isUseConfigUrl();
        boolean useConfigOutUrl = jumpUrlParam.isUseConfigOutUrl();
        if (msgActionTypeEnum== MsgActionTypeEnum.redirectUrl){
            Map<String,String> queryMap=new HashMap<>();
            props.forEach((key, value) -> queryMap.put(key, value.toString()));
            return getAuthRedirectUrl(HttpUtils.buildUrl(urlPath, queryMap),useConfigUrl,useConfigOutUrl,customUrl);
        }else if (msgActionTypeEnum== MsgActionTypeEnum.openDialog){
            DialogComponent dialogComponent=new DialogComponent();
            dialogComponent.setProps(props);
            dialogComponent.setPath(componentPath);
            return getAuthOpenDialog(JSONUtil.toJsonStr(dialogComponent),useConfigUrl,useConfigOutUrl,customUrl);
        }else if (msgActionTypeEnum== MsgActionTypeEnum.outDirectLink){
            return jumpUrlParam.getOutDirectLink();
        }else {
            return "";
        }
    }
}

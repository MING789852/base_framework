package com.xm.msg.params;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.JumpUrlParamGenerate;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.util.dingding.DingdingUtil;
import com.xm.util.dingding.params.DialogComponent;
import com.xm.util.http.HttpUtils;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class DDJumpUrlParamGenerate implements JumpUrlParamGenerate {
    @Override
    public String generate(JumpUrlParam jumpUrlParam) {
        JSONObject props = jumpUrlParam.getProps();
        MsgActionTypeEnum msgActionTypeEnum = jumpUrlParam.getMsgActionTypeEnum();
        String urlPath = jumpUrlParam.getUrlPath();
        String componentPath = jumpUrlParam.getComponentPath();
        if (props==null){
            props=new JSONObject();
        }
        if (msgActionTypeEnum== MsgActionTypeEnum.redirectUrl){
            Map<String,String> queryMap=new HashMap<>();
            props.forEach((key, value) -> queryMap.put(key, value.toString()));
            return DingdingUtil.getAuthRedirectUrl(HttpUtils.buildUrl(urlPath, queryMap));
        }else {
            DialogComponent dialogComponent=new DialogComponent();
            dialogComponent.setProps(props);
            dialogComponent.setPath(componentPath);
            return DingdingUtil.getAuthOpenDialog(JSONUtil.toJsonStr(dialogComponent));
        }
    }
}

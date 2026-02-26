package com.xm.core.msg.params;

import cn.hutool.json.JSONObject;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.JumpUrlParamGenerate;
import com.xm.util.bean.SpringBeanUtil;
import lombok.Data;

@Data
public class JumpUrlParam {
    //authActionTypeEnum=openDialog生效
    //例子：src/components/business/action/viewData.vue
    //组件参数由props提供
    protected String componentPath;
    //authActionTypeEnum=redirectUrl生效
    //例子：/mini/identity?type=viewOne&bid=xxxxx
    //?后参数由props拼接
    protected String urlPath;
    protected JSONObject props;
    protected MsgActionTypeEnum msgActionTypeEnum;

    //是否使用baseUrl配置,默认为true，如果为false则需要添加customUrl
    private boolean useConfigUrl=true;
    //是否使用外网链接(useConfigUrl为true时才生效)
    private boolean useConfigOutUrl=false;

    //authActionTypeEnum=outLink生效
    //链接直接跳转
    //例如直接跳转百度：https://www.baidu.com/
    private String outDirectLink;
    //http(s)://(ip或域名):(端口)
    private String customUrl;


    public String generate(Class<? extends JumpUrlParamGenerate> jumpUrlParamGenerateClass){
        JumpUrlParamGenerate jumpUrlParamGenerate = SpringBeanUtil.getBeanByClass(jumpUrlParamGenerateClass);
        return jumpUrlParamGenerate.generate(this);
    }
}

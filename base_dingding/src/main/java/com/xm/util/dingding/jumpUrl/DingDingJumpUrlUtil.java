package com.xm.util.dingding.jumpUrl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.xm.configuration.baseUrl.BaseUrlProperty;
import com.xm.configuration.dingding.DingdingConfig;
import com.xm.configuration.dingding.DingdingProperty;
import com.xm.configuration.dingding.KeyDingdingProperty;
import com.xm.core.enums.MsgActionTypeEnum;
import com.xm.core.msg.JumpUrlParamGenerate;
import com.xm.core.msg.params.JumpUrlParam;
import com.xm.util.bean.SpringBeanUtil;
import com.xm.util.dingding.DingDingUtil;
import com.xm.util.dingding.jumpUrl.params.DDJumpUrlParamGenerate;

public class DingDingJumpUrlUtil {

    private final static BaseUrlProperty baseUrlProperty;


    static {
        baseUrlProperty = SpringBeanUtil.getBeanByClass(BaseUrlProperty.class);
    }

    /**
     * 获取钉钉回调url
     */
    public static String getCallBackUrl(String path){
        return baseUrlProperty.getCallBackUrl()+path;
    }

    /**
     * 获取跳转到工作台的url
     */
    public static String getJumpDDWorkbenchUrl(String url) {
        DingdingProperty dingdingProperty = DingDingUtil.getCurrentDingdingProperty();
        return StrUtil.format("dingtalk://dingtalkclient/action/openapp?corpid={}&container_type=work_platform&app_id={}&redirect_type=jump&redirect_url={}"
                ,dingdingProperty.getCorpId(),dingdingProperty.getAgentId(), URLUtil.encodeAll(url));
    }

    /**
     * 获取跳转到侧边栏url
     */
    public static String getJumpDDSidebarUrl(String url){
        return StrUtil.format("dingtalk://dingtalkclient/page/link?url={}&pc_slide=true"
                , URLUtil.encodeAll(url));
    }
}

package com.xm.core.msg.params;

import cn.hutool.json.JSONObject;
import com.xm.core.enums.MsgActionTypeEnum;
import lombok.Data;

@Data
public class JumpUrlParam {
    //authActionTypeEnum=openDialog生效
    //例子：src/components/business/action/viewData.vue
    protected String componentPath;
    //authActionTypeEnum=redirectUrl生效
    //例子：/mini/identity?type=viewOne&bid=xxxxx
    //?后参数由props拼接
    protected String urlPath;
    protected JSONObject props;
    protected MsgActionTypeEnum msgActionTypeEnum;
}

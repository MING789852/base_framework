package com.xm.util.dingding.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDRobotCardCallbackRegisterParam {
    private String callback_url;
    private String api_secret;
    private String callbackRouteKey;
    private boolean forceUpdate;
}

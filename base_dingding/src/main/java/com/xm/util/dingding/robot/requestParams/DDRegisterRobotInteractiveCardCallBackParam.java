package com.xm.util.dingding.robot.requestParams;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDRegisterRobotInteractiveCardCallBackParam {
    private String callback_url;
    private String api_secret;
    private String callbackRouteKey;
    private boolean forceUpdate;
}

package com.xm.util.dingding.params;

import cn.hutool.json.JSONObject;
import lombok.Data;

@Data
public class DialogComponent {
    private String path;
    private JSONObject props;
}

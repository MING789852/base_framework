package com.xm.util.dingding.params;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class DDCardCallBackParam {

    private String outTrackId;
    private String extension;
    private String corpId;
    private String value;
    private String userId;
    private String content;

    public JSONObject getActualData(){
        JSONObject jsonObject= JSONUtil.parseObj(this.value);
        return jsonObject.getJSONObject("cardPrivateData").getJSONObject("params");
    }
}

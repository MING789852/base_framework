package com.xm.util.miniprogram.res;

import com.xm.util.miniprogram.res.property.LineColor;
import lombok.Data;

@Data
public class MiniUnLimitQrCode {
    //最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）
    private String scene;
    //	默认是主页，页面 page，例如 pages/index/index，根路径前不要填加 /，不能携带参数（参数请放在scene字段里），如果不填写这个字段，默认跳主页面。scancode_time为系统保留参数，不允许配置
    private String page;
    //默认是{"r":0,"g":0,"b":0}
    private LineColor line_color;
    //要打开的小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"。默认是正式版。
    private String env_version;
}

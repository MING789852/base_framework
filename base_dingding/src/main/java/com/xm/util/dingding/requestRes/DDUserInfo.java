package com.xm.util.dingding.requestRes;

import lombok.Data;

@Data
public class DDUserInfo {
    //用户的userId
    private String userid;
    //设备ID
    private String device_id;
    //是否为管理员
    private Boolean sys;
    /**
     * 1：主管理员
     *
     * 2：子管理员
     *
     * 100：老板
     *
     * 0：其他（如普通员工）
     */
    private Integer sys_level;
    //用户关联的unionId
    private String associated_unionid;
    //用户unionId
    private String unionid;
    //用户名字
    private String name;
}

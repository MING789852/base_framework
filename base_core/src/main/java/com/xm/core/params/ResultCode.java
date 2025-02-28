package com.xm.core.params;

public enum ResultCode {
    SUCCESS(200,"操作成功"),
    ERROR(400,"操作失败"),
    ERRORDATA(460,"携带data的错误");

    Integer code;
    String des;

    private ResultCode(Integer code,String des){
        this.code=code;
        this.des=des;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }
}


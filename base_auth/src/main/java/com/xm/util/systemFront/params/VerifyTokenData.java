package com.xm.util.systemFront.params;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class VerifyTokenData implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userName;
    private Date loginTime;
}

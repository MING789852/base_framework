package com.xm.auth.domain.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserTypeResult implements Serializable {
    private Integer value;
    private String label;
}

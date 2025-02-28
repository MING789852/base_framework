package com.xm.util.valid;

import lombok.Data;

@Data
public class ValidationResult {
    private Boolean isLegal;

    private String msg;
}

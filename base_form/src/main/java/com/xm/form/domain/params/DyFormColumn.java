package com.xm.form.domain.params;

import lombok.Data;

@Data
public class DyFormColumn {
    private String prop;
    private String label;
    private String type;
    private Boolean required;
    private String placeholder;
}

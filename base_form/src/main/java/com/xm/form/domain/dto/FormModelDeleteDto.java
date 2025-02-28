package com.xm.form.domain.dto;

import lombok.Data;

@Data
public class FormModelDeleteDto {
    private String formInsId;

    public FormModelDeleteDto(String formInsId) {
        this.formInsId = formInsId;
    }
}

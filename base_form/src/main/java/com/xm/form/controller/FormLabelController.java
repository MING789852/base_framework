package com.xm.form.controller;

import com.xm.core.params.Result;
import com.xm.form.service.TcFormLabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("formLabel")
public class FormLabelController {
    private final TcFormLabelService formLabelService;

    @GetMapping("getAllLabelNameMapping")
    public Result<Map<String, String>> getAllLabelNameMapping(){
        return Result.successForData(formLabelService.getAllLabelNameMapping());
    }
}

package com.xm.module.fileTemplate.controller;

import com.xm.core.params.Result;
import com.xm.module.fileTemplate.domain.dto.FileTemplateSaveDto;
import com.xm.module.fileTemplate.domain.vo.FileTemplateDataVo;
import com.xm.module.fileTemplate.service.FileTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("fileTemplate")
@RequiredArgsConstructor
public class FileTemplateController {
    private final FileTemplateService fileTemplateService;

    @PostMapping("saveOrUpdateData")
    public Result<String> saveOrUpdateData(@RequestBody FileTemplateSaveDto saveDto) {
        return Result.successForData(fileTemplateService.saveOrUpdateData(saveDto));
    }

    @GetMapping("getDataByCode")
    public Result<List<FileTemplateDataVo>> getDataByCode(String code) {
        return Result.successForData(fileTemplateService.getDataByCode(code));
    }
}

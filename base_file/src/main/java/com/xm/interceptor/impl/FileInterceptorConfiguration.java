package com.xm.interceptor.impl;

import com.xm.interceptor.FileReadInterceptor;
import com.xm.util.file.FileApplyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FileInterceptorConfiguration{

    @Bean
    @ConditionalOnMissingBean(FileReadInterceptor.class)
    public FileReadInterceptor fileReadInterceptor(){
        return new FileReadInterceptor() {
            @Override
            public boolean disableCheckAuthCertificate(List<String> fileIdList) {
                //未开启文件授权则不检查文件授权状态
                return FileApplyUtil.disableCheckAuthCertificate(fileIdList);
            }

            @Override
            public boolean allowRead(List<String> fileIdList) {
                return FileApplyUtil.allowRead(fileIdList);
            }
        };
    }
}

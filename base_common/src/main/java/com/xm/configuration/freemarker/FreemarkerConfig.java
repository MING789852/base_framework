package com.xm.configuration.freemarker;


import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FreemarkerConfig {

    @Bean("classPathResourceFreemarkerConfiguration")
    @Primary
    public Configuration  freemarkerConfiguration() throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
        // Specify the source where the template files come from. Here I set a
        // plain directory for it, but non-file-system sources are possible too:
        //打包成jar不能使用File读取jar里面的资源，需要使用流
//        Resource resource = new ClassPathResource("template");
//        cfg.setDirectoryForTemplateLoading(resource.getFile());
        cfg.setTemplateLoader(new ClassTemplateLoader(
                this.getClass().getClassLoader(),  "/template"));

        // Set the preferred charset template files are stored in. UTF-8 is
        // a good choice in most applications:
        cfg.setDefaultEncoding("UTF-8");

        // Sets how errors will appear.
        // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        return cfg;
    }
}

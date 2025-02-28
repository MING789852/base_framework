package com.xm.configuration.pdf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "pdf.html-to-pdf")
@Data
@Configuration
public class HtmlToPdfEnv {
    private String wkHtmlToPdfBinPath;
    private String htmlTempPath;
    private String pdfPath;
}

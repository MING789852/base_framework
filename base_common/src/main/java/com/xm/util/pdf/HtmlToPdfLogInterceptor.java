package com.xm.util.pdf;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class HtmlToPdfLogInterceptor implements Runnable{
    private final InputStream inputStream;

    public HtmlToPdfLogInterceptor(InputStream is) {
        this.inputStream = is;
    }

    @Override
    public void run() {
        InputStreamReader isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        try {
            String line;
            while((line = br.readLine()) != null) {
                log.debug(line);
            }
        } catch (IOException e) {
            log.error("wkHtmlToPdf转换失败", e);
        } finally {
            try {
                inputStream.close();
                isr.close();
                br.close();
            } catch (IOException e) {
                log.error("流关闭失败", e);
            }
        }
    }
}

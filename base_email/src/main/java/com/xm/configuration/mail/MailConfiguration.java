package com.xm.configuration.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


import java.util.Properties;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MailConfiguration {

    private final MailProperty mailProperty;

    @Bean
    @Lazy
    public JavaMailSender javaMailSender() {
        Properties properties = new Properties();

//        properties.setProperty("mail.debug", "true");//启用调试
        //mail.smtp.connectiontimeout：连接时间限制，单位毫秒。是关于与邮件服务器建立连接的时间长短的。默认是无限制。
        //mail.smtp.timeout：邮件smtp时间限制，单位毫秒。这个是有关邮件读取时间长短。默认是无限制。
        //mail.smtp.writetimeout：邮件发送时间限制，单位毫秒。有关发送邮件时内容上传的时间长短。默认同样是无限制。
        properties.setProperty("mail.smtp.timeout", "5000");
        properties.setProperty("mail.smtp.port", String.valueOf(mailProperty.getPort()));//设置端口

        properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        if (mailProperty.isSsl()) {
            properties.setProperty("mail.smtp.auth", "true");//开启认证
            properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(mailProperty.getPort()));//设置ssl端口
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailProperty.getHost());
        sender.setPort(mailProperty.getPort());
        sender.setUsername(mailProperty.getUsername());
        sender.setPassword(mailProperty.getPassword());
        sender.setDefaultEncoding("UTF-8");
        sender.setJavaMailProperties(properties);

        return sender;
    }
}

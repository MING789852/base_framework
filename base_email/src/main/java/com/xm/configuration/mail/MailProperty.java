package com.xm.configuration.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "msg.email")
@Component
@Data
public class MailProperty {
    private boolean enable;
    private String sender;
    private String username;
    private String password;
    private boolean ssl;
    private String host;
    private int port;
}

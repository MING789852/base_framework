package com.xm.util.mail;

import com.xm.util.mail.convert.MailTableValueConvert;
import lombok.Data;

import java.lang.reflect.Field;

@Data
public class ModelAndTitleMailMapping {
    private Field field;

    private String title;

    private MailTableValueConvert mailTableValueConvert;
}

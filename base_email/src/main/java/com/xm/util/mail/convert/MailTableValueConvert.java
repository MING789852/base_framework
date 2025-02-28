package com.xm.util.mail.convert;

public interface MailTableValueConvert <R,V>{
    R convert(V value);
}

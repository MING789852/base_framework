package com.xm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//字段注解
@Target({ElementType.FIELD})
//运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {
    String value();
}

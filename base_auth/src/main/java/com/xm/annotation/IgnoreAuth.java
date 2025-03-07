package com.xm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//方法注解
@Target({ElementType.METHOD})
//运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuth {
    boolean required() default true;
}

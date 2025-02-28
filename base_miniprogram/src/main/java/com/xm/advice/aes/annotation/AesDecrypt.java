package com.xm.advice.aes.annotation;

import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//属性注解
@Target({ElementType.METHOD})
//运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface AesDecrypt {
}

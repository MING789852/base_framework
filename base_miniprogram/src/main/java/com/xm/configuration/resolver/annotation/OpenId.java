package com.xm.configuration.resolver.annotation;

import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//属性注解
@Target({ElementType.PARAMETER})
//运行时生效
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenId {
}

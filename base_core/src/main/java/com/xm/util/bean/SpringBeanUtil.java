package com.xm.util.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("SpringBeanUtil")
public class SpringBeanUtil {

    private static ApplicationContext context;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        SpringBeanUtil.context = context;
    }

    public static <T> T getBeanByClass(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    public static <T> Map<String, T> getBeanMapOfType(Class<T> beanClass) {
        return context.getBeansOfType(beanClass);
    }

    public static <T> T getBeanByName(String name,Class<T> clazz){
        return context.getBean(name,clazz);
    }

    public static boolean isBeanOfTypeExists(Class<?> beanClass) {
        return context.getBeansOfType(beanClass).values().stream().findFirst().isPresent();
    }

}

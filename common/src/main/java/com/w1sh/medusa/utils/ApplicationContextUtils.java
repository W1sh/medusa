package com.w1sh.medusa.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ApplicationContextUtils {

    private final ApplicationContext applicationContext;

    public ApplicationContextUtils(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public <T> Collection<T> findAllByType(Class<T> tClass){
        return applicationContext.getBeansOfType(tClass).values();
    }
}

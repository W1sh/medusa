package com.w1sh.medusa.configurations;

import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

@Configuration
public class AppConfiguration {

    @Bean
    public ResourceBundleMessageSource messageSource() {
        final var source = new ResourceBundleMessageSource();
        source.setBasenames("messages");
        return source;
    }

    @Bean
    public Reflections reflections(){ return new Reflections("com.w1sh.medusa.events"); }

}

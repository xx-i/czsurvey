package com.github.czsurvey.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author YanYu
 */
@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private static final long MAX_AGE_SECS = 3600;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("HEAD", "OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE")
            .allowedHeaders("*")
            .exposedHeaders("*")
            .maxAge(MAX_AGE_SECS);
    }
}

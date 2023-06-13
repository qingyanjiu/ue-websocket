package com.zeta.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //对那些请求路径有效
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(10000);
    }
}

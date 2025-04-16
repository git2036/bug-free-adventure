//package com.example.demo.config;
//
//import org.apache.ibatis.builder.annotation.ProviderSqlSource;
//import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MyBatisConfig {
//
//    @Bean
//    public ConfigurationCustomizer configurationCustomizer() {
//        return configuration -> {
//            // 启用 Spring 管理的 Provider
//            configuration.setDefaultSqlProviderType(ProviderSqlSource.class);
//        };
//    }
//}
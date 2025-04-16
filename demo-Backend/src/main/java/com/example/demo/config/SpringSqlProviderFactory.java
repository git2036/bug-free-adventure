//package com.example.demo.config;
//
//import org.apache.ibatis.builder.annotation.ProviderSqlSource;
//import org.apache.ibatis.mapping.SqlSource;
//import org.apache.ibatis.session.Configuration;
//import org.springframework.context.ApplicationContext;
//
//public class SpringSqlProviderFactory extends ProviderSqlSource.ProviderMethodResolver {
//    private final ApplicationContext applicationContext;
//
//    public SpringSqlProviderFactory(ApplicationContext applicationContext) {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public Object resolve(Class<?> providerType) {
//        return applicationContext.getBean(providerType);
//    }
//}
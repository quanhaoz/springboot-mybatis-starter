package com.zhizus.mybatis;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by Dempe on 2017/7/20 0020.
 */
@Configuration
@AutoConfigureAfter(MybatisAutoConfiguration.class)
//兼容旧实现
@ConditionalOnProperty(name={"url"} , prefix ="datasource.druid")
public class MyBatisMapperScannerConfig implements EnvironmentAware {

    private String basePackage = "com.zhizus";

    private RelaxedPropertyResolver propertyResolver ;

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(@Qualifier("sqlSessionFactory") SqlSessionFactory sessionFactory) {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setAnnotationClass(MyBatisRepository.class);

        mapperScannerConfigurer.setBasePackage(basePackage);
        return mapperScannerConfigurer;
    }

    @Override
    public void setEnvironment(Environment environment) {
        propertyResolver = new RelaxedPropertyResolver(environment, "mybatis.") ;

        if(propertyResolver.getProperty("basePackage") != null){
            basePackage = propertyResolver.getProperty("basePackage") ;
        }
    }
}

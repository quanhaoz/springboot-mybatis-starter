package com.zhizus.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Dempe on 2017/7/20 0020.
 */
@Configuration
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
//兼容旧实现
@ConditionalOnProperty(name={"url"} , prefix ="datasource.druid")
public class DruidDataSourceBean implements EnvironmentAware {

    private RelaxedPropertyResolver propertyResolver ;

    private DruidDataSource dataSource = new DruidDataSource();

    @Override
    public void setEnvironment(Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment, "datasource.druid.");

    }

    @Bean
    public DataSource dataSource() throws SQLException {
        dataSource.setUrl(propertyResolver.getProperty("url"));
        dataSource.setUsername(propertyResolver.getProperty("username"));
        dataSource.setPassword(propertyResolver.getProperty("password"));
        dataSource.setInitialSize(propertyResolver.getProperty("initialSize", Integer.class , 1));
        dataSource.setMinIdle(propertyResolver.getProperty("minIdle", Integer.class , 1));
        dataSource.setMaxActive(propertyResolver.getProperty("maxActive", Integer.class , 32));
        dataSource.setMaxWait(propertyResolver.getProperty("maxWait", Long.class , 60000L));
        dataSource.setValidationQuery("SELECT 'x'");
        dataSource.setFilters("wall,stat");
        dataSource.setConnectionProperties("druid.stat.logSlowSql=true");
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        return dataSource ;
    }
}

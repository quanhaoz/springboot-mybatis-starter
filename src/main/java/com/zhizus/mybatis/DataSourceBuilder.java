package com.zhizus.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.typesafe.config.Config;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by Dempe on 2017/7/20 0020.
 */
public class DataSourceBuilder {
    public static DataSource createDataSource(ExtraConf.Conf conf) throws SQLException {
        DruidDataSource source = new DruidDataSource();
        source.setUrl(conf.getProperty("url"));
        source.setUsername(conf.getProperty("username"));
        source.setPassword(conf.getProperty("password"));
        source.setInitialSize(conf.getProperty("initialSize", Integer.class , 1));
        source.setMinIdle(conf.getProperty("minIdle", Integer.class , 1));
        source.setMaxActive(conf.getProperty("maxActive", Integer.class , 32));
        source.setMaxWait(conf.getProperty("maxWait", Long.class , 60000L));
        source.setValidationQuery("SELECT 'x'");
        source.setFilters("wall,stat");
        source.setConnectionProperties("druid.stat.logSlowSql=true");
        source.setDriverClassName("com.mysql.jdbc.Driver");
        return source ;
    }

    public static DataSource createDataSource(Config conf) throws SQLException {
        DruidDataSource source = new DruidDataSource();
        source.setUrl(conf.getString("url"));
        source.setUsername(conf.getString("username"));
        source.setPassword(conf.getString("password"));
        source.setInitialSize( 1);
        source.setMinIdle(1);
        source.setMaxActive(32);
        source.setMaxWait(  60000L);
        source.setValidationQuery("SELECT 'x'");
        source.setFilters("wall,stat");
        source.setConnectionProperties("druid.stat.logSlowSql=true");
        source.setDriverClassName("com.mysql.jdbc.Driver");
        return source ;
    }



    public static DataSource createDataSource( RelaxedPropertyResolver propertyResolver ) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
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


    public static SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPlugins(new Interceptor[]{new SqlExecutorInterceptor()});

        return factory.getObject();
    }

    public static SqlSessionFactory createSqlSessionFactory( DataSource dataSource , String  mappers) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPlugins(new  Interceptor[]{new SqlExecutorInterceptor()});

        if(mappers != null){
            String [] mappingArr  = mappers.split(",") ;
            Resource[] p = new ClassPathResource[mappingArr.length];
            for (int i = 0; i < mappingArr.length; i++) {
                p[i] = new ClassPathResource(mappingArr[i]);
            }
            factory.setMapperLocations(p);
        }
        return factory.getObject();
    }


    public static SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


    public MapperScannerConfigurer mapperScannerConfigurer(String sqlSessionFactoryName , String basePackage) {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName(sqlSessionFactoryName);
        mapperScannerConfigurer.setAnnotationClass(MyBatisRepository.class);
        mapperScannerConfigurer.setBasePackage(basePackage);
        return mapperScannerConfigurer;
    }
}

package com.zhizus.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dempezheng on 2017/7/20.
 */
@Configuration
@AutoConfigureAfter({MybatisAutoConfiguration.class, MyBatisMapperScannerConfig.class, DruidDataSourceBean.class})
public class DataSourceAutoConfiguration implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    ConcurrentHashMap<String, DataSource> groupDataSourceMap = Maps.newConcurrentMap();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (initDefaultDataSource(registry)) {
            LOGGER.info("has init default datasource");
        }

        try {
            loadMapperScanner(registry);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    private void registerScanner(BeanDefinitionRegistry registry, String sourceName, String mappers, String basePackage, DataSource dataSource) throws Exception {
        BeanDefinitionBuilder sessionFactoryDefinition = BeanDefinitionBuilder.genericBeanDefinition(DefaultSqlSessionFactory.class);

        SqlSessionFactory sessionFactory = DataSourceBuilder.createSqlSessionFactory(dataSource, mappers);
        sessionFactoryDefinition.addConstructorArgValue(sessionFactory.getConfiguration());

        String sessionFactoryName = "sessionFactory@" + sourceName;
        //定义sessionFactory
        registry.registerBeanDefinition(sessionFactoryName, sessionFactoryDefinition.getBeanDefinition());


        BeanDefinitionBuilder mapperScannerConfigurer = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);
        mapperScannerConfigurer.addPropertyValue("sqlSessionFactoryBeanName", sessionFactoryName);
        mapperScannerConfigurer.addPropertyValue("annotationClass", MyBatisRepository.class);
        mapperScannerConfigurer.addPropertyValue("basePackage", basePackage);

        //定义扫描的路径与注解类
        registry.registerBeanDefinition("mapperScanner@" + sourceName, mapperScannerConfigurer.getBeanDefinition());
    }


    private void loadMapperScanner(BeanDefinitionRegistry registry) throws Exception {
        Config load = ConfigFactory.load("application.conf");
        Config group = load.getConfig("mybatis").getConfig("group");
        String basePackage = group.getString("package");
        String mappers = group.getString("mappers");
        String name = group.getString("name");
        List<? extends Config> configList = group.getConfigList("source-group");
        //当datasource-group只有一个的时候，不需要自动切换
        if (configList.size() == 1) {
            Config config = configList.get(0);
            DataSource dataSource = DataSourceBuilder.createDataSource(config);
            registerScanner(registry, name, mappers, basePackage, dataSource);

        } else {
            for (Config config : configList) {
                String groupKey = config.getString("group");
                DataSource dataSource = DataSourceBuilder.createDataSource(config);
                groupDataSourceMap.put(groupKey, dataSource);
            }
            DataSource dynamicDataSource = new DynamicDataSource(groupDataSourceMap);
            registerScanner(registry, name, mappers, basePackage, dynamicDataSource);
        }

    }


    public boolean initDefaultDataSource(BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = registry.getBeanDefinition("dataSource");

        if (beanDefinition == null || "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration$NonEmbeddedConfiguration"
                .equals(beanDefinition.getFactoryBeanName())) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
            //覆盖spring boot的默认行为
            registry.registerBeanDefinition("dataSource", builder.getBeanDefinition());
            return true;
        }
        return false;
    }
}

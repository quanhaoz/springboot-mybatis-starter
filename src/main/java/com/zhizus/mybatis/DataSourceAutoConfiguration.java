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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by dempezheng on 2017/7/20.
 */
@Configuration
@AutoConfigureAfter({MybatisAutoConfiguration.class, MyBatisMapperScannerConfig.class, DruidDataSourceBean.class})
public class DataSourceAutoConfiguration implements BeanDefinitionRegistryPostProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void postProcessBeanDefinitionRegistry(
            BeanDefinitionRegistry registry) throws BeansException {


        if (initDefaultDataSource(registry)) {
            LOGGER.info("has init default datasource");
        }

        if (!ExtraConf.hasExtraConf(ExtraConf.ConfType.MYSQL)) {
            return;
        }
        Set<String> sourceNames = ExtraConf.getInitBeanNames(ExtraConf.ConfType.MYSQL);
        if (sourceNames == null || sourceNames.isEmpty()) {
            return;
        }
        for (String sourceName : sourceNames) {
            try {
                ExtraConf.Conf conf = ExtraConf.getConfig(ExtraConf.ConfType.MYSQL, sourceName);

                String basePackage = conf.getProperty("basePackage");

                LOGGER.info("registry extra datasource {} , basePackage :{}", new Object[]{sourceName, basePackage});

                BeanDefinitionBuilder sessionFactoryDefinition = BeanDefinitionBuilder.genericBeanDefinition(DefaultSqlSessionFactory.class);
                DataSource dataSource = DataSourceBuilder.createDataSource(conf);
                SqlSessionFactory sessionFactory = DataSourceBuilder.createSqlSessionFactory(dataSource, conf.getProperty("mappers"));
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


            } catch (Exception e) {
                LOGGER.error("register extra datasource: {} error ", sourceName, e);
            }
        }

    }

    Map<String, DataSource> groupDataSourceMap = Maps.newConcurrentMap();

    private void loadMapperScanner(  BeanDefinitionRegistry registry) throws SQLException {
        Config load = ConfigFactory.load("application.conf");
        Config group = load.getConfig("mybatis").getConfig("group");
        String basePackage = group.getString("package");
        List<? extends Config> configList = group.getConfigList("source-group");
        //当datasource-group只有一个的时候，不需要自动切换
        if (configList.size() == 1) {
            Config config = configList.get(0);
            DataSource dataSource = DataSourceBuilder.createDataSource(config);


        }
        for (Config config : configList) {
            String groupKey = config.getString("group");
            DataSource dataSource = DataSourceBuilder.createDataSource(config);
            groupDataSourceMap.put(groupKey, dataSource);

        }
    }

    private void registerSqlSessionFactory( BeanDefinitionRegistry registry,DataSource dataSource,String mappers,String groupKey) throws Exception {
        BeanDefinitionBuilder sessionFactoryDefinition = BeanDefinitionBuilder.genericBeanDefinition(DefaultSqlSessionFactory.class);
        SqlSessionFactory sessionFactory = DataSourceBuilder.createSqlSessionFactory(dataSource,mappers );
        sessionFactoryDefinition.addConstructorArgValue(sessionFactory.getConfiguration());

        String sessionFactoryName = "sessionFactory@" + groupKey;
        //定义sessionFactory
        registry.registerBeanDefinition(sessionFactoryName, sessionFactoryDefinition.getBeanDefinition());
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

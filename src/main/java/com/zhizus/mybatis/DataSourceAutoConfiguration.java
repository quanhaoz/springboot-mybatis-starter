package com.zhizus.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.kugou.config.ExtraConf;
import com.kugou.mybatis.MyBatisRepository;
import com.kugou.mybatis.autoconfigure.DataSourceBuilder;
import com.kugou.mybatis.autoconfigure.DruidDataSourceBean;
import com.kugou.mybatis.autoconfigure.MybatisAutoConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
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
import java.util.Set;

/**
 * Created by dempezheng on 2017/7/20.
 */
@Configuration
@AutoConfigureAfter({MybatisAutoConfiguration.class, com.kugou.mybatis.autoconfigure.MyBatisMapperScannerConfig.class, DruidDataSourceBean.class})
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

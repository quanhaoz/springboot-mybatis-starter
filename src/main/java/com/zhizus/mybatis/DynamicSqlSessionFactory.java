package com.zhizus.mybatis;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.springframework.stereotype.Service;

/**
 * Created by dempezheng on 2017/7/20.
 */
@Service
public class DynamicSqlSessionFactory extends DefaultSqlSessionFactory{
    public DynamicSqlSessionFactory(Configuration configuration) {
        super(configuration);
    }


}

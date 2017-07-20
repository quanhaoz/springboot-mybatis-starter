package com.zhizus.mybatis;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 1.监控mapper的执行状况，如果异常超过阈值，自动切换
 * 这里的lookUpKey可根据监控状况获取
 * Created by dempezheng on 2017/7/20.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        //get key
        //这里动态的获取可用的key
        return null;
    }
}

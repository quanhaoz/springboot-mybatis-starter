package com.zhizus.mybatis;

import com.zhizus.mybatis.loadbalance.RandomLoadBalance;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.List;

/**
 * 1.监控mapper的执行状况，如果异常超过阈值，自动切换
 * 这里的lookUpKey可根据监控状况获取
 * Created by dempezheng on 2017/7/20.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private volatile List<GroupInfo> list;

    private LoadBalance<GroupInfo> loadBalance;
    private IsolationStrategy<GroupInfo> isolationStrategy;

    public DynamicDataSource(List<GroupInfo> list) {
        this.list = list;
        isolationStrategy = new IsolationStrategy<>();
        loadBalance = new RandomLoadBalance(isolationStrategy);
    }


    @Override
    protected Object determineCurrentLookupKey() {
        //get key
        //这里动态的获取可用的key
        GroupInfo select = list.get(0);
        System.out.println(select);
        return select.getGroupKey();
    }

}

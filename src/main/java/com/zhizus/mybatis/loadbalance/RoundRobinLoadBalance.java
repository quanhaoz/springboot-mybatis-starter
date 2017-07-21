package com.zhizus.mybatis.loadbalance;


import com.zhizus.mybatis.GroupInfo;
import com.zhizus.mybatis.IsolationStrategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dempe on 2016/12/22.
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private AtomicInteger idx = new AtomicInteger(0);

    public RoundRobinLoadBalance(IsolationStrategy<GroupInfo> isolationStrategy) {
        super(isolationStrategy);
    }

    @Override
    public GroupInfo select(String key) {
        List<GroupInfo> availableServerList = getAvailableServerList();
        return availableServerList.get(idx.incrementAndGet() % availableServerList.size());
    }
}

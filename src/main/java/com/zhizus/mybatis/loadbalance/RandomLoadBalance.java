package com.zhizus.mybatis.loadbalance;


import com.zhizus.mybatis.GroupInfo;
import com.zhizus.mybatis.IsolationStrategy;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Dempe on 2016/12/26.
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    public RandomLoadBalance(IsolationStrategy<GroupInfo> isolationStrategy, List<GroupInfo> groupInfoList) {
        super(isolationStrategy, groupInfoList);
    }

    @Override
    public GroupInfo select(String key) {
        List<GroupInfo> availableServers = getAvailableServerList();
        int idx = (int) (ThreadLocalRandom.current().nextDouble() * availableServers.size());
        return availableServers.get((idx) % availableServers.size());
    }


}

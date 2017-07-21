package com.zhizus.mybatis.loadbalance;

import com.zhizus.mybatis.GroupInfo;
import com.zhizus.mybatis.IsolationStrategy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 低并发优先
 * Created by Dempe on 2016/12/22.
 */
public class ActiveWeightLoadBalance extends AbstractLoadBalance {

    public ActiveWeightLoadBalance(IsolationStrategy<GroupInfo> isolationStrategy) {
        super(isolationStrategy);
    }

    @Override
    public GroupInfo select(String key) {
        List<GroupInfo> availableServerList = getAvailableServerList();
        if (availableServerList.size() < 1) {
            return null;
        }
        Collections.sort(availableServerList, new Comparator<GroupInfo>() {
            @Override
            public int compare(GroupInfo o1, GroupInfo o2) {
                return o1.activeCountGet() - o2.activeCountGet();
            }
        });
        return availableServerList.get(0);
    }
}

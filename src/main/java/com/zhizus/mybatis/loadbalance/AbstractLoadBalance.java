package com.zhizus.mybatis.loadbalance;

import com.google.common.collect.Lists;
import com.zhizus.mybatis.GroupInfo;
import com.zhizus.mybatis.IsolationStrategy;
import com.zhizus.mybatis.LoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;


/**
 * Created by Dempe on 2016/12/26.
 */
public abstract class AbstractLoadBalance implements LoadBalance<GroupInfo> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalance.class);


    private List<GroupInfo> GroupInfoList = Lists.newArrayList();

    private IsolationStrategy<GroupInfo> isolationStrategy;

    public AbstractLoadBalance(IsolationStrategy<GroupInfo> isolationStrategy) {
        this.isolationStrategy = isolationStrategy;
    }

    public List<GroupInfo> getAvailableServerList() {
        List<GroupInfo> availableList = Lists.newArrayList();
        Set<GroupInfo> failed = isolationStrategy.getFailed();
        for (GroupInfo GroupInfo : GroupInfoList) {
            if (!failed.contains(GroupInfo)) {
                availableList.add(GroupInfo);
            }
        }
        if (availableList.isEmpty()) {
            for (GroupInfo GroupInfo : GroupInfoList) {
                availableList.add(GroupInfo);
            }
            LOGGER.warn("available server list is empty, use failed back list instead");
        }
        return GroupInfoList;
    }

    @Override
    public void setList(List<GroupInfo> list) {
        GroupInfoList = list;
    }
}

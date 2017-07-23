package com.zhizus.mybatis;

import com.zhizus.mybatis.loadbalance.LoadBalance;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 1.监控mapper的执行状况，如果异常超过阈值，自动切换
 * 这里的lookUpKey可根据监控状况获取
 * Created by dempezheng on 2017/7/20.
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    private LoadBalance<GroupInfo> loadBalance;

    public DynamicDataSource(LoadBalance<GroupInfo> loadBalance) {
        this.loadBalance = loadBalance;
    }


    @Override
    protected Object determineCurrentLookupKey() {
        //get key
        GroupInfo select = loadBalance.select(null);

        String groupKey = select.getGroupKey();
        GroupParamHolderId.setGroupId(groupKey);
//        System.out.println(">>>>>>>>>>>>"+GroupParamHolderId.getAndRemoveGroupId());
        return groupKey;
    }

}

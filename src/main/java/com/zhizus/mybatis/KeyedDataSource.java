package com.zhizus.mybatis;

import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Dempe on 2017/7/21 0021.
 */
public class KeyedDataSource {

    private ConcurrentHashMap<String, DataSource> groupDataSourceMap;

    public DataSource getDataSourceByKey(String groupKey) {
        return groupDataSourceMap.get(groupKey);
    }
}

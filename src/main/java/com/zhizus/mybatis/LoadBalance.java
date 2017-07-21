package com.zhizus.mybatis;

import java.util.List;

/**
 * Created by Dempe on 2016/12/26.
 */
public interface LoadBalance<T> {

    T select(String key);

    List<GroupInfo> getAvailableServerList();

    void setList(List<GroupInfo> list);
}

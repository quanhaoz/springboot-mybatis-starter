package com.zhizus.mybatis;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dempe on 2017/7/21 0021.
 */
public class GroupInfo {
    private String groupKey;
    private String mappers;
    private String databaseName;
    private String url;
    private AtomicInteger activeCount = new AtomicInteger(0);

    public AtomicInteger getActiveCount() {
        return activeCount;
    }

    public GroupInfo setActiveCount(AtomicInteger activeCount) {
        this.activeCount = activeCount;
        return this;
    }

    public int activeCountGet() {
        return activeCount.get();
    }

    public String getGroupKey() {
        return groupKey;
    }

    public GroupInfo setGroupKey(String groupKey) {
        this.groupKey = groupKey;
        return this;
    }

    public String getMappers() {
        return mappers;
    }

    public GroupInfo setMappers(String mappers) {
        this.mappers = mappers;
        return this;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public GroupInfo setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GroupInfo setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String toString() {
        return "GroupInfo{" +
                "groupKey='" + groupKey + '\'' +
                ", mappers='" + mappers + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", url='" + url + '\'' +
                ", activeCount=" + activeCount +
                '}';
    }
}

package com.zhizus.mybatis;

/**
 * Created by dempezheng on 2017/7/20.
 */
public class GroupParamHolderId {

    public final static ThreadLocal<String> groupIdThreadLocal = new ThreadLocal<>();

    public static void setGroupId(String id) {
        groupIdThreadLocal.set(id);
    }

    public static String getGroupId() {
        return groupIdThreadLocal.get();
    }

    public static String getAndRemoveGroupId() {
        try {
            return groupIdThreadLocal.get();
        } finally {
            groupIdThreadLocal.remove();
        }
    }
}

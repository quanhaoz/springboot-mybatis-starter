package com.zhizus.mybatis.mapper;

import com.zhizus.mybatis.GeneratedGroupId;
import com.zhizus.mybatis.GroupParamId;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

/**
 * Created by dempezheng on 2017/7/20.
 */
public interface SampleMapper<T> {

    @Insert("")
    @GeneratedGroupId
    int insert(T t);

    @Select("")
    T selectById(@GroupParamId String id);

}

package com.zhizus.mybatis.mapper;

import com.zhizus.mybatis.GeneratedGroupId;
import com.zhizus.mybatis.MyBatisRepository;
import com.zhizus.mybatis.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by dempezheng on 2017/7/20.
 */
@MyBatisRepository
public interface SampleMapper {


    @GeneratedGroupId
    int insert(User t);

    @Select("select * from user where id=#{id}")
    User selectById(@Param("id") Integer id);



}

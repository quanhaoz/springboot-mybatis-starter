package com.zhizus.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Strings;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Dempe on 2017/7/20 0020.
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SqlExecutorInterceptor implements Interceptor {

    private IsolationStrategy isolationStrategy;

    public SqlExecutorInterceptor(IsolationStrategy isolationStrategy) {
        this.isolationStrategy = isolationStrategy;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if (args != null && args.length > 0) {
            MappedStatement statement = (MappedStatement) args[0];

            if (statement != null) {
                SqlCommandType sqlCommandType = statement.getSqlCommandType();

                DataSource ds = statement.getConfiguration().getEnvironment().getDataSource();
                if (ds instanceof DruidDataSource) {
                    @SuppressWarnings("resource")
                    DruidDataSource dataSource = (DruidDataSource) ds;
                }
            }
        }
        String groupKey = "";
        try {
            Object obj = invocation.proceed();
            groupKey = GroupParamHolderId.getAndRemoveGroupId();

            return obj;
        } catch (Exception e) {
            if(!Strings.isNullOrEmpty(groupKey)){
                isolationStrategy.fail(groupKey);
            }
            throw e;
        } finally {
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}

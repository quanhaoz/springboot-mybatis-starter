## springboot-mybatis-starter


![Alt text](./20170723103201.jpg)

## future

- 监控统计sql，故障时候自动切换数据源


- 跳单




实现思路：

1.监控mapper执行状态，维护<groupKey,DataSource>,
实现类似hystrix的容灾，断路器

2.实现AbstractRoutingDataSource.determineCurrentLookupKey方法，返回可用的机房groupKey，
是的getConnection方法获取的都是可用的机房连接。


3.实现GeneratedGroupId注解，方法上面添加该注解 可以自动生成特定格式的id（groupKey-id），将该id作为主键写入db
实现GroupIdParam注解，查询时候自动根据该id路由到指定的DataSource


todo
1.找到拦截mapper执行状态入口，监控状态


2.找到mapper代理的地方，并扩展，植入id生成的代码

3.将GroupIdParam标注的字段传递给AbstractRoutingDataSource.determineCurrentLookupKey方法，解析id，获取groupKey
并返回。 

暂时没有好的方法将id传递过去，正常传递，需要改动的api较大，所以考虑将id放到threadLocal中，

参考文章：
[微信支付：如何打造移动支付时代的高可用收银系统？](http://mp.weixin.qq.com/s?__biz=MjM5MDE0Mjc4MA==&mid=2650995797&idx=1&sn=cbbf307bb7abff8923af05947ff58f05&chksm=bdbf04068ac88d10a06157721ac466170182d340eb7d966c9f0b11b0f33c265b6829d40a4d29&mpshare=1&scene=1&srcid=0721y6FpiPLbTawKHkKmfWcS#rd)

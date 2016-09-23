# Easy-retry
轻量级服务重试库，类似 [Spring Retry][1]，适用于小概率失败的服务重试场景

To make processing more robust and less prone to failure, sometimes it helps to automatically retry a failed operation in case it might succeed on a subsequent attempt.

## Features
* 依赖`DelayQueue`，一个拥有特化参数`Delayed`的`BlockingQueue`
* 提供重试回调接口`RetryAbstractService`，利用反射回调相应方法
* 提供数据持久化接口`RetryDataPersistService`，支持重启恢复

## Usage
* 编写重试接口实现类，如`RetryRollBackTestService`
* 如果需保障Spring重启恢复未完成的任务，编写数据持久化接口实现类，如`RetryDataPersistTestServiceImpl`
* Spring注入重试任务
```xml
<bean id="retryTask" class="cc.ttcc.retry.RetryTask">
    <!--必选服务，用于重试回调，执行具体的重试逻辑，须实现RetryAbstractService服务 -->
    <constructor-arg name="callbackService" ref="rollBackService"/>
    <!--可选服务，用于数据持久化以便重启恢复，须实现RetryDataPersistService服务-->
    <constructor-arg name="persistService" ref="persistService"/>
</bean>
<bean id="rollBackService" class="cc.ttcc.retry.service.RetryRollBackTestService"/>
<bean id="persistService" class="cc.ttcc.retry.service.RetryRedisPersistServiceImpl"/>
```
* 或者手动初始化重试对象
```java
// 必选服务，用于重试回调，执行具体的重试逻辑，须实现RetryAbstractService服务
RetryAbstractService retryBackService = new RetryRollBackTestService();

// 可选服务，用于数据持久化以便重启恢复，须实现RetryDataPersistService服务
RetryDataPersistService retryDataPersistService = new RetryRedisPersistServiceImpl();

RetryTask retryTask = new RetryTask(retryBackService, retryDataPersistService);
```
Restry
```java
String remark = "remark";
String method = "rollback";
String parameters = "test";
int[] intervals = new int[] { 60, 120, 300, 600 };
RetryEntity retryItem = new RetryEntity(remark, rollback, parameters, intervals);
retryTask.add(retryItem);
```
其中`RetryEntity`属性：  
* remark：重试项的备注名，便于只管区分不同任务，非必须；  
* method：重试项的回调方法`retryBackService.method`，返回true则重试成功，返回false则进入下一次重试，须自行捕获异常；  
* parameters：method对应的参数（一般是JSON字符串），非必须  
* intervals：重试的时间间隔（秒），递增，可不传    
* uuid：唯一标识，格式："六位随机字母串-时间戳"
* retryTimes：当前重试的次数，初始为1

## Test
See `cc.ttcc.retry.MainTest`  
如要测试`RetryRedisPersistServiceImpl`重启恢复，请自行实现`RedisUtil`逻辑

[1]: http://docs.spring.io/spring-batch/trunk/reference/html/retry.html
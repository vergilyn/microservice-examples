# spring-cloud-alibaba
nacos作为注册（或配置中心）均是单独的服务，需要自行安装。参考: [Nacos quick-start]

相比较于spring-cloud-eureka项目，只是将eureka替换为nacos，并且实现了配置中心（动态限流，动态配置等）。
（其余功能完全一样）

参考:
 - [Nacos quick-start]
 - [Nacos Github]
 - [Nacos Examples]
 - [Spring-Cloud-Alibaba Github]


## 注意
1. 
特别需要注意，示例中更改了nacos-namespace，注意修改成相应的id（nacos控制台中可以获得）。
```
spring.cloud.nacos.config.namespace: 35c1aa066-7a0b-4199-81e9-db70852e5409
```

2.
**关于动态配置：暂时并不清楚具体哪些配置支持动态配置，具体测试再说。**
（待了解动态配置的原理）

3. bootstrap 与 application
加载顺序: bootstrap比application先加载。（bootstrap优先级更高，其部分属性是可以被覆盖的）

bootstrap 配置文件有以下几个应用场景: 
- 使用spring-cloud-config配置中心时，需要在 bootstrap 配置文件中添加连接到配置中心的配置属性来加载外部配置中心的配置信息；
- 一些固定的不能被覆盖的属性
- 一些加密/解密的场景；


## 测试
| project  | server.port  | management.server.port |
| :-----   | :----------: |:----------------------:|
| NACOS    | 8848         | -----                  |
| GATEWAY  | 9000         | 19000                  |
| BUSINESS | 901X         | 1901X                  |
| ACCOUNT  | 902X         | 1902X                  |
| ORDER    | 903X         | 1903X                  |
| STORAGE  | 904X         | 1904X                  |

访问: http://127.0.0.1:8848/nacos （默认帐号密码nacos）

1. 创建namespace
```
命名空间名: vergilyn-nacos-local
描述: xxxx

创建成功后的命名空间ID：35c1aa066-7a0b-4199-81e9-db70852e5409
```
修改`bootstrap.yaml`中的`namespace`(填写namespace-id)。

2. 创建配置文件
注意: 一定要在相应的namespace中创建

```
# 公共的actuator配置

Data ID: common-actuator.yaml
Group: DEFAULT_GROUP
配置格式: yaml
配置内容:
# actuator
management:
  server:
    port: 1${server.port}
    ssl:
      enabled: false
  endpoint:
    health:
      show-details: always
      cache:
        time-to-live: 30s
    httptrace:
      enabled: true
  endpoints:
    web:
      exposure:
        include:
          - info
          - health
          - httptrace
        
# 自定义扩展/actuator/info信息
info:
  app:
    name: ${spring.application.name}
    port: ${server.port}
```

```
# OpenFeign

Data ID: feign.yaml
Group: FEIGN_GROUP
配置格式: yaml
配置内容:
# feign
# org.springframework.cloud.openfeign.FeignClientProperties
feign:
  hystrix:
    enabled: true
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
```

```
# API Gateway

Data ID: api-gateway.yaml
Group: GATEWAY_GROUP
配置格式: yaml
配置内容:
# gateway
spring:
  # redis-rate-limiter 注意: 与RedisTemplate冲突
  redis:
    host: localhost
    port: 6379
    database: 4
  # gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      default-filters: # org.springframework.cloud.gateway.filter
      #  - PrefixPath=/api
        - name: Hystrix
          args:
            name: defaultHystrixCommand
            fallbackUri: 'forward:/default-fallback'

      routes:
        - id: account
          uri: lb://cloud-alibaba-client-account
          predicates:
            - Path=/api/account/**
          filters:
            - StripPrefix=1

        - id: order
          uri: lb://cloud-alibaba-client-order
          predicates:
            - Path=/api/order/**

        - id: order
          uri: lb://cloud-alibaba-client-storage
          predicates:
            - Path=/api/storage/**
          filters:
            - StripPrefix=1

        - id: business
          uri: lb://cloud-alibaba-client-merge-business
          predicates:
            - Path=/api/business/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.burstCapacity: 10
                redis-rate-limiter.replenishRate: 1
                key-resolver: "#{@ipKeyResolver}"
```

```
# Hystrix

Data ID: gateway-hystrix.yaml
Group: HYSTRIX_GROUP
配置格式: yaml
配置内容:
# hystrix
# com.netflix.hystrix.HystrixThreadPoolProperties
# com.netflix.hystrix.HystrixCommandProperties
hystrix:
  threadpool:
    default:
      coreSize: 20
      maxQueueSize: -1
      queueSizeRejectionThreshold: 5
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 4000

```

```
# Hystrix

Data ID: hystrix.yaml
Group: HYSTRIX_GROUP
配置格式: yaml
配置内容:
# hystrix
# com.netflix.hystrix.HystrixThreadPoolProperties
# com.netflix.hystrix.HystrixCommandProperties
vergilyn:
  custom:
    hystrix-timeout: 2000

hystrix:
  threadpool:
    default:
      coreSize: 20
      maxQueueSize: -1
      queueSizeRejectionThreshold: 5
  command:
    default:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: ${vergilyn.custom.hystrix-timeout}
```

```
# 公共的logging配置

Data ID: common-logging.yaml
Group: COMMON_GROUP
配置格式: yaml
配置内容:
# logging
logging:
  level:
    org.springframework.cloud.gateway: trace
    org.springframework.http.server.reactive: debug
    org.springframework.web.reactive: debug
    reactor.ipc.netty: debug
```

```
Data ID: datasource-druid.yaml
Group: DATASOURCE_GROUP
配置格式: yaml
配置内容:
spring:
  datasource:
    username: root
    password: 123456
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/test_microservice?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 100
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validationQuery: select 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      filters: config,wall,stat
      poolPreparedStatements: true
      maxPoolPreparedStatementPerConnectionSize: 21
      maxOpenPreparedStatements: 22
      connectionProperties: druid.stat.slowSqlMillis=200;druid.stat.logSlowSql=true;config.decrypt=false
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: /druid/*,*.js,*.gif,*.jpg,*.bmp,*.png,*.css,*.ico
        session-stat-enable: true
        session-stat-max-count: 10
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: true
        login-username: admin
        login-password: admin
      enable: true
```





3. 动态配置、限流等
访问: http://127.0.0.1:1xxxx/actuator/info
通过在nacos控制台动态修改保存common-actuator，可以发现能获取到更新后的info信息。

或者修改gateway.yaml中的`redis-rate-limiter.burstCapacity: 3`、
`redis-rate-limiter.replenishRate: 1`也可以及时生效。

但部分配置不支持动态，比如`spring.redis.database`，服务可以获取到更新后的配置，
但`redisTemplate`并未改变连接，但是database-connect的连接又可以。
（尝试过各种方法，均没能动态修改redisTemplate，等了解动态刷新配置的原理再说）

## 疑问
1.
```
spring:
  cloud:
    nacos:
      config:
        server-addr: 127.0.0.1:8848
        namespace: 3df4b460-c0ef-4799-91a1-417eeba054c6
        file-extension: yaml
        group: COMMON_CONFIG
        shared-dataids: common-actuator.yaml
        refreshable-dataids: common-actuator.yaml
```
期望shared-dataids找的是COMMON_CONFIG下的common-actuator.yaml。但貌似group并未生效，还是找的DEFAULT_GROUP。

通过查看源码，shared-dataids的group="DEFAULT_GROUP"，并不支持配置！
see: `org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceLocator.loadSharedConfiguration`
（jar: spring-cloud-alibaba-nacos-config-0.9.0.RELEASE）

2. service-instance未正确注册到指定的namespace。
[ISSUE: Nacos Discovery namespace is not support isolate the data](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/issues/317)

```
spring:
  application:
    name: cloud-alibaba-client-consumer
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        namespace: 3df4b460-c0ef-4799-91a1-417eeba054c6 # 期望注册到自定义的namespace
```
由issue可知，貌似是因为nacos-client-0.6.2不支持，在nacos-client-0.8.0之后修复。
然而spring-cloud-starter-alibaba-nacos-discovery-0.2.1.RELEASE（未发布更新版本的starter）中依赖的是nacos-client-0.6.2。

解决: 将nacos-client更新到0.9.1成功解决该问题。

[Nacos quick-start]: https://nacos.io/zh-cn/docs/quick-start.html
[Nacos Github]: https://github.com/alibaba/nacos
[Nacos Examples]: https://github.com/nacos-group/nacos-examples
[Spring-Cloud-Alibaba Github]: https://github.com/spring-cloud-incubator/spring-cloud-alibaba
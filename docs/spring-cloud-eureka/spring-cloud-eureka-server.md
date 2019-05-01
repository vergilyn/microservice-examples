# spring-cloud-eureka-server
- [spring-cloud-netflix project]
- [spring-cloud-netflix reference]
- [spring-cloud-ribbon reference]

## 概念
### Eureka
- 构建服务注册中心
- 服务注册与服务发现

1. 服务注册的任务由eureka-server完成。
2. 服务发现的任务由eureka-client完成。

### Ribbon
服务消费的任务由Ribbon完成。Ribbon是一个基于HTTP和TCP的客户端负载均衡器，它可以在通过客户端中配置的ribbonServerList服务端列表去**轮询**访问以达到均衡负载的作用。

Ribbon在Eureka服务发现的基础上，实现了一套对服务实例的选择策略，从而实现对服务的消费。
访问实例的选择
> eureka中有region和zone的概念，一个region中可以包含多个zone，每个服务客户端需要被注册到一个zone中，所以每个客户端对应一个region和zone。
在进行服务调用的时候，优先访问同处一个zone中的服务提供方，若访问不到，就访问其他的zone。



## 2. 基础架构
eureka服务治理基础架构的三个核心要素：
- 服务注册中心: eureka提供的服务端，提供服务注册与发现的功能。
- 服务提供者: 提供服务的应用，可以是spring-boot应用，也可以是其他技术平台且遵循eureka通信机制的应用。它将自己提供的服务注册到eureka，以供其他应用发现。
- 服务消费者: 消费者应用从服务注册中心获取服务列表，从而是消费者可以知道去何处调用其所需的服务。

很多时候，客户端既是服务提供者也是服务消费者。

### 2.1 服务注册中心
集群注册中心下，当服务提供者发送注册请求到一个服务注册中心时，它会将该请求转发给集群中相连的其他注册中心，从而实现注册中心之间的服务同步。
通过服务同步，两个服务提供者的服务信息就可以通过两台服务注册中心中的任意一台获取到。

在服务注册时，需要确认参数是否正确:
- eureka.client.register-with-eureka: 是否启动服务注册，默认true。

### 2.2 服务续约（renew）
服务提供者在注册完服务后，会维护一个心跳来持续通知eureka-server，以防止eureka-server的“剔除任务”将该服务实例从服务列表中移除。

- eureka.instance.lease-renewal-interval-in-seconds: 服务续约调用的间隔，默认30s。
- eureka.instance.lease-expiration-duration-in-seconds: eureka-server在收到最后一次心跳之后等待的时间上限，单位为秒，超过则剔除，默认90s。

客户端可以告诉服务端按照此规则等待自己，而不统一由服务端控制。

### 2.3 服务消费者
服务消费者启动时，会发送一个REST请求给服务注册中心，来获取上面住的服务清单。
为了性能考虑，eureka-server会维护一份只读的服务清单返回给客户端，同时该缓存清单会每间隔30s更新一次。

- eureka.client.fetch-registry: 是否允许检索服务，默认true。
- eureka.client.registry-fetch-interval-seconds: 从eureka-server获取注册信息的间隔时间，默认30s。

### 2.4 服务下线
系统运行过程中，必然会面临某个服务实例需要关闭或重启的情况，在服务异常期间，自然不会希望客户端会继续调用该实例。
所以在客户端程序中，当服务进行**正常的关闭操作**时，会触发一个服务下线的REST请求给eureka-server。
eureka-server在接收到请求后，将该服务状态设置为下线DOWN，并将下线事件传播出去。

但有时候并不是正常下线，可能是内存溢出、网络异常导致服务不能正常工作，而注册中心并未收到“服务下线”的请求。
为了从服务列表中那这些无法提供服务的实例移除，eureka-server在启动的时候会创建一个定时任务，默认间隔60s将当前清单中超过90s没有续约的服务移除。

- eureka.server.eviction-interval-timer-in-ms: 剔除失效服务间隔，默认60*1000ms。
- eureka.instance.lease-expiration-duration-in-seconds: 客户端可以定制设置，告知服务端。

### 2.5 自我保护
eureka-server在运行期间，会统计心跳失败的比例在15min之内是否低于85%，如果出现低于的情况（单机调试或实际生产环境由于网络不稳定导致），
eureka-server会将当前的实例注册信息保存起来，让这些实例不会过期，尽可能保护这些注册信息。
但是，在这段保护期间内若出现问题，那么客户端很容易拿到实际不存在的服务实例，出现调用失败的情况，
所以客户端必须要有容错机制，比如可以使用请求充实、断路器等机制。

本地调试很容易触发注册中心的保护机制，这会使得注册中心维护的服务实例不够准确。
所以本地开发时，可以通过参数来关闭保护机制，以确保注册中心可以将不可用的实例正确剔除。

[1] org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean
- eureka.server.enable-self-preservation: 是否开启注册中心的保护机制，默认true。
- eureka.server.renewal-percent-threshold: 触发自我保护的心跳数比例阈值，默认0.85。
- eureka.server.renewal-threshold-update-interval-ms: 多久重置一下心跳阈值，默认15 * 60 * 1000ms。

## 3. 源码分析
### 3.1 eureka-client如何注册到eureka-server
通过跟踪client主类中的配置`@EnableDiscoveryClient`，发现真正实现服务发现的类是`com.netflix.discovery.DiscoveryClient`，其主要功能是::
- 向eureka-server注册服务实例
- 向eureka-server服务租约
- 当服务实例关闭时，向eureka-server取消租约
- 查询eureka-server中的服务实例列表
- eureka-client需要配置一个eureka-server的URL列表

#### 3.1.1 eureka-client如何配置eureka-server的URL列表
之前我们配置了`eureka.client.service-url.defaultZone`，通过跟踪源码`org.springframework.cloud.netflix.eureka.EurekaClientConfigBean`可知:
[1] service-url的类型是Map<String, String>。
[2] service-url初始化设置了一个kv。"defaultZone": "http://localhost:8761/eureka" 。

回到`DiscoveryClient`发现其实现了`EurekaClient`中的`getServiceUrlsFromConfig()`、`getServiceUrlsFromDNS()`。
通过注释可知，这2个方法已`@Deprecated`，取而代之的是`com.netflix.discovery.endpoint.EndpointUtils`。

阅读源码`com.netflix.discovery.endpoint.EndpointUtils.getServiceUrlsMapFromConfig(...)`可知，client遗迹加载了2个内容: `region`和`zone`。
- 通过getRegion函数，可知一个微服务应用只属于一个region。通过`eureka.client.region`定义，默认为default。
- 通过getAvailabilityZones `org.springframework.cloud.netflix.eureka.EurekaClientConfigBean.getAvailabilityZones(java.lang.String)`函数可知
    1. region与zone是一对多关系。
    2. 当某个region的zones为null时，默认返回defaultZone。
    3. 可以通过`eureka.client.availability-zones`设置，其类型是`Map<String, String>`，key表示region，value表示zones（通过逗号分割）。

在获取region和zone的信息后，才开始真正加载eureka-server的具体地址。通过一定的算法确定加载位于哪一个zone配置的serviceUrls.
```
    int myZoneOffset = getZoneOffset(instanceZone, preferSameZone, availZones);

    String zone = availZones[myZoneOffset];
    List<String> serviceUrls = clientConfig.getEurekaServerServiceUrls(zone);
```
通过查看`org.springframework.cloud.netflix.eureka.EurekaClientConfigBean.getEurekaServerServiceUrls(...)`可知serviceUrls获取逻辑。

当使用ribbon来实现服务调用时，对于zone的设置可以在负载均衡时实现区域亲和特性：
ribbon的默认策略会优先访问通客户端处于一个zone中的服务端实例，只有当同一个zone中没有可用服务端实例时才会访问其他zone中的实例（ribbon通过轮询实现）。
**所以通过zone属性的定义，配合实际部署的物理结构，可以有效地设计出对区域性故障的容错集群。**



项目端口
[1] 8761: eureka-server
[2] 8770: eureka-client-add
[3] 8780: eureka-client-sub
[4] 8790: eureka-client-consumer

1. maven依赖中，`spring-cloud-starter-eureka-server | spring-cloud-starter-eureka-client`已被废弃。
最新的是`spring-cloud-starter-netflix-eureka-server | spring-cloud-starter-netflix-eureka-client`。

2. `@EnableEurekaServer`注解启用一个服务注册中心，提供给其它应用进行对话。
在默认设置下，该服务注册中心也会将自己作为client来尝试注册它自己。我们需要禁用它的client注册行为。
```YAML
server:
  port: 8761

--- # eureka-server
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/

```

- eureka.client.register-with-eureka: 由于自身就是注册中心，所以设置为`false`，代表不向注册中心注册自己。
- eureka.client.fetch-registry: 由于注册中心的职责就是维护实例，它并不需要去检索服务，所以设置成`false`。
- eureka.client.service-url.defaultZone: 指定服务注册中心的地址。

3. `@EnableDiscoveryClient`激活Eureka中的DiscoveryClient实现。
（自动化配置，创建DiscoveryClient接口针对Eureka客户端的EurekaDiscoveryClient实例）
让应用注册为Eureka客户端应用，以获得服务发现的能力。
```YAML
server:
  port: 8762

spring:
  application:
    name: eureka-client-add # 即eureka-client的serviceId

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/    # 指定服务注册中心的地址
    enabled: true

```

- `org.springframework.cloud.client.discovery.DiscoveryClient`: 可以从服务注册中心获取服务相关的信息。

4. `instance-id`生成规则。
源码参考：
- `org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration.eurekaInstanceConfigBean(InetUtils inetUtils, ManagementMetadataProvider managementMetadataProvider)`
- `org.springframework.cloud.commons.util.IdUtils.getDefaultInstanceId(PropertyResolver resolver)`
- `eureka.instance.prefer-ip-address=true`，true显示`eureka.instance.ip-address`，false显示`eureka.instance.hostname`。
`org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean.getHostName(boolean refresh)`

　　默认生成规则：`${vcap.application.instance_id} | (${spring.cloud.client.hostname}:${spring.application.name}:(${spring.application.instance_id} | ${server.port}))`。
或者直接定义`eureka.instance.instance-id`。

5. `@LoadBalanced`开启eureka-client负载均衡。



## 高可用注册中心（注册中心集群）
如果使用单节点的服务注册中心，意味着若此节点故障，则所有服务之间无法正常访问。这在生产环境显然不合适，所以需要构建高可用的服务注册中心增强系统的可用信。
在Eureka的服务治理设计中，所有的节点即是服务提供方，也是服务消费房，服务注册中心也不列外。

在之前的单节点注册中心，通过设置以下参数，让服务注册中心不注册自己：
```yaml
eureka:
  client:
    register-with-eureka: false # 是否启用服务注册。
    fetch-registry: false # 是否检索服务
```
高可用实际上就是将自己作为服务向其他服务注册中心注册自己，这样就可以形成一组互相注册的服务注册中心，以实现服务清单的互相同步，达到高可用。
特别: 是eureka服务端（服务注册中心）向**其他服务注册中心**注册自己，并不是向**eureka客户端**注册自己。
```yaml
spring.application.name=eureka-service-01
server.port=1001
eureka.client.service-url.defaultZone=http://127.0.0.1:1002/eureka/,http://127.0.0.1:1003/eureka/

---
spring.application.name=eureka-service-02
server.port=1002
eureka.client.service-url.defaultZone=http://127.0.0.1:1001/eureka/,http://127.0.0.1:1003/eureka/

---
spring.application.name=eureka-service-03
server.port=1003
eureka.client.service-url.defaultZone=http://127.0.0.1:1001/eureka/,http://127.0.0.1:1002/eureka/

---
spring.application.name=eureka-client-01
eureka.client.service-url.defaultZone=http://127.0.0.1:1001/eureka/,http://127.0.0.1:1002/eureka/,http://127.0.0.1:1003/eureka/

---
spring.application.name=eureka-client-02
eureka.client.service-url.defaultZone=http://127.0.0.1:1001/eureka/,http://127.0.0.1:1002/eureka/,http://127.0.0.1:1003/eureka/

```
这样client-01、client-02同时被注册到service-01、service-02、service-03中。
若此时service-01故障，因为client-01、client-02同时还注册在service-02、service-03中，所以彼此之间依然可以正常访问。

## 常用配置及说明
[Spring Cloud Eureka 常用配置及说明]


[Spring Cloud Eureka 常用配置及说明]: https://www.cnblogs.com/li3807/p/7282492.html
[spring-cloud-netflix project]: https://spring.io/projects/spring-cloud-netflix#overview
[spring-cloud-netflix reference]: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-eureka-server
[spring-cloud-ribbon reference]: https://cloud.spring.io/spring-cloud-static/spring-cloud-netflix/2.0.2.RELEASE/single/spring-cloud-netflix.html#spring-cloud-ribbon
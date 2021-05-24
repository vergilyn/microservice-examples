# spring-cloud-eureka

spring-cloud全家桶搭建的微服务示例

## 测试
利用idea的`override parameters`指定不同的`server.port`和`management.server.port`。

| project  | server.port  | management.server.port |
| :-----   | :----------: |:----------------------:|
| SERVER   | 8761         | 18761                  |
| GATEWAY  | 8765         | 18765                  |
| ACCOUNT  | 881X         | 1881X                  |
| ORDER    | 882X         | 1882X                  |
| STORAGE  | 883X         | 1883X                  |


## 备注
1. hystrix的请求合并、请求缓存
请求合并需要服务提供者支持。
请求缓存是保存在服务消费者。

本示例中未实现，待完成。

2. 服务注册中心集群
未在示例中体现出来，但不难理解。

3. feign可以全局配置，也可以指定服务配置
未在示例中体现出来

4. API Gateway
请求鉴权、请求拦截、请求记录等未展示。

特别需要注意:
  貌似服务之间调用默认未传递Cookie、Headers等敏感信息。
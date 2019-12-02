# spring-boot-actuator

[spring-boot reference: spring-boot-actuator][spring-boot-actuator cn]  
[Spring Boot 2.0官方文档之 Actuator][spring-boot-actuator zh]  
[Introducing Actuator Endpoints in Spring Boot 2.0]

## 问题
1. 访问`http://localhost:xxxx/health`返回`404`。
spring-boot 2.X的访问端点改为`http://localhost:xxxx/actuator/health`，所有endpoints移至`/actuator`下。
可以通过`management.endpoints.web.base-path=/`修改。

2. `health`只返回`{"status":"UP"}`。
修改配置文件application.yaml中的`management.endpoint.health.show-details=always`。

3. actuator只暴露了`health`和`info`端点。
修改配置`management.endpoints.web.exposure.include="*"`(*在yaml文件属于关键字，所以需要加引号)。  

## 实用的扩展
### 1. 记录请求日志
参考: [SpringBoot记录HTTP请求日志](https://www.jianshu.com/p/29459bcf6e6a)
需求: 记录每一个http请求的信息，包含请求ip、路径、参数、时间、响应结果、耗时等。

spring-boot-actuator默认会把最近100次的HTTP请求记录到内存中，相关的实现类: 
- `org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository`
- `org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter`
  
通过分析`HttpTraceFilter`源码可知，HttpTrace会影响**响应速度**。并且是基于`synchronized`实现，所以并发量大的情况下，性能越差。

总结:
1. spring-boot-actuator默认是不支持获取`request-body/response-body`。
github-issues: [Unable to add request/response body to HttpTrace]

2. 文章中是直接定义的`public class HttpTraceFilter extends OncePerRequestFilter implements Ordered`，这样其实只是定义了一个新的filter。
如果按重写的思路，更好的是`public class CustomHttpTraceFilter extends HttpTraceFilter`，同时需要注意`HttpTraceRepository`。

### 2. 扩展`/actuator/info`的返回信息
默认只返回空的json对象`{}`，通过`info.*`自定义json。
```yaml
info:
  app:
    name: spring-boot-actuator-test
    version: 1.0.0
    author: vergilyn
```

[spring-boot-actuator cn]: https://docs.spring.io/spring-boot/docs/2.1.1.RELEASE/reference/htmlsingle/#production-ready
[spring-boot-actuator zh]: https://blog.csdn.net/alinyua/article/details/80009435
[Unable to add request/response body to HttpTrace]: https://github.com/spring-projects/spring-boot/issues/12953#issuecomment-383830749
[Introducing Actuator Endpoints in Spring Boot 2.0]: https://spring.io/blog/2017/08/22/introducing-actuator-endpoints-in-spring-boot-2-0
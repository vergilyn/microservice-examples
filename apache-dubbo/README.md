# apache-dubbo
- [Apache Dubbo zh-cn]：dubbo官网（支持中文/英文）
- [Dubbo Docs zh-cn]：官网的中文文档
- [Github Dubbo]：dubbo源码
- [Github dubbo-spring-boot-project]：spring-boot
- [Github dubbo-admin]：UI管理


[Apache Dubbo zh-cn]: http://dubbo.apache.org/zh-cn/
[Dubbo Docs zh-cn]: http://dubbo.apache.org/zh-cn/docs/user/quick-start.html
[Github Dubbo]: https://github.com/apache/dubbo
[Github dubbo-spring-boot-project]: https://github.com/apache/dubbo-spring-boot-project
[Github dubbo-admin]: https://github.com/apache/dubbo-admin

## 特性
1. 面向接口代理的高性能RPC调用  
提供高性能的基于代理的远程调用能力，服务以接口为粒度，为开发者屏蔽远程调用底层细节。

2. 智能负载均衡  
内置多种负载均衡策略，智能感知下游节点健康状况，显著减少调用延迟，提高系统吞吐量。

3. 服务自动注册与发现  
支持多种注册中心服务，服务实例上下线实时感知。

4. 高度可扩展能力  
遵循微内核+插件的设计原则，所有核心能力如Protocol、Transport、Serialization被设计为扩展点，平等对待内置实现和第三方实现。

5. 运行期流量调度  
内置条件、脚本等路由策略，通过配置不同的路由规则，轻松实现灰度发布，同机房优先等功能。

6. 可视化的服务治理与运维  
提供丰富服务治理、运维工具：随时查询服务元数据、服务健康状态及调用统计，实时下发路由策略、调整配置参数。

## 环境

| project       | server.port  | management.server.port | dubbo.protocol.port |
| :--------     | :----------: |:----------------------:| :-----------------: |
| provider      | 901X         | 1901X                  | 10010               |
| consumer      | 902X         | 1902X                  |                     |
| nacos(v1.0.0) | 8848         |                        |                     |

### dubbo-nacos
1) 需要nacos服务
2) maven（provider、consumer的pom.xml）增加nacos的依赖:  
    ```
         <!--region registry-nacos dependencies -->
        <dependency>
            <groupId>org.apache.dubbo</groupId>
            <artifactId>dubbo-registry-nacos</artifactId>
            <version>${apache-dubbo.version}</version>
        </dependency>

        <dependency>
            <groupId>com.alibaba.nacos</groupId>
            <artifactId>nacos-client</artifactId>
            <version>${alibaba-nacos.version}</version>
        </dependency>
        <!--endregion-->
    ```

之后可以在 http://127.0.0.1:8848/nacos 的 `服务管理 - 服务列表`看到注册信息。

## 问题备注
1. dubbo 部分配置貌似暂不支持yaml。

2. issue
 - [issue#590 Comsumer没有默认使用Provider的设置（例如timeout, retires等配置项）](https://github.com/apache/dubbo-spring-boot-project/issues/590)


package com.vergilyn.examples.configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 自定义限流标志的key，多个维度可以从这里入手。
 * exchange对象中获取服务ID、请求信息，用户信息等。
 *
 * <pre> redis-key:
 *   "request_rate_limiter.{KeyResolver}.timestamp": 存储的是当前时间的秒数，也就是System.currentTimeMillis() / 1000或者Instant.now().getEpochSecond()
 *   "request_rate_limiter.{KeyResolver}.tokens": 存储的是当前这秒钟的对应的可用的令牌数量
 * </pre>
 */
@Configuration
public class KeyResolverConfiguration {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        // IP限流
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }

    @Bean
    public KeyResolver tokenKeyResolver() {
        // 用户限流 > 请求路径中必须携带token参数
        return exchange -> Mono.just(exchange.getRequest().getQueryParams().getFirst("token"));
    }

    @Bean
    public KeyResolver pathKeyResolver() {
        // 接口限流 > 获取请求地址的uri作为限流key
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }
}

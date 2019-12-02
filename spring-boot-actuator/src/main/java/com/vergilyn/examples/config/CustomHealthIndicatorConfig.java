package com.vergilyn.examples.config;

import com.vergilyn.examples.actuator.health.CustomHealthIndicator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 2019-11-28：
 *  貌似`2.2.0.RELEASE`不存在{@linkplain RedisHealthIndicatorAutoConfiguration}（2.1.3.RELEASE存在）
 * @date 2019/1/16
 * @see org.springframework.boot.actuate.autoconfigure.redis.RedisHealthIndicatorAutoConfiguration
 */
@Configuration
public class CustomHealthIndicatorConfig {

    @Bean
    @ConditionalOnMissingBean(name = "customHealthIndicator")
    public CustomHealthIndicator customHealthIndicator(){
        return new CustomHealthIndicator("desc...");
    }
}

package com.vergilyn.examples.config;

import com.vergilyn.examples.actuator.health.CustomHealthIndicator;

import org.springframework.boot.actuate.autoconfigure.redis.RedisHealthIndicatorAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @date 2019/1/16
 * @see RedisHealthIndicatorAutoConfiguration
 */
@Configuration
public class CustomHealthIndicatorConfig {

    @Bean
    @ConditionalOnMissingBean(name = "customHealthIndicator")
    public CustomHealthIndicator customHealthIndicator(){
        return new CustomHealthIndicator("desc...");
    }
}

package com.vergilyn.examples.actuator.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 *
 * @date 2019/1/16
 * @see org.springframework.boot.actuate.redis.RedisHealthIndicator
 */
public class CustomHealthIndicator extends AbstractHealthIndicator {
    private String desc;

    public CustomHealthIndicator(String desc) {
        super("Custom health check failed");
        this.desc = desc;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        builder.up().withDetail("custom-health", desc);
    }
}

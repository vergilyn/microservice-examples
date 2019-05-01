package com.vergilyn.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
public class EurekaApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(EurekaApiGatewayApplication.class);
        application.run(args);
    }
}

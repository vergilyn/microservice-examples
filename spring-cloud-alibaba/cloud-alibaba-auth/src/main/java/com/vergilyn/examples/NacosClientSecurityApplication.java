package com.vergilyn.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class NacosClientSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosClientSecurityApplication.class, args);
    }
}

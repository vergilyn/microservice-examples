package com.vergilyn.examples.dubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@SpringBootApplication
public class DubboProviderApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DubboProviderApplication.class);
        application.setAdditionalProfiles("dubbo-nacos");
        application.run(args);
    }
}

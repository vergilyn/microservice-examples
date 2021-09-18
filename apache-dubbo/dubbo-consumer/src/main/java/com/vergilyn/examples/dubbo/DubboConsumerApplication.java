package com.vergilyn.examples.dubbo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@SpringBootApplication
@Slf4j
public class DubboConsumerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DubboConsumerApplication.class);
        application.setAdditionalProfiles("dubbo-nacos");
        application.run(args);
    }

}

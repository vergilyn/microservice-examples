package com.vergilyn.examples.dubbo;

import com.vergilyn.examples.dubbo.service.ProviderService;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@SpringBootApplication
@Slf4j
public class DubboConsumerApplication {

    // @Reference(version = "${demo.service.version}", url = "${demo.service.url}", timeout = 2000)  // dubbo-basic 需要指定url
    @Reference(version = "${demo.service.version}", timeout = 2000)  // dubbo-nacos 不需要指定url
    private ProviderService providerService;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(DubboConsumerApplication.class);
        application.setAdditionalProfiles("dubbo-nacos");
        application.run(args);
    }

    @Bean
    public ApplicationRunner runner() {
        return args -> log.info(providerService.sayHello("vergilyn"));
    }

}

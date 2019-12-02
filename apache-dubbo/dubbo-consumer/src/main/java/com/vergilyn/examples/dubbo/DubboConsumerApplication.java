package com.vergilyn.examples.dubbo;

import com.vergilyn.examples.dubbo.service.ProviderService;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@SpringBootApplication
@Slf4j
public class DubboConsumerApplication implements CommandLineRunner {

    @Reference(version = "${demo.service.version}", url = "${demo.service.url}", timeout = 1200)
    private ProviderService providerService;

    public static void main(String[] args) {
        SpringApplication.run(DubboConsumerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(providerService.sayHello("vergilyn"));
    }
}

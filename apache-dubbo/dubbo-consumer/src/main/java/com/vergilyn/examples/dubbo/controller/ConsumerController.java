package com.vergilyn.examples.dubbo.controller;

import com.vergilyn.examples.dubbo.service.ProviderService;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@RestController
@RequestMapping("/consumer")
@Slf4j
public class ConsumerController {

    @Reference(version = "${demo.service.version}", url = "${demo.service.url}")
    private ProviderService providerService;

    @GetMapping(value = "/say-hello")
    public String sayHello(@RequestParam String name) {
        String resp = providerService.sayHello(name);
        log.info("sayHello() >>>> name: {}, resp: {}", name, resp);
        return resp;
    }
}

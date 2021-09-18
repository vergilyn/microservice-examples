package com.vergilyn.examples.dubbo.service;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
	// @Reference(version = "${demo.service.version}", url = "${demo.service.url}", timeout = 2000)  // dubbo-basic 需要指定url
	@Reference(version = "${demo.service.version}", timeout = 2000, check = false)  // dubbo-nacos 不需要指定url
	private ProviderService providerService;

	public String sayHello(String name){
		return providerService.sayHello(name);
	}
}

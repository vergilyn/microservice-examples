package com.vergilyn.examples.dubbo.service;

import com.vergilyn.examples.dubbo.ProviderConstants;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
	// dubbo-basic 需要指定url
	// @Reference(version = "${demo.service.version}", url = "${demo.service.url}", timeout = 2000)
	// dubbo-nacos 不需要指定url。
	// @Reference(version = ProviderConstants.DUBBO_VERSION, timeout = 2000, check = true)
	@DubboReference(version = ProviderConstants.DUBBO_VERSION, timeout = 2000, check = true)
	private ProviderService providerService;

	public String sayHello(String name){
		return providerService.sayHello(name);
	}
}

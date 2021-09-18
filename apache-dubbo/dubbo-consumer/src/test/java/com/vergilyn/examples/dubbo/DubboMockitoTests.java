package com.vergilyn.examples.dubbo;

import com.vergilyn.examples.dubbo.service.ConsumerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DubboConsumerApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dubbo-nacos")
public class DubboMockitoTests {

	@SpyBean
	private ConsumerService consumerService;
	// @MockBean(name = "com.vergilyn.examples.dubbo.service.ProviderService") // dubbo bean-name 不是 全限定名
	// @MockBean(name = "@Reference(check=false,timeout=2000,version=1.0.0) com.vergilyn.examples.dubbo.service.ProviderService")
	// private ProviderService providerService;

	@Autowired
	private ApplicationContext applicationContext;

	@BeforeEach
	public void beforeEach(){
	}

	@Test
	public void mock(){
		// Mockito.when(providerService.sayHello(ArgumentMatchers.any())).thenReturn("mock-say-hello");

		final String result = consumerService.sayHello("vergilyn");

		System.out.println(result);
	}
}

package com.vergilyn.examples.dubbo;

import java.lang.reflect.Field;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.dubbo.service.ConsumerService;
import com.vergilyn.examples.dubbo.service.ProviderService;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;


/**
 *
 *
 * @author vergilyn
 * @since 2021-09-18
 *
 * @see org.apache.dubbo.config.ConsumerConfig
 */
@SpringBootTest(classes = DubboConsumerApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
		// `injvm`: Whether to find reference's instance from the current JVM.(Deprecated
		// instead, use the parameter scope to judge if it's in jvm, scope=local)
		// `scope`: the scope for referring/exporting a service, if it's local, it means searching in current JVM only.
		"dubbo.consumer.scope = local"
		// `check`: (default true), Check if service provider exists, if not exists, it will be fast fail.
		, "dubbo.consumer.check = false"
		// `lazy`: (default false), Whether to make connection when the client is created。
		, "dubbo.consumer.lazy = false"
})
public class DubboMockitoTests {

	@SpyBean
	private ConsumerService consumerService;
	// dubbo bean-name 默认不是“全限定名”
	// @MockBean(name = "com.vergilyn.examples.dubbo.service.ProviderService")
	// 可能的 dubbo bean-name，很不友好，跟“成员变量”依赖注入的时候声明有关系。
	// @MockBean(name = "@Reference(check=false,timeout=2000,version=1.0.0) com.vergilyn.examples.dubbo.service.ProviderService")
	@MockBean
	private ProviderService providerService;

	@Autowired
	private ApplicationContext applicationContext;

	@BeforeEach
	public void beforeEach(){
		System.out.println(ProviderService.class.getSimpleName() + " bean-names >>>> "
					+ JSON.toJSONString(applicationContext.getBeanNamesForType(ProviderService.class), true));
	}

	@SneakyThrows
	@Test
	public void mock(){
		final Field field = ReflectionUtils.findField(consumerService.getClass(), "providerService");
		field.setAccessible(true);
		field.set(consumerService, providerService);

		Mockito.when(this.providerService.sayHello(ArgumentMatchers.any())).thenReturn("mock-say-hello");

		final String result = consumerService.sayHello("vergilyn");

		System.out.println(result);
	}
}

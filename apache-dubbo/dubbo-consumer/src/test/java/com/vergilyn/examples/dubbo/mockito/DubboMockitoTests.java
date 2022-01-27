package com.vergilyn.examples.dubbo.mockito;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.dubbo.DubboConsumerApplication;
import com.vergilyn.examples.dubbo.ProviderConstants;
import com.vergilyn.examples.dubbo.service.ProviderService;
import lombok.SneakyThrows;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoPostProcessor;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Field;

/**
 * 如果通过 spring-boot-test 友好的 Mock dubbo-reference？
 *
 * SEE: <br/>
 * <p>
 *  <a href="https://github.com/spring-projects/spring-boot/issues/21379">Spring-Boot, Behavior changed of MockBean in spring-boot-2.2.7</a>
 *  ：重点！</p>
 *
 * <p>
 *  <a href=https://github.com/spring-cloud/spring-cloud-openfeign/issues/337">Spring-Cloud, Add FactoryBean.OBJECT_TYPE_ATTRIBUTE to registered beans</a>
 *  ：最终是由spring-cloud解决，版本在`Hoxton.BUILD-20200515.034536-2455`以上（发布版本貌似是  Hoxton.SR5+）</p>
 *
 * <p>
 *  <a href="https://github.com/spring-projects/spring-boot/issues/22229">@MockBean on feign client is not working</a>
 *  ：由于上面一个问题造成 </p>
 *
 *
 * @author vergilyn
 * @since 2021-09-18
 *
 */
@SpringBootTest(classes = DubboConsumerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
		// `injvm`: Whether to find reference's instance from the current JVM.(Deprecated
		// instead, use the parameter scope to judge if it's in jvm, scope=local)
		// `scope`: the scope for referring/exporting a service, if it's local, it means searching in current JVM only.
		"dubbo.consumer.scope = local"
		// `check`: (default true), Check if service provider exists, if not exists, it will be fast fail.
		, "dubbo.consumer.check = false"
		// `lazy`: (default false), Whether to make connection when the client is created。
		// , "dubbo.consumer.lazy = false"
})
// @org.springframework.context.annotation.Import(MockReferenceAnnotationBeanPostProcessor.class)
@org.springframework.context.annotation.Import(DubboReferenceInstantiationAwareBeanPostProcessor.class)
// @org.springframework.context.annotation.Import(DubboReferencePostProcessAfterInitialization.class)
@SuppressWarnings("JavadocReference")
public class DubboMockitoTests {

	/**
	 * {@linkplain SpyBean} 未触发 {@linkplain MockDubboReferenceBeanProcessor#postProcessAfterInitialization(Object, String)}。
	 * 原因：{@linkplain MockitoPostProcessor#registerSpy(ConfigurableListableBeanFactory, BeanDefinitionRegistry, SpyDefinition, Field)} )} 中直接registry-singleton-object
	 */
	@SpyBean
	// @Autowired
	private ConsumerMockitoService consumerMockitoService;

	// ERROR: dubbo bean-name 默认不是“全限定名”
	// @MockBean(name = "com.vergilyn.examples.dubbo.service.ProviderService")
	// 可能的 dubbo bean-name，很不友好，跟“成员变量”依赖注入的时候声明有关系。(依然无法达到mock)
	// @MockBean(name = "@Reference(check=false,timeout=2000,version=1.0.0) com.vergilyn.examples.dubbo.service.ProviderService")
	@MockBean
	private ProviderService providerService;

	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private BeanFactory beanFactory;

	@BeforeEach
	public void beforeEach(){
		System.out.println(ProviderService.class.getSimpleName() + " bean-names >>>> "
					+ JSON.toJSONString(applicationContext.getBeanNamesForType(ProviderService.class), true));
	}

	/**
	 * 2022-01-27 >>>>
	 *   3种方式其实都差不多。
	 *   现在相对推荐 `{@link MockReferenceAnnotationBeanPostProcessor} > {@link DubboReferenceInstantiationAwareBeanPostProcessor}`
	 */
	@SneakyThrows
	@Test
	public void mock(){
		// 能达到mock的效果，只是代码不够友好
		// final Field field = ReflectionUtils.findField(consumerMockitoService.getClass(), "providerService");
		// field.setAccessible(true);
		// field.set(consumerMockitoService, providerService);

		Mockito.when(this.providerService.sayHello(ArgumentMatchers.any())).thenReturn("mock-say-hello");

		final String result = consumerMockitoService.sayHello("vergilyn");

		System.out.println("consumer sayHello >>>> resp: " + result);
	}

	@Service
	public static class ConsumerMockitoService {
		@DubboReference(version = ProviderConstants.DUBBO_VERSION, check = false)
		private ProviderService providerService;

		public String sayHello(String name){
			return providerService.sayHello(name);
		}
	}


}

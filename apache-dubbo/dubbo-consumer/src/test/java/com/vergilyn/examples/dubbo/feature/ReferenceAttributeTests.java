package com.vergilyn.examples.dubbo.feature;

import com.alibaba.spring.util.AnnotationUtils;
import com.vergilyn.examples.dubbo.DubboConsumerApplication;
import com.vergilyn.examples.dubbo.ProviderConstants;
import com.vergilyn.examples.dubbo.service.ProviderService;
import lombok.SneakyThrows;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.PropertyResolver;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * 验证 `dubbo.@Reference`(2.7.0+) 或 `dubbo.@DubboReference`(2.7.7+) 其指定的 `attribute` 与 `dubbo.consumer.*` 的优先级。
 *
 * <p>
 *   以`@DubboReference(check=)` 和 `dubbo.consumer.check` 为例。（
 *   随笔：<a href="https://docs.qq.com/doc/DWHpzblp5RlBRZGdi">[dubbo] 配置优先级</a>）
 * </p>
 *
 * <p>相关代码:
 *   <pre>
 *    入口代码 {@linkplain ReferenceConfig#init()}:
 *    1) {@linkplain ReferenceConfig#check}: 近似{@linkplain DubboReference#check()}
 *    2) `appendParameters(map, consumer)`: `consumer = {@linkplain org.apache.dubbo.config.ConsumerConfig}`，即`dubbo.consumer.*`。
 *    3) 在`2)`之后再`appendParameters(map, this)`: `this = {@linkplain ReferenceConfig}`。所以，`@Reference`属性的优先级高于`dubbo.consumer.*`。
 *    4) {@linkplain ReferenceConfig#createProxy(Map)}
 *    5) {@linkplain ReferenceConfig#shouldCheck()}: `check`核心判断
 *      5.1) 优先使用 {@linkplain ReferenceConfig#check}，如果非NULL，则返回。
 *      5.2) 如果NULL，则用`dubbo.consumer.check`，如果非NULL，则返回。
 *      5.3) 如果还是NULL，则用默认值`true`
 *   </pre>
 * </p>
 *
 * <p>
 *   主要关注 {@linkplain ReferenceConfig#shouldCheck()} 中的判断，以及 {@linkplain ReferenceConfig#check} 的赋值。
 *   <pre>
 *     例如`@Reference()`，期望：`ReferenceConfig#check = null`；`@Reference(check=true)`，期望：`ReferenceConfig#check = true`。
 *     但是，<b>实际情况是最终都是`ReferenceConfig#check = null`，从而使用`dubbo.consumer.check`，导致最终的`check`不满足预期！！！</b>
 *
 *     相关代码：
 *       1) {@linkplain com.alibaba.spring.beans.factory.annotation.AbstractAnnotationBeanPostProcessor#postProcessMergedBeanDefinition(RootBeanDefinition, Class, String)}
 *       2) ...省略
 *       3) {@linkplain AnnotationUtils#getAttributes(Annotation, PropertyResolver, boolean, boolean, boolean, String...)}：核心，如果`attribute-value = default-value`会忽略该属性。
 *  </pre>
 *  所以，即使显式指定`@Reference(check=true)`，最终也会使用`dubbo.consumer.check`。但如果显式指定`@Reference(check=false)`，并不会使用`dubbo.consumer.check`。
 *
 * </p>
 *
 * @author vergilyn
 * @since 2021-09-23
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
		// , "dubbo.consumer.lazy = false"
})
@SuppressWarnings("JavadocReference")
public class ReferenceAttributeTests {

	@SpyBean
	private ConsumerReferencePropertiesService consumerReferencePropertiesService;
	@MockBean
	private ProviderService providerService;

	@Service
	public static class ConsumerReferencePropertiesService {
		@DubboReference(version = ProviderConstants.DUBBO_VERSION, check = true)
		private ProviderService providerService;

		public String sayHello(String name){
			return providerService.sayHello(name);
		}
	}

	@Test
	public void mock(){
		final String result = consumerReferencePropertiesService.sayHello("vergilyn");

		System.out.println("consumer sayHello >>>> resp: " + result);
	}

	@SneakyThrows
	@BeforeEach
	public void beforeEach(){
		final Field field = ReflectionUtils.findField(consumerReferencePropertiesService.getClass(), "providerService");
		field.setAccessible(true);
		field.set(consumerReferencePropertiesService, providerService);

		Mockito.when(this.providerService.sayHello(ArgumentMatchers.any())).thenReturn("mock-say-hello");
	}
}

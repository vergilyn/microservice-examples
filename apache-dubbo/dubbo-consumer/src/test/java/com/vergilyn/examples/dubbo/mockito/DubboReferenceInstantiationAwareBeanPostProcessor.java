package com.vergilyn.examples.dubbo.mockito;

import org.apache.dubbo.config.spring.ReferenceBean;
import org.apache.dubbo.config.spring.beans.factory.annotation.AbstractAnnotationBeanPostProcessor;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.test.mock.mockito.MockitoPostProcessor;

import java.lang.reflect.Field;

/**
 * 在 x-dubbo-reference 注入到 bean-field 前，会先 registry-spring-bean，并通过 spring 创建 dubbo-reference-bean。
 * 在通过 class 创建 dubbo-reference-bean 时，获取`@MockBean` 创建的bean。
 *
 * @author vergilyn
 * @since 2022-01-27
 *
 * @deprecated 2022-01-27, 不如用 {@linkplain MockReferenceAnnotationBeanPostProcessor} 的方式。
 */
@SuppressWarnings("JavadocReference")
public class DubboReferenceInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor,
		BeanFactoryPostProcessor {

	private ConfigurableListableBeanFactory beanFactory;

	/**
	 * 调用链：<br/>
	 * 当xxx-consumer 需要注入依赖时，会调用{@linkplain AbstractAnnotationBeanPostProcessor.AnnotatedInjectElement#inject(Object, String, PropertyValues)}
	 * 通过spring-core-5.2.10 去 create-bean
	 * <pre>
	 * - {@linkplain AbstractBeanFactory#doGetBean(String, Class, Object[], boolean)}  Line: 324
	 * - {@linkplain AbstractAutowireCapableBeanFactory#createBean(String, RootBeanDefinition, Object[])}  Line: 505
	 * - {@linkplain AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation(String, RootBeanDefinition)} 此方法中会调用`postProcessBeforeInstantiation`
	 * </pre>
	 *
	 * 但是执行`postProcessBeforeInstantiation`的前提是`rootBeanDefinition.beforeInstantiationResolved != false`（null/true）。<br/>
	 *
	 * @param beanClass 例如 {@linkplain org.apache.dubbo.config.spring.ReferenceBean}
	 * @param beanName 例如 "providerService"
	 *
	 */
	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		if (ReferenceBean.class != beanClass){
			return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
		}

		// 例如`@DubboReference ProviderService provider;`
		RootBeanDefinition beanDefinition = ((RootBeanDefinition) beanFactory.getBeanDefinition(beanName));
		GenericBeanDefinition genericBeanDefinition = ((GenericBeanDefinition) beanDefinition.getDecoratedDefinition().getBeanDefinition());

		// actualBeanClass -> `com.vergilyn.examples.dubbo.service.ProviderService`
		Class<?> actualBeanClass = genericBeanDefinition.getBeanClass();

		/**
		 * spring-boot-test-2.2.11.RELEASE & spring-core-5.2.10.RELEASE
		 * {@linkplain MockitoPostProcessor#registerMock(ConfigurableListableBeanFactory, BeanDefinitionRegistry, MockDefinition, Field)}
		 *   line:184 `beanFactory.registerSingleton(transformedBeanName, mock-bean);` transformedBeanName = "com.vergilyn.examples.dubbo.service.ProviderService#0"
		 *
		 * No qualifying bean of type 'com.vergilyn.examples.dubbo.service.ProviderService' available:
		 *   expected single matching bean but found 2: com.vergilyn.examples.dubbo.service.ProviderService#0,providerService
		 *
		 * 怎么拿到 已经创建的 mock-bean？
		 * 1) 根据class 拿到所有的 bean-names
		 * 2) 再根据 bean-names 来到所有的beans
		 * 3) 然后 判断bean 是否是 `MockBean / SpyBean`，如果是 则返回。否则走创建逻辑！
		 */
		String[] beanNamesForType = beanFactory.getBeanNamesForType(actualBeanClass);
		for (String existBeanName : beanNamesForType) {
			Object bean = beanFactory.getBean(existBeanName);
			if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)){
				return bean;
			}
		}

		return InstantiationAwareBeanPostProcessor.super.postProcessBeforeInstantiation(beanClass, beanName);
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}

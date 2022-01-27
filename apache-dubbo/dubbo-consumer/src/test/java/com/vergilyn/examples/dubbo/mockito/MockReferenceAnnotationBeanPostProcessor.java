package com.vergilyn.examples.dubbo.mockito;

import org.apache.dubbo.config.spring.beans.factory.annotation.ReferenceAnnotationBeanPostProcessor;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Member;
import java.util.Map;

/**
 * 重写 {@linkplain #registerReferenceBean(String, Class, Map, Member)} 以满足 mock-bean的注入。<br/>
 *
 * 这种方式是在 创建 dubbo-reference-bean 前，所以更推荐是用这种方式！
 *
 * @author vergilyn
 * @since 2022-01-27
 */
public class MockReferenceAnnotationBeanPostProcessor extends ReferenceAnnotationBeanPostProcessor implements
		BeanDefinitionRegistryPostProcessor {

	@Override
	public String registerReferenceBean(String propertyName, Class<?> injectedType, Map<String, Object> attributes,
			Member member) throws BeansException {

		ConfigurableListableBeanFactory beanFactory = getBeanFactory();

		// `MockitoPostProcessor#registerMock(...)` 中已经 registry-mock-bean，此处尝试直接获取被注入consumer-dubbo-reference！
		String[] beanNamesForType = beanFactory.getBeanNamesForType(injectedType);
		for (String beanName : beanNamesForType) {
			Object bean = beanFactory.getBean(beanName);
			if (MockUtil.isMock(bean) || MockUtil.isSpy(bean)){
				return beanName;
			}
		}

		return super.registerReferenceBean(propertyName, injectedType, attributes, member);
	}

	/**
	 * 因为不知道怎么定义 registry-bean-definition 的先后顺序，所以将已有的 remove 再重新 registry！
	 *
	 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar
	 * @see org.apache.dubbo.config.spring.util.DubboBeanUtils#registerCommonBeans(BeanDefinitionRegistry)
	 */
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		String beanName = ReferenceAnnotationBeanPostProcessor.BEAN_NAME;

		if (registry.containsBeanDefinition(beanName)){
			registry.removeBeanDefinition(beanName);
		}

		RootBeanDefinition beanDefinition = new RootBeanDefinition(MockReferenceAnnotationBeanPostProcessor.class);
		beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

		registry.registerBeanDefinition(beanName, beanDefinition);
	}
}

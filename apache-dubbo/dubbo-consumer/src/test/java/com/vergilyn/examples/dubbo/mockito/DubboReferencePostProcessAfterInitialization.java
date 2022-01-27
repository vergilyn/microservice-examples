package com.vergilyn.examples.dubbo.mockito;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @deprecated 2022-01-18，个人而言不太想通过此方式达到目的。因为，
 *   1) 还是会生成很多 actual-dubbo-reference-bean。
 *   2) 需要考虑 多个{@linkplain BeanPostProcessor#postProcessAfterInitialization(Object, String)} 的执行顺序，以确保`field.set(...)`是正确的。
 *
 * @see <a href="https://github.com/apache/dubbo/issues/9116#issuecomment-952469354"></a>
 */
public class DubboReferencePostProcessAfterInitialization implements BeanPostProcessor, BeanFactoryPostProcessor, Ordered {

	private ConfigurableListableBeanFactory beanFactory;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * @see AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		List<Field> annotatedFields = Lists.newArrayList();

		// dubbo-reference 相关注解
		annotatedFields.addAll(FieldUtils.getFieldsListWithAnnotation(bean.getClass(), org.apache.dubbo.config.annotation.Reference.class));
		annotatedFields.addAll(FieldUtils.getFieldsListWithAnnotation(bean.getClass(), com.alibaba.dubbo.config.annotation.Reference.class));
		annotatedFields.addAll(FieldUtils.getFieldsListWithAnnotation(bean.getClass(), DubboReference.class));

		for (Field field : annotatedFields) {
			try {
				Class<?> type = field.getType();
				String mockBeanName = BeanFactoryUtils.transformedBeanName(type.getSimpleName());
				Object fieldValue = null;
				if (!beanFactory.containsBean(mockBeanName)) {
					fieldValue = Mockito.mock(type);
					beanFactory.registerSingleton(mockBeanName, fieldValue);
				}else {
					// FIXME 2022-01-17 “expected single matching bean but found X:...”
					//   因为 dubbo 对相同 class可能生成多个 bean-object (例如不同`version`的x-provider)
					fieldValue = beanFactory.getBean(type);
				}

				ReflectionUtils.makeAccessible(field);
				field.set(bean, fieldValue);
			} catch (IllegalAccessException e) {
			}
		}
		return bean;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
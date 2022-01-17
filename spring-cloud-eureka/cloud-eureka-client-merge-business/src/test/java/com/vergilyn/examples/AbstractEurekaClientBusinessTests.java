package com.vergilyn.examples;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * @author vergilyn
 * @since 2021-09-15
 */
@SpringBootTest(classes = EurekaClientBusinessApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractEurekaClientBusinessTests {

	@Autowired
	protected ApplicationContext applicationContext;

	@Autowired
	protected BeanFactory beanFactory;
}

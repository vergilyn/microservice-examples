package com.vergilyn.examples;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author vergilyn
 * @since 2021-09-15
 */
@SpringBootTest(classes = EurekaClientBusinessApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractEurekaClientBusinessTests {
}

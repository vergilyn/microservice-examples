package com.vergilyn.examples.service;

import com.vergilyn.examples.AbstractEurekaClientBusinessTests;
import com.vergilyn.examples.dto.BusinessDTO;
import com.vergilyn.examples.feign.OrderFeignClient;
import com.vergilyn.examples.feign.StorageFeignClient;
import com.vergilyn.examples.response.ObjectResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

public class BusinessServiceTests extends AbstractEurekaClientBusinessTests {

	@SpyBean
	private BusinessService businessService;

	/**
	 * "spring-boot:2.2.6 & spring-cloud:Hoxton.RELEASE" 不需要{@linkplain MockBean#name()}；
	 * "spring-boot:2.2.7+ & spring-cloud:Hoxton.SR12"
	 *
	 * <p>参考：<br/>
	 * <a href="https://github.com/spring-projects/spring-boot/issues/21379">Spring-Boot, Behavior changed of MockBean in spring-boot-2.2.7</a>
	 * <br/>
	 * <a href="https://github.com/spring-cloud/spring-cloud-openfeign/issues/337">Spring-Cloud, Add FactoryBean.OBJECT_TYPE_ATTRIBUTE to registered beans</a>：最终是由spring-cloud解决，版本在`Hoxton.BUILD-20200515.034536-2455`以上
	 */
	// @MockBean(name = "com.vergilyn.examples.feign.StorageFeignClient")
	@MockBean
	private StorageFeignClient storageFeignClient;
	@MockBean
	private OrderFeignClient orderFeignClient;

	@BeforeEach
	public void beforeEach(){
		// spring-boot-test 不需要
		// MockitoAnnotations.openMocks(this);
	}

	@Test
	public void mock(){
		ObjectResponse<Void> storageResp = ObjectResponse.success();
		storageResp.setMessage("mock-response");
		Mockito.when(storageFeignClient.decrease(ArgumentMatchers.any()))
				.thenReturn(storageResp);

		businessService.handleBusiness(new BusinessDTO());
	}

}

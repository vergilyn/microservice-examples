package com.vergilyn.examples.feign;

import com.vergilyn.examples.constants.EurekaConstants;
import com.vergilyn.examples.dto.OrderDTO;
import com.vergilyn.examples.response.ObjectResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = EurekaConstants.APPLICATION_ORDER)
public interface OrderFeignClient {

    @RequestMapping(path = "/order/create", method = RequestMethod.POST)
    ObjectResponse<OrderDTO> create(@RequestBody OrderDTO orderDTO);
}

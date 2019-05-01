package com.vergilyn.examples.feign;

import com.vergilyn.examples.constants.EurekaConstants;
import com.vergilyn.examples.dto.AccountDTO;
import com.vergilyn.examples.response.ObjectResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = EurekaConstants.APPLICATION_ACCOUNT)
public interface AccountFeignClient {

    @RequestMapping(path = "/account/decrease", method = RequestMethod.POST)
    ObjectResponse<Void> decrease(@RequestBody AccountDTO accountDTO);
}
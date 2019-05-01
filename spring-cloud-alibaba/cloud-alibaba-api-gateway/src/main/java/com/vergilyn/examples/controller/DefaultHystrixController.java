package com.vergilyn.examples.controller;

import com.vergilyn.examples.enums.RspStatusEnum;
import com.vergilyn.examples.response.ObjectResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultHystrixController {

    @RequestMapping("/default-fallback")
    public ObjectResponse<Void> fallback(){
        return ObjectResponse.failure(RspStatusEnum.GATEWAY_DEFAULT_HYSTRIX);
    }
}

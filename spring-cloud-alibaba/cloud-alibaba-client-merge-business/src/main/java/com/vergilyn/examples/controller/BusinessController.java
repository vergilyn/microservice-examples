package com.vergilyn.examples.controller;

import com.vergilyn.examples.dto.BusinessDTO;
import com.vergilyn.examples.dto.OrderDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.BusinessService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@Slf4j
public class BusinessController {
    @Autowired
    private BusinessService businessService;

    @Value("${server.port}")
    private String port;
    @Value("${vergilyn.custom.hystrix-timeout}")
    private Long hystrixTimeout;

    /**
     * 模拟用户购买商品下单业务逻辑流程
     * @Param:
     * @Return:
     */
    @PostMapping(value = "/buy")
    public ObjectResponse<OrderDTO> handleBusiness(@RequestBody BusinessDTO businessDTO){
        log.info("请求参数：{}", businessDTO.toString());
        return businessService.handleBusiness(businessDTO);
    }

    @RequestMapping(path = "hystrix")
    public ObjectResponse<StorageDTO> hystrix(String code, Long millis){
        long beginTime = System.currentTimeMillis();
        ObjectResponse<StorageDTO> response = businessService.get(code, millis);

        String desc = " >>>> hystrixTimeout: %s, exec-time: %s";
        response.setMessage(response.getMessage() + String.format(desc, hystrixTimeout, System.currentTimeMillis() - beginTime));

        return response;
    }

    @RequestMapping(path = "ribbon")
    public String ribbon(){
        return "server.port: " + port;
    }
}

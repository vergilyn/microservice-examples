package com.vergilyn.examples.service.impl;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.dto.BusinessDTO;
import com.vergilyn.examples.dto.CommodityDTO;
import com.vergilyn.examples.dto.OrderDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.enums.RspStatusEnum;
import com.vergilyn.examples.exception.DefaultException;
import com.vergilyn.examples.feign.OrderFeignClient;
import com.vergilyn.examples.feign.StorageFeignClient;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.BusinessService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BusinessServiceImpl implements BusinessService {
    @Autowired
    private StorageFeignClient storageFeignClient;
    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    public ObjectResponse<OrderDTO> handleBusiness(BusinessDTO businessDTO) {
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setTotal(businessDTO.getTotal());
        ObjectResponse storageResponse = storageFeignClient.decrease(commodityDTO);
        log.info("storage >>>> request: {}, response: {}", JSON.toJSONString(commodityDTO), JSON.toJSONString(storageResponse));

        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderTotal(businessDTO.getTotal());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        ObjectResponse<OrderDTO> response = orderFeignClient.create(orderDTO);
        log.info("order >>>> request: {}, response: {}", JSON.toJSONString(orderDTO), JSON.toJSONString(response));

        if (storageResponse.getStatus() != 200 || response.getStatus() != 200) {
            throw new DefaultException(RspStatusEnum.FAIL);
        }

        return ObjectResponse.success(response.getData());
    }

    @Override
    public ObjectResponse<StorageDTO> get(String code, Long millis) {
        return storageFeignClient.get(code, millis);
    }

}

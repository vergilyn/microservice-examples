package com.vergilyn.examples.service.impl;

import java.util.Optional;

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
//    @GlobalTransactional(timeoutMills = 10000, name = NacosConstant.TX_SERVICE_GROUP + "-business")
    public ObjectResponse handleBusiness(BusinessDTO businessDTO) {
//        System.out.println("开始全局事务，XID = " + RootContext.getXID());
        //1、扣减库存
        CommodityDTO commodityDTO = new CommodityDTO();
        commodityDTO.setCommodityCode(businessDTO.getCommodityCode());
        commodityDTO.setTotal(businessDTO.getTotal());

        // System.out.printf("storage-%d >>>> %s", thread.get(), storageFeignService.get(commodityDTO.getCommodityCode()).getData());

        ObjectResponse storageResponse = storageFeignClient.decrease(commodityDTO);

        //2、创建订单
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(businessDTO.getUserId());
        orderDTO.setCommodityCode(businessDTO.getCommodityCode());
        orderDTO.setOrderTotal(businessDTO.getTotal());
        orderDTO.setOrderAmount(businessDTO.getAmount());
        ObjectResponse<OrderDTO> response = orderFeignClient.create(orderDTO);

        // true: 测试事务发生异常后，全局回滚功能
        if (Optional.ofNullable(businessDTO.getRollback()).orElse(Boolean.FALSE)) {
            throw new RuntimeException("测试抛异常后，分布式事务回滚！");
        }

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

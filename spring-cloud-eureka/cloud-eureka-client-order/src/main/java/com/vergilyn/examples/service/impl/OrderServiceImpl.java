package com.vergilyn.examples.service.impl;

import java.util.UUID;

import javax.transaction.Transactional;

import com.vergilyn.examples.dto.AccountDTO;
import com.vergilyn.examples.dto.OrderDTO;
import com.vergilyn.examples.entity.Order;
import com.vergilyn.examples.enums.RspStatusEnum;
import com.vergilyn.examples.feign.AccountFeignClient;
import com.vergilyn.examples.repository.OrderRepository;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.OrderService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private AccountFeignClient accountFeignClient;

    @Override
    @Transactional
    public ObjectResponse<OrderDTO> create(OrderDTO orderDTO) {

        ObjectResponse<OrderDTO> response = new ObjectResponse<>();
        //扣减用户账户
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setUserId(orderDTO.getUserId());
        accountDTO.setAmount(orderDTO.getOrderAmount());
        ObjectResponse objectResponse = accountFeignClient.decrease(accountDTO);

        //生成订单号
        orderDTO.setOrderNo(UUID.randomUUID().toString().replace("-",""));
        //生成订单
        Order order = new Order();
        BeanUtils.copyProperties(orderDTO, order);
        order.setTotal(orderDTO.getOrderTotal());
        order.setAmount(orderDTO.getOrderAmount().doubleValue());
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            return response.result(RspStatusEnum.FAIL);
        }

        if (objectResponse.getStatus() != 200) {
            return response.result(RspStatusEnum.FAIL);
        }

        return response.result(RspStatusEnum.SUCCESS);
    }
}

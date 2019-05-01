package com.vergilyn.examples.service;

import com.vergilyn.examples.dto.CommodityDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.response.ObjectResponse;

public interface StorageService {

    /**
     * 扣减库存
     */
    ObjectResponse<Void> decrease(CommodityDTO commodityDTO);

    /**
     * 获取库存
     */
    ObjectResponse<StorageDTO> getByCommodityCode(String code, Long millis);
}

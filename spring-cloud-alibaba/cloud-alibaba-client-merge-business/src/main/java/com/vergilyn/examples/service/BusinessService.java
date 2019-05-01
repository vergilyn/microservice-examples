package com.vergilyn.examples.service;

import com.vergilyn.examples.dto.BusinessDTO;
import com.vergilyn.examples.dto.OrderDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.response.ObjectResponse;

public interface BusinessService {

    ObjectResponse<OrderDTO> handleBusiness(BusinessDTO businessDTO);

    ObjectResponse<StorageDTO> get(String code, Long millis);
}

package com.vergilyn.examples.service.impl;

import javax.transaction.Transactional;

import com.vergilyn.examples.dto.CommodityDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.entity.Storage;
import com.vergilyn.examples.repository.StorageRepository;
import com.vergilyn.examples.response.ObjectResponse;
import com.vergilyn.examples.service.StorageService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    private StorageRepository storageRepository;

    @Override
    @Transactional
    public ObjectResponse<Void> decrease(CommodityDTO commodityDTO) {

        int storage = storageRepository.decreaseStorage(commodityDTO.getCommodityCode(), commodityDTO.getTotal());

        return storage > 0 ? ObjectResponse.success() : ObjectResponse.failure();
    }

    @Override
    public ObjectResponse<StorageDTO> getByCommodityCode(String code, Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }

        Storage storage = storageRepository.getFirstByCommodityCode(code);
        StorageDTO dto = new StorageDTO();
        BeanUtils.copyProperties(storage, dto);
        return ObjectResponse.success(dto);
    }
}

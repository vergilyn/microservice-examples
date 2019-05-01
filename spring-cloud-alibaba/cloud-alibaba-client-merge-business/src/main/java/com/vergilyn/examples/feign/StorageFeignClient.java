package com.vergilyn.examples.feign;

import com.vergilyn.examples.constants.NacosConstant;
import com.vergilyn.examples.dto.CommodityDTO;
import com.vergilyn.examples.dto.StorageDTO;
import com.vergilyn.examples.enums.RspStatusEnum;
import com.vergilyn.examples.response.ObjectResponse;

import feign.hystrix.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = NacosConstant.APPLICATION_STORAGE, fallbackFactory = StorageFeignClientHystrix.class)
public interface StorageFeignClient {

    @RequestMapping(path = "/storage/decrease", method = RequestMethod.POST)
    ObjectResponse<Void> decrease(@RequestBody CommodityDTO commodityDTO);

    @RequestMapping(path = "/storage/get", method = RequestMethod.GET)
    ObjectResponse<StorageDTO> get(@RequestParam("code") String code, @RequestParam("millis") Long millis);
}

@Component
class StorageFeignClientHystrix implements FallbackFactory<StorageFeignClient> {
    @Override
    public StorageFeignClient create(Throwable cause) {
        return new StorageFeignClient() {
            @Override
            public ObjectResponse<Void> decrease(CommodityDTO commodityDTO) {
                return null;
            }

            @Override
            public ObjectResponse<StorageDTO> get(String code, Long millis) {
                ObjectResponse<StorageDTO> response = new ObjectResponse<>();
                response.result(RspStatusEnum.HYSTRIX);
                response.setMessage(response.getMessage() + " error: " + cause.getMessage());
                return response;
            }
        };
    }

}
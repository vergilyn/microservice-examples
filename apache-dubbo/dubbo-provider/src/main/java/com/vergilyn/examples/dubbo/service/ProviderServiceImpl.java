package com.vergilyn.examples.dubbo.service;

import com.vergilyn.examples.dubbo.ProviderConstants;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author VergiLyn
 * @date 2019-11-28
 */
@Service(version = ProviderConstants.DUBBO_VERSION)
public class ProviderServiceImpl implements ProviderService {

    /**
     * The default value of ${dubbo.application.name} is ${spring.application.name}
     */
    @Value("${dubbo.application.name}")
    private String serviceName;

    @Override
    public String sayHello(String name) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

        }
        return String.format("[%s] >>>>>>>> Hello, %s", serviceName, name);
    }
}
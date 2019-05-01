package com.vergilyn.examples;

import com.vergilyn.examples.config.CustomHttpTraceConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.CUSTOM, classes = {TypeExcludeFilter.class, AutoConfigurationExcludeFilter.class}),
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CustomHttpTraceConfig.class)
})
public class ActuatorApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ActuatorApplication.class);
        application.run(args);
    }
}

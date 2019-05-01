package com.vergilyn.examples.config;

import com.vergilyn.examples.actuator.trace.CustomHttpTraceRepository;
import com.vergilyn.examples.actuator.trace.CustomHttpTraceFilter;

import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomHttpTraceConfig {

    @Bean
    public HttpTraceRepository traceRepository(){
        return new CustomHttpTraceRepository();
    }

    @Bean
    public HttpTraceFilter httpTraceLogFilter(HttpTraceRepository repository, HttpExchangeTracer tracer) {
        return new CustomHttpTraceFilter(repository, tracer);
    }

    /**
     * 禁用/启用 HttpTrace（或者management.endpoint.httptrace.enabled = true/false）
     */
    @Bean
    public FilterRegistrationBean registration(HttpTraceFilter httpTraceFilter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(httpTraceFilter);
        registration.setEnabled(true);
        return registration;
    }
}

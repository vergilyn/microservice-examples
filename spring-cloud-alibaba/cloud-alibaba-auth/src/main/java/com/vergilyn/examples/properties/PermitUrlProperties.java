package com.vergilyn.examples.properties;


import java.util.List;

import com.google.common.collect.Lists;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.oauth2")
public class PermitUrlProperties {

    /**
     * 监控中心和swagger需要访问的url
     */
    private static final String[] ENDPOINTS = {"/**/actuator/**", "/**/actuator/**/**",  //断点监控
            "/**/v2/api-docs/**", "/**/swagger-ui.html", "/**/swagger-resources/**", "/**/webjars/**", //swagger
            "/**/turbine.stream", "/**/turbine.stream**/**", "/**/hystrix", "/**/hystrix.stream", "/**/hystrix/**", "/**/hystrix/**/**", "/**/proxy.stream/**", //熔断监控
            "/**/druid/**", "/**/favicon.ico", "/**/prometheus"};

    private List<String> ignores;

    /**
     * 需要放开权限的url
     */
    public String[] getIgnored() {
        if (ignores == null || ignores.isEmpty()) {
            return ENDPOINTS;
        }

        List<String> list = Lists.newArrayList(ENDPOINTS);
        list.addAll(ignores);

        return list.toArray(new String[0]);
    }

    public void setIgnored(List<String> ignores) {
        this.ignores = ignores;
    }

}

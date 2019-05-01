package com.vergilyn.examples;

public class BusinessUrl {
    private static final String URL_BUSINESS_BUY = "%s/business/buy";
    private static final String URL_BUSINESS_RIBBON = "%s/business/ribbon";
    private static final String URL_BUSINESS_HYSTRIX = "%s/business/hystrix?code=C201901140001&millis=%s";
    private static final String URL_GATEWAY_FALLBACK = "%s/business/unknown";

    private String hostname;

    public BusinessUrl(String hostname) {
        this.hostname = hostname;
    }

    public String buy(){
        return String.format(URL_BUSINESS_BUY, hostname);
    }

    public String ribbon(){
        return String.format(URL_BUSINESS_RIBBON, hostname);
    }

    public String hystrix(Long millis){
        return String.format(URL_BUSINESS_HYSTRIX, hostname, millis);
    }

    public String gatewayFallback(){
        return String.format(URL_GATEWAY_FALLBACK, hostname);
    }
}

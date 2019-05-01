package com.vergilyn.examples;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @date 2019/3/11
 */
public class RequestRateTest {
    private HttpClient httpClient;

    @BeforeTest
    public void  before(){
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(10)
                .setMaxConnTotal(10)
                .build();
    }

    /* 因为 burstCapacity = 3, replenishRate = 1；
     * (期望现象) 3个请求返回200，2个请求返回429。
     */
    @Test(invocationCount = 5, threadPoolSize = 5)
    public void test(){
        HttpGet httpGet = new HttpGet("http://127.0.0.1:8765/api/producer/gateway");
        try {
            HttpResponse execute = httpClient.execute(httpGet);
            Assert.assertEquals(execute.getStatusLine().getStatusCode(), 200);
            execute.getEntity().getContent().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

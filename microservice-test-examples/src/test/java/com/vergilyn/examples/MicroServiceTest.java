package com.vergilyn.examples;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.alibaba.fastjson.JSON;
import com.vergilyn.examples.dto.BusinessDTO;

import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class MicroServiceTest {
    private static final String URL_GATEWAY_EUREKA = "http://127.0.0.1:8765/api";
    private static final String URL_GATEWAY_ALIBABA = "http://127.0.0.1:9000/api";
    private HttpClient httpClient;
    private BusinessUrl businessUrl;

    @BeforeTest
    public void before(){
        httpClient = HttpClients.custom()
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(40)
                .build();

       // businessUrl = new BusinessUrl(URL_GATEWAY_EUREKA);
        businessUrl = new BusinessUrl(URL_GATEWAY_ALIBABA);
    }

    @Test
    public void businessBuy() {
        HttpPost post = new HttpPost(businessUrl.buy());
        BusinessDTO param = new BusinessDTO();
        param.setUserId("1");
        param.setCommodityCode("C201901140001");
        param.setName("test-" + RandomUtils.nextInt(1, 10));
        param.setTotal(40);
        param.setAmount(new BigDecimal("400.00"));

        post.addHeader("Content-type","application/json; charset=utf-8");
        post.setHeader("Accept", "application/json");
        post.setEntity(new StringEntity(JSON.toJSONString(param), Charset.forName("UTF-8")));

        try {
            HttpResponse response = httpClient.execute(post);
            String str = EntityUtils.toString(response.getEntity());
            System.out.println(businessUrl.buy() + " >>>> " +str );
            // Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(description = "测试负载均衡, ribbon默认是轮询，所以是xxxx/xxxx交替访问")
    public void ribbon() throws ExecutionException, InterruptedException {
        final int count = 10;
        ExecutorService exs = Executors.newFixedThreadPool(count);

        List<Future<String>> futureList = Lists.newArrayList(count);
        for(int i = 0; i < count; i++){
            futureList.add(exs.submit(() -> {
                HttpGet httpGet = new HttpGet(businessUrl.ribbon());
                return EntityUtils.toString(httpClient.execute(httpGet).getEntity());
            }));
        }

        StringBuilder response = new StringBuilder();
        for(int index = 1; index <= count; index++){
            response.append(" >>>> ")
                    .append(index).append(": ").append(futureList.get(index - 1).get())
                    .append("\n");
        }

        System.out.println(response);

        // Assert.assertThat(response.toString(), allOf(containsString("8080"), containsString("8081")));
    }

    @Test(description = "测试服务降级")
    public void hystrix() throws IOException {
        // api-gateway: hystrix.timeoutInMilliseconds=4000ms
        // business: hystrix.timeoutInMilliseconds=2000ms

        // 正常
        HttpGet normalGet = new HttpGet(businessUrl.hystrix(200L));
        String normal = EntityUtils.toString(httpClient.execute(normalGet).getEntity());

        HttpGet hystrixGet = new HttpGet(businessUrl.hystrix(2000L));
        String hystrix = EntityUtils.toString(httpClient.execute(hystrixGet).getEntity());

        System.out.println("normal >>>> " + normal);

        System.out.println("hystrix >>>> " + hystrix);
    }

    @Test(description = "访问一个不存在的path，并不会触发gateway的default-hystrix")
    public void unknownPath() throws IOException {
        HttpGet httpGet = new HttpGet(businessUrl.gatewayFallback());
        String unknown = EntityUtils.toString(httpClient.execute(httpGet).getEntity());

        System.out.println("gateway-fallback >>>> " + httpGet.getURI().getPath() + ", " + unknown);

    }

    @Test(description = "测试api-gateway默认降级处理")
    public void gatewayFallback() throws IOException {

    }

    @Test(description = "测试限流")
    public void requestRateLimiter() throws ExecutionException, InterruptedException {
        final int count = 20;
        ExecutorService exs = Executors.newFixedThreadPool(count);

        List<Future<String>> futureList = Lists.newArrayList(count);
        for(int i = 0; i < count; i++){
            futureList.add(exs.submit(() -> {
                HttpGet httpGet = new HttpGet(businessUrl.ribbon());
                RequestConfig config = RequestConfig.custom()
                        .setConnectTimeout(1000)
                        .setConnectionRequestTimeout(1000)
                        .setConnectionRequestTimeout(100000)
                        .build();
                httpGet.setConfig(config);
                return httpClient.execute(httpGet).getStatusLine().getStatusCode() + "";
            }));
        }

        StringBuilder response = new StringBuilder();
        for(int index = 1; index <= count; index++){
            response.append(" >>>> ")
                    .append(index).append(": ").append(futureList.get(index - 1).get())
                    .append("\n");
        }

        System.out.println(response);

        // Assert.assertThat(response.toString(), allOf(containsString("200"), containsString("429")));
    }
}

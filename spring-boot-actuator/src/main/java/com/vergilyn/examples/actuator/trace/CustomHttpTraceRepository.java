package com.vergilyn.examples.actuator.trace;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

/**
 * 自定义{@link HttpTraceRepository}。
 * <p>缺点：
 *   {@link HttpTrace}中无法获取`POST`中的参数(request-body)，和response-body。
 *   github-issues: <a href="https://github.com/spring-projects/spring-boot/issues/12953#issuecomment-383830749">Unable to add request/response body to HttpTrace </a>
 * </p>
 * @see org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
 */
public class CustomHttpTraceRepository implements HttpTraceRepository {

    /* http://127.0.0.1:xxxx/actuator/httptrace 查询*/
    @Override
    public List<HttpTrace> findAll() {
        return Collections.emptyList();
    }

    @Override
    public void add(HttpTrace trace) {
        System.out.println(JSON.toJSONString(trace, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteMapNullValue));
    }
}

package com.vergilyn.examples.actuator.trace;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;

import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.autoconfigure.trace.http.HttpTraceAutoConfiguration;
import org.springframework.boot.actuate.trace.http.HttpExchangeTracer;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.boot.actuate.web.trace.servlet.HttpTraceFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * <a href="https://www.jianshu.com/p/29459bcf6e6a">SpringBoot记录HTTP请求日志</a>
 * <p>备注:
 *   通过{@link HttpTraceAutoConfiguration}可知，不应该是`public class CustomHttpTraceFilter extends OncePerRequestFilter implements Ordered`。
 *   这样其实会同时执行{@link HttpTraceFilter}、{@link CustomHttpTraceFilter}。</br>
 *   如果是单独写过滤器，那么可以这样定义。
 * </p>
 * @see HttpTraceFilter
 * @see HttpTraceAutoConfiguration
 */
public class CustomHttpTraceFilter extends HttpTraceFilter {

    private static final String IGNORE_CONTENT_TYPE = "multipart/form-data";

    public CustomHttpTraceFilter(HttpTraceRepository repository, HttpExchangeTracer tracer) {
        super(repository, tracer);
    }

    /* 如果是extends HttpTraceFilter，可以照搬HttpTraceFilter.doFilterInternal实现，
     * 在其中引入自己的需求代码，比如将自己需求的`HttpTraceLog`写入队列。
     * 如果是自己实现filter来记录请求日记，且为了性能需要禁用HttpTraceFilter，
     * 可以参考: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto-disable-registration-of-a-servlet-or-filter
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!isRequestValid(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        request = request instanceof ContentCachingRequestWrapper ? request : new ContentCachingRequestWrapper(request);
        response = response instanceof ContentCachingResponseWrapper ? response : new ContentCachingResponseWrapper(response);

        int status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();
        } finally {
            String path = request.getRequestURI();
            if (!Objects.equals(IGNORE_CONTENT_TYPE, request.getContentType())) {

                /*
                 注意：
                    request-body里字符的传输是通过HttpServletRequest中的字节流getInputStream()获得的；而这个字节流在读取了一次之后就不复存在了。

                 解决方法：
                    利用ContentCachingRequestWrapper对HttpServletRequest的请求包一层，该类会将inputstream中的copy一份到自己的字节数组中，这样就不会报错了。
                    读取完body后，需要调用`wrappedResponse.copyBodyToResponse()`。
                */
                // String requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);

                //1. 记录日志
                HttpTraceLog traceLog = new HttpTraceLog();
                traceLog.setPath(path);
                traceLog.setMethod(request.getMethod());
                long latency = System.currentTimeMillis() - startTime;
                traceLog.setTimeTaken(latency);
                traceLog.setTime(LocalDateTime.now().toString());
                traceLog.setParameterMap(JSON.toJSONString(request.getParameterMap()));
                traceLog.setStatus(response.getStatus());
                traceLog.setRequestBody(getRequestBody(request));
                traceLog.setResponseBody(getResponseBody(response));

                System.out.println("Http trace log: " + JSON.toJSONString(traceLog));
            }
            updateResponse(response);
        }
    }

    private boolean isRequestValid(HttpServletRequest request) {
        try {
            new URI(request.getRequestURL().toString());
            return true;
        } catch (URISyntaxException ex) {
            return false;
        }
    }

    private String getRequestBody(HttpServletRequest request) {
        String requestBody = "";
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
        if (wrapper != null) {
            try {
                requestBody = IOUtils.toString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            } catch (IOException e) {
                // NOOP
            }
        }
        return requestBody;
    }

    private String getResponseBody(HttpServletResponse response) {
        String responseBody = "";
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        if (wrapper != null) {
            try {
                responseBody = IOUtils.toString(wrapper.getContentAsByteArray(), wrapper.getCharacterEncoding());
            } catch (IOException e) {
                // NOOP
            }
        }
        return responseBody;
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper responseWrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        Objects.requireNonNull(responseWrapper).copyBodyToResponse();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return super.shouldNotFilter(request);
    }

    @Data
    private static class HttpTraceLog {
        private String path;
        private String parameterMap;
        private String method;
        private Long timeTaken;
        private String time;
        private Integer status;
        private String requestBody;
        private String responseBody;
    }

}

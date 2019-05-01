package com.vergilyn.examples.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RequestMapping("/hello")
    public String index(){
        redisTemplate.opsForValue().set("key-01", "value-01");
        return "hello world";
    }

}

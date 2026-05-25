package com.exemple.activity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/redis-test")
    public String redisTest(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            return "Erro Redis: " + e.getMessage();
        }
    }

}

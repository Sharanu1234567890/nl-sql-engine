package com.nlsql.nl_sql_engine.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    public void save(String question, Object result) {
        String key = "nlsql:" + question.hashCode();
        redisTemplate.opsForValue().set(key, result, TTL);
    }

    public Object get(String question) {
        String key = "nlsql:" + question.hashCode();
        return redisTemplate.opsForValue().get(key);
    }
}

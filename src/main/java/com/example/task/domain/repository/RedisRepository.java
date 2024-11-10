package com.example.task.domain.repository;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * key-value 저장 메소드
     *
     * @param key 저장하려는 key 값
     * @param value 저장하려는 value 값
     */
    public void save(String key, String value){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    /**
     * key value 조회 메소드
     *
     * @param key 조회 하려는 key 값
     * @return key 에 해당하는 value 값
     */
    public String getValue(String key){
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    /**
     * 만료 시간 지정 메소드
     *
     * @param key 대상 key
     * @param time 만료 기간 (초단위)
     */
    public void setExpire(String key, Long time){
        redisTemplate.expire(key,time, TimeUnit.SECONDS);
    }

    public Long getTimeToLive(String key){
        return redisTemplate.getExpire(key);
    }
}

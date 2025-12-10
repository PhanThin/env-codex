package vn.com.viettel.redis.services;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisService {
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedisTemplate<Object, Object> redisJSONTemplate;

    public void put(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public void put(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void putHash(String key, Object field, Object value) {
        stringRedisTemplate.opsForHash().put(key, field, value);
    }

    public void putObject(Object key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void putObject(Object key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void putJSONObject(Object key, Object value) {
        redisJSONTemplate.opsForValue().set(key, value);
    }

    public void putJSONObject(Object key, Object value, long timeout, TimeUnit unit) {
        redisJSONTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public String getAndDelete(String key) {
        return stringRedisTemplate.opsForValue().getAndDelete(key);
    }

    public Object getHash(String key, Object field) {
        return stringRedisTemplate.opsForHash().get(key, field);
    }

    public Object getObject(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Object getAndDeleteObject(Object key) {
        return redisTemplate.opsForValue().getAndDelete(key);
    }

    public Object getJSONObject(Object key) {
        return redisJSONTemplate.opsForValue().get(key);
    }
}

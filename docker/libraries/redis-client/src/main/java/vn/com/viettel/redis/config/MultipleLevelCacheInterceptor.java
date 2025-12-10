package vn.com.viettel.redis.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;
import org.springframework.data.redis.cache.RedisCache;

public class MultipleLevelCacheInterceptor extends CacheInterceptor {

    private final CacheManager caffeineCacheManager;

    public MultipleLevelCacheInterceptor(CacheManager caffeineCacheManager) {
        this.caffeineCacheManager = caffeineCacheManager;
    }

    @Override
    protected Cache.ValueWrapper doGet(Cache cache, Object key) {
        Cache.ValueWrapper existingCacheValue = super.doGet(cache, key);

        if (existingCacheValue != null && isRedisCache(cache)) {
            Cache caffeineCache = caffeineCacheManager.getCache(cache.getName());
            if (caffeineCache != null) {
                caffeineCache.putIfAbsent(key, existingCacheValue.get());
            }
        }

        return existingCacheValue;
    }

    private boolean isRedisCache(Cache cache) {
        if (cache.getClass() == RedisCache.class) return true;

        if (cache instanceof TransactionAwareCacheDecorator &&
                ((TransactionAwareCacheDecorator) cache).getTargetCache().getClass() == RedisCache.class) return true;

        return false;
    }
}

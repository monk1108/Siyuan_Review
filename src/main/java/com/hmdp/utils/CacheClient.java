package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.hmdp.utils.RedisConstants.CACHE_NULL_TTL;
import static com.hmdp.utils.RedisConstants.LOCK_SHOP_KEY;

@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

//      set data with logical expiration
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // write into redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

//    cache passthrough
//    can return any type, set generics R
    public <R,ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit){
        String key = keyPrefix + id;
        // 1. search shop cache from redis
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. see whether the cache exists
        if (StrUtil.isNotBlank(json)) {
            // 3. exists
            return JSONUtil.toBean(json, type);
        }
        // see whether json data (cache from redis) is null
        if (json != null) {
            return null;
        }

        // 4. cache doesn't exist, search database with id
        R r = dbFallback.apply(id);
        // 5. doesn't exist
        if (r == null) {
            // write null into redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6. user exists, write into redis
        this.set(key, r, time, unit);
        return r;
    }

//    solve Cache Breakdown problem with logical expire
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. search shop info from redis
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. see whether shop cache exists
        if (StrUtil.isBlank(json)) {
            // 3. cache is null, return null
//            in cache breakdown situation, the cache is about hot data, and with logical expire, the cache
//            won't expire, so all in all such cache is always present.
            return null;
        }
        // 4. there exists such json data, unserialize json into object
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 5. check whether the data expired
        if(expireTime.isAfter(LocalDateTime.now())) {
            // 5.1 haven't expire, return shop info
            return r;
        }
        // 5.2 expired, cache restoration
        // 6.1 get mutex
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 6.2 check whether get mutex successfully
        if (isLock){
//            6.3 create an independent thread to perform cache restoration
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    // search from the database
                    R newR = dbFallback.apply(id);
                    // write into redis
                    this.setWithLogicalExpire(key, newR, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });
        }
        // 6.4 return expired shop info
        return r;
    }

//    solve Cache Breakdown problem with mutex
    public <R, ID> R queryWithMutex(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, type);
        }
        if (shopJson != null) {
            return null;
        }

        // 4.1 try to get mutex
        String lockKey = LOCK_SHOP_KEY + id;
        R r = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 4.2 see if successfully get the mutex
            if (!isLock) {
                // 4.3 fail, sleep and then try again
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallback, time, unit);
            }
            // 4.4 succeed, try to rebuild cache from the database
            r = dbFallback.apply(id);
            // 5. data not exist in database
            if (r == null) {
                // write null into redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6. data exists in database, write into redis
            this.set(key, r, time, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            // 7. unlock key
            unlock(lockKey);
        }
        // 8. return
        return r;
    }

//    try to get the lock
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}

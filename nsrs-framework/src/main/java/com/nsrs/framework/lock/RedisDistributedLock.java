package com.nsrs.framework.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的分布式锁实现
 * 使用Lua脚本确保操作的原子性
 *
 * @author NSRS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDistributedLock implements DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 锁的前缀
     */
    private static final String LOCK_PREFIX = "nsrs:lock:";
    
    /**
     * 默认锁过期时间（秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 30;
    
    /**
     * 当前线程的锁标识
     */
    private static final ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();
    
    /**
     * 获取锁的Lua脚本
     * 如果key不存在，则设置key和过期时间，返回1
     * 如果key存在且值相同（重入），则更新过期时间，返回1
     * 否则返回0
     */
    private static final String LOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == false then " +
        "  redis.call('set', KEYS[1], ARGV[1]) " +
        "  redis.call('expire', KEYS[1], ARGV[2]) " +
        "  return 1 " +
        "elseif redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  redis.call('expire', KEYS[1], ARGV[2]) " +
        "  return 1 " +
        "else " +
        "  return 0 " +
        "end";
    
    /**
     * 释放锁的Lua脚本
     * 只有当key存在且值相同时才删除，确保只能释放自己的锁
     */
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "  return redis.call('del', KEYS[1]) " +
        "else " +
        "  return 0 " +
        "end";

    @Override
    public boolean tryLock(String lockKey) {
        return tryLock(lockKey, 0, DEFAULT_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    public boolean tryLock(String lockKey, long timeout, TimeUnit unit) {
        return tryLock(lockKey, timeout, DEFAULT_EXPIRE_TIME, unit);
    }

    @Override
    public boolean tryLock(String lockKey, long timeout, long expireTime, TimeUnit unit) {
        String key = LOCK_PREFIX + lockKey;
        String value = generateLockValue();
        
        long timeoutMillis = unit.toMillis(timeout);
        long expireSeconds = unit.toSeconds(expireTime);
        
        long startTime = System.currentTimeMillis();
        
        try {
            do {
                // 执行Lua脚本尝试获取锁
                DefaultRedisScript<Long> script = new DefaultRedisScript<>(LOCK_SCRIPT, Long.class);
                Long result = redisTemplate.execute(script, Collections.singletonList(key), value, String.valueOf(expireSeconds));
                
                if (result != null && result == 1) {
                    // 获取锁成功，保存锁标识
                    LOCK_VALUE.set(value);
                    log.debug("Successfully acquired distributed lock: {}, value: {}, expire time: {} seconds", key, value, expireSeconds);
                    return true;
                }
                
                // 如果设置了超时时间，检查是否超时
                if (timeout > 0) {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime >= timeoutMillis) {
                        log.debug("Distributed lock acquisition timeout: {}, timeout: {}ms", key, timeoutMillis);
                        break;
                    }
                    
                    // 短暂休眠后重试
                    Thread.sleep(50);
                }
            } while (timeout > 0);
            
            log.debug("Failed to acquire distributed lock: {}", key);
            return false;
            
        } catch (Exception e) {
            log.error("Exception occurred while acquiring distributed lock: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean unlock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String value = LOCK_VALUE.get();
        
        if (value == null) {
            log.warn("Attempting to release a lock not held: {}", key);
            return false;
        }
        
        try {
            // 执行Lua脚本释放锁
            DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
            Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
            
            if (result != null && result == 1) {
                LOCK_VALUE.remove();
                log.debug("Successfully released distributed lock: {}, value: {}", key, value);
                return true;
            } else {
                log.warn("Failed to release distributed lock, lock may have expired or held by another thread: {}", key);
                return false;
            }
            
        } catch (Exception e) {
            log.error("Exception occurred while releasing distributed lock: {}", key, e);
            return false;
        } finally {
            // 清理ThreadLocal，避免内存泄漏
            LOCK_VALUE.remove();
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Exception occurred while checking lock status: {}", key, e);
            return false;
        }
    }

    @Override
    public long getExpireTime(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception occurred while getting lock expire time: {}", key, e);
            return -2;
        }
    }
    
    /**
     * 生成锁的唯一标识
     * 使用线程ID + UUID确保唯一性
     *
     * @return 锁标识
     */
    private String generateLockValue() {
        return Thread.currentThread().getId() + ":" + UUID.randomUUID().toString();
    }
}
package com.nsrs.simcard.cache.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsrs.simcard.cache.InventoryCacheService;
import com.nsrs.simcard.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based Inventory Cache Service Implementation
 * Used when cache.type=redis and Redis is available
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "nsrs.inventory.cache.type", havingValue = "redis")
public class RedisInventoryCacheServiceImpl implements InventoryCacheService {
    
    private static final String CACHE_KEY_PREFIX = "inventory:cache:";
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CacheProperties cacheProperties;
    
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        if (!isEnabled()) {
            return null;
        }
        
        try {
            String redisKey = CACHE_KEY_PREFIX + key;
            Object cachedData = redisTemplate.opsForValue().get(redisKey);
            
            if (cachedData == null) {
                log.debug("Cache miss for key: {}", key);
                return null;
            }
            
            // Convert cached data back to Map
            if (cachedData instanceof String) {
                return objectMapper.readValue((String) cachedData, new TypeReference<Map<String, Object>>() {});
            } else if (cachedData instanceof Map) {
                log.debug("Cache hit for key: {}", key);
                return (Map<String, Object>) cachedData;
            }
            
            log.warn("Unexpected cached data type for key: {}, type: {}", key, cachedData.getClass().getSimpleName());
            return null;
            
        } catch (Exception e) {
            log.error("Failed to get cache for key: {}", key, e);
            return null;
        }
    }
    
    @Override
    public void put(String key, Map<String, Object> data) {
        if (!isEnabled()) {
            return;
        }
        
        try {
            String redisKey = CACHE_KEY_PREFIX + key;
            redisTemplate.opsForValue().set(redisKey, data, cacheProperties.getExpireTime(), TimeUnit.MINUTES);
            log.debug("Cache entry added for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to put cache for key: {}, error: {}", key, e.getMessage());
        }
    }
    
    @Override
    public void remove(String key) {
        if (!isEnabled()) {
            return;
        }
        
        try {
            String redisKey = CACHE_KEY_PREFIX + key;
            redisTemplate.delete(redisKey);
            log.debug("Cache entry removed for key: {}", key);
        } catch (Exception e) {
            log.error("Failed to remove cache for key: {}, error: {}", key, e.getMessage());
        }
    }
    
    @Override
    public void clearAll() {
        if (!isEnabled()) {
            return;
        }
        
        try {
            Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all cache entries, total: {}", keys.size());
            }
        } catch (Exception e) {
            log.error("Failed to clear all cache, error: {}", e.getMessage());
        }
    }
    
    @Override
    public void clearExpired() {
        // Redis automatically handles expiration, no manual cleanup needed
        log.debug("Redis automatically handles expired entries");
    }
    
    @Override
    public boolean isEnabled() {
        return cacheProperties.isEnabled() && redisTemplate != null;
    }
}
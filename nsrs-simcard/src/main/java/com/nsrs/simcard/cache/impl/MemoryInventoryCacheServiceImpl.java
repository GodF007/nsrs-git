package com.nsrs.simcard.cache.impl;

import com.nsrs.simcard.cache.InventoryCacheService;
import com.nsrs.simcard.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory-based Inventory Cache Service Implementation
 * Used when cache.type=memory or Redis is not available
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "nsrs.inventory.cache.type", havingValue = "memory", matchIfMissing = true)
public class MemoryInventoryCacheServiceImpl implements InventoryCacheService {
    
    @Autowired
    private CacheProperties cacheProperties;
    
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    /**
     * Cache Entry with timestamp
     */
    private static class CacheEntry {
        private final Map<String, Object> data;
        private final long timestamp;
        
        public CacheEntry(Map<String, Object> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public boolean isExpired(long expireTime) {
            return System.currentTimeMillis() - timestamp > expireTime;
        }
    }
    
    @Override
    public Map<String, Object> get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        
        long expireTime = cacheProperties.getExpireTime() * 60 * 1000L; // Convert minutes to milliseconds
         if (entry.isExpired(expireTime)) {
            cache.remove(key);
            log.debug("Cache entry expired and removed for key: {}", key);
            return null;
        }
        
        log.debug("Cache hit for key: {}", key);
        return entry.getData();
    }
    
    @Override
    public void put(String key, Map<String, Object> data) {
        cache.put(key, new CacheEntry(data));
        log.debug("Cache entry added for key: {}", key);
    }
    
    @Override
    public void remove(String key) {
        cache.remove(key);
        log.debug("Cache entry removed for key: {}", key);
    }
    
    @Override
    public void clearAll() {
        int size = cache.size();
        cache.clear();
        log.info("Cleared all cache entries, total: {}", size);
    }
    
    @Override
     public void clearExpired() {
         long currentTime = System.currentTimeMillis();
         final int[] removedCount = {0}; // Use array to make it effectively final
         long expireTime = cacheProperties.getExpireTime() * 60 * 1000L; // Convert minutes to milliseconds
          
          cache.entrySet().removeIf(entry -> {
              if (entry.getValue().isExpired(expireTime)) {
                 removedCount[0]++;
                 return true;
             }
             return false;
         });
         
         log.debug("Cleared {} expired cache entries, remaining: {}", removedCount[0], cache.size());
     }
    
    @Override
    public boolean isEnabled() {
        return cacheProperties.isEnabled();
    }
}
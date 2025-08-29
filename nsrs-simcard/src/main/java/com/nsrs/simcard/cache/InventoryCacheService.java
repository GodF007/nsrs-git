package com.nsrs.simcard.cache;

import java.util.Map;

/**
 * Inventory Cache Service Interface
 * Provides abstraction for different cache implementations (memory, Redis)
 */
public interface InventoryCacheService {
    
    /**
     * Get cached inventory data
     * @param key cache key
     * @return cached data or null if not found or expired
     */
    Map<String, Object> get(String key);
    
    /**
     * Put inventory data into cache
     * @param key cache key
     * @param data inventory data
     */
    void put(String key, Map<String, Object> data);
    
    /**
     * Remove specific cache entry
     * @param key cache key
     */
    void remove(String key);
    
    /**
     * Clear all cache entries
     */
    void clearAll();
    
    /**
     * Clear expired cache entries
     */
    void clearExpired();
    
    /**
     * Check if cache is enabled
     * @return true if cache is enabled
     */
    boolean isEnabled();
}
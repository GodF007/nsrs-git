package com.nsrs.simcard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cache Configuration Properties
 * Reads cache-related configuration from application.yml
 */
@Component
@ConfigurationProperties(prefix = "nsrs.inventory.cache")
public class CacheProperties {
    
    /**
     * Cache type: memory or redis
     */
    private String type = "memory";
    
    /**
     * Cache expiration time in minutes
     */
    private int expireTime = 5;
    
    /**
     * Whether cache is enabled
     */
    private boolean enabled = true;
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getExpireTime() {
        return expireTime;
    }
    
    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Check if memory cache is enabled
     */
    public boolean isMemoryCache() {
        return "memory".equalsIgnoreCase(type);
    }
    
    /**
     * Check if Redis cache is enabled
     */
    public boolean isRedisCache() {
        return "redis".equalsIgnoreCase(type);
    }
}
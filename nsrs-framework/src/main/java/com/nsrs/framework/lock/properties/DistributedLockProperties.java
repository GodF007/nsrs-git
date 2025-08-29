package com.nsrs.framework.lock.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 分布式锁配置属性
 * 用于配置分布式锁的默认参数
 *
 * @author NSRS
 */
@Data
@Component
@ConfigurationProperties(prefix = "nsrs.distributed-lock")
public class DistributedLockProperties {

    /**
     * 是否启用分布式锁，默认启用
     */
    private boolean enabled = true;

    /**
     * 锁的默认前缀
     */
    private String prefix = "nsrs:lock";

    /**
     * 默认锁过期时间（秒）
     */
    private long defaultExpireTime = 30;

    /**
     * 默认获取锁超时时间（秒）
     */
    private long defaultTimeout = 0;

    /**
     * 获取锁失败时的重试间隔（毫秒）
     */
    private long retryInterval = 50;

    /**
     * 是否启用锁的监控和统计
     */
    private boolean enableMonitor = false;

    /**
     * 锁的最大过期时间（秒），防止设置过长的过期时间
     */
    private long maxExpireTime = 3600;

    /**
     * 锁的最大超时时间（秒），防止设置过长的超时时间
     */
    private long maxTimeout = 300;
}
package com.nsrs.framework.lock.config;

import com.nsrs.framework.lock.DistributedLock;
import com.nsrs.framework.lock.DistributedLockUtil;
import com.nsrs.framework.lock.RedisDistributedLock;
import com.nsrs.framework.lock.aspect.DistributedLockAspect;
import com.nsrs.framework.lock.properties.DistributedLockProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 分布式锁自动配置类
 * 自动配置分布式锁相关的Bean
 *
 * @author NSRS
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DistributedLockProperties.class)
@ConditionalOnProperty(prefix = "nsrs.distributed-lock", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DistributedLockAutoConfiguration {

    /**
     * 配置Redis分布式锁实现
     */
    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock(RedisTemplate<String, Object> redisTemplate) {
        log.info("Initializing Redis distributed lock implementation");
        return new RedisDistributedLock(redisTemplate);
    }

    /**
     * 配置分布式锁工具类
     */
    @Bean
    @ConditionalOnMissingBean(DistributedLockUtil.class)
    public DistributedLockUtil distributedLockUtil(DistributedLock distributedLock) {
        log.info("Initializing distributed lock utility class");
        return new DistributedLockUtil(distributedLock);
    }

    /**
     * 配置分布式锁AOP切面
     */
    @Bean
    @ConditionalOnMissingBean(DistributedLockAspect.class)
    public DistributedLockAspect distributedLockAspect(DistributedLock distributedLock) {
        log.info("Initializing distributed lock AOP aspect");
        return new DistributedLockAspect(distributedLock);
    }
}
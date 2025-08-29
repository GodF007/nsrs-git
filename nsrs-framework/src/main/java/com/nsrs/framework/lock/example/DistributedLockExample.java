package com.nsrs.framework.lock.example;

import com.nsrs.framework.lock.DistributedLockUtil;
import com.nsrs.framework.lock.annotation.DistributedLockable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁使用示例
 * 展示如何在业务代码中使用分布式锁
 *
 * @author NSRS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockExample {

    private final DistributedLockUtil lockUtil;

    /**
     * 示例1：使用注解方式加锁
     * 锁key为：user:#{#userId}
     */
    @DistributedLockable(
        key = "user:#{#userId}",
        timeout = 5,
        expireTime = 30,
        timeUnit = TimeUnit.SECONDS,
        errorMessage = "用户操作正在进行中，请稍后重试"
    )
    public void updateUserInfo(Long userId, String userInfo) {
        log.info("Starting to update user info: userId={}, userInfo={}", userId, userInfo);
        
        // 模拟业务处理
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("User info update completed: userId={}", userId);
    }

    /**
     * 示例2：使用注解方式加锁（复杂对象）
     * 锁key为：order:#{#order.id}
     */
    @DistributedLockable(
        key = "order:#{#order.id}",
        prefix = "business",
        timeout = 3,
        expireTime = 60,
        timeUnit = TimeUnit.SECONDS
    )
    public void processOrder(OrderInfo order) {
        log.info("Starting to process order: orderId={}", order.getId());
        
        // 模拟订单处理逻辑
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("Order processing completed: orderId={}", order.getId());
    }

    /**
     * 示例3：使用工具类方式加锁（无返回值）
     */
    public void transferMoney(String fromAccount, String toAccount, Double amount) {
        String lockKey = "transfer:" + fromAccount + ":" + toAccount;
        
        boolean success = lockUtil.executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
            log.info("Starting transfer: from={}, to={}, amount={}", fromAccount, toAccount, amount);
            
            // 模拟转账逻辑
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("Transfer completed: from={}, to={}, amount={}", fromAccount, toAccount, amount);
        });
        
        if (!success) {
            log.warn("Transfer failed, lock acquisition timeout: {}", lockKey);
        }
    }

    /**
     * 示例4：使用工具类方式加锁（有返回值）
     */
    public String generateUniqueNumber(String prefix) {
        String lockKey = "generate:" + prefix;
        
        return lockUtil.executeWithLock(lockKey, () -> {
            log.info("Starting to generate unique number: prefix={}", prefix);
            
            // 模拟生成唯一编号的逻辑
            String uniqueNumber = prefix + System.currentTimeMillis();
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            log.info("Unique number generation completed: {}", uniqueNumber);
            return uniqueNumber;
        });
    }

    /**
     * 示例5：使用工具类方式加锁（带fallback）
     */
    public String getOrCreateCache(String cacheKey) {
        String lockKey = "cache:" + cacheKey;
        
        return lockUtil.executeWithLockOrFallback(
            lockKey,
            () -> {
                log.info("Starting to create cache: {}", cacheKey);
                
                // 模拟创建缓存的逻辑
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                String cacheValue = "cache_value_" + System.currentTimeMillis();
                log.info("Cache creation completed: {} = {}", cacheKey, cacheValue);
                return cacheValue;
            },
            () -> {
                log.info("Failed to acquire lock, returning default value: {}", cacheKey);
                return "default_value";
            }
        );
    }

    /**
     * 示例6：检查锁状态
     */
    public boolean isResourceLocked(String resourceId) {
        String lockKey = "resource:" + resourceId;
        return lockUtil.isLocked(lockKey);
    }

    /**
     * 示例7：获取锁的剩余时间
     */
    public long getLockExpireTime(String resourceId) {
        String lockKey = "resource:" + resourceId;
        return lockUtil.getExpireTime(lockKey);
    }

    /**
     * 订单信息类（示例用）
     */
    public static class OrderInfo {
        private Long id;
        private String orderNo;
        private Double amount;

        public OrderInfo(Long id, String orderNo, Double amount) {
            this.id = id;
            this.orderNo = orderNo;
            this.amount = amount;
        }

        public Long getId() {
            return id;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public Double getAmount() {
            return amount;
        }
    }
}
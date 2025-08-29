package com.nsrs.framework.lock;

import com.nsrs.framework.config.TestRedisConfig;
import com.nsrs.framework.lock.annotation.DistributedLockable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分布式锁集成测试
 * 需要启动Redis服务才能运行
 *
 * @author NSRS
 */
@SpringBootTest(classes = com.nsrs.framework.TestApplication.class)
@Import({TestRedisConfig.class, DistributedLockIntegrationTest.TestLockServiceConfig.class})
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure.class,
    org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration.class
})
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=${embedded.redis.port:6379}",
    "spring.redis.database=0",
    "spring.redis.timeout=2000ms",
    "spring.redis.jedis.pool.max-active=20",
    "spring.data.redis.repositories.enabled=false"
})
class DistributedLockIntegrationTest {

    @Autowired
    private TestLockService testLockService;

    @Autowired
    private DistributedLockUtil lockUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testAnnotationBasedLock() {
        // 测试注解方式的分布式锁
        String result = testLockService.processWithLock("test-user-123");
        assertEquals("处理完成: test-user-123", result);
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    boolean success = lockUtil.executeWithLock("concurrent-test", 1, 10, TimeUnit.SECONDS, () -> {
                        // 模拟业务处理
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        System.out.println("线程 " + index + " 执行业务逻辑");
                    });
                    
                    if (success) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // 验证只有部分线程成功获取锁
        assertTrue(successCount.get() > 0, "应该有线程成功获取锁");
        assertTrue(failCount.get() > 0, "应该有线程获取锁失败");
        assertEquals(threadCount, successCount.get() + failCount.get(), "总数应该等于线程数");
        
        System.out.println("成功获取锁的线程数: " + successCount.get());
        System.out.println("获取锁失败的线程数: " + failCount.get());
    }

    @Test
    void testLockExpiration() throws InterruptedException {
        String lockKey = "expiration-test";
        
        // 获取一个短期锁
        boolean locked = lockUtil.executeWithLock(lockKey, 0, 2, TimeUnit.SECONDS, () -> {
            // 不做任何操作，让锁自动过期
        });
        
        assertTrue(locked, "应该成功获取锁");
        
        // 等待锁过期
        Thread.sleep(3000);
        
        // 验证锁已经过期
        assertFalse(lockUtil.isLocked(lockKey), "锁应该已经过期");
        
        // 再次获取锁应该成功
        boolean lockedAgain = lockUtil.executeWithLock(lockKey, () -> {
            // 业务逻辑
        });
        
        assertTrue(lockedAgain, "锁过期后应该能再次获取");
    }

    @Test
    void testSpELExpressionInAnnotation() {
        // 测试SpEL表达式
        String result = testLockService.processOrder("ORDER-123", "PROCESS");
        assertEquals("订单处理完成: ORDER-123", result);
    }

    @Test
    void testLockWithFallback() {
        String result = lockUtil.executeWithLockOrFallback(
            "fallback-test",
            0, 1, TimeUnit.SECONDS,
            () -> {
                // 主要业务逻辑
                try {
                    Thread.sleep(2000); // 模拟长时间处理
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "主要任务完成";
            },
            () -> {
                // 降级逻辑
                return "降级任务完成";
            }
        );
        
        assertNotNull(result);
        System.out.println("执行结果: " + result);
    }

    @Test
    void testLockUtilMethods() {
        String lockKey = "util-test";
        
        // 测试获取锁
        boolean success = lockUtil.executeWithLock(lockKey, 0, 30, TimeUnit.SECONDS, () -> {
            // 在锁内检查状态
            assertTrue(lockUtil.isLocked(lockKey), "在锁内应该显示已锁定");
            
            long expireTime = lockUtil.getExpireTime(lockKey);
            assertTrue(expireTime > 0, "过期时间应该大于0");
            assertTrue(expireTime <= 30, "过期时间应该小于等于30秒");
        });
        
        assertTrue(success, "应该成功执行");
        
        // 锁释放后检查状态
        assertFalse(lockUtil.isLocked(lockKey), "锁释放后应该显示未锁定");
    }

    @Test
    void testForceUnlock() {
        String lockKey = "force-unlock-test";
        
        // 手动获取锁（不通过工具类）
        RedisDistributedLock lock = new RedisDistributedLock(redisTemplate);
        boolean acquired = lock.tryLock(lockKey, 0, 30, TimeUnit.SECONDS);
        assertTrue(acquired, "应该成功获取锁");
        
        // 验证锁存在
        assertTrue(lockUtil.isLocked(lockKey), "锁应该存在");
        
        // 强制释放锁
        boolean unlocked = lockUtil.forceUnlock(lockKey);
        assertTrue(unlocked, "应该成功强制释放锁");
        
        // 验证锁已释放
        assertFalse(lockUtil.isLocked(lockKey), "锁应该已被释放");
    }

    @TestConfiguration
    public static class TestLockServiceConfig {
        
        @Bean
        public TestLockService testLockService() {
            return new TestLockService();
        }
    }

    public static class TestLockService {

        @DistributedLockable(key = "user:#{#p0}", timeout = 5, expireTime = 30, errorMessage = "用户正在处理中，请稍后重试")
        public String processWithLock(String userId) {
            // 模拟业务处理
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "处理完成: " + userId;
        }

        @DistributedLockable(
            key = "order:#{#p0}:#{#p1}", 
            timeout = 3, 
            expireTime = 20, 
            errorMessage = "订单正在处理中"
        )
        public String processOrder(String orderId, String action) {
            // 模拟订单处理
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "订单处理完成: " + orderId;
        }
    }
}
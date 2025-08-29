package com.nsrs.framework.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis分布式锁测试类
 *
 * @author NSRS
 */
@ExtendWith(MockitoExtension.class)
class RedisDistributedLockTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private RedisDistributedLock distributedLock;

    @BeforeEach
    void setUp() {
        distributedLock = new RedisDistributedLock(redisTemplate);
    }

    @Test
    void testTryLockSuccess() {
        // 模拟获取锁成功
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(1L);

        boolean result = distributedLock.tryLock("test-key");

        assertTrue(result);
        verify(redisTemplate).execute(any(DefaultRedisScript.class), eq(Collections.singletonList("nsrs:lock:test-key")), any(), any());
    }

    @Test
    void testTryLockFailed() {
        // 模拟获取锁失败
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(0L);

        boolean result = distributedLock.tryLock("test-key");

        assertFalse(result);
    }

    @Test
    void testTryLockWithTimeout() {
        // 模拟第一次获取锁失败，第二次成功
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(0L)  // 第一次失败
                .thenReturn(1L); // 第二次成功

        boolean result = distributedLock.tryLock("test-key", 1, TimeUnit.SECONDS);

        assertTrue(result);
        verify(redisTemplate, atLeast(2)).execute(any(DefaultRedisScript.class), anyList(), any(), any());
    }

    @Test
    void testUnlockSuccess() {
        // 先获取锁
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(1L);  // 获取锁成功
        
        distributedLock.tryLock("test-key");
        
        // 模拟释放锁成功
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenReturn(1L);

        boolean result = distributedLock.unlock("test-key");

        assertTrue(result);
    }

    @Test
    void testUnlockWithoutLock() {
        // 没有获取锁就尝试释放
        boolean result = distributedLock.unlock("test-key");

        assertFalse(result);
        verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), any());
    }

    @Test
    void testIsLocked() {
        when(redisTemplate.hasKey("nsrs:lock:test-key")).thenReturn(true);

        boolean result = distributedLock.isLocked("test-key");

        assertTrue(result);
        verify(redisTemplate).hasKey("nsrs:lock:test-key");
    }

    @Test
    void testGetExpireTime() {
        when(redisTemplate.getExpire("nsrs:lock:test-key", TimeUnit.SECONDS)).thenReturn(25L);

        long expireTime = distributedLock.getExpireTime("test-key");

        assertEquals(25L, expireTime);
        verify(redisTemplate).getExpire("nsrs:lock:test-key", TimeUnit.SECONDS);
    }

    @Test
    void testTryLockWithException() {
        // 模拟Redis异常
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        boolean result = distributedLock.tryLock("test-key");

        assertFalse(result);
    }

    @Test
    void testUnlockWithException() {
        // 先获取锁
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any()))
                .thenReturn(1L);
        
        distributedLock.tryLock("test-key");
        
        // 模拟释放锁时异常
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any()))
                .thenThrow(new RuntimeException("Redis connection failed"));

        boolean result = distributedLock.unlock("test-key");

        assertFalse(result);
    }
}
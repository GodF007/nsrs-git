package com.nsrs.framework.lock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁工具类测试
 *
 * @author NSRS
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockUtilTest {

    @Mock
    private DistributedLock distributedLock;

    private DistributedLockUtil lockUtil;

    @BeforeEach
    void setUp() {
        lockUtil = new DistributedLockUtil(distributedLock);
    }

    @Test
    void testExecuteWithLockSuccess() {
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        AtomicInteger counter = new AtomicInteger(0);
        
        boolean result = lockUtil.executeWithLock("test-key", () -> {
            counter.incrementAndGet();
        });

        assertTrue(result);
        assertEquals(1, counter.get());
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testExecuteWithLockFailed() {
        // 模拟获取锁失败
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        AtomicInteger counter = new AtomicInteger(0);
        
        boolean result = lockUtil.executeWithLock("test-key", () -> {
            counter.incrementAndGet();
        });

        assertFalse(result);
        assertEquals(0, counter.get());
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock, never()).unlock(anyString());
    }

    @Test
    void testExecuteWithLockWithReturnValue() {
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        String result = lockUtil.executeWithLock("test-key", () -> {
            return "success";
        });

        assertEquals("success", result);
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testExecuteWithLockWithReturnValueFailed() {
        // 模拟获取锁失败
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        String result = lockUtil.executeWithLock("test-key", () -> {
            return "success";
        });

        assertNull(result);
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock, never()).unlock(anyString());
    }

    @Test
    void testExecuteWithLockOrFallback() {
        // 模拟获取锁失败
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        String result = lockUtil.executeWithLockOrFallback(
            "test-key",
            () -> "main-task",
            () -> "fallback-task"
        );

        assertEquals("fallback-task", result);
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock, never()).unlock(anyString());
    }

    @Test
    void testExecuteWithLockOrFallbackMainSuccess() {
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        String result = lockUtil.executeWithLockOrFallback(
            "test-key",
            () -> "main-task",
            () -> "fallback-task"
        );

        assertEquals("main-task", result);
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testExecuteWithLockException() {
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lockUtil.executeWithLock("test-key", () -> {
                throw new RuntimeException("Business logic error");
            });
        });

        assertEquals("执行带锁的业务逻辑异常", exception.getMessage());
        assertEquals("Business logic error", exception.getCause().getMessage());
        
        // 确保锁被释放
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testExecuteWithLockUnlockFailed() {
        // 模拟获取锁成功，但释放锁失败
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(false);

        AtomicInteger counter = new AtomicInteger(0);
        
        boolean result = lockUtil.executeWithLock("test-key", () -> {
            counter.incrementAndGet();
        });

        assertTrue(result);  // 业务逻辑执行成功
        assertEquals(1, counter.get());
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testIsLocked() {
        when(distributedLock.isLocked("test-key")).thenReturn(true);

        boolean result = lockUtil.isLocked("test-key");

        assertTrue(result);
        verify(distributedLock).isLocked("test-key");
    }

    @Test
    void testGetExpireTime() {
        when(distributedLock.getExpireTime("test-key")).thenReturn(25L);

        long expireTime = lockUtil.getExpireTime("test-key");

        assertEquals(25L, expireTime);
        verify(distributedLock).getExpireTime("test-key");
    }

    @Test
    void testForceUnlock() {
        when(distributedLock.unlock("test-key")).thenReturn(true);

        boolean result = lockUtil.forceUnlock("test-key");

        assertTrue(result);
        verify(distributedLock).unlock("test-key");
    }

    @Test
    void testExecuteWithLockWithCustomTimeout() {
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        boolean result = lockUtil.executeWithLock("test-key", 5, 60, TimeUnit.SECONDS, () -> {
            // 业务逻辑
        });

        assertTrue(result);
        verify(distributedLock).tryLock("test-key", 5, 60, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");
    }
}
package com.nsrs.framework.lock;

import com.nsrs.framework.lock.aspect.DistributedLockAspect;
import com.nsrs.framework.lock.annotation.DistributedLockable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分布式锁切面测试类
 *
 * @author NSRS
 */
@ExtendWith(MockitoExtension.class)
class DistributedLockAspectTest {

    @Mock
    private DistributedLock distributedLock;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private DistributedLockAspect lockAspect;

    @BeforeEach
    void setUp() {
        lockAspect = new DistributedLockAspect(distributedLock);
    }

    @Test
    void testAroundWithLockSuccess() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("test-key", 5, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethod", String.class);
        Object[] args = {"param1"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("success");
        
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        Object result = lockAspect.around(joinPoint, annotation);

        assertEquals("success", result);
        verify(distributedLock).tryLock("test-key", 5, 30, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");
        verify(joinPoint).proceed();
    }

    @Test
    void testAroundWithLockFailed() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("test-key", 5, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethod", String.class);
        Object[] args = {"param1"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        
        // 模拟获取锁失败
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lockAspect.around(joinPoint, annotation);
        });

        assertTrue(exception.getMessage().contains("获取锁失败"));
        verify(distributedLock).tryLock("test-key", 5, 30, TimeUnit.SECONDS);
        verify(distributedLock, never()).unlock(anyString());
        verify(joinPoint, never()).proceed();
    }

    @Test
    void testAroundWithSpELExpression() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("user:#{#param}", 0, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethod", String.class);
        Object[] args = {"123"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("success");
        
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        Object result = lockAspect.around(joinPoint, annotation);

        assertEquals("success", result);
        verify(distributedLock).tryLock(contains("user:"), eq(0L), eq(30L), eq(TimeUnit.SECONDS));
        verify(distributedLock).unlock(contains("user:"));
    }

    @Test
    void testAroundWithBusinessException() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("test-key", 0, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethod", String.class);
        Object[] args = {"param1"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenThrow(new RuntimeException("Business error"));
        
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lockAspect.around(joinPoint, annotation);
        });

        assertEquals("Business error", exception.getMessage());
        verify(distributedLock).tryLock("test-key", 0, 30, TimeUnit.SECONDS);
        verify(distributedLock).unlock("test-key");  // 确保锁被释放
    }

    @Test
    void testAroundWithComplexSpELExpression() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("order:#{#order.id}:#{#action}", 0, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethodWithObject", TestOrder.class, String.class);
        TestOrder order = new TestOrder();
        order.setId("12345");
        Object[] args = {order, "process"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.proceed()).thenReturn("success");
        
        // 模拟获取锁成功
        when(distributedLock.tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(distributedLock.unlock(anyString())).thenReturn(true);

        Object result = lockAspect.around(joinPoint, annotation);

        assertEquals("success", result);
        verify(distributedLock).tryLock(contains("order:"), eq(0L), eq(30L), eq(TimeUnit.SECONDS));
        verify(distributedLock).unlock(contains("order:"));
    }

    @Test
    void testAroundWithInvalidSpELExpression() throws Throwable {
        // 准备测试数据
        DistributedLockable annotation = createAnnotation("invalid:#{#nonExistentParam}", 0, 30, "获取锁失败");
        Method method = TestService.class.getMethod("testMethod", String.class);
        Object[] args = {"param1"};
        
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lockAspect.around(joinPoint, annotation);
        });

        // 由于SpEL表达式解析失败，应该抛出异常
        assertNotNull(exception);
        verify(distributedLock, never()).tryLock(anyString(), anyLong(), anyLong(), any(TimeUnit.class));
    }

    // 辅助方法：创建注解实例
    private DistributedLockable createAnnotation(String key, long timeout, long expireTime, String errorMessage) {
        return new DistributedLockable() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLockable.class;
            }

            @Override
            public String key() {
                return key;
            }

            @Override
            public String prefix() {
                return "";
            }

            @Override
            public long timeout() {
                return timeout;
            }

            @Override
            public long expireTime() {
                return expireTime;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public String errorMessage() {
                return errorMessage;
            }

            @Override
            public boolean throwException() {
                return true;
            }
        };
    }

    // 测试用的服务类
    public static class TestService {
        public String testMethod(String param) {
            return "result";
        }
        
        public String testMethodWithObject(TestOrder order, String action) {
            return "result";
        }
    }

    // 测试用的订单类
    public static class TestOrder {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
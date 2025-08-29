package com.nsrs.framework.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 * 提供便捷的分布式锁使用方法
 *
 * @author NSRS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtil {

    private final DistributedLock distributedLock;

    /**
     * 执行带锁的操作（无返回值）
     *
     * @param lockKey 锁的key
     * @param task    要执行的任务
     * @return 是否执行成功
     */
    public boolean executeWithLock(String lockKey, Runnable task) {
        return executeWithLock(lockKey, 0, 30, TimeUnit.SECONDS, () -> {
            task.run();
            return null;
        }) != null;
    }

    /**
     * 执行带锁的操作（有返回值）
     *
     * @param lockKey  锁的key
     * @param supplier 要执行的任务
     * @param <T>      返回值类型
     * @return 任务执行结果，获取锁失败返回null
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        return executeWithLock(lockKey, 0, 30, TimeUnit.SECONDS, supplier);
    }

    /**
     * 执行带锁的操作（带超时时间）
     *
     * @param lockKey    锁的key
     * @param timeout    获取锁的超时时间
     * @param expireTime 锁的过期时间
     * @param unit       时间单位
     * @param task       要执行的任务
     * @return 是否执行成功
     */
    public boolean executeWithLock(String lockKey, long timeout, long expireTime, TimeUnit unit, Runnable task) {
        return executeWithLock(lockKey, timeout, expireTime, unit, () -> {
            task.run();
            return null;
        }) != null;
    }

    /**
     * 执行带锁的操作（带超时时间，有返回值）
     *
     * @param lockKey    锁的key
     * @param timeout    获取锁的超时时间
     * @param expireTime 锁的过期时间
     * @param unit       时间单位
     * @param supplier   要执行的任务
     * @param <T>        返回值类型
     * @return 任务执行结果，获取锁失败返回null
     */
    public <T> T executeWithLock(String lockKey, long timeout, long expireTime, TimeUnit unit, Supplier<T> supplier) {
        boolean locked = false;
        try {
            // 尝试获取锁
            locked = distributedLock.tryLock(lockKey, timeout, expireTime, unit);
            if (!locked) {
                log.warn("Failed to acquire distributed lock: {}", lockKey);
                return null;
            }

            // 执行业务逻辑
            log.debug("Starting to execute business logic with lock: {}", lockKey);
            return supplier.get();

        } catch (Exception e) {
            log.error("Exception occurred while executing business logic with lock: {}", lockKey, e);
            throw new RuntimeException("Exception occurred while executing business logic with lock", e);
        } finally {
            // 释放锁
            if (locked) {
                boolean unlocked = distributedLock.unlock(lockKey);
                if (unlocked) {
                    log.debug("Successfully released distributed lock: {}", lockKey);
                } else {
                    log.warn("Failed to release distributed lock: {}", lockKey);
                }
            }
        }
    }

    /**
     * 尝试执行带锁的操作，如果获取锁失败则执行fallback
     *
     * @param lockKey  锁的key
     * @param task     主要任务
     * @param fallback 获取锁失败时的备选任务
     * @param <T>      返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLockOrFallback(String lockKey, Supplier<T> task, Supplier<T> fallback) {
        return executeWithLockOrFallback(lockKey, 0, 30, TimeUnit.SECONDS, task, fallback);
    }

    /**
     * 尝试执行带锁的操作，如果获取锁失败则执行fallback（带超时时间）
     *
     * @param lockKey    锁的key
     * @param timeout    获取锁的超时时间
     * @param expireTime 锁的过期时间
     * @param unit       时间单位
     * @param task       主要任务
     * @param fallback   获取锁失败时的备选任务
     * @param <T>        返回值类型
     * @return 任务执行结果
     */
    public <T> T executeWithLockOrFallback(String lockKey, long timeout, long expireTime, TimeUnit unit,
                                           Supplier<T> task, Supplier<T> fallback) {
        T result = executeWithLock(lockKey, timeout, expireTime, unit, task);
        if (result == null && fallback != null) {
            log.info("Failed to acquire lock, executing fallback logic: {}", lockKey);
            return fallback.get();
        }
        return result;
    }

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的key
     * @return 锁是否存在
     */
    public boolean isLocked(String lockKey) {
        return distributedLock.isLocked(lockKey);
    }

    /**
     * 获取锁的剩余过期时间
     *
     * @param lockKey 锁的key
     * @return 剩余过期时间（秒）
     */
    public long getExpireTime(String lockKey) {
        return distributedLock.getExpireTime(lockKey);
    }

    /**
     * 强制释放锁（谨慎使用）
     * 注意：这会释放任何线程持有的锁，可能导致并发问题
     *
     * @param lockKey 锁的key
     * @return 是否释放成功
     */
    public boolean forceUnlock(String lockKey) {
        log.warn("Force releasing distributed lock: {}", lockKey);
        return distributedLock.unlock(lockKey);
    }
}
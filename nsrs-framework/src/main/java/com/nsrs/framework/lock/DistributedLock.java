package com.nsrs.framework.lock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁接口
 * 定义分布式锁的基本操作
 *
 * @author NSRS
 */
public interface DistributedLock {

    /**
     * 尝试获取锁，立即返回
     *
     * @param lockKey 锁的key
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey);

    /**
     * 尝试获取锁，带超时时间
     *
     * @param lockKey 锁的key
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey, long timeout, TimeUnit unit);

    /**
     * 尝试获取锁，带超时时间和锁过期时间
     *
     * @param lockKey    锁的key
     * @param timeout    获取锁的超时时间
     * @param expireTime 锁的过期时间
     * @param unit       时间单位
     * @return 是否获取成功
     */
    boolean tryLock(String lockKey, long timeout, long expireTime, TimeUnit unit);

    /**
     * 释放锁
     *
     * @param lockKey 锁的key
     * @return 是否释放成功
     */
    boolean unlock(String lockKey);

    /**
     * 检查锁是否存在
     *
     * @param lockKey 锁的key
     * @return 锁是否存在
     */
    boolean isLocked(String lockKey);

    /**
     * 获取锁的剩余过期时间
     *
     * @param lockKey 锁的key
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    long getExpireTime(String lockKey);
}
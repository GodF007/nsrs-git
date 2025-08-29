package com.nsrs.framework.lock.exception;

/**
 * 分布式锁异常类
 * 用于处理分布式锁相关的异常情况
 *
 * @author NSRS
 */
public class DistributedLockException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DistributedLockException() {
        super();
    }

    public DistributedLockException(String message) {
        super(message);
    }

    public DistributedLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public DistributedLockException(Throwable cause) {
        super(cause);
    }

    /**
     * 获取锁超时异常
     */
    public static class LockTimeoutException extends DistributedLockException {
        public LockTimeoutException(String lockKey, long timeout) {
            super(String.format("获取分布式锁超时: %s, 超时时间: %dms", lockKey, timeout));
        }
    }

    /**
     * 释放锁失败异常
     */
    public static class UnlockFailedException extends DistributedLockException {
        public UnlockFailedException(String lockKey) {
            super(String.format("释放分布式锁失败: %s", lockKey));
        }
    }

    /**
     * 锁已被其他线程持有异常
     */
    public static class LockAlreadyHeldException extends DistributedLockException {
        public LockAlreadyHeldException(String lockKey) {
            super(String.format("分布式锁已被其他线程持有: %s", lockKey));
        }
    }
}
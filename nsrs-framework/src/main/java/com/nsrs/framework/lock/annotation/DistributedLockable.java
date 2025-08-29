package com.nsrs.framework.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * 在方法上使用此注解可以自动加分布式锁
 *
 * @author NSRS
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLockable {

    /**
     * 锁的key，支持SpEL表达式
     * 例如："user:#{#userId}" 或 "order:#{#order.id}"
     */
    String key();

    /**
     * 锁的前缀，默认为空
     */
    String prefix() default "";

    /**
     * 获取锁的超时时间，默认为0（立即返回）
     */
    long timeout() default 0;

    /**
     * 锁的过期时间，默认为30秒
     */
    long expireTime() default 30;

    /**
     * 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁失败时是否抛出异常，默认为true
     */
    boolean throwException() default true;

    /**
     * 获取锁失败时的错误信息
     */
    String errorMessage() default "获取分布式锁失败";
}
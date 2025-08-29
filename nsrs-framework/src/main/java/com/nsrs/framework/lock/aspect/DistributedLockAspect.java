package com.nsrs.framework.lock.aspect;

import com.nsrs.framework.lock.DistributedLock;
import com.nsrs.framework.lock.annotation.DistributedLockable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 分布式锁AOP切面
 * 处理@DistributedLockable注解的自动加锁逻辑
 *
 * @author NSRS
 */
@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final DistributedLock distributedLock;
    
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 环绕通知，处理分布式锁逻辑
     */
    @Around("@annotation(distributedLockable)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLockable distributedLockable) throws Throwable {
        // 解析锁的key
        String lockKey = parseLockKey(joinPoint, distributedLockable);
        
        boolean locked = false;
        try {
            // 尝试获取锁
            locked = distributedLock.tryLock(
                lockKey, 
                distributedLockable.timeout(), 
                distributedLockable.expireTime(), 
                distributedLockable.timeUnit()
            );
            
            if (!locked) {
                String errorMsg = distributedLockable.errorMessage() + ": " + lockKey;
                log.warn(errorMsg);
                
                if (distributedLockable.throwException()) {
                    throw new RuntimeException(errorMsg);
                } else {
                    return null;
                }
            }
            
            log.debug("Successfully acquired distributed lock, starting business logic: {}", lockKey);
            
            // 执行目标方法
            return joinPoint.proceed();
            
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
     * 解析锁的key，支持SpEL表达式
     *
     * @param joinPoint          切点
     * @param distributedLockable 注解
     * @return 解析后的锁key
     */
    private String parseLockKey(ProceedingJoinPoint joinPoint, DistributedLockable distributedLockable) {
        String key = distributedLockable.key();
        String prefix = distributedLockable.prefix();
        
        // 如果key包含SpEL表达式，则解析
        if (key.contains("#")) {
            key = parseSpelExpression(joinPoint, key);
        }
        
        // 添加前缀
        if (StringUtils.hasText(prefix)) {
            key = prefix + ":" + key;
        }
        
        return key;
    }

    /**
     * 解析SpEL表达式
     *
     * @param joinPoint 切点
     * @param key       包含SpEL表达式的key
     * @return 解析后的key
     */
    private String parseSpelExpression(ProceedingJoinPoint joinPoint, String key) {
        try {
            // 获取方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            
            // 获取参数名和参数值
            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            
            // 创建SpEL上下文
            EvaluationContext context = new StandardEvaluationContext();
            
            // 将方法参数添加到上下文中
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length && i < args.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }
            
            // 同时支持参数索引访问 (p0, p1, p2...)
            for (int i = 0; i < args.length; i++) {
                context.setVariable("p" + i, args[i]);
            }
            
            // 解析表达式 - 使用模板解析器
            Expression expression = parser.parseExpression(key, new org.springframework.expression.common.TemplateParserContext());
            Object value = expression.getValue(context);
            
            return value != null ? value.toString() : "";
            
        } catch (Exception e) {
            log.warn("SpEL expression parsing failed, trying simple replacement: {}", key);
            // 如果SpEL解析失败，尝试简单的字符串替换
            return simpleReplace(joinPoint, key);
        }
    }
    
    /**
     * 简单的字符串替换方法
     */
    private String simpleReplace(ProceedingJoinPoint joinPoint, String key) {
        Object[] args = joinPoint.getArgs();
        String result = key;
        
        // 替换 #{#p0}, #{#p1} 等
        for (int i = 0; i < args.length; i++) {
            String placeholder = "#{#p" + i + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, args[i] != null ? args[i].toString() : "");
            }
        }
        
        return result;
    }
}
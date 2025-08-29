# NSRS 分布式锁工具

基于Redis实现的分布式锁工具，提供简单易用的API和注解支持。

## 功能特性

- 🔒 **基于Redis实现**：使用Redis的原子操作确保锁的可靠性
- 🚀 **Lua脚本保证原子性**：获取锁和释放锁使用Lua脚本，确保操作的原子性
- 🎯 **注解支持**：提供`@DistributedLockable`注解，简化使用
- 🛠️ **工具类支持**：提供`DistributedLockUtil`工具类，支持编程式使用
- 🔄 **可重入锁**：支持同一线程多次获取同一把锁
- ⏰ **超时控制**：支持获取锁超时和锁自动过期
- 🎨 **SpEL表达式**：注解中的锁key支持SpEL表达式
- 📊 **监控支持**：可配置锁的监控和统计

## 快速开始

### 1. 添加依赖

项目已经包含了Redis依赖，确保Redis服务正常运行。

### 2. 配置Redis

在`application.yml`中配置Redis连接：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 10000

# 分布式锁配置（可选）
nsrs:
  distributed-lock:
    enabled: true                    # 是否启用分布式锁，默认true
    prefix: "nsrs:lock"             # 锁的默认前缀
    default-expire-time: 30         # 默认锁过期时间（秒）
    default-timeout: 0              # 默认获取锁超时时间（秒）
    retry-interval: 50              # 重试间隔（毫秒）
    enable-monitor: false           # 是否启用监控
    max-expire-time: 3600           # 最大过期时间（秒）
    max-timeout: 300                # 最大超时时间（秒）
```

### 3. 使用方式

#### 方式一：注解方式（推荐）

```java
@Service
public class UserService {
    
    /**
     * 使用注解加锁，锁key为：user:#{#userId}
     */
    @DistributedLockable(
        key = "user:#{#userId}",
        timeout = 5,
        expireTime = 30,
        timeUnit = TimeUnit.SECONDS,
        errorMessage = "用户操作正在进行中，请稍后重试"
    )
    public void updateUserInfo(Long userId, String userInfo) {
        // 业务逻辑
        System.out.println("更新用户信息: " + userId);
    }
    
    /**
     * 复杂对象的锁key
     */
    @DistributedLockable(
        key = "order:#{#order.id}",
        prefix = "business",
        timeout = 3,
        expireTime = 60
    )
    public void processOrder(Order order) {
        // 处理订单逻辑
        System.out.println("处理订单: " + order.getId());
    }
}
```

#### 方式二：工具类方式

```java
@Service
@RequiredArgsConstructor
public class TransferService {
    
    private final DistributedLockUtil lockUtil;
    
    /**
     * 无返回值的业务逻辑
     */
    public void transferMoney(String fromAccount, String toAccount, Double amount) {
        String lockKey = "transfer:" + fromAccount + ":" + toAccount;
        
        boolean success = lockUtil.executeWithLock(lockKey, 5, 30, TimeUnit.SECONDS, () -> {
            // 转账逻辑
            System.out.println("执行转账操作");
        });
        
        if (!success) {
            throw new RuntimeException("转账失败，系统繁忙");
        }
    }
    
    /**
     * 有返回值的业务逻辑
     */
    public String generateUniqueNumber(String prefix) {
        String lockKey = "generate:" + prefix;
        
        return lockUtil.executeWithLock(lockKey, () -> {
            // 生成唯一编号的逻辑
            return prefix + System.currentTimeMillis();
        });
    }
    
    /**
     * 带fallback的业务逻辑
     */
    public String getOrCreateCache(String cacheKey) {
        String lockKey = "cache:" + cacheKey;
        
        return lockUtil.executeWithLockOrFallback(
            lockKey,
            () -> {
                // 创建缓存的逻辑
                return "new_cache_value";
            },
            () -> {
                // 获取锁失败时的fallback逻辑
                return "default_value";
            }
        );
    }
}
```

#### 方式三：直接使用DistributedLock接口

```java
@Service
@RequiredArgsConstructor
public class LowLevelService {
    
    private final DistributedLock distributedLock;
    
    public void doSomething(String resourceId) {
        String lockKey = "resource:" + resourceId;
        boolean locked = false;
        
        try {
            // 尝试获取锁
            locked = distributedLock.tryLock(lockKey, 5, 30, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("获取锁失败");
            }
            
            // 业务逻辑
            System.out.println("执行业务逻辑");
            
        } finally {
            // 释放锁
            if (locked) {
                distributedLock.unlock(lockKey);
            }
        }
    }
}
```

## API 文档

### @DistributedLockable 注解

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| key | String | - | 锁的key，支持SpEL表达式 |
| prefix | String | "" | 锁的前缀 |
| timeout | long | 0 | 获取锁的超时时间 |
| expireTime | long | 30 | 锁的过期时间 |
| timeUnit | TimeUnit | SECONDS | 时间单位 |
| throwException | boolean | true | 获取锁失败时是否抛出异常 |
| errorMessage | String | "获取分布式锁失败" | 错误信息 |

### DistributedLock 接口

| 方法 | 说明 |
|------|------|
| `tryLock(String lockKey)` | 尝试获取锁，立即返回 |
| `tryLock(String lockKey, long timeout, TimeUnit unit)` | 尝试获取锁，带超时时间 |
| `tryLock(String lockKey, long timeout, long expireTime, TimeUnit unit)` | 尝试获取锁，带超时时间和过期时间 |
| `unlock(String lockKey)` | 释放锁 |
| `isLocked(String lockKey)` | 检查锁是否存在 |
| `getExpireTime(String lockKey)` | 获取锁的剩余过期时间 |

### DistributedLockUtil 工具类

| 方法 | 说明 |
|------|------|
| `executeWithLock(String lockKey, Runnable task)` | 执行带锁的操作（无返回值） |
| `executeWithLock(String lockKey, Supplier<T> supplier)` | 执行带锁的操作（有返回值） |
| `executeWithLockOrFallback(...)` | 执行带锁的操作，失败时执行fallback |
| `isLocked(String lockKey)` | 检查锁是否存在 |
| `getExpireTime(String lockKey)` | 获取锁的剩余过期时间 |
| `forceUnlock(String lockKey)` | 强制释放锁（谨慎使用） |

## SpEL 表达式支持

注解中的`key`属性支持SpEL表达式，可以动态生成锁的key：

```java
// 使用方法参数
@DistributedLockable(key = "user:#{#userId}")
public void updateUser(Long userId) { }

// 使用对象属性
@DistributedLockable(key = "order:#{#order.id}")
public void processOrder(Order order) { }

// 复杂表达式
@DistributedLockable(key = "#{#user.type}:#{#user.id}")
public void processUser(User user) { }

// 字符串拼接
@DistributedLockable(key = "transfer:#{#from}:#{#to}")
public void transfer(String from, String to) { }
```

## 最佳实践

### 1. 锁的粒度

- **细粒度锁**：针对具体资源加锁，如`user:123`、`order:456`
- **避免粗粒度锁**：避免使用全局锁，如`global_lock`

### 2. 锁的超时时间

- **获取锁超时**：根据业务场景设置合理的超时时间，避免无限等待
- **锁过期时间**：设置合理的过期时间，防止死锁，但要确保业务能在过期时间内完成

### 3. 异常处理

```java
@DistributedLockable(
    key = "user:#{#userId}",
    throwException = false  // 不抛出异常，返回null
)
public String processUser(Long userId) {
    // 如果获取锁失败，方法返回null
    return "processed";
}
```

### 4. 锁的命名规范

- 使用有意义的前缀：`user:`、`order:`、`inventory:`
- 包含业务标识：`transfer:account1:account2`
- 避免特殊字符：使用字母、数字、冒号、下划线

### 5. 监控和日志

```java
// 检查锁状态
if (lockUtil.isLocked("resource:123")) {
    log.warn("资源正在被处理: {}", "resource:123");
}

// 获取锁的剩余时间
long expireTime = lockUtil.getExpireTime("resource:123");
log.info("锁剩余时间: {}秒", expireTime);
```

## 注意事项

1. **Redis连接**：确保Redis服务稳定，网络连接可靠
2. **锁的释放**：使用注解或工具类会自动释放锁，手动使用时要确保在finally块中释放
3. **重入性**：同一线程可以多次获取同一把锁
4. **时钟同步**：分布式环境下确保各节点时钟同步
5. **Redis持久化**：建议配置Redis持久化，防止锁信息丢失

## 故障排查

### 常见问题

1. **获取锁失败**
   - 检查Redis连接是否正常
   - 检查锁是否被其他线程持有
   - 调整超时时间

2. **锁无法释放**
   - 检查锁的key是否正确
   - 检查是否在正确的线程中释放锁
   - 使用`forceUnlock`强制释放（谨慎使用）

3. **性能问题**
   - 减少锁的持有时间
   - 优化业务逻辑
   - 调整Redis配置

### 日志级别

```yaml
logging:
  level:
    com.nsrs.framework.lock: DEBUG  # 开启分布式锁的调试日志
```

## 版本历史

- **v1.0.0**：初始版本，支持基本的分布式锁功能
  - Redis分布式锁实现
  - 注解支持
  - 工具类支持
  - SpEL表达式支持
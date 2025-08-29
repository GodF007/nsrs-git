# API网关设计详解

## 概述

NSRS号卡资源管理系统采用API网关作为统一入口，实现请求路由、负载均衡、认证授权、限流熔断等功能。本文档详细阐述了基于Spring Cloud Gateway的API网关设计方案。

### API网关目标

- **统一入口**: 所有外部请求的统一接入点
- **服务路由**: 智能路由到后端微服务
- **安全控制**: 统一认证、授权和安全策略
- **流量管控**: 限流、熔断、降级保护
- **监控分析**: 全链路监控和API分析

## 核心枚举定义

### 1. 路由策略枚举

```java
/**
 * 路由策略枚举
 */
public enum RouteStrategy {
    ROUND_ROBIN("轮询", "按顺序轮流分发请求"),
    WEIGHTED_ROUND_ROBIN("加权轮询", "根据权重分发请求"),
    LEAST_CONNECTIONS("最少连接", "分发到连接数最少的实例"),
    RANDOM("随机", "随机选择后端实例"),
    IP_HASH("IP哈希", "根据客户端IP哈希选择实例"),
    CONSISTENT_HASH("一致性哈希", "基于一致性哈希算法"),
    HEALTH_BASED("健康优先", "优先选择健康的实例");
    
    private final String name;
    private final String description;
    
    RouteStrategy(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // getters...
}
```

### 2. 限流策略枚举

```java
/**
 * 限流策略枚举
 */
public enum RateLimitStrategy {
    TOKEN_BUCKET("令牌桶", "基于令牌桶算法限流"),
    LEAKY_BUCKET("漏桶", "基于漏桶算法限流"),
    FIXED_WINDOW("固定窗口", "固定时间窗口计数限流"),
    SLIDING_WINDOW("滑动窗口", "滑动时间窗口限流"),
    ADAPTIVE("自适应", "根据系统负载自适应限流");
    
    private final String name;
    private final String description;
    
    RateLimitStrategy(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // getters...
}
```

### 3. 认证类型枚举

```java
/**
 * 认证类型枚举
 */
public enum AuthenticationType {
    NONE("无认证", "不需要认证"),
    BASIC("基础认证", "用户名密码认证"),
    BEARER_TOKEN("Bearer令牌", "JWT或OAuth2令牌"),
    API_KEY("API密钥", "API Key认证"),
    OAUTH2("OAuth2", "OAuth2授权码模式"),
    CUSTOM("自定义", "自定义认证方式");
    
    private final String name;
    private final String description;
    
    AuthenticationType(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // getters...
}
```

### 4. 网关状态枚举

```java
/**
 * 网关状态枚举
 */
public enum GatewayStatus {
    ACTIVE("活跃", "网关正常运行"),
    INACTIVE("非活跃", "网关暂停服务"),
    MAINTENANCE("维护中", "网关维护模式"),
    OVERLOADED("过载", "网关负载过高"),
    ERROR("错误", "网关运行异常");
    
    private final String description;
    private final String detail;
    
    GatewayStatus(String description, String detail) {
        this.description = description;
        this.detail = detail;
    }
    
    // getters...
}
```

## 核心实体设计

### 1. 路由配置实体

```java
/**
 * 路由配置实体
 */
@Entity
@Table(name = "gateway_route_config")
public class GatewayRouteConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "route_id", unique = true, nullable = false)
    private String routeId;
    
    @Column(name = "route_name")
    private String routeName;
    
    @Column(name = "path_pattern", nullable = false)
    private String pathPattern; // 如: /api/sim-cards/**
    
    @Column(name = "service_name")
    private String serviceName; // 目标服务名
    
    @Column(name = "target_uri")
    private String targetUri; // 目标URI
    
    @Enumerated(EnumType.STRING)
    @Column(name = "route_strategy")
    private RouteStrategy routeStrategy;
    
    @Column(name = "predicates", columnDefinition = "JSON")
    private String predicates; // 路由断言配置
    
    @Column(name = "filters", columnDefinition = "JSON")
    private String filters; // 过滤器配置
    
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata; // 路由元数据
    
    @Column(name = "order_num")
    private Integer orderNum; // 路由优先级
    
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    
    @Column(name = "timeout_ms")
    private Integer timeoutMs; // 超时时间
    
    @Column(name = "retry_times")
    private Integer retryTimes; // 重试次数
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // constructors, getters, setters...
}
```

### 2. 限流配置实体

```java
/**
 * 限流配置实体
 */
@Entity
@Table(name = "gateway_rate_limit_config")
public class GatewayRateLimitConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "limit_key", unique = true)
    private String limitKey; // 限流键，如: route_id, user_id, ip等
    
    @Column(name = "route_id")
    private String routeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "strategy")
    private RateLimitStrategy strategy;
    
    @Column(name = "limit_count")
    private Long limitCount; // 限流数量
    
    @Column(name = "time_window_seconds")
    private Integer timeWindowSeconds; // 时间窗口（秒）
    
    @Column(name = "burst_capacity")
    private Long burstCapacity; // 突发容量
    
    @Column(name = "refill_rate")
    private Double refillRate; // 令牌补充速率
    
    @Column(name = "key_resolver")
    private String keyResolver; // 限流键解析器
    
    @Column(name = "scope")
    private String scope; // 限流范围: GLOBAL, ROUTE, USER, IP
    
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // constructors, getters, setters...
}
```

### 3. API访问日志实体

```java
/**
 * API访问日志实体
 */
@Entity
@Table(name = "gateway_access_log")
public class GatewayAccessLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "trace_id")
    private String traceId; // 链路追踪ID
    
    @Column(name = "request_id")
    private String requestId; // 请求ID
    
    @Column(name = "route_id")
    private String routeId;
    
    @Column(name = "client_ip")
    private String clientIp;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "request_method")
    private String requestMethod;
    
    @Column(name = "request_uri")
    private String requestUri;
    
    @Column(name = "request_headers", columnDefinition = "JSON")
    private String requestHeaders;
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;
    
    @Column(name = "response_status")
    private Integer responseStatus;
    
    @Column(name = "response_headers", columnDefinition = "JSON")
    private String responseHeaders;
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
    
    @Column(name = "request_time")
    private LocalDateTime requestTime;
    
    @Column(name = "response_time")
    private LocalDateTime responseTime;
    
    @Column(name = "duration_ms")
    private Long durationMs; // 请求耗时
    
    @Column(name = "target_service")
    private String targetService;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    // constructors, getters, setters...
}
```

### 4. 熔断配置实体

```java
/**
 * 熔断配置实体
 */
@Entity
@Table(name = "gateway_circuit_breaker_config")
public class GatewayCircuitBreakerConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "breaker_name", unique = true)
    private String breakerName;
    
    @Column(name = "route_id")
    private String routeId;
    
    @Column(name = "failure_rate_threshold")
    private Double failureRateThreshold; // 失败率阈值
    
    @Column(name = "slow_call_rate_threshold")
    private Double slowCallRateThreshold; // 慢调用率阈值
    
    @Column(name = "slow_call_duration_threshold")
    private Integer slowCallDurationThreshold; // 慢调用时间阈值
    
    @Column(name = "minimum_number_of_calls")
    private Integer minimumNumberOfCalls; // 最小调用次数
    
    @Column(name = "sliding_window_size")
    private Integer slidingWindowSize; // 滑动窗口大小
    
    @Column(name = "wait_duration_in_open_state")
    private Integer waitDurationInOpenState; // 熔断器打开状态等待时间
    
    @Column(name = "permitted_calls_in_half_open")
    private Integer permittedCallsInHalfOpen; // 半开状态允许的调用次数
    
    @Column(name = "fallback_uri")
    private String fallbackUri; // 降级URI
    
    @Column(name = "is_enabled")
    private Boolean isEnabled;
    
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // constructors, getters, setters...
}
```

## API网关服务实现

### 1. 动态路由服务

```java
/**
 * 动态路由服务
 */
@Service
@Slf4j
public class DynamicRouteService {
    
    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private GatewayRouteConfigRepository routeConfigRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ROUTE_CACHE_KEY = "gateway:routes";
    
    /**
     * 加载所有路由配置
     */
    @PostConstruct
    public void loadRoutes() {
        try {
            log.info("开始加载路由配置...");
            
            List<GatewayRouteConfig> configs = routeConfigRepository.findByIsEnabledTrue();
            
            for (GatewayRouteConfig config : configs) {
                RouteDefinition routeDefinition = buildRouteDefinition(config);
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            }
            
            // 刷新路由
            refreshRoutes();
            
            log.info("路由配置加载完成，共加载 {} 条路由", configs.size());
            
        } catch (Exception e) {
            log.error("加载路由配置失败", e);
        }
    }
    
    /**
     * 添加路由
     */
    public void addRoute(GatewayRouteConfig config) {
        try {
            log.info("添加路由: {}", config.getRouteId());
            
            // 保存到数据库
            config.setCreatedTime(LocalDateTime.now());
            config.setUpdatedTime(LocalDateTime.now());
            routeConfigRepository.save(config);
            
            // 构建路由定义
            RouteDefinition routeDefinition = buildRouteDefinition(config);
            
            // 添加到网关
            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            
            // 刷新路由
            refreshRoutes();
            
            // 更新缓存
            updateRouteCache();
            
            log.info("路由添加成功: {}", config.getRouteId());
            
        } catch (Exception e) {
            log.error("添加路由失败: {}", config.getRouteId(), e);
            throw new RuntimeException("添加路由失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新路由
     */
    public void updateRoute(GatewayRouteConfig config) {
        try {
            log.info("更新路由: {}", config.getRouteId());
            
            // 更新数据库
            config.setUpdatedTime(LocalDateTime.now());
            routeConfigRepository.save(config);
            
            // 删除旧路由
            routeDefinitionWriter.delete(Mono.just(config.getRouteId())).subscribe();
            
            // 添加新路由
            if (config.getIsEnabled()) {
                RouteDefinition routeDefinition = buildRouteDefinition(config);
                routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
            }
            
            // 刷新路由
            refreshRoutes();
            
            // 更新缓存
            updateRouteCache();
            
            log.info("路由更新成功: {}", config.getRouteId());
            
        } catch (Exception e) {
            log.error("更新路由失败: {}", config.getRouteId(), e);
            throw new RuntimeException("更新路由失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除路由
     */
    public void deleteRoute(String routeId) {
        try {
            log.info("删除路由: {}", routeId);
            
            // 从网关删除
            routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
            
            // 从数据库删除
            routeConfigRepository.deleteByRouteId(routeId);
            
            // 刷新路由
            refreshRoutes();
            
            // 更新缓存
            updateRouteCache();
            
            log.info("路由删除成功: {}", routeId);
            
        } catch (Exception e) {
            log.error("删除路由失败: {}", routeId, e);
            throw new RuntimeException("删除路由失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建路由定义
     */
    private RouteDefinition buildRouteDefinition(GatewayRouteConfig config) {
        RouteDefinition definition = new RouteDefinition();
        definition.setId(config.getRouteId());
        definition.setUri(URI.create(config.getTargetUri()));
        definition.setOrder(config.getOrderNum());
        
        // 构建断言
        List<PredicateDefinition> predicates = buildPredicates(config);
        definition.setPredicates(predicates);
        
        // 构建过滤器
        List<FilterDefinition> filters = buildFilters(config);
        definition.setFilters(filters);
        
        // 设置元数据
        if (config.getMetadata() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> metadata = mapper.readValue(config.getMetadata(), Map.class);
                definition.setMetadata(metadata);
            } catch (Exception e) {
                log.warn("解析路由元数据失败: {}", config.getRouteId(), e);
            }
        }
        
        return definition;
    }
    
    /**
     * 构建路由断言
     */
    private List<PredicateDefinition> buildPredicates(GatewayRouteConfig config) {
        List<PredicateDefinition> predicates = new ArrayList<>();
        
        // 路径断言
        PredicateDefinition pathPredicate = new PredicateDefinition();
        pathPredicate.setName("Path");
        pathPredicate.addArg("pattern", config.getPathPattern());
        predicates.add(pathPredicate);
        
        // 解析自定义断言
        if (config.getPredicates() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> customPredicates = mapper.readValue(config.getPredicates(), List.class);
                
                for (Map<String, Object> predicate : customPredicates) {
                    PredicateDefinition predicateDefinition = new PredicateDefinition();
                    predicateDefinition.setName((String) predicate.get("name"));
                    
                    Map<String, String> args = (Map<String, String>) predicate.get("args");
                    if (args != null) {
                        args.forEach(predicateDefinition::addArg);
                    }
                    
                    predicates.add(predicateDefinition);
                }
            } catch (Exception e) {
                log.warn("解析自定义断言失败: {}", config.getRouteId(), e);
            }
        }
        
        return predicates;
    }
    
    /**
     * 构建路由过滤器
     */
    private List<FilterDefinition> buildFilters(GatewayRouteConfig config) {
        List<FilterDefinition> filters = new ArrayList<>();
        
        // 添加默认过滤器
        
        // 1. 请求日志过滤器
        FilterDefinition logFilter = new FilterDefinition();
        logFilter.setName("RequestLogging");
        filters.add(logFilter);
        
        // 2. 超时过滤器
        if (config.getTimeoutMs() != null && config.getTimeoutMs() > 0) {
            FilterDefinition timeoutFilter = new FilterDefinition();
            timeoutFilter.setName("Timeout");
            timeoutFilter.addArg("timeout", config.getTimeoutMs() + "ms");
            filters.add(timeoutFilter);
        }
        
        // 3. 重试过滤器
        if (config.getRetryTimes() != null && config.getRetryTimes() > 0) {
            FilterDefinition retryFilter = new FilterDefinition();
            retryFilter.setName("Retry");
            retryFilter.addArg("retries", String.valueOf(config.getRetryTimes()));
            filters.add(retryFilter);
        }
        
        // 4. 负载均衡过滤器
        FilterDefinition lbFilter = new FilterDefinition();
        lbFilter.setName("LoadBalancerClient");
        filters.add(lbFilter);
        
        // 解析自定义过滤器
        if (config.getFilters() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> customFilters = mapper.readValue(config.getFilters(), List.class);
                
                for (Map<String, Object> filter : customFilters) {
                    FilterDefinition filterDefinition = new FilterDefinition();
                    filterDefinition.setName((String) filter.get("name"));
                    
                    Map<String, String> args = (Map<String, String>) filter.get("args");
                    if (args != null) {
                        args.forEach(filterDefinition::addArg);
                    }
                    
                    filters.add(filterDefinition);
                }
            } catch (Exception e) {
                log.warn("解析自定义过滤器失败: {}", config.getRouteId(), e);
            }
        }
        
        return filters;
    }
    
    /**
     * 刷新路由
     */
    private void refreshRoutes() {
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
    
    /**
     * 更新路由缓存
     */
    private void updateRouteCache() {
        try {
            List<GatewayRouteConfig> configs = routeConfigRepository.findByIsEnabledTrue();
            redisTemplate.opsForValue().set(ROUTE_CACHE_KEY, configs, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("更新路由缓存失败", e);
        }
    }
    
    /**
     * 获取路由统计信息
     */
    public RouteStatistics getRouteStatistics(String routeId, LocalDateTime startTime, LocalDateTime endTime) {
        // 从访问日志中统计路由信息
        // 这里可以使用数据库查询或者从监控系统获取数据
        
        RouteStatistics statistics = new RouteStatistics();
        statistics.setRouteId(routeId);
        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        
        // 示例统计数据
        statistics.setTotalRequests(1000L);
        statistics.setSuccessRequests(950L);
        statistics.setFailedRequests(50L);
        statistics.setAverageResponseTime(150.0);
        statistics.setP95ResponseTime(300.0);
        statistics.setP99ResponseTime(500.0);
        statistics.setThroughput(100.0);
        
        return statistics;
    }
}
```

### 2. 限流服务

```java
/**
 * 限流服务
 */
@Service
@Slf4j
public class RateLimitService {
    
    @Autowired
    private GatewayRateLimitConfigRepository rateLimitConfigRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private RedisScript<Long> rateLimitScript;
    
    /**
     * 检查是否允许请求
     */
    public boolean isAllowed(String routeId, String clientIp, String userId) {
        try {
            // 获取限流配置
            List<GatewayRateLimitConfig> configs = rateLimitConfigRepository
                .findByRouteIdAndIsEnabledTrue(routeId);
            
            if (configs.isEmpty()) {
                return true; // 没有配置限流，允许通过
            }
            
            // 检查每个限流规则
            for (GatewayRateLimitConfig config : configs) {
                if (!checkRateLimit(config, routeId, clientIp, userId)) {
                    log.warn("请求被限流: routeId={}, clientIp={}, userId={}, limitKey={}", 
                        routeId, clientIp, userId, config.getLimitKey());
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("限流检查失败", e);
            return true; // 异常时允许通过，避免影响业务
        }
    }
    
    /**
     * 检查单个限流规则
     */
    private boolean checkRateLimit(GatewayRateLimitConfig config, String routeId, String clientIp, String userId) {
        String limitKey = buildLimitKey(config, routeId, clientIp, userId);
        
        switch (config.getStrategy()) {
            case TOKEN_BUCKET:
                return checkTokenBucket(limitKey, config);
            case LEAKY_BUCKET:
                return checkLeakyBucket(limitKey, config);
            case FIXED_WINDOW:
                return checkFixedWindow(limitKey, config);
            case SLIDING_WINDOW:
                return checkSlidingWindow(limitKey, config);
            case ADAPTIVE:
                return checkAdaptiveLimit(limitKey, config);
            default:
                return true;
        }
    }
    
    /**
     * 令牌桶限流
     */
    private boolean checkTokenBucket(String limitKey, GatewayRateLimitConfig config) {
        String bucketKey = "rate_limit:token_bucket:" + limitKey;
        String lastRefillKey = bucketKey + ":last_refill";
        
        long now = System.currentTimeMillis();
        
        // 获取当前令牌数和上次补充时间
        Long currentTokens = (Long) redisTemplate.opsForValue().get(bucketKey);
        Long lastRefill = (Long) redisTemplate.opsForValue().get(lastRefillKey);
        
        if (currentTokens == null) {
            currentTokens = config.getBurstCapacity();
            lastRefill = now;
        }
        
        // 计算需要补充的令牌数
        long timePassed = now - lastRefill;
        long tokensToAdd = (long) (timePassed / 1000.0 * config.getRefillRate());
        
        // 更新令牌数
        currentTokens = Math.min(config.getBurstCapacity(), currentTokens + tokensToAdd);
        
        if (currentTokens > 0) {
            // 消耗一个令牌
            currentTokens--;
            
            // 更新Redis
            redisTemplate.opsForValue().set(bucketKey, currentTokens, Duration.ofSeconds(config.getTimeWindowSeconds()));
            redisTemplate.opsForValue().set(lastRefillKey, now, Duration.ofSeconds(config.getTimeWindowSeconds()));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 漏桶限流
     */
    private boolean checkLeakyBucket(String limitKey, GatewayRateLimitConfig config) {
        String bucketKey = "rate_limit:leaky_bucket:" + limitKey;
        String lastLeakKey = bucketKey + ":last_leak";
        
        long now = System.currentTimeMillis();
        
        // 获取当前水位和上次漏水时间
        Long currentLevel = (Long) redisTemplate.opsForValue().get(bucketKey);
        Long lastLeak = (Long) redisTemplate.opsForValue().get(lastLeakKey);
        
        if (currentLevel == null) {
            currentLevel = 0L;
            lastLeak = now;
        }
        
        // 计算漏出的水量
        long timePassed = now - lastLeak;
        long waterToLeak = (long) (timePassed / 1000.0 * config.getRefillRate());
        
        // 更新水位
        currentLevel = Math.max(0, currentLevel - waterToLeak);
        
        if (currentLevel < config.getBurstCapacity()) {
            // 加入一滴水
            currentLevel++;
            
            // 更新Redis
            redisTemplate.opsForValue().set(bucketKey, currentLevel, Duration.ofSeconds(config.getTimeWindowSeconds()));
            redisTemplate.opsForValue().set(lastLeakKey, now, Duration.ofSeconds(config.getTimeWindowSeconds()));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * 固定窗口限流
     */
    private boolean checkFixedWindow(String limitKey, GatewayRateLimitConfig config) {
        long windowStart = System.currentTimeMillis() / (config.getTimeWindowSeconds() * 1000L) * (config.getTimeWindowSeconds() * 1000L);
        String windowKey = "rate_limit:fixed_window:" + limitKey + ":" + windowStart;
        
        Long currentCount = redisTemplate.opsForValue().increment(windowKey);
        
        if (currentCount == 1) {
            // 设置过期时间
            redisTemplate.expire(windowKey, Duration.ofSeconds(config.getTimeWindowSeconds()));
        }
        
        return currentCount <= config.getLimitCount();
    }
    
    /**
     * 滑动窗口限流
     */
    private boolean checkSlidingWindow(String limitKey, GatewayRateLimitConfig config) {
        String windowKey = "rate_limit:sliding_window:" + limitKey;
        long now = System.currentTimeMillis();
        long windowStart = now - config.getTimeWindowSeconds() * 1000L;
        
        // 使用Redis的ZSET实现滑动窗口
        // 1. 移除过期的记录
        redisTemplate.opsForZSet().removeRangeByScore(windowKey, 0, windowStart);
        
        // 2. 获取当前窗口内的请求数
        Long currentCount = redisTemplate.opsForZSet().count(windowKey, windowStart, now);
        
        if (currentCount < config.getLimitCount()) {
            // 3. 添加当前请求
            redisTemplate.opsForZSet().add(windowKey, UUID.randomUUID().toString(), now);
            redisTemplate.expire(windowKey, Duration.ofSeconds(config.getTimeWindowSeconds()));
            return true;
        }
        
        return false;
    }
    
    /**
     * 自适应限流
     */
    private boolean checkAdaptiveLimit(String limitKey, GatewayRateLimitConfig config) {
        // 根据系统负载动态调整限流阈值
        double systemLoad = getSystemLoad();
        long adaptiveLimit = (long) (config.getLimitCount() * (1.0 - systemLoad));
        
        // 使用固定窗口算法，但限制数量是动态的
        long windowStart = System.currentTimeMillis() / (config.getTimeWindowSeconds() * 1000L) * (config.getTimeWindowSeconds() * 1000L);
        String windowKey = "rate_limit:adaptive:" + limitKey + ":" + windowStart;
        
        Long currentCount = redisTemplate.opsForValue().increment(windowKey);
        
        if (currentCount == 1) {
            redisTemplate.expire(windowKey, Duration.ofSeconds(config.getTimeWindowSeconds()));
        }
        
        return currentCount <= adaptiveLimit;
    }
    
    /**
     * 构建限流键
     */
    private String buildLimitKey(GatewayRateLimitConfig config, String routeId, String clientIp, String userId) {
        switch (config.getScope()) {
            case "GLOBAL":
                return "global";
            case "ROUTE":
                return "route:" + routeId;
            case "USER":
                return "user:" + (userId != null ? userId : "anonymous");
            case "IP":
                return "ip:" + clientIp;
            default:
                return config.getLimitKey();
        }
    }
    
    /**
     * 获取系统负载
     */
    private double getSystemLoad() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double load = osBean.getSystemLoadAverage();
        int processors = osBean.getAvailableProcessors();
        
        if (load < 0) {
            return 0.0; // 系统不支持负载平均值
        }
        
        return Math.min(1.0, load / processors);
    }
    
    /**
     * 获取限流统计信息
     */
    public RateLimitStatistics getRateLimitStatistics(String limitKey, LocalDateTime startTime, LocalDateTime endTime) {
        RateLimitStatistics statistics = new RateLimitStatistics();
        statistics.setLimitKey(limitKey);
        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        
        // 从Redis或数据库获取统计数据
        // 这里是示例数据
        statistics.setTotalRequests(10000L);
        statistics.setAllowedRequests(9500L);
        statistics.setRejectedRequests(500L);
        statistics.setRejectionRate(5.0);
        
        return statistics;
    }
}
```

### 3. 认证授权服务

```java
/**
 * 认证授权服务
 */
@Service
@Slf4j
public class GatewayAuthService {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private UserService userService;
    
    /**
     * 验证请求认证
     */
    public AuthenticationResult authenticate(ServerHttpRequest request, String routeId) {
        try {
            // 获取认证类型配置
            AuthenticationType authType = getAuthTypeForRoute(routeId);
            
            switch (authType) {
                case NONE:
                    return AuthenticationResult.success();
                case BASIC:
                    return authenticateBasic(request);
                case BEARER_TOKEN:
                    return authenticateBearer(request);
                case API_KEY:
                    return authenticateApiKey(request);
                case OAUTH2:
                    return authenticateOAuth2(request);
                default:
                    return AuthenticationResult.failure("不支持的认证类型");
            }
            
        } catch (Exception e) {
            log.error("认证失败", e);
            return AuthenticationResult.failure("认证异常: " + e.getMessage());
        }
    }
    
    /**
     * Basic认证
     */
    private AuthenticationResult authenticateBasic(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return AuthenticationResult.failure("缺少Basic认证头");
        }
        
        try {
            String credentials = new String(Base64.getDecoder().decode(authorization.substring(6)));
            String[] parts = credentials.split(":", 2);
            
            if (parts.length != 2) {
                return AuthenticationResult.failure("Basic认证格式错误");
            }
            
            String username = parts[0];
            String password = parts[1];
            
            // 验证用户名密码
            User user = userService.authenticate(username, password);
            if (user != null) {
                return AuthenticationResult.success(user.getId(), user.getUsername(), user.getRoles());
            } else {
                return AuthenticationResult.failure("用户名或密码错误");
            }
            
        } catch (Exception e) {
            return AuthenticationResult.failure("Basic认证解析失败");
        }
    }
    
    /**
     * Bearer Token认证
     */
    private AuthenticationResult authenticateBearer(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return AuthenticationResult.failure("缺少Bearer Token");
        }
        
        String token = authorization.substring(7);
        
        try {
            // 验证JWT Token
            if (jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                List<String> roles = jwtTokenProvider.getRolesFromToken(token);
                
                // 检查Token是否在黑名单中
                if (isTokenBlacklisted(token)) {
                    return AuthenticationResult.failure("Token已失效");
                }
                
                return AuthenticationResult.success(userId, username, roles);
            } else {
                return AuthenticationResult.failure("Token无效或已过期");
            }
            
        } catch (Exception e) {
            return AuthenticationResult.failure("Token验证失败: " + e.getMessage());
        }
    }
    
    /**
     * API Key认证
     */
    private AuthenticationResult authenticateApiKey(ServerHttpRequest request) {
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        
        if (apiKey == null) {
            apiKey = request.getQueryParams().getFirst("api_key");
        }
        
        if (apiKey == null) {
            return AuthenticationResult.failure("缺少API Key");
        }
        
        try {
            // 验证API Key
            ApiKeyInfo apiKeyInfo = validateApiKey(apiKey);
            if (apiKeyInfo != null && apiKeyInfo.isActive()) {
                return AuthenticationResult.success(apiKeyInfo.getUserId(), apiKeyInfo.getName(), apiKeyInfo.getPermissions());
            } else {
                return AuthenticationResult.failure("API Key无效或已禁用");
            }
            
        } catch (Exception e) {
            return AuthenticationResult.failure("API Key验证失败: " + e.getMessage());
        }
    }
    
    /**
     * OAuth2认证
     */
    private AuthenticationResult authenticateOAuth2(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return AuthenticationResult.failure("缺少OAuth2 Access Token");
        }
        
        String accessToken = authorization.substring(7);
        
        try {
            // 验证OAuth2 Access Token
            OAuth2TokenInfo tokenInfo = validateOAuth2Token(accessToken);
            if (tokenInfo != null && !tokenInfo.isExpired()) {
                return AuthenticationResult.success(tokenInfo.getUserId(), tokenInfo.getUsername(), tokenInfo.getScopes());
            } else {
                return AuthenticationResult.failure("OAuth2 Token无效或已过期");
            }
            
        } catch (Exception e) {
            return AuthenticationResult.failure("OAuth2认证失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查权限
     */
    public boolean hasPermission(String userId, String routeId, String method, String path) {
        try {
            // 获取用户权限
            List<String> userPermissions = getUserPermissions(userId);
            
            // 获取路由所需权限
            List<String> requiredPermissions = getRequiredPermissions(routeId, method, path);
            
            // 检查权限
            for (String required : requiredPermissions) {
                if (!userPermissions.contains(required)) {
                    log.warn("用户 {} 缺少权限: {}", userId, required);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("权限检查失败", e);
            return false;
        }
    }
    
    // 辅助方法
    private AuthenticationType getAuthTypeForRoute(String routeId) {
        // 从配置中获取路由的认证类型
        // 这里可以从数据库或配置文件中获取
        return AuthenticationType.BEARER_TOKEN; // 示例
    }
    
    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = "auth:blacklist:" + DigestUtils.md5Hex(token);
        return redisTemplate.hasKey(blacklistKey);
    }
    
    private ApiKeyInfo validateApiKey(String apiKey) {
        // 从数据库或缓存中验证API Key
        // 这里是示例实现
        return null;
    }
    
    private OAuth2TokenInfo validateOAuth2Token(String accessToken) {
        // 调用OAuth2服务验证Token
        // 这里是示例实现
        return null;
    }
    
    private List<String> getUserPermissions(String userId) {
        // 从缓存或数据库获取用户权限
        String cacheKey = "auth:permissions:" + userId;
        List<String> permissions = (List<String>) redisTemplate.opsForValue().get(cacheKey);
        
        if (permissions == null) {
            permissions = userService.getUserPermissions(userId);
            redisTemplate.opsForValue().set(cacheKey, permissions, Duration.ofMinutes(30));
        }
        
        return permissions;
    }
    
    private List<String> getRequiredPermissions(String routeId, String method, String path) {
        // 根据路由配置获取所需权限
        // 这里可以从数据库或配置文件中获取
        return Arrays.asList("sim-card:read"); // 示例
    }
}
```

## Gateway过滤器实现

### 1. 请求日志过滤器

```java
/**
 * 请求日志过滤器
 */
@Component
public class RequestLoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<RequestLoggingGatewayFilterFactory.Config> {
    
    @Autowired
    private GatewayAccessLogRepository accessLogRepository;
    
    public RequestLoggingGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String traceId = UUID.randomUUID().toString();
            
            // 添加追踪ID到请求头
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Trace-Id", traceId)
                .build();
            
            // 记录请求开始时间
            long startTime = System.currentTimeMillis();
            
            // 创建访问日志
            GatewayAccessLog accessLog = new GatewayAccessLog();
            accessLog.setTraceId(traceId);
            accessLog.setRequestId(UUID.randomUUID().toString());
            accessLog.setClientIp(getClientIp(request));
            accessLog.setUserAgent(request.getHeaders().getFirst("User-Agent"));
            accessLog.setRequestMethod(request.getMethod().name());
            accessLog.setRequestUri(request.getURI().toString());
            accessLog.setRequestTime(LocalDateTime.now());
            accessLog.setCreatedTime(LocalDateTime.now());
            
            // 记录请求头
            try {
                ObjectMapper mapper = new ObjectMapper();
                accessLog.setRequestHeaders(mapper.writeValueAsString(request.getHeaders().toSingleValueMap()));
            } catch (Exception e) {
                // 忽略序列化错误
            }
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> {
                    // 记录响应信息
                    ServerHttpResponse response = exchange.getResponse();
                    long endTime = System.currentTimeMillis();
                    
                    accessLog.setResponseStatus(response.getStatusCode().value());
                    accessLog.setResponseTime(LocalDateTime.now());
                    accessLog.setDurationMs(endTime - startTime);
                    
                    // 记录响应头
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        accessLog.setResponseHeaders(mapper.writeValueAsString(response.getHeaders().toSingleValueMap()));
                    } catch (Exception e) {
                        // 忽略序列化错误
                    }
                    
                    // 异步保存访问日志
                    CompletableFuture.runAsync(() -> {
                        try {
                            accessLogRepository.save(accessLog);
                        } catch (Exception e) {
                            // 记录日志保存失败，但不影响业务
                        }
                    });
                });
        };
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Data
    public static class Config {
        private boolean includeRequestBody = false;
        private boolean includeResponseBody = false;
        private int maxBodySize = 1024; // 最大记录的body大小
    }
}
```

### 2. 认证过滤器

```java
/**
 * 认证过滤器
 */
@Component
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {
    
    @Autowired
    private GatewayAuthService authService;
    
    public AuthenticationGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String routeId = getRouteId(exchange);
            
            // 检查是否需要认证
            if (config.isSkipAuth() || isWhitelistPath(request.getPath().value())) {
                return chain.filter(exchange);
            }
            
            // 执行认证
            AuthenticationResult authResult = authService.authenticate(request, routeId);
            
            if (!authResult.isSuccess()) {
                // 认证失败，返回401
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().add("Content-Type", "application/json");
                
                String body = "{\"error\":\"Unauthorized\",\"message\":\"" + authResult.getErrorMessage() + "\"}";
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                return response.writeWith(Mono.just(buffer));
            }
            
            // 认证成功，添加用户信息到请求头
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", authResult.getUserId())
                .header("X-Username", authResult.getUsername())
                .header("X-User-Roles", String.join(",", authResult.getRoles()))
                .build();
            
            // 检查权限
            if (config.isCheckPermission()) {
                boolean hasPermission = authService.hasPermission(
                    authResult.getUserId(), 
                    routeId, 
                    request.getMethod().name(), 
                    request.getPath().value()
                );
                
                if (!hasPermission) {
                    // 权限不足，返回403
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.FORBIDDEN);
                    response.getHeaders().add("Content-Type", "application/json");
                    
                    String body = "{\"error\":\"Forbidden\",\"message\":\"权限不足\"}";
                    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                    return response.writeWith(Mono.just(buffer));
                }
            }
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }
    
    private String getRouteId(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getId() : "unknown";
    }
    
    private boolean isWhitelistPath(String path) {
        // 白名单路径，不需要认证
        String[] whitelist = {
            "/actuator/health",
            "/api/auth/login",
            "/api/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**"
        };
        
        for (String pattern : whitelist) {
            if (path.matches(pattern.replace("**", ".*"))) {
                return true;
            }
        }
        
        return false;
    }
    
    @Data
    public static class Config {
        private boolean skipAuth = false;
        private boolean checkPermission = true;
    }
}
```

### 3. 限流过滤器

```java
/**
 * 限流过滤器
 */
@Component
public class RateLimitGatewayFilterFactory extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {
    
    @Autowired
    private RateLimitService rateLimitService;
    
    public RateLimitGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String routeId = getRouteId(exchange);
            String clientIp = getClientIp(request);
            String userId = request.getHeaders().getFirst("X-User-Id");
            
            // 检查限流
            boolean allowed = rateLimitService.isAllowed(routeId, clientIp, userId);
            
            if (!allowed) {
                // 被限流，返回429
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().add("Content-Type", "application/json");
                response.getHeaders().add("X-RateLimit-Limit", String.valueOf(config.getLimit()));
                response.getHeaders().add("X-RateLimit-Remaining", "0");
                response.getHeaders().add("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + config.getWindowSize() * 1000));
                
                String body = "{\"error\":\"Too Many Requests\",\"message\":\"请求过于频繁，请稍后再试\"}";
                DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
                return response.writeWith(Mono.just(buffer));
            }
            
            return chain.filter(exchange);
        };
    }
    
    private String getRouteId(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getId() : "unknown";
    }
    
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    @Data
    public static class Config {
        private long limit = 100; // 限制数量
        private int windowSize = 60; // 时间窗口（秒）
    }
}
```

## API网关配置

### 1. Spring Cloud Gateway配置

```yaml
# application.yml
spring:
  cloud:
    gateway:
      # 全局过滤器
      default-filters:
        - name: RequestLogging
        - name: Authentication
          args:
            skip-auth: false
            check-permission: true
        - name: RateLimit
          args:
            limit: 1000
            window-size: 60
      
      # 路由配置（动态路由会覆盖这些配置）
      routes:
        - id: sim-card-service
          uri: lb://sim-card-service
          predicates:
            - Path=/api/sim-cards/**
          filters:
            - StripPrefix=2
            - name: CircuitBreaker
              args:
                name: sim-card-cb
                fallback-uri: forward:/fallback/sim-cards
        
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=2
      
      # 全局CORS配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
      
      # 发现配置
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true

# Resilience4j熔断器配置
resilience4j:
  circuitbreaker:
    instances:
      sim-card-cb:
        failure-rate-threshold: 50
        slow-call-rate-threshold: 50
        slow-call-duration-threshold: 2s
        minimum-number-of-calls: 10
        sliding-window-size: 20
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 5
        automatic-transition-from-open-to-half-open-enabled: true

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  endpoint:
    health:
      show-details: always
    gateway:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. 网关启动类配置

```java
/**
 * API网关启动类
 */
@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties
public class GatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
    
    /**
     * 路由定位器
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // 健康检查路由
            .route("health-check", r -> r.path("/health")
                .uri("http://localhost:8080/actuator/health"))
            
            // API文档路由
            .route("api-docs", r -> r.path("/v3/api-docs/**")
                .filters(f -> f.rewritePath("/v3/api-docs/(?<segment>.*)", "/v3/api-docs/${segment}"))
                .uri("lb://sim-card-service"))
            
            .build();
    }
    
    /**
     * Redis限流脚本
     */
    @Bean
    public RedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(
            "local key = KEYS[1]\n" +
            "local limit = tonumber(ARGV[1])\n" +
            "local window = tonumber(ARGV[2])\n" +
            "local current = redis.call('GET', key)\n" +
            "if current == false then\n" +
            "  redis.call('SET', key, 1)\n" +
            "  redis.call('EXPIRE', key, window)\n" +
            "  return 1\n" +
            "else\n" +
            "  local count = tonumber(current)\n" +
            "  if count < limit then\n" +
            "    redis.call('INCR', key)\n" +
            "    return count + 1\n" +
            "  else\n" +
            "    return -1\n" +
            "  end\n" +
            "end"
        );
        script.setResultType(Long.class);
        return script;
    }
    
    /**
     * 全局异常处理
     */
    @Bean
    @Order(-1)
    public GlobalErrorWebExceptionHandler globalErrorHandler(ErrorAttributes errorAttributes) {
        return new GlobalErrorWebExceptionHandler(errorAttributes, 
            new DefaultErrorWebExceptionHandler.ErrorProperties(), null);
    }
}
```

### 3. 自定义全局异常处理

```java
/**
 * 全局异常处理器
 */
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                        WebProperties.Resources resources,
                                        ApplicationContext applicationContext) {
        super(errorAttributes, resources, applicationContext);
    }
    
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new HashMap<>();
        
        Throwable error = getError(request);
        
        if (error instanceof ResponseStatusException) {
            ResponseStatusException ex = (ResponseStatusException) error;
            errorAttributes.put("status", ex.getStatus().value());
            errorAttributes.put("error", ex.getStatus().getReasonPhrase());
            errorAttributes.put("message", ex.getReason());
        } else if (error instanceof ConnectException) {
            errorAttributes.put("status", 503);
            errorAttributes.put("error", "Service Unavailable");
            errorAttributes.put("message", "服务暂时不可用");
        } else if (error instanceof TimeoutException) {
            errorAttributes.put("status", 504);
            errorAttributes.put("error", "Gateway Timeout");
            errorAttributes.put("message", "请求超时");
        } else {
            errorAttributes.put("status", 500);
            errorAttributes.put("error", "Internal Server Error");
            errorAttributes.put("message", "内部服务器错误");
        }
        
        errorAttributes.put("timestamp", Instant.now().toString());
        errorAttributes.put("path", request.path());
        
        return errorAttributes;
    }
    
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }
    
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        
        int status = (int) errorAttributes.get("status");
        
        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorAttributes));
    }
}
```

## 降级处理

### 1. 降级控制器

```java
/**
 * 降级处理控制器
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    /**
     * SIM卡服务降级
     */
    @GetMapping("/sim-cards")
    public ResponseEntity<Map<String, Object>> simCardFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "SIM卡服务暂时不可用，请稍后重试");
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 用户服务降级
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> userFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "用户服务暂时不可用，请稍后重试");
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * 通用降级处理
     */
    @RequestMapping("/**")
    public ResponseEntity<Map<String, Object>> defaultFallback(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "服务暂时不可用，请稍后重试");
        response.put("path", request.getRequestURI());
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

## 监控和管理

### 1. 网关监控服务

```java
/**
 * 网关监控服务
 */
@Service
@Slf4j
public class GatewayMonitoringService {
    
    @Autowired
    private MeterRegistry meterRegistry;
    
    @Autowired
    private GatewayAccessLogRepository accessLogRepository;
    
    // 计数器
    private final Counter requestCounter;
    private final Counter errorCounter;
    
    // 计时器
    private final Timer requestTimer;
    
    public GatewayMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("gateway.requests.total")
            .description("Total number of requests")
            .register(meterRegistry);
        this.errorCounter = Counter.builder("gateway.errors.total")
            .description("Total number of errors")
            .register(meterRegistry);
        this.requestTimer = Timer.builder("gateway.request.duration")
            .description("Request duration")
            .register(meterRegistry);
    }
    
    /**
     * 记录请求指标
     */
    public void recordRequest(String routeId, String method, int status, long duration) {
        // 请求计数
        requestCounter.increment(
            Tags.of(
                "route", routeId,
                "method", method,
                "status", String.valueOf(status)
            )
        );
        
        // 错误计数
        if (status >= 400) {
            errorCounter.increment(
                Tags.of(
                    "route", routeId,
                    "method", method,
                    "status", String.valueOf(status)
                )
            );
        }
        
        // 请求耗时
        requestTimer.record(duration, TimeUnit.MILLISECONDS,
            Tags.of(
                "route", routeId,
                "method", method
            )
        );
    }
    
    /**
     * 获取网关统计信息
     */
    public GatewayStatistics getGatewayStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        GatewayStatistics statistics = new GatewayStatistics();
        statistics.setStartTime(startTime);
        statistics.setEndTime(endTime);
        
        // 从访问日志统计
        List<Object[]> results = accessLogRepository.getStatistics(startTime, endTime);
        
        long totalRequests = 0;
        long successRequests = 0;
        long errorRequests = 0;
        double totalDuration = 0;
        
        for (Object[] result : results) {
            totalRequests += (Long) result[0];
            if ((Integer) result[1] < 400) {
                successRequests += (Long) result[0];
            } else {
                errorRequests += (Long) result[0];
            }
            totalDuration += (Double) result[2];
        }
        
        statistics.setTotalRequests(totalRequests);
        statistics.setSuccessRequests(successRequests);
        statistics.setErrorRequests(errorRequests);
        statistics.setErrorRate(totalRequests > 0 ? (double) errorRequests / totalRequests * 100 : 0);
        statistics.setAverageResponseTime(totalRequests > 0 ? totalDuration / totalRequests : 0);
        
        return statistics;
    }
    
    /**
     * 获取路由健康状态
     */
    public List<RouteHealthStatus> getRouteHealthStatus() {
        List<RouteHealthStatus> healthStatuses = new ArrayList<>();
        
        // 从监控指标获取路由健康状态
        // 这里可以结合Actuator的健康检查
        
        return healthStatuses;
    }
}
```

### 2. 网关管理API

```java
/**
 * 网关管理API
 */
@RestController
@RequestMapping("/admin/gateway")
@Slf4j
public class GatewayAdminController {
    
    @Autowired
    private DynamicRouteService dynamicRouteService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    @Autowired
    private GatewayMonitoringService monitoringService;
    
    /**
     * 获取所有路由
     */
    @GetMapping("/routes")
    public ResponseEntity<List<GatewayRouteConfig>> getAllRoutes() {
        // 实现获取所有路由的逻辑
        return ResponseEntity.ok(new ArrayList<>());
    }
    
    /**
     * 添加路由
     */
    @PostMapping("/routes")
    public ResponseEntity<String> addRoute(@RequestBody GatewayRouteConfig config) {
        try {
            dynamicRouteService.addRoute(config);
            return ResponseEntity.ok("路由添加成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("路由添加失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新路由
     */
    @PutMapping("/routes/{routeId}")
    public ResponseEntity<String> updateRoute(@PathVariable String routeId, 
                                            @RequestBody GatewayRouteConfig config) {
        try {
            config.setRouteId(routeId);
            dynamicRouteService.updateRoute(config);
            return ResponseEntity.ok("路由更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("路由更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除路由
     */
    @DeleteMapping("/routes/{routeId}")
    public ResponseEntity<String> deleteRoute(@PathVariable String routeId) {
        try {
            dynamicRouteService.deleteRoute(routeId);
            return ResponseEntity.ok("路由删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("路由删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取网关统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<GatewayStatistics> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        GatewayStatistics statistics = monitoringService.getGatewayStatistics(startTime, endTime);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取路由健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<List<RouteHealthStatus>> getRouteHealth() {
        List<RouteHealthStatus> healthStatuses = monitoringService.getRouteHealthStatus();
        return ResponseEntity.ok(healthStatuses);
    }
}
```

## 部署脚本

### 1. Docker部署脚本

```bash
#!/bin/bash
# gateway-deploy.sh - API网关部署脚本

set -e

echo "开始部署API网关..."

# 配置变量
IMAGE_NAME="nsrs/api-gateway"
IMAGE_TAG="latest"
CONTAINER_NAME="nsrs-gateway"
PORT="8080"
PROFILE="prod"

# 构建镜像
echo "构建Docker镜像..."
docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .

# 停止并删除旧容器
echo "停止旧容器..."
docker stop ${CONTAINER_NAME} || true
docker rm ${CONTAINER_NAME} || true

# 启动新容器
echo "启动新容器..."
docker run -d \
  --name ${CONTAINER_NAME} \
  --restart unless-stopped \
  -p ${PORT}:8080 \
  -e SPRING_PROFILES_ACTIVE=${PROFILE} \
  -e JAVA_OPTS="-Xms512m -Xmx1024m" \
  --network nsrs-network \
  ${IMAGE_NAME}:${IMAGE_TAG}

# 健康检查
echo "等待服务启动..."
sleep 30

for i in {1..10}; do
  if curl -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
    echo "API网关部署成功！"
    exit 0
  fi
  echo "等待服务启动... ($i/10)"
  sleep 10
done

echo "API网关启动失败！"
exit 1
```

### 2. Kubernetes部署配置

```yaml
# gateway-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nsrs-gateway
  namespace: nsrs
  labels:
    app: nsrs-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: nsrs-gateway
  template:
    metadata:
      labels:
        app: nsrs-gateway
    spec:
      containers:
      - name: gateway
        image: nsrs/api-gateway:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: JAVA_OPTS
          value: "-Xms512m -Xmx1024m"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: nsrs-gateway-service
  namespace: nsrs
spec:
  selector:
    app: nsrs-gateway
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: nsrs-gateway-ingress
  namespace: nsrs
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.nsrs.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: nsrs-gateway-service
            port:
              number: 80
```

## API网关最佳实践

### 1. 设计原则

- **单一职责**: 网关专注于路由、认证、限流等横切关注点
- **高可用**: 多实例部署，避免单点故障
- **可扩展**: 支持动态路由配置和水平扩展
- **安全优先**: 实施多层安全防护
- **监控完善**: 全面的监控和日志记录

### 2. 性能优化

- **连接池优化**: 合理配置HTTP客户端连接池
- **缓存策略**: 缓存路由配置和认证信息
- **异步处理**: 使用响应式编程模型
- **资源限制**: 设置合理的内存和CPU限制
- **负载均衡**: 智能的负载均衡策略

### 3. 安全加固

- **HTTPS强制**: 所有外部通信使用HTTPS
- **认证授权**: 多种认证方式支持
- **输入验证**: 严格的请求参数验证
- **安全头**: 添加必要的安全响应头
- **审计日志**: 完整的安全审计日志

### 4. 运维管理

- **健康检查**: 完善的健康检查机制
- **优雅停机**: 支持优雅停机和重启
- **配置管理**: 集中化的配置管理
- **版本控制**: 路由配置的版本控制
- **回滚机制**: 快速回滚能力

### 5. 监控告警

- **关键指标**: 监控QPS、延迟、错误率等关键指标
- **实时告警**: 基于阈值的实时告警
- **链路追踪**: 分布式链路追踪
- **日志聚合**: 集中化的日志聚合和分析
- **可视化**: 直观的监控仪表板

## 总结

NSRS号卡资源管理系统的API网关设计采用了Spring Cloud Gateway作为核心框架，实现了：

1. **统一入口管理**: 所有外部请求的统一接入和路由
2. **动态路由配置**: 支持运行时动态添加、修改、删除路由
3. **多层安全防护**: 认证、授权、限流、熔断等安全机制
4. **全面监控体系**: 请求日志、性能指标、健康检查等监控功能
5. **高可用架构**: 多实例部署、故障转移、降级处理

通过合理的架构设计和最佳实践，API网关为整个微服务系统提供了稳定、安全、高效的统一入口，有效提升了系统的可维护性和可扩展性。
```
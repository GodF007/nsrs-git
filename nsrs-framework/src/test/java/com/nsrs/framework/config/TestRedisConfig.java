package com.nsrs.framework.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 测试用Mock Redis配置
 * 使用内存Map模拟Redis行为，避免依赖外部Redis服务
 *
 * @author NSRS
 */
@TestConfiguration
public class TestRedisConfig {

    // 使用内存Map模拟Redis存储
    private final ConcurrentMap<String, String> mockRedisStorage = new ConcurrentHashMap<>();

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        RedisConnectionFactory factory = Mockito.mock(RedisConnectionFactory.class);
        RedisConnection connection = Mockito.mock(RedisConnection.class);
        
        when(factory.getConnection()).thenReturn(connection);
        
        // Mock SET操作
        when(connection.setNX(any(byte[].class), any(byte[].class))).thenAnswer(invocation -> {
            String key = new String((byte[]) invocation.getArgument(0));
            String value = new String((byte[]) invocation.getArgument(1));
            return mockRedisStorage.putIfAbsent(key, value) == null;
        });
        
        // Mock GET操作
        when(connection.get(any(byte[].class))).thenAnswer(invocation -> {
            String key = new String((byte[]) invocation.getArgument(0));
            String value = mockRedisStorage.get(key);
            return value != null ? value.getBytes() : null;
        });
        
        // Mock DEL操作
        when(connection.del(any(byte[].class))).thenAnswer(invocation -> {
            String key = new String((byte[]) invocation.getArgument(0));
            return mockRedisStorage.remove(key) != null ? 1L : 0L;
        });
        
        // Mock EXPIRE操作
        when(connection.expire(any(byte[].class), any(long.class))).thenReturn(true);
        
        return factory;
    }

    @Bean("testRedisTemplate")
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = Mockito.spy(new RedisTemplate<>());
        template.setConnectionFactory(connectionFactory);
        
        // 设置序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // key和hashKey采用String序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // value和hashValue采用JSON序列化方式
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        // Mock Lua脚本执行
        when(template.execute(any(RedisScript.class), any(List.class), any())).thenAnswer(invocation -> {
            RedisScript<?> script = invocation.getArgument(0);
            List<String> keys = invocation.getArgument(1);
            Object[] args = new Object[invocation.getArguments().length - 2];
            System.arraycopy(invocation.getArguments(), 2, args, 0, args.length);
            
            return executeLuaScript(script.getScriptAsString(), keys, args);
        });
        
        // Mock hasKey方法
        when(template.hasKey(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return mockRedisStorage.containsKey(key);
        });
        
        // Mock getExpire方法
        when(template.getExpire(anyString(), any(TimeUnit.class))).thenReturn(30L);
        
        return template;
    }
    
    /**
     * 模拟Lua脚本执行
     */
    private Object executeLuaScript(String script, List<String> keys, Object[] args) {
        if (keys.isEmpty()) {
            return 0L;
        }
        
        String key = keys.get(0);
        
        // 模拟获取锁的Lua脚本
        if (script.contains("redis.call('get', KEYS[1]) == false")) {
            String value = args.length > 0 ? String.valueOf(args[0]) : "";
            
            // 如果key不存在，设置key并返回1
            if (!mockRedisStorage.containsKey(key)) {
                mockRedisStorage.put(key, value);
                return 1L;
            }
            // 如果key存在且值相同（重入），返回1
            else if (value.equals(mockRedisStorage.get(key))) {
                return 1L;
            }
            // 否则返回0
            else {
                return 0L;
            }
        }
        
        // 模拟释放锁的Lua脚本
        if (script.contains("redis.call('get', KEYS[1]) == ARGV[1]")) {
            String value = args.length > 0 ? String.valueOf(args[0]) : "";
            
            // 只有当key存在且值相同时才删除
            if (value.equals(mockRedisStorage.get(key))) {
                mockRedisStorage.remove(key);
                return 1L;
            } else {
                return 0L;
            }
        }
        
        return 0L;
    }
}
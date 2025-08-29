package com.nsrs.common.auth;

/**
 * 权限服务接口
 * 定义了认证和授权的基本操作
 */
public interface AuthService {
    
    /**
     * 验证用户是否已登录
     *
     * @return 是否已登录
     */
    boolean isAuthenticated();
    
    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    Long getCurrentUserId();
    
    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    String getCurrentUsername();
    
    /**
     * 检查当前用户是否拥有指定权限
     *
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    boolean hasPermission(String permission);
    
    /**
     * 检查当前用户是否拥有指定角色
     *
     * @param role 角色标识
     * @return 是否拥有角色
     */
    boolean hasRole(String role);
} 
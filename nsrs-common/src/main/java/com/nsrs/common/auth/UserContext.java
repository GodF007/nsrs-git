package com.nsrs.common.auth;

import lombok.Data;
import java.io.Serializable;
import java.util.Set;

/**
 * 用户上下文信息
 * 存储当前用户的基本信息和权限信息
 */
@Data
public class UserContext implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 角色列表
     */
    private Set<String> roles;
    
    /**
     * 权限列表
     */
    private Set<String> permissions;
} 
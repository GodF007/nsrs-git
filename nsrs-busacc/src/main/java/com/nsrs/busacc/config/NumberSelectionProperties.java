package com.nsrs.busacc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 选卡选号配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "nsrs.number-selection")
public class NumberSelectionProperties {
    
    /**
     * 随机号码池大小，默认30个
     */
    private Integer poolSize = 30;
    
    /**
     * 是否启用选号功能，默认启用
     */
    private Boolean enabled = true;
    
    /**
     * 号码状态过滤，默认只返回空闲状态的号码
     */
    private Integer[] allowedStatuses = {1}; // 1-空闲
    
    /**
     * 最大查询分表数量，防止查询过多分表影响性能
     */
    private Integer maxShardingTables = 10;
}
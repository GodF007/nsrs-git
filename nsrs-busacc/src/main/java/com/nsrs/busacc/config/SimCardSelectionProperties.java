package com.nsrs.busacc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SIM卡选择功能配置属性
 */
@Data
@ConfigurationProperties(prefix = "nsrs.sim-card-selection")
public class SimCardSelectionProperties {
    
    /**
     * 随机SIM卡池大小，默认30个
     */
    private Integer poolSize = 30;
    
    /**
     * 是否启用选卡功能，默认启用
     */
    private Boolean enabled = true;
    
    /**
     * SIM卡状态过滤，默认只返回已发布状态的SIM卡
     */
    private Integer[] allowedStatuses = {1}; // 1-已发布
    
    /**
     * 最大查询分表数量，防止查询过多分表影响性能
     */
    private Integer maxShardingTables = 10;
}
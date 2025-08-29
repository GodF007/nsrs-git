package com.nsrs.simcard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SimCard分表配置
 */
@Component
@ConfigurationProperties(prefix = "simcard.sharding")
public class SimCardShardingConfig {
    
    /**
     * 分表数量，默认10
     */
    private int tableCount = 10;
    
    /**
     * 表名前缀
     */
    private String tablePrefix = "sim_card_";
    
    /**
     * 是否启用分表
     */
    private boolean enabled = true;
    
    public int getTableCount() {
        return tableCount;
    }
    
    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }
    
    public String getTablePrefix() {
        return tablePrefix;
    }
    
    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 根据ICCID计算分表后缀
     * @param iccid ICCID
     * @return 分表后缀
     */
    public String getTableSuffix(String iccid) {
        if (iccid == null || iccid.isEmpty()) {
            return "0";
        }
        return String.valueOf(Math.abs(iccid.hashCode()) % tableCount);
    }
    
    /**
     * 根据ICCID获取完整表名
     * @param iccid ICCID
     * @return 完整表名
     */
    public String getTableName(String iccid) {
        if (!enabled) {
            return "sim_card";
        }
        return tablePrefix + getTableSuffix(iccid);
    }
    
    /**
     * 获取所有分表名称
     * @return 所有分表名称列表
     */
    public java.util.List<String> getAllTableNames() {
        java.util.List<String> tableNames = new java.util.ArrayList<>();
        if (!enabled) {
            tableNames.add("sim_card");
            return tableNames;
        }
        
        for (int i = 0; i < tableCount; i++) {
            tableNames.add(tablePrefix + i);
        }
        return tableNames;
    }
}
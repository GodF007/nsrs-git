//package com.nsrs.framework.sharding;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
//
//import java.util.Collection;
//import java.util.Properties;
//
///**
// * SIM卡ICCID哈希分表算法
// * 根据ICCID的哈希值对N取模进行分表
// */
//@Slf4j
//public class IccidHashShardingAlgorithm implements StandardShardingAlgorithm<String> {
//
//    private Properties props = new Properties();
//
//    /**
//     * 模值，默认为10（分10个表）
//     */
//    private int modValue;
//
//    @Override
//    public void init(Properties properties) {
//        this.props = properties;
//        this.modValue = Integer.parseInt(props.getProperty("mod.value", "10"));
//        log.info("初始化ICCID哈希分表算法 - 模值: {}", modValue);
//    }
//
//    @Override
//    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<String> shardingValue) {
//        String iccid = shardingValue.getValue();
//
//        // 处理ICCID为空的情况
//        if (iccid == null || iccid.isEmpty()) {
//            log.warn("ICCID为空，使用默认分表");
//            return availableTargetNames.iterator().next();
//        }
//
//        // 计算ICCID的哈希值对模值取模
//        int hashValue = Math.abs(iccid.hashCode() % modValue);
//
//        // 构造目标表名
//        String targetTable = shardingValue.getLogicTableName() + "_" + hashValue;
//
//        // 检查目标表是否在可用表列表中
//        for (String availableTable : availableTargetNames) {
//            if (availableTable.equals(targetTable)) {
//                log.debug("ICCID [{}] 分配到表 [{}]", iccid, availableTable);
//                return availableTable;
//            }
//        }
//
//        // 如果没有找到匹配的表，使用默认分表
//        log.warn("未找到ICCID [{}] 对应的表 [{}]，使用默认分表", iccid, targetTable);
//        return availableTargetNames.iterator().next();
//    }
//
//    @Override
//    public Collection<String> doSharding(Collection<String> availableTargetNames, RangeShardingValue<String> shardingValue) {
//        // 对于范围查询，返回所有表
//        log.debug("ICCID范围查询，返回所有分表: {}", availableTargetNames);
//        return availableTargetNames;
//    }
//
//    @Override
//    public String getType() {
//        return "ICCID_HASH";
//    }
//
//    @Override
//    public boolean isDefault() {
//        return false;
//    }
//}
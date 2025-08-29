package com.nsrs.common.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通用序列服务
 * 用于获取数据库序列值，支持单个和批量获取
 */
@Service
public class SequenceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * SIM卡ID序列名称
     */
    private static final String SIM_CARD_ID_SEQ = "sim_card_id_seq";
    
    /**
     * 号码资源ID序列名称
     */
    private static final String NUMBER_RESOURCE_ID_SEQ = "number_resource_id_seq";

    /**
     * 获取下一个序列值
     * @param sequenceName 序列名称
     * @return 序列值
     */
    public Long getNextSequenceValue(String sequenceName) {
        String sql = "SELECT get_next_sequence_value(?)";
        return jdbcTemplate.queryForObject(sql, Long.class, sequenceName);
    }
    
    /**
     * 获取SIM卡ID序列值
     * @return 下一个SIM卡ID
     */
    public Long getNextSimCardId() {
        return getNextSequenceValue(SIM_CARD_ID_SEQ);
    }
    
    /**
     * 获取号码资源ID序列值
     * @return 下一个号码资源ID
     */
    public Long getNextNumberResourceId() {
        return getNextSequenceValue(NUMBER_RESOURCE_ID_SEQ);
    }

    /**
     * 批量获取序列值
     * @param sequenceName 序列名称
     * @param batchSize 批量大小
     * @return 序列值列表
     */
    public List<Long> getBatchSequenceValues(String sequenceName, int batchSize) {
        String sql = "SELECT get_batch_sequence_values(?, ?)";
        String result = jdbcTemplate.queryForObject(sql, String.class, sequenceName, batchSize);
        
        // 解析返回的序列值字符串，可能包含方括号，格式如："[15, 16, 17, 18, 19]" 或 "15,16,17,18,19"
        if (result == null || result.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 去除可能存在的方括号和多余的空格
        String cleanResult = result.trim()
                .replaceAll("^\\[", "")  // 去除开头的方括号
                .replaceAll("\\]$", "")  // 去除结尾的方括号
                .trim();
        
        if (cleanResult.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        String[] values = cleanResult.split(",");
        return java.util.Arrays.stream(values)
                .map(String::trim)  // 去除每个值的空格
                .filter(s -> !s.isEmpty())  // 过滤空字符串
                .map(Long::valueOf)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 批量获取SIM卡ID序列值
     * @param count 数量
     * @return SIM卡ID列表
     */
    public List<Long> getNextSimCardIds(int count) {
        return getBatchSequenceValues(SIM_CARD_ID_SEQ, count);
    }
    
    /**
     * 批量获取序列值（通用方法）
     * @param sequenceName 序列名称
     * @param count 数量
     * @return 序列值列表
     */
    public List<Long> getNextSequenceValues(String sequenceName, int count) {
        return getBatchSequenceValues(sequenceName, count);
    }
}
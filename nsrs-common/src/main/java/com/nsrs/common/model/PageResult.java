package com.nsrs.common.model;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页数据
     */
    private List<T> records;
    
    /**
     * 当前页码
     */
    private long current;
    
    /**
     * 每页大小
     */
    private long size;
    
    /**
     * 总页数
     */
    private long pages;
    
    /**
     * 创建分页结果对象
     * 
     * @param total 总记录数
     * @param records 当前页数据
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果对象
     */
    public static <T> PageResult<T> of(long total, List<T> records, long current, long size) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setCurrent(current);
        result.setSize(size);
        result.setPages(total % size == 0 ? total / size : total / size + 1);
        return result;
    }
} 
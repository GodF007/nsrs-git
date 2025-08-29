package com.nsrs.common.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 * 
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 列表数据
     */
    private List<T> list;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private long pageNum;
    
    /**
     * 每页记录数
     */
    private long pageSize;
    
    /**
     * 总页数
     */
    private long pages;
    
    /**
     * 构造函数
     * 
     * @param list 列表数据
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页记录数
     */
    public PageResult(List<T> list, long total, long pageNum, long pageSize) {
        this.list = list != null ? list : Collections.emptyList();
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pageSize == 0 ? 0 : (long) Math.ceil((double) total / pageSize);
    }
}
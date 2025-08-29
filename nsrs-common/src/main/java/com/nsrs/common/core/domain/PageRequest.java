package com.nsrs.common.core.domain;

import lombok.Data;
import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 分页请求基类
 * @author system
 * @date 2025-01-20
 */
@Data
public class PageRequest<T> implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 查询条件
     */
    private T query;
    
    /**
     * 当前页码
     */
    @NotNull(message = "Current page cannot be null")
    @Min(value = 1, message = "Current page must be greater than 0")
    private Long current;

    /**
     * 每页大小
     */
    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be greater than 0")
    private Long size;


}
package com.nsrs.busacc.service;

import com.nsrs.busacc.dto.NumberSelectionRequest;
import com.nsrs.busacc.dto.NumberSelectionResponse;

/**
 * 号码选择服务接口
 */
public interface NumberSelectionService {
    
    /**
     * 根据号段查询可用号码
     * 如果未输入号段，则随机从分表中获取指定数量的号码
     * 
     * @param request 查询请求
     * @return 号码选择响应
     */
    NumberSelectionResponse selectNumbers(NumberSelectionRequest request);
    
    /**
     * 随机获取号码池
     * @param request 选号请求
     * @return 选号结果
     */
    NumberSelectionResponse getRandomPool(NumberSelectionRequest request);
}
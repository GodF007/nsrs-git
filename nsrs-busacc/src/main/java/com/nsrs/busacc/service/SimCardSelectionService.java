package com.nsrs.busacc.service;

import com.nsrs.busacc.dto.SimCardSelectionRequest;
import com.nsrs.busacc.dto.SimCardSelectionResponse;

/**
 * SIM卡选择服务接口
 */
public interface SimCardSelectionService {
    
    /**
     * 根据ICCID后缀查询可用SIM卡
     * 如果未输入ICCID后缀，则随机从分表中获取指定数量的SIM卡
     * 
     * @param request 查询请求
     * @return SIM卡选择响应
     */
    SimCardSelectionResponse selectSimCards(SimCardSelectionRequest request);
    
    /**
     * 随机获取SIM卡池
     * @param request 选卡请求
     * @return 选卡结果
     */
    SimCardSelectionResponse getRandomPool(SimCardSelectionRequest request);
}
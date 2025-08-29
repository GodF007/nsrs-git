package com.nsrs.simcard.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.simcard.entity.SimCardOperation;
import com.nsrs.simcard.model.dto.SimCardOperationDTO;
import com.nsrs.simcard.model.query.SimCardOperationQuery;
import com.nsrs.common.core.domain.PageRequest;

import java.util.List;
import java.util.Map;

/**
 * SIM卡操作记录服务接口
 */
public interface SimCardOperationService extends IService<SimCardOperation> {
    
    /**
     * 分页查询SIM卡操作记录
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<SimCardOperationDTO> pageOperation(PageRequest<SimCardOperationQuery> request);
    
    /**
     * 添加SIM卡操作记录
     * @param operationDTO 操作记录信息
     * @return 操作结果
     */
    boolean addOperation(SimCardOperationDTO operationDTO);
    
    /**
     * 获取SIM卡操作记录详情
     * @param id 操作ID
     * @return 操作记录详情
     */
    SimCardOperationDTO getOperationDetail(Long id);
    
    /**
     * 获取指定SIM卡的操作记录
     * @param cardId SIM卡ID
     * @return 操作记录列表
     */
    List<SimCardOperationDTO> getOperationsByCardId(Long cardId);
    
    /**
     * 获取指定ICCID的操作记录
     * @param iccid ICCID
     * @return 操作记录列表
     */
    List<SimCardOperationDTO> getOperationsByIccid(String iccid);
    
    /**
     * 获取最近的操作记录
     * @param limit 数量限制
     * @return 操作记录列表
     */
    List<SimCardOperationDTO> getRecentOperations(int limit);
    
    /**
     * 统计各类型操作数量
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 操作类型统计
     */
    List<Object> countByOperationType(String beginDate, String endDate);
    
    /**
     * 统计操作类型数量
     * @param query 查询条件
     * @return 统计结果
     */
    Map<String, Object> countByOperationType(SimCardOperationQuery query);
    
    /**
     * 统计操作结果状态数量
     * @param query 查询条件
     * @return 统计结果
     */
    Map<String, Object> countByResultStatus(SimCardOperationQuery query);
}
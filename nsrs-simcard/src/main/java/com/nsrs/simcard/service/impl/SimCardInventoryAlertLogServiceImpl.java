package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.entity.SimCardInventoryAlert;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.entity.SimCardInventoryAlertLog;
import com.nsrs.simcard.mapper.SimCardInventoryAlertLogMapper;
import com.nsrs.simcard.mapper.SimCardInventoryAlertMapper;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertLogDTO;
import com.nsrs.simcard.model.query.SimCardInventoryAlertLogQuery;
import com.nsrs.simcard.constants.AlertConstant;
import com.nsrs.simcard.service.SimCardInventoryAlertLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SIM Card Inventory Alert Log Service Implementation
 */
@Slf4j
@Service
public class SimCardInventoryAlertLogServiceImpl extends ServiceImpl<SimCardInventoryAlertLogMapper, SimCardInventoryAlertLog> implements SimCardInventoryAlertLogService {

    @Autowired
    private SimCardInventoryAlertMapper alertMapper;
    
    @Override
    public PageResult<SimCardInventoryAlertLogDTO> pageAlertLog(PageRequest<SimCardInventoryAlertLogQuery> request) {
        // 参数校验和默认值设置
        SimCardInventoryAlertLogQuery query = request.getQuery();
        if (query == null) {
            query = new SimCardInventoryAlertLogQuery();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<SimCardInventoryAlertLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (query.getAlertId() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getAlertId, query.getAlertId());
        }
        
        if (query.getCardTypeId() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getCardTypeId, query.getCardTypeId());
        }
        
        if (query.getSpecId() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getSpecId, query.getSpecId());
        }
        
        if (query.getSupplierId() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getSupplierId, query.getSupplierId());
        }
        
        if (query.getOrgId() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getOrgId, query.getOrgId());
        }
        
        if (query.getAlertType() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getAlertType, query.getAlertType());
        }
        
        if (query.getNotifyStatus() != null) {
            queryWrapper.eq(SimCardInventoryAlertLog::getNotifyStatus, query.getNotifyStatus());
        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(SimCardInventoryAlertLog::getAlertTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(SimCardInventoryAlertLog::getAlertTime, query.getEndDate());
        }
        
        // 按预警时间降序排序
        queryWrapper.orderByDesc(SimCardInventoryAlertLog::getAlertTime);
        
        // 执行分页查询
        Page<SimCardInventoryAlertLog> page = new Page<>(request.getCurrent(), request.getSize());
        Page<SimCardInventoryAlertLog> pageResult = page(page, queryWrapper);
        
        // 转换为DTO
        List<SimCardInventoryAlertLogDTO> logDTOList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(
            logDTOList, 
            pageResult.getTotal(), 
            pageResult.getCurrent(), 
            pageResult.getSize()
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAlertLog(SimCardInventoryAlertLogDTO logDTO) {
        // 检查预警ID是否存在
        if (logDTO.getAlertId() != null) {
            SimCardInventoryAlert alert = alertMapper.selectById(logDTO.getAlertId());
            if (alert == null) {
                throw new BusinessException(ErrorMessageEnum.ALERT_CONFIG_NOT_EXISTS.getMessage());
            }
        }
        
        // 创建日志实体并设置属性
        SimCardInventoryAlertLog log = new SimCardInventoryAlertLog();
        BeanUtils.copyProperties(logDTO, log);
        
        // 设置默认值
        if (log.getNotifyStatus() == null) {
            log.setNotifyStatus(AlertConstant.NOTIFY_STATUS_PENDING); // 默认未通知
        }
        
        if (log.getAlertTime() == null) {
            log.setAlertTime(new Date()); // 默认当前时间
        }
        
        // 保存预警日志
        return save(log);
    }
    
    @Override
    public SimCardInventoryAlertLogDTO getAlertLogDetail(Long id) {
        // 获取日志信息
        SimCardInventoryAlertLog log = getById(id);
        if (log == null) {
            return null;
        }
        
        // 转换为DTO并返回
        return convertToDTO(log);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNotifyStatus(Long id, Integer notifyStatus) {
        // 检查日志是否存在
        SimCardInventoryAlertLog log = getById(id);
        if (log == null) {
            throw new BusinessException(ErrorMessageEnum.ALERT_LOG_NOT_EXISTS.getMessage());
        }
        
        // 更新通知状态和通知时间
        log.setNotifyStatus(notifyStatus);
        if (notifyStatus == AlertConstant.NOTIFY_STATUS_SENT) {
            log.setNotifyTime(new Date());
        }
        
        return updateById(log);
    }
    
    @Override
    public List<SimCardInventoryAlertLogDTO> getLogsByAlertId(Long alertId) {
        // 查询指定预警ID的所有日志
        List<SimCardInventoryAlertLog> logList = list(new LambdaQueryWrapper<SimCardInventoryAlertLog>()
                .eq(SimCardInventoryAlertLog::getAlertId, alertId)
                .orderByDesc(SimCardInventoryAlertLog::getAlertTime));
        
        // 转换为DTO列表
        return logList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public int countUnnotifiedLogs() {
        // 统计未通知的预警日志数量
        return Math.toIntExact(count(new LambdaQueryWrapper<SimCardInventoryAlertLog>()
                .eq(SimCardInventoryAlertLog::getNotifyStatus, AlertConstant.NOTIFY_STATUS_PENDING)));
    }
    
    @Override
    public List<SimCardInventoryAlertLogDTO> getRecentLogs(int limit) {
        // 查询最近的预警日志
        List<SimCardInventoryAlertLog> logList = list(new LambdaQueryWrapper<SimCardInventoryAlertLog>()
                .orderByDesc(SimCardInventoryAlertLog::getAlertTime)
                .last("LIMIT " + limit));
        
        // 转换为DTO列表
        return logList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SimCardInventoryAlertLogDTO> getPendingNotificationLogs(int limit) {
        // 查询待处理的通知日志
        List<SimCardInventoryAlertLog> logList = list(new LambdaQueryWrapper<SimCardInventoryAlertLog>()
                .eq(SimCardInventoryAlertLog::getNotifyStatus, AlertConstant.NOTIFY_STATUS_PENDING)
                .orderByAsc(SimCardInventoryAlertLog::getAlertTime) // 按预警时间升序，优先处理早期的预警
                .last("LIMIT " + limit));
        
        // 转换为DTO列表
        return logList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SimCardInventoryAlertLogDTO> getFailedNotificationLogs(int limit) {
        // 查询失败的通知日志
        List<SimCardInventoryAlertLog> logList = list(new LambdaQueryWrapper<SimCardInventoryAlertLog>()
                .eq(SimCardInventoryAlertLog::getNotifyStatus, AlertConstant.NOTIFY_STATUS_FAILED)
                .orderByAsc(SimCardInventoryAlertLog::getAlertTime) // 按预警时间升序，优先重试早期的失败预警
                .last("LIMIT " + limit));
        
        // 转换为DTO列表
        return logList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     * @param log 预警日志实体
     * @return 预警日志DTO
     */
    private SimCardInventoryAlertLogDTO convertToDTO(SimCardInventoryAlertLog log) {
        if (log == null) {
            return null;
        }
        
        SimCardInventoryAlertLogDTO dto = new SimCardInventoryAlertLogDTO();
        BeanUtils.copyProperties(log, dto);
        
        // 查询预警名称
        if (log.getAlertId() != null) {
            SimCardInventoryAlert alert = alertMapper.selectById(log.getAlertId());
            if (alert != null) {
                dto.setAlertName(alert.getName());
            }
        }
        
        // 设置预警类型名称
        if (log.getAlertType() != null) {
            dto.setAlertTypeName(log.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY ? "Low Stock Alert" : "Overstock Alert");
        }
        
        // 设置通知状态名称
        if (log.getNotifyStatus() != null) {
            dto.setNotifyStatusName(log.getNotifyStatus() == AlertConstant.NOTIFY_STATUS_SENT ? "Notified" : "Not Notified");
        }
        
        // 这里可以添加关联数据的查询，如卡类型名称、规格名称、供应商名称、组织名称等
        // 这需要依赖其他服务，在实际开发中需要添加相应的依赖注入和调用
        
        return dto;
    }
}
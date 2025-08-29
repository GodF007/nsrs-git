package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.entity.SimCardInventoryAlert;
import com.nsrs.simcard.entity.SimCardInventoryAlertLog;
import com.nsrs.simcard.utils.SimCardConstant;
import com.nsrs.simcard.mapper.SimCardInventoryAlertMapper;
import com.nsrs.simcard.mapper.SimCardMapper;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertDTO;
import com.nsrs.simcard.model.query.SimCardInventoryAlertQuery;
import com.nsrs.simcard.constants.AlertConstant;
import com.nsrs.simcard.cache.InventoryCacheService;
import com.nsrs.simcard.service.SimCardInventoryAlertLogService;
import com.nsrs.simcard.service.SimCardInventoryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SIM Card Inventory Alert Configuration Service Implementation
 */
@Slf4j
@Service
public class SimCardInventoryAlertServiceImpl extends ServiceImpl<SimCardInventoryAlertMapper, SimCardInventoryAlert> implements SimCardInventoryAlertService {
    
    @Autowired
    private SimCardInventoryAlertLogService alertLogService;
    
    @Autowired
    private SimCardMapper simCardMapper;
    
    @Autowired
    private InventoryCacheService cacheService;
    
    @Override
    public PageResult<SimCardInventoryAlertDTO> pageAlert(PageRequest<SimCardInventoryAlertQuery> request) {
        // 参数校验和默认值设置
        SimCardInventoryAlertQuery query = request.getQuery();
        if (query == null) {
            query = new SimCardInventoryAlertQuery();
        }
        
        // 构建查询条件
        LambdaQueryWrapper<SimCardInventoryAlert> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(query.getName())) {
            queryWrapper.like(SimCardInventoryAlert::getName, query.getName());
        }
        
        if (query.getCardTypeId() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getCardTypeId, query.getCardTypeId());
        }
        
        if (query.getSpecId() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getSpecId, query.getSpecId());
        }
        
        if (query.getSupplierId() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getSupplierId, query.getSupplierId());
        }
        
        if (query.getOrgId() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getOrgId, query.getOrgId());
        }
        
        if (query.getAlertType() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getAlertType, query.getAlertType());
        }
        
        if (query.getIsActive() != null) {
            queryWrapper.eq(SimCardInventoryAlert::getIsActive, query.getIsActive());
        }
        
        // 处理日期范围查询
        if (StringUtils.hasText(query.getBeginDate())) {
            queryWrapper.ge(SimCardInventoryAlert::getCreateTime, query.getBeginDate());
        }
        
        if (StringUtils.hasText(query.getEndDate())) {
            queryWrapper.le(SimCardInventoryAlert::getCreateTime, query.getEndDate());
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(SimCardInventoryAlert::getCreateTime);
        
        // 执行分页查询
        Page<SimCardInventoryAlert> page = new Page<>(request.getCurrent(), request.getSize());
        Page<SimCardInventoryAlert> pageResult = page(page, queryWrapper);
        
        // 转换为DTO
        List<SimCardInventoryAlertDTO> alertDTOList = pageResult.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 返回分页结果
        return new PageResult<>(
            alertDTOList, 
            pageResult.getTotal(), 
            pageResult.getCurrent(), 
            pageResult.getSize()
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addAlert(SimCardInventoryAlertDTO alertDTO) {
        // 检查名称是否重复
        if (StringUtils.hasText(alertDTO.getName())) {
            LambdaQueryWrapper<SimCardInventoryAlert> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SimCardInventoryAlert::getName, alertDTO.getName());
            if (count(queryWrapper) > 0) {
                throw new BusinessException(ErrorMessageEnum.ALERT_NAME_ALREADY_EXISTS.getMessage());
            }
        }
        
        // 创建预警实体并设置属性
        SimCardInventoryAlert alert = new SimCardInventoryAlert();
        BeanUtils.copyProperties(alertDTO, alert);
        
        // 设置默认值
        if (alert.getIsActive() == null) {
            alert.setIsActive(AlertConstant.ALERT_STATUS_ACTIVE); // 默认启用
        }
        
        // 设置创建和更新时间
        Date now = new Date();
        alert.setCreateTime(now);
        alert.setUpdateTime(now);
        
        // 保存预警配置
        return save(alert);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAlert(SimCardInventoryAlertDTO alertDTO) {
        // 检查预警是否存在
        SimCardInventoryAlert existingAlert = getById(alertDTO.getId());
        if (existingAlert == null) {
            throw new BusinessException(ErrorMessageEnum.ALERT_CONFIG_NOT_EXISTS.getMessage());
        }
        
        // 检查名称是否重复
        if (StringUtils.hasText(alertDTO.getName()) && !alertDTO.getName().equals(existingAlert.getName())) {
            LambdaQueryWrapper<SimCardInventoryAlert> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SimCardInventoryAlert::getName, alertDTO.getName());
            if (count(queryWrapper) > 0) {
                throw new BusinessException(ErrorMessageEnum.ALERT_NAME_ALREADY_EXISTS.getMessage());
            }
        }
        
        // 更新预警实体
        SimCardInventoryAlert alert = new SimCardInventoryAlert();
        BeanUtils.copyProperties(alertDTO, alert);
        
        // 设置更新时间
        alert.setUpdateTime(new Date());
        
        // 更新预警配置
        return updateById(alert);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAlert(Long id) {
        // 检查预警是否存在
//        if (!getBaseMapper().delete(new LambdaQueryWrapper<SimCardInventoryAlert>().eq(SimCardInventoryAlert::getId, id))) {
//            throw new BusinessException(ErrorMessageEnum.ALERT_CONFIG_NOT_EXISTS.getMessage());
//        }
        
        // 删除预警配置
        return removeById(id);
    }
    
    @Override
    public SimCardInventoryAlertDTO getAlertDetail(Long id) {
        // 获取预警信息
        SimCardInventoryAlert alert = getById(id);
        if (alert == null) {
            return null;
        }
        
        // 转换为DTO并返回
        return convertToDTO(alert);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAlertStatus(Long id, Integer isActive) {
        // 检查预警是否存在
        SimCardInventoryAlert alert = getById(id);
        if (alert == null) {
            throw new BusinessException(ErrorMessageEnum.ALERT_CONFIG_NOT_EXISTS.getMessage());
        }
        
        // 更新状态
        alert.setIsActive(isActive);
        alert.setUpdateTime(new Date());
        
        return updateById(alert);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkInventoryAndGenerateAlerts() {
        log.info(AlertConstant.LOG_ALERT_CHECK_START);
        int alertCount = 0;
        
        try {
            // 获取所有启用的预警配置
            List<SimCardInventoryAlert> alertList = list(new LambdaQueryWrapper<SimCardInventoryAlert>()
                    .eq(SimCardInventoryAlert::getIsActive, AlertConstant.ALERT_STATUS_ACTIVE));
        
            if (alertList.isEmpty()) {
                log.info("No active alert configurations found");
                return 0;
            }
            
            // 过滤需要检查的配置（距离上次告警超过1小时）
            List<SimCardInventoryAlert> alertsToCheck = alertList.stream()
                .filter(this::shouldTriggerAlert)
                .collect(Collectors.toList());
                
            if (alertsToCheck.isEmpty()) {
                log.info("No configurations need alert check at this time");
                return 0;
            }
            
            // 批量查询库存数据
            List<Map<String, Object>> inventoryData = batchGetInventoryData(alertsToCheck);
            
            // 创建配置键到库存数据的映射
            Map<String, Map<String, Object>> inventoryMap = inventoryData.stream()
                .collect(Collectors.toMap(
                    data -> (String) data.get("config_key"),
                    data -> data
                ));
        
            Date now = new Date();
            List<SimCardInventoryAlert> alertsToUpdate = new ArrayList<>();
        
            // 检查每个预警配置
            for (SimCardInventoryAlert alert : alertsToCheck) {
                try {
                    String configKey = buildConfigKey(alert);
                    Map<String, Object> inventory = inventoryMap.get(configKey);
                    
                    if (inventory == null) {
                        // 没有找到对应的库存数据，说明该配置下没有SIM卡
                        continue;
                    }
                    
                    int availableCount = ((Number) inventory.getOrDefault("available_count", 0)).intValue();
                    int totalCount = ((Number) inventory.getOrDefault("total_count", 0)).intValue();
                    
                    boolean isAlerted = false;
                
                // 检查是否达到预警条件
                if (alert.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY) { // 低库存预警
                    if (alert.getMinThreshold() != null && availableCount <= alert.getMinThreshold()) {
                        // 生成预警日志
                        String alertMessage = String.format(AlertConstant.LOW_INVENTORY_MESSAGE_TEMPLATE, 
                                alert.getName(), availableCount, alert.getMinThreshold());
                        createAlertLog(alert, availableCount, alert.getMinThreshold(), now, alertMessage);
                        log.info(AlertConstant.LOG_ALERT_GENERATED, alert.getName(), availableCount, alert.getMinThreshold());
                        isAlerted = true;
                        alertCount++;
                    }
                } else if (alert.getAlertType() == AlertConstant.ALERT_TYPE_HIGH_INVENTORY) { // 超量预警
                    if (alert.getMaxThreshold() != null && totalCount >= alert.getMaxThreshold()) {
                        // 生成预警日志
                        String alertMessage = String.format(AlertConstant.HIGH_INVENTORY_MESSAGE_TEMPLATE, 
                                alert.getName(), totalCount, alert.getMaxThreshold());
                        createAlertLog(alert, totalCount, alert.getMaxThreshold(), now, alertMessage);
                        log.info(AlertConstant.LOG_ALERT_GENERATED, alert.getName(), totalCount, alert.getMaxThreshold());
                        isAlerted = true;
                        alertCount++;
                    }
                }
                
                    // 记录需要更新告警时间的配置
                    if (isAlerted) {
                        alert.setLastAlertTime(now);
                        alertsToUpdate.add(alert);
                    }
                    
                } catch (Exception e) {
                    log.error("Error checking alert for config: {}", alert.getName(), e);
                }
            }
            
            // 批量更新告警时间
            if (!alertsToUpdate.isEmpty()) {
                try {
                    updateBatchById(alertsToUpdate);
                } catch (Exception e) {
                    log.error("Error updating alert configurations", e);
                }
            }
        
            log.info(AlertConstant.LOG_ALERT_CHECK_COMPLETE, alertCount);
            return alertCount;
        
        } catch (Exception e) {
            log.error(AlertConstant.LOG_ALERT_TASK_ERROR, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
       * 批量获取库存数据（带缓存）
       * @param alerts 预警配置列表
       * @return 库存数据列表
       */
      private List<Map<String, Object>> batchGetInventoryData(List<SimCardInventoryAlert> alerts) {
          List<Map<String, Object>> result = new ArrayList<>();
          List<SimCardInventoryAlert> alertsNeedQuery = new ArrayList<>();
          
          // 检查缓存
          for (SimCardInventoryAlert alert : alerts) {
              String configKey = buildConfigKey(alert);
              Map<String, Object> cachedData = cacheService.get(configKey);
              
              if (cachedData != null) {
                  // 缓存命中
                  Map<String, Object> resultData = new HashMap<>(cachedData);
                  resultData.put("config_key", configKey);
                  result.add(resultData);
              } else {
                  // 缓存未命中
                  alertsNeedQuery.add(alert);
              }
          }
          
          // 批量查询未缓存的数据
          if (!alertsNeedQuery.isEmpty()) {
              List<Map<String, Object>> paramsList = alertsNeedQuery.stream()
                  .map(this::buildQueryParams)
                  .collect(Collectors.toList());
                  
              List<Map<String, Object>> queryResult = simCardMapper.batchCountInventoryForAlerts(paramsList);
              
              // 更新缓存并添加到结果中
              for (Map<String, Object> data : queryResult) {
                  String configKey = (String) data.get("config_key");
                  
                  // 缓存数据（不包含config_key）
                  Map<String, Object> cacheData = new HashMap<>(data);
                  cacheData.remove("config_key");
                  cacheService.put(configKey, cacheData);
                  
                  result.add(data);
              }
              
              log.debug("Queried {} inventory records from database, {} from cache", 
                  queryResult.size(), alerts.size() - alertsNeedQuery.size());
          } else {
              log.debug("All {} inventory records retrieved from cache", alerts.size());
          }
          
          return result;
       }
    
    /**
      * 构建配置键
      * @param alert 预警配置
      * @return 配置键
      */
     private String buildConfigKey(SimCardInventoryAlert alert) {
         return String.format("%s_%s_%s_%s", 
             alert.getCardTypeId() != null ? alert.getCardTypeId() : "null",
             alert.getSpecId() != null ? alert.getSpecId() : "null",
             alert.getSupplierId() != null ? alert.getSupplierId() : "null",
             alert.getOrgId() != null ? alert.getOrgId() : "null");
     }
     
     /**
      * 清理过期缓存
      */
     public void clearExpiredCache() {
         if (cacheService.isEnabled()) {
             cacheService.clearExpired();
             log.debug("Cleared expired cache entries");
         }
     }
     
     /**
      * 清理所有缓存
      */
     public void clearAllCache() {
         if (cacheService.isEnabled()) {
             cacheService.clearAll();
             log.debug("Cleared all inventory cache");
         }
      }
     
     /**
      * 根据SIM卡信息清理相关缓存
      * 当SIM卡状态发生变化时调用此方法
      * @param simCard SIM卡信息
      */
       public void clearCacheBySimCard(SimCard simCard) {
           if (cacheService.isEnabled()) {
               String configKey = String.format("%s_%s_%s_%s", 
                   simCard.getCardTypeId() != null ? simCard.getCardTypeId() : "null",
                   simCard.getSpecId() != null ? simCard.getSpecId() : "null",
                   simCard.getSupplierId() != null ? simCard.getSupplierId() : "null",
                   simCard.getOrganizationId() != null ? simCard.getOrganizationId() : "null");
               
               cacheService.remove(configKey);
               log.debug("Cleared cache for config key: {}", configKey);
           }
       }
    
    /**
     * 创建预警日志
     * @param alert 预警配置
     * @param currentCount 当前数量
     * @param threshold 阈值
     * @param alertTime 预警时间
     * @param alertMessage 预警消息
     */
    private void createAlertLog(SimCardInventoryAlert alert, int currentCount, int threshold, Date alertTime, String alertMessage) {
        SimCardInventoryAlertLog alertLog = new SimCardInventoryAlertLog();
        
        alertLog.setAlertId(alert.getId());
        alertLog.setAlertTime(alertTime);
        alertLog.setCardTypeId(alert.getCardTypeId());
        alertLog.setSpecId(alert.getSpecId());
        alertLog.setSupplierId(alert.getSupplierId());
        alertLog.setOrgId(alert.getOrgId());
        alertLog.setCurrentCount(currentCount);
        alertLog.setThreshold(threshold);
        alertLog.setAlertType(alert.getAlertType());
        alertLog.setNotifyStatus(AlertConstant.NOTIFY_STATUS_PENDING);
        alertLog.setRemark(alertMessage);
        
        // 保存预警日志
        alertLogService.save(alertLog);
    }
    
    /**
     * 获取可用库存数量（只统计已发布状态的SIM卡）
     * @param alert 告警配置
     * @return 可用库存数量
     */
    private int getAvailableInventoryCount(SimCardInventoryAlert alert) {
        Map<String, Object> params = buildQueryParams(alert);
        
        // 使用专门的告警统计方法
        Integer count = simCardMapper.countAvailableByAlertConditions(params);
        return count != null ? count : 0;
    }
    
    /**
     * 构建查询参数
     * @param alert 告警配置
     * @return 查询参数Map
     */
    private Map<String, Object> buildQueryParams(SimCardInventoryAlert alert) {
        Map<String, Object> params = new HashMap<>();
        
        if (alert.getCardTypeId() != null) {
            params.put("cardTypeId", alert.getCardTypeId());
        }
        if (alert.getSpecId() != null) {
            params.put("specId", alert.getSpecId());
        }
        if (alert.getSupplierId() != null) {
            params.put("supplierId", alert.getSupplierId());
        }
        if (alert.getOrgId() != null) {
            params.put("orgId", alert.getOrgId());
        }
        
        return params;
    }
    
    /**
     * 获取总库存数量（统计所有状态的SIM卡）
     * @param alert 告警配置
     * @return 总库存数量
     */
    private int getTotalInventoryCount(SimCardInventoryAlert alert) {
        Map<String, Object> params = buildQueryParams(alert);
        
        // 使用专门的告警统计方法
        Integer count = simCardMapper.countTotalByAlertConditions(params);
        return count != null ? count : 0;
    }
    
    /**
     * 判断是否应该触发告警（避免频繁告警）
     * @param alert 告警配置
     * @return 是否应该触发告警
     */
    private boolean shouldTriggerAlert(SimCardInventoryAlert alert) {
        if (alert.getLastAlertTime() == null) {
            return true;
        }
        
        // 同一告警配置在1小时内不重复告警
        long timeDiff = System.currentTimeMillis() - alert.getLastAlertTime().getTime();
        return timeDiff > 3600000; // 1小时 = 3600000毫秒
    }
    
    @Override
    public List<SimCardInventoryAlertDTO> listActiveAlerts() {
        // 查询所有启用的预警配置
        List<SimCardInventoryAlert> alertList = list(new LambdaQueryWrapper<SimCardInventoryAlert>()
                .eq(SimCardInventoryAlert::getIsActive, AlertConstant.ALERT_STATUS_ACTIVE)
                .orderByDesc(SimCardInventoryAlert::getCreateTime));
        
        // 转换为DTO列表
        return alertList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     * @param alert 预警实体
     * @return 预警DTO
     */
    private SimCardInventoryAlertDTO convertToDTO(SimCardInventoryAlert alert) {
        if (alert == null) {
            return null;
        }
        
        SimCardInventoryAlertDTO dto = new SimCardInventoryAlertDTO();
        BeanUtils.copyProperties(alert, dto);
        
        // 设置预警类型名称
        if (alert.getAlertType() != null) {
            dto.setAlertTypeName(alert.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY ? "Low Stock Alert" : "Overstock Alert");
        }
        
        // 设置状态名称
        if (alert.getIsActive() != null) {
            dto.setStatusName(alert.getIsActive() == AlertConstant.ALERT_STATUS_ACTIVE ? "Enabled" : "Disabled");
        }
        
        // 这里可以添加关联数据的查询，如卡类型名称、规格名称、供应商名称、组织名称等
        // 这需要依赖其他服务，在实际开发中需要添加相应的依赖注入和调用
        
        return dto;
    }
    
    @Override
    public String triggerManualAlertCheck() {
        try {
            log.info("Manual alert check triggered via API");
            
            // 清理过期缓存
            clearExpiredCache();
            
            // 执行库存检查并生成告警
            int alertCount = checkInventoryAndGenerateAlerts();
            
            String result = String.format("Manual alert check completed successfully, generated %d alerts", alertCount);
            log.info(result);
            
            return result;
            
        } catch (Exception e) {
            String errorMsg = "Failed to execute manual alert check: " + e.getMessage();
            log.error(errorMsg, e);
            throw new BusinessException(errorMsg);
        }
    }
}
package com.nsrs.binding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.binding.dto.BatchUnbindRequest;

import com.nsrs.binding.entity.NumberImsiBinding;
import com.nsrs.binding.mapper.NumberImsiBindingMapper;
// 注释：不再需要ImsiIccidMappingService
// import com.nsrs.simcard.service.ImsiIccidMappingService;
import com.nsrs.binding.query.NumberImsiBindingQuery;
import com.nsrs.binding.service.NumberImsiBindingService;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.enums.NumberStatusEnum;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.utils.SequenceService;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.service.NumberResourceService;
import com.nsrs.msisdn.vo.NumberResourceVO;
import com.nsrs.binding.constants.BindingConstants;

import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.enums.ErrorMessageEnum;
import com.nsrs.simcard.enums.ImsiStatusEnum;
import com.nsrs.simcard.enums.SimCardStatusEnum;
import com.nsrs.simcard.service.ImsiResourceService;
import com.nsrs.simcard.service.SimCardService;
import com.nsrs.simcard.model.dto.ImsiResourceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 号码与IMSI绑定服务实现类
 */
@Service
public class NumberImsiBindingServiceImpl extends ServiceImpl<NumberImsiBindingMapper, NumberImsiBinding> implements NumberImsiBindingService {
    
    private static final Logger logger = LoggerFactory.getLogger(NumberImsiBindingServiceImpl.class);
    
    @Autowired
    private NumberImsiBindingMapper bindingMapper;
    
    @Autowired
    private NumberResourceService numberResourceService;
    
    @Autowired
    private ImsiResourceService imsiResourceService;

    @Autowired
    private SimCardService simCardService;
    
    @Autowired
    private SequenceService sequenceService;
    
    // 注释：不再需要ImsiIccidMappingService，因为选卡选号时直接提供iccid
    // @Autowired
    // private ImsiIccidMappingService imsiIccidMappingService;

    @Override
    public PageResult<NumberImsiBinding> page(PageRequest<NumberImsiBindingQuery> request) {
        // 构建查询条件
        LambdaQueryWrapper<NumberImsiBinding> queryWrapper = new LambdaQueryWrapper<>();
        
        NumberImsiBindingQuery query = request.getQuery();
        if (query != null) {
            // 号码查询条件（支持精确查询和前缀查询）
            if (StringUtils.hasText(query.getNumber())) {
                String number = query.getNumber();
                if (number.length() < 11) {
                    // 前缀查询使用范围查询触发分表算法优化
                    String rangeStart = number + "00000000000".substring(0, 11 - number.length());
                    String rangeEnd = number + "99999999999".substring(0, 11 - number.length());
                    queryWrapper.between(NumberImsiBinding::getNumber, rangeStart, rangeEnd);
                } else {
                    // 完整号码使用精确查询
                    queryWrapper.eq(NumberImsiBinding::getNumber, number);
                }
            }
            
            // IMSI查询条件
            if (StringUtils.hasText(query.getImsi())) {
                queryWrapper.eq(NumberImsiBinding::getImsi, query.getImsi());
            }
            
            // ICCID查询条件
            if (StringUtils.hasText(query.getIccid())) {
                queryWrapper.eq(NumberImsiBinding::getIccid, query.getIccid());
            }
            
            // 绑定状态查询条件
            if (query.getBindingStatus() != null) {
                queryWrapper.eq(NumberImsiBinding::getBindingStatus, query.getBindingStatus());
            }
            
            // 绑定类型查询条件
            if (query.getBindingType() != null) {
                queryWrapper.eq(NumberImsiBinding::getBindingType, query.getBindingType());
            }
            
            // 订单ID查询条件
            if (query.getOrderId() != null) {
                queryWrapper.eq(NumberImsiBinding::getOrderId, query.getOrderId());
            }
            
            // 操作用户ID查询条件
            if (query.getOperatorUserId() != null) {
                queryWrapper.eq(NumberImsiBinding::getOperatorUserId, query.getOperatorUserId());
            }
            
            // 时间范围查询条件
            if (query.getStartTime() != null) {
                queryWrapper.ge(NumberImsiBinding::getCreateTime, query.getStartTime());
            }
            if (query.getEndTime() != null) {
                queryWrapper.le(NumberImsiBinding::getCreateTime, query.getEndTime());
            }
        }
        
        // 默认按创建时间倒序
        queryWrapper.orderByDesc(NumberImsiBinding::getCreateTime);
        
        // 执行分页查询，利用ShardingSphere自动路由到对应分表
        Page<NumberImsiBinding> page = new Page<>(request.getCurrent(), request.getSize());
        IPage<NumberImsiBinding> resultPage = this.page(page, queryWrapper);
        
        PageResult<NumberImsiBinding> result = new PageResult<>();
        result.setList(resultPage.getRecords());
        result.setTotal(resultPage.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((resultPage.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }

    @Override
    public PageResult<NumberImsiBinding> prefixQuery(IPage<NumberImsiBinding> page, String numberPrefix, Map<String, Object> params) {
        // 构建优化的前缀查询条件
        LambdaQueryWrapper<NumberImsiBinding> queryWrapper = buildPrefixQueryWrapper(numberPrefix, params);
        
        // 执行分页查询，利用分表算法进行路由优化
        IPage<NumberImsiBinding> result = this.page(page, queryWrapper);
        
        // 转换为PageResult
        return new PageResult<>(result.getRecords(), result.getTotal(), 
                               (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 构建前缀查询条件（优化版本，利用分表算法）
     */
    private LambdaQueryWrapper<NumberImsiBinding> buildPrefixQueryWrapper(String numberPrefix, Map<String, Object> params) {
        LambdaQueryWrapper<NumberImsiBinding> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(numberPrefix)) {
            // 对于前缀查询，使用范围查询来触发分表算法的优化逻辑
            if (numberPrefix.length() < 11) {
                // 构造范围查询，让分表算法能够识别前缀并路由到指定分表
                // rangeStart: 前缀 + 补0到11位
                String rangeStart = numberPrefix + "00000000000".substring(0, 11 - numberPrefix.length());
                // rangeEnd: 前缀 + 补9到11位
                String rangeEnd = numberPrefix + "99999999999".substring(0, 11 - numberPrefix.length());
                queryWrapper.between(NumberImsiBinding::getNumber, rangeStart, rangeEnd);
            } else {
                // 完整号码使用精确查询
                queryWrapper.eq(NumberImsiBinding::getNumber, numberPrefix);
            }
        }
        
        // 添加其他查询条件
        if (params != null) {
            if (params.containsKey("imsi") && params.get("imsi") != null) {
                queryWrapper.eq(NumberImsiBinding::getImsi, params.get("imsi").toString());
            }
            
            if (params.containsKey("bindingStatus") && params.get("bindingStatus") != null) {
                queryWrapper.eq(NumberImsiBinding::getBindingStatus, params.get("bindingStatus"));
            }
            
            if (params.containsKey("bindingType") && params.get("bindingType") != null) {
                queryWrapper.eq(NumberImsiBinding::getBindingType, params.get("bindingType"));
            }
            
            if (params.containsKey("orderId") && params.get("orderId") != null) {
                queryWrapper.eq(NumberImsiBinding::getOrderId, params.get("orderId"));
            }
        }
        
        // 默认按创建时间倒序
        queryWrapper.orderByDesc(NumberImsiBinding::getCreateTime);
        
        return queryWrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> bind(String number, String imsi, String iccid, Long orderId,
                               Integer bindingType, Long operatorUserId, String remark) {
        // 检查参数
        if (!StringUtils.hasText(number) || !StringUtils.hasText(imsi) || !StringUtils.hasText(iccid)) {
            logger.warn("Parameter validation failed for binding: number={}, imsi={}, iccid={}", number, imsi, iccid);
            return CommonResult.failed(ErrorMessageEnum.BINDING_PARAMS_REQUIRED.getMessage());
        }
        
        // 检查是否已绑定
        if (isNumberBound(number)) {
            logger.warn("Number already bound: {}", number);
            return CommonResult.failed(ErrorMessageEnum.NUMBER_ALREADY_BOUND.getMessage());
        }
        if (isIccidBound(iccid)) {
            logger.warn("ICCID already bound: {}", iccid);
            return CommonResult.failed(BindingConstants.ErrorMessage.ICCID_ALREADY_BOUND);
        }
        
        try {
            // 注释：选卡选号时iccid、号码、imsi信息都有了，不需要通过ImsiIccidMapping查询
            // String iccid = imsiIccidMappingService.getIccidByImsi(imsi);
            // if (iccid == null) {
            //     logger.warn("IMSI-ICCID mapping not found for IMSI: {}", imsi);
            //     return CommonResult.failed("IMSI-ICCID mapping not found for IMSI: " + imsi);
            // }
            
            // 获取号码资源信息，获取number_id
            NumberResourceVO numberResource = numberResourceService.getByNumber(number);
            if (numberResource == null) {
                logger.warn("Number resource not found: {}", number);
                return CommonResult.failed("Number resource not found: " + number);
            }
            
            // 获取IMSI资源信息，获取imsi_id
            ImsiResourceDTO imsiResource = imsiResourceService.getImsiResourceByImsi(imsi);
            if (imsiResource == null) {
                logger.warn("IMSI resource not found: {}", imsi);
                return CommonResult.failed("IMSI resource not found: " + imsi);
            }
            
            // 创建绑定关系
            NumberImsiBinding binding = new NumberImsiBinding();
            // 生成全局ID
            Long globalId = sequenceService.getNextSequenceValue(BindingConstants.SequenceName.NUMBER_IMSI_BINDING_ID_SEQ);
            binding.setBindingId(globalId);
            binding.setNumberId(numberResource.getNumberId()); // 设置号码ID
            binding.setNumber(number);
            binding.setImsiId(imsiResource.getImsiId()); // 设置IMSI ID
            binding.setImsi(imsi);
            binding.setIccid(iccid);
            binding.setOrderId(orderId); // 设置订单ID
            binding.setBindingType(bindingType);
            binding.setBindingStatus(BindingConstants.BindingStatus.BOUND); // 绑定状态
            binding.setOperatorUserId(operatorUserId);
            binding.setRemark(remark);
            binding.setBindingTime(new Date()); // 设置绑定时间
            binding.setCreateTime(new Date());
            binding.setUpdateTime(new Date());
            
            // 保存绑定关系
            boolean savedBinding = save(binding);
            if (!savedBinding) {
                logger.error("Failed to save binding relationship: number={}, imsi={}", number, imsi);
                return CommonResult.failed(BindingConstants.ErrorMessage.SAVE_BINDING_FAILED);
            }
            
            // 记录成功绑定日志
            logger.info(BindingConstants.LogMessage.BINDING_SUCCESS, number, imsi, binding.getBindingId());
            
            // 更新号码资源表中的ICCID字段和状态
            // 由于分表策略，需要通过号码查询而不是ID查询
            if (numberResource != null) {
                // 使用LambdaUpdateWrapper根据号码更新ICCID，利用分表路由
                LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(NumberResource::getNumber, number)
                            .set(NumberResource::getIccid, iccid)
                            .set(NumberResource::getUpdateTime, new Date());
                boolean updatedNumber = numberResourceService.update(updateWrapper);
                if (!updatedNumber) {
                    logger.warn(BindingConstants.ErrorMessage.UPDATE_NUMBER_RESOURCE_ICCID_FAILED + ", number: {}", number);
                    // 继续执行，不影响绑定流程
                }
                
                // 更新号码状态为激活
                try {
                    boolean statusUpdateResult = numberResourceService.updateStatus(
                            number, 
                            NumberStatusEnum.ACTIVATED.getCode()
                    );
                    
                    if (statusUpdateResult) {
                        logger.info("Number status updated to ACTIVATED successfully: {}", number);
                    } else {
                        logger.warn("Failed to update number status to ACTIVATED: {}", number);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating number status: {}", number, e);
                }
            } else {
                logger.warn(BindingConstants.ErrorMessage.NUMBER_RESOURCE_NOT_FOUND + ", number: {}", number);
            }
            
            // 更新IMSI状态为绑定
            try {
                List<String> imsiList = new ArrayList<>();
                imsiList.add(imsi);
                boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(
                        imsiList, 
                        ImsiStatusEnum.BOUND.getCode()
                );
                
                if (imsiStatusUpdateResult) {
                    logger.info("IMSI status updated to BOUND successfully: {}", imsi);
                } else {
                    logger.warn("Failed to update IMSI status to BOUND: {}", imsi);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while updating IMSI status: {}", imsi, e);
            }
            
            // 更新SimCard状态为激活
            try {
                boolean simCardStatusUpdateResult = simCardService.updateStatusByIccid(
                        iccid, 
                        SimCardStatusEnum.ACTIVATED.getCode()
                );
                
                if (simCardStatusUpdateResult) {
                    logger.info("SimCard status updated to ACTIVATED successfully for ICCID: {}", iccid);
                } else {
                    logger.warn("Failed to update SimCard status to ACTIVATED for ICCID: {}", iccid);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while updating SimCard status for ICCID: {}", iccid, e);
            }
            
            return CommonResult.success();
        } catch (Exception e) {
            logger.error("Error occurred during number and IMSI binding process: number={}, imsi={}", number, imsi, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> unbindByNumber(String number, Long operatorUserId, String remark) {
        // 检查参数
        if (!StringUtils.hasText(number)) {
            logger.warn("Parameter validation failed: number is null or empty");
            return CommonResult.failed(BindingConstants.ErrorMessage.NUMBER_REQUIRED);
        }
        
        try {
            // 查询绑定关系
            NumberImsiBinding binding = getByNumber(number);
            if (binding == null) {
                logger.warn("Binding relationship not found for number: {}", number);
                return CommonResult.failed(BindingConstants.ErrorMessage.BINDING_NOT_FOUND);
            }
            
            // 检查绑定状态
            if (BindingConstants.BindingStatus.UNBOUND.equals(binding.getBindingStatus())) {
                logger.warn("Number is already unbound: {}", number);
                return CommonResult.failed("Number is already unbound");
            }
            
            String imsi = binding.getImsi();
            
            // 更新绑定状态 - 使用号码作为分表路由条件
            LambdaUpdateWrapper<NumberImsiBinding> bindingUpdateWrapper = new LambdaUpdateWrapper<>();
            bindingUpdateWrapper.eq(NumberImsiBinding::getNumber, number)
                               .set(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.UNBOUND)
                               .set(NumberImsiBinding::getOperatorUserId, operatorUserId)
                               .set(NumberImsiBinding::getRemark, remark)
                               .set(NumberImsiBinding::getUpdateTime, new Date())
                               .set(NumberImsiBinding::getUnbindTime, new Date());
            
            boolean updated = this.update(bindingUpdateWrapper);
            if (!updated) {
                logger.error("Failed to update binding status for number: {}", number);
                return CommonResult.failed(BindingConstants.ErrorMessage.UPDATE_BINDING_STATUS_FAILED);
            }
            
            // 记录成功解绑日志
            logger.info(BindingConstants.LogMessage.UNBINDING_SUCCESS, binding.getBindingId(), number, imsi);
            
            // 清除号码资源表中的ICCID字段
            NumberResourceVO numberResource = numberResourceService.getByNumber(number);
            if (numberResource != null) {
                // 使用LambdaUpdateWrapper根据号码清除ICCID，利用分表路由
                LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(NumberResource::getNumber, number)
                            .set(NumberResource::getIccid, null)
                            .set(NumberResource::getUpdateTime, new Date());
                boolean updatedNumber = numberResourceService.update(updateWrapper);
                if (!updatedNumber) {
                    logger.warn(BindingConstants.ErrorMessage.CLEAR_NUMBER_RESOURCE_ICCID_FAILED + ", number: {}", number);
                    // 继续执行，不影响解绑流程
                }
                
                // 更新号码状态为空闲
                try {
                    boolean statusUpdateResult = numberResourceService.updateStatus(
                             number, 
                             NumberStatusEnum.IDLE.getCode()
                     );
                    
                    if (statusUpdateResult) {
                        logger.info("Number status updated to IDLE successfully: {}", number);
                    } else {
                        logger.warn("Failed to update number status to IDLE: {}", number);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating number status: {}", number, e);
                }
            }
            
            // 更新IMSI状态为空闲
            if (imsi != null) {
                try {
                    List<String> imsiList = new ArrayList<>();
                    imsiList.add(imsi);
                    boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(
                            imsiList, 
                            ImsiStatusEnum.IDLE.getCode()
                    );
                    
                    if (imsiStatusUpdateResult) {
                        logger.info("IMSI status updated to IDLE successfully: {}", imsi);
                    } else {
                        logger.warn("Failed to update IMSI status to IDLE: {}", imsi);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating IMSI status: {}", imsi, e);
                }
            }
            
            // 更新SIM卡状态为已发布
            String iccid = binding.getIccid();
            if (StringUtils.hasText(iccid)) {
                try {
                    boolean simCardUpdateResult = simCardService.updateStatusByIccid(
                        iccid, SimCardStatusEnum.PUBLISHED.getCode());
                    if (simCardUpdateResult) {
                        logger.info("Successfully updated SIM card status to PUBLISHED for ICCID: {}", iccid);
                    } else {
                        logger.error("Failed to update SIM card status to PUBLISHED for ICCID: {}", iccid);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating SIM card status: {}", iccid, e);
                }
            } else {
                logger.warn("ICCID not found in binding relationship for number: {}", number);
            }
            
            return CommonResult.success();
        } catch (Exception e) {
            logger.error("Error occurred during number unbinding process: number={}", number, e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> batchBind(List<NumberImsiBinding> bindingList, Long operatorUserId) {
        if (CollectionUtils.isEmpty(bindingList)) {
            return CommonResult.failed(ErrorMessageEnum.BINDING_LIST_EMPTY.getMessage());
        }
        
        // 批量获取全局ID
        List<Long> globalIds = sequenceService.getBatchSequenceValues(BindingConstants.SequenceName.NUMBER_IMSI_BINDING_ID_SEQ, bindingList.size());
        
        int successCount = 0;
        List<String> numbersToUpdate = new ArrayList<>();
        List<String> imsiListToUpdate = new ArrayList<>();
        
        for (int i = 0; i < bindingList.size(); i++) {
            NumberImsiBinding binding = bindingList.get(i);
            try {
                // 检查是否已绑定
                if (isNumberBound(binding.getNumber())) {
                    logger.warn("Number already bound: {}", binding.getNumber());
                    continue;
                }
                if (isIccidBound(binding.getIccid())) {
                    logger.warn("ICCID already bound: {}", binding.getIccid());
                    continue;
                }
                
                // 注释：选卡选号时iccid、号码、imsi信息都有了，不需要通过ImsiIccidMapping查询
                // String imsi = binding.getImsi();
                // String iccid = imsiIccidMappingService.getIccidByImsi(imsi);
                // if (iccid == null) {
                //     logger.warn("IMSI-ICCID mapping not found for IMSI: {}", imsi);
                //     continue;
                // }
                
                String imsi = binding.getImsi();
                String iccid = binding.getIccid();
                if (!StringUtils.hasText(iccid)) {
                    logger.warn("ICCID is required for binding: {}", imsi);
                    continue;
                }
                
                // 获取号码资源信息，获取number_id
                NumberResourceVO numberResource = numberResourceService.getByNumber(binding.getNumber());
                if (numberResource == null) {
                    logger.warn("Number resource not found: {}", binding.getNumber());
                    continue;
                }
                
                // 获取IMSI资源信息，获取imsi_id
                ImsiResourceDTO imsiResource = imsiResourceService.getImsiResourceByImsi(imsi);
                if (imsiResource == null) {
                    logger.warn("IMSI resource not found: {}", imsi);
                    continue;
                }
                
                // 设置绑定信息
                binding.setBindingId(globalIds.get(i)); // 设置全局ID
                binding.setNumberId(numberResource.getNumberId()); // 设置号码ID
                binding.setImsiId(imsiResource.getImsiId()); // 设置IMSI ID
                binding.setBindingStatus(BindingConstants.BindingStatus.BOUND); // 绑定状态
                binding.setOperatorUserId(operatorUserId);
                binding.setBindingTime(new Date()); // 设置绑定时间
                binding.setCreateTime(new Date());
                binding.setUpdateTime(new Date());
                
                // 保存绑定关系到分表，使用MyBatis-Plus自动路由
                boolean insertResult = this.save(binding);
                if (!insertResult) {
                    logger.warn("Failed to save binding relationship: number={}", binding.getNumber());
                    continue;
                }
                

                
                // 更新号码资源表中的ICCID字段
                // 由于分表策略，需要通过号码查询而不是ID查询
                if (numberResource != null) {
                    // 使用LambdaUpdateWrapper根据号码更新ICCID，利用分表路由
                    LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(NumberResource::getNumber, binding.getNumber())
                                .set(NumberResource::getIccid, iccid)
                                .set(NumberResource::getUpdateTime, new Date());
                    boolean updatedNumber = numberResourceService.update(updateWrapper);
                    if (!updatedNumber) {
                        logger.warn("Failed to update number resource ICCID field: numberId={}", binding.getNumberId());
                    } else {
                        // 收集成功更新的号码，用于后续状态更新
                        numbersToUpdate.add(binding.getNumber());
                    }
                }
                
                // 收集IMSI用于批量状态更新
                imsiListToUpdate.add(binding.getImsi());
                
                // 更新SimCard状态为激活
                try {
                    boolean simCardStatusUpdateResult = simCardService.updateStatusByIccid(
                            iccid, 
                            SimCardStatusEnum.ACTIVATED.getCode()
                    );
                    
                    if (simCardStatusUpdateResult) {
                        logger.info("SimCard status updated to ACTIVATED successfully for ICCID: {}", iccid);
                    } else {
                        logger.warn("Failed to update SimCard status to ACTIVATED for ICCID: {}", iccid);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating SimCard status for ICCID: {}", iccid, e);
                }
                
                successCount++;
            } catch (Exception e) {
                logger.error("Error occurred during batch binding process, number: {}, imsi: {}", binding.getNumber(), binding.getImsi(), e);
                // 继续处理下一条记录
            }
        }
        
        // 批量更新号码状态为已激活
        if (!numbersToUpdate.isEmpty()) {
            try {
                boolean statusUpdateResult = numberResourceService.batchUpdateNumberStatusByNumber(
                        numbersToUpdate, 
                        NumberStatusEnum.ACTIVATED.getCode()
                );
                
                if (statusUpdateResult) {
                    logger.info("Successfully updated number status to ACTIVATED for numbers: {}", numbersToUpdate);
                } else {
                    logger.error("Failed to update number status to ACTIVATED for numbers: {}", numbersToUpdate);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while updating number status", e);
            }
        }
        
        // 批量更新IMSI状态为已绑定
        if (!imsiListToUpdate.isEmpty()) {
            try {
                boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(
                        imsiListToUpdate, 
                        ImsiStatusEnum.BOUND.getCode()
                );
                
                if (imsiStatusUpdateResult) {
                    logger.info("Successfully updated IMSI status to BOUND for IMSIs: {}", imsiListToUpdate);
                } else {
                    logger.error("Failed to update IMSI status to BOUND for IMSIs: {}", imsiListToUpdate);
                }
            } catch (Exception e) {
                logger.error("Exception occurred while updating IMSI status", e);
            }
        }
        
        // SIM卡状态已在循环中单独更新
        
        logger.info("Batch binding completed: {} successful out of {} total", successCount, bindingList.size());
        return CommonResult.success(successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Integer> batchUnbindV2(BatchUnbindRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getUnbindItems())) {
            return CommonResult.failed(ErrorMessageEnum.BINDING_ID_LIST_EMPTY.getMessage());
        }
        
        int successCount = 0;
        Long operatorUserId = request.getOperatorUserId();
        String remark = request.getRemark();
        
        for (BatchUnbindRequest.UnbindItem item : request.getUnbindItems()) {
            try {
                String number = item.getNumber();
                String imsi = item.getImsi();
                
                if (!StringUtils.hasText(number) || !StringUtils.hasText(imsi)) {
                    logger.warn("Invalid unbind item: number={}, imsi={}", number, imsi);
                    continue;
                }
                
                // 根据号码和IMSI查询绑定关系，使用MyBatis-Plus标准查询
                LambdaQueryWrapper<NumberImsiBinding> bindingWrapper = new LambdaQueryWrapper<>();
                bindingWrapper.eq(NumberImsiBinding::getNumber, number);
                bindingWrapper.eq(NumberImsiBinding::getImsi, imsi);
                bindingWrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.BOUND);
                NumberImsiBinding binding = getOne(bindingWrapper);
                
                if (binding == null) {
                    logger.warn("Binding relationship not found: number={}, imsi={}", number, imsi);
                    continue;
                }
                
                if (!BindingConstants.BindingStatus.BOUND.equals(binding.getBindingStatus())) {
                    logger.warn("Binding is not in BOUND status: number={}, imsi={}, status={}", number, imsi, binding.getBindingStatus());
                    continue;
                }
                
                // 更新绑定状态 - 使用号码作为分表路由条件
                LambdaUpdateWrapper<NumberImsiBinding> bindingUpdateWrapper = new LambdaUpdateWrapper<>();
                bindingUpdateWrapper.eq(NumberImsiBinding::getNumber, number)
                                   .eq(NumberImsiBinding::getImsi, imsi)
                                   .set(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.UNBOUND)
                                   .set(NumberImsiBinding::getOperatorUserId, operatorUserId)
                                   .set(NumberImsiBinding::getRemark, remark)
                                   .set(NumberImsiBinding::getUpdateTime, new Date())
                                   .set(NumberImsiBinding::getUnbindTime, new Date());
                
                boolean updateResult = this.update(bindingUpdateWrapper);
                
                if (!updateResult) {
                    logger.warn("Failed to update binding status: number={}, imsi={}", number, imsi);
                    continue;
                }
                
                // 清除号码资源表中的ICCID字段
                NumberResourceVO numberResource = numberResourceService.getByNumber(number);
                if (numberResource != null) {
                    // 使用LambdaUpdateWrapper根据号码清除ICCID，利用分表路由
                    LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(NumberResource::getNumber, number)
                                .set(NumberResource::getIccid, null)
                                .set(NumberResource::getUpdateTime, new Date());
                    boolean updatedNumber = numberResourceService.update(updateWrapper);
                    if (!updatedNumber) {
                        logger.warn(BindingConstants.ErrorMessage.CLEAR_NUMBER_RESOURCE_ICCID_FAILED + ", number: {}", number);
                        // 继续执行，不影响解绑流程
                    }
                    
                    // 更新号码状态为空闲
                    try {
                        boolean statusUpdateResult = numberResourceService.updateStatus(number, NumberStatusEnum.IDLE.getCode());
                        if (statusUpdateResult) {
                            logger.info("Number status updated to IDLE successfully: {}", number);
                        } else {
                            logger.warn("Failed to update number status to IDLE: {}", number);
                        }
                    } catch (Exception e) {
                        logger.error("Exception occurred while updating number status: {}", number, e);
                    }
                }
                
                // 更新IMSI状态为空闲
                try {
                    List<String> imsiList = new ArrayList<>();
                    imsiList.add(imsi);
                    boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(imsiList, ImsiStatusEnum.IDLE.getCode());
                    
                    if (imsiStatusUpdateResult) {
                        logger.info("IMSI status updated to IDLE successfully: {}", imsi);
                    } else {
                        logger.warn("Failed to update IMSI status to IDLE: {}", imsi);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating IMSI status: {}", imsi, e);
                }
                
                // 更新SIM卡状态为已发布
                try {
                    // 注释：从绑定关系中获取iccid，不再通过ImsiIccidMapping查询
                    // String iccid = imsiIccidMappingService.getIccidByImsi(imsi);
                    String iccid = binding.getIccid();
                    if (StringUtils.hasText(iccid)) {
                        boolean simCardUpdateResult = simCardService.updateStatusByIccid(iccid, SimCardStatusEnum.PUBLISHED.getCode());
                        if (simCardUpdateResult) {
                            logger.info("Successfully updated SIM card status to PUBLISHED for ICCID: {}", iccid);
                        } else {
                            logger.error("Failed to update SIM card status to PUBLISHED for ICCID: {}", iccid);
                        }
                    } else {
                        logger.warn("ICCID not found in binding relationship for IMSI: {}", imsi);
                    }
                } catch (Exception e) {
                    logger.error("Exception occurred while updating SIM card status: {}", imsi, e);
                }
                
                // 注释：不再需要删除IMSI-ICCID映射关系，因为选卡选号时直接提供iccid
                // try {
                //     boolean mappingDeleted = imsiIccidMappingService.deleteByImsi(imsi);
                //     if (mappingDeleted) {
                //         logger.info("IMSI-ICCID mapping deleted successfully for IMSI: {}", imsi);
                //     } else {
                //         logger.warn("Failed to delete IMSI-ICCID mapping for IMSI: {}", imsi);
                //     }
                // } catch (Exception e) {
                //     logger.error("Exception occurred while deleting IMSI-ICCID mapping: {}", imsi, e);
                // }
                
                successCount++;
                logger.info("Successfully unbound: number={}, imsi={}", number, imsi);
                
            } catch (Exception e) {
                logger.error("Error occurred during unbinding process: number={}, imsi={}", item.getNumber(), item.getImsi(), e);
            }
        }
        
        logger.info("Batch unbind completed: {} successful out of {} total", successCount, request.getUnbindItems().size());
        
        return CommonResult.success(successCount);
    }




    @Override
    public NumberImsiBinding getByNumber(String number) {
        if (!StringUtils.hasText(number)) {
            return null;
        }
        
        // 使用MyBatis-Plus标准查询，让ShardingSphere自动路由到对应分表
        LambdaQueryWrapper<NumberImsiBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NumberImsiBinding::getNumber, number);
        wrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.BOUND);
        
        return getOne(wrapper);
    }

    @Override
    public NumberImsiBinding getByImsi(String imsi) {
        if (!StringUtils.hasText(imsi)) {
            return null;
        }
        
        // 由于IMSI不是分表字段，需要查询所有分表
        // 这里可以考虑优化：如果有其他条件（如号码）可以确定分表，则只查询对应分表
        // 暂时使用原有的查询方式，但需要注意性能问题
        LambdaQueryWrapper<NumberImsiBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NumberImsiBinding::getImsi, imsi);
        wrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.BOUND);
        
        return getOne(wrapper);
    }

    @Override
    public List<NumberImsiBinding> getByOrderId(Long orderId) {
        if (orderId == null) {
            return new ArrayList<>();
        }
        
        // 由于orderId不是分表字段，需要查询所有分表
        // 暂时使用原有的查询方式，但需要注意性能问题
        LambdaQueryWrapper<NumberImsiBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NumberImsiBinding::getOrderId, orderId);
        wrapper.orderByDesc(NumberImsiBinding::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public boolean isNumberBound(String number) {
        if (!StringUtils.hasText(number)) {
            return false;
        }
        
        NumberImsiBinding binding = getByNumber(number);
        return binding != null && BindingConstants.BindingStatus.BOUND.equals(binding.getBindingStatus());
    }



    @Override
    public boolean isIccidBound(String iccid) {
        if (!StringUtils.hasText(iccid)) {
            return false;
        }
        
        // 通过ICCID查询绑定关系，这是正确的业务逻辑
        // ICCID是SIM卡的唯一标识，应该通过ICCID判断SIM卡资源是否被绑定
        LambdaQueryWrapper<NumberImsiBinding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NumberImsiBinding::getIccid, iccid);
        wrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.BOUND);
        
        NumberImsiBinding binding = getOne(wrapper);
        return binding != null;
    }

    @Override
    public Map<String, Object> countBindings(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        // 由于需要统计所有分表的数据，暂时保持原有逻辑
        // 后续可以考虑优化为并行查询各个分表
        
        // 总绑定数
        LambdaQueryWrapper<NumberImsiBinding> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            if (params.containsKey("number") && params.get("number") != null) {
                wrapper.like(NumberImsiBinding::getNumber, params.get("number"));
            }
            if (params.containsKey("imsi") && params.get("imsi") != null) {
                wrapper.like(NumberImsiBinding::getImsi, params.get("imsi"));
            }
            if (params.containsKey("bindingStatus") && params.get("bindingStatus") != null) {
                wrapper.eq(NumberImsiBinding::getBindingStatus, params.get("bindingStatus"));
            }
            if (params.containsKey("startTime") && params.get("startTime") != null) {
                wrapper.ge(NumberImsiBinding::getCreateTime, params.get("startTime"));
            }
            if (params.containsKey("endTime") && params.get("endTime") != null) {
                wrapper.le(NumberImsiBinding::getCreateTime, params.get("endTime"));
            }
        }
        
        long totalCount = count(wrapper);
        result.put("totalCount", totalCount);
        
        // 按状态统计
        LambdaQueryWrapper<NumberImsiBinding> boundWrapper = new LambdaQueryWrapper<>();
        boundWrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.BOUND); // 已绑定
        long boundCount = count(boundWrapper);
        
        LambdaQueryWrapper<NumberImsiBinding> unboundWrapper = new LambdaQueryWrapper<>();
        unboundWrapper.eq(NumberImsiBinding::getBindingStatus, BindingConstants.BindingStatus.UNBOUND); // 已解绑
        long unboundCount = count(unboundWrapper);
        
        result.put("boundCount", boundCount);
        result.put("unboundCount", unboundCount);
        
        return result;
    }
}
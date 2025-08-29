package com.nsrs.simcard.service.impl;

import com.nsrs.simcard.entity.ImsiIccidMapping;
import com.nsrs.simcard.mapper.ImsiIccidMappingMapper;
import com.nsrs.simcard.service.ImsiIccidMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * IMSI-ICCID映射服务实现类
 * 
 * @author NSRS
 * @since 2025-07-24
 */
@Slf4j
@Service
public class ImsiIccidMappingServiceImpl implements ImsiIccidMappingService {

    @Autowired
    private ImsiIccidMappingMapper imsiIccidMappingMapper;

    @Override
    public String getIccidByImsi(String imsi) {
        if (!StringUtils.hasText(imsi)) {
            log.warn("IMSI parameter is empty");
            return null;
        }
        
        try {
            return imsiIccidMappingMapper.getIccidByImsi(imsi);
        } catch (Exception e) {
            log.error("Failed to query ICCID by IMSI: {}", imsi, e);
            return null;
        }
    }

    @Override
    public boolean createOrUpdateMapping(String imsi, String iccid) {
        if (!StringUtils.hasText(imsi) || !StringUtils.hasText(iccid)) {
            log.warn("IMSI or ICCID parameter is empty: imsi={}, iccid={}", imsi, iccid);
            return false;
        }
        
        try {
            // 先查询是否已存在映射关系
            String existingIccid = imsiIccidMappingMapper.getIccidByImsi(imsi);
            
            if (existingIccid != null) {
                // 如果已存在且ICCID相同，则无需更新
                if (iccid.equals(existingIccid)) {
                    log.debug("IMSI-ICCID mapping already exists and is the same: imsi={}, iccid={}", imsi, iccid);
                    return true;
                }
                
                // 更新现有映射关系
                int updateResult = imsiIccidMappingMapper.updateMapping(imsi, iccid);
                if (updateResult > 0) {
                    log.info("Successfully updated IMSI-ICCID mapping: imsi={}, old_iccid={}, new_iccid={}", imsi, existingIccid, iccid);
                    return true;
                } else {
                    log.error("Failed to update IMSI-ICCID mapping: imsi={}, iccid={}", imsi, iccid);
                    return false;
                }
            } else {
                // 创建新的映射关系
                ImsiIccidMapping mapping = new ImsiIccidMapping();
                mapping.setImsi(imsi);
                mapping.setIccid(iccid);
                
                int insertResult = imsiIccidMappingMapper.insert(mapping);
                if (insertResult > 0) {
                    log.info("Successfully created IMSI-ICCID mapping: imsi={}, iccid={}", imsi, iccid);
                    return true;
                } else {
                    log.error("Failed to create IMSI-ICCID mapping: imsi={}, iccid={}", imsi, iccid);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Exception occurred while creating or updating IMSI-ICCID mapping: imsi={}, iccid={}", imsi, iccid, e);
            return false;
        }
    }

    @Override
    public boolean batchCreateMappings(List<ImsiIccidMapping> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            log.warn("Mappings list is empty");
            return false;
        }
        
        try {
            int insertResult = imsiIccidMappingMapper.batchInsert(mappings);
            if (insertResult > 0) {
                log.info("Successfully batch created {} IMSI-ICCID mappings", insertResult);
                return true;
            } else {
                log.error("Failed to batch create IMSI-ICCID mappings");
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred while batch creating IMSI-ICCID mappings", e);
            return false;
        }
    }

    @Override
    public boolean deleteByImsi(String imsi) {
        if (!StringUtils.hasText(imsi)) {
            log.warn("IMSI parameter is empty");
            return false;
        }
        
        try {
            int deleteResult = imsiIccidMappingMapper.deleteByImsi(imsi);
            if (deleteResult > 0) {
                log.info("Successfully deleted IMSI-ICCID mapping: imsi={}", imsi);
                return true;
            } else {
                log.warn("No IMSI-ICCID mapping found to delete: imsi={}", imsi);
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred while deleting IMSI-ICCID mapping: imsi={}", imsi, e);
            return false;
        }
    }

    @Override
    public boolean batchDeleteByImsi(List<String> imsiList) {
        if (CollectionUtils.isEmpty(imsiList)) {
            log.warn("IMSI list is empty");
            return false;
        }
        
        try {
            int deleteResult = imsiIccidMappingMapper.batchDeleteByImsi(imsiList);
            if (deleteResult > 0) {
                log.info("Successfully batch deleted {} IMSI-ICCID mappings", deleteResult);
                return true;
            } else {
                log.warn("No IMSI-ICCID mappings found to delete for provided IMSI list");
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred while batch deleting IMSI-ICCID mappings", e);
            return false;
        }
    }
}
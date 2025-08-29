package com.nsrs.simcard.service;

import com.nsrs.simcard.entity.ImsiIccidMapping;

import java.util.List;

/**
 * IMSI-ICCID映射服务接口
 */
public interface ImsiIccidMappingService {
    
    /**
     * 根据IMSI查询ICCID
     *
     * @param imsi IMSI号码
     * @return ICCID
     */
    String getIccidByImsi(String imsi);
    
    /**
     * 创建或更新映射关系
     *
     * @param imsi IMSI号码
     * @param iccid ICCID
     * @return 是否成功
     */
    boolean createOrUpdateMapping(String imsi, String iccid);
    
    /**
     * 批量创建映射关系
     *
     * @param mappings 映射关系列表
     * @return 是否成功
     */
    boolean batchCreateMappings(List<ImsiIccidMapping> mappings);
    
    /**
     * 根据IMSI删除映射关系
     *
     * @param imsi IMSI号码
     * @return 是否成功
     */
    boolean deleteByImsi(String imsi);
    
    /**
     * 批量根据IMSI删除映射关系
     *
     * @param imsiList IMSI列表
     * @return 是否成功
     */
    boolean batchDeleteByImsi(List<String> imsiList);
}
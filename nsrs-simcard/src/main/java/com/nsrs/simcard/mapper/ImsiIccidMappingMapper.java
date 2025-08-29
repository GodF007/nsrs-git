package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.ImsiIccidMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * IMSI-ICCID映射表数据访问层
 * 
 * @author NSRS
 * @since 2025-07-24
 */
@Mapper
public interface ImsiIccidMappingMapper extends BaseMapper<ImsiIccidMapping> {

    /**
     * 根据IMSI查询ICCID
     * 
     * @param imsi IMSI号码
     * @return ICCID号码
     */
    String getIccidByImsi(@Param("imsi") String imsi);

    /**
     * 批量插入IMSI-ICCID映射关系
     * 
     * @param mappings 映射关系列表
     * @return 插入成功的记录数
     */
    int batchInsert(@Param("mappings") List<ImsiIccidMapping> mappings);

    /**
     * 根据IMSI删除映射关系
     * 
     * @param imsi IMSI号码
     * @return 删除成功的记录数
     */
    int deleteByImsi(@Param("imsi") String imsi);

    /**
     * 批量根据IMSI删除映射关系
     * 
     * @param imsiList IMSI号码列表
     * @return 删除成功的记录数
     */
    int batchDeleteByImsi(@Param("imsiList") List<String> imsiList);

    /**
     * 更新IMSI-ICCID映射关系
     * 
     * @param imsi IMSI号码
     * @param iccid ICCID号码
     * @return 更新成功的记录数
     */
    int updateMapping(@Param("imsi") String imsi, @Param("iccid") String iccid);
}
package com.nsrs.binding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.binding.entity.NumberImsiBinding;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 号码与IMSI绑定Mapper接口
 */
@Mapper
public interface NumberImsiBindingMapper extends BaseMapper<NumberImsiBinding> {
    
    /**
     * 自定义分页查询
     *
     * @param page 分页参数
     * @param params 查询参数
     * @param tableName 表名
     * @return 分页结果
     */
    IPage<NumberImsiBinding> selectPageWithParams(Page<NumberImsiBinding> page, @Param("params") Map<String, Object> params, @Param("tableName") String tableName);
    


    /**
     * 根据号码查询绑定关系
     *
     * @param number 号码
     * @param tableName 表名
     * @return 绑定关系
     */
    NumberImsiBinding selectByNumber(@Param("number") String number, @Param("tableName") String tableName);
    
    /**
     * 根据IMSI查询绑定关系
     *
     * @param imsi IMSI号码
     * @param tableName 表名
     * @return 绑定关系
     */
    NumberImsiBinding selectByImsi(@Param("imsi") String imsi, @Param("tableName") String tableName);
    
    /**
     * 根据号码和IMSI查询绑定关系
     *
     * @param number 号码
     * @param imsi IMSI号码
     * @param tableName 表名
     * @return 绑定关系
     */
    NumberImsiBinding selectByNumberAndImsi(@Param("number") String number, @Param("imsi") String imsi, @Param("tableName") String tableName);
    
    /**
     * 根据订单ID查询绑定关系列表
     *
     * @param orderId 订单ID
     * @param tableName 表名
     * @return 绑定关系列表
     */
    List<NumberImsiBinding> selectByOrderId(@Param("orderId") Long orderId, @Param("tableName") String tableName);
    
    /**
     * 批量插入绑定关系
     *
     * @param list 绑定关系列表
     * @param tableName 表名
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<NumberImsiBinding> list, @Param("tableName") String tableName);
    
    /**
     * 批量更新绑定状态为已解绑
     *
     * @param bindingIds 绑定ID列表
     * @param operatorUserId 操作用户ID
     * @param remark 备注
     * @param tableName 表名
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("list") List<Long> bindingIds, @Param("operatorUserId") Long operatorUserId, @Param("remark") String remark, @Param("tableName") String tableName);
    
    /**
     * 统计各状态的绑定数量
     *
     * @param params 统计参数
     * @param tableName 表名
     * @return 统计结果
     */
    List<Map<String, Object>> countByStatus(@Param("params") Map<String, Object> params, @Param("tableName") String tableName);
    
    /**
     * 根据前缀获取表名
     *
     * @param number 号码
     * @return 表名
     */
    String getTableNameByNumber(@Param("number") String number);
}
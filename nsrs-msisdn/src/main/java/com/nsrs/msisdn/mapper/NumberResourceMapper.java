package com.nsrs.msisdn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nsrs.msisdn.entity.NumberResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 号码资源Mapper接口
 */
@Mapper
public interface NumberResourceMapper extends BaseMapper<NumberResource> {
    
    /**
     * 更新号码状态
     *
     * @param numberId 号码ID
     * @param status   状态
     * @return 影响行数
     */
    @Update("UPDATE number_resource SET status = #{status}, update_time = NOW() WHERE number_id = #{numberId}")
    int updateStatus(@Param("numberId") Long numberId, @Param("status") Integer status);
    
    /**
     * 根据号码段ID更新号码状态
     *
     * @param segmentId 号码段ID
     * @param status    状态
     * @return 影响行数
     */
    @Update("UPDATE number_resource SET status = #{status}, update_time = NOW() WHERE segment_id = #{segmentId}")
    int updateStatusBySegmentId(@Param("segmentId") Long segmentId, @Param("status") Integer status);

    /**
     * 跨表查询
     *
     * @param sql 跨表SQL
     * @return 号码资源列表
     */
    List<NumberResource> crossTableQuery(@Param("sql") String sql);
    
    /**
     * 跨表查询总数
     *
     * @param sql 跨表统计SQL
     * @return 总数
     */
    Long crossTableCount(@Param("sql") String sql);
    
    /**
     * 根据号码查询
     *
     * @param number 号码
     * @return 号码资源
     */
    NumberResource selectByNumber(@Param("number") String number);
    
    /**
     * 创建分表
     *
     * @param tableName 表名
     * @return 是否成功
     */
    int createTable(@Param("tableName") String tableName);
    
    /**
     * 执行自定义SQL查询
     *
     * @param sql 自定义SQL
     * @return 号码资源列表
     */
    List<NumberResource> selectByCustomSql(@Param("sql") String sql);
    
    /**
     * 批量更新号码状态（分表优化版本）
     * 使用CASE WHEN语句实现真正的批量更新
     *
     * @param numbers 号码列表
     * @param statuses 对应的状态列表
     * @return 影响行数
     */
    int batchUpdateStatusByNumbers(@Param("numbers") List<String> numbers, @Param("statuses") List<Integer> statuses);
    
    /**
     * 批量查询号码资源（按号码）
     *
     * @param numbers 号码列表
     * @return 号码资源列表
     */
    List<NumberResource> selectByNumbers(@Param("numbers") List<String> numbers);
}
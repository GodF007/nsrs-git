package com.nsrs.msisdn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.msisdn.entity.NumberSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 号码段Mapper接口
 */
@Mapper
public interface NumberSegmentMapper extends BaseMapper<NumberSegment> {
    
    /**
     * 统计总号码数量
     */
    @Select("SELECT SUM(total_qty) FROM number_segment")
    Integer sumTotalQty();
    
    /**
     * 统计空闲号码数量
     */
    @Select("SELECT SUM(idle_qty) FROM number_segment")
    Integer sumIdleQty();
    
    /**
     * 统计预留号码数量
     */
    @Select("SELECT SUM(reserved_qty) FROM number_segment")
    Integer sumReservedQty();
    
    /**
     * 统计已激活号码数量
     */
    @Select("SELECT SUM(activated_qty) FROM number_segment")
    Integer sumActivatedQty();
    
    /**
     * 统计已冻结号码数量
     */
    @Select("SELECT SUM(frozen_qty) FROM number_segment")
    Integer sumFrozenQty();
    
    /**
     * 统计已锁定号码数量
     */
    @Select("SELECT SUM(blocked_qty) FROM number_segment")
    Integer sumBlockedQty();
    
    /**
     * 统计已释放号码数量
     */

}
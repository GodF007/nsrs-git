package com.nsrs.msisdn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.msisdn.entity.NumberOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 号码操作日志Mapper接口
 */
@Mapper
public interface NumberOperationLogMapper extends BaseMapper<NumberOperationLog> {
} 
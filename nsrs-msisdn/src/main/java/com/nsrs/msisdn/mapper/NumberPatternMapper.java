package com.nsrs.msisdn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.msisdn.entity.NumberPattern;
import org.apache.ibatis.annotations.Mapper;

/**
 * 号码模式Mapper接口
 */
@Mapper
public interface NumberPatternMapper extends BaseMapper<NumberPattern> {
} 
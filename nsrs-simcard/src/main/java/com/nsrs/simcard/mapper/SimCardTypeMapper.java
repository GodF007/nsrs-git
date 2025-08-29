package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.SimCardType;
import org.apache.ibatis.annotations.Mapper;

/**
 * SIM卡类型数据访问层
 */
@Mapper
public interface SimCardTypeMapper extends BaseMapper<SimCardType> {
} 
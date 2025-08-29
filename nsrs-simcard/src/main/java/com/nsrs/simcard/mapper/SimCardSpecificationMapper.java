package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.SimCardSpecification;
import org.apache.ibatis.annotations.Mapper;

/**
 * SIM卡规格数据访问层
 */
@Mapper
public interface SimCardSpecificationMapper extends BaseMapper<SimCardSpecification> {
} 
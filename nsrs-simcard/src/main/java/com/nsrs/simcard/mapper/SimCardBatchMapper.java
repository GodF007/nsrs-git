package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.SimCardBatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * SIM卡批次数据访问层
 */
@Mapper
public interface SimCardBatchMapper extends BaseMapper<SimCardBatch> {
} 
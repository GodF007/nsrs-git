package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.simcard.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商数据访问层
 */
@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {
} 
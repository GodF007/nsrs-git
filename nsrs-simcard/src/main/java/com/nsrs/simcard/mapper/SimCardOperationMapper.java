package com.nsrs.simcard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsrs.simcard.entity.SimCardOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * SIM卡操作记录数据访问层
 */
@Mapper
public interface SimCardOperationMapper extends BaseMapper<SimCardOperation> {
    
    /**
     * 根据条件分页查询SIM卡操作记录
     * 
     * @param page 分页参数
     * @param params 查询参数
     * @return 分页结果
     */
    IPage<SimCardOperation> selectOperationPage(Page<SimCardOperation> page, @Param("params") Map<String, Object> params);
} 
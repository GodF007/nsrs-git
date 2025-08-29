package com.nsrs.msisdn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nsrs.msisdn.entity.NumberApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 号码审批Mapper接口
 */
@Mapper
public interface NumberApprovalMapper extends BaseMapper<NumberApproval> {
    
    /**
     * 更新审批状态
     *
     * @param approvalId 审批ID
     * @param status     状态
     * @return 影响行数
     */
    @Update("UPDATE number_approval SET status = #{status}, approval_time = NOW() WHERE approval_id = #{approvalId}")
    int updateStatus(@Param("approvalId") Long approvalId, @Param("status") Integer status);
} 
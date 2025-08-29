package com.nsrs.binding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.binding.entity.BatchBindingDetail;
import com.nsrs.binding.query.BatchBindingDetailQuery;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * 批量绑定详情服务接口
 */
public interface BatchBindingDetailService extends IService<BatchBindingDetail> {

    /**
     * 分页查询批量绑定详情
     *
     * @param request 分页查询请求
     * @return 分页结果
     */
    PageResult<BatchBindingDetail> page(PageRequest<BatchBindingDetailQuery> request);

    /**
     * 批量创建详情
     *
     * @param taskId 任务ID
     * @param details 详情列表
     * @return 创建结果
     */
    CommonResult<Integer> batchCreate(Long taskId, List<BatchBindingDetail> details);

    /**
     * 更新详情状态
     *
     * @param detailId 详情ID
     * @param status 状态
     * @param errorMsg 错误信息
     * @return 更新结果
     */
    CommonResult<Void> updateStatus(Long detailId, Integer status, String errorMsg);

    /**
     * 批量更新详情状态
     *
     * @param detailIds 详情ID列表
     * @param status 状态
     * @return 更新结果
     */
    CommonResult<Integer> batchUpdateStatus(List<Long> detailIds, Integer status);

    /**
     * 统计任务各状态详情数量
     *
     * @param taskId 任务ID
     * @return 统计结果
     */
    Map<String, Integer> countStatus(Long taskId);

    /**
     * 按任务ID删除详情
     *
     * @param taskId 任务ID
     * @return 删除结果
     */
    CommonResult<Integer> deleteByTaskId(Long taskId);
}
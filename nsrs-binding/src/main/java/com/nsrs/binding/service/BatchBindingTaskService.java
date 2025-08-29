package com.nsrs.binding.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nsrs.binding.entity.BatchBindingTask;
import com.nsrs.binding.query.BatchBindingTaskQuery;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 批量绑定任务服务接口
 */
public interface BatchBindingTaskService extends IService<BatchBindingTask> {

    /**
     * 分页查询批量绑定任务
     *
     * @param request 分页查询请求
     * @return 分页结果
     */
    PageResult<BatchBindingTask> page(PageRequest<BatchBindingTaskQuery> request);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    BatchBindingTask getTaskDetail(Long taskId);

    /**
     * 创建批量绑定任务
     *
     * @param file Excel文件
     * @param taskName 任务名称
     * @param description 任务描述
     * @return 操作结果
     */
    CommonResult<String> createTask(MultipartFile file, String taskName, String description);

    /**
     * 创建批量解绑任务
     *
     * @param file Excel文件
     * @param taskName 任务名称
     * @param description 任务描述
     * @return 操作结果
     */
    CommonResult<String> createUnbindTask(MultipartFile file, String taskName, String description);

    /**
     * 创建批量绑定任务（旧版本）
     *
     * @param task 任务信息
     * @param file Excel文件
     * @return 操作结果
     */
    CommonResult<Void> createTaskOld(BatchBindingTask task, MultipartFile file);

    /**
     * 更新任务
     *
     * @param task 任务信息
     * @return 操作结果
     */
    CommonResult<Void> updateTask(BatchBindingTask task);

    /**
     * 删除任务
     *
     * @param taskIds 任务ID列表
     * @return 操作结果
     */
    CommonResult<Void> deleteTasks(List<Long> taskIds);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    CommonResult<Void> cancelTask(Long taskId);

    /**
     * 重试任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    CommonResult<Void> retryTask(Long taskId);

    /**
     * 处理任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    CommonResult<Void> processTask(Long taskId);

    /**
     * 启动任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    CommonResult<Void> startTask(Long taskId);

    /**
     * 停止任务
     *
     * @param taskId 任务ID
     * @return 操作结果
     */
    CommonResult<Void> stopTask(Long taskId);

    /**
     * 测试事务注解是否生效
     *
     * @param throwException 是否抛出异常以测试事务回滚
     * @return 操作结果
     */
    CommonResult<String> testTransaction(boolean throwException);

    /**
     * 下载批量绑定模板
     *
     * @param response HTTP响应
     */
    void downloadBindingTemplate(javax.servlet.http.HttpServletResponse response);

    /**
     * 下载批量解绑模板
     *
     * @param response HTTP响应
     */
    void downloadUnbindTemplate(javax.servlet.http.HttpServletResponse response);
}
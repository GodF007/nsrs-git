package com.nsrs.binding.constants;

/**
 * 绑定模块常量定义
 */
public final class BindingConstants {
    
    private BindingConstants() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 绑定状态常量
     */
    public static final class BindingStatus {
        /** 已绑定 */
        public static final Integer BOUND = 1;
        /** 已解绑 */
        public static final Integer UNBOUND = 2;
        
        private BindingStatus() {}
    }
    
    /**
     * 绑定类型常量
     */
    public static final class BindingType {
        /** 普通绑定 */
        public static final Integer NORMAL = 1;
        /** 批量绑定 */
        public static final Integer BATCH = 2;
        /** 测试绑定 */
        public static final Integer TEST = 3;
        
        private BindingType() {}
    }
    
    /**
     * 任务状态常量
     */
    public static final class TaskStatus {
        /** 待处理 */
        public static final Integer PENDING = 0;
        /** 处理中 */
        public static final Integer PROCESSING = 1;
        /** 成功 */
        public static final Integer SUCCESS = 2;
        /** 失败 */
        public static final Integer FAILED = 3;
        /** 部分成功 */
        public static final Integer PARTIAL_SUCCESS = 4;
        /** 已完成 */
        public static final Integer COMPLETED = 5;
        
        private TaskStatus() {}
    }
    
    /**
     * 任务类型常量
     */
    public static final class TaskType {
        /** 绑定任务 */
        public static final Integer BIND = 1;
        /** 解绑任务 */
        public static final Integer UNBIND = 2;
        
        private TaskType() {}
    }
    
    /**
     * 处理状态常量
     */
    public static final class ProcessStatus {
        /** 待处理 */
        public static final Integer PENDING = 0;
        /** 成功 */
        public static final Integer SUCCESS = 1;
        /** 失败 */
        public static final Integer FAILED = 2;
        
        private ProcessStatus() {}
    }
    
    /**
     * 序列名称常量
     */
    public static final class SequenceName {
        /** 号码IMSI绑定ID序列 */
        public static final String NUMBER_IMSI_BINDING_ID_SEQ = "number_imsi_binding_id_seq";
        
        private SequenceName() {}
    }
    
    /**
     * 错误消息常量
     */
    public static final class ErrorMessage {
        /** 号码已绑定 */
        public static final String NUMBER_ALREADY_BOUND = "Number already bound";

        /** ICCID已绑定 */
        public static final String ICCID_ALREADY_BOUND = "ICCID already bound";
        /** 绑定关系不存在 */
        public static final String BINDING_NOT_FOUND = "Binding relationship not found";
        /** 保存绑定关系失败 */
        public static final String SAVE_BINDING_FAILED = "Failed to save binding relationship";
        /** 更新绑定状态失败 */
        public static final String UPDATE_BINDING_STATUS_FAILED = "Failed to update binding status";
        /** 更新号码资源失败 */
        public static final String UPDATE_NUMBER_RESOURCE_FAILED = "Failed to update number resource";
        /** 绑定ID为空 */
        public static final String BINDING_ID_REQUIRED = "Binding ID is required";
        /** 参数验证失败 */
        public static final String PARAMETER_VALIDATION_FAILED = "Parameter validation failed";
        /** 文件解析失败 */
        public static final String FILE_PARSE_FAILED = "Failed to parse file";
        /** 任务不存在 */
        public static final String TASK_NOT_FOUND = "Task not found";
        /** 任务状态无效 */
        public static final String INVALID_TASK_STATUS = "Invalid task status";
        /** 更新号码资源ICCID字段失败 */
        public static final String UPDATE_NUMBER_RESOURCE_ICCID_FAILED = "Failed to update number resource ICCID field";
        /** 清除号码资源ICCID字段失败 */
        public static final String CLEAR_NUMBER_RESOURCE_ICCID_FAILED = "Failed to clear number resource ICCID field";
        /** 未找到号码资源记录 */
        public static final String NUMBER_RESOURCE_NOT_FOUND = "Number resource record not found";
        /** 未找到绑定关系 */
        public static final String BINDING_RELATIONSHIP_NOT_FOUND = "Binding relationship not found";
        /** 更新绑定状态失败 */
        public static final String UPDATE_BINDING_STATUS_FAILED_BATCH = "Failed to update binding status";
        /** 参数不正确 */
        public static final String INVALID_PARAMETERS = "Invalid parameters";
        /** 详情不存在 */
        public static final String DETAIL_NOT_FOUND = "Detail not found";
        /** 更新状态失败 */
        public static final String UPDATE_STATUS_FAILED = "Failed to update status";
        /** 批量创建详情失败 */
        public static final String BATCH_CREATE_DETAILS_FAILED = "Failed to batch create details";
        /** 批量更新状态失败 */
        public static final String BATCH_UPDATE_STATUS_FAILED = "Failed to batch update status";
        /** 绑定列表为空 */
        public static final String BINDING_LIST_EMPTY = "Binding list is empty";
        /** 绑定ID列表为空 */
        public static final String BINDING_ID_LIST_EMPTY = "Binding ID list is empty";
        /** 只有待处理的任务才能被处理 */
        public static final String ONLY_PENDING_TASK_CAN_BE_PROCESSED = "Only pending tasks can be processed";
        /** 保存测试任务失败 */
        public static final String SAVE_TEST_TASK_FAILED = "Failed to save test task";
        /** 任务ID必填 */
        public static final String TASK_ID_REQUIRED = "Task ID is required";
        /** 绑定参数必填 */
        public static final String BINDING_PARAMS_REQUIRED = "Binding parameters are required";
        /** 只有待处理或处理中的任务才能被取消 */
        public static final String ONLY_PENDING_OR_PROCESSING_TASK_CAN_BE_CANCELLED = "Only pending or processing tasks can be cancelled";
        /** 取消任务失败 */
        public static final String CANCEL_TASK_FAILED = "Failed to cancel task";
        /** 只有失败的任务才能重试 */
        public static final String ONLY_FAILED_TASK_CAN_BE_RETRIED = "Only failed tasks can be retried";
        /** 重试任务失败 */
        public static final String RETRY_TASK_FAILED = "Failed to retry task";
        /** 只有待处理的任务才能被修改 */
        public static final String ONLY_PENDING_TASK_CAN_BE_MODIFIED = "Only pending tasks can be modified";
        /** 更新任务失败 */
        public static final String UPDATE_TASK_FAILED = "Failed to update task";
        /** 任务ID列表必填 */
        public static final String TASK_ID_LIST_REQUIRED = "Task ID list is required";
        /** 删除任务失败 */
        public static final String DELETE_TASK_FAILED = "Failed to delete task";
        /** 文件未找到 */
        public static final String FILE_NOT_FOUND = "File not found";
        /** 批量绑定异常 */
        public static final String BATCH_BINDING_EXCEPTION = "Batch binding exception";
        /** IMSI不匹配 */
        public static final String IMSI_MISMATCH = "IMSI mismatch";
        /** ICCID不匹配 */
        public static final String ICCID_MISMATCH = "ICCID mismatch";
        /** 批量解绑异常 */
        public static final String BATCH_UNBINDING_EXCEPTION = "Batch unbinding exception";
        /** 保存文件失败 */
        public static final String SAVE_FILE_FAILED = "Failed to save file";
        /** 创建任务失败 */
        public static final String CREATE_TASK_FAILED = "Failed to create task";
        /** Excel中没有有效数据 */
        public static final String NO_VALID_DATA_IN_EXCEL = "No valid data in Excel";
        /** 文件必填 */
        public static final String FILE_REQUIRED = "File is required";
        /** 创建目录失败 */
        public static final String CREATE_DIRECTORY_FAILED = "Failed to create directory";
        /** 号码必填 */
        public static final String NUMBER_REQUIRED = "Number is required";
        
        private ErrorMessage() {}
    }
    
    /**
     * 日志消息常量
     */
    public static final class LogMessage {
        /** 绑定成功 */
        public static final String BINDING_SUCCESS = "Successfully bound number and IMSI: number={}, imsi={}, bindingId={}";
        /** 解绑成功 */
        public static final String UNBINDING_SUCCESS = "Successfully unbound number and IMSI: bindingId={}, number={}, imsi={}";
        /** 批量绑定开始 */
        public static final String BATCH_BINDING_START = "Starting batch binding process: taskId={}, totalCount={}";
        /** 批量绑定完成 */
        public static final String BATCH_BINDING_COMPLETE = "Batch binding process completed: taskId={}, successCount={}, failCount={}";
        /** 批量解绑开始 */
        public static final String BATCH_UNBINDING_START = "Starting batch unbinding process: bindingIds={}";
        /** 批量解绑完成 */
        public static final String BATCH_UNBINDING_COMPLETE = "Batch unbinding process completed: successCount={}";
        /** 任务状态更新 */
        public static final String TASK_STATUS_UPDATED = "Task status updated: taskId={}, status={}";
        
        private LogMessage() {}
    }
}
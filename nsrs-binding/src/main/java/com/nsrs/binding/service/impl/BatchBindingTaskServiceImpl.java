package com.nsrs.binding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsrs.binding.constants.BindingConstants;
import com.nsrs.binding.dto.BatchBindingTemplateDto;
import com.nsrs.binding.dto.BatchUnbindRequest;
import com.nsrs.binding.dto.BatchUnbindTemplateDto;
import com.nsrs.binding.entity.BatchBindingDetail;
import com.nsrs.binding.entity.BatchBindingTask;
import com.nsrs.binding.entity.NumberImsiBinding;
import com.nsrs.binding.mapper.BatchBindingDetailMapper;
import com.nsrs.binding.mapper.BatchBindingTaskMapper;
import com.nsrs.binding.query.BatchBindingTaskQuery;
import com.nsrs.binding.service.BatchBindingDetailService;
import com.nsrs.binding.service.BatchBindingTaskService;
import com.nsrs.binding.service.NumberImsiBindingService;
import com.nsrs.binding.service.TaskExecutionManager;
import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.exception.BusinessException;
import com.nsrs.common.model.CommonResult;
import com.nsrs.common.enums.NumberStatusEnum;
import com.nsrs.common.utils.ExcelUtils;
import com.nsrs.msisdn.entity.NumberResource;
import com.nsrs.msisdn.service.NumberResourceService;
import com.nsrs.msisdn.vo.NumberResourceVO;
import com.nsrs.simcard.entity.SimCard;
import com.nsrs.simcard.enums.ImsiStatusEnum;
import com.nsrs.simcard.enums.SimCardStatusEnum;
import com.nsrs.simcard.service.ImsiResourceService;
import com.nsrs.simcard.service.SimCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Batch binding task service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchBindingTaskServiceImpl extends ServiceImpl<BatchBindingTaskMapper, BatchBindingTask> implements BatchBindingTaskService {

    private final BatchBindingDetailMapper batchBindingDetailMapper;
    private final BatchBindingDetailService batchBindingDetailService;
    private final NumberImsiBindingService numberImsiBindingService;
    private final NumberResourceService numberResourceService;
    private final ImsiResourceService imsiResourceService;
    private final SimCardService simCardService;
    private final TaskExecutionManager taskExecutionManager;
    
    @Value("${nsrs.file.upload.path:/upload/}")
     private String uploadPath;
     
     @Value("${nsrs.file.template.path:/templates/}")
     private String templatePath;

    @Override
    public PageResult<BatchBindingTask> page(PageRequest<BatchBindingTaskQuery> request) {
        LambdaQueryWrapper<BatchBindingTask> queryWrapper = new LambdaQueryWrapper<>();
        
        BatchBindingTaskQuery query = request.getQuery();
        if (query != null) {
            // 任务名称查询条件
            if (StringUtils.isNotBlank(query.getTaskName())) {
                queryWrapper.like(BatchBindingTask::getTaskName, query.getTaskName());
            }
            
            // 任务状态查询条件
            if (query.getStatus() != null) {
                queryWrapper.eq(BatchBindingTask::getStatus, query.getStatus());
            }
            
            // 任务类型查询条件
            if (query.getTaskType() != null) {
                queryWrapper.eq(BatchBindingTask::getTaskType, query.getTaskType());
            }
            
            // 创建用户ID查询条件
            if (query.getCreateUserId() != null) {
                queryWrapper.eq(BatchBindingTask::getCreateUserId, query.getCreateUserId());
            }
            
            // 时间范围查询条件
            if (query.getStartTime() != null) {
                queryWrapper.ge(BatchBindingTask::getCreateTime, query.getStartTime());
            }
            if (query.getEndTime() != null) {
                queryWrapper.le(BatchBindingTask::getCreateTime, query.getEndTime());
            }
        }
        
        // 默认按创建时间倒序
        queryWrapper.orderByDesc(BatchBindingTask::getCreateTime);
        
        // 执行分页查询
        Page<BatchBindingTask> page = new Page<>(request.getCurrent(), request.getSize());
        Page<BatchBindingTask> pageResult = this.page(page, queryWrapper);
        
        // 构建返回结果
        PageResult<BatchBindingTask> result = new PageResult<>();
        result.setList(pageResult.getRecords());
        result.setTotal(pageResult.getTotal());
        result.setPageNum(request.getCurrent());
        result.setPageSize(request.getSize());
        result.setPages((pageResult.getTotal() + request.getSize() - 1) / request.getSize());
        
        return result;
    }

    @Override
    public BatchBindingTask getTaskDetail(Long taskId) {
        // Use custom mapper.xml method to query task details
        return baseMapper.selectTaskDetailById(taskId);
    }

    @Override
    public CommonResult<String> createTask(MultipartFile file, String taskName, String description) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return CommonResult.failed("File cannot be empty");
            }
            
            if (!ExcelUtils.isValidExcelFile(file)) {
                return CommonResult.failed("Invalid Excel file format");
            }
            
            // Save file
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadPath + fileName;
            String fullPath = System.getProperty("user.dir") + filePath;
            
            // Ensure upload directory exists
            File uploadDir = new File(System.getProperty("user.dir") + uploadPath);
            if (!uploadDir.exists()) {
                boolean mkdirs = uploadDir.mkdirs();
                if (!mkdirs) {
                    return CommonResult.failed("Failed to create upload directory");
                }
            }
            
            file.transferTo(new File(fullPath));
            
            // Create task
            BatchBindingTask task = new BatchBindingTask();
            task.setTaskName(taskName);
            // Note: description参数暂时不使用，因为实体类中没有对应字段
            task.setTaskType(BindingConstants.TaskType.BIND);
            task.setStatus(BindingConstants.TaskStatus.PENDING);
            task.setFilePath(filePath);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            
            this.save(task);
            
            log.info("Created batch binding task: {}", task.getTaskId());
            
            // 任务创建后不立即执行，等待手动启动
            // Future<Void> future = processTaskAsync(task.getTaskId());
            // taskExecutionManager.registerTask(task.getTaskId(), future);
            
            return CommonResult.success(String.valueOf(task.getTaskId()));
        } catch (Exception e) {
            log.error("Failed to create task", e);
            return CommonResult.failed("Failed to create task: " + e.getMessage());
        }
    }

    @Override
    public CommonResult<String> createUnbindTask(MultipartFile file, String taskName, String description) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return CommonResult.failed("File cannot be empty");
            }
            
            if (!ExcelUtils.isValidExcelFile(file)) {
                return CommonResult.failed("Invalid Excel file format");
            }
            
            // Save file
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = uploadPath + fileName;
            String fullPath = System.getProperty("user.dir") + filePath;
            
            // Ensure upload directory exists
            File uploadDir = new File(System.getProperty("user.dir") + uploadPath);
            if (!uploadDir.exists()) {
                boolean mkdirs = uploadDir.mkdirs();
                if (!mkdirs) {
                    return CommonResult.failed("Failed to create upload directory");
                }
            }
            
            file.transferTo(new File(fullPath));
            
            // Create task
            BatchBindingTask task = new BatchBindingTask();
            task.setTaskName(taskName);
            // Note: description参数暂时不使用，因为实体类中没有对应字段
            task.setTaskType(BindingConstants.TaskType.UNBIND);
            task.setStatus(BindingConstants.TaskStatus.PENDING);
            task.setFilePath(filePath);
            task.setCreateTime(new Date());
            task.setUpdateTime(new Date());
            
            this.save(task);
            
            log.info("Created batch unbind task: {}", task.getTaskId());
            
            // 任务创建后不立即执行，等待手动启动
            // Future<Void> future = processTaskAsync(task.getTaskId());
            // taskExecutionManager.registerTask(task.getTaskId(), future);
            
            return CommonResult.success(String.valueOf(task.getTaskId()));
        } catch (Exception e) {
            log.error("Failed to create unbind task", e);
            return CommonResult.failed("Failed to create unbind task: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> createTaskOld(BatchBindingTask task, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return CommonResult.failed(BindingConstants.ErrorMessage.FILE_REQUIRED);
        }
        
        // Save uploaded file
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        String filePath = "/upload/" + fileName;
        
        try {
            File destFile = new File(System.getProperty("user.dir") + filePath);
            if (!destFile.getParentFile().exists()) {
                boolean mkdirs = destFile.getParentFile().mkdirs();
                if (!mkdirs) {
                    return CommonResult.failed(BindingConstants.ErrorMessage.CREATE_DIRECTORY_FAILED);
                }
            }
            FileCopyUtils.copy(file.getBytes(), destFile);
            task.setFilePath(filePath);
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return CommonResult.failed(BindingConstants.ErrorMessage.SAVE_FILE_FAILED);
        }
        
        // Set task status and time
        task.setStatus(BindingConstants.TaskStatus.PENDING); // Pending
        Date now = new Date();
        task.setCreateTime(now);
        task.setUpdateTime(now);
        
        // Save task
        boolean saveResult = this.save(task);
        if (!saveResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.CREATE_TASK_FAILED);
        }
        
        // 任务创建后不立即执行，等待手动启动
        // Future<Void> future = processTaskAsync(task.getTaskId());
        // taskExecutionManager.registerTask(task.getTaskId(), future);
        
        return CommonResult.success();
    }
    
    @Async
    public Future<Void> processTaskAsync(Long taskId) {
        log.info("Starting to process task: {}", taskId);
        
        try {
            // Check if task is interrupted
            if (taskExecutionManager.isTaskInterrupted(taskId)) {
                log.info("Task was interrupted before processing: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            BatchBindingTask task = this.getById(taskId);
            if (task == null) {
                log.error("Task does not exist: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            // Update task status to processing
        task.setStatus(BindingConstants.TaskStatus.PROCESSING); // Processing
            task.setStartTime(new Date());
            this.updateById(task);
            
            // Check interruption
            if (taskExecutionManager.isTaskInterrupted(taskId)) {
                log.info("Task was interrupted during status update: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            // Parse Excel file
            List<BatchBindingDetail> details = parseExcelFile(task);
            if (CollectionUtils.isEmpty(details)) {
                throw new BusinessException("NO_DATA", BindingConstants.ErrorMessage.NO_VALID_DATA_IN_EXCEL);
            }
            
            // Check interruption
            if (taskExecutionManager.isTaskInterrupted(taskId)) {
                log.info("Task was interrupted during file parsing: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            // Save detail records
            batchBindingDetailService.batchCreate(taskId, details);
            
            // Update task total count
            task.setTotalCount(details.size());
            this.updateById(task);
            
            // Check interruption
            if (taskExecutionManager.isTaskInterrupted(taskId)) {
                log.info("Task was interrupted during detail saving: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            // Execute actual binding operations
            processBindingDetails(task);
            
            // Final interruption check
            if (taskExecutionManager.isTaskInterrupted(taskId)) {
                log.info("Task was interrupted before completion: {}", taskId);
                return CompletableFuture.completedFuture(null);
            }
            
            // Update task status to completed
        task.setStatus(BindingConstants.TaskStatus.COMPLETED); // Completed
            task.setEndTime(new Date());
            task.setUpdateTime(new Date());
            this.updateById(task);
            
            log.info("Task processing completed: {}", taskId);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Exception occurred while processing task: {}", taskId, e);
            
            // Update task status to failed
            BatchBindingTask task = this.getById(taskId);
            task.setStatus(BindingConstants.TaskStatus.FAILED); // Failed
            task.setEndTime(new Date());
            task.setUpdateTime(new Date());
            this.updateById(task);
            return CompletableFuture.completedFuture(null);
        } finally {
            // Clean up resources after task completion
            taskExecutionManager.taskCompleted(taskId);
        }
    }
    
    /**
     * Parse Excel file
     */
    private List<BatchBindingDetail> parseExcelFile(BatchBindingTask task) throws IOException {
        String filePath = System.getProperty("user.dir") + task.getFilePath();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new BusinessException("FILE_NOT_FOUND", BindingConstants.ErrorMessage.FILE_NOT_FOUND + ": " + filePath);
        }
        
        List<BatchBindingDetail> details = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
             
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            int startRow = 1;
            int endRow = sheet.getLastRowNum();
            
            for (int i = startRow; i <= endRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                // Read number, IMSI and ICCID
                String number = getCellValueAsString(row.getCell(0));
                String imsi = getCellValueAsString(row.getCell(1));
                String iccid = getCellValueAsString(row.getCell(2)); // Third column is ICCID
                
                // Skip empty rows (number and IMSI are required, ICCID is optional)
                if (StringUtils.isBlank(number) || StringUtils.isBlank(imsi)) {
                    continue;
                }
                
                // Create detail record
                BatchBindingDetail detail = new BatchBindingDetail();
                detail.setTaskId(task.getTaskId());
                detail.setNumber(number);
                detail.setImsi(imsi);
                detail.setIccid(iccid); // Set ICCID
                detail.setStatus(BindingConstants.ProcessStatus.PENDING); // Pending
                detail.setCreateTime(new Date());
                detail.setUpdateTime(new Date());
                detail.setCreateUserId(task.getCreateUserId());
                detail.setUpdateUserId(task.getUpdateUserId());
                
                details.add(detail);
            }
        }
        
        return details;
    }
    
    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    long longValue = (long) value;
                    if (value == longValue) {
                        return String.valueOf(longValue);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    /**
     * Process binding details
     */
    private void processBindingDetails(BatchBindingTask task) {
        // Query pending details
        LambdaQueryWrapper<BatchBindingDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatchBindingDetail::getTaskId, task.getTaskId())
                   .eq(BatchBindingDetail::getStatus, BindingConstants.ProcessStatus.PENDING); // Pending
                   
        List<BatchBindingDetail> details = batchBindingDetailService.list(queryWrapper);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        
        // Process according to task type
        if (task.getTaskType() != null && task.getTaskType() == BindingConstants.TaskType.UNBIND) {
            // Unbind task
            processUnbindingDetails(task, details);
        } else {
            // Bind task (default)
            processBindingDetailsInternal(task, details);
        }
    }
    
    /**
     * Process binding details (internal method)
     */
    private void processBindingDetailsInternal(BatchBindingTask task, List<BatchBindingDetail> details) {
        int successCount = 0;
        int failCount = 0;
        
        // Batch processing, 100 records per batch
        int batchSize = 100;
        List<List<BatchBindingDetail>> batches = new ArrayList<>();
        for (int i = 0; i < details.size(); i += batchSize) {
            batches.add(details.subList(i, Math.min(i + batchSize, details.size())));
        }
        
        for (List<BatchBindingDetail> batch : batches) {
            // Check interruption before each batch processing
            if (taskExecutionManager.isTaskInterrupted(task.getTaskId())) {
                log.info("Task was interrupted during batch processing: {}", task.getTaskId());
                return;
            }
            
            // Process a batch of data
            List<NumberImsiBinding> bindingList = new ArrayList<>();
            
            for (BatchBindingDetail detail : batch) {
                // Check interruption before processing each detail
                if (taskExecutionManager.isTaskInterrupted(task.getTaskId())) {
                    log.info("Task was interrupted during detail processing: {}", task.getTaskId());
                    return;
                }
                
                try {
                    // Check if number and IMSI are already bound
                    if (numberImsiBindingService.isNumberBound(detail.getNumber())) {
                        // Already bound, mark as failed
                        detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                        detail.setErrorMsg(BindingConstants.ErrorMessage.NUMBER_ALREADY_BOUND);
                        detail.setProcessTime(new Date());
                        detail.setUpdateTime(new Date());
                        failCount++;
                    } else if (numberImsiBindingService.isIccidBound(detail.getIccid())) {
                        // Already bound, mark as failed
                        detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                        detail.setErrorMsg(BindingConstants.ErrorMessage.ICCID_ALREADY_BOUND);
                        detail.setProcessTime(new Date());
                        detail.setUpdateTime(new Date());
                        failCount++;
                    } else {
                        // Not bound, prepare for binding
                        NumberImsiBinding binding = new NumberImsiBinding();
                        binding.setNumber(detail.getNumber());
                        binding.setImsi(detail.getImsi());
                        binding.setIccid(detail.getIccid()); // Set ICCID
                        binding.setBindingType(BindingConstants.BindingType.BATCH); // Batch binding
                        binding.setOperatorUserId(task.getCreateUserId());
                        binding.setCreateUserId(task.getCreateUserId());
                        binding.setUpdateUserId(task.getCreateUserId());
                        
                        bindingList.add(binding);
                        
                        // Mark as success
                        detail.setStatus(BindingConstants.ProcessStatus.SUCCESS); // Success
                        detail.setProcessTime(new Date());
                        detail.setUpdateTime(new Date());
                        successCount++;
                    }
                } catch (Exception e) {
                    // Handle exception, mark as failed
                    detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                    detail.setErrorMsg(e.getMessage());
                    detail.setProcessTime(new Date());
                    detail.setUpdateTime(new Date());
                    failCount++;
                    
                    log.error("Exception occurred while processing binding detail: {}", detail.getDetailId(), e);
                }
            }
            
            // Batch update detail status
            batchBindingDetailService.updateBatchById(batch);
            
            // Execute batch binding
            if (!bindingList.isEmpty()) {
                try {
                    numberImsiBindingService.batchBind(bindingList, task.getCreateUserId());
                    
                    // After successful batch binding, update resource status
                    List<String> numbersToUpdate = new ArrayList<>();
                    List<String> imsiListToUpdate = new ArrayList<>();
                    
                    for (NumberImsiBinding binding : bindingList) {
                        try {
                            // Update ICCID field in number resource table
                            LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                            updateWrapper.eq(NumberResource::getNumber, binding.getNumber())
                                    .set(NumberResource::getIccid, binding.getIccid())
                                    .set(NumberResource::getUpdateTime, new Date());
                            boolean updatedNumber = numberResourceService.update(updateWrapper);
                            if (updatedNumber) {
                                numbersToUpdate.add(binding.getNumber());
                            } else {
                                log.warn("Failed to update number resource ICCID field: number={}", binding.getNumber());
                            }
                            
                            // Collect IMSI for batch status update
                            imsiListToUpdate.add(binding.getImsi());
                            
                            // Update SimCard status to active
                            if (binding.getIccid() != null) {
                                boolean simCardStatusUpdateResult = simCardService.updateStatusByIccid(
                                        binding.getIccid(), 
                                        SimCardStatusEnum.ACTIVATED.getCode()
                                );
                                
                                if (simCardStatusUpdateResult) {
                                    log.info("SimCard status updated to ACTIVATED successfully for ICCID: {}", binding.getIccid());
                                } else {
                                    log.warn("Failed to update SimCard status to ACTIVATED for ICCID: {}", binding.getIccid());
                                }
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred while updating resource status for number: {}, imsi: {}", binding.getNumber(), binding.getImsi(), e);
                        }
                    }
                    
                    // Batch update number status to active
                    if (!numbersToUpdate.isEmpty()) {
                        try {
                            boolean statusUpdateResult = numberResourceService.batchUpdateNumberStatusByNumber(
                                    numbersToUpdate, 
                                    NumberStatusEnum.ACTIVATED.getCode()
                            );
                            
                            if (statusUpdateResult) {
                                log.info("Successfully updated number status to ACTIVATED for numbers: {}", numbersToUpdate);
                            } else {
                                log.error("Failed to update number status to ACTIVATED for numbers: {}", numbersToUpdate);
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred while updating number status", e);
                        }
                    }
                    
                    // Batch update IMSI status to bound
                    if (!imsiListToUpdate.isEmpty()) {
                        try {
                            boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(
                                    imsiListToUpdate, 
                                    ImsiStatusEnum.BOUND.getCode()
                            );
                            
                            if (imsiStatusUpdateResult) {
                                log.info("Successfully updated IMSI status to BOUND for IMSIs: {}", imsiListToUpdate);
                            } else {
                                log.error("Failed to update IMSI status to BOUND for IMSIs: {}", imsiListToUpdate);
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred while updating IMSI status", e);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("Batch binding exception", e);
                    
                    // If batch binding fails, update detail status
                    List<String> numbers = bindingList.stream()
                        .map(NumberImsiBinding::getNumber)
                        .collect(Collectors.toList());
                    
                    for (BatchBindingDetail detail : batch) {
                        if (numbers.contains(detail.getNumber())) {
                            detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                            detail.setErrorMsg(BindingConstants.ErrorMessage.BATCH_BINDING_EXCEPTION + ": " + e.getMessage());
                            detail.setUpdateTime(new Date());
                            successCount--;
                            failCount++;
                        }
                    }
                    
                    // Batch update detail status again
                    batchBindingDetailService.updateBatchById(batch);
                }
            }
            
            // Update task statistics
            task.setSuccessCount(successCount);
            task.setFailCount(failCount);
            this.updateById(task);
        }
        
        // Update final task statistics
        task.setSuccessCount(successCount);
        task.setFailCount(failCount);
        this.updateById(task);
    }
    
    /**
     * Process unbinding details
     */
    private void processUnbindingDetails(BatchBindingTask task, List<BatchBindingDetail> details) {
        int successCount = 0;
        int failCount = 0;
        
        // Batch processing, 100 records per batch
        int batchSize = 100;
        List<List<BatchBindingDetail>> batches = new ArrayList<>();
        for (int i = 0; i < details.size(); i += batchSize) {
            batches.add(details.subList(i, Math.min(i + batchSize, details.size())));
        }
        
        for (List<BatchBindingDetail> batch : batches) {
            // Check interruption before each batch processing
            if (taskExecutionManager.isTaskInterrupted(task.getTaskId())) {
                log.info("Task was interrupted during unbinding batch processing: {}", task.getTaskId());
                return;
            }
            
            List<Long> bindingIds = new ArrayList<>();
            Map<String, String> numberToIccidMap = new HashMap<>(); // Save number to ICCID mapping
            
            for (BatchBindingDetail detail : batch) {
                // Check interruption before processing each detail
                if (taskExecutionManager.isTaskInterrupted(task.getTaskId())) {
                    log.info("Task was interrupted during unbinding detail processing: {}", task.getTaskId());
                    return;
                }
                
                try {
                    // Find binding relationship by number
                    NumberImsiBinding binding = numberImsiBindingService.getByNumber(detail.getNumber());
                    if (binding == null) {
                        // Binding relationship not found, mark as failed
                        detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                        detail.setErrorMsg(BindingConstants.ErrorMessage.BINDING_NOT_FOUND);
                        detail.setProcessTime(new Date());
                        detail.setUpdateTime(new Date());
                        failCount++;
                    } else {
                        // Verify if IMSI matches
                        if (!binding.getImsi().equals(detail.getImsi())) {
                            detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                            detail.setErrorMsg(BindingConstants.ErrorMessage.IMSI_MISMATCH);
                            detail.setProcessTime(new Date());
                            detail.setUpdateTime(new Date());
                            failCount++;
                        } else if (StringUtils.isNotBlank(detail.getIccid()) && 
                                   StringUtils.isNotBlank(binding.getIccid()) && 
                                   !binding.getIccid().equals(detail.getIccid())) {
                            // Verify if ICCID matches (when both are provided)
                            detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                            detail.setErrorMsg(BindingConstants.ErrorMessage.ICCID_MISMATCH);
                            detail.setProcessTime(new Date());
                            detail.setUpdateTime(new Date());
                            failCount++;
                        } else {
                            // Prepare for unbinding
                            bindingIds.add(binding.getBindingId());
                            // Save ICCID information for later status update
                            if (binding.getIccid() != null) {
                                numberToIccidMap.put(detail.getNumber(), binding.getIccid());
                            }
                            
                            // Mark as success
                            detail.setStatus(BindingConstants.ProcessStatus.SUCCESS); // Success
                            detail.setProcessTime(new Date());
                            detail.setUpdateTime(new Date());
                            successCount++;
                        }
                    }
                } catch (Exception e) {
                    // Handle exception, mark as failed
                detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                    detail.setErrorMsg(e.getMessage());
                    detail.setProcessTime(new Date());
                    detail.setUpdateTime(new Date());
                    failCount++;
                    
                    log.error("Exception occurred while processing unbinding detail: {}", detail.getDetailId(), e);
                }
            }
            
            // Batch update detail status
            batchBindingDetailService.updateBatchById(batch);
            
            // Execute batch unbinding
            if (!bindingIds.isEmpty()) {
                try {
                    // Construct BatchUnbindRequest object
                    BatchUnbindRequest unbindRequest = new BatchUnbindRequest();
                    List<BatchUnbindRequest.UnbindItem> unbindItems = new ArrayList<>();
                    
                    for (BatchBindingDetail detail : batch) {
                        if (detail.getStatus() == BindingConstants.ProcessStatus.SUCCESS) {
                            BatchUnbindRequest.UnbindItem item = new BatchUnbindRequest.UnbindItem();
                            item.setNumber(detail.getNumber());
                            item.setImsi(detail.getImsi());
                            item.setIccid(numberToIccidMap.get(detail.getNumber()));
                            unbindItems.add(item);
                        }
                    }
                    
                    unbindRequest.setUnbindItems(unbindItems);
                    unbindRequest.setOperatorUserId(task.getCreateUserId());
                    unbindRequest.setRemark("Batch unbinding task");
                    
                    numberImsiBindingService.batchUnbindV2(unbindRequest);
                    
                    // After successful batch unbinding, update resource status
                    List<String> numbersToUpdate = new ArrayList<>();
                    List<String> imsiListToUpdate = new ArrayList<>();
                    
                    for (BatchBindingDetail detail : batch) {
                         if (detail.getStatus() == BindingConstants.ProcessStatus.SUCCESS) {
                             try {
                                 // Get ICCID from previously saved mapping
                                 String iccid = numberToIccidMap.get(detail.getNumber());
                                 
                                 // Clear ICCID field in number resource table
                                 LambdaUpdateWrapper<NumberResource> updateWrapper = new LambdaUpdateWrapper<>();
                                 updateWrapper.eq(NumberResource::getNumber, detail.getNumber())
                                         .set(NumberResource::getIccid, null) // Clear ICCID
                                         .set(NumberResource::getUpdateTime, new Date());
                                 boolean updatedNumber = numberResourceService.update(updateWrapper);
                                 if (updatedNumber) {
                                     numbersToUpdate.add(detail.getNumber());
                                 } else {
                                     log.warn("Failed to clear number resource ICCID field: number={}", detail.getNumber());
                                 }
                                 
                                 // Collect IMSI for batch status update
                                 imsiListToUpdate.add(detail.getImsi());
                                 
                                 // Update SIM card status to released
                                 if (iccid != null) {
                                     boolean simCardUpdateResult = simCardService.updateStatusByIccid(
                                             iccid, 
                                             SimCardStatusEnum.PUBLISHED.getCode()
                                     );
                                     if (simCardUpdateResult) {
                                         log.info("Successfully updated SIM card status to PUBLISHED for ICCID: {}", iccid);
                                     } else {
                                         log.error("Failed to update SIM card status to PUBLISHED for ICCID: {}", iccid);
                                     }
                                 }
                             } catch (Exception e) {
                                 log.error("Exception occurred while updating resource status for number: {}, imsi: {}", detail.getNumber(), detail.getImsi(), e);
                             }
                         }
                     }
                    
                    // Batch update number status to idle
                    if (!numbersToUpdate.isEmpty()) {
                        try {
                            boolean statusUpdateResult = numberResourceService.batchUpdateNumberStatusByNumber(
                                    numbersToUpdate, 
                                    NumberStatusEnum.IDLE.getCode()
                            );
                            
                            if (statusUpdateResult) {
                                log.info("Successfully updated number status to IDLE for numbers: {}", numbersToUpdate);
                            } else {
                                log.error("Failed to update number status to IDLE for numbers: {}", numbersToUpdate);
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred while updating number status", e);
                        }
                    }
                    
                    // Batch update IMSI status to idle
                    if (!imsiListToUpdate.isEmpty()) {
                        try {
                            boolean imsiStatusUpdateResult = imsiResourceService.batchUpdateImsiStatusByImsi(
                                    imsiListToUpdate, 
                                    ImsiStatusEnum.IDLE.getCode()
                            );
                            
                            if (imsiStatusUpdateResult) {
                                log.info("Successfully updated IMSI status to IDLE for IMSIs: {}", imsiListToUpdate);
                            } else {
                                log.error("Failed to update IMSI status to IDLE for IMSIs: {}", imsiListToUpdate);
                            }
                        } catch (Exception e) {
                            log.error("Exception occurred while updating IMSI status", e);
                        }
                    }
                    
                } catch (Exception e) {
                    log.error("Batch unbinding exception", e);
                    
                    // If batch unbinding fails, update detail status
                    for (BatchBindingDetail detail : batch) {
                        if (detail.getStatus() == BindingConstants.ProcessStatus.SUCCESS) { // Previously marked as success
                        detail.setStatus(BindingConstants.ProcessStatus.FAILED); // Failed
                            detail.setErrorMsg(BindingConstants.ErrorMessage.BATCH_UNBINDING_EXCEPTION + ": " + e.getMessage());
                            detail.setUpdateTime(new Date());
                            successCount--;
                            failCount++;
                        }
                    }
                    
                    // Batch update detail status again
                    batchBindingDetailService.updateBatchById(batch);
                }
            }
            
            // Update task statistics
            task.setSuccessCount(successCount);
            task.setFailCount(failCount);
            this.updateById(task);
        }
        
        // Update final task statistics
        task.setSuccessCount(successCount);
        task.setFailCount(failCount);
        this.updateById(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateTask(BatchBindingTask task) {
        if (task.getTaskId() == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_REQUIRED);
        }
        
        BatchBindingTask existingTask = this.getById(task.getTaskId());
        if (existingTask == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        if (existingTask.getStatus() != BindingConstants.TaskStatus.PENDING) {
            return CommonResult.failed(BindingConstants.ErrorMessage.ONLY_PENDING_TASK_CAN_BE_MODIFIED);
        }
        
        existingTask.setTaskName(task.getTaskName());
        existingTask.setUpdateTime(new Date());
        
        boolean updateResult = this.updateById(existingTask);
        if (!updateResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.UPDATE_TASK_FAILED);
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> deleteTasks(List<Long> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_LIST_REQUIRED);
        }
        
        // Delete task-related detail records
        for (Long taskId : taskIds) {
            batchBindingDetailService.deleteByTaskId(taskId);
        }
        
        // Delete task records
        boolean removeResult = this.removeByIds(taskIds);
        if (!removeResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.DELETE_TASK_FAILED);
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> cancelTask(Long taskId) {
        BatchBindingTask task = this.getById(taskId);
        if (task == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() != BindingConstants.TaskStatus.PENDING && task.getStatus() != BindingConstants.TaskStatus.PROCESSING) {
            return CommonResult.failed(BindingConstants.ErrorMessage.ONLY_PENDING_OR_PROCESSING_TASK_CAN_BE_CANCELLED);
        }
        // Update task status to cancelled
        task.setStatus(BindingConstants.TaskStatus.FAILED); // Failed
        task.setEndTime(new Date());
        task.setUpdateTime(new Date());
        
        boolean updateResult = this.updateById(task);
        if (!updateResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.CANCEL_TASK_FAILED);
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> retryTask(Long taskId) {
        BatchBindingTask task = this.getById(taskId);
        if (task == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        if (task.getStatus() != BindingConstants.TaskStatus.FAILED) {
            return CommonResult.failed(BindingConstants.ErrorMessage.ONLY_FAILED_TASK_CAN_BE_RETRIED);
        }
        
        task.setStatus(BindingConstants.TaskStatus.PENDING); // Pending
        task.setStartTime(null);
        task.setEndTime(null);
        task.setUpdateTime(new Date());
        task.setSuccessCount(0);
        task.setFailCount(0);
        
        boolean updateResult = this.updateById(task);
        if (!updateResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.RETRY_TASK_FAILED);
        }
        
        // Reset detail record status
        LambdaQueryWrapper<BatchBindingDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BatchBindingDetail::getTaskId, taskId)
                   .eq(BatchBindingDetail::getStatus, BindingConstants.ProcessStatus.FAILED); // Failed records
                   
        List<BatchBindingDetail> details = batchBindingDetailService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(details)) {
            for (BatchBindingDetail detail : details) {
                detail.setStatus(BindingConstants.ProcessStatus.PENDING); // Reset to pending
                detail.setErrorMsg(null);
                detail.setProcessTime(null);
                detail.setUpdateTime(new Date());
            }
            
            batchBindingDetailService.updateBatchById(details);
        }
        
        // Process task asynchronously and register to task manager
        Future<Void> future = processTaskAsync(taskId);
        taskExecutionManager.registerTask(taskId, future);
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> processTask(Long taskId) {
        if (taskId == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_REQUIRED);
        }
        
        BatchBindingTask task = this.getById(taskId);
        if (task == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        // Only pending tasks can be processed
        if (task.getStatus() != BindingConstants.TaskStatus.PENDING) {
            return CommonResult.failed(BindingConstants.ErrorMessage.ONLY_PENDING_TASK_CAN_BE_PROCESSED);
        }
        
        // Start async processing and register to task manager
        Future<Void> future = processTaskAsync(taskId);
        taskExecutionManager.registerTask(taskId, future);
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> startTask(Long taskId) {
        if (taskId == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_REQUIRED);
        }
        
        BatchBindingTask task = this.getById(taskId);
        if (task == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        // Only pending tasks can be started
        if (task.getStatus() != BindingConstants.TaskStatus.PENDING) {
            return CommonResult.failed(BindingConstants.ErrorMessage.ONLY_PENDING_TASK_CAN_BE_PROCESSED);
        }
        
        // Start async processing and register to task manager
        Future<Void> future = processTaskAsync(taskId);
        taskExecutionManager.registerTask(taskId, future);
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> stopTask(Long taskId) {
        if (taskId == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_ID_REQUIRED);
        }
        
        BatchBindingTask task = this.getById(taskId);
        if (task == null) {
            return CommonResult.failed(BindingConstants.ErrorMessage.TASK_NOT_FOUND);
        }
        
        // Only processing tasks can be stopped
        if (task.getStatus() != BindingConstants.TaskStatus.PROCESSING) {
            return CommonResult.failed("Only processing tasks can be stopped");
        }
        
        // Try to stop the running task
        boolean stopped = taskExecutionManager.stopTask(taskId);
        
        if (stopped) {
            // Update task status to failed (stopped)
            task.setStatus(BindingConstants.TaskStatus.FAILED);
            task.setEndTime(new Date());
            task.setUpdateTime(new Date());
            
            boolean updateResult = this.updateById(task);
            if (!updateResult) {
                return CommonResult.failed("Failed to stop task");
            }
            
            log.info("Task stopped successfully: {}", taskId);
        } else {
            log.warn("Failed to stop task, it may have already completed: {}", taskId);
            return CommonResult.failed("Failed to stop task, it may have already completed");
        }
        
        return CommonResult.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<String> testTransaction(boolean throwException) {
        // Create a test task
        BatchBindingTask testTask = new BatchBindingTask();
        testTask.setTaskName("Transaction test task-" + System.currentTimeMillis());
        testTask.setStatus(BindingConstants.TaskStatus.PENDING);
        Date now = new Date();
        testTask.setCreateTime(now);
        testTask.setUpdateTime(now);
        testTask.setTotalCount(0);
        testTask.setSuccessCount(0);
        testTask.setFailCount(0);
        
        // Save task to database
        boolean saveResult = this.save(testTask);
        if (!saveResult) {
            return CommonResult.failed(BindingConstants.ErrorMessage.SAVE_TEST_TASK_FAILED);
        }
        
        log.info("Transaction test: Task saved, ID={}, throwException={}", testTask.getTaskId(), throwException);
        
        // If exception needs to be thrown, throw exception to trigger transaction rollback
        if (throwException) {
            log.info("Transaction test: About to throw exception, expecting transaction rollback");
            throw new RuntimeException("Transaction rollback test - This is an intentionally thrown exception");
        }
        
        return CommonResult.success("Transaction test successful, task ID: " + testTask.getTaskId());
    }
    
    @Override
     public void downloadBindingTemplate(javax.servlet.http.HttpServletResponse response) {
         try {
             // Create template data with example
             List<BatchBindingTemplateDto> templateData = new ArrayList<>();
             BatchBindingTemplateDto example = new BatchBindingTemplateDto();
             example.setNumber("13800138000");
             example.setImsi("460001234567890");
             example.setIccid("89860012345678901234");
             example.setRemark("示例数据，请删除此行后填入实际数据");
             templateData.add(example);
             
             // Download template using ExcelUtils
             ExcelUtils.downloadTemplate(response, BatchBindingTemplateDto.class, templateData, 
                     "批量绑定模板", "batch_binding_template");
             
         } catch (Exception e) {
             log.error("Failed to download binding template", e);
             throw new RuntimeException("Failed to download template: " + e.getMessage());
         }
     }
    
    @Override
     public void downloadUnbindTemplate(javax.servlet.http.HttpServletResponse response) {
         try {
             // Create template data with example
             List<BatchUnbindTemplateDto> templateData = new ArrayList<>();
             BatchUnbindTemplateDto example = new BatchUnbindTemplateDto();
             example.setNumber("13800138000");
             example.setImsi("460001234567890");
             example.setIccid("89860012345678901234");
             example.setRemark("示例数据，请删除此行后填入实际数据");
             templateData.add(example);
             
             // Download template using ExcelUtils
             ExcelUtils.downloadTemplate(response, BatchUnbindTemplateDto.class, templateData, 
                     "批量解绑模板", "batch_unbind_template");
             
         } catch (Exception e) {
             log.error("Failed to download unbind template", e);
             throw new RuntimeException("Failed to download template: " + e.getMessage());
         }
     }
}
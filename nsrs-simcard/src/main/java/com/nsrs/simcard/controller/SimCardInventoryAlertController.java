package com.nsrs.simcard.controller;

import com.nsrs.common.core.domain.PageRequest;
import com.nsrs.common.core.domain.PageResult;
import com.nsrs.common.model.CommonResult;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertDTO;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertLogDTO;
import com.nsrs.simcard.model.query.SimCardInventoryAlertQuery;
import com.nsrs.simcard.model.query.SimCardInventoryAlertLogQuery;
import com.nsrs.simcard.service.SimCardAlertNotificationService;
import com.nsrs.simcard.service.SimCardInventoryAlertLogService;
import com.nsrs.simcard.service.SimCardInventoryAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SIM Card Inventory Alert Unified Controller
 * 统一管理告警配置、告警日志和告警操作的Controller
 */
@Slf4j
@Tag(name = "SIM Card Inventory Alert Management", description = "SIM card inventory alert unified management")
@RestController
@RequestMapping("/simcard/inventory-alert")
public class SimCardInventoryAlertController {

    @Autowired
    private SimCardInventoryAlertService alertService;
    
    @Autowired
    private SimCardInventoryAlertLogService alertLogService;
    
    @Autowired
    private SimCardAlertNotificationService notificationService;

    // ==================== 告警配置管理 ====================
    
    /**
     * 分页查询告警配置列表
     */
    @PostMapping("/config/page")
    @Operation(summary = "Paginated Query Alert Configuration List")
    public CommonResult<PageResult<SimCardInventoryAlertDTO>> pageConfig(@Valid @RequestBody PageRequest<SimCardInventoryAlertQuery> request) {
        PageResult<SimCardInventoryAlertDTO> pageResult = alertService.pageAlert(request);
        return CommonResult.success(pageResult);
    }

    /**
     * 获取告警配置详情
     */
    @GetMapping("/config/{id}")
    @Operation(summary = "Get Alert Configuration Details")
    public CommonResult<SimCardInventoryAlertDTO> getConfigDetail(@Parameter(description = "Alert Configuration ID") @PathVariable Long id) {
        SimCardInventoryAlertDTO alertDTO = alertService.getAlertDetail(id);
        return alertDTO != null ? CommonResult.success(alertDTO) : CommonResult.failed("Alert configuration not found");
    }

    /**
     * 添加告警配置
     */
    @PostMapping("/config/add")
    @Operation(summary = "Add Alert Configuration")
    public CommonResult<Void> addConfig(@Valid @RequestBody SimCardInventoryAlertDTO alertDTO) {
        return alertService.addAlert(alertDTO) ? CommonResult.success() : CommonResult.failed("Failed to add alert configuration");
    }

    /**
     * 更新告警配置
     */
    @PutMapping("/config/update")
    @Operation(summary = "Update Alert Configuration")
    public CommonResult<Void> updateConfig(@Valid @RequestBody SimCardInventoryAlertDTO alertDTO) {
        return alertService.updateAlert(alertDTO) ? CommonResult.success() : CommonResult.failed("Failed to update alert configuration");
    }

    /**
     * 删除告警配置
     */
    @DeleteMapping("/config/{id}")
    @Operation(summary = "Delete Alert Configuration")
    public CommonResult<Void> deleteConfig(@Parameter(description = "Alert Configuration ID") @PathVariable Long id) {
        return alertService.deleteAlert(id) ? CommonResult.success() : CommonResult.failed("Failed to delete alert configuration");
    }

    /**
     * 更新告警配置状态（启用/禁用）
     */
    @PutMapping("/config/{id}/status/{isActive}")
    @Operation(summary = "Update Alert Configuration Status")
    public CommonResult<Void> updateConfigStatus(
            @Parameter(description = "Alert Configuration ID") @PathVariable Long id, 
            @Parameter(description = "Status: 0-Disabled, 1-Enabled") @PathVariable Integer isActive) {
        return alertService.updateAlertStatus(id, isActive) ? CommonResult.success() : CommonResult.failed("Failed to update alert configuration status");
    }

    /**
     * 获取所有活跃的告警配置
     */
    @GetMapping("/config/active")
    @Operation(summary = "Get All Active Alert Configurations")
    public CommonResult<List<SimCardInventoryAlertDTO>> listActiveConfigs() {
        List<SimCardInventoryAlertDTO> activeAlerts = alertService.listActiveAlerts();
        return CommonResult.success(activeAlerts);
    }
    
    // ==================== 告警日志管理 ====================
    
    /**
     * 分页查询告警日志列表
     */
    @PostMapping("/log/page")
    @Operation(summary = "Paginated Query Alert Log List")
    public CommonResult<PageResult<SimCardInventoryAlertLogDTO>> pageLog(@Valid @RequestBody PageRequest<SimCardInventoryAlertLogQuery> request) {
        PageResult<SimCardInventoryAlertLogDTO> pageResult = alertLogService.pageAlertLog(request);
        return CommonResult.success(pageResult);
    }
    
    /**
     * 获取告警日志详情
     */
    @GetMapping("/log/{id}")
    @Operation(summary = "Get Alert Log Details")
    public CommonResult<SimCardInventoryAlertLogDTO> getLogDetail(@Parameter(description = "Alert Log ID") @PathVariable Long id) {
        SimCardInventoryAlertLogDTO dto = alertLogService.getAlertLogDetail(id);
        return dto != null ? CommonResult.success(dto) : CommonResult.failed("Alert log does not exist");
    }
    
    /**
     * 根据告警ID获取所有日志
     */
    @GetMapping("/log/alert/{alertId}")
    @Operation(summary = "Get All Logs for Specified Alert ID")
    public CommonResult<List<SimCardInventoryAlertLogDTO>> getLogsByAlertId(@Parameter(description = "Alert Configuration ID") @PathVariable Long alertId) {
        List<SimCardInventoryAlertLogDTO> logList = alertLogService.getLogsByAlertId(alertId);
        return CommonResult.success(logList);
    }
    
    /**
     * 更新告警日志通知状态
     */
    @PutMapping("/log/{id}/notify/{notifyStatus}")
    @Operation(summary = "Update Alert Log Notification Status")
    public CommonResult<Void> updateLogNotifyStatus(
            @Parameter(description = "Alert Log ID") @PathVariable Long id, 
            @Parameter(description = "Notification Status: 0-Not notified, 1-Notified, 2-Failed") @PathVariable Integer notifyStatus) {
        boolean result = alertLogService.updateNotifyStatus(id, notifyStatus);
        return result ? CommonResult.success() : CommonResult.failed("Failed to update notification status");
    }
    
    /**
     * 统计未通知的告警日志数量
     */
    @GetMapping("/log/count/unnotified")
    @Operation(summary = "Count Unnotified Alert Logs")
    public CommonResult<Integer> countUnnotifiedLogs() {
        int count = alertLogService.countUnnotifiedLogs();
        return CommonResult.success(count);
    }
    
    /**
     * 获取最近的告警日志
     */
    @GetMapping("/log/recent")
    @Operation(summary = "Get Recent Alert Logs")
    public CommonResult<List<SimCardInventoryAlertLogDTO>> getRecentLogs(@Parameter(description = "Quantity limit") @RequestParam(defaultValue = "10") int limit) {
        List<SimCardInventoryAlertLogDTO> logList = alertLogService.getRecentLogs(limit);
        return CommonResult.success(logList);
    }
    
    // ==================== 告警操作管理 ====================
    
    /**
     * 手动触发库存检查
     */
    @PostMapping("/operation/check")
    @Operation(summary = "Manually Trigger Inventory Check")
    public CommonResult<String> triggerInventoryCheck() {
        try {
            String result = alertService.triggerManualAlertCheck();
            log.info("Manual inventory check triggered successfully");
            return CommonResult.success(result);
        } catch (Exception e) {
            log.error("Failed to trigger inventory check", e);
            return CommonResult.failed("Failed to trigger inventory check: " + e.getMessage());
        }
    }
    
    /**
     * 处理待发送的通知
     */
    @PostMapping("/operation/process-notifications")
    @Operation(summary = "Process Pending Notifications")
    public CommonResult<String> processPendingNotifications() {
        try {
            notificationService.processPendingNotifications();
            log.info("Pending notifications processed successfully");
            return CommonResult.success("Pending notifications processed successfully");
        } catch (Exception e) {
            log.error("Failed to process pending notifications", e);
            return CommonResult.failed("Failed to process pending notifications: " + e.getMessage());
        }
    }
    
    /**
     * 重试失败的通知
     */
    @PostMapping("/operation/retry-failed-notifications")
    @Operation(summary = "Retry Failed Notifications")
    public CommonResult<String> retryFailedNotifications() {
        try {
            notificationService.retryFailedNotifications();
            log.info("Failed notifications retry triggered successfully");
            return CommonResult.success("Failed notifications retry triggered successfully");
        } catch (Exception e) {
            log.error("Failed to retry failed notifications", e);
            return CommonResult.failed("Failed to retry failed notifications: " + e.getMessage());
        }
    }
    
    /**
     * 获取告警系统状态
     */
    @GetMapping("/status")
    @Operation(summary = "Get Alert System Status")
    public CommonResult<Map<String, Object>> getAlertSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 统计活跃的告警配置数量
        List<SimCardInventoryAlertDTO> activeAlerts = alertService.listActiveAlerts();
        status.put("activeAlertCount", activeAlerts.size());
        
        // 统计未通知的告警日志数量
        int unnotifiedCount = alertLogService.countUnnotifiedLogs();
        status.put("unnotifiedLogCount", unnotifiedCount);
        
        // 获取最近的告警日志
        List<SimCardInventoryAlertLogDTO> recentLogs = alertLogService.getRecentLogs(5);
        status.put("recentLogs", recentLogs);
        
        return CommonResult.success(status);
    }
}
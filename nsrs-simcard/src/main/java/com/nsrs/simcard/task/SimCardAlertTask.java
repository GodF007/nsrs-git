package com.nsrs.simcard.task;

import com.nsrs.simcard.constants.AlertConstant;
import com.nsrs.simcard.service.SimCardAlertNotificationService;
import com.nsrs.simcard.service.SimCardInventoryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SIM Card Alert Scheduled Task
 * Handles periodic alert checking and notification sending
 */
@Slf4j
@Component
public class SimCardAlertTask {
    
    @Autowired
    private SimCardInventoryAlertService alertService;
    
    @Autowired
    private SimCardAlertNotificationService notificationService;
    
    /**
     * Check inventory and generate alerts
     * Runs every 30 minutes
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void checkInventoryAlerts() {
        try {
            log.info("Starting scheduled inventory alert check");
            
            // 清理过期缓存
            alertService.clearExpiredCache();
            
            int alertCount = alertService.checkInventoryAndGenerateAlerts();
            
            log.info("Scheduled inventory alert check completed, generated {} alerts", alertCount);
            
        } catch (Exception e) {
            log.error(AlertConstant.LOG_ALERT_TASK_ERROR, e.getMessage(), e);
        }
    }
    
    /**
     * Process pending notifications
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes
    public void processPendingNotifications() {
        try {
            log.info("Starting to process pending alert notifications");
            
            int processedCount = notificationService.processPendingNotifications();
            
            log.info("Processed {} pending alert notifications", processedCount);
            
        } catch (Exception e) {
            log.error("Error occurred during pending notification processing: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Retry failed notifications
     * Runs every hour
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    public void retryFailedNotifications() {
        try {
            log.info("Starting to retry failed alert notifications");
            
            int retriedCount = notificationService.retryFailedNotifications();
            
            log.info("Retried {} failed alert notifications", retriedCount);
            
        } catch (Exception e) {
            log.error("Error occurred during failed notification retry: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual trigger for alert check
     * Can be called via API or management interface
     */
    public void triggerAlertCheck() {
        try {
            log.info("Manual alert check triggered");
            
            int alertCount = alertService.checkInventoryAndGenerateAlerts();
            int processedCount = notificationService.processPendingNotifications();
            
            log.info("Manual alert check completed, generated {} alerts, processed {} notifications", 
                    alertCount, processedCount);
            
        } catch (Exception e) {
            log.error("Error occurred during manual alert check: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Manual trigger for notification processing
     * Can be called via API or management interface
     */
    public void triggerNotificationProcessing() {
        try {
            log.info("Manual notification processing triggered");
            
            int processedCount = notificationService.processPendingNotifications();
            int retriedCount = notificationService.retryFailedNotifications();
            
            log.info("Manual notification processing completed, processed {} pending, retried {} failed", 
                    processedCount, retriedCount);
            
        } catch (Exception e) {
            log.error("Error occurred during manual notification processing: {}", e.getMessage(), e);
        }
    }

}
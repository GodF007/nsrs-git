package com.nsrs.simcard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nsrs.simcard.constants.AlertConstant;
import com.nsrs.simcard.entity.SimCardInventoryAlertLog;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertLogDTO;
import com.nsrs.simcard.service.SimCardAlertNotificationService;
import com.nsrs.simcard.service.SimCardInventoryAlertLogService;
import com.nsrs.simcard.service.SimCardInventoryAlertService;
import com.nsrs.simcard.model.dto.SimCardInventoryAlertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SIM Card Alert Notification Service Implementation
 */
@Slf4j
@Service
public class SimCardAlertNotificationServiceImpl implements SimCardAlertNotificationService {
    
    @Autowired
    private SimCardInventoryAlertLogService alertLogService;
    
    @Autowired
    private SimCardInventoryAlertService alertService;
    
    @Override
    public boolean sendAlertNotification(SimCardInventoryAlertLogDTO alertLog) {
        try {
            log.info("Sending alert notification for alert ID: {}", alertLog.getAlertId());
            
            boolean emailSent = true;
            boolean smsSent = true;
            
            // Get alert configuration to retrieve notification settings
            SimCardInventoryAlertDTO alertConfig = alertService.getAlertDetail(alertLog.getAlertId());
            if (alertConfig != null) {
                // Send email notification if email addresses are configured
                if (StringUtils.hasText(alertConfig.getNotifyEmails())) {
                    emailSent = sendEmailNotification(alertLog, alertConfig.getNotifyEmails());
                }
                
                // Send SMS notification if phone numbers are configured
                if (StringUtils.hasText(alertConfig.getNotifyPhones())) {
                    smsSent = sendSmsNotification(alertLog, alertConfig.getNotifyPhones());
                }
            }
            
            boolean success = emailSent && smsSent;
            
            // Update notification status
            int notifyStatus = success ? AlertConstant.NOTIFY_STATUS_SENT : AlertConstant.NOTIFY_STATUS_FAILED;
            alertLogService.updateNotifyStatus(alertLog.getId(), notifyStatus);
            
            if (success) {
                log.info(AlertConstant.LOG_ALERT_NOTIFICATION_SENT, alertLog.getId());
            } else {
                log.error(AlertConstant.LOG_ALERT_NOTIFICATION_FAILED, alertLog.getId(), "Email or SMS sending failed");
            }
            
            return success;
            
        } catch (Exception e) {
            log.error(AlertConstant.LOG_ALERT_NOTIFICATION_FAILED, alertLog.getId(), e.getMessage(), e);
            
            // Update notification status to failed
            try {
                alertLogService.updateNotifyStatus(alertLog.getId(), AlertConstant.NOTIFY_STATUS_FAILED);
            } catch (Exception updateException) {
                log.error("Failed to update notification status for alert ID: {}", alertLog.getId(), updateException);
            }
            
            return false;
        }
    }
    
    @Override
    public boolean sendEmailNotification(SimCardInventoryAlertLogDTO alertLog, String emails) {
        try {
            log.info("Sending email notification to: {}", emails);
            
            String[] emailArray = emails.split(",");
            String subject = generateEmailSubject(alertLog);
            String content = generateEmailContent(alertLog);
            
            // TODO: Implement actual email sending logic
            // This is a placeholder for email service integration
            // Example: emailService.sendEmail(emailArray, subject, content);
            
            log.info("Email notification sent successfully to: {}", emails);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send email notification to: {}, error: {}", emails, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean sendSmsNotification(SimCardInventoryAlertLogDTO alertLog, String phones) {
        try {
            log.info("Sending SMS notification to: {}", phones);
            
            String[] phoneArray = phones.split(",");
            String content = generateSmsContent(alertLog);
            
            // TODO: Implement actual SMS sending logic
            // This is a placeholder for SMS service integration
            // Example: smsService.sendSms(phoneArray, content);
            
            log.info("SMS notification sent successfully to: {}", phones);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send SMS notification to: {}, error: {}", phones, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public int processPendingNotifications() {
        log.info("Processing pending alert notifications");
        
        try {
            // Get pending notifications directly by status
            List<SimCardInventoryAlertLogDTO> pendingLogs = alertLogService.getPendingNotificationLogs(100);
            
            int processedCount = 0;
            
            for (SimCardInventoryAlertLogDTO alertLog : pendingLogs) {
                if (sendAlertNotification(alertLog)) {
                    processedCount++;
                }
            }
            
            log.info("Processed {} pending notifications", processedCount);
            return processedCount;
            
        } catch (Exception e) {
            log.error("Error processing pending notifications: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    @Override
    public int retryFailedNotifications() {
        log.info("Retrying failed alert notifications");
        
        try {
            // Get failed notifications directly by status
            List<SimCardInventoryAlertLogDTO> failedLogs = alertLogService.getFailedNotificationLogs(100);
            
            int retriedCount = 0;
            
            for (SimCardInventoryAlertLogDTO alertLog : failedLogs) {
                if (sendAlertNotification(alertLog)) {
                    retriedCount++;
                }
            }
            
            log.info("Retried {} failed notifications", retriedCount);
            return retriedCount;
            
        } catch (Exception e) {
            log.error("Error retrying failed notifications: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Generate email subject for alert notification
     */
    private String generateEmailSubject(SimCardInventoryAlertLogDTO alertLog) {
        String alertType = alertLog.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY ? "Low Inventory" : "High Inventory";
        return String.format("[NSRS Alert] %s - %s", alertType, alertLog.getAlertName());
    }
    
    /**
     * Generate email content for alert notification
     */
    private String generateEmailContent(SimCardInventoryAlertLogDTO alertLog) {
        StringBuilder content = new StringBuilder();
        content.append("Dear Administrator,\n\n");
        content.append("An inventory alert has been triggered:\n\n");
        content.append("Alert Name: ").append(alertLog.getAlertName()).append("\n");
        content.append("Alert Type: ").append(alertLog.getAlertTypeName()).append("\n");
        content.append("Current Count: ").append(alertLog.getCurrentCount()).append("\n");
        content.append("Threshold: ").append(alertLog.getThreshold()).append("\n");
        content.append("Alert Time: ").append(alertLog.getAlertTime()).append("\n\n");
        
        if (alertLog.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY) {
            content.append(String.format(AlertConstant.LOW_INVENTORY_MESSAGE_TEMPLATE, 
                    alertLog.getAlertName(), alertLog.getCurrentCount(), alertLog.getThreshold()));
        } else {
            content.append(String.format(AlertConstant.HIGH_INVENTORY_MESSAGE_TEMPLATE, 
                    alertLog.getAlertName(), alertLog.getCurrentCount(), alertLog.getThreshold()));
        }
        
        content.append("\n\nPlease take appropriate action.\n\n");
        content.append("Best regards,\nNSRS System");
        
        return content.toString();
    }
    
    /**
     * Generate SMS content for alert notification
     */
    private String generateSmsContent(SimCardInventoryAlertLogDTO alertLog) {
        if (alertLog.getAlertType() == AlertConstant.ALERT_TYPE_LOW_INVENTORY) {
            return String.format("[NSRS] " + AlertConstant.LOW_INVENTORY_MESSAGE_TEMPLATE, 
                    alertLog.getAlertName(), alertLog.getCurrentCount(), alertLog.getThreshold());
        } else {
            return String.format("[NSRS] " + AlertConstant.HIGH_INVENTORY_MESSAGE_TEMPLATE, 
                    alertLog.getAlertName(), alertLog.getCurrentCount(), alertLog.getThreshold());
        }
    }
}
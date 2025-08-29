package com.nsrs.simcard.service;

import com.nsrs.simcard.model.dto.SimCardInventoryAlertLogDTO;

/**
 * SIM Card Alert Notification Service
 * Handles sending alert notifications via email, SMS, etc.
 */
public interface SimCardAlertNotificationService {
    
    /**
     * Send alert notification
     * @param alertLog Alert log information
     * @return true if notification sent successfully, false otherwise
     */
    boolean sendAlertNotification(SimCardInventoryAlertLogDTO alertLog);
    
    /**
     * Send email notification
     * @param alertLog Alert log information
     * @param emails Email addresses to send to
     * @return true if email sent successfully, false otherwise
     */
    boolean sendEmailNotification(SimCardInventoryAlertLogDTO alertLog, String emails);
    
    /**
     * Send SMS notification
     * @param alertLog Alert log information
     * @param phones Phone numbers to send to
     * @return true if SMS sent successfully, false otherwise
     */
    boolean sendSmsNotification(SimCardInventoryAlertLogDTO alertLog, String phones);
    
    /**
     * Process pending notifications
     * Processes all pending alert notifications
     * @return Number of notifications processed
     */
    int processPendingNotifications();
    
    /**
     * Retry failed notifications
     * Retries notifications that failed to send
     * @return Number of notifications retried
     */
    int retryFailedNotifications();
}
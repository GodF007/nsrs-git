package com.nsrs.simcard.constants;

/**
 * Alert Module Constants
 * Unified constants for SIM card inventory alert system
 */
public class AlertConstant {
    
    /**
     * Alert Types
     */
    public static final int ALERT_TYPE_LOW_INVENTORY = 1;
    public static final int ALERT_TYPE_HIGH_INVENTORY = 2;
    
    /**
     * Alert Status
     */
    public static final int ALERT_STATUS_INACTIVE = 0;
    public static final int ALERT_STATUS_ACTIVE = 1;
    
    /**
     * Notification Status
     */
    public static final int NOTIFY_STATUS_PENDING = 0;
    public static final int NOTIFY_STATUS_SENT = 1;
    public static final int NOTIFY_STATUS_FAILED = 2;
    
    /**
     * Alert Level
     */
    public static final int ALERT_LEVEL_LOW = 1;
    public static final int ALERT_LEVEL_MEDIUM = 2;
    public static final int ALERT_LEVEL_HIGH = 3;
    
    /**
     * Alert Processing Status
     */
    public static final int PROCESS_STATUS_PENDING = 0;
    public static final int PROCESS_STATUS_PROCESSING = 1;
    public static final int PROCESS_STATUS_COMPLETED = 2;
    public static final int PROCESS_STATUS_FAILED = 3;
    
    /**
     * Default Alert Check Interval (minutes)
     */
    public static final int DEFAULT_CHECK_INTERVAL = 30;
    
    /**
     * Maximum Alert Retry Count
     */
    public static final int MAX_RETRY_COUNT = 3;
    
    /**
     * Alert Message Templates
     */
    public static final String LOW_INVENTORY_MESSAGE_TEMPLATE = 
        "Low inventory alert: %s current count is %d, below threshold %d";
    
    public static final String HIGH_INVENTORY_MESSAGE_TEMPLATE = 
        "High inventory alert: %s current count is %d, above threshold %d";
    
    /**
     * Log Messages
     */
    public static final String LOG_ALERT_CHECK_START = "Starting inventory alert check";
    public static final String LOG_ALERT_CHECK_COMPLETE = "Inventory alert check completed, generated {} alerts";
    public static final String LOG_ALERT_GENERATED = "Alert generated for config: {}, current count: {}, threshold: {}";
    public static final String LOG_ALERT_NOTIFICATION_SENT = "Alert notification sent successfully for alert ID: {}";
    public static final String LOG_ALERT_NOTIFICATION_FAILED = "Failed to send alert notification for alert ID: {}, error: {}";
    public static final String LOG_ALERT_CONFIG_NOT_FOUND = "Alert configuration not found for ID: {}";
    public static final String LOG_ALERT_TASK_ERROR = "Error occurred during alert task execution: {}";
}
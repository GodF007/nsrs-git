package com.nsrs.simcard.constants;

/**
 * Status Constants
 * Centralized management of all status values to avoid magic numbers
 */
public class StatusConstants {
    
    // Common Status
    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
    
    // SIM Card Status
    public static final int SIMCARD_STATUS_PUBLISHED = 1;
    public static final int SIMCARD_STATUS_ASSIGNED = 2;
    public static final int SIMCARD_STATUS_ACTIVATED = 3;
    public static final int SIMCARD_STATUS_DEACTIVATED = 4;
    public static final int SIMCARD_STATUS_RECYCLED = 5;
    
    // IMSI Status
    public static final int IMSI_STATUS_IDLE = 1;
    public static final int IMSI_STATUS_BOUND = 2;
    public static final int IMSI_STATUS_USED = 3;
    public static final int IMSI_STATUS_LOCKED = 4;
    
    // Operation Result Status
    public static final int OPERATION_RESULT_FAILED = 0;
    public static final int OPERATION_RESULT_SUCCESS = 1;
    
    // Alert Status
    public static final int ALERT_STATUS_INACTIVE = 0;
    public static final int ALERT_STATUS_ACTIVE = 1;
    
    // Notification Status
    public static final int NOTIFY_STATUS_PENDING = 0;
    public static final int NOTIFY_STATUS_SENT = 1;
    public static final int NOTIFY_STATUS_FAILED = 2;
    
    // Approval Status
    public static final int APPROVAL_STATUS_PENDING = 0;
    public static final int APPROVAL_STATUS_APPROVED = 1;
    public static final int APPROVAL_STATUS_REJECTED = 2;
    public static final int APPROVAL_STATUS_CANCELLED = 3;
    
    // Binding Status
    public static final int BINDING_STATUS_BOUND = 1;
    public static final int BINDING_STATUS_UNBOUND = 2;
    
    // Task Status
    public static final int TASK_STATUS_PENDING = 0;
    public static final int TASK_STATUS_PROCESSING = 1;
    public static final int TASK_STATUS_COMPLETED = 2;
    public static final int TASK_STATUS_FAILED = 3;
    
    // Private constructor to prevent instantiation
    private StatusConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
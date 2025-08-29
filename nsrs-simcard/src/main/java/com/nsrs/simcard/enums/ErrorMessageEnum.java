package com.nsrs.simcard.enums;

/**
 * Error Message Enumeration
 */
public enum ErrorMessageEnum {
    
    /**
     * SIM Card related errors
     */
    SIM_CARD_NOT_FOUND("SIM_CARD_NOT_FOUND", "SIM card not found"),
    SIMCARD_NOT_FOUND("SIMCARD_NOT_FOUND", "SIM card does not exist"),
    ICCID_ALREADY_EXISTS("ICCID_ALREADY_EXISTS", "ICCID already exists"),
    SIM_CARD_DELETE_FAILED("SIM_CARD_DELETE_FAILED", "Failed to delete SIM card"),
    SIM_CARD_UPDATE_STATUS_FAILED("SIM_CARD_UPDATE_STATUS_FAILED", "Failed to update SIM card status"),
    SIM_CARD_ALLOCATE_FAILED("SIM_CARD_ALLOCATE_FAILED", "Failed to allocate SIM card"),
    SIM_CARD_ACTIVATE_FAILED("SIM_CARD_ACTIVATE_FAILED", "Failed to activate SIM card"),
    SIM_CARD_DEACTIVATE_FAILED("SIM_CARD_DEACTIVATE_FAILED", "Failed to deactivate SIM card"),
    SIM_CARD_RECYCLE_FAILED("SIM_CARD_RECYCLE_FAILED", "Failed to recycle SIM card"),
    SIM_CARD_BATCH_OPERATION_FAILED("SIM_CARD_BATCH_OPERATION_FAILED", "Batch operation failed"),
    SIM_CARD_ICCID_NOT_FOUND("SIM_CARD_ICCID_NOT_FOUND", "SIM card with specified ICCID not found"),
    
    /**
     * IMSI related errors
     */
    IMSI_NOT_FOUND("IMSI_NOT_FOUND", "IMSI resource not found"),
    IMSI_START_GREATER_THAN_END("IMSI_START_GREATER_THAN_END", "Start IMSI cannot be greater than end IMSI"),
    IMSI_RANGE_INCOMPLETE("IMSI_RANGE_INCOMPLETE", "IMSI range is incomplete"),
    IMSI_GROUP_EXHAUSTED("IMSI_GROUP_EXHAUSTED", "IMSI group is exhausted"),
    IMSI_DELETE_FAILED("IMSI_DELETE_FAILED", "Failed to delete IMSI resource"),
    IMSI_UPDATE_STATUS_FAILED("IMSI_UPDATE_STATUS_FAILED", "Failed to update IMSI status"),
    IMSI_BATCH_UPDATE_STATUS_FAILED("IMSI_BATCH_UPDATE_STATUS_FAILED", "Failed to batch update IMSI status"),
    IMSI_ALREADY_EXISTS("IMSI_ALREADY_EXISTS", "IMSI already exists"),
    IMSI_INVALID_STATUS("IMSI_INVALID_STATUS", "Invalid IMSI status"),
    IMSI_INVALID_TYPE("IMSI_INVALID_TYPE", "Invalid IMSI type"),
    IMSI_INVALID_FORMAT("IMSI_INVALID_FORMAT", "Invalid IMSI format"),
    IMSI_BINDING_FAILED("IMSI_BINDING_FAILED", "IMSI binding failed"),
    IMSI_REQUIRED("IMSI_REQUIRED", "IMSI number is required for query"),
    IMSI_RESOURCE_NOT_FOUND("IMSI_RESOURCE_NOT_FOUND", "IMSI resource does not exist"),
    IMSI_GROUP_NOT_FOUND("IMSI_GROUP_NOT_FOUND", "IMSI group does not exist"),
    
    /**
     * Specification related errors
     */
    SPECIFICATION_NOT_FOUND("SPECIFICATION_NOT_FOUND", "Specification not found"),
    SPECIFICATION_DELETE_FAILED("SPECIFICATION_DELETE_FAILED", "Failed to delete specification"),
    
    /**
     * Batch related errors
     */
    BATCH_NOT_EXISTS("BATCH_NOT_EXISTS", "Batch does not exist"),
    BATCH_NOT_FOUND("BATCH_NOT_FOUND", "Batch not found"),
    
    /**
     * General errors
     */
    OPERATION_FAILED("OPERATION_FAILED", "Operation failed"),
    INVALID_PARAMETER("INVALID_PARAMETER", "Invalid parameter"),
    INVALID_PARAMETERS("INVALID_PARAMETERS", "Invalid parameters"),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Unauthorized access"),
    SYSTEM_ERROR("SYSTEM_ERROR", "System error"),
    UNSUPPORTED_OPERATION_TYPE("UNSUPPORTED_OPERATION_TYPE", "Unsupported operation type"),
    SUPPLIER_NOT_FOUND("SUPPLIER_NOT_FOUND", "Supplier not found"),
    GROUP_ID_REQUIRED("GROUP_ID_REQUIRED", "Group ID is required"),
    BATCH_ID_REQUIRED("BATCH_ID_REQUIRED", "Batch ID is required"),
    INVALID_GENERATE_COUNT("INVALID_GENERATE_COUNT", "Generate count must be between 1-10000"),
    INSUFFICIENT_IMSI_CAPACITY("INSUFFICIENT_IMSI_CAPACITY", "Insufficient IMSI capacity, remaining: "),
    GENERATE_COUNT_EXCEEDS_RANGE("GENERATE_COUNT_EXCEEDS_RANGE", "Generate count exceeds IMSI group range"),
    SIMCARD_ID_LIST_EMPTY("SIMCARD_ID_LIST_EMPTY", "SIM card ID list cannot be empty"),
    ORG_ID_REQUIRED("ORG_ID_REQUIRED", "Organization ID is required"),
    
    /**
     * Binding related errors
     */
    BINDING_PARAMS_REQUIRED("BINDING_PARAMS_REQUIRED", "Number ID, number, IMSI ID or IMSI cannot be empty"),
    NUMBER_ALREADY_BOUND("NUMBER_ALREADY_BOUND", "Number already bound"),
    IMSI_ALREADY_BOUND("IMSI_ALREADY_BOUND", "IMSI already bound"),
    SAVE_BINDING_FAILED("SAVE_BINDING_FAILED", "Failed to save binding relationship"),
    BINDING_ID_REQUIRED("BINDING_ID_REQUIRED", "Binding ID is required"),
    BINDING_NOT_FOUND("BINDING_NOT_FOUND", "Binding relationship not found"),
    UPDATE_BINDING_STATUS_FAILED("UPDATE_BINDING_STATUS_FAILED", "Failed to update binding status"),
    BINDING_LIST_EMPTY("BINDING_LIST_EMPTY", "Binding list cannot be empty"),
    BINDING_ID_LIST_EMPTY("BINDING_ID_LIST_EMPTY", "Binding ID list cannot be empty"),
    
    /**
     * Group related errors
     */
    GROUP_NAME_ALREADY_EXISTS("GROUP_NAME_ALREADY_EXISTS", "Group name already exists"),
    IMSI_GROUP_NOT_EXISTS("IMSI_GROUP_NOT_EXISTS", "IMSI group does not exist"),
    
    /**
     * Type and Specification related errors
     */
    ADD_TYPE_FAILED("ADD_TYPE_FAILED", "Failed to add type"),
    UPDATE_TYPE_FAILED("UPDATE_TYPE_FAILED", "Failed to update type"),
    DELETE_TYPE_FAILED("DELETE_TYPE_FAILED", "The deleted card type does not exist"),
    ADD_SPECIFICATION_FAILED("ADD_SPECIFICATION_FAILED", "Failed to add specification"),
    UPDATE_SPECIFICATION_FAILED("UPDATE_SPECIFICATION_FAILED", "Failed to update specification"),
    DELETE_SPECIFICATION_FAILED("DELETE_SPECIFICATION_FAILED", "The deleted card specification does not exist"),
    SPECIFICATION_NOT_EXISTS("SPECIFICATION_NOT_EXISTS", "Specification not found"),
    TYPE_NOT_EXISTS("TYPE_NOT_EXISTS", "Type not found"),
    
    /**
     * Batch related errors
     */
    BATCH_NOT_EXISTS_MSG("BATCH_NOT_EXISTS_MSG", "Batch does not exist"),
    
    /**
     * Operation related errors
     */
    OPERATION_TYPE_REQUIRED("OPERATION_TYPE_REQUIRED", "Operation type is required"),
    IMPORT_SIMCARD_FAILED("IMPORT_SIMCARD_FAILED", "Failed to import SIM card"),
    
    /**
     * Alert related errors
     */
    ALERT_NAME_ALREADY_EXISTS("ALERT_NAME_ALREADY_EXISTS", "Alert name already exists"),
    ALERT_CONFIG_NOT_EXISTS("ALERT_CONFIG_NOT_EXISTS", "Alert configuration does not exist"),
    ALERT_LOG_NOT_EXISTS("ALERT_LOG_NOT_EXISTS", "Alert log does not exist"),
    
    /**
     * Organization related errors
     */
    ORG_CODE_ALREADY_EXISTS("ORG_CODE_ALREADY_EXISTS", "Organization code already exists"),
    PARENT_ORG_NOT_EXISTS("PARENT_ORG_NOT_EXISTS", "Parent organization does not exist"),
    ORG_NOT_EXISTS("ORG_NOT_EXISTS", "Organization does not exist"),
    CANNOT_SELECT_SELF_AS_PARENT("CANNOT_SELECT_SELF_AS_PARENT", "Cannot select self as parent organization"),
    CANNOT_SELECT_CHILD_AS_PARENT("CANNOT_SELECT_CHILD_AS_PARENT", "Cannot select child organization as parent"),
    ORG_HAS_CHILDREN_CANNOT_DELETE("ORG_HAS_CHILDREN_CANNOT_DELETE", "Organization has child organizations and cannot be deleted");
    
    private final String code;
    private final String message;
    
    ErrorMessageEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    /**
     * Get enum by code
     */
    public static ErrorMessageEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ErrorMessageEnum error : values()) {
            if (error.getCode().equals(code)) {
                return error;
            }
        }
        return null;
    }
    
    /**
     * Check if code is valid
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
package com.nsrs.simcard.constants;

/**
 * API Description Constants
 * Used to uniformly manage parameter description information in API interfaces
 */
public class ApiDescriptionConstants {
    
    /**
     * Common Parameter Descriptions
     */
    public static final String PAGE_NUM = "Page Number";
    public static final String PAGE_SIZE = "Page Size";
    public static final String STATUS = "Status";
    public static final String ID = "ID";
    
    /**
     * SIM Card Related Descriptions
     */
    public static final String SIMCARD_ID = "SIM Card ID";
    public static final String SIMCARD_STATUS = "SIM Card Status: 1-Published, 2-Assigned, 3-Activated, 4-Deactivated, 5-Recycled";
    public static final String ICCID = "ICCID";
    public static final String MSISDN = "MSISDN";
    public static final String IMSI = "IMSI";
    
    /**
     * SIM Card Specification Related Descriptions
     */
    public static final String SPEC_ID = "Specification ID";
    public static final String SPEC_NAME = "Specification Name";
    public static final String SPEC_CODE = "Specification Code";
    public static final String SPEC_STATUS = "Specification Status: 0-Disabled, 1-Enabled";
    
    /**
     * SIM Card Type Related Descriptions
     */
    public static final String TYPE_ID = "Type ID";
    public static final String TYPE_NAME = "Type Name";
    public static final String TYPE_CODE = "Type Code";
    public static final String TYPE_STATUS = "Type Status: 0-Disabled, 1-Enabled";
    
    /**
     * SIM Card Batch Related Descriptions
     */
    public static final String BATCH_ID = "Batch ID";
    public static final String BATCH_STATUS = "Batch Status: 0-Normal, 1-Disabled";
    
    /**
     * Organization Related Descriptions
     */
    public static final String ORG_ID = "Organization ID";
    
    /**
     * IMSI Related Descriptions
     */
    public static final String IMSI_ID = "IMSI ID";
    public static final String IMSI_STATUS = "IMSI Status: 1-Idle, 2-Bound, 3-Used, 4-Locked";
    
    /**
     * Supplier Related Descriptions
     */
    public static final String SUPPLIER_ID = "Supplier ID";
    public static final String SUPPLIER_STATUS = "Supplier Status: 0-Disabled, 1-Enabled";
    
    /**
     * Private constructor to prevent instantiation
     */
    private ApiDescriptionConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
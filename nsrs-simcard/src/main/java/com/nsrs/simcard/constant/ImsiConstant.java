package com.nsrs.simcard.constant;

/**
 * IMSI常量类
 */
public class ImsiConstant {
    
    /**
     * IMSI类型：GSM Postpaid
     */
    public static final int IMSI_TYPE_GSM_POSTPAID = 1;
    
    /**
     * IMSI类型：GSM Prepaid
     */
    public static final int IMSI_TYPE_GSM_PREPAID = 2;
    
    /**
     * IMSI类型：CDMA
     */
    public static final int IMSI_TYPE_CDMA = 3;
    
    /**
     * 状态：空闲
     */
    public static final int STATUS_IDLE = 1;
    
    /**
     * 状态：已绑定
     */
    public static final int STATUS_BOUND = 2;
    
    /**
     * 状态：已使用
     */
    public static final int STATUS_USED = 3;
    
    /**
     * 状态：已锁定
     */
    public static final int STATUS_LOCKED = 4;
    
    /**
     * 分表数量
     */
    public static final int SHARDING_TABLE_COUNT = 10;
} 
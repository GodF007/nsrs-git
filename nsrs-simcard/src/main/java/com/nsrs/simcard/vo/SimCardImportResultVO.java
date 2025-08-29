package com.nsrs.simcard.vo;

import com.nsrs.simcard.dto.SimCardImportItem;
import lombok.Data;

import java.util.List;

/**
 * SIM卡导入结果VO
 */
@Data
public class SimCardImportResultVO {
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 批次编号
     */
    private String batchCode;
    
    /**
     * 批次名称
     */
    private String batchName;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 成功记录数
     */
    private Integer successCount;
    
    /**
     * 失败记录数
     */
    private Integer failCount;
    
    /**
     * 导入细节列表
     */
    private List<SimCardImportItem> details;
} 
package com.nsrs.simcard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * IMSI-ICCID映射表实体类
 * 用于建立IMSI与ICCID的映射关系，优化通过IMSI查询ICCID的性能
 * 
 * @author NSRS
 * @since 2025-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("imsi_iccid_mapping")
public class ImsiIccidMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * IMSI号码
     */
    private String imsi;

    /**
     * ICCID号码
     */
    private String iccid;
}
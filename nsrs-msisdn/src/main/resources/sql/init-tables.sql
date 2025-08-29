-- 创建号码资源主表
CREATE TABLE IF NOT EXISTS `number_resource` (
  `number_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '号码ID',
  `number` varchar(20) NOT NULL COMMENT '号码',
  `number_type` tinyint(4) DEFAULT NULL COMMENT '号码类型：1-PSTN，2-Mobile，3-FTTH，4-SIP，5-VSAT',
  `segment_id` bigint(20) DEFAULT NULL COMMENT '号码段ID',
  `level_id` bigint(20) DEFAULT NULL COMMENT '号码级别ID',
  `pattern_id` bigint(20) DEFAULT NULL COMMENT '号码模式ID',
  `hlr_id` bigint(20) DEFAULT NULL COMMENT 'HLR/交换机ID',
  `iccid` varchar(20) DEFAULT NULL COMMENT 'ICCID',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定',
  `charge` decimal(10,2) DEFAULT NULL COMMENT '费用',
  `attributive_org` varchar(50) DEFAULT NULL COMMENT '归属组织',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_user_id` bigint(20) DEFAULT NULL COMMENT '创建用户ID',
  `update_user_id` bigint(20) DEFAULT NULL COMMENT '更新用户ID',
  PRIMARY KEY (`number_id`),
  UNIQUE KEY `idx_number` (`number`),
  KEY `idx_segment_id` (`segment_id`),
  KEY `idx_status` (`status`),
  KEY `idx_number_type` (`number_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号码资源表';

-- 创建分表脚本
DELIMITER //
CREATE PROCEDURE create_number_resource_tables()
BEGIN
    DECLARE i INT DEFAULT 0;
    WHILE i < 10 DO
        SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS `number_resource_', i, '` LIKE `number_resource`');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 调用存储过程创建分表
CALL create_number_resource_tables();

-- 删除存储过程
DROP PROCEDURE IF EXISTS create_number_resource_tables; 
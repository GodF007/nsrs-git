-- IMSI-ICCID映射表
CREATE TABLE `imsi_iccid_mapping` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `imsi` varchar(20) NOT NULL COMMENT 'IMSI号码',
  `iccid` varchar(20) NOT NULL COMMENT 'ICCID号码',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_imsi` (`imsi`) COMMENT 'IMSI唯一索引',
  KEY `idx_iccid` (`iccid`) COMMENT 'ICCID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IMSI-ICCID映射表';

-- 创建索引
-- CREATE UNIQUE INDEX `uk_imsi_iccid_mapping_imsi` ON `imsi_iccid_mapping` (`imsi`);
-- CREATE INDEX `idx_imsi_iccid_mapping_iccid` ON `imsi_iccid_mapping` (`iccid`);
-- 全局序列表
CREATE TABLE IF NOT EXISTS global_sequence (
                                               sequence_name VARCHAR(50) NOT NULL COMMENT '序列名称',
                                               current_value BIGINT NOT NULL DEFAULT 0 COMMENT '当前值',
                                               increment_step INT NOT NULL DEFAULT 1 COMMENT '增长步长',
                                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               PRIMARY KEY (sequence_name)
) ENGINE=InnoDB COMMENT='全局序列表';

-- 初始化号码资源序列
INSERT INTO global_sequence (sequence_name, current_value) VALUES ('number_resource_id_seq', 1000000) ON DUPLICATE KEY UPDATE sequence_name=sequence_name;

-- 初始化IMSI资源序列
INSERT INTO global_sequence (sequence_name, current_value) VALUES ('imsi_resource_id_seq', 1000000) ON DUPLICATE KEY UPDATE sequence_name=sequence_name;

-- 初始化SIM卡序列
INSERT INTO global_sequence (sequence_name, current_value) VALUES ('sim_card_id_seq', 1000000) ON DUPLICATE KEY UPDATE sequence_name=sequence_name;

-- 初始化号码IMSI绑定序列
INSERT INTO global_sequence (sequence_name, current_value) VALUES ('number_imsi_binding_id_seq', 1000000) ON DUPLICATE KEY UPDATE sequence_name=sequence_name;

-- 获取下一个序列值的函数（线程安全版本）
DELIMITER //
DROP FUNCTION IF EXISTS get_next_sequence_value //
CREATE FUNCTION get_next_sequence_value(seq_name VARCHAR(50))
    RETURNS BIGINT
BEGIN
    DECLARE current_val BIGINT DEFAULT 0;
    DECLARE step_val INT DEFAULT 1;
    DECLARE result_val BIGINT DEFAULT 0;
    
    -- 使用SELECT FOR UPDATE确保线程安全
    SELECT current_value, increment_step INTO current_val, step_val 
    FROM global_sequence WHERE sequence_name = seq_name FOR UPDATE;
    
    -- 计算新的序列值
    SET result_val = current_val + step_val;
    
    -- 更新序列表
    UPDATE global_sequence SET current_value = result_val WHERE sequence_name = seq_name;
    
    RETURN result_val;
END //

-- 批量获取序列值的函数
DROP FUNCTION IF EXISTS get_batch_sequence_values //
CREATE FUNCTION get_batch_sequence_values(seq_name VARCHAR(50), batch_count INT)
    RETURNS JSON
    READS SQL DATA
    MODIFIES SQL DATA
    NOT DETERMINISTIC
BEGIN
    DECLARE current_val BIGINT DEFAULT 0;
    DECLARE step_val INT DEFAULT 1;
    DECLARE start_val BIGINT DEFAULT 0;
    DECLARE counter INT DEFAULT 0;
    DECLARE result_json JSON DEFAULT JSON_ARRAY();
    DECLARE temp_val BIGINT DEFAULT 0;
    
    IF batch_count <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Batch count must be greater than 0';
    END IF;
    
    -- 使用SELECT FOR UPDATE确保线程安全
    SELECT current_value, increment_step INTO current_val, step_val 
    FROM global_sequence WHERE sequence_name = seq_name FOR UPDATE;
    
    -- 计算起始值
    SET start_val = current_val + step_val;
    
    -- 批量更新序列值
    UPDATE global_sequence SET current_value = current_value + (step_val * batch_count) 
    WHERE sequence_name = seq_name;
    
    -- 生成序列值数组
    WHILE counter < batch_count DO
        SET temp_val = start_val + (counter * step_val);
        SET result_json = JSON_ARRAY_APPEND(result_json, '$', temp_val);
        SET counter = counter + 1;
    END WHILE;
    
    RETURN result_json;
END //

-- 获取号码资源ID序列的便捷函数
DROP FUNCTION IF EXISTS get_next_number_resource_id //
CREATE FUNCTION get_next_number_resource_id()
    RETURNS BIGINT
    READS SQL DATA
    MODIFIES SQL DATA
    NOT DETERMINISTIC
BEGIN
    RETURN get_next_sequence_value('number_resource_id_seq');
END //

DELIMITER ;


-- 号资源管理表模型-- start

-- 号码级别定义表
CREATE TABLE IF NOT EXISTS number_level (
    level_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '级别ID',
    level_name VARCHAR(50) NOT NULL COMMENT '级别名称',
    level_code VARCHAR(50) NOT NULL COMMENT '级别代码',
    charge DECIMAL(10,2) COMMENT '费用',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    need_approval TINYINT NOT NULL DEFAULT 0 COMMENT '是否需要审批：0-不需要审批，1-需要审批',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (level_id),
    UNIQUE KEY uk_level_code (level_code)
) ENGINE=InnoDB COMMENT='号码级别定义表';

-- 号码模式定义表
CREATE TABLE IF NOT EXISTS number_pattern (
    pattern_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模式ID',
    pattern_name VARCHAR(50) NOT NULL COMMENT '模式名称',
    level_id BIGINT COMMENT '归属号码级别ID',
    pattern_format VARCHAR(200) COMMENT '模式格式',
    expression VARCHAR(100) NOT NULL COMMENT '正则表达式',
    remark VARCHAR(200) COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (pattern_id)
) ENGINE=InnoDB COMMENT='号码模式定义表';

-- 区域表
CREATE TABLE IF NOT EXISTS region (
    region_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '区域ID',
    region_code VARCHAR(50) NOT NULL COMMENT '区域代码',
    region_name VARCHAR(100) NOT NULL COMMENT '区域名称',
    region_type TINYINT COMMENT '区域类型：1-国家，2-省份，3-城市，4-区县',
    parent_id BIGINT COMMENT '父区域ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (region_id),
    UNIQUE KEY uk_region_code (region_code),
    INDEX idx_parent_id (parent_id),
    CONSTRAINT fk_region_parent FOREIGN KEY (parent_id) REFERENCES region (region_id)
) ENGINE=InnoDB COMMENT='区域表';

-- HLR/交换机表
DROP TABLE IF EXISTS hlr_switch;
CREATE TABLE IF NOT EXISTS hlr_switch (
    hlr_id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'HLR/交换机ID',
    hlr_name VARCHAR(50) NOT NULL COMMENT 'HLR/交换机名称',
    hlr_code VARCHAR(50) NOT NULL COMMENT 'HLR/交换机代码',
    hlr_type TINYINT COMMENT 'HLR/交换机类型：1-HLR，2-交换机，3-服务平台',
    region_id BIGINT COMMENT '区域ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port VARCHAR(10) COMMENT '端口',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (hlr_id),
    UNIQUE KEY uk_hlr_code (hlr_code),
    INDEX idx_region_id (region_id),
    CONSTRAINT fk_hlr_region FOREIGN KEY (region_id) REFERENCES region (region_id)
) ENGINE=InnoDB COMMENT='HLR/交换机表';

-- 号码段表
DROP TABLE IF EXISTS number_segment;
CREATE TABLE IF NOT EXISTS number_segment (
    segment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '号码段ID',
    segment_code VARCHAR(50) NOT NULL COMMENT '号码段代码',
    segment_type TINYINT NOT NULL COMMENT '号码段类型：1-PSTN Number 公共交换电话网号码（固话/传真），2-Mobile Number，3-FTTH Number 光纤到户终端标识号（Fiber To The Home），4-SIP 基于SIP协议的网络电话标识（Session Initiation Protocol）, 5-VSAT卫星通信终端编号（Very Small Aperture Terminal）',
    hlr_switch_id BIGINT COMMENT 'HLR/交换机ID',
    region_id BIGINT COMMENT '区域ID',
    start_number VARCHAR(50) NOT NULL COMMENT '开始号码',
    end_number VARCHAR(50) NOT NULL COMMENT '结束号码',
    total_qty INT NOT NULL COMMENT '总数量',
    idle_qty INT NOT NULL COMMENT '空闲数量',
    activated_qty INT NOT NULL DEFAULT 0 COMMENT '已激活数量',
    frozen_qty INT NOT NULL DEFAULT 0 COMMENT '已冻结数量',
    blocked_qty INT NOT NULL DEFAULT 0 COMMENT '已锁定数量',
    reserved_qty INT NOT NULL DEFAULT 0 COMMENT '已预留数量',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (segment_id),
    UNIQUE KEY uk_segment_code (segment_code),
    INDEX idx_hlr_switch_id (hlr_switch_id),
    INDEX idx_region_id (region_id),
    CONSTRAINT fk_segment_hlr FOREIGN KEY (hlr_switch_id) REFERENCES hlr_switch (hlr_id),
    CONSTRAINT fk_segment_region FOREIGN KEY (region_id) REFERENCES region (region_id)
) ENGINE=InnoDB COMMENT='号码段表';

-- 号码资源表（按前三位分表）
-- 示例表：number_resource_138, number_resource_139 等
CREATE TABLE IF NOT EXISTS number_resource (
    number_id BIGINT NOT NULL COMMENT '号码ID（使用全局序列）',
    number VARCHAR(50) NOT NULL COMMENT '号码',
    number_type TINYINT NOT NULL COMMENT '号码类型：1-PSTN Number 公共交换电话网号码（固话/传真），2-Mobile Number，3-FTTH Number 光纤到户终端标识号（Fiber To The Home），4-SIP 基于SIP协议的网络电话标识（Session Initiation Protocol）, 5-VSAT卫星通信终端编号（Very Small Aperture Terminal）',
    segment_id BIGINT COMMENT '号码段ID',
    level_id BIGINT COMMENT '号码级别ID',
    pattern_id BIGINT COMMENT '号码模式ID',
    hlr_id BIGINT COMMENT 'HLR/交换机ID',
    iccid VARCHAR(50) COMMENT 'ICCID', -- 绑定之后更新保存该字段信息，这样根据号码能反查卡信息
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定',
    charge DECIMAL(10,2) COMMENT '费用', -- 预留，可能暂时用不到
    attributive_org VARCHAR(50) COMMENT '归属组织',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (number_id),
    UNIQUE KEY uk_number (number),
    INDEX idx_segment_id (segment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='号码资源表';

-- 创建号码审批表
CREATE TABLE number_approval (
    approval_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '审批ID',
    approval_no VARCHAR(50) NOT NULL COMMENT '审批编号',
    number_id BIGINT NOT NULL COMMENT '号码ID',
    number VARCHAR(20) NOT NULL COMMENT '号码',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) NOT NULL COMMENT '申请人姓名',
    apply_time DATETIME NOT NULL COMMENT '申请时间',
    apply_reason VARCHAR(200) NOT NULL COMMENT '申请原因',
    customer_info VARCHAR(200) COMMENT '客户信息',
    level_id BIGINT NOT NULL COMMENT '号码级别ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '审批状态：0-待审批，1-已通过，2-已拒绝，3-已取消',
    approver_id BIGINT COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approval_time DATETIME COMMENT '审批时间',
    approval_opinion VARCHAR(200) COMMENT '审批意见',
    PRIMARY KEY (approval_id),
    UNIQUE KEY uk_approval_no (approval_no),
    KEY idx_number_id (number_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_level_id (level_id),
    KEY idx_approver_id (approver_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='号码审批表';

-- 号码操作日志表（合并号码状态历史表和号码操作记录表，按月分区）
CREATE TABLE IF NOT EXISTS number_operation_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    number_id BIGINT NOT NULL COMMENT '号码ID',
    number VARCHAR(50) NOT NULL COMMENT '号码',
    number_type TINYINT COMMENT '号码类型',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-创建，2-预留，3-分配，4-激活，5-冻结，6-解冻，7-释放，8-回收, 9-修改, 10-删除',
    old_status TINYINT COMMENT '原状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定',
    new_status TINYINT COMMENT '新状态：1-空闲，2-预留，3-已分配，4-已激活，5-已使用，6-已冻结，7-已锁定',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operator_user_id BIGINT COMMENT '操作用户ID',
    charge DECIMAL(10,2) COMMENT '费用',
    org_name VARCHAR(50) COMMENT '组织名称',
    result_status TINYINT DEFAULT 1 COMMENT '操作结果状态：0-失败，1-成功',
    remark VARCHAR(256) COMMENT '备注',
    PRIMARY KEY (log_id),
    INDEX idx_number_id (number_id),
    INDEX idx_number (number),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time),
    INDEX idx_new_status (new_status)
) ENGINE=InnoDB COMMENT='号码操作日志表（按月分区）'
PARTITION BY RANGE (YEAR(operation_time) * 100 + MONTH(operation_time)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p202403 VALUES LESS THAN (202404),
    PARTITION p202404 VALUES LESS THAN (202405),
    PARTITION p202405 VALUES LESS THAN (202406),
    PARTITION p202406 VALUES LESS THAN (202407),
    PARTITION p202407 VALUES LESS THAN (202408),
    PARTITION p202408 VALUES LESS THAN (202409),
    PARTITION p202409 VALUES LESS THAN (202410),
    PARTITION p202410 VALUES LESS THAN (202411),
    PARTITION p202411 VALUES LESS THAN (202412),
    PARTITION p202412 VALUES LESS THAN (202501),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 创建自动添加分区的存储过程
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS add_log_partition_for_month(IN target_year INT, IN target_month INT)
BEGIN
    DECLARE partition_name VARCHAR(20);
    DECLARE next_year INT;
    DECLARE next_month INT;
    DECLARE partition_value INT;
    
    SET partition_name = CONCAT('p', target_year, LPAD(target_month, 2, '0'));
    
    -- Calculate next month
    IF target_month = 12 THEN
        SET next_year = target_year + 1;
        SET next_month = 1;
    ELSE
        SET next_year = target_year;
        SET next_month = target_month + 1;
    END IF;
    
    SET partition_value = next_year * 100 + next_month;
    
    SET @sql = CONCAT('ALTER TABLE number_operation_log REORGANIZE PARTITION p_future INTO (',
                     'PARTITION ', partition_name, ' VALUES LESS THAN (', partition_value, '),',
                     'PARTITION p_future VALUES LESS THAN MAXVALUE)');
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- 号资源管理表模型-- end


-- SIM卡资源管理表模型 -- start
-- SIM卡类型表
CREATE TABLE IF NOT EXISTS sim_card_type (
    type_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '类型ID',
    type_name VARCHAR(50) NOT NULL COMMENT '类型名称',
    type_code VARCHAR(50) NOT NULL COMMENT '类型代码，GSM、CDMA',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (type_id),
    UNIQUE KEY uk_type_code (type_code)
) ENGINE=InnoDB COMMENT='SIM卡类型表';

-- SIM卡规格表
CREATE TABLE IF NOT EXISTS sim_card_specification (
    spec_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规格ID',
    spec_name VARCHAR(50) NOT NULL COMMENT '规格名称',
    spec_code VARCHAR(50) NOT NULL COMMENT '规格代码',
    type_id BIGINT COMMENT '类型ID',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (spec_id),
    UNIQUE KEY uk_spec_code (spec_code)
) ENGINE=InnoDB COMMENT='SIM卡规格表';

-- 供应商表
CREATE TABLE IF NOT EXISTS supplier (
    supplier_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
    supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    supplier_code VARCHAR(50) NOT NULL COMMENT '供应商代码',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    address VARCHAR(200) COMMENT '地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (supplier_id),
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB COMMENT='供应商表';

-- 组织表
CREATE TABLE IF NOT EXISTS organization (
    org_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '组织ID',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    org_code VARCHAR(50) NOT NULL COMMENT '组织代码',
    org_type TINYINT COMMENT '组织类型：1-公司，2-部门，3-分支机构，4-合作伙伴',
    parent_id BIGINT COMMENT '父组织ID',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    address VARCHAR(200) COMMENT '地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (org_id),
    UNIQUE KEY uk_org_code (org_code)
) ENGINE=InnoDB COMMENT='组织表';

-- SIM卡批次表
CREATE TABLE IF NOT EXISTS sim_card_batch (
    batch_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '批次ID',
    batch_code VARCHAR(50) NOT NULL COMMENT '批次编号',
    batch_name VARCHAR(100) COMMENT '批次名称',
    supplier_id BIGINT COMMENT '供应商ID',
    production_date DATE COMMENT '生产日期',
    import_date DATE COMMENT '入库日期',
    import_user_id BIGINT COMMENT '入库操作用户ID',
    total_count INT DEFAULT 0 COMMENT '总数量',
    activated_count INT DEFAULT 0 COMMENT '已激活数量',
    deactivated_count INT DEFAULT 0 COMMENT '已停用数量',
    recycled_count INT DEFAULT 0 COMMENT '已回收数量',
    available_count INT DEFAULT 0 COMMENT '可用数量',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (batch_id),
    UNIQUE KEY uk_batch_code (batch_code),
    INDEX idx_supplier_id (supplier_id),
    CONSTRAINT fk_batch_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id)
) ENGINE=InnoDB COMMENT='SIM卡批次表';

-- 库存预警配置表
CREATE TABLE IF NOT EXISTS sim_card_inventory_alert (
    alert_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    alert_name VARCHAR(100) NOT NULL COMMENT '预警名称',
    card_type_id BIGINT COMMENT '卡类型ID',
    spec_id BIGINT COMMENT '规格ID',
    supplier_id BIGINT COMMENT '供应商ID',
    org_id BIGINT COMMENT '组织ID',
    min_threshold INT NOT NULL COMMENT '最小阈值',
    max_threshold INT COMMENT '最大阈值',
    alert_type TINYINT NOT NULL DEFAULT 1 COMMENT '预警类型：1-低库存预警，2-超量预警',
    is_active TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    notify_emails VARCHAR(500) COMMENT '通知邮箱，多个用逗号分隔',
    notify_phones VARCHAR(200) COMMENT '通知手机号，多个用逗号分隔',
    -- 这个地方可能使用PBS通知组件代替，关联一个notification rule即可
    last_alert_time DATETIME COMMENT '上次预警时间',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (alert_id),
    INDEX idx_card_type_id (card_type_id),
    INDEX idx_spec_id (spec_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_org_id (org_id),
    CONSTRAINT fk_alert_card_type FOREIGN KEY (card_type_id) REFERENCES sim_card_type (type_id),
    CONSTRAINT fk_alert_spec FOREIGN KEY (spec_id) REFERENCES sim_card_specification (spec_id),
    CONSTRAINT fk_alert_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id),
    CONSTRAINT fk_alert_org FOREIGN KEY (org_id) REFERENCES organization (org_id)
) ENGINE=InnoDB COMMENT='库存预警配置表';

-- 库存预警日志表
CREATE TABLE IF NOT EXISTS sim_card_inventory_alert_log (
    log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    alert_id BIGINT NOT NULL COMMENT '预警ID',
    alert_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间',
    card_type_id BIGINT COMMENT '卡类型ID',
    spec_id BIGINT COMMENT '规格ID',
    supplier_id BIGINT COMMENT '供应商ID',
    org_id BIGINT COMMENT '组织ID',
    current_count INT COMMENT '当前数量',
    threshold INT COMMENT '阈值',
    alert_type TINYINT COMMENT '预警类型：1-低库存预警，2-超量预警',
    notify_status TINYINT COMMENT '通知状态：0-未通知，1-已通知',
    notify_time DATETIME COMMENT '通知时间',
    remark VARCHAR(256) COMMENT '备注',
    PRIMARY KEY (log_id),
    INDEX idx_alert_id (alert_id),
    INDEX idx_alert_time (alert_time),
    CONSTRAINT fk_alert_log_alert FOREIGN KEY (alert_id) REFERENCES sim_card_inventory_alert (alert_id)
) ENGINE=InnoDB COMMENT='库存预警日志表';

-- SIM卡表(修改，添加批次ID)
DROP TABLE IF EXISTS sim_card;
CREATE TABLE IF NOT EXISTS sim_card (
    card_id BIGINT NOT NULL COMMENT '卡ID（全局序列生成）',
    iccid VARCHAR(50) NOT NULL COMMENT 'ICCID',
    imsi VARCHAR(50) COMMENT 'IMSI', 
    batch_id BIGINT COMMENT '批次ID',
    card_type_id BIGINT COMMENT '卡类型ID',
    spec_id BIGINT COMMENT '规格ID',
    data_type TINYINT COMMENT '数据类型：1-流量卡，2-语音卡，3-双模卡，4-物联网卡',
    supplier_id BIGINT COMMENT '供应商ID',
    org_id BIGINT COMMENT '组织ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收',
    remark VARCHAR(256) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (card_id),
    UNIQUE KEY uk_iccid (iccid),
    INDEX idx_imsi (imsi),
    INDEX idx_batch_id (batch_id),
    INDEX idx_status (status),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_org_id (org_id),
    CONSTRAINT fk_sim_batch FOREIGN KEY (batch_id) REFERENCES sim_card_batch (batch_id),
    CONSTRAINT fk_sim_org FOREIGN KEY (org_id) REFERENCES organization (org_id),
    CONSTRAINT fk_sim_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id),
    CONSTRAINT fk_sim_card_type FOREIGN KEY (card_type_id) REFERENCES sim_card_type (type_id),
    CONSTRAINT fk_sim_spec FOREIGN KEY (spec_id) REFERENCES sim_card_specification (spec_id)
) ENGINE=InnoDB COMMENT='SIM卡表';

-- SIM卡分表（sim_card_0 到 sim_card_9）
CREATE TABLE IF NOT EXISTS sim_card_0 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_1 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_2 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_3 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_4 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_5 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_6 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_7 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_8 LIKE sim_card;
CREATE TABLE IF NOT EXISTS sim_card_9 LIKE sim_card;

-- SIM卡操作记录表
DROP TABLE IF EXISTS sim_card_operation;
CREATE TABLE IF NOT EXISTS sim_card_operation (
    operation_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '操作ID',
    card_id BIGINT NOT NULL COMMENT '卡ID',
    iccid VARCHAR(50) NOT NULL COMMENT 'ICCID',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-分配，2-回收，3-激活，4-停用，5-更新',
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    operator_user_id BIGINT COMMENT '操作用户ID',
    old_status TINYINT COMMENT '原状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收',
    new_status TINYINT COMMENT '新状态：1-已发布，2-已分配，3-已激活，4-已停用，5-已回收',
    stock_out_org_id BIGINT COMMENT '调出组织ID',
    stock_in_org_id BIGINT COMMENT '调入组织ID',
    remark VARCHAR(256) COMMENT '备注',
    result_status TINYINT DEFAULT 1 COMMENT '操作结果状态：0-失败，1-成功',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (operation_id),
    INDEX idx_card_id (card_id),
    INDEX idx_iccid (iccid),
    INDEX idx_operation_time (operation_time),
    INDEX idx_operation_type (operation_type),
    INDEX idx_stock_out_org_id (stock_out_org_id),
    INDEX idx_stock_in_org_id (stock_in_org_id),
    -- CONSTRAINT fk_operation_card FOREIGN KEY (card_id) REFERENCES sim_card (card_id), -- 分表环境下外键约束无法正确工作，已注释
    CONSTRAINT fk_operation_stock_out_org FOREIGN KEY (stock_out_org_id) REFERENCES organization (org_id),
    CONSTRAINT fk_operation_stock_in_org FOREIGN KEY (stock_in_org_id) REFERENCES organization (org_id)
) ENGINE=InnoDB COMMENT='SIM卡操作记录表';

-- SIM卡资源管理表模型 -- end


-- IMSI资源表模型 -- start

-- IMSI组表
CREATE TABLE IF NOT EXISTS imsi_group (
    group_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '组ID',
    group_name VARCHAR(50) NOT NULL COMMENT '组名称',
    imsi_prefix VARCHAR(20) COMMENT 'IMSI前缀',
    imsi_start VARCHAR(20) COMMENT '起始IMSI',
    imsi_end VARCHAR(20) COMMENT '结束IMSI',
    imsi_type TINYINT COMMENT 'IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......',
    total_count INT DEFAULT 0 COMMENT '总数量',
    used_count INT DEFAULT 0 COMMENT '已使用数量',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (group_id)
) ENGINE=InnoDB COMMENT='IMSI组表';

-- IMSI资源表（按IMSI尾缀分表）
CREATE TABLE IF NOT EXISTS imsi_resource (
    imsi_id BIGINT NOT NULL COMMENT 'IMSI ID（使用全局序列）',
    imsi VARCHAR(20) NOT NULL COMMENT 'IMSI号码',
    imsi_type TINYINT COMMENT 'IMSI类型：1-GSM Postpaid，2-GSM Prepaid，3-CDMA......',
    group_id BIGINT COMMENT '所属组ID',
    supplier_id BIGINT COMMENT '供应商ID',
    password VARCHAR(50) COMMENT '密码',
    -- 什么作用？
    bill_id VARCHAR(50) COMMENT '账单ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-空闲，2-已绑定，3-已使用，4-已锁定',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (imsi_id),
    UNIQUE KEY uk_imsi (imsi),
    INDEX idx_group_id (group_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='IMSI资源表';

-- IMSI资源分表（imsi_resource_0 到 imsi_resource_9）
CREATE TABLE IF NOT EXISTS imsi_resource_0 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_1 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_2 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_3 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_4 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_5 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_6 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_7 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_8 LIKE imsi_resource;
CREATE TABLE IF NOT EXISTS imsi_resource_9 LIKE imsi_resource;

-- IMSI资源表模型 -- end


-- 绑定关系表模型 -- start

-- 号码与IMSI绑定表
CREATE TABLE IF NOT EXISTS number_imsi_binding (
    binding_id BIGINT NOT NULL COMMENT '绑定ID（全局序列生成）',
    number_id BIGINT NOT NULL COMMENT '号码ID',
    number VARCHAR(50) NOT NULL COMMENT '号码',
    imsi_id BIGINT NOT NULL COMMENT 'IMSI ID',
    imsi VARCHAR(20) NOT NULL COMMENT 'IMSI号码',
    iccid VARCHAR(50) COMMENT 'ICCID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    binding_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    binding_type TINYINT COMMENT '绑定类型：1-普通绑定，2-批量绑定，3-测试',
    binding_status TINYINT NOT NULL DEFAULT 1 COMMENT '绑定状态：1-已绑定，2-已解绑',
    unbind_time DATETIME COMMENT '解绑时间',
    operator_user_id BIGINT COMMENT '操作用户ID',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (binding_id),
    UNIQUE KEY uk_number_imsi (number, imsi),
    INDEX idx_number_id (number_id),
    INDEX idx_imsi_id (imsi_id),
    INDEX idx_binding_status (binding_status)
) ENGINE=InnoDB COMMENT='号码与IMSI绑定表';

-- 批量绑定任务表
CREATE TABLE IF NOT EXISTS batch_binding_task (
    task_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(100) COMMENT '任务名称',
    task_type TINYINT NOT NULL COMMENT '任务类型：1-绑定，2-解绑',
    file_path VARCHAR(200) COMMENT '文件路径',
    total_count INT DEFAULT 0 COMMENT '总记录数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-成功，3-失败，4-部分成功',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID',
    PRIMARY KEY (task_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='批量绑定任务表';

-- 批量绑定任务详情表
CREATE TABLE IF NOT EXISTS batch_binding_detail (
    detail_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    number VARCHAR(50) COMMENT '号码',
    imsi VARCHAR(20) COMMENT 'IMSI号码',
    iccid VARCHAR(50) COMMENT 'ICCID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-成功，2-失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    process_time DATETIME COMMENT '处理时间',
    PRIMARY KEY (detail_id),
    INDEX idx_task_id (task_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='批量绑定任务详情表';

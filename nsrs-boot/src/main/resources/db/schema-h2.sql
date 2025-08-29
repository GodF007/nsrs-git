-- HLR/交换机表
CREATE TABLE hlr_switch (
    hlr_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'HLR/交换机ID',
    hlr_name VARCHAR(100) NOT NULL COMMENT 'HLR/交换机名称',
    hlr_code VARCHAR(50) NOT NULL COMMENT 'HLR/交换机代码',
    hlr_type INT NOT NULL COMMENT 'HLR/交换机类型：1-HLR，2-交换机，3-服务平台',
    region_id BIGINT COMMENT '区域ID',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port VARCHAR(10) COMMENT '端口',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID'
);

-- 号码段表
CREATE TABLE number_segment (
    segment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '号码段ID',
    segment_code VARCHAR(50) NOT NULL COMMENT '号码段代码',
    segment_type INT NOT NULL COMMENT '号码段类型：1-PSTN，2-Mobile，3-FTTH，4-SIP，5-VSAT',
    hlr_switch_id BIGINT COMMENT 'HLR/交换机ID',
    region_id BIGINT COMMENT '区域ID',
    level_id BIGINT COMMENT '号码等级ID',
    pattern_id BIGINT COMMENT '号码模式ID',
    start_number VARCHAR(20) NOT NULL COMMENT '开始号码',
    end_number VARCHAR(20) NOT NULL COMMENT '结束号码',
    total_qty BIGINT DEFAULT 0 COMMENT '总数量',
    idle_qty BIGINT DEFAULT 0 COMMENT '空闲数量',
    released_qty BIGINT DEFAULT 0 COMMENT '已释放数量',
    activated_qty BIGINT DEFAULT 0 COMMENT '已激活数量',
    frozen_qty BIGINT DEFAULT 0 COMMENT '已冻结数量',
    blocked_qty BIGINT DEFAULT 0 COMMENT '已锁定数量',
    reserved_qty BIGINT DEFAULT 0 COMMENT '已预留数量',
    status INT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user_id BIGINT COMMENT '创建用户ID',
    update_user_id BIGINT COMMENT '更新用户ID'
);

-- 号码操作日志表
CREATE TABLE number_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    number_id BIGINT COMMENT '号码ID',
    number VARCHAR(20) NOT NULL COMMENT '号码',
    number_type INT COMMENT '号码类型',
    operation_type INT NOT NULL COMMENT '操作类型：1-创建，2-预留，3-分配，4-激活，5-冻结，6-解冻，7-释放，8-回收，9-修改，10-删除',
    old_status INT COMMENT '原状态',
    new_status INT COMMENT '新状态',
    operation_time DATETIME NOT NULL COMMENT '操作时间',
    operator_user_id BIGINT COMMENT '操作用户ID',
    charge DECIMAL(10,2) COMMENT '费用',
    org_name VARCHAR(100) COMMENT '组织名称',
    result_status INT DEFAULT 1 COMMENT '操作结果状态：0-失败，1-成功',
    remark VARCHAR(500) COMMENT '备注',
    approval_id BIGINT COMMENT '审批ID'
);
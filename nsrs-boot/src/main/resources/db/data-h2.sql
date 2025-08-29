-- 插入HLR/交换机测试数据
INSERT INTO hlr_switch (hlr_name, hlr_code, hlr_type, region_id, ip_address, port, status, create_time, update_time)
VALUES 
('HLR-BJ-01', 'HLR001', 1, 1, '192.168.1.100', '8080', 1, NOW(), NOW()),
('HLR-SH-01', 'HLR002', 1, 2, '192.168.1.101', '8080', 1, NOW(), NOW()),
('SW-GZ-01', 'SW001', 2, 3, '192.168.1.102', '8080', 1, NOW(), NOW());

-- 插入号码段测试数据
INSERT INTO number_segment (segment_code, segment_type, hlr_switch_id, region_id, start_number, end_number, 
                          total_qty, idle_qty, status, create_time, update_time)
VALUES 
('139', 2, 1, 1, '13900000000', '13900009999', 10000, 10000, 1, NOW(), NOW()),
('177', 2, 2, 2, '17700000000', '17700009999', 10000, 10000, 1, NOW(), NOW());

-- 插入号码操作日志测试数据
INSERT INTO number_operation_log (number, number_type, operation_type, old_status, new_status, 
                                operation_time, result_status, remark)
VALUES 
('13900000001', 2, 1, NULL, 1, NOW(), 1, '创建号码'),
('13900000002', 2, 2, 1, 2, NOW(), 1, '预留号码'),
('17700000001', 2, 1, NULL, 1, NOW(), 1, '创建号码'); 
-- ============================================================
-- 修复测试账号：统一密码为 admin123，并补充测试数据
-- ============================================================
USE charging_db;

-- 1. 统一所有账号密码为 admin123（使用 init.sql 中已验证的 BCrypt hash）
UPDATE `user` SET
    `password` = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK',
    `nickname` = CASE
        WHEN username = 'admin'       THEN '系统管理员'
        WHEN username = 'testop001'   THEN '测试运营商A'
        WHEN username = 'testop002'   THEN '测试运营商B'
        WHEN username = 'testuser001' THEN '测试用户甲'
        WHEN username = 'testuser002' THEN '测试用户乙'
        WHEN username = 'liyz6'       THEN '测试用户丙'
        ELSE nickname
    END
WHERE username IN ('admin','testop001','testop002','testuser001','testuser002','liyz6');

-- 2. 修正 testop001 角色为 OPERATOR（之前被误设为 ADMIN）
UPDATE `user` SET `role` = 'OPERATOR' WHERE username = 'testop001';

-- 3. 新增专用测试账号（使用不冲突的手机号）
INSERT IGNORE INTO `user` (`username`, `phone`, `password`, `role`, `nickname`, `status`) VALUES
('operator01', '13700000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'OPERATOR', '绿城充电运营商', 1),
('user01',     '13700000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'USER',     '张三（标准用户）',  1),
('user02',     '13700000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'USER',     '李四（标准用户）',  1);

-- 4. 为新用户创建钱包
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 0.00, 0.00, 0.00 FROM `user` WHERE username = 'operator01';

INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 500.00, 500.00, 0.00 FROM `user` WHERE username = 'user01';

INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 200.00, 200.00, 0.00 FROM `user` WHERE username = 'user02';

-- 补充充值记录
INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT id, NULL, 500.00, 'RECHARGE', '测试充值' FROM `user` WHERE username = 'user01'
AND NOT EXISTS (SELECT 1 FROM payment_record p WHERE p.user_id = (SELECT id FROM `user` WHERE username='user01') AND p.type='RECHARGE');

INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT id, NULL, 200.00, 'RECHARGE', '测试充值' FROM `user` WHERE username = 'user02'
AND NOT EXISTS (SELECT 1 FROM payment_record p WHERE p.user_id = (SELECT id FROM `user` WHERE username='user02') AND p.type='RECHARGE');

-- 5. 为 operator01 创建充电站
INSERT IGNORE INTO `charging_station`
    (`operator_id`, `name`, `address`, `city`, `longitude`, `latitude`, `business_hours`, `parking_fee`, `status`)
SELECT
    id,
    '绿城·西湖充电中心',
    '浙江省杭州市西湖区文三路100号',
    '杭州',
    120.1536000,
    30.2936000,
    '00:00-24:00',
    '充电期间免停车费',
    'ONLINE'
FROM `user` WHERE username = 'operator01'
AND NOT EXISTS (SELECT 1 FROM charging_station WHERE name='绿城·西湖充电中心');

INSERT IGNORE INTO `charging_station`
    (`operator_id`, `name`, `address`, `city`, `longitude`, `latitude`, `business_hours`, `parking_fee`, `status`)
SELECT
    id,
    '绿城·滨江充电站',
    '浙江省杭州市滨江区江南大道199号',
    '杭州',
    120.2120000,
    30.2050000,
    '06:00-23:00',
    '停车费2元/小时',
    'ONLINE'
FROM `user` WHERE username = 'operator01'
AND NOT EXISTS (SELECT 1 FROM charging_station WHERE name='绿城·滨江充电站');

-- 6. 充电桩
INSERT IGNORE INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-WH-001', s.id, u.id, 'FAST', 120.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心' AND u.username = 'operator01';

INSERT IGNORE INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-WH-002', s.id, u.id, 'SLOW', 7.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心' AND u.username = 'operator01';

INSERT IGNORE INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-BJ-001', s.id, u.id, 'FAST', 180.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站' AND u.username = 'operator01';

INSERT IGNORE INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-BJ-002', s.id, u.id, 'SLOW', 7.0, 'FAULT'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站' AND u.username = 'operator01';

-- 7. 充电枪
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'DC', 120.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-001';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'DC', 120.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-001';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'AC', 7.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-002';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'AC', 7.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-002';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'DC', 180.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-001';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'DC', 180.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-001';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'AC', 7.0, 'FAULT' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-002';
INSERT IGNORE INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'AC', 7.0, 'FAULT' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-002';

-- 8. 计费规则（西湖站）
INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'PEAK',   8,  22, 0.8500, 0.4000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心' AND u.username = 'operator01';

INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'FLAT',   0,   8, 0.5800, 0.3000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心' AND u.username = 'operator01';

INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'VALLEY', 22,  24, 0.3200, 0.2000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心' AND u.username = 'operator01';

-- 计费规则（滨江站）
INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'PEAK',   8,  22, 0.9000, 0.4500, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站' AND u.username = 'operator01';

INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'FLAT',   0,   8, 0.6000, 0.3200, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站' AND u.username = 'operator01';

INSERT IGNORE INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'VALLEY', 22,  24, 0.3500, 0.2200, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站' AND u.username = 'operator01';

-- 9. 为 user01 添加车辆
INSERT IGNORE INTO `vehicle` (`user_id`, `plate_no`, `brand`, `model`, `battery_cap`)
SELECT id, '浙A88888', '比亚迪', '海豹', 82.5 FROM `user` WHERE username = 'user01'
AND NOT EXISTS (SELECT 1 FROM vehicle WHERE plate_no = '浙A88888');

INSERT IGNORE INTO `vehicle` (`user_id`, `plate_no`, `brand`, `model`, `battery_cap`)
SELECT id, '浙A99999', '特斯拉', 'Model 3', 75.0 FROM `user` WHERE username = 'user02'
AND NOT EXISTS (SELECT 1 FROM vehicle WHERE plate_no = '浙A99999');

-- 10. user01 历史订单（方便测试订单列表和评价功能）
INSERT IGNORE INTO `charging_order`
    (`order_no`, `user_id`, `vehicle_id`, `gun_id`, `pile_id`, `station_id`, `operator_id`,
     `start_time`, `end_time`, `charge_kwh`, `charge_fee`, `service_fee`, `total_fee`,
     `status`, `pay_status`)
SELECT
    'ORDTEST0000000001',
    u.id,
    v.id,
    g.id,
    p.id,
    s.id,
    op.id,
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR,
    32.500, 27.63, 13.00, 40.63,
    'FINISHED', 'PAID'
FROM `user` u
JOIN `vehicle` v ON v.user_id = u.id AND v.plate_no = '浙A88888'
JOIN `charging_station` s ON s.name = '绿城·西湖充电中心'
JOIN `user` op ON op.id = s.operator_id AND op.username = 'operator01'
JOIN `charging_pile` p ON p.station_id = s.id AND p.pile_no = 'PILE-WH-001'
JOIN `charging_gun` g ON g.pile_id = p.id AND g.gun_no = 'A'
WHERE u.username = 'user01'
AND NOT EXISTS (SELECT 1 FROM charging_order WHERE order_no = 'ORDTEST0000000001');

INSERT IGNORE INTO `charging_order`
    (`order_no`, `user_id`, `vehicle_id`, `gun_id`, `pile_id`, `station_id`, `operator_id`,
     `start_time`, `end_time`, `charge_kwh`, `charge_fee`, `service_fee`, `total_fee`,
     `status`, `pay_status`)
SELECT
    'ORDTEST0000000002',
    u.id,
    v.id,
    g.id,
    p.id,
    s.id,
    op.id,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR,
    50.000, 42.50, 20.00, 62.50,
    'FINISHED', 'PAID'
FROM `user` u
JOIN `vehicle` v ON v.user_id = u.id AND v.plate_no = '浙A88888'
JOIN `charging_station` s ON s.name = '绿城·西湖充电中心'
JOIN `user` op ON op.id = s.operator_id AND op.username = 'operator01'
JOIN `charging_pile` p ON p.station_id = s.id AND p.pile_no = 'PILE-WH-001'
JOIN `charging_gun` g ON g.pile_id = p.id AND g.gun_no = 'B'
WHERE u.username = 'user01'
AND NOT EXISTS (SELECT 1 FROM charging_order WHERE order_no = 'ORDTEST0000000002');

-- 11. 评价（第一个订单）
INSERT IGNORE INTO `evaluation` (`user_id`, `order_id`, `station_id`, `rating`, `content`)
SELECT u.id, o.id, o.station_id, 5, '充电速度快，服务很好！'
FROM `user` u JOIN `charging_order` o ON o.user_id = u.id
WHERE u.username = 'user01' AND o.order_no = 'ORDTEST0000000001'
AND NOT EXISTS (SELECT 1 FROM evaluation WHERE order_id = o.id);

-- 12. 故障上报记录
INSERT IGNORE INTO `fault_record` (`user_id`, `pile_id`, `pile_no`, `description`, `status`)
SELECT u.id, p.id, p.pile_no, '充电桩显示屏无法正常显示，按键无响应', 'PENDING'
FROM `user` u, `charging_pile` p
WHERE u.username = 'user01' AND p.pile_no = 'PILE-BJ-002'
AND NOT EXISTS (SELECT 1 FROM fault_record WHERE pile_no = 'PILE-BJ-002' AND user_id = u.id);

-- 13. 预约（user01 预约明天）
INSERT IGNORE INTO `reservation` (`user_id`, `pile_id`, `reserve_date`, `start_time`, `end_time`, `status`)
SELECT
    u.id, p.id,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 10:00:00'),
    CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 12:00:00'),
    'PENDING'
FROM `user` u, `charging_pile` p
WHERE u.username = 'user01' AND p.pile_no = 'PILE-WH-001'
AND NOT EXISTS (SELECT 1 FROM reservation WHERE user_id = u.id AND reserve_date = DATE_ADD(CURDATE(), INTERVAL 1 DAY));

-- 14. 公告（如不存在则添加）
INSERT IGNORE INTO `announcement` (`title`, `content`, `type`, `status`, `creator_id`)
SELECT '系统上线公告', '新能源汽车充电桩运营管理系统正式上线，欢迎使用！', 'NOTICE', 'ONLINE', id
FROM `user` WHERE username = 'admin'
AND NOT EXISTS (SELECT 1 FROM announcement WHERE title = '系统上线公告');

INSERT IGNORE INTO `announcement` (`title`, `content`, `type`, `status`, `creator_id`)
SELECT '五一假期维护通知', '系统将于2025年5月1日凌晨2:00-4:00进行例行维护，届时服务暂停。', 'MAINTENANCE', 'ONLINE', id
FROM `user` WHERE username = 'admin'
AND NOT EXISTS (SELECT 1 FROM announcement WHERE title = '五一假期维护通知');

-- ============================================================
-- 新能源汽车充电桩运营管理系统 - 测试数据脚本
-- 所有测试账号密码统一为：admin123
-- BCrypt hash（来自 init.sql，已验证有效）:
--   $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK
-- ============================================================
USE charging_db;

-- ------------------------------------------------------------
-- 1. 测试账号（三个角色）
-- ------------------------------------------------------------
-- 密码均为 admin123
INSERT IGNORE INTO `user` (`username`, `phone`, `password`, `role`, `nickname`, `status`) VALUES
('operator01', '13900000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'OPERATOR', '绿城充电运营商', 1),
('user01',     '13900000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'USER',     '测试用户张三',   1),
('user02',     '13900000003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'USER',     '测试用户李四',   1);

-- ------------------------------------------------------------
-- 2. 钱包（为所有用户创建，含初始余额）
-- ------------------------------------------------------------
-- admin 钱包
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 0.00, 0.00, 0.00 FROM `user` WHERE username = 'admin';

-- operator01 钱包
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 0.00, 0.00, 0.00 FROM `user` WHERE username = 'operator01';

-- user01 钱包（预充 500 元）
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 500.00, 500.00, 0.00 FROM `user` WHERE username = 'user01';

-- user02 钱包（预充 200 元）
INSERT IGNORE INTO `wallet` (`user_id`, `balance`, `total_recharge`, `total_consume`)
SELECT id, 200.00, 200.00, 0.00 FROM `user` WHERE username = 'user02';

-- user01/user02 充值记录
INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT id, NULL, 500.00, 'RECHARGE', '测试充值' FROM `user` WHERE username = 'user01';

INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT id, NULL, 200.00, 'RECHARGE', '测试充值' FROM `user` WHERE username = 'user02';

-- ------------------------------------------------------------
-- 3. 车辆（user01 名下）
-- ------------------------------------------------------------
INSERT INTO `vehicle` (`user_id`, `plate_no`, `brand`, `model`, `battery_cap`)
SELECT id, '浙A88888', '比亚迪', '海豹', 82.5 FROM `user` WHERE username = 'user01';

INSERT INTO `vehicle` (`user_id`, `plate_no`, `brand`, `model`, `battery_cap`)
SELECT id, '浙A99999', '特斯拉', 'Model 3', 75.0 FROM `user` WHERE username = 'user02';

-- ------------------------------------------------------------
-- 4. 充电站（operator01 名下，2 座）
-- ------------------------------------------------------------
INSERT INTO `charging_station`
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
FROM `user` WHERE username = 'operator01';

INSERT INTO `charging_station`
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
FROM `user` WHERE username = 'operator01';

-- ------------------------------------------------------------
-- 5. 充电桩（每站各 2 根桩）
-- ------------------------------------------------------------
-- 站1桩
INSERT INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-WH-001', s.id, u.id, 'FAST', 120.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心';

INSERT INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-WH-002', s.id, u.id, 'SLOW', 7.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·西湖充电中心';

-- 站2桩
INSERT INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-BJ-001', s.id, u.id, 'FAST', 180.0, 'IDLE'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站';

INSERT INTO `charging_pile` (`pile_no`, `station_id`, `operator_id`, `pile_type`, `power`, `status`)
SELECT 'PILE-BJ-002', s.id, u.id, 'SLOW', 7.0, 'FAULT'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id
WHERE s.name = '绿城·滨江充电站';

-- ------------------------------------------------------------
-- 6. 充电枪（每桩 2 把枪）
-- ------------------------------------------------------------
INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'DC', 120.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-001';
INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'DC', 120.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-001';

INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'AC', 7.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-002';
INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'AC', 7.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-WH-002';

INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'DC', 180.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-001';
INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'DC', 180.0, 'IDLE' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-001';

INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'A', 'AC', 7.0, 'FAULT' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-002';
INSERT INTO `charging_gun` (`pile_id`, `gun_no`, `gun_type`, `power`, `status`)
SELECT id, 'B', 'AC', 7.0, 'FAULT' FROM `charging_pile` WHERE pile_no = 'PILE-BJ-002';

-- ------------------------------------------------------------
-- 7. 计费规则（西湖站，峰/平/谷三档）
-- ------------------------------------------------------------
INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'PEAK',   8,  22, 0.8500, 0.4000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·西湖充电中心';

INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'FLAT',   0,   8, 0.5800, 0.3000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·西湖充电中心';

INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'VALLEY', 22,  24, 0.3200, 0.2000, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·西湖充电中心';

-- 滨江站计费规则
INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'PEAK',   8,  22, 0.9000, 0.4500, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·滨江充电站';

INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'FLAT',   0,   8, 0.6000, 0.3200, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·滨江充电站';

INSERT INTO `billing_rule` (`station_id`, `operator_id`, `period_type`, `start_hour`, `end_hour`, `electricity_price`, `service_price`, `effective_date`)
SELECT s.id, u.id, 'VALLEY', 22,  24, 0.3500, 0.2200, '2025-01-01'
FROM `charging_station` s JOIN `user` u ON u.id = s.operator_id WHERE s.name = '绿城·滨江充电站';

-- ------------------------------------------------------------
-- 8. 历史充电订单（user01，已完成，用于测试订单列表/评价）
-- ------------------------------------------------------------
INSERT INTO `charging_order`
    (`order_no`, `user_id`, `vehicle_id`, `gun_id`, `pile_id`, `station_id`, `operator_id`,
     `start_time`, `end_time`, `charge_kwh`, `charge_fee`, `service_fee`, `total_fee`,
     `status`, `pay_status`)
SELECT
    CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '000001'),
    u.id,
    v.id,
    g.id,
    p.id,
    s.id,
    op.id,
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR,
    32.500,
    27.63,
    13.00,
    40.63,
    'FINISHED',
    'PAID'
FROM `user` u
JOIN `vehicle` v ON v.user_id = u.id AND v.plate_no = '浙A88888'
JOIN `charging_station` s ON s.name = '绿城·西湖充电中心'
JOIN `user` op ON op.id = s.operator_id
JOIN `charging_pile` p ON p.station_id = s.id AND p.pile_no = 'PILE-WH-001'
JOIN `charging_gun` g ON g.pile_id = p.id AND g.gun_no = 'A'
WHERE u.username = 'user01';

-- 第二个完成订单（未评价，方便测试发表评价）
INSERT INTO `charging_order`
    (`order_no`, `user_id`, `vehicle_id`, `gun_id`, `pile_id`, `station_id`, `operator_id`,
     `start_time`, `end_time`, `charge_kwh`, `charge_fee`, `service_fee`, `total_fee`,
     `status`, `pay_status`)
SELECT
    CONCAT('ORD', DATE_FORMAT(NOW(),'%Y%m%d'), '000002'),
    u.id,
    v.id,
    g.id,
    p.id,
    s.id,
    op.id,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR,
    50.000,
    42.50,
    20.00,
    62.50,
    'FINISHED',
    'PAID'
FROM `user` u
JOIN `vehicle` v ON v.user_id = u.id AND v.plate_no = '浙A88888'
JOIN `charging_station` s ON s.name = '绿城·西湖充电中心'
JOIN `user` op ON op.id = s.operator_id
JOIN `charging_pile` p ON p.station_id = s.id AND p.pile_no = 'PILE-WH-001'
JOIN `charging_gun` g ON g.pile_id = p.id AND g.gun_no = 'B'
WHERE u.username = 'user01';

-- 更新 user01 消费记录与钱包余额
UPDATE `wallet` w
JOIN `user` u ON u.id = w.user_id
SET w.total_consume = 103.13, w.balance = 396.87
WHERE u.username = 'user01';

INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT u.id, o.id, -40.63, 'CONSUME', '充电消费'
FROM `user` u JOIN `charging_order` o ON o.user_id = u.id
WHERE u.username = 'user01' AND o.order_no LIKE '%000001';

INSERT INTO `payment_record` (`user_id`, `order_id`, `amount`, `type`, `remark`)
SELECT u.id, o.id, -62.50, 'CONSUME', '充电消费'
FROM `user` u JOIN `charging_order` o ON o.user_id = u.id
WHERE u.username = 'user01' AND o.order_no LIKE '%000002';

-- ------------------------------------------------------------
-- 9. 评价（第一个订单已评价）
-- ------------------------------------------------------------
INSERT INTO `evaluation` (`user_id`, `order_id`, `station_id`, `rating`, `content`)
SELECT u.id, o.id, o.station_id, 5, '充电速度很快，设备维护得很好，下次还会来！'
FROM `user` u JOIN `charging_order` o ON o.user_id = u.id
WHERE u.username = 'user01' AND o.order_no LIKE '%000001';

-- ------------------------------------------------------------
-- 10. 故障上报记录
-- ------------------------------------------------------------
INSERT INTO `fault_record` (`user_id`, `pile_id`, `pile_no`, `description`, `status`)
SELECT u.id, p.id, p.pile_no, '充电桩显示屏无法正常显示，按键无响应', 'PENDING'
FROM `user` u, `charging_pile` p
WHERE u.username = 'user01' AND p.pile_no = 'PILE-BJ-002';

-- ------------------------------------------------------------
-- 11. 系统公告（管理员发布）
-- ------------------------------------------------------------
INSERT INTO `announcement` (`title`, `content`, `type`, `status`, `creator_id`)
SELECT '系统上线公告', '新能源汽车充电桩运营管理系统正式上线，欢迎使用！', 'NOTICE', 'ONLINE', id
FROM `user` WHERE username = 'admin';

INSERT INTO `announcement` (`title`, `content`, `type`, `status`, `creator_id`)
SELECT '五一假期维护通知', '系统将于2025年5月1日凌晨2:00-4:00进行例行维护，届时服务暂停，请提前安排充电计划。', 'MAINTENANCE', 'ONLINE', id
FROM `user` WHERE username = 'admin';

-- ------------------------------------------------------------
-- 12. 预约记录（user01 预约明天）
-- ------------------------------------------------------------
INSERT INTO `reservation` (`user_id`, `pile_id`, `reserve_date`, `start_time`, `end_time`, `status`)
SELECT
    u.id,
    p.id,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 10:00:00'),
    CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY), ' 12:00:00'),
    'PENDING'
FROM `user` u, `charging_pile` p
WHERE u.username = 'user01' AND p.pile_no = 'PILE-WH-001';

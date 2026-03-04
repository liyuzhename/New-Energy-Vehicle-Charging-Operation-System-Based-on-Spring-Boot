-- ============================================================
-- 新能源汽车充电桩运营管理系统 - 数据库初始化脚本
-- 数据库：charging_db
-- ============================================================

CREATE DATABASE IF NOT EXISTS charging_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE charging_db;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名（唯一）',
    `phone`       VARCHAR(20)  NOT NULL COMMENT '手机号（唯一）',
    `password`    VARCHAR(100) NOT NULL COMMENT '加密密码（BCrypt）',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：USER/OPERATOR/ADMIN',
    `nickname`    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(200)          DEFAULT NULL COMMENT '头像路径',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 车辆表
-- ============================================================
CREATE TABLE IF NOT EXISTS `vehicle` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT      NOT NULL COMMENT '所属用户ID',
    `plate_no`     VARCHAR(20) NOT NULL COMMENT '车牌号',
    `brand`        VARCHAR(50)          DEFAULT NULL COMMENT '品牌',
    `model`        VARCHAR(50)          DEFAULT NULL COMMENT '型号',
    `battery_cap`  DECIMAL(6,1)         DEFAULT NULL COMMENT '电池容量(kWh)',
    `deleted`      TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆表';

-- ============================================================
-- 3. 充电站表
-- ============================================================
CREATE TABLE IF NOT EXISTS `charging_station` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`   BIGINT       NOT NULL COMMENT '运营商用户ID',
    `name`          VARCHAR(100) NOT NULL COMMENT '充电站名称',
    `address`       VARCHAR(200) NOT NULL COMMENT '详细地址',
    `city`          VARCHAR(50)           DEFAULT NULL COMMENT '城市',
    `longitude`     DECIMAL(10,7)         DEFAULT NULL COMMENT '经度',
    `latitude`      DECIMAL(10,7)         DEFAULT NULL COMMENT '纬度',
    `business_hours` VARCHAR(50)          DEFAULT NULL COMMENT '营业时间',
    `parking_fee`   VARCHAR(200)          DEFAULT NULL COMMENT '停车费说明',
    `status`        VARCHAR(20)  NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE/OFFLINE',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电站表';

-- ============================================================
-- 4. 充电桩表
-- ============================================================
CREATE TABLE IF NOT EXISTS `charging_pile` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pile_no`     VARCHAR(30)  NOT NULL COMMENT '桩编号（唯一）',
    `station_id`  BIGINT       NOT NULL COMMENT '所属充电站ID',
    `operator_id` BIGINT       NOT NULL COMMENT '归属运营商用户ID',
    `pile_type`   VARCHAR(10)  NOT NULL COMMENT '桩类型：SLOW/FAST',
    `power`       DECIMAL(5,1) NOT NULL DEFAULT 7.0 COMMENT '额定功率(kW)',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'IDLE' COMMENT '状态：IDLE/OCCUPIED/FAULT/OFFLINE',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pile_no` (`pile_no`),
    KEY `idx_station_id` (`station_id`),
    KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电桩表';

-- ============================================================
-- 5. 充电枪表
-- ============================================================
CREATE TABLE IF NOT EXISTS `charging_gun` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pile_id`     BIGINT      NOT NULL COMMENT '所属充电桩ID',
    `gun_no`      VARCHAR(10) NOT NULL COMMENT '枪口编号（A/B）',
    `gun_type`    VARCHAR(10) NOT NULL COMMENT '枪类型：AC交流/DC直流',
    `power`       DECIMAL(5,1)         DEFAULT NULL COMMENT '额定功率(kW)',
    `status`      VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT '状态：IDLE/OCCUPIED/FAULT',
    `deleted`     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_pile_id` (`pile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电枪表';

-- ============================================================
-- 6. 预约表
-- ============================================================
CREATE TABLE IF NOT EXISTS `reservation` (
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT   NOT NULL COMMENT '用户ID',
    `pile_id`        BIGINT   NOT NULL COMMENT '充电桩ID',
    `reserve_date`   DATE     NOT NULL COMMENT '预约日期',
    `start_time`     DATETIME NOT NULL COMMENT '预约开始时间',
    `end_time`       DATETIME NOT NULL COMMENT '预约结束时间',
    `status`         VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/CONFIRMED/CANCELLED/EXPIRED',
    `deleted`        TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_pile_id` (`pile_id`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约表';

-- ============================================================
-- 7. 计费规则表
-- ============================================================
CREATE TABLE IF NOT EXISTS `billing_rule` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `station_id`        BIGINT       NOT NULL COMMENT '所属充电站ID',
    `operator_id`       BIGINT       NOT NULL COMMENT '配置运营商ID',
    `period_type`       VARCHAR(10)  NOT NULL COMMENT '时段类型：PEAK/FLAT/VALLEY',
    `start_hour`        TINYINT      NOT NULL COMMENT '开始小时(0~23)',
    `end_hour`          TINYINT      NOT NULL COMMENT '结束小时(0~23)',
    `electricity_price` DECIMAL(6,4) NOT NULL COMMENT '电费单价(元/kWh)',
    `service_price`     DECIMAL(6,4) NOT NULL COMMENT '服务费单价(元/kWh)',
    `effective_date`    DATE         NOT NULL COMMENT '生效日期',
    `deleted`           TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_station_id` (`station_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计费规则表';

-- ============================================================
-- 8. 充电订单表
-- ============================================================
CREATE TABLE IF NOT EXISTS `charging_order` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`    VARCHAR(30)   NOT NULL COMMENT '订单编号（唯一）',
    `user_id`     BIGINT        NOT NULL COMMENT '发起用户ID',
    `vehicle_id`  BIGINT                 DEFAULT NULL COMMENT '使用车辆ID',
    `gun_id`      BIGINT        NOT NULL COMMENT '使用充电枪ID',
    `pile_id`     BIGINT        NOT NULL COMMENT '充电桩ID',
    `station_id`  BIGINT        NOT NULL COMMENT '充电站ID',
    `operator_id` BIGINT        NOT NULL COMMENT '运营商ID',
    `start_time`  DATETIME      NOT NULL COMMENT '充电开始时间',
    `end_time`    DATETIME               DEFAULT NULL COMMENT '充电结束时间',
    `charge_kwh`  DECIMAL(8,3)  NOT NULL DEFAULT 0 COMMENT '充电电量(kWh)',
    `charge_fee`  DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '电费(元)',
    `service_fee` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '服务费(元)',
    `total_fee`   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总费用(元)',
    `status`      VARCHAR(20)   NOT NULL DEFAULT 'CHARGING' COMMENT '订单状态：WAITING/CHARGING/FINISHED/CANCELLED/REFUNDING/REFUNDED',
    `pay_status`  VARCHAR(20)   NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID/PAID/REFUNDING/REFUNDED',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_station_id` (`station_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充电订单表';

-- ============================================================
-- 9. 钱包表
-- ============================================================
CREATE TABLE IF NOT EXISTS `wallet` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT        NOT NULL COMMENT '用户ID（唯一）',
    `balance`        DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '当前余额(元)',
    `total_recharge` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计充值金额',
    `total_consume`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额',
    `deleted`        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户钱包表';

-- ============================================================
-- 10. 支付记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `payment_record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '用户ID',
    `order_id`    BIGINT                 DEFAULT NULL COMMENT '关联订单ID',
    `amount`      DECIMAL(10,2) NOT NULL COMMENT '金额(元)，充值为正，消费/退款为负',
    `type`        VARCHAR(20)   NOT NULL COMMENT '类型：RECHARGE充值/CONSUME消费/REFUND退款',
    `remark`      VARCHAR(200)           DEFAULT NULL COMMENT '备注',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 11. 评价表
-- ============================================================
CREATE TABLE IF NOT EXISTS `evaluation` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '评价用户ID',
    `order_id`    BIGINT   NOT NULL COMMENT '关联订单ID',
    `station_id`  BIGINT   NOT NULL COMMENT '充电站ID',
    `rating`      TINYINT  NOT NULL COMMENT '评分(1~5)',
    `content`     TEXT              DEFAULT NULL COMMENT '评价内容',
    `reply`       TEXT              DEFAULT NULL COMMENT '运营商回复',
    `is_hidden`   TINYINT  NOT NULL DEFAULT 0 COMMENT '是否屏蔽：0正常 1屏蔽',
    `deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_station_id` (`station_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================================
-- 12. 故障记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS `fault_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '上报用户ID',
    `pile_id`     BIGINT       NOT NULL COMMENT '故障充电桩ID',
    `pile_no`     VARCHAR(30)  NOT NULL COMMENT '充电桩编号',
    `description` TEXT         NOT NULL COMMENT '故障描述',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/PROCESSING/REPAIRED',
    `handle_note` VARCHAR(500)          DEFAULT NULL COMMENT '处理备注',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_pile_id` (`pile_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='故障记录表';

-- ============================================================
-- 13. 公告表
-- ============================================================
CREATE TABLE IF NOT EXISTS `announcement` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title`       VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`     TEXT         NOT NULL COMMENT '公告内容',
    `type`        VARCHAR(20)  NOT NULL DEFAULT 'NOTICE' COMMENT '类型：NOTICE通知/MAINTENANCE维护/ACTIVITY活动',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE上线/OFFLINE下线',
    `creator_id`  BIGINT       NOT NULL COMMENT '创建者ID（管理员）',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

-- ============================================================
-- 14. 操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id`  BIGINT                DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(50)          DEFAULT NULL COMMENT '操作人名称',
    `operation`    VARCHAR(200) NOT NULL COMMENT '操作内容',
    `method`       VARCHAR(200)          DEFAULT NULL COMMENT '请求方法',
    `params`       TEXT                  DEFAULT NULL COMMENT '请求参数',
    `ip`           VARCHAR(50)           DEFAULT NULL COMMENT '操作IP',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '操作状态：1成功 0失败',
    `error_msg`    TEXT                  DEFAULT NULL COMMENT '错误信息',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 默认管理员账号（密码：admin123，BCrypt加密）
INSERT IGNORE INTO `user` (`username`, `phone`, `password`, `role`, `nickname`, `status`)
VALUES ('admin', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6khmK', 'ADMIN', '系统管理员', 1);

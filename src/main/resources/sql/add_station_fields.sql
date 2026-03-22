-- 为 charging_station 表添加联系电话和站点描述字段
ALTER TABLE `charging_station`
    ADD COLUMN `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话' AFTER `parking_fee`,
    ADD COLUMN `description` VARCHAR(500) DEFAULT NULL COMMENT '站点描述' AFTER `contact_phone`;

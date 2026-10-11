-- ============================================================
-- 考研刷题小程序 - 「编辑资料」扩充字段 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: user 表新增 7 个资料字段
--         基本信息：signature 个性签名 / gender 性别 / birthday 生日 / region 地区
--         备考信息：target_school 目标院校 / target_major 报考专业 / exam_year 考研年份
-- 说明: 字段全部可空 —— 老用户不填也得能正常用；前端「只提交改动字段」的
--       动态 SET 逻辑也依赖「可空」来区分「没改」与「清空」。
-- 执行: mysql -uroot -p < profile_fields_migration.sql
-- 注意: 全新环境可直接跑 init.sql（已同步这些列）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- MySQL 的 ADD COLUMN 没有 IF NOT EXISTS，用 information_schema 判断后再动态执行，
-- 这样脚本可以重复跑（第二次会全部跳过）。PREPARE/EXECUTE 需在会话内完成。

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'signature') = 0,
    'ALTER TABLE `user` ADD COLUMN `signature` VARCHAR(64) NULL COMMENT ''个性签名'' AFTER `avatar_url`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'gender') = 0,
    'ALTER TABLE `user` ADD COLUMN `gender` TINYINT NOT NULL DEFAULT 0 COMMENT ''性别：0未知 1男 2女'' AFTER `signature`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'birthday') = 0,
    'ALTER TABLE `user` ADD COLUMN `birthday` DATE NULL COMMENT ''生日（可空）'' AFTER `gender`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'region') = 0,
    'ALTER TABLE `user` ADD COLUMN `region` VARCHAR(32) NULL COMMENT ''所在地区'' AFTER `birthday`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'target_school') = 0,
    'ALTER TABLE `user` ADD COLUMN `target_school` VARCHAR(64) NULL COMMENT ''目标院校'' AFTER `region`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'target_major') = 0,
    'ALTER TABLE `user` ADD COLUMN `target_major` VARCHAR(64) NULL COMMENT ''报考专业'' AFTER `target_school`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'exam_year') = 0,
    'ALTER TABLE `user` ADD COLUMN `exam_year` SMALLINT NULL COMMENT ''考研年份（如 2027）'' AFTER `target_major`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

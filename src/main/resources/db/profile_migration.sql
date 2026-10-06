-- ============================================================
-- 考研刷题小程序 - 「编辑资料」功能 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: user.avatar_url 由 VARCHAR(512) 改为 MEDIUMTEXT
--       原设计为「头像 URL 预留」，现改为直接存 Base64 DataURL
--       （前端压到 160x160 JPEG q0.75，约 5~10KB，VARCHAR(512) 装不下）
-- 执行: mysql -uroot -p < profile_migration.sql
-- 注意: 全新环境可直接跑 init.sql（已同步该定义）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- MODIFY 语句本身幂等：重复执行结果一致，只是列定义不变
ALTER TABLE `user`
    MODIFY COLUMN `avatar_url` MEDIUMTEXT NULL COMMENT '头像（Base64 DataURL，如 data:image/jpeg;base64,...）';

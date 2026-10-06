-- ============================================================
-- 考研刷题小程序 - 好友功能 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: 新建 friend 好友关系表 / friend_request 好友请求表
-- 执行: mysql -uroot -p < friend_migration.sql
-- 注意: 全新环境可直接跑 init.sql（含同样的表）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- ------------------------------------------------------------
-- friend 好友关系表
--   采用「双向各存一行」模型：A 与 B 成为好友时写 (A,B) 与 (B,A) 两行。
--   好处：查「我的好友」只需 WHERE user_id = ?，无需 OR 分支，索引利用率高。
--   uk_user_friend 保证同一对关系不重复（重复添加走 INSERT IGNORE）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `friend` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `user_id`    BIGINT UNSIGNED NOT NULL                COMMENT '关系拥有者用户ID',
    `friend_id`  BIGINT UNSIGNED NOT NULL                COMMENT '对方用户ID',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend` (`friend_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友关系表（双向各存一行）';

-- ------------------------------------------------------------
-- friend_request 好友请求表
--   status: 0待处理 1已同意 2已拒绝
--   uk_from_to：同一方向只保留一条，被拒绝后可复用同一行
--   （发送时用 INSERT ... ON DUPLICATE KEY UPDATE status = IF(status = 2, 0, status) 重新激活）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `friend_request` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '请求ID',
    `from_user_id` BIGINT UNSIGNED NOT NULL                COMMENT '发起人用户ID',
    `to_user_id`   BIGINT UNSIGNED NOT NULL                COMMENT '接收人用户ID',
    `status`       TINYINT         NOT NULL DEFAULT 0      COMMENT '状态：0待处理 1已同意 2已拒绝',
    `message`      VARCHAR(64)     DEFAULT NULL            COMMENT '验证附言（可选）',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发起时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_from_to` (`from_user_id`, `to_user_id`),
    KEY `idx_to_status` (`to_user_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友请求表';

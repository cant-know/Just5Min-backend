-- ============================================================
-- 每日打卡功能迁移脚本（幂等：已有数据环境可安全重复执行）
-- 全新环境直接跑 init.sql 即可，无需执行本文件。
-- ============================================================

USE `just5min`;

-- 表：每日打卡记录（每用户每天最多一条，靠唯一键保证）
CREATE TABLE IF NOT EXISTS `check_in` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '打卡记录ID',
    `user_id`    BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `check_date` DATE            NOT NULL                COMMENT '打卡日期（自然日）',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `check_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '每日打卡记录表';

-- ============================================================
-- 考研刷题小程序 - 收藏功能 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: 新建 favorite 收藏表
-- 执行: mysql -uroot -p < favorite_migration.sql
-- 注意: 全新环境可直接跑 init.sql（含同样的表）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- ------------------------------------------------------------
-- favorite 收藏表
--   category_id 为冗余字段，用于「我的收藏」按分类筛选（免 join question）
--   uk_user_question 保证同一用户同一题只收藏一次（重复收藏走 INSERT IGNORE）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `question_id` BIGINT UNSIGNED NOT NULL                COMMENT '题目ID',
    `category_id` BIGINT UNSIGNED NOT NULL                COMMENT '分类ID（冗余，按分类筛选）',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_cat` (`user_id`, `category_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题目收藏表';

-- ============================================================
-- 考研刷题小程序 - 聊天私信 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: 新建 message 私信消息表
-- 执行: mysql -uroot -p < message_migration.sql
-- 注意: 全新环境可直接跑 init.sql（含同样的表）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- ------------------------------------------------------------
-- message 私信消息表（好友之间一对一聊天）
--   消息按「单行单方向」存储，一条消息只占一行：
--     我发给好友 → (from_user_id=我, to_user_id=好友)
--     好友发给我 → (from_user_id=好友, to_user_id=我)
--   会话查询用「A→B 或 B→A」两个方向的 UNION，靠下面两条复合索引走索引：
--     idx_from_to (from_user_id, to_user_id, id) —— 我发出的消息按人倒序翻页
--     idx_to_from (to_user_id, from_user_id, id) —— 对方发给我的消息按人倒序翻页 / 未读数
--   is_read: 0未读 1已读；只有收件人能置已读（进会话页时把对方发来的未读批量置 1）
--   content 用 VARCHAR(500)：单条文本消息足够，且避免 TEXT 参与排序/索引的额外开销
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `message` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `from_user_id` BIGINT UNSIGNED NOT NULL                COMMENT '发送者用户ID',
    `to_user_id`   BIGINT UNSIGNED NOT NULL                COMMENT '接收者用户ID',
    `content`      VARCHAR(500)    NOT NULL                COMMENT '消息正文（纯文本）',
    `is_read`      TINYINT         NOT NULL DEFAULT 0      COMMENT '是否已读：0未读 1已读',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    KEY `idx_from_to` (`from_user_id`, `to_user_id`, `id`),
    KEY `idx_to_from` (`to_user_id`, `from_user_id`, `id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '好友私信消息表';

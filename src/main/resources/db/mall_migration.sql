-- ============================================================
-- 考研刷题小程序 - 积分 + 商城 增量迁移脚本
-- 用途: 在已有数据的环境上安全升级（不 DROP、不清数据，可重复执行）
-- 内容: user 表加 points 列；新建 mall_category / mall_product /
--       exchange_record / points_log 四表；写入示例种子数据
-- 执行: mysql -uroot -p < mall_migration.sql
-- 注意: 全新环境可直接跑 init.sql（含同样的表与种子）；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- ------------------------------------------------------------
-- 1. user 表加积分列（MySQL 8 无 ADD COLUMN IF NOT EXISTS，用 information_schema 判断）
-- ------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'points');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `points` INT NOT NULL DEFAULT 0 COMMENT ''积分余额'' AFTER `status`',
    'SELECT 1 AS points_column_already_exists');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. mall_category 商城分类表（仅一级，不做多级）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mall_category` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商城分类ID',
    `name`       VARCHAR(64)     NOT NULL                COMMENT '分类名称，如：会员特权/备考资料',
    `icon`       VARCHAR(512)    DEFAULT NULL            COMMENT '图标URL（预留）',
    `sort`       INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `status`     TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`, `id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商城分类表';

-- ------------------------------------------------------------
-- 3. mall_product 商品表（MVP 均为虚拟电子商品）
--    price 为兑换所需积分；库存对虚拟商品同样生效，防超兑
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mall_product` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `category_id`  BIGINT UNSIGNED NOT NULL                COMMENT '商城分类ID',
    `name`         VARCHAR(128)    NOT NULL                COMMENT '商品名称',
    `description`  VARCHAR(512)    DEFAULT NULL            COMMENT '商品简介',
    `cover_url`    VARCHAR(512)    DEFAULT NULL            COMMENT '封面图URL（预留）',
    `price`        INT             NOT NULL                COMMENT '兑换所需积分',
    `stock`        INT             NOT NULL DEFAULT 0      COMMENT '库存（虚拟商品同样占库存）',
    `product_type` TINYINT         NOT NULL DEFAULT 1      COMMENT '类型：1虚拟电子商品（预留2实物）',
    `status`       TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1上架 0下架',
    `sort`         INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_cat_status` (`category_id`, `status`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品表';

-- ------------------------------------------------------------
-- 4. exchange_record 兑换记录表
--    product_name / points_cost 为兑换时刻快照，商品后续改名/调价不影响历史
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `exchange_record` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '兑换记录ID',
    `user_id`      BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `product_id`   BIGINT UNSIGNED NOT NULL                COMMENT '商品ID',
    `product_name` VARCHAR(128)    NOT NULL                COMMENT '商品名称快照',
    `points_cost`  INT             NOT NULL                COMMENT '消耗积分快照',
    `status`       TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1已兑换（预留2已发放 3已取消）',
    `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '兑换记录表';

-- ------------------------------------------------------------
-- 5. points_log 积分流水表
--    source: 1答题 2兑换；change_amount 正为获取、负为消耗；balance 为变动后余额快照
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `points_log` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '流水ID',
    `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `change_amount` INT             NOT NULL                COMMENT '变动值，正获取负消耗',
    `balance`       INT             NOT NULL                COMMENT '变动后余额快照',
    `source`        TINYINT         NOT NULL                COMMENT '来源：1答题 2兑换',
    `ref_id`        BIGINT UNSIGNED DEFAULT NULL            COMMENT '关联业务ID（答题记录ID/兑换记录ID）',
    `remark`        VARCHAR(255)    DEFAULT NULL            COMMENT '备注',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '积分流水表';

-- ============================================================
-- 种子数据（幂等：显式 id + ON DUPLICATE KEY UPDATE，可重复执行）
-- ============================================================

-- 商城分类（4 个一级，id 固定）
INSERT INTO `mall_category` (`id`, `name`, `sort`, `status`) VALUES
(1, '会员特权', 1, 1),
(2, '备考资料', 2, 1),
(3, '视频课程', 3, 1),
(4, '学习工具', 4, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`), `status` = VALUES(`status`);

-- 商品（10 个示例电子商品）
INSERT INTO `mall_product`
(`id`, `category_id`, `name`, `description`, `price`, `stock`, `product_type`, `status`, `sort`) VALUES
(1,  1, '会员 3 天',              '解锁全部题库与解析 3 天',        50,  999, 1, 1, 1),
(2,  1, '会员 7 天',              '解锁全部题库与解析 7 天',       120,  999, 1, 1, 2),
(3,  1, '会员 30 天',             '解锁全部题库与解析 30 天',      400,  200, 1, 1, 3),
(4,  2, '考研政治冲刺必背 100 题', 'PDF 电子版，随时查看',           80,  500, 1, 1, 1),
(5,  2, '考研英语高频词汇手册',   'PDF 电子版，含例句',             60,  500, 1, 1, 2),
(6,  2, '数学公式速查卡',         'PDF 电子版，考前速览',           40,  500, 1, 1, 3),
(7,  3, '英语写作模板课',         '录播课 3 讲',                   300,  100, 1, 1, 1),
(8,  3, '408 数据结构精讲',       '录播课 8 讲',                   600,   50, 1, 1, 2),
(9,  4, '错题导出工具使用权',     '将错题本导出为 PDF',            200,  100, 1, 1, 1),
(10, 4, '学习打卡皮肤',           '个性化打卡背景',                 30,  999, 1, 1, 2)
ON DUPLICATE KEY UPDATE `price` = VALUES(`price`), `stock` = VALUES(`stock`), `status` = VALUES(`status`);

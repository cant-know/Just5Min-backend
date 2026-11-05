-- ============================================================
-- 积分商城改造：虚拟装扮商品（头像框 / 聊天气泡 / 主页挂件）
--   1) user 加 3 个佩戴槽位（avatar_frame_id / chat_bubble_id / pendant_id）
--   2) mall_category 加 biz_type（1资料文章 2装扮商城），文章专栏默认只取 1
--   3) mall_product 加 decoration_kind / decoration_asset
--   4) exchange_record 加 idx_user_product（拥有判断 / 详情页幂等）
--   5) 旧 10 个资料商品软下架（status=0，不删行——兑换历史与 article 关联依赖）
--   6) 新增装扮分类 5/6/7 与装扮商品 11~22（每类 4 件，价格 0/30/50/100）
-- 幂等：可重复执行；已有数据环境只跑本脚本，init.sql 是 DROP 重建勿在数据环境执行
-- ============================================================
USE `just5min`;

-- ---------- 1. user 佩戴槽位（3 列，逐列判断） ----------
SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'avatar_frame_id') = 0,
    'ALTER TABLE `user` ADD COLUMN `avatar_frame_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''佩戴中的头像框商品ID'' AFTER `points`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'chat_bubble_id') = 0,
    'ALTER TABLE `user` ADD COLUMN `chat_bubble_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''佩戴中的聊天气泡商品ID'' AFTER `avatar_frame_id`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'pendant_id') = 0,
    'ALTER TABLE `user` ADD COLUMN `pendant_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''佩戴中的主页挂件商品ID'' AFTER `chat_bubble_id`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. mall_category.biz_type ----------
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mall_category' AND COLUMN_NAME = 'biz_type') = 0,
    'ALTER TABLE `mall_category` ADD COLUMN `biz_type` TINYINT NOT NULL DEFAULT 1 COMMENT ''业务类型：1资料文章 2装扮商城'' AFTER `name`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 3. mall_product.decoration_kind / decoration_asset ----------
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'decoration_kind') = 0,
    'ALTER TABLE `mall_product` ADD COLUMN `decoration_kind` TINYINT DEFAULT NULL COMMENT ''装扮类型：1头像框 2聊天气泡 3主页挂件；NULL=非装扮'' AFTER `product_type`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'decoration_asset') = 0,
    'ALTER TABLE `mall_product` ADD COLUMN `decoration_asset` VARCHAR(255) DEFAULT NULL COMMENT ''装扮资源：头像框/挂件为透明PNG路径；气泡为主题key(lime/sakura/ocean/gold)'' AFTER `decoration_kind`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 4. exchange_record 拥有判断索引 ----------
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'exchange_record' AND INDEX_NAME = 'idx_user_product') = 0,
    'ALTER TABLE `exchange_record` ADD KEY `idx_user_product` (`user_id`, `product_id`)',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 5. 旧资料商品软下架（保留行：兑换历史与文章关联依赖） ----------
UPDATE `mall_product` SET `status` = 0
WHERE `id` BETWEEN 1 AND 10 AND `article_id` IS NOT NULL;

-- ---------- 6. 装扮分类（id 5/6/7，biz_type=2；不动资料分类 1~4） ----------
INSERT INTO `mall_category` (`id`, `name`, `biz_type`, `sort`, `status`) VALUES
(5, '头像框',   2, 1, 1),
(6, '聊天气泡', 2, 2, 1),
(7, '主页挂件', 2, 3, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `biz_type` = VALUES(`biz_type`),
                        `sort` = VALUES(`sort`), `status` = VALUES(`status`);

-- ---------- 7. 装扮商品（id 11~22，每类 4 件，价格梯度 0/30/50/100） ----------
-- decoration_asset：头像框/挂件 = 小程序包内透明 PNG 路径；气泡 = 前端 CSS 主题 key
INSERT INTO `mall_product`
(`id`, `category_id`, `name`, `description`, `cover_url`, `article_id`, `price`, `stock`,
 `product_type`, `decoration_kind`, `decoration_asset`, `status`, `sort`) VALUES
(11, 5, '简约光圈', '干净的白色描边光圈，低调百搭', '/static/goods/frame-minimal.png', NULL, 0, 9999, 1, 1, '/static/decoration/frame-minimal.png', 1, 1),
(12, 5, '樱花边框', '粉色樱花环绕，春日气息拉满', '/static/goods/frame-sakura.png', NULL, 30, 9999, 1, 1, '/static/decoration/frame-sakura.png', 1, 2),
(13, 5, '星河之环', '蓝紫星云光环，缀满点点星光', '/static/goods/frame-galaxy.png', NULL, 50, 9999, 1, 1, '/static/decoration/frame-galaxy.png', 1, 3),
(14, 5, '鎏金皇冠', '厚金描边加皇冠，尊贵感十足', '/static/goods/frame-gold.png', NULL, 100, 9999, 1, 1, '/static/decoration/frame-gold.png', 1, 4),
(15, 6, '青柠气泡', '清爽青柠渐变，聊天自带凉风', '/static/goods/bubble-lime.png', NULL, 0, 9999, 1, 2, 'lime', 1, 1),
(16, 6, '樱花气泡', '樱花粉渐变，说话都变温柔', '/static/goods/bubble-sakura.png', NULL, 30, 9999, 1, 2, 'sakura', 1, 2),
(17, 6, '海盐气泡', '海盐蓝渐变，像海风一样清新', '/static/goods/bubble-ocean.png', NULL, 50, 9999, 1, 2, 'ocean', 1, 3),
(18, 6, '鎏金气泡', '暗金质感，一开口就是大佬', '/static/goods/bubble-gold.png', NULL, 100, 9999, 1, 2, 'gold', 1, 4),
(19, 7, '学习小猫', '趴在头像边陪你学习的小猫', '/static/goods/pendant-cat.png', NULL, 0, 9999, 1, 3, '/static/decoration/pendant-cat.png', 1, 1),
(20, 7, '上岸火箭', '一飞冲天的火箭，上岸好兆头', '/static/goods/pendant-rocket.png', NULL, 30, 9999, 1, 3, '/static/decoration/pendant-rocket.png', 1, 2),
(21, 7, '学霸皇冠', '金光闪闪的小皇冠，学霸认证', '/static/goods/pendant-crown.png', NULL, 50, 9999, 1, 3, '/static/decoration/pendant-crown.png', 1, 3),
(22, 7, '长明台灯', '深夜自习室的那盏灯', '/static/goods/pendant-lamp.png', NULL, 100, 9999, 1, 3, '/static/decoration/pendant-lamp.png', 1, 4)
ON DUPLICATE KEY UPDATE `category_id` = VALUES(`category_id`), `name` = VALUES(`name`),
    `description` = VALUES(`description`), `cover_url` = VALUES(`cover_url`),
    `article_id` = VALUES(`article_id`), `price` = VALUES(`price`), `stock` = VALUES(`stock`),
    `product_type` = VALUES(`product_type`), `decoration_kind` = VALUES(`decoration_kind`),
    `decoration_asset` = VALUES(`decoration_asset`), `status` = VALUES(`status`), `sort` = VALUES(`sort`);

-- ---------- 8. 自检 ----------
SELECT id, category_id, name, price, status, decoration_kind, decoration_asset, cover_url
FROM mall_product ORDER BY id;
SELECT id, name, biz_type, sort, status FROM mall_category ORDER BY id;

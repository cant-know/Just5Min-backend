-- ============================================================
-- 考研刷题小程序 - 数据库初始化脚本
-- 数据库: just5min   字符集: utf8mb4
-- 表: user / category / question / answer_record / wrong_question
--      mall_category / mall_product / exchange_record / points_log（积分商城）
-- 分类: 三级结构（一级 → 二级分组/直挂叶子 → 三级叶子），靠 parent_id 自引用
-- 题型: 目前为选择题(单选+多选)，question_type 已为判断/填空/简答预留
-- 执行: mysql -uroot -p < init.sql
-- 注意: 脚本为 DROP + CREATE 全量重建，会清空 user / answer_record / wrong_question
-- ============================================================

CREATE DATABASE IF NOT EXISTS `just5min`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `just5min`;

-- ------------------------------------------------------------
-- 1. user 用户表（双登录体系）
--    微信登录：openid 有值、phone/password_hash 为空
--    手机号注册：phone/password_hash 有值、openid 为空
--    两条唯一键均允许 NULL（MySQL 唯一索引对 NULL 不生效），互不冲突
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid`        VARCHAR(64)     DEFAULT NULL            COMMENT '微信openid（微信登录用户才有）',
    `unionid`       VARCHAR(64)     DEFAULT NULL            COMMENT '微信unionid（预留：绑定开放平台后才有）',
    `phone`         VARCHAR(20)     DEFAULT NULL            COMMENT '手机号（手机号注册用户才有）',
    `password_hash` VARCHAR(255)    DEFAULT NULL            COMMENT '密码哈希PBKDF2（微信登录用户为空）',
    `nickname`      VARCHAR(64)     DEFAULT NULL            COMMENT '昵称（微信静默登录时为空）',
    `avatar_url`    VARCHAR(512)    DEFAULT NULL            COMMENT '头像URL（预留）',
    `status`        TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1正常 0禁用',
    `points`        INT             NOT NULL DEFAULT 0      COMMENT '积分余额（每提交一次答案+1，兑换商品扣减）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_unionid` (`unionid`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表';

-- ------------------------------------------------------------
-- 2. category 分类表（三级：一级 / 二级分组或直挂叶子 / 三级叶子）
--    parent_id = 0 表示一级分类；自引用可继续向下扩展
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id`  BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '父分类ID，0=一级分类【自引用，支持多级】',
    `name`       VARCHAR(64)     NOT NULL                COMMENT '分类名称，如：考研政治/计算机408',
    `icon`       VARCHAR(512)    DEFAULT NULL            COMMENT '图标URL（预留）',
    `sort`       INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `status`     TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1启用 0停用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题目分类表';

-- ------------------------------------------------------------
-- 3. question 题目表（核心）
--    question_type: 1单选 2多选（预留 3判断 4填空 5简答）
--    options: JSON，格式 {"A":"...","B":"...","C":"...","D":"..."}
--    answer : 正确选项，单选 "A"；多选按字母升序 "ABD"
--    category_id 指向**叶子分类**（有题目的最小分类）
--    【判断题扩展】question_type=3 时 options 存 {"A":"正确","B":"错误"}，表无需改动
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `category_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属分类ID（叶子分类）',
    `question_type` TINYINT         NOT NULL DEFAULT 1      COMMENT '题型：1单选 2多选（预留3判断4填空5简答）',
    `content`       TEXT            NOT NULL                COMMENT '题干',
    `options`       JSON            NOT NULL                COMMENT '选项JSON：{"A":"...","B":"...","C":"...","D":"..."}',
    `answer`        VARCHAR(16)     NOT NULL                COMMENT '正确答案，单选"A"，多选字母升序"ABD"',
    `analysis`      TEXT            DEFAULT NULL            COMMENT '答案解析（可为空）',
    `difficulty`    TINYINT         NOT NULL DEFAULT 1      COMMENT '难度1-5（预留）',
    `status`        TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1上架 0下架（预留）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_cat_type` (`category_id`, `question_type`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '题目表';

-- ------------------------------------------------------------
-- 4. answer_record 答题记录表
--    category_id 为冗余字段，用于按分类统计（免 join question）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `answer_record`;
CREATE TABLE `answer_record` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id`     BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `question_id` BIGINT UNSIGNED NOT NULL                COMMENT '题目ID',
    `category_id` BIGINT UNSIGNED NOT NULL                COMMENT '分类ID（冗余，便于按分类统计）',
    `user_answer` VARCHAR(16)     NOT NULL                COMMENT '用户提交的答案（规范化后），如"ABD"',
    `is_correct`  TINYINT(1)      NOT NULL                COMMENT '是否正确：1对 0错',
    `duration_ms` INT             DEFAULT NULL            COMMENT '本题用时毫秒（预留）',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_time` (`user_id`, `created_at`),
    KEY `idx_user_cat` (`user_id`, `category_id`, `created_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '答题记录表';

-- ------------------------------------------------------------
-- 5. wrong_question 错题本表（answer_record 的派生汇总表）
--    答错时 INSERT ... ON DUPLICATE KEY UPDATE wrong_count+1
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `wrong_question`;
CREATE TABLE `wrong_question` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '错题ID',
    `user_id`           BIGINT UNSIGNED NOT NULL                COMMENT '用户ID',
    `question_id`       BIGINT UNSIGNED NOT NULL                COMMENT '题目ID',
    `category_id`       BIGINT UNSIGNED NOT NULL                COMMENT '分类ID（冗余，按分类筛选）',
    `wrong_count`       INT             NOT NULL DEFAULT 1      COMMENT '累计错误次数',
    `last_wrong_answer` VARCHAR(16)     DEFAULT NULL            COMMENT '最近一次错误提交的答案',
    `master_status`     TINYINT         NOT NULL DEFAULT 0      COMMENT '掌握状态：0未掌握 1已掌握（预留）',
    `last_wrong_at`     DATETIME        NOT NULL                COMMENT '最近一次答错时间',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次加入错题本时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_cat` (`user_id`, `category_id`, `master_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '错题本表';

-- ============================================================
-- 种子数据
-- ============================================================

-- ------------------------------------------------------------
-- 分类：三级结构（id 固定，便于题目引用）
--   100 学历提升 ── 110 考研公共课 ── 111 考研政治 / 112 英语一 / 113 英语二 / 114 数学一 / 115 数学二
--                 ├ 120 考研专业课 ── 121 计算机408
--                 └ 130 计算机四级（二级直挂叶子）
--   200 计算机考试 ── 201 计算机二级 / 202 计算机三级（二级直挂叶子）
--   300 财会金融   ── 301 初级会计 / 302 银行从业（二级直挂叶子）
-- ------------------------------------------------------------
INSERT INTO `category` (`id`, `parent_id`, `name`, `sort`, `status`) VALUES
-- 一级分类
(100, 0,   '学历提升',     1, 1),
(200, 0,   '计算机考试',   2, 1),
(300, 0,   '财会金融',     3, 1),
-- 学历提升 → 二级（分组）
(110, 100, '考研公共课',   1, 1),
(120, 100, '考研专业课',   2, 1),
-- 学历提升 → 二级（直挂叶子）
(130, 100, '计算机四级',   3, 1),
-- 考研公共课 → 三级叶子
(111, 110, '考研政治',     1, 1),
(112, 110, '考研英语一',   2, 1),
(113, 110, '考研英语二',   3, 1),
(114, 110, '考研数学一',   4, 1),
(115, 110, '考研数学二',   5, 1),
-- 考研专业课 → 三级叶子
(121, 120, '计算机408',    1, 1),
-- 计算机考试 → 二级直挂叶子
(201, 200, '计算机二级',   1, 1),
(202, 200, '计算机三级',   2, 1),
-- 财会金融 → 二级直挂叶子
(301, 300, '初级会计',     1, 1),
(302, 300, '银行从业',     2, 1);

-- ============================================================
-- 题目（共 100 道）
-- ============================================================

-- 题目：考研政治（category_id=111，11 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(111, 1, '马克思主义哲学的直接理论来源是（  ）',
 '{"A":"古希腊罗马哲学","B":"德国古典哲学","C":"英国经验主义哲学","D":"法国启蒙思想"}',
 'B', '马克思主义哲学的直接理论来源是德国古典哲学，特别是黑格尔的辩证法和费尔巴哈的唯物主义。', 2),
(111, 1, '实践是检验真理的唯一标准，这是因为实践具有（  ）',
 '{"A":"客观物质性","B":"主观能动性","C":"社会历史性","D":"直接现实性"}',
 'D', '实践是联结主观与客观的桥梁，具有直接现实性，能把主观认识同客观实际联系起来加以对照，因此成为检验真理的唯一标准。', 3),
(111, 1, '唯物辩证法的实质和核心是（  ）',
 '{"A":"质量互变规律","B":"对立统一规律","C":"否定之否定规律","D":"联系和发展的观点"}',
 'B', '对立统一规律揭示了事物发展的源泉和动力，是唯物辩证法的实质和核心。', 2),
(111, 1, '认识的本质是（  ）',
 '{"A":"主体对客体的能动反映","B":"主观自生的","C":"客观精神的产物","D":"感性材料的简单堆积"}',
 'A', '辩证唯物主义认为认识是主体在实践基础上对客体的能动反映。', 2),
(111, 1, '社会存在决定社会意识，社会意识是社会存在的反映。这一观点体现了（  ）',
 '{"A":"唯物主义","B":"辩证法","C":"历史唯物主义","D":"认识论"}',
 'C', '社会存在决定社会意识是历史唯物主义的基本观点。', 2),
(111, 1, '生产力中最活跃、最革命的因素是（  ）',
 '{"A":"劳动者","B":"劳动资料","C":"劳动对象","D":"生产工具"}',
 'A', '劳动者是生产力中最活跃的因素，生产工具是生产力发展水平的客观尺度。', 2),
(111, 1, '经济基础是指（  ）',
 '{"A":"占统治地位的生产关系的总和","B":"各种生产关系的总和","C":"生产力","D":"生产方式"}',
 'A', '经济基础是指由社会一定发展阶段的生产力所决定的生产关系的总和，即占统治地位的生产关系各方面的总和。', 3),
(111, 2, '矛盾的基本属性包括（  ）',
 '{"A":"普遍性","B":"特殊性","C":"同一性","D":"斗争性"}',
 'CD', '矛盾的两种基本属性是同一性和斗争性；普遍性和特殊性是矛盾的特性而非基本属性。', 3),
(111, 2, '下列属于唯物辩证法总特征的有（  ）',
 '{"A":"联系的观点","B":"发展的观点","C":"矛盾的观点","D":"否定的观点"}',
 'AB', '唯物辩证法的总特征是联系的观点和发展的观点。', 3),
(111, 2, '社会意识相对独立性的表现有（  ）',
 '{"A":"与社会存在发展的不完全同步性","B":"历史继承性","C":"各种形式之间相互影响","D":"对社会存在的能动反作用"}',
 'ABCD', '社会意识相对独立性表现在：与经济发展的不平衡性、历史继承性、各种形式相互影响以及对存在具有能动反作用。', 4),
(111, 2, '人民群众是历史的创造者，其表现有（  ）',
 '{"A":"是社会物质财富的创造者","B":"是社会精神财富的创造者","C":"是社会变革的决定力量","D":"是历史发展的唯一动力"}',
 'ABC', '人民群众是历史创造者表现在物质财富、精神财富的创造和社会变革的决定力量；"唯一动力"表述错误。', 3);

-- 题目：考研英语一（category_id=112，11 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(112, 1, 'The professor''s lecture was so ______ that many students fell asleep.',
 '{"A":"tedious","B":"fascinating","C":"concise","D":"vivid"}',
 'A', 'tedious 枯燥乏味的，符合"学生睡着"的语境；fascinating 迷人的，concise 简明的，vivid 生动的，均不合逻辑。', 2),
(112, 1, 'He ______ the proposal without a second thought.',
 '{"A":"turned down","B":"turned up","C":"turned on","D":"turned in"}',
 'A', 'turn down 意为"拒绝"；turn up 出现，turn on 打开，turn in 上交。', 2),
(112, 1, 'The government has taken measures to ______ inflation.',
 '{"A":"curb","B":"cure","C":"curse","D":"carve"}',
 'A', 'curb 意为"抑制、控制"，与 inflation（通货膨胀）搭配恰当。', 3),
(112, 1, 'It is essential that every student ______ the safety rules.',
 '{"A":"observes","B":"observe","C":"observed","D":"will observe"}',
 'B', 'It is essential that 引导的主语从句用虚拟语气，谓语用 (should) + 动词原形。', 3),
(112, 1, 'Only when the war was over ______ return to his hometown.',
 '{"A":"he could","B":"could he","C":"he can","D":"can he"}',
 'B', 'only 引导状语置于句首时主句部分倒装；且从句为过去时，主句用过去式 could。', 3),
(112, 1, 'The new policy will come into ______ next month.',
 '{"A":"effect","B":"affect","C":"effort","D":"affair"}',
 'A', 'come into effect 意为"生效、开始实施"；affect 是动词，effort 努力，affair 事务。', 2),
(112, 1, 'She is very ______ to criticism.',
 '{"A":"sensible","B":"sensitive","C":"senseless","D":"sensational"}',
 'B', 'be sensitive to 意为"对……敏感"；sensible 明智的，senseless 无知觉的，sensational 轰动的。', 2),
(112, 1, 'Hardly ______ the office when it began to rain.',
 '{"A":"I had left","B":"had I left","C":"I left","D":"did I leave"}',
 'B', 'hardly 置于句首时用部分倒装，且固定搭配 hardly...when... 主句用过去完成时。', 3),
(112, 1, 'The reason ______ he was late was that he missed the bus.',
 '{"A":"why","B":"that","C":"which","D":"because"}',
 'A', '先行词为 reason，从句中作原因状语，用关系副词 why 引导定语从句。', 2),
(112, 2, '下列属于英语从句引导词的有（  ）',
 '{"A":"that","B":"whether","C":"because","D":"although"}',
 'ABCD', 'that/whether 引导名词性从句，because/although 引导状语从句，四者均为从句引导词。', 2),
(112, 2, '以下属于形容词比较级/最高级不规则变化的有（  ）',
 '{"A":"good - better - best","B":"bad - worse - worst","C":"far - farther - farthest","D":"big - bigger - biggest"}',
 'ABC', 'big-bigger-biggest 是规则变化（双写辅音加 er/est）；A、B、C 均为不规则变化。', 3);

-- 题目：考研英语二（category_id=113，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(113, 1, 'The manager asked the staff to ______ the report before Friday.',
 '{"A":"submit","B":"summit","C":"subtract","D":"subscribe"}',
 'A', 'submit 意为"提交、呈交"，与 report 搭配；summit 峰会，subtract 减去，subscribe 订阅。', 2),
(113, 1, 'The new evidence ______ his innocence.',
 '{"A":"confirmed","B":"conformed","C":"confined","D":"conflicted"}',
 'A', 'confirm 证实、确认；conform 符合（conform to），confine 限制，conflict 冲突。', 2),
(113, 1, 'It was not until midnight ______ he finished the work.',
 '{"A":"that","B":"when","C":"which","D":"then"}',
 'A', '这是强调句型 It was not until ... that ...，被强调部分是时间状语，只能用 that。', 3),
(113, 1, 'The company is trying to ______ the costs of production.',
 '{"A":"reduce","B":"deduce","C":"induce","D":"produce"}',
 'A', 'reduce 降低、减少，与 costs 搭配；deduce 推断，induce 引起，produce 生产。', 2),
(113, 1, 'He would rather ______ at home than go out on such a cold day.',
 '{"A":"stay","B":"stays","C":"stayed","D":"staying"}',
 'A', 'would rather do sth than do sth 结构中，rather 与 than 后均接动词原形。', 2),
(113, 1, 'The manager insisted that the meeting ______ postponed.',
 '{"A":"be","B":"was","C":"is","D":"will be"}',
 'A', 'insist 表示"坚持要求"时，宾语从句用虚拟语气 (should) + 动词原形；被动语态即 (should) be done。', 3),
(113, 2, '下列动词中，含"扩大、增加"之义的有（  ）',
 '{"A":"expand","B":"enlarge","C":"extend","D":"contract"}',
 'ABC', 'expand 扩大、enlarge 增大、extend 延伸，均有"扩大"义；contract 意为"收缩"。', 3),
(113, 2, '下列句子中使用虚拟语气的有（  ）',
 '{"A":"I wish I were a bird.","B":"If I had time, I would go with you.","C":"He suggested that we should leave early.","D":"She is reading a book."}',
 'ABC', 'wish 后从句、if 虚拟条件句、suggest 后 (should) do 均为虚拟语气；D 为一般现在进行时的陈述句。', 3);

-- 题目：考研数学一（category_id=114，11 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(114, 1, '函数 f(x)=x³-3x 的极大值是（  ）',
 '{"A":"2","B":"-2","C":"0","D":"1"}',
 'A', 'f''(x)=3x²-3=0 得 x=±1；f''(x)=6x，f''(-1)=-6<0 故 x=-1 处取极大值 f(-1)=2。', 3),
(114, 1, '定积分 ∫₀¹ x² dx = （  ）',
 '{"A":"1/2","B":"1/3","C":"1/4","D":"1"}',
 'B', '∫x²dx = x³/3，代入 0 到 1 得 1/3。', 2),
(114, 1, '极限 lim(x→0) sin x / x = （  ）',
 '{"A":"0","B":"1","C":"∞","D":"不存在"}',
 'B', '这是重要极限之一，lim(x→0) sin x / x = 1。', 1),
(114, 1, '矩阵 A 为 3 阶方阵，|A|=2，则 |2A| = （  ）',
 '{"A":"4","B":"8","C":"16","D":"6"}',
 'C', '|kA| = kⁿ|A|，n=3，故 |2A| = 2³×2 = 16。', 3),
(114, 1, '设 A = [[1,2],[3,4]]，则行列式 |A| = （  ）',
 '{"A":"-2","B":"2","C":"-1","D":"1"}',
 'A', '|A| = 1×4 - 2×3 = 4 - 6 = -2。', 2),
(114, 1, '级数 Σ(1/n²)（n 从 1 到 +∞）（  ）',
 '{"A":"发散","B":"收敛","C":"条件收敛","D":"无法判断"}',
 'B', '这是 p 级数，p=2>1，故收敛（绝对收敛）。', 2),
(114, 1, 'd/dx (ln x) = （  ）',
 '{"A":"1/x","B":"x","C":"ln x","D":"1/(x ln x)"}',
 'A', '基本导数公式 (ln x)'' = 1/x（x>0）。', 1),
(114, 1, '设 f(x)=e^(2x)，则 f''(x) = （  ）',
 '{"A":"e^(2x)","B":"2e^(2x)","C":"2e^x","D":"e^x"}',
 'B', 'f''(x) = 2e^(2x)（复合函数求导，外层指数不变、内层乘 2）。', 2),
(114, 2, '下列函数在其定义域内连续的有（  ）',
 '{"A":"多项式函数","B":"指数函数","C":"对数函数","D":"sin x"}',
 'ABCD', '初等函数在其定义域内连续；多项式、e 为底的指数、对数（x>0）、sin x 均在其定义域内连续。', 2),
(114, 2, '下列各组向量中线性相关的有（  ）',
 '{"A":"(1,0),(0,1)","B":"(1,2),(2,4)","C":"(1,0,0),(0,1,0),(0,0,1)","D":"(1,2),(3,6)"}',
 'BD', 'B 中 (2,4)=2×(1,2)、D 中 (3,6)=3×(1,2)，成比例故线性相关；A、C 对应分量不成比例/为单位向量组，线性无关。', 3),
(114, 2, '下列函数中属于偶函数的有（  ）',
 '{"A":"x²","B":"cos x","C":"x³","D":"|x|"}',
 'ABD', '满足 f(-x)=f(x) 的是偶函数：x²、cos x、|x| 均为偶函数；x³ 是奇函数。', 2);

-- 题目：考研数学二（category_id=115，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(115, 1, '极限 lim(x→0) (1-cos x)/x² = （  ）',
 '{"A":"0","B":"1/2","C":"1","D":"2"}',
 'B', '当 x→0 时 1-cos x ~ x²/2，故极限为 1/2。', 2),
(115, 1, '曲线 y = x³ - 3x² + 2 的拐点是（  ）',
 '{"A":"x=0","B":"x=1","C":"x=2","D":"x=3"}',
 'B', 'y''''=6x-6，令 y''''=0 得 x=1，且两侧凹凸性相反，故 (1,0) 为拐点。', 3),
(115, 1, '不定积分 ∫ x·e^x dx = （  ）',
 '{"A":"x·e^x - e^x + C","B":"x·e^x + e^x + C","C":"e^x + C","D":"x²e^x/2 + C"}',
 'A', '分部积分：∫x e^x dx = x e^x - ∫e^x dx = x e^x - e^x + C。', 3),
(115, 1, '微分方程 y'' = 2x 的通解为（  ）',
 '{"A":"y = x² + C","B":"y = 2x + C","C":"y = 2x² + C","D":"y = x²/2 + C"}',
 'A', '两边对 x 积分得 y = x² + C。', 2),
(115, 1, '设 f(x) = x·ln x，则 f''(x) = （  ）',
 '{"A":"ln x","B":"ln x + 1","C":"1/x","D":"x + ln x"}',
 'B', '乘积求导 (uv)'' = u''v + uv''，得 ln x + x·(1/x) = ln x + 1。', 2),
(115, 1, '定积分 ∫₀² (2x + 1) dx = （  ）',
 '{"A":"4","B":"5","C":"6","D":"8"}',
 'C', '原函数为 x² + x，代入上下限得 (4+2) - 0 = 6。', 2),
(115, 2, '下列函数在 x=0 处可导的有（  ）',
 '{"A":"x²","B":"sin x","C":"|x|","D":"x³"}',
 'ABD', '|x| 在 x=0 处左右导数分别为 -1 和 1，不相等故不可导；其余三个在 x=0 处均可导。', 3),
(115, 2, '关于函数单调性与极值，下列说法正确的有（  ）',
 '{"A":"若 f''(x) > 0 在区间 I 上恒成立，则 f 在 I 上单调递增","B":"若 f 在 I 上单调递增，则必有 f''(x) > 0","C":"单调递增函数的导数在某些点可以等于 0","D":"可导函数的极值点必为驻点"}',
 'ACD', 'B 错误——单调递增只要求 f''(x) ≥ 0，例如 y=x³ 在 x=0 处导数为 0 仍单调递增；A、C、D 均正确。', 4);

-- 题目：计算机408（category_id=121，11 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(121, 1, '在数据结构中，具有"先进先出"特性的是（  ）',
 '{"A":"栈","B":"队列","C":"树","D":"图"}',
 'B', '队列是先进先出（FIFO）的线性表；栈是后进先出（LIFO）。', 1),
(121, 1, '平均时间复杂度为 O(n log n) 的排序算法是（  ）',
 '{"A":"冒泡排序","B":"插入排序","C":"快速排序","D":"选择排序"}',
 'C', '快速排序平均时间复杂度为 O(n log n)；冒泡、插入、选择排序平均均为 O(n²)。', 2),
(121, 1, 'TCP 是面向（  ）的传输层协议。',
 '{"A":"连接","B":"无连接","C":"广播","D":"组播"}',
 'A', 'TCP 是面向连接的可靠传输协议；UDP 是无连接的。', 1),
(121, 1, '进程与线程的主要区别在于（  ）',
 '{"A":"线程是资源分配单位","B":"进程是处理机调度单位","C":"线程是调度单位，进程是资源分配单位","D":"二者没有区别"}',
 'C', '进程是资源分配的基本单位，线程是处理机调度的基本单位，线程共享所属进程的资源。', 2),
(121, 1, '在二叉树的遍历中，先访问根节点的遍历方式是（  ）',
 '{"A":"前序遍历","B":"中序遍历","C":"后序遍历","D":"层次遍历"}',
 'A', '前序遍历（先序遍历）的顺序为：根 → 左 → 右，最先访问根节点。', 2),
(121, 1, 'Cache 的主要作用是（  ）',
 '{"A":"扩大内存容量","B":"缓解 CPU 与主存速度不匹配","C":"提高主存容量","D":"存储操作系统"}',
 'B', 'Cache 利用程序访问的局部性原理，缓解 CPU 与主存之间的速度差异。', 2),
(121, 1, 'IPv4 地址的长度为（  ）位。',
 '{"A":"16","B":"32","C":"64","D":"128"}',
 'B', 'IPv4 地址为 32 位；IPv6 地址为 128 位。', 1),
(121, 1, '关系数据库中，用于唯一标识一个元组的是（  ）',
 '{"A":"主键","B":"外键","C":"索引","D":"视图"}',
 'A', '主键（Primary Key）唯一标识表中的每一行（元组）；外键用于建立表间关联。', 1),
(121, 2, '下列属于 OSI 参考模型层次的有（  ）',
 '{"A":"物理层","B":"网络层","C":"传输层","D":"会话层"}',
 'ABCD', 'OSI 七层：物理层、数据链路层、网络层、传输层、会话层、表示层、应用层，四项均属于。', 2),
(121, 2, '下列属于进程间通信方式的有（  ）',
 '{"A":"管道","B":"消息队列","C":"共享内存","D":"信号量"}',
 'ABCD', '常见 IPC 方式包括管道、消息队列、共享内存、信号量、套接字等。', 3),
(121, 2, '下列属于黑盒测试方法的有（  ）',
 '{"A":"等价类划分","B":"边界值分析","C":"因果图法","D":"语句覆盖"}',
 'ABC', '等价类划分、边界值分析、因果图法均属黑盒测试；语句覆盖属于白盒测试。', 3);

-- 题目：计算机四级（category_id=130，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(130, 1, '现代操作系统中，处理机调度的基本单位是（  ）',
 '{"A":"进程","B":"线程","C":"程序","D":"文件"}',
 'B', '引入线程后，线程成为处理机调度的基本单位，进程则是资源分配的基本单位。', 2),
(130, 1, '关系模式满足第二范式（2NF）是指（  ）',
 '{"A":"不存在非主属性对码的部分函数依赖","B":"不存在非主属性对码的传递函数依赖","C":"所有属性都是原子的","D":"每个属性都不重复"}',
 'A', '2NF 要求消除非主属性对码的部分函数依赖；消除传递函数依赖属于 3NF，属性原子性属于 1NF。', 3),
(130, 1, '在 TCP/IP 参考模型中，IP 协议工作在（  ）',
 '{"A":"网络接口层","B":"网际层","C":"传输层","D":"应用层"}',
 'B', 'IP 是网际层（网络层）的核心协议，负责寻址与路由。', 2),
(130, 1, '分页存储管理中，页表的主要作用是（  ）',
 '{"A":"记录逻辑地址与物理地址的映射","B":"记录进程优先级","C":"记录文件目录","D":"记录设备状态"}',
 'A', '页表用于保存页号到物理块号的映射，实现逻辑地址到物理地址的转换。', 2),
(130, 1, '软件维护中，工作量占比通常最大的一类是（  ）',
 '{"A":"纠错性维护","B":"适应性维护","C":"完善性维护","D":"预防性维护"}',
 'C', '完善性维护（增加或改进功能、提升性能）通常占全部维护工作量的一半以上。', 2),
(130, 1, '与 SRAM 相比，DRAM 的主要特点是（  ）',
 '{"A":"速度更快","B":"需要定期刷新","C":"不需要刷新","D":"单位成本更高"}',
 'B', 'DRAM 用电容存储信息，电荷会泄漏，必须定期刷新；SRAM 用触发器存储，无需刷新但成本高。', 2),
(130, 2, '操作系统的基本功能包括（  ）',
 '{"A":"处理机管理","B":"存储器管理","C":"设备管理","D":"文件管理"}',
 'ABCD', '现代操作系统的基本功能包括处理机管理、存储器管理、设备管理、文件管理，并提供用户接口。', 2),
(130, 2, '下列关于数据库事务 ACID 特性的描述，正确的有（  ）',
 '{"A":"原子性指事务是不可分割的工作单位","B":"一致性指事务执行前后数据库都处于一致状态","C":"隔离性指并发事务之间互不干扰","D":"持久性指事务提交后对数据库的改变是永久的"}',
 'ABCD', '原子性（Atomicity）、一致性（Consistency）、隔离性（Isolation）、持久性（Durability）是事务的四个基本特性，四项描述均正确。', 3);

-- 题目：计算机二级（category_id=201，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(201, 1, '在 C 语言中，下列关于指针的说法正确的是（  ）',
 '{"A":"指针变量存放的是地址","B":"指针不能参与算术运算","C":"指针变量定义时必须初始化","D":"指针不能指向数组元素"}',
 'A', '指针变量存放的是内存地址；指针可参与有限算术运算，定义时可以不初始化，也可指向数组元素。', 2),
(201, 1, '在 C 语言中，数组名作为函数实参传递时，实际传递的是（  ）',
 '{"A":"数组的全部元素","B":"数组首元素的地址","C":"数组的长度","D":"数组的副本"}',
 'B', '数组名作实参时退化为指向首元素的指针，因此函数内对数组元素的修改会影响原数组。', 3),
(201, 1, '冒泡排序在最坏情况下的时间复杂度为（  ）',
 '{"A":"O(n)","B":"O(n log n)","C":"O(n²)","D":"O(log n)"}',
 'C', '冒泡排序最坏情况下需要约 n(n-1)/2 次比较，时间复杂度为 O(n²)。', 2),
(201, 1, '在关系数据库中，下列不属于数据完整性约束的是（  ）',
 '{"A":"主键约束","B":"外键约束","C":"唯一约束","D":"排序约束"}',
 'D', '常见完整性约束包括主键、外键、唯一、非空、检查等；"排序约束"不是数据库约束类型。', 2),
(201, 1, '在 Python 中，表达式 len("hello") 的返回值是（  ）',
 '{"A":"4","B":"5","C":"6","D":"报错"}',
 'B', 'len() 返回字符串的字符个数，"hello" 含 5 个字符。', 1),
(201, 1, '十进制数 13 转换为二进制数是（  ）',
 '{"A":"1011","B":"1101","C":"1110","D":"1001"}',
 'B', '13 = 8 + 4 + 1 = 2³ + 2² + 2⁰，故二进制为 1101。', 2),
(201, 2, '下列属于 C 语言基本数据类型的有（  ）',
 '{"A":"int","B":"float","C":"char","D":"struct"}',
 'ABC', 'int、float、char 是 C 语言的基本数据类型；struct 是构造类型（用户自定义类型）。', 2),
(201, 2, '下列属于 Python 内置数据类型的有（  ）',
 '{"A":"list","B":"dict","C":"tuple","D":"array"}',
 'ABC', 'list、dict、tuple 均为 Python 内置数据类型；array 需先 import array 模块，不是内置类型。', 3);

-- 题目：计算机三级（category_id=202，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(202, 1, '在 OSI 参考模型中，负责路由选择的是（  ）',
 '{"A":"数据链路层","B":"网络层","C":"传输层","D":"会话层"}',
 'B', '网络层负责逻辑寻址与路由选择，决定分组从源到目的地的路径。', 2),
(202, 1, '某 C 类网络的子网掩码为 255.255.255.192，可划分的子网数为（  ）',
 '{"A":"2","B":"4","C":"6","D":"8"}',
 'B', '掩码末字节 192 = 11000000，借用 2 位主机位作子网号，可划分 2² = 4 个子网。', 3),
(202, 1, '用于在 IP 网络中报告差错与控制报文的协议是（  ）',
 '{"A":"ICMP","B":"ARP","C":"DHCP","D":"SNMP"}',
 'A', 'ICMP（网际控制报文协议）用于传递差错报告和控制信息，ping 与 traceroute 均基于它。', 2),
(202, 1, '传统以太网采用的介质访问控制方法是（  ）',
 '{"A":"CSMA/CD","B":"CSMA/CA","C":"令牌环","D":"TDMA"}',
 'A', '传统以太网使用 CSMA/CD（载波监听多路访问/冲突检测）；无线局域网使用 CSMA/CA。', 2),
(202, 1, 'TCP 建立连接时采用的是（  ）',
 '{"A":"二次握手","B":"三次握手","C":"四次握手","D":"五次握手"}',
 'B', 'TCP 通过 SYN、SYN+ACK、ACK 三次握手建立连接，以确保双方收发能力正常。', 1),
(202, 1, '下列属于对称加密算法的是（  ）',
 '{"A":"RSA","B":"DES","C":"ECC","D":"DSA"}',
 'B', 'DES 属于对称加密算法（加解密使用同一密钥）；RSA、ECC、DSA 均为非对称加密算法。', 3),
(202, 2, '下列可用于网络互联的设备有（  ）',
 '{"A":"集线器","B":"交换机","C":"路由器","D":"网关"}',
 'ABCD', '集线器（物理层）、交换机（数据链路层）、路由器（网络层）、网关（高层）均可参与网络连接与互联。', 3),
(202, 2, 'TCP 协议的特性包括（  ）',
 '{"A":"面向连接","B":"可靠传输","C":"提供流量控制","D":"不保证数据顺序"}',
 'ABC', 'TCP 面向连接、可靠传输、提供流量控制与拥塞控制，并保证数据按序到达，故 D 错误。', 3);

-- 题目：初级会计（category_id=301，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(301, 1, '会计的基本职能是（  ）',
 '{"A":"核算和监督","B":"预测和决策","C":"计划和控制","D":"分析和考核"}',
 'A', '会计的基本职能是核算（反映）和监督（控制），其他职能均由此派生。', 1),
(301, 1, '会计等式"资产 = 负债 + 所有者权益"反映的是（  ）',
 '{"A":"资金运动的动态表现","B":"某一时点的财务状况","C":"某一期间的经营成果","D":"现金流量情况"}',
 'B', '该等式是静态等式，反映企业在某一特定时点的财务状况。', 2),
(301, 1, '在借贷记账法下，资产类账户的增加额登记在（  ）',
 '{"A":"借方","B":"贷方","C":"借贷任一方","D":"不登记"}',
 'A', '借贷记账法下，资产类、成本类、费用类账户借方登记增加，贷方登记减少。', 2),
(301, 1, '下列属于原始凭证的是（  ）',
 '{"A":"增值税专用发票","B":"记账凭证","C":"总分类账","D":"资产负债表"}',
 'A', '原始凭证是经济业务发生或完成时取得的最初书面证明，如发票、收据；记账凭证属于记账的依据。', 2),
(301, 1, '在资产负债表中，"应收账款"项目反映的是企业的（  ）',
 '{"A":"债权","B":"债务","C":"所有者权益","D":"收入"}',
 'A', '应收账款是企业因销售商品等应向购货方收取的款项，属于流动资产中的债权。', 2),
(301, 1, '采用年限平均法计提固定资产折旧，其年折旧额的计算公式是（  ）',
 '{"A":"(原值 - 预计净残值) ÷ 预计使用年限","B":"原值 ÷ 预计使用年限","C":"原值 × 折旧率","D":"原值 - 累计折旧"}',
 'A', '年限平均法（直线法）年折旧额 = (固定资产原值 - 预计净残值) ÷ 预计使用年限。', 3),
(301, 2, '下列属于资产类科目的有（  ）',
 '{"A":"库存现金","B":"应收账款","C":"固定资产","D":"应付账款"}',
 'ABC', '库存现金、应收账款、固定资产均为资产类科目；应付账款属于负债类科目。', 2),
(301, 2, '下列属于会计六大要素的有（  ）',
 '{"A":"资产","B":"负债","C":"收入","D":"现金流量"}',
 'ABC', '会计六要素为资产、负债、所有者权益、收入、费用、利润；现金流量不属于会计要素。', 2);

-- 题目：银行从业（category_id=302，8 题）
INSERT INTO `question` (`category_id`, `question_type`, `content`, `options`, `answer`, `analysis`, `difficulty`) VALUES
(302, 1, '商业银行最主要的资金来源是（  ）',
 '{"A":"各项存款","B":"同业拆借","C":"发行债券","D":"向中央银行借款"}',
 'A', '存款是商业银行最传统、最主要的负债业务，构成其资金的主要来源。', 1),
(302, 1, '中央银行提高法定存款准备金率，通常会导致（  ）',
 '{"A":"货币供应量减少","B":"货币供应量增加","C":"市场利率下降","D":"信贷规模扩大"}',
 'A', '提高准备金率会减少商业银行可用于放贷的资金，通过货币乘数效应使货币供应量减少。', 3),
(302, 1, '商业银行最传统的中间业务是（  ）',
 '{"A":"结算业务","B":"贷款业务","C":"存款业务","D":"证券投资业务"}',
 'A', '结算业务是商业银行最传统、最基本的中间业务，银行以中介身份为客户办理资金收付。', 2),
(302, 1, '下列不属于货币政策工具的是（  ）',
 '{"A":"法定存款准备金率","B":"再贴现率","C":"公开市场业务","D":"个人所得税率"}',
 'D', '个人所得税率属于财政政策工具；存款准备金率、再贴现率、公开市场业务是货币政策三大工具。', 2),
(302, 1, '信用最基本的特征是（  ）',
 '{"A":"偿还和付息","B":"无偿性","C":"强制性","D":"固定性"}',
 'A', '信用是以偿还和付息为条件的价值单方面让渡，偿还性和付息性是其最基本的特征。', 2),
(302, 1, '在金融市场中，期限在一年以内的金融工具交易市场称为（  ）',
 '{"A":"货币市场","B":"资本市场","C":"外汇市场","D":"金融衍生品市场"}',
 'A', '货币市场是短期（一年以内）资金融通市场，如同业拆借、票据、国库券市场；一年以上为资本市场。', 2),
(302, 2, '下列属于货币政策工具的有（  ）',
 '{"A":"法定存款准备金率","B":"再贴现政策","C":"公开市场业务","D":"利率政策"}',
 'ABCD', '三大传统工具为存款准备金率、再贴现、公开市场业务；利率政策也是常用的货币政策手段。', 3),
(302, 2, '商业银行的主要业务包括（  ）',
 '{"A":"负债业务","B":"资产业务","C":"中间业务","D":"货币发行"}',
 'ABC', '商业银行业务分为负债、资产、中间三大类；货币发行是中央银行的独占职能。', 3);

-- ============================================================
-- 商城（积分兑换电子商品；仅一级分类）
-- ============================================================

-- ------------------------------------------------------------
-- 6. mall_category 商城分类表（仅一级，不做多级）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `mall_category`;
CREATE TABLE `mall_category` (
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
-- 7. mall_product 商品表（MVP 均为虚拟电子商品）
--    price 为兑换所需积分；库存对虚拟商品同样生效，防超兑
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `mall_product`;
CREATE TABLE `mall_product` (
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
-- 8. exchange_record 兑换记录表
--    product_name / points_cost 为兑换时刻快照
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `exchange_record`;
CREATE TABLE `exchange_record` (
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
-- 9. points_log 积分流水表
--    source: 1答题 2兑换；change_amount 正为获取、负为消耗
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `points_log`;
CREATE TABLE `points_log` (
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

-- 商城分类（4 个一级，id 固定）
INSERT INTO `mall_category` (`id`, `name`, `sort`, `status`) VALUES
(1, '会员特权', 1, 1),
(2, '备考资料', 2, 1),
(3, '视频课程', 3, 1),
(4, '学习工具', 4, 1);

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
(10, 4, '学习打卡皮肤',           '个性化打卡背景',                 30,  999, 1, 1, 2);

-- ============================================================
-- 初始化完成
-- 分类: 16 个（3 个一级 / 5 个二级分组或直挂 / 8 个叶子）
-- 题目: 100 道（政治11 英语一11 英语二8 数学一11 数学二8 408:11 四级8 二级8 三级8 初级会计8 银行从业8）
-- 商城: 4 个分类 / 10 个商品；user.points 初始 0
-- 用户、答题记录、错题本表初始为空，由小程序运行后产生。
-- ============================================================

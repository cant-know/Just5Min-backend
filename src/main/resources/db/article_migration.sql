-- ============================================================
-- 考研刷题小程序 - 商城内容改造迁移（电子商品 → 学习资料文章）
-- 用途: 在已有数据的环境上安全升级（不 DROP 用户数据，可重复执行）
-- 内容:
--   1. 新建 article 文章表（学习资料专栏，含正文/标签/阅读量）
--   2. 改造 mall_category / mall_product：加 article_id 关联列
--   3. 重命名分类为资料分类；种子资料商品（price=0，兑换即解锁阅读）
-- 执行: mysql -uroot -p < mall_article_migration.sql
-- 注意: 全新环境可直接跑 init.sql；已有数据环境严禁跑 init.sql
-- ============================================================

USE `just5min`;

-- ------------------------------------------------------------
-- 1. article 学习资料文章表
--    content 用 MEDIUMTEXT（单篇长文可到 MB 级）；summary 供列表页快速展示
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `category_id` BIGINT UNSIGNED NOT NULL                COMMENT '所属资料分类ID（对应 mall_category.id）',
    `title`       VARCHAR(128)    NOT NULL                COMMENT '标题',
    `summary`     VARCHAR(512)    DEFAULT NULL            COMMENT '摘要（列表页展示）',
    `content`     MEDIUMTEXT      DEFAULT NULL            COMMENT '正文（Markdown/纯文本）',
    `tags`        VARCHAR(255)    DEFAULT NULL            COMMENT '标签，逗号分隔',
    `cover_url`   VARCHAR(512)    DEFAULT NULL            COMMENT '封面图URL（预留）',
    `read_count`  INT             NOT NULL DEFAULT 0      COMMENT '阅读量',
    `status`      TINYINT         NOT NULL DEFAULT 1      COMMENT '状态：1发布 0下架',
    `sort`        INT             NOT NULL DEFAULT 0      COMMENT '排序值，越小越靠前',
    `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_cat_status` (`category_id`, `status`, `sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '学习资料文章表';

-- ------------------------------------------------------------
-- 2. mall_product 加 article_id（虚拟资料商品 → 关联文章；实物/其它为 NULL）
--    注意：不加外键约束，避免删除商品时受文章牵连
-- ------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'mall_product' AND COLUMN_NAME = 'article_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE `mall_product` ADD COLUMN `article_id` BIGINT UNSIGNED DEFAULT NULL COMMENT ''关联文章ID（虚拟资料商品用；实物为空）'' AFTER `cover_url`',
    'SELECT 1 AS article_id_column_already_exists');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3. mall_category 改名为「资料分类」语义（删旧分类会牵连已有兑换记录，故只改名不改 id）
-- ------------------------------------------------------------
INSERT INTO `mall_category` (`id`, `name`, `sort`, `status`) VALUES
(1, '政治资料', 1, 1),
(2, '英语资料', 2, 1),
(3, '数学资料', 3, 1),
(4, '专业课资料', 4, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `sort` = VALUES(`sort`), `status` = VALUES(`status`);

-- ------------------------------------------------------------
-- 4. 资料文章种子（每篇对应 1 个虚拟资料商品）
-- ------------------------------------------------------------
INSERT INTO `article` (`id`, `category_id`, `title`, `summary`, `content`, `tags`, `sort`, `status`) VALUES
(1, 1, '考研政治冲刺必背 100 题（上）', '马原 + 毛中特高频考点，考前必过一遍的选择题金句与易混辨析。',
'# 考研政治冲刺必背 100 题（上）

## 一、马原 · 唯物论

**1. 物质的唯一特性是客观实在性。**
易混：物质的根本属性是运动；物质的存在方式是时间和空间。

**2. 意识是客观世界的主观映象。**
辨析：意识的内容是客观的，形式是主观的。

**3. 实践是自然存在与社会存在区分和统一的基础。**

## 二、马原 · 辩证法

**4. 对立统一规律是唯物辩证法的实质和核心。**

**5. 矛盾的普遍性与特殊性的关系是共性与个性、一般与个别的关系，不是整体与部分。**

**6. 量变是质变的必要准备，质变是量变的必然结果，二者相互渗透。**

## 三、毛中特高频金句

**7. 中国式现代化是人口规模巨大的现代化。**
**8. 高质量发展是全面建设社会主义现代化国家的首要任务。**
**9. 全过程人民民主是社会主义民主政治的本质属性。**

## 四、易混辨析速记

| 易混对 | 区分要点 |
|---|---|
| 主要矛盾 / 矛盾的主要方面 | 前者用于「办事情」，后者用于「看问题」 |
| 生产力 / 生产关系 | 生产力决定生产关系，生产关系反作用于生产力 |
| 国体 / 政体 | 国体是人民民主专政，政体是人民代表大会制度 |

> 复习建议：每天背 10 条，第二天先默写前一天的，再背新的。',
'马原,毛中特,易混辨析', 1, 1),

(2, 2, '考研英语高频词汇手册（核心 500 词）', '按真题出现频次排序，含例句与熟词僻义。',
'# 考研英语高频词汇手册

## 使用说明
本手册按近 10 年真题词频排序，前 500 词覆盖约 72% 的阅读文本。
每个词给出：释义 + 真题例句 + 常见熟词僻义。

## 第一部分 · 高频动词

**1. address** v. 处理，应对（不只有「地址」）
> The report addresses the issue of rising costs.

**2. argue** v. 主张，认为（学术论文中极常见）
> Some scholars argue that the policy has failed.

**3. assume** v. 假定，认为；承担
**4. account for** 解释；占比
**5. yield** v. 产生；让步 n. 产量
**6. sustain** v. 维持；遭受
**7. warrant** v. 使…有必要 n. 授权令
**8. subject to** 使遭受；取决于

## 第二部分 · 高频形容词

**9. viable** 可行的
**10. vulnerable** 脆弱的，易受伤害的
**11. subtle** 微妙的
**12. plausible** 貌似合理的
**13. inherent** 固有的
**14. ambiguous** 模棱两可的

## 第三部分 · 熟词僻义（拉分点）

| 词 | 常见义 | 僻义 |
|---|---|---|
| company | 公司 | 陪伴 |
| plant | 植物 | 工厂；安放 |
| school | 学校 | 学派 |
| story | 故事 | 楼层 |

> 记忆法：把僻义和常见义编进同一句话，例如 "The company kept me company."',
'英语一,英语二,高频词汇', 2, 1),

(3, 3, '考研数学公式速查卡', '高数 / 线代 / 概率必背公式一页速览，考前 30 分钟救命版。',
'# 考研数学公式速查卡

## 一、高等数学

### 常用等价无穷小（x→0）
- sin x ~ x，tan x ~ x，arcsin x ~ x
- 1 − cos x ~ x²/2
- ln(1 + x) ~ x，eˣ − 1 ~ x
- (1 + x)^α − 1 ~ αx

### 泰勒展开
- eˣ = 1 + x + x²/2! + x³/3! + o(x³)
- ln(1 + x) = x − x²/2 + x³/3 + o(x³)
- sin x = x − x³/3! + o(x³)

### 常用积分
- ∫ dx/√(a² − x²) = arcsin(x/a) + C
- ∫ dx/(a² + x²) = (1/a)arctan(x/a) + C
- ∫ ln x dx = x ln x − x + C

## 二、线性代数

- |AB| = |A||B|
- r(AB) ≤ min(r(A), r(B))
- 可逆 ⇔ 行列式非零 ⇔ 特征值均非零
- 实对称矩阵必可正交对角化

## 三、概率论

- 全概率：P(B) = Σ P(Aᵢ)P(B|Aᵢ)
- 贝叶斯：P(Aᵢ|B) = P(Aᵢ)P(B|Aᵢ) / P(B)
- 方差：D(X) = E(X²) − [E(X)]²
- 正态可加性：X+Y ~ N(μ₁+μ₂, σ₁²+σ₂²)（独立）

> 考前技巧：等价无穷小和泰勒展开优先于洛必达，能省一半时间。',
'数学一,数学二,公式', 3, 1),

(4, 4, '408 数据结构核心考点梳理', '线性表 → 树 → 图 → 排序查找，含复杂度对照表。',
'# 408 数据结构核心考点梳理

## 一、复杂度对照表

| 算法 | 平均 | 最坏 | 稳定性 |
|---|---|---|---|
| 冒泡排序 | O(n²) | O(n²) | 稳定 |
| 快速排序 | O(n log n) | O(n²) | 不稳定 |
| 归并排序 | O(n log n) | O(n log n) | 稳定 |
| 堆排序 | O(n log n) | O(n log n) | 不稳定 |

## 二、线性表

- 顺序表：随机访问 O(1)，插入删除平均 O(n)
- 链表：插入删除 O(1)（已知前驱），查找 O(n)

## 三、树

**二叉树性质**
- 第 i 层最多 2^(i−1) 个结点
- 高度 h 的二叉树最多 2^h − 1 个结点
- n₀ = n₂ + 1（叶子数 = 度为 2 的结点数 + 1）

**哈夫曼树**：带权路径长度 WPL 最小，不存在度为 1 的结点。

## 四、图

- 邻接矩阵适合稠密图，邻接表适合稀疏图
- Dijkstra 不能处理负权边；Floyd 可以
- 拓扑排序用于 AOV 网；关键路径用于 AOE 网

## 五、查找

- 折半查找要求顺序存储且有序，时间复杂度 O(log n)
- B 树：多路平衡查找树；B+ 树：所有数据在叶子，叶子间有链表

> 高频陷阱：快排「最坏 O(n²)」出现在基本有序时；堆排建堆是 O(n)。',
'计算机408,数据结构,考点', 4, 1),

(5, 1, '近代史纲时间线速记（1840—1949）', '按时间轴串起重大事件，选择题时间匹配题必备。',
'# 近代史纲时间线速记

## 一、旧民主主义革命时期（1840—1919）

| 时间 | 事件 | 关键词 |
|---|---|---|
| 1840 | 鸦片战争 | 近代史开端 |
| 1842 | 《南京条约》 | 第一个不平等条约 |
| 1851 | 太平天国 | 农民阶级探索 |
| 1894 | 甲午战争 | 洋务运动破产 |
| 1898 | 戊戌变法 | 资产阶级维新 |
| 1911 | 辛亥革命 | 君主专制终结 |

## 二、新民主主义革命时期（1919—1949）

- **1919 五四运动**：新民主主义革命开端，无产阶级登上历史舞台
- **1921 中共一大**：中国革命有了坚强领导核心
- **1927 南昌起义**：武装反抗国民党第一枪
- **1935 遵义会议**：确立毛泽东在党中央的领导地位
- **1945 抗战胜利**：近代以来第一次完全胜利的民族解放战争
- **1949 新中国成立**：新民主主义革命基本胜利

> 记忆口诀：鸦片开局、甲午转折、辛亥推翻、五四开端、遵义转折、抗战全胜。',
'近代史,时间线', 5, 1),

(6, 2, '英语大小作文模板（含高分句式）', '小作文 7 类 + 大作文图画/图表，附可直接替换的句式骨架。',
'# 英语大小作文模板

## 一、小作文（应用文）7 类

1. **书信 · 建议信**：开头点明身份与目的 → 主体 2~3 条建议 → 结尾期望采纳
2. **书信 · 感谢信**：具体化感谢的事由，比空泛的 "Thank you" 更得分
3. **书信 · 道歉信**：道歉 + 说明原因 + 补救措施
4. **通知 Notice**：标题居中，落款在右下
5. **推荐信**：推荐理由要具体到「能力 + 事例」
6. **邀请信**：时间地点必须写全
7. **告示**：注意正式语气

## 二、大作文（英语一）图画作文三段式

**第一段 · 描述图画**
> As is vividly depicted in the picture, ...

**第二段 · 阐释含义**
> The drawing subtly conveys the message that ...
> Several factors can be attributed to this phenomenon.

**第三段 · 个人评论**
> From my perspective, it is imperative that we should ...
> Only in this way can we ...

## 三、万能高分句式

- 开头：It is universally acknowledged that ...
- 递进：What''s more / Moreover / In addition
- 转折：Nevertheless / On the contrary
- 结论：To sum up / In conclusion / All things considered

> 提分要点：模板句只占 1/3，剩下 2/3 必须替换成与题目相关的具体内容，否则会被判套作。',
'作文,模板,英语一', 6, 1),

(7, 3, '概率论核心结论与常见分布', '7 大分布的期望方差对照 + 常见题型模板。',
'# 概率论核心结论与常见分布

## 一、常见分布对照表

| 分布 | 记号 | 期望 | 方差 |
|---|---|---|---|
| 0-1 分布 | B(1, p) | p | p(1−p) |
| 二项分布 | B(n, p) | np | np(1−p) |
| 泊松分布 | P(λ) | λ | λ |
| 均匀分布 | U(a, b) | (a+b)/2 | (b−a)²/12 |
| 指数分布 | E(λ) | 1/λ | 1/λ² |
| 正态分布 | N(μ, σ²) | μ | σ² |
| 卡方分布 | χ²(n) | n | 2n |

## 二、必背结论

1. **指数分布无记忆性**：P(X > s+t | X > s) = P(X > t)
2. **正态分布标准化**：(X−μ)/σ ~ N(0, 1)
3. **泊松可加**：独立时 X+Y ~ P(λ₁+λ₂)
4. **切比雪夫不等式**：P(|X−E(X)| ≥ ε) ≤ D(X)/ε²
5. **独立 ⇒ 不相关**，反之不成立（正态分布下两者等价）

## 三、常见题型

- **求分布函数**：先定区间，再分段积分，注意端点的连续性
- **求期望**：优先用 E(X) = Σx·P 或 ∫x·f(x)dx；对称性可秒杀
- **多维**：先求边缘，再判独立（f(x,y) = f_X(x)·f_Y(y)）

> 易错点：分布函数的右连续性；密度的不唯一性不影响概率计算。',
'数学一,概率论', 7, 1),

(8, 4, '操作系统高频考点：进程与内存', '进程调度 / 死锁 / 页面置换算法，附经典计算题套路。',
'# 操作系统高频考点：进程与内存

## 一、进程与线程

- 进程是资源分配的基本单位，线程是调度的基本单位
- 线程共享：地址空间、打开文件、全局变量
- 线程私有：栈、寄存器、PC

## 二、调度算法

| 算法 | 特点 | 是否可抢占 |
|---|---|---|
| FCFS | 简单，对短作业不利 | 否 |
| SJF | 平均等待时间最短 | 否 |
| RR | 时间片轮转，公平 | 是 |
| 多级反馈队列 | 兼顾长短作业 | 是 |

## 三、死锁

**四个必要条件**：互斥、不可剥夺、请求并保持、循环等待。
**处理策略**：预防（破坏必要条件）、避免（银行家算法）、检测与解除。

> 计算套路：银行家算法先算 Need 矩阵 = Max − Allocation，再逐个试分配。

## 四、页面置换算法

- **FIFO**：可能产生 Belady 异常
- **LRU**：最近最少使用，性能好但开销大
- **CLOCK**：LRU 的近似实现
- **OPT**：理论最优，用于衡量其它算法

> 计算套路：给引用串 + 帧数，画表逐列填，缺页率 = 缺页次数 / 总访问次数。',
'计算机408,操作系统', 8, 1),

(9, 1, '马原哲学易错 50 点辨析', '逐条列出易混淆的哲学论断，专治选择题「差一个字」。',
'# 马原哲学易错 50 点辨析

## 一、唯物论部分

1. 物质的**唯一特性**是客观实在性；**根本属性**是运动。
2. 意识是客观世界的主观映象：内容客观，形式主观。
3. 世界上除了运动着的物质什么也没有。
4. 唯心主义的根源是**夸大**意识的作用，不是否认物质。
5. 实践是自然界与人类社会**分化与统一**的基础。

## 二、辩证法部分

6. 对立统一规律是辩证法的**实质和核心**。
7. 质量互变规律揭示了事物发展的**形式和状态**。
8. 否定之否定规律揭示了事物发展的**方向和道路**。
9. 矛盾的**普遍性**与**特殊性**是共性与个性的关系。
10. 矛盾的主要方面决定事物的**性质**；主要矛盾决定**工作重心**。

## 三、认识论部分

11. 感性认识有待于发展到理性认识；理性认识依赖于感性认识。
12. 实践是检验真理的**唯一**标准。
13. 真理是**具体**的，有条件的。
14. 真理与谬误在一定条件下相互转化。

## 四、唯物史观部分

15. 社会存在决定社会意识；社会意识具有**相对独立性**。
16. 生产力是社会发展的**最终**决定力量。
17. 人民群众是历史的创造者，但**不是**随心所欲地创造。

> 复习方法：把每条论断的「关键词」圈出来单独背，考场上先看关键词对不对。',
'马原,辨析,易错', 9, 1),

(10, 2, '考研英语阅读长难句拆解 20 例', '从真题中抽出的典型长句，教你怎么找主干、拆从句。',
'# 考研英语阅读长难句拆解 20 例

## 拆解三步骤

1. **找谓语** → 定主干（主谓宾）
2. **划连词** → 切从句（that / which / because / although...）
3. **剥修饰** → 去插入语、分词短语、同位语

## 例 1
> The idea **that** the journalist must be able to work well with others **is** an attractive one.

- 主干：The idea is an attractive one.
- 同位语从句：that the journalist must be able to work well with others（解释 idea）

## 例 2
> **Although** the rise of the internet has changed the media landscape dramatically, the basic principles **that** govern good journalism remain unchanged.

- 让步状语从句：Although ...
- 主干：the basic principles remain unchanged
- 定语从句：that govern good journalism 修饰 principles

## 例 3
> **What** is remarkable about this finding is **that** the effect was observed in every single age group studied.

- 主语从句：What is remarkable about this finding
- 表语从句：that the effect was observed ...

## 例 4
> Researchers, **most of whom** had previously focused on adults, were surprised to find the pattern in children as young as three.

- 非限定性定语从句：most of whom had previously focused on adults

## 练习方法
每天精拆 3 句：先自己划主干，再对照译文；坚持两周，阅读速度会明显提升。',
'英语,长难句,阅读', 10, 1)
ON DUPLICATE KEY UPDATE
    `category_id` = VALUES(`category_id`),
    `title`       = VALUES(`title`),
    `summary`     = VALUES(`summary`),
    `content`     = VALUES(`content`),
    `tags`        = VALUES(`tags`),
    `sort`        = VALUES(`sort`),
    `status`      = VALUES(`status`);

-- ------------------------------------------------------------
-- 5. 资料商品：price=0（答题攒的积分用于解锁），改名为资料标题
--    兑换后前端直接跳文章详情，不再扣减积分
-- ------------------------------------------------------------
INSERT INTO `mall_product`
(`id`, `category_id`, `name`, `description`, `price`, `stock`, `product_type`, `status`, `sort`, `article_id`) VALUES
(1,  1, '考研政治冲刺必背 100 题（上）',   '马原 + 毛中特高频考点精选',      0, 9999, 1, 1, 1, 1),
(2,  2, '考研英语高频词汇手册（核心 500 词）', '按真题词频排序，含熟词僻义',     0, 9999, 1, 1, 1, 2),
(3,  3, '考研数学公式速查卡',               '高数 / 线代 / 概率一页速览',     0, 9999, 1, 1, 1, 3),
(4,  4, '408 数据结构核心考点梳理',         '线性表 → 树 → 图 → 排序查找',    0, 9999, 1, 1, 1, 4),
(5,  1, '近代史纲时间线速记（1840—1949）',  '按时间轴串起重大事件',           0, 9999, 1, 1, 2, 5),
(6,  2, '英语大小作文模板（含高分句式）',     '小作文 7 类 + 大作文三段式',     0, 9999, 1, 1, 2, 6),
(7,  3, '概率论核心结论与常见分布',         '7 大分布期望方差 + 题型套路',    0, 9999, 1, 1, 2, 7),
(8,  4, '操作系统高频考点：进程与内存',      '调度 / 死锁 / 页面置换算法',     0, 9999, 1, 1, 2, 8),
(9,  1, '马原哲学易错 50 点辨析',           '专治选择题「差一个字」',         0, 9999, 1, 1, 3, 9),
(10, 2, '考研英语阅读长难句拆解 20 例',      '找主干 / 拆从句 / 剥修饰',       0, 9999, 1, 1, 3, 10)
ON DUPLICATE KEY UPDATE
    `category_id` = VALUES(`category_id`),
    `name`        = VALUES(`name`),
    `description` = VALUES(`description`),
    `price`       = VALUES(`price`),
    `stock`       = VALUES(`stock`),
    `status`      = VALUES(`status`),
    `sort`        = VALUES(`sort`),
    `article_id`  = VALUES(`article_id`);

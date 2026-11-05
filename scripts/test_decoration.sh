#!/usr/bin/env bash
# 商城装扮（虚拟商品）联调脚本（在 Just5Min-backend 目录下运行）
# 前置：后端已启动；库已跑 db/mall_decoration_migration.sql；用户 5/8 存在且未兑换过装扮 11~22
set -u
BASE=http://localhost:8080
NODE="D:/HBuilderX/plugins/node/node.exe"
OUT=target/dectest
mkdir -p $OUT

T5=$($NODE scripts/gen_test_token.js 5 | awk '{print $4}')
T8=$($NODE scripts/gen_test_token.js 8 | awk '{print $4}')

show() { $NODE -e "
const j=require('./$1');
console.log('code=' + j.code + ' msg=' + (j.message||''));
if (j.data) console.log('  ' + JSON.stringify(j.data).slice(0, 400));"; }

echo "=== 1. 游客装扮分类 bizType=2（应返回 3 个装扮分类） ==="
curl -s "$BASE/api/mall/categories?bizType=2" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 2. 游客分类默认 bizType=1（文章专栏视角，应只见资料分类、无装扮分类） ==="
curl -s "$BASE/api/mall/categories" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
console.log('code=' + j.code + ' names=' + (j.data||[]).map(c=>c.name).join(','));"

echo
echo "=== 3. 游客查资料分类 1 商品（旧资料商品应全部下架 → 空列表） ==="
curl -s "$BASE/api/mall/products?categoryId=1" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 4. 游客查头像框分类 5（应 4 个商品，含 decorationKind=1/decorationAsset） ==="
curl -s "$BASE/api/mall/products?categoryId=5" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
console.log('code=' + j.code + ' n=' + (j.data||[]).length);
(j.data||[]).forEach(p=>console.log('  ' + p.id + ' ' + p.name + ' price=' + p.price
  + ' kind=' + p.decorationKind + ' asset=' + p.decorationAsset));"

echo
echo "=== 5. 游客商品详情 11（owned/equipped 应为 null，不下发答案式隐私） ==="
curl -s "$BASE/api/mall/products/11" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 6. 游客商品详情 999（应 404 / 1011） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" "$BASE/api/mall/products/999"; show $OUT/o.json

echo
echo "=== 7. 游客 GET 我的装扮（应 401） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" "$BASE/api/user/decoration"; show $OUT/o.json

echo
echo "=== 8. 游客 PUT 佩戴（应 401） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/decoration" \
  -H "Content-Type: application/json" -d '{"kind":1,"productId":11}'; show $OUT/o.json

echo
echo "=== 9. 游客兑换（应 401） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X POST "$BASE/api/mall/exchange" \
  -H "Content-Type: application/json" -d '{"productId":11}'; show $OUT/o.json

echo
echo "=== 10. 用户5 初始装扮（三个槽位应全 null） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/decoration" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 11. 用户5 兑换免费头像框 11（cost=0、equipped=true、alreadyOwned=false） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"productId":11}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 12. 用户5 重复兑换 11（幂等：alreadyOwned=true、cost=0） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"productId":11}' -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 13. 用户5 装扮态（avatarFrameId 应为 11 且带 url） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/decoration" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
const d=j.data||{};
console.log('code=' + j.code
  + ' frame=' + d.avatarFrameId + '/' + d.avatarFrameUrl
  + ' bubble=' + d.chatBubbleId + '/' + d.chatBubbleSkin
  + ' pendant=' + d.pendantId + '/' + d.pendantUrl);"

echo
echo "=== 14. 用户5 兑换免费气泡 15（自动佩戴 chatBubbleSkin=lime） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"productId":15}' -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 15. 用户5 兑换免费挂件 19（自动佩戴 pendantId=19） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"productId":19}' -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 16. 用户5 佩戴未拥有的头像框 14（应 1014 未拥有） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/decoration" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"kind":1,"productId":14}'; show $OUT/o.json

echo
echo "=== 17. 用户5 类型不匹配：kind=1 配气泡 15（应 1015） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/decoration" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"kind":1,"productId":15}'; show $OUT/o.json

echo
echo "=== 18. 用户5 佩戴不存在的商品 999（应 1011） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/decoration" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"kind":1,"productId":999}'; show $OUT/o.json

echo
echo "=== 19. 用户5 kind 越界 0（应 400 参数校验） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/decoration" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"kind":0,"productId":11}'; show $OUT/o.json

echo
echo "=== 20. 用户5 兑换付费头像框 12（30 积分：应扣 30 并自动佩戴） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"productId":12}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 21. 用户5 摘下头像框（frame 变 null，气泡/挂件保留） ==="
curl -s -X PUT "$BASE/api/user/decoration" -H "Authorization: Bearer $T5" \
  -H "Content-Type: application/json" -d '{"kind":1,"productId":null}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/decoration" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
const d=j.data||{};
console.log('  frame=' + d.avatarFrameId + ' bubble=' + d.chatBubbleId + ' pendant=' + d.pendantId);"

echo
echo "=== 22. 用户8 视角商品 11 详情（owned=false，与用户5 互不影响） ==="
curl -s -H "Authorization: Bearer $T8" "$BASE/api/mall/products/11" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
console.log('code=' + j.code + ' owned=' + j.data.owned + ' equipped=' + j.data.equipped);"

echo
echo "=== 23. 用户8 积分余额（供 24 判断预期） ==="
curl -s -H "Authorization: Bearer $T8" "$BASE/api/user/stats" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
console.log('code=' + j.code + ' points=' + (j.data||{}).points);"

echo
echo "=== 24. 用户8 兑换 100 分气泡 18（积分不足应 1003，够则成功扣 100） ==="
curl -s -X POST "$BASE/api/mall/exchange" -H "Authorization: Bearer $T8" \
  -H "Content-Type: application/json" -d '{"productId":18}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 25. 用户5 profile 应内嵌 decoration 对象 ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/profile" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
const d=(j.data||{}).decoration||{};
console.log('code=' + j.code
  + ' frame=' + d.avatarFrameId + ' bubbleSkin=' + d.chatBubbleSkin + ' pendant=' + d.pendantId);"

echo
echo "=== 26. 好友列表回带 decoration（用于聊天页路由传参） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/friends" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
const list=j.data||[];
console.log('code=' + j.code + ' n=' + list.length);
const f=list[0];
if (f) console.log('  第一位: ' + f.nickname + ' decoration=' + JSON.stringify(f.decoration||{}).slice(0,200));
else console.log('  （用户5 暂无好友，可手动加好友后重跑）');"

echo
echo "全部用例执行完毕。"

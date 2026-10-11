#!/usr/bin/env bash
# 聊天接口联调脚本（在 Just5Min-backend 目录下运行）
set -u
BASE=http://localhost:8080
NODE="D:/HBuilderX/plugins/node/node.exe"
OUT=target/chattest
mkdir -p $OUT

T5=$($NODE scripts/gen_test_token.js 5 | awk '{print $4}')
T8=$($NODE scripts/gen_test_token.js 8 | awk '{print $4}')

show() { $NODE -e "
const j=require('./$1');
console.log('code=' + j.code + ' msg=' + j.message);
if (j.data) console.log(JSON.stringify(j.data));"; }

echo "=== 1. 游客访问 /api/messages（应 401） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" "$BASE/api/messages?friendId=8"; show $OUT/o.json

echo
echo "=== 2. 用户5 拉与好友8 的历史（空库，应 code=0 空数组） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/messages?friendId=8" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 3. 用户5 给非好友3 发消息（应 1012 / 403） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X POST "$BASE/api/messages" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"toUserId":3,"content":"你好，陌生人"}'; show $OUT/o.json

echo
echo "=== 4. 用户5 给自己发（应 1013 / 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X POST "$BASE/api/messages" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"toUserId":5,"content":"自言自语"}'; show $OUT/o.json

echo
echo "=== 5. 空内容（应 400 参数错误） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X POST "$BASE/api/messages" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"toUserId":8,"content":"   "}'; show $OUT/o.json

echo
echo "=== 6. 用户5 → 好友8 发 3 条 ==="
for i in 1 2 3; do
  curl -s -X POST "$BASE/api/messages" -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
    -d "{\"toUserId\":8,\"content\":\"五号消息 $i\"}" -o $OUT/m$i.json
  $NODE -e "const j=require('./$OUT/m$i.json'); console.log('  -> code='+j.code+' id='+(j.data&&j.data.id)+' '+JSON.stringify(j.data&&j.data.content)+' mine='+(j.data&&j.data.mine)+' read='+(j.data&&j.data.read)+' at='+(j.data&&j.data.createdAt));"
done

echo
echo "=== 7. 用户8 → 好友5 发 2 条（检验双向） ==="
for i in 1 2; do
  curl -s -X POST "$BASE/api/messages" -H "Authorization: Bearer $T8" -H "Content-Type: application/json" \
    -d "{\"toUserId\":5,\"content\":\"八号消息 $i\"}" -o $OUT/n$i.json
  $NODE -e "const j=require('./$OUT/n$i.json'); console.log('  -> code='+j.code+' id='+(j.data&&j.data.id)+' '+JSON.stringify(j.data&&j.data.content)+' mine='+(j.data&&j.data.mine));"
done

echo
echo "=== 8. 用户8 的会话列表（应有 1 条会话，未读 3） ==="
curl -s -H "Authorization: Bearer $T8" "$BASE/api/messages/sessions" -o $OUT/s8.json; show $OUT/s8.json

echo
echo "=== 9. 用户5 拉历史（正序 5 条；同时把 8 发来的 2 条置已读） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/messages?friendId=8" -o $OUT/hist.json
$NODE -e "
const j=require('./$OUT/hist.json');
console.log('code=' + j.code + ' n=' + (j.data||[]).length);
(j.data||[]).forEach(m=>console.log('  id='+m.id, 'mine='+m.mine, 'read='+m.read, JSON.stringify(m.content), m.createdAt));"

echo
echo "=== 10. 用户5 的会话列表未读数（应为 0：第9步刚把 8 发来的读掉了） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/messages/sessions" -o $OUT/s5b.json; show $OUT/s5b.json

echo
echo "=== 10b. 用户8 的会话列表未读数（应仍为 3：8 还没进过会话） ==="
curl -s -H "Authorization: Bearer $T8" "$BASE/api/messages/sessions" -o $OUT/s8c.json; show $OUT/s8c.json

echo
echo "=== 11. 翻页：limit=2 取最新 2 条，再 beforeId 取更早 ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/messages?friendId=8&limit=2" -o $OUT/p1.json
$NODE -e "const j=require('./$OUT/p1.json'); console.log('页1 n='+(j.data||[]).length+' ids='+(j.data||[]).map(m=>m.id).join(','));"
FIRST=$($NODE -e "console.log(require('./$OUT/p1.json').data[0].id)")
curl -s -H "Authorization: Bearer $T5" "$BASE/api/messages?friendId=8&limit=2&beforeId=$FIRST" -o $OUT/p2.json
$NODE -e "const j=require('./$OUT/p2.json'); console.log('页2 n='+(j.data||[]).length+' ids='+(j.data||[]).map(m=>m.id).join(',')); console.log('  内容: '+(j.data||[]).map(m=>m.content).join(' | '));"

echo
echo "=== 12. 缺 friendId（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -H "Authorization: Bearer $T5" "$BASE/api/messages"; show $OUT/o.json

echo
echo "=== 13. 用户1（无任何好友）会话列表（应空数组） ==="
T1=$($NODE scripts/gen_test_token.js 1 | awk '{print $4}')
curl -s -H "Authorization: Bearer $T1" "$BASE/api/messages/sessions" -o $OUT/o.json; show $OUT/o.json

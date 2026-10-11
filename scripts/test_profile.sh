#!/usr/bin/env bash
# 「编辑资料」新字段联调脚本（在 Just5Min-backend 目录下运行）
set -u
BASE=http://localhost:8080
NODE="D:/HBuilderX/plugins/node/node.exe"
OUT=target/proftest
mkdir -p $OUT

T5=$($NODE scripts/gen_test_token.js 5 | awk '{print $4}')
T8=$($NODE scripts/gen_test_token.js 8 | awk '{print $4}')

show() { $NODE -e "
const j=require('./$1');
console.log('code=' + j.code + ' msg=' + j.message);
if (j.data) console.log('  ' + JSON.stringify(j.data));"; }

echo "=== 1. 游客 GET profile（应 401） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" "$BASE/api/user/profile"; show $OUT/o.json

echo
echo "=== 2. 用户5 初始 profile（新字段应为 null / gender=0） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/profile" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 3. 一次性写入全部 7 个新字段 ==="
curl -s -X PUT "$BASE/api/user/profile" -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"signature":"一战成硕，绝不将就","gender":1,"birthday":"2003-06-01","region":"四川 成都","targetSchool":"电子科技大学","targetMajor":"计算机科学与技术","examYear":"2027"}' \
  -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 4. 只改昵称，验证其它字段不被清空 ==="
curl -s -X PUT "$BASE/api/user/profile" -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"nickname":"改个名字"}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 5. 清空签名 + 清空生日 + 清空年份（三个字段独立清空） ==="
curl -s -X PUT "$BASE/api/user/profile" -H "Authorization: Bearer $T5" -H "Content-Type: application/json" \
  -d '{"signature":"","birthday":"","examYear":""}' -o $OUT/o.json -w "http=%{http_code}\n"; show $OUT/o.json

echo
echo "=== 6. 清空后复查：签名 null、生日 null、年份 null，但院校/专业/地区/性别仍在 ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/user/profile" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 7. 校验：性别越界 3（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"gender":3}'; show $OUT/o.json

echo
echo "=== 8. 校验：生日格式错（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"birthday":"2003/06/01"}'; show $OUT/o.json

echo
echo "=== 9. 校验：生日为未来日期（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"birthday":"2099-01-01"}'; show $OUT/o.json

echo
echo "=== 10. 校验：考研年份越界 1990（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"examYear":"1990"}'; show $OUT/o.json

echo
echo "=== 11. 校验：考研年份非数字（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"examYear":"abc"}'; show $OUT/o.json

echo
echo "=== 12. 校验：签名超 64 字（应 400） ==="
LONG=$(printf 'a%.0s' $(seq 1 70))
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d "{\"signature\":\"$LONG\"}"; show $OUT/o.json

echo
echo "=== 13. 校验：空 payload（应 400 没有需要更新的内容） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{}'; show $OUT/o.json

echo
echo "=== 14. 昵称清空（应 400） ==="
curl -s -o $OUT/o.json -w "http=%{http_code}\n" -X PUT "$BASE/api/user/profile" \
  -H "Authorization: Bearer $T5" -H "Content-Type: application/json" -d '{"nickname":"   "}'; show $OUT/o.json

echo
echo "=== 15. 用户8 的 profile 不受用户5 改动影响 ==="
curl -s -H "Authorization: Bearer $T8" "$BASE/api/user/profile" -o $OUT/o.json; show $OUT/o.json

echo
echo "=== 16. 好友列表接口不返回新字段（响应体体积无增长） ==="
curl -s -H "Authorization: Bearer $T5" "$BASE/api/friends" -o $OUT/o.json
$NODE -e "
const j=require('./$OUT/o.json');
const f=(j.data||[])[0]||{};
console.log('code=' + j.code + ' n=' + (j.data||[]).length);
console.log('  字段: ' + Object.keys(f).join(','));"

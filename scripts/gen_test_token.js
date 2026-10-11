// 联调辅助：用后端同一套 JWT 配置，直接签发任意 userId 的 token（免登录接口）
// 仅用于本地接口联调，勿用于生产。HS256 = base64url(header).base64url(payload).base64url(HMAC)
const crypto = require('crypto');

const SECRET = process.env.JWT_SECRET || 'just5min-dev-secret-please-change-me-0123456789abcdef';

function b64url(buf) {
  return Buffer.from(buf).toString('base64').replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

function sign(userId) {
  const now = Math.floor(Date.now() / 1000);
  const header = { alg: 'HS256' };
  const payload = { sub: String(userId), iat: now, exp: now + 168 * 3600 };
  const signingInput = b64url(JSON.stringify(header)) + '.' + b64url(JSON.stringify(payload));
  const sig = crypto.createHmac('sha256', SECRET).update(signingInput).digest();
  return signingInput + '.' + b64url(sig);
}

const ids = process.argv.slice(2);
for (const id of ids) {
  console.log('USER ' + id + ' TOKEN ' + sign(id));
}

#!/usr/bin/env bash
# =====================================================================
# aicyi 平台后端 API 回归测试套件（生产上线验收）
#
# 覆盖：认证与令牌生命周期 / 网关权限收口 / admin 管理 / message / work-order /
#       log 审计（含清理守卫与留痕）/ 跨服务令牌吊销
#
# 用法：
#   ./api-regression.sh            # 完整验收（含 31s 吊销窗口等待，约 2 分钟）
#   ./api-regression.sh --fast     # 快速自测（跳过吊销窗口用例，约 30s）
#
# 退出码：0 全绿；1 存在失败
# 依赖：curl / jq / docker（mysql redis 计数校验）
# =====================================================================
set -u
cd "$(dirname "$0")"

GW=http://127.0.0.1:18000
ADMIN_SVC=http://127.0.0.1:18090  # 合并后 admin 域由 aicyi-auth(18090) 承载
TS=$(date +%Y%m%d%H%M%S)
FAST=0
[ "${1:-}" = "--fast" ] && FAST=1

PASS=0; FAIL=0; SKIP=0; FAILED_CASES=()
ok()   { PASS=$((PASS+1)); echo "  ✅ $1"; }
bad()  { FAIL=$((FAIL+1)); FAILED_CASES+=("$1"); echo "  ❌ $1 —— $2"; }
skip() { SKIP=$((SKIP+1)); echo "  ⏭  $1（fast 模式跳过）"; }
assert_eq() { # name expected actual
  if [ "$2" = "$3" ]; then ok "$1"; else bad "$1" "期望=$2 实际=$3"; fi
}
code_of() { echo "$1" | jq -r '.code // "NOCODE"'; }

section() { echo; echo "━━ $1 ━━"; }

echo "╔══════════════════════════════════════════╗"
echo "║  aicyi 平台后端 API 回归测试              ║"
echo "║  模式: $([ $FAST = 1 ] && echo fast || echo FULL)                                     ║"
echo "╚══════════════════════════════════════════╝"

# ---------------------------------------------------------------- S0 环境前置
section "S0 环境前置检查"
for p in 18000 18090 18092 18093 18094; do
  nc -z 127.0.0.1 $p 2>/dev/null && ok "端口 $p 存活" || bad "端口 $p 存活" "服务未启动"
done

# ---------------------------------------------------------------- S1 认证与令牌
section "S1 认证与令牌生命周期（aicyi-auth）"
R=$(curl -s -X POST $GW/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}')
assert_eq "管理员登录" "0" "$(code_of "$R")"
AT_A=$(echo "$R" | jq -r '.data.accessToken // empty'); RT_A=$(echo "$R" | jq -r '.data.refreshToken // empty')
[ -n "$AT_A" ] && ok "签发 accessToken/refreshToken" || bad "签发 accessToken/refreshToken" "令牌为空"

R=$(curl -s -X POST $GW/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"wrong-password"}')
[ "$(code_of "$R")" != "0" ] && ok "错误密码拒绝（code=$(code_of "$R")）" || bad "错误密码拒绝" "不应成功"
assert_eq "防枚举：忘记密码恒定成功" "0" "$(code_of "$(curl -s -X POST $GW/api/auth/forgot-password -H "Content-Type: application/json" -d '{"username":"admin"}')")"

R=$(curl -s -X POST $GW/api/auth/login -H "Content-Type: application/json" -d '{"username":"tst_noperm","password":"Tst12345"}')
assert_eq "零权限用户登录" "0" "$(code_of "$R")"
AT_T=$(echo "$R" | jq -r '.data.accessToken // empty'); RT_T=$(echo "$R" | jq -r '.data.refreshToken // empty')

# 刷新：新 AT 生效且与旧 AT 不同
R=$(curl -s -X POST $GW/api/auth/refresh -H "Content-Type: application/json" -d "{\"refreshToken\":\"$RT_T\"}")
assert_eq "刷新令牌" "0" "$(code_of "$R")"
AT_T2=$(echo "$R" | jq -r '.data.accessToken // empty')
[ -n "$AT_T2" ] && [ "$AT_T2" != "$AT_T" ] && ok "刷新签发新 accessToken" || bad "刷新签发新 accessToken" "AT 未变化"
assert_eq "新 accessToken 认证有效（权限层正确拒绝）" "40300" "$(code_of "$(curl -s "$GW/api/work-order/page?page=1&size=1" -H "Authorization: Bearer $AT_T2")")"

# 无令牌访问
HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$GW/api/work-order/page?page=1&size=1")
[ "$HTTP" = "401" ] && ok "无令牌访问被拒（HTTP 401）" || bad "无令牌访问被拒" "HTTP=$HTTP"

# ---------------------------------------------------------------- S2 网关权限收口
section "S2 网关权限收口（PermissionGlobalFilter）"
assert_eq "零权限用户读工单被拒" "40300" "$(code_of "$(curl -s "$GW/api/work-order/page?page=1&size=1" -H "Authorization: Bearer $AT_T")")"
assert_eq "零权限用户读审计日志被拒" "40300" "$(code_of "$(curl -s "$GW/api/log/oper/list?page=1&size=1" -H "Authorization: Bearer $AT_T")")"
assert_eq "零权限用户读日志详情被拒" "40300" "$(code_of "$(curl -s "$GW/api/log/oper/1" -H "Authorization: Bearer $AT_T")")"
assert_eq "零权限用户调清理被拒" "40300" "$(code_of "$(curl -s -X DELETE "$GW/api/log/clean?before=2020-01-01T00:00:00" -H "Authorization: Bearer $AT_T")")"
assert_eq "内部权限端点经网关被拒" "40300" "$(code_of "$(curl -s "$GW/api/system/perm/api-mappings" -H "Authorization: Bearer $AT_A")")"
assert_eq "超级管理员放行（工单分页）" "0" "$(code_of "$(curl -s "$GW/api/work-order/page?page=1&size=1" -H "Authorization: Bearer $AT_A")")"
R=$(curl -s "$ADMIN_SVC/api/system/perm/api-mappings" -H "X-User-Id: 2" -H "X-Username: admin")
N=$(echo "$R" | jq '.data | length' 2>/dev/null)
[ "$(code_of "$R")" = "0" ] && [ "${N:-0}" -gt 5 ] && ok "权限映射 RPC 正常（${N} 条映射）" || bad "权限映射 RPC 正常" "code=$(code_of "$R") N=$N"

# ---------------------------------------------------------------- S3 admin 管理
section "S3 系统管理（aicyi-admin）"
assert_eq "用户分页" "0" "$(code_of "$(curl -s "$GW/api/system/user/list?page=1&size=5" -H "Authorization: Bearer $AT_A")")"
R=$(curl -s "$GW/api/system/user/list?page=1&size=20" -H "Authorization: Bearer $AT_A")
assert_eq "角色分页" "0" "$(code_of "$(curl -s "$GW/api/system/role/list?page=1&size=5" -H "Authorization: Bearer $AT_A")")"
assert_eq "菜单列表" "0" "$(code_of "$(curl -s "$GW/api/system/menu/list" -H "Authorization: Bearer $AT_A")")"

# 用户 CRUD 回路：创建 → 查询 → 删除
UN="api_test_$TS"
R=$(curl -s -X POST $GW/api/system/user/add -H "Authorization: Bearer $AT_A" -H "Content-Type: application/json" \
  -d "{\"username\":\"$UN\",\"password\":\"Apidemo123!\",\"nickname\":\"API回归测试\",\"mobile\":\"13800000000\",\"email\":\"$UN@test.local\"}")
assert_eq "创建用户" "0" "$(code_of "$R")"
NEW_ID=$(echo "$R" | jq -r '.data.id // .data // empty')
R=$(curl -s "$GW/api/system/user/list?page=1&size=50&username=$UN" -H "Authorization: Bearer $AT_A")
FOUND=$(echo "$R" | jq -r --arg u "$UN" '[.data.list[]? | select(.username==$u)] | length')
[ "${FOUND:-0}" -ge 1 ] && ok "新用户可查询" || bad "新用户可查询" "分页未命中 $UN"
if [ -n "$NEW_ID" ] && [ "$NEW_ID" != "null" ]; then
  assert_eq "删除测试用户" "0" "$(code_of "$(curl -s -X POST "$GW/api/system/user/delete?id=$NEW_ID" -H "Authorization: Bearer $AT_A")")"
else
  bad "删除测试用户" "未取得新用户 id"
fi

# ---------------------------------------------------------------- S4 message
section "S4 消息中心（aicyi-message，不做真实发送）"
assert_eq "模板分页" "0" "$(code_of "$(curl -s "$GW/api/message/template/list?page=1&size=5" -H "Authorization: Bearer $AT_A")")"
R=$(curl -s -X POST $GW/api/message/template/add -H "Authorization: Bearer $AT_A" -H "Content-Type: application/json" \
  -d "{\"templateCode\":\"API_TEST_$TS\",\"templateName\":\"API回归测试模板\",\"messageType\":\"mail\",\"content\":\"自动化测试内容\"}")
TPL_CODE_OK=0
if [ "$(code_of "$R")" = "0" ]; then TPL_CODE_OK=1; ok "创建模板"; else bad "创建模板" "code=$(code_of "$R")"; fi
TPL_ID=$(echo "$R" | jq -r '.data.id // .data // empty')
R=$(curl -s "$GW/api/message/template/list?page=1&size=20&templateCode=API_TEST_$TS" -H "Authorization: Bearer $AT_A")
FOUND=$(echo "$R" | jq -r --arg c "API_TEST_$TS" '[.data.list[]? | select(.templateCode==$c)] | length')
[ "${FOUND:-0}" -ge 1 ] && ok "新模板可查询" || bad "新模板可查询" "分页未命中"
[ "$TPL_ID" != "" ] && [ "$TPL_ID" != "null" ] && \
  assert_eq "删除测试模板" "0" "$(code_of "$(curl -s -X POST "$GW/api/message/template/delete?id=$TPL_ID" -H "Authorization: Bearer $AT_A")")" \
  || bad "删除测试模板" "未取得模板 id"
C=$(curl -s -X POST "$GW/api/message/template/test-send/0" -H "Authorization: Bearer $AT_A" -H "Content-Type: application/json" -d '{}' | jq -r '.code // "ERR"' 2>/dev/null | head -c 40 | tr -d '\n\r')
if [ "$C" != "0" ]; then ok "无效发送请求被拦截（code=${C}）"; else bad "无效发送请求被拦截" "code=0"; fi

# ---------------------------------------------------------------- S5 work-order
section "S5 工单中心（aicyi-work-order）"
assert_eq "工单分页" "0" "$(code_of "$(curl -s "$GW/api/work-order/page?page=1&size=5" -H "Authorization: Bearer $AT_A")")"
WO_ID=$(curl -s "$GW/api/work-order/page?page=1&size=1" -H "Authorization: Bearer $AT_A" | jq -r '.data.list[0].id // empty')
[ -n "$WO_ID" ] && assert_eq "工单详情" "0" "$(code_of "$(curl -s "$GW/api/work-order/$WO_ID" -H "Authorization: Bearer $AT_A")")" \
  || bad "工单详情" "库中无工单可测"

# ---------------------------------------------------------------- S6 log 审计
section "S6 审计日志（aicyi-log）"
R=$(curl -s "$GW/api/log/oper/list?page=1&size=5" -H "Authorization: Bearer $AT_A")
assert_eq "审计日志分页" "0" "$(code_of "$R")"
TOTAL=$(echo "$R" | jq -r '.data.total // 0')
[ "${TOTAL:-0}" -gt 0 ] && ok "审计数据非空（total=${TOTAL}）" || bad "审计数据非空" "total=$TOTAL"
LOG_ID=$(echo "$R" | jq -r '.data.list[0].id // empty')
[ -n "$LOG_ID" ] && assert_eq "审计详情" "0" "$(code_of "$(curl -s "$GW/api/log/oper/$LOG_ID" -H "Authorization: Bearer $AT_A")")" \
  || bad "审计详情" "无日志可测"
assert_eq "清理保留期守卫（未来时间拒绝）" "40001" "$(code_of "$(curl -s -X DELETE "$GW/api/log/clean?before=2027-01-01T00:00:00" -H "Authorization: Bearer $AT_A")")"

# 留痕链路：触发一次 @OperLog 动作 → 轮询落库
UID_T=361534134612393984
curl -s -X PUT "$GW/api/system/user/status/$UID_T?status=0" -H "Authorization: Bearer $AT_A" -o /dev/null
curl -s -X PUT "$GW/api/system/user/status/$UID_T?status=1" -H "Authorization: Bearer $AT_A" -o /dev/null
TRAIL=0
for i in $(seq 1 6); do
  sleep 2
  TRAIL=$(docker exec mysql mysql -uroot -proot --default-character-set=utf8mb4 aicyi_platform -N -e \
    "SELECT COUNT(*) FROM sys_oper_log WHERE oper_type='STATUS' AND operate_time >= NOW() - INTERVAL 1 MINUTE" 2>/dev/null)
  [ "${TRAIL:-0}" -ge 1 ] && break
done
[ "${TRAIL:-0}" -ge 1 ] && ok "@OperLog 操作留痕落库（异步 ≤12s）" || bad "@OperLog 操作留痕落库" "60s 内未见 STATUS 行"

# ---------------------------------------------------------------- S7 跨服务令牌吊销
section "S7 跨服务令牌吊销（admin kickOff → 网关 40102）"
if [ $FAST = 1 ]; then
  skip "登出后旧令牌 40102（吊销窗口 31s）"
  skip "禁用用户跨服务踢下线 40102（吊销窗口 31s）"
else
  A2=$(curl -s -X POST $GW/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}')
  AT_A2=$(echo "$A2" | jq -r '.data.accessToken // empty'); RT_A2=$(echo "$A2" | jq -r '.data.refreshToken // empty')
  curl -s -X POST $GW/api/auth/logout -H "Authorization: Bearer $AT_A2" -H "Content-Type: application/json" -d "{\"refreshToken\":\"$RT_A2\"}" -o /dev/null
  sleep 31
  assert_eq "登出后旧令牌被网关拒绝" "40102" "$(code_of "$(curl -s "$GW/api/work-order/page?page=1&size=1" -H "Authorization: Bearer $AT_A2")")"

  A3=$(curl -s -X POST $GW/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}')
  AT_A3=$(echo "$A3" | jq -r '.data.accessToken // empty')
  curl -s -X PUT "$GW/api/system/user/status/$UID_T?status=0" -H "Authorization: Bearer $AT_A3" -o /dev/null
  sleep 31
  assert_eq "禁用用户旧令牌跨服务失效" "40102" "$(code_of "$(curl -s "$GW/api/log/oper/list?page=1&size=1" -H "Authorization: Bearer $AT_T2")")"
  assert_eq "重新启用用户" "0" "$(code_of "$(curl -s -X PUT "$GW/api/system/user/status/$UID_T?status=1" -H "Authorization: Bearer $AT_A3")")"
fi

# ---------------------------------------------------------------- 汇总
section "汇总"
echo "  通过: $PASS   失败: $FAIL   跳过: $SKIP"
if [ $FAIL -gt 0 ]; then
  echo "  失败清单:"
  for c in "${FAILED_CASES[@]}"; do echo "    - $c"; done
  echo
  echo "❌ 未达上线验收标准"
  exit 1
fi
echo
echo "✅ 后端 API 回归全绿，达到上线验收标准"

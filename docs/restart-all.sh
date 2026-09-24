#!/usr/bin/env bash
###############################################################################
# restart-all.sh — 一键重启 aicyi 微服务全家桶（含单体并行期 + 前端）
#
#   覆盖服务（端口）：
#     aicyi-auth           18090  认证授权中心
#     aicyi-admin          18091  RBAC 权限管理微服务（替代单体承接 /api/system/**）
#     aicyi-log          18092  审计日志（MQ 消费）
#     aicyi-message      18093  消息通知（MQ 消费）
#     aicyi-work-order   18094  工单中心
#     aicyi-gateway      18000  统一入口（前端唯一访问点）
#     aicyi-platform-ui  5173   Vite 前端
#
#   依赖：Docker 起的 mysql(3306)/redis(6379)/nacos(8848)/rabbitmq(5672) 需先就绪。
#   数据源/MQ 等运行期配置由 Nacos 共享配置下发（无需在本脚本注入）。
#
# 用法:
#   ./restart-all.sh              # 停止→用现有 jar 启动→起前端
#   ./restart-all.sh --build      # 停止→各服务 reactor 重新 mvn package→启动
#   ./restart-all.sh --skip-ui    # 不启动前端
#   ./restart-all.sh --only gateway,auth   # 只重启指定服务（逗号分隔；key 见下方 SERVICES：admin/auth/work-order/log/message/gateway，前端用 ui）
#
# 停止: ./restart-all.sh --stop-only
# 日志/PID: 工作区根目录 logs/<服务名>.log / .pid
###############################################################################
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 本脚本位于 aicyi-platform/docs/，工作区根目录需向上两级（后端服务在 aicyi-platform/ 下，前端在根目录下）
ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

# JDK 17（默认 java 可能是 JDK 8，本项目构建/运行需 17）
JDK17="/Users/liangchaomin/Library/Java/JavaVirtualMachines/temurin-17.0.20/Contents/Home"
if [[ ! -x "$JDK17/bin/java" ]]; then
  JDK17="$(/usr/libexec/java_home -v 17 2>/dev/null || true)"
fi
JAVA_BIN="${JDK17:+$JDK17/bin/java}"
JAVA_BIN="${JAVA_BIN:-java}"

# 服务定义：key|端口|工作目录(相对 ROOT)|jar(相对 ROOT)|进程匹配特征
# 顺序即启动顺序；停止时逆序。gateway 依赖下游注册但 lb 懒解析，置于后端最后。
SERVICES=(
  "auth|18090|aicyi-platform/aicyi-auth|aicyi-platform/aicyi-auth/aicyi-auth-boot/target/aicyi-auth-boot-0.0.1-SNAPSHOT.jar|aicyi-auth-boot-.*\.jar"
  "admin|18091|aicyi-platform/aicyi-admin|aicyi-platform/aicyi-admin/aicyi-admin-boot/target/aicyi-admin-boot-0.0.1-SNAPSHOT.jar|aicyi-admin-boot-.*\.jar"
  "log|18092|aicyi-platform/aicyi-log|aicyi-platform/aicyi-log/aicyi-log-boot/target/aicyi-log-boot-0.0.1-SNAPSHOT.jar|aicyi-log-boot-.*\.jar"
  "message|18093|aicyi-platform/aicyi-message|aicyi-platform/aicyi-message/aicyi-message-boot/target/aicyi-message-boot-0.0.1-SNAPSHOT.jar|aicyi-message-boot-.*\.jar"
  "work-order|18094|aicyi-platform/aicyi-work-order|aicyi-platform/aicyi-work-order/aicyi-work-order-boot/target/aicyi-work-order-boot-0.0.1-SNAPSHOT.jar|aicyi-work-order-boot-.*\.jar"
  "gateway|18000|aicyi-platform/aicyi-gateway|aicyi-platform/aicyi-gateway/target/aicyi-gateway-0.0.1-SNAPSHOT.jar|aicyi-gateway-0\.0\.1-SNAPSHOT\.jar"
)
UI_PORT=5173
UI_PATTERN="aicyi-platform-ui/node_modules"

# ── 参数解析 ────────────────────────────────────────────────────────────────
DO_BUILD=0
START_UI=1
STOP_ONLY=0
ONLY=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --build) DO_BUILD=1 ;;
    --skip-ui) START_UI=0 ;;
    --stop-only) STOP_ONLY=1 ;;
    --only) ONLY="${2:-}"; shift ;;
    -h|--help) sed -n '2,40p' "$0"; exit 0 ;;
    *) echo "未知参数: $1（支持 --build/--skip-ui/--stop-only/--only a,b,c）"; exit 1 ;;
  esac
  shift
done

log()  { printf '%s\n' "$*"; }
fail() { printf '❌ %s\n' "$*" >&2; exit 1; }

# 是否需要处理某服务（--only 过滤）
selected() { # $1=key
  [[ -z "$ONLY" ]] && return 0
  [[ ",$ONLY," == *",$1,"* ]]
}

# ── 停止：pid 文件 + 端口占用 + 进程特征 三层兜底，先 TERM 后 KILL -9 ────────
stop_service() { # $1=port $2=pidfile $3=pattern $4=name
  local pids pid port_pids pat_pids
  pids=""
  [[ -f "$2" ]] && pids="$(cat "$2" 2>/dev/null || true)"
  port_pids="$(lsof -ti tcp:"$1" 2>/dev/null || true)"
  pat_pids="$(pgrep -f "$3" 2>/dev/null | grep -vx "$$" || true)"
  pids="$(echo "$pids $port_pids $pat_pids" | tr ' ' '\n' | sort -u | grep -v '^$' || true)"
  [[ -z "$pids" ]] && { log "  （$4 未在运行）"; return 0; }
  for pid in $pids; do kill "$pid" 2>/dev/null || true; done
  # 最多等 10s，未退则强杀
  for _ in $(seq 1 20); do
    local alive=""
    for pid in $pids; do kill -0 "$pid" 2>/dev/null && alive="$alive $pid"; done
    [[ -z "${alive// /}" ]] && break
    sleep 0.5
  done
  for pid in $pids; do kill -0 "$pid" 2>/dev/null && { kill -9 "$pid" 2>/dev/null || true; }; done
  rm -f "$2"
  log "  ⏹ 已停止 $4"
}

stop_all() {
  local i
  # 前端
  selected ui && stop_service "$UI_PORT" "$LOG_DIR/aicyi-platform-ui.pid" "$UI_PATTERN" "aicyi-platform-ui"
  # 后端逆序
  for (( i=${#SERVICES[@]}-1; i>=0; i-- )); do
    IFS='|' read -r key port _dir _jar pat <<< "${SERVICES[$i]}"
    selected "$key" && stop_service "$port" "$LOG_DIR/aicyi-$key.pid" "$pat" "aicyi-$key"
  done
}

# ── 预检 ────────────────────────────────────────────────────────────────────
precheck_infra() {
  local p
  for p in 3306 6379 8848 5672; do
    nc -z 127.0.0.1 "$p" 2>/dev/null || fail "本机端口 $p 未监听（MySQL/Redis/Nacos/RabbitMQ 未就绪），请先启动 Docker 依赖"
  done
  log "✅ 依赖中间件就绪（mysql/redis/nacos/rabbitmq）"
}

# ── 启动：nohup 后台 + 端口就绪等待 ──────────────────────────────────────────
# 端口就绪判定用 lsof（兼容 IPv4/IPv6/通配，Vite 默认监听 ::1 时 nc 127.0.0.1 会漏判）
wait_port() { # $1=port $2=timeout秒 —— 就绪返回0
  local port="$1" timeout="$2" i
  for ((i=1; i<=timeout; i++)); do
    lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1 && return 0
    sleep 1
  done
  return 1
}

start_service() { # $1=key $2=port $3=workdir $4=jar $5=pattern
  local key="$1" port="$2" dir="$3" jar="$4"
  local logfile="$LOG_DIR/aicyi-$key.log" pidfile="$LOG_DIR/aicyi-$key.pid"
  [[ -f "$ROOT/$jar" ]] || { log "⚠️ 跳过 aicyi-${key}：未找到 jar（${jar}），如需构建加 --build"; return 1; }
  (cd "$ROOT/$dir" && nohup "$JAVA_BIN" -jar "$ROOT/$jar" > "$logfile" 2>&1 < /dev/null & echo $! > "$pidfile")
  if wait_port "$port" 90; then
    log "✅ aicyi-$key 就绪（:${port}）"
    return 0
  fi
  log "❌ aicyi-${key} 90s 内未监听 :${port}，日志尾部："
  tail -n 30 "$logfile" 2>/dev/null || true
  return 1
}

start_ui() {
  local logfile="$LOG_DIR/aicyi-platform-ui.log" pidfile="$LOG_DIR/aicyi-platform-ui.pid"
  if [[ ! -d "$ROOT/aicyi-platform-ui/node_modules" ]]; then
    log "→ 前端依赖缺失，执行 npm ci ..."
    (cd "$ROOT/aicyi-platform-ui" && npm ci --no-fund --no-audit --cache "$ROOT/.npm-cache") || fail "前端依赖安装失败"
  fi
  (cd "$ROOT/aicyi-platform-ui" && nohup npm run dev -- --strictPort > "$logfile" 2>&1 < /dev/null & echo $! > "$pidfile")
  if wait_port "$UI_PORT" 45; then
    log "✅ 前端就绪（http://localhost:${UI_PORT}/）"
    return 0
  fi
  log "❌ 前端 45s 内未监听 :${UI_PORT}，日志尾部："
  tail -n 30 "$logfile" 2>/dev/null || true
  return 1
}

build_service() { # $1=key $2=dir
  local key="$1" dir="$2"
  [[ "$DO_BUILD" -eq 1 ]] || return 0
  [[ -f "$ROOT/$dir/pom.xml" ]] || { log "  （无 pom，跳过构建：aicyi-${key}）"; return 0; }
  log "→ 构建 aicyi-$key ..."
  (cd "$ROOT/$dir" && JAVA_HOME="$JDK17" mvn -q -DskipTests package) || fail "aicyi-$key 构建失败"
}

# ── 主流程 ──────────────────────────────────────────────────────────────────
log "======== aicyi 一键重启 ========"
stop_all
[[ "$STOP_ONLY" -eq 1 ]] && { log "======== 已按 --stop-only 停止全部服务 ========"; exit 0; }

precheck_infra

if [[ "$DO_BUILD" -eq 1 ]]; then
  { [[ "$JAVA_BIN" == java ]] || [[ -x "$JAVA_BIN" ]]; } || fail "未找到 JDK 17，请安装 Temurin 17 或调整脚本 JDK17 路径"
  log "→ Maven 构建各服务（跳过测试；注意：aicyi 框架/middleware 改动需另行 mvn install）"
  for entry in "${SERVICES[@]}"; do
    IFS='|' read -r key _p dir _j _pat <<< "$entry"
    selected "$key" && build_service "$key" "$dir"
  done
fi

log "→ 启动后端服务 ..."
for entry in "${SERVICES[@]}"; do
  IFS='|' read -r key port dir jar pat <<< "$entry"
  selected "$key" && start_service "$key" "$port" "$dir" "$jar" "$pat"
done

if [[ "$START_UI" -eq 1 ]] && selected ui; then
  log "→ 启动前端 ..."
  start_ui
fi

log ""
log "======== ✅ 完成 ========"
for entry in "${SERVICES[@]}"; do
  IFS='|' read -r key port _d _j _p <<< "$entry"
  selected "$key" && printf '  aicyi-%-12s :%s\n' "$key" "$port"
done
if [[ "$START_UI" -eq 1 ]] && selected ui; then
  printf '  %-18s http://localhost:%s/\n' "前端(ui)" "$UI_PORT"
fi
log "  统一入口(前端代理): http://localhost:18000"
log "  日志: logs/<服务名>.log   停止: ./restart-all.sh --stop-only"
log "=========================="

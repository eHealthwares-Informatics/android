#!/usr/bin/env bash
#
# host.sh — serve any file (default: the debug APK) over the local network.
#
# Usage:
#   ./host.sh [FILE] [PORT]     start server, symlinking FILE into the host dir
#   ./host.sh stop              stop the running server
#   ./host.sh status            show current server status and URLs
#   ./host.sh --copy FILE       start server with a copy instead of a symlink
#
# The file is symlinked by default so a rebuilt APK is served immediately
# without re-running the tool. Use --copy if you need a snapshot instead.
#
# Examples:
#   ./host.sh                                     # host the debug APK on :8000
#   ./host.sh app/build/outputs/apk/debug/app-debug.apk 8080
#   ./host.sh --copy ~/some-file.bin 9000

set -euo pipefail

PORT="${PORT:-8000}"
HOST_DIR="${HOST_DIR:-${TMPDIR:-/tmp}/rxsoft-host}"
PID_FILE="$HOST_DIR/server.pid"
LOG_FILE="$HOST_DIR/server.log"
DEFAULT_APK="$(cd "$(dirname "$0")" && pwd)/app/build/outputs/apk/debug/app-debug.apk"
MODE="symlink"

log()  { printf '[host] %s\n' "$*"; }
die()  { printf '[host] ERROR: %s\n' "$*" >&2; exit 1; }

lan_ip() {
  local ip
  for iface in en0 en1 en2 en3; do
    ip="$(ipconfig getifaddr "$iface" 2>/dev/null || true)"
    [[ -n "$ip" ]] && { printf '%s' "$ip"; return; }
  done
  printf '%s' "127.0.0.1"
}

running_pid() {
  [[ -f "$PID_FILE" ]] || return 1
  local pid
  pid="$(cat "$PID_FILE")"
  kill -0 "$pid" 2>/dev/null
}

stop_server() {
  if running_pid; then
    kill "$(cat "$PID_FILE")"
    rm -f "$PID_FILE"
    log "server stopped"
  else
    rm -f "$PID_FILE"
    log "no server was running"
  fi
}

urls() {
  local file
  file="$1"
  local name
  name="$(basename "$file")"
  log "local : http://localhost:$PORT/$name"
  log "LAN   : http://$(lan_ip):$PORT/$name"
}

status() {
  if running_pid; then
    local file
    file="$(readlink "$HOST_DIR/served" 2>/dev/null || cat "$HOST_DIR/served-target" 2>/dev/null || echo "?")"
    log "server running (pid $(cat "$PID_FILE")) on port $PORT, serving:"
    urls "$file"
  else
    log "no server running"
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    stop)   stop_server; exit 0 ;;
    status) status; exit 0 ;;
    --copy) MODE="copy"; shift ;;
    *) break ;;
  esac
done

SOURCE="${1:-$DEFAULT_APK}"
PORT="${2:-$PORT}"

[[ -e "$SOURCE" ]] || die "file not found: $SOURCE"
SOURCE="$(cd "$(dirname "$SOURCE")" && pwd)/$(basename "$SOURCE")"
NAME="$(basename "$SOURCE")"

if running_pid; then
  log "restarting server (previous pid $(cat "$PID_FILE"))"
  stop_server
fi

if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  die "port $PORT already in use"
fi

mkdir -p "$HOST_DIR"
rm -f "$HOST_DIR/served" "$HOST_DIR/served-target" "$HOST_DIR/$NAME"
printf '%s\n' "$SOURCE" > "$HOST_DIR/served-target"

if [[ "$MODE" == "symlink" ]] && ln -s "$SOURCE" "$HOST_DIR/served" 2>/dev/null; then
  ln -s "$SOURCE" "$HOST_DIR/$NAME"
  log "linking $SOURCE -> $HOST_DIR/$NAME"
else
  MODE="copy"
  cp "$SOURCE" "$HOST_DIR/$NAME"
  ln -s "$HOST_DIR/$NAME" "$HOST_DIR/served"
  log "copying $SOURCE -> $HOST_DIR/$NAME"
fi

nohup python3 -m http.server "$PORT" --bind 0.0.0.0 --directory "$HOST_DIR" \
  >"$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
sleep 1

kill -0 "$(cat "$PID_FILE")" 2>/dev/null || { log "server failed to start"; cat "$LOG_FILE" >&2; exit 1; }

log "serving $NAME (mode: $MODE) on port $PORT"
urls "$SOURCE"
log "log: $LOG_FILE  |  stop: ./host.sh stop"

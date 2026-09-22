#!/usr/bin/env bash
#
# host.sh — build and serve the debug APK over the local network for 10 minutes.
#
# Usage:
#   ./host.sh                         build, host APK for 10 minutes
#   ./host.sh [FILE] [PORT]           host an existing file for 10 minutes
#   ./host.sh stop                    stop the running server
#   ./host.sh status                  show current server status and URLs
#   ./host.sh --copy FILE [PORT]      host a copy instead of a symlink
#   ./host.sh --no-build              skip build and host existing APK
#
# Default:
#   - Builds the debug APK
#   - Hosts it on port 8000
#   - Automatically stops after 10 minutes
#

set -euo pipefail

PORT="${PORT:-8000}"
HOST_DIR="${HOST_DIR:-${TMPDIR:-/tmp}/rxsoft-host}"
PID_FILE="$HOST_DIR/server.pid"
LOG_FILE="$HOST_DIR/server.log"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APK_DIR="$SCRIPT_DIR/app/build/outputs/apk/debug"
DEFAULT_APK="$APK_DIR/app-debug.apk"

MODE="symlink"
BUILD=true
CACHE_BUST="$(date +%s)"
BUILD_NUMBER="$(git describe --tags --always 2>/dev/null || echo 0)"
HOST_DURATION=600 # 10 minutes = 600 seconds

log() {
  printf '[host] %s\n' "$*"
}

die() {
  printf '[host] ERROR: %s\n' "$*" >&2
  exit 1
}

lan_ip() {
  local ip

  for iface in en0 en1 en2 en3; do
    ip="$(ipconfig getifaddr "$iface" 2>/dev/null || true)"
    [[ -n "$ip" ]] && {
      printf '%s' "$ip"
      return
    }
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
    local pid
    pid="$(cat "$PID_FILE")"

    kill "$pid" 2>/dev/null || true
    rm -f "$PID_FILE"

    log "server stopped"
  else
    rm -f "$PID_FILE"
    log "no server was running"
  fi
}

urls() {
  local name="$1"

  log "local : http://localhost:$PORT/$name?v=$CACHE_BUST"
  log "LAN   : http://$(lan_ip):$PORT/$name?v=$CACHE_BUST"
}

status() {
  if running_pid; then
    local file
    local name

    file="$(
      readlink "$HOST_DIR/served" 2>/dev/null ||
      cat "$HOST_DIR/served-target" 2>/dev/null ||
      echo "?"
    )"
    name="$(basename "$file")"

    log "server running (pid $(cat "$PID_FILE")) on port $PORT, serving:"
    urls "$name"
    log "auto-stop: after 10 minutes"
  else
    log "no server running"
  fi
}

build_apk() {
  log "building debug APK..."

  cd "$SCRIPT_DIR"

  if [[ -x "./gradlew" ]]; then
    ./gradlew assembleDebug
  else
    die "Gradle wrapper not found: $SCRIPT_DIR/gradlew"
  fi

  [[ -f "$DEFAULT_APK" ]] || DEFAULT_APK="$(ls -t "$APK_DIR"/*.apk 2>/dev/null | head -1)"
  [[ -f "$DEFAULT_APK" ]] || die "build completed but APK was not found in: $APK_DIR"

  log "build completed successfully"
  log "APK: $DEFAULT_APK"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    stop)
      stop_server
      exit 0
      ;;

    status)
      status
      exit 0
      ;;

    --copy)
      MODE="copy"
      shift
      ;;

    --no-build)
      BUILD=false
      shift
      ;;

    --duration)
      [[ $# -ge 2 ]] || die "--duration requires seconds"
      HOST_DURATION="$2"
      shift 2
      ;;

    *)
      break
      ;;
  esac
done

# ------------------------------------------------------------
# Build
# ------------------------------------------------------------

if [[ "$BUILD" == true ]]; then
  build_apk
fi

# Resolve the newest built APK when no explicit source is given (supports the
# versioned `-rxsoft` output name and --no-build).
if [[ $# -eq 0 || -z "${1:-}" ]]; then
  [[ -f "$DEFAULT_APK" ]] || DEFAULT_APK="$(ls -t "$APK_DIR"/*.apk 2>/dev/null | head -1)"
fi

# ------------------------------------------------------------
# Determine source file
# ------------------------------------------------------------

SOURCE="${1:-$DEFAULT_APK}"
PORT="${2:-$PORT}"

[[ -e "$SOURCE" ]] || die "file not found: $SOURCE"

SOURCE="$(cd "$(dirname "$SOURCE")" && pwd)/$(basename "$SOURCE")"
if [[ "$SOURCE" == "$DEFAULT_APK" ]]; then
  APP_VERSION="$(grep -oE 'versionName[[:space:]]*=[[:space:]]*"[^"]+"' "$SCRIPT_DIR/app/build.gradle.kts" 2>/dev/null | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
  [[ -n "$APP_VERSION" ]] || APP_VERSION="$(date +%Y%m%d-%H%M%S)"
  NAME="app-debug-${APP_VERSION}-rxsoft.apk"
else
  NAME="$(basename "$SOURCE")"
fi
# Cache-busting stamp: reconstructed from the file's modification time so a
# rebuilt APK produces a distinct URL (phones/browsers otherwise reuse the
# cached file when the name is unchanged).
CACHE_BUST="$(stat -f %m "$SOURCE" 2>/dev/null || date +%s)"

# ------------------------------------------------------------
# Stop existing server
# ------------------------------------------------------------

if running_pid; then
  log "restarting server (previous pid $(cat "$PID_FILE"))"
  stop_server
fi

# ------------------------------------------------------------
# Check port
# ------------------------------------------------------------

if lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  die "port $PORT already in use"
fi

# ------------------------------------------------------------
# Prepare hosting directory
# ------------------------------------------------------------

mkdir -p "$HOST_DIR"

rm -f \
  "$HOST_DIR/served" \
  "$HOST_DIR/served-target" \
  "$HOST_DIR/$NAME" \
  "$HOST_DIR"/app-debug*.apk

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

# ------------------------------------------------------------
# Start HTTP server
# ------------------------------------------------------------

nohup python3 -m http.server "$PORT" \
  --bind 0.0.0.0 \
  --directory "$HOST_DIR" \
  >"$LOG_FILE" 2>&1 &

SERVER_PID=$!
echo "$SERVER_PID" > "$PID_FILE"

sleep 1

if ! kill -0 "$SERVER_PID" 2>/dev/null; then
  log "server failed to start"
  cat "$LOG_FILE" >&2
  rm -f "$PID_FILE"
  exit 1
fi

# ------------------------------------------------------------
# Display information
# ------------------------------------------------------------

log ""
log "=========================================="
log " APK HOSTING STARTED"
log "=========================================="
log ""
log "file:     $NAME"
log "mode:     $MODE"
log "port:     $PORT"
log "duration: $((HOST_DURATION / 60)) minutes"
log "pid:      $SERVER_PID"
log ""

urls "$NAME"

log ""
log "log:      $LOG_FILE"
log "stop:     ./host.sh stop"
log "status:   ./host.sh status"
log ""
log "server will automatically stop in $((HOST_DURATION / 60)) minutes."
log ""

# ------------------------------------------------------------
# Auto-stop after specified duration
# ------------------------------------------------------------

(
  sleep "$HOST_DURATION"

  if [[ -f "$PID_FILE" ]]; then
    CURRENT_PID="$(cat "$PID_FILE")"

    if kill -0 "$CURRENT_PID" 2>/dev/null; then
      log "10-minute hosting period expired; stopping server..."

      kill "$CURRENT_PID" 2>/dev/null || true
      rm -f "$PID_FILE"

      log "server stopped automatically"
    fi
  fi
) >/dev/null 2>&1 &


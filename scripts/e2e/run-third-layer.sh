#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

POSTGRES_CONTAINER="${ATLAS_E2E_POSTGRES_CONTAINER:-atlas-e2e-third-postgres}"
POSTGRES_IMAGE="${ATLAS_E2E_POSTGRES_IMAGE:-postgres:16}"
POSTGRES_PORT="${ATLAS_E2E_POSTGRES_PORT:-55434}"
BACKEND_PORT="${ATLAS_E2E_BACKEND_PORT:-18082}"
BACKEND_URL="http://127.0.0.1:${BACKEND_PORT}"
DB_NAME="${ATLAS_E2E_DB_NAME:-atlas_knowledge_hub}"
DB_USER="${ATLAS_E2E_DB_USER:-atlas_user}"
DB_PASSWORD="${ATLAS_E2E_DB_PASSWORD:-change-me-local-only}"
KEEP_STACK="${KEEP_ATLAS_E2E_STACK:-0}"
BACKEND_LOG="${ROOT_DIR}/samples/output/e2e/third-layer-backend.log"
MODEL_PROVIDER="${ATLAS_MODEL_PROVIDER:-deepseek}"
MODEL_ENDPOINT="${ATLAS_MODEL_ENDPOINT:-https://api.deepseek.com}"
MODEL_NAME="${ATLAS_MODEL_NAME:-deepseek-chat}"

backend_pid=""

cleanup() {
  if [ -n "${backend_pid}" ] && kill -0 "${backend_pid}" 2>/dev/null; then
    kill "${backend_pid}" 2>/dev/null || true
    wait "${backend_pid}" 2>/dev/null || true
  fi
  if [ "${KEEP_STACK}" != "1" ]; then
    docker rm -f "${POSTGRES_CONTAINER}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_command docker
require_command mvn
require_command npm
require_command curl
require_command rg

if [ -z "${ATLAS_MODEL_API_KEY:-}" ]; then
  echo "Missing required config: ATLAS_MODEL_API_KEY" >&2
  echo "Third-layer provider-backed E2E is opt-in and requires a local DeepSeek API key." >&2
  exit 1
fi

if [ "${MODEL_PROVIDER}" != "deepseek" ]; then
  echo "Unsupported ATLAS_MODEL_PROVIDER=${MODEL_PROVIDER}. Third-layer E2E currently supports deepseek only." >&2
  exit 1
fi

export ATLAS_MODE="configured"
export ATLAS_SAMPLE_INPUT="${ATLAS_SAMPLE_INPUT:-samples/input/e2e}"
export ATLAS_SAMPLE_OUTPUT="${ATLAS_SAMPLE_OUTPUT:-samples/output/e2e}"
export ATLAS_MARKDOWN_OUTPUT="${ATLAS_MARKDOWN_OUTPUT:-samples/output/e2e/markdown}"
export ATLAS_API_BASE_URL="${BACKEND_URL}"

echo "Preparing third-layer provider-backed E2E state..."
bash scripts/e2e/reset-local-state.sh
bash scripts/e2e/seed-sample-data.sh
rm -rf frontend/playwright-report frontend/test-results backend/target
mkdir -p "$(dirname "${BACKEND_LOG}")"

echo "Starting local PostgreSQL container ${POSTGRES_CONTAINER} on port ${POSTGRES_PORT}..."
docker rm -f "${POSTGRES_CONTAINER}" >/dev/null 2>&1 || true
docker run --name "${POSTGRES_CONTAINER}" \
  -e POSTGRES_DB="${DB_NAME}" \
  -e POSTGRES_USER="${DB_USER}" \
  -e POSTGRES_PASSWORD="${DB_PASSWORD}" \
  -p "${POSTGRES_PORT}:5432" \
  -d "${POSTGRES_IMAGE}" >/dev/null

for _ in {1..60}; do
  if docker exec "${POSTGRES_CONTAINER}" pg_isready -U "${DB_USER}" -d "${DB_NAME}" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done
docker exec "${POSTGRES_CONTAINER}" pg_isready -U "${DB_USER}" -d "${DB_NAME}" >/dev/null

echo "Starting Spring Boot backend on ${BACKEND_URL} with provider-backed model adapter..."
(
  cd backend
  ATLAS_DB_URL="jdbc:postgresql://127.0.0.1:${POSTGRES_PORT}/${DB_NAME}" \
    ATLAS_DB_USERNAME="${DB_USER}" \
    ATLAS_DB_PASSWORD="${DB_PASSWORD}" \
    ATLAS_DB_SCHEMA="atlas" \
    ATLAS_MODEL_PROVIDER="${MODEL_PROVIDER}" \
    ATLAS_MODEL_ENDPOINT="${MODEL_ENDPOINT}" \
    ATLAS_MODEL_API_KEY="${ATLAS_MODEL_API_KEY}" \
    ATLAS_MODEL_NAME="${MODEL_NAME}" \
    mvn spring-boot:run \
      -Dspring-boot.run.arguments="--server.port=${BACKEND_PORT} --atlas.cors.allowed-origins=http://127.0.0.1:4173,http://localhost:4173"
) >"${BACKEND_LOG}" 2>&1 &
backend_pid="$!"

echo "Waiting for backend health via /api/spaces..."
for _ in {1..120}; do
  if curl -fsS "${BACKEND_URL}/api/spaces" >/dev/null 2>&1; then
    break
  fi
  if ! kill -0 "${backend_pid}" 2>/dev/null; then
    echo "Backend exited before becoming ready. Last log lines:" >&2
    tail -80 "${BACKEND_LOG}" >&2 || true
    exit 1
  fi
  sleep 1
done
curl -fsS "${BACKEND_URL}/api/spaces" >/dev/null

echo "Building frontend with live local backend API base..."
VITE_ATLAS_API_BASE_URL="${BACKEND_URL}" npm --prefix frontend run build

echo "Running third-layer provider-backed Playwright E2E..."
ATLAS_API_BASE_URL="${BACKEND_URL}" VITE_ATLAS_API_BASE_URL="${BACKEND_URL}" \
  npm --prefix frontend run e2e:third-layer

echo "Checking provider-backed artifact hygiene..."
if rg -n "Authorization:\\s*Bearer\\s+\\S+|sk-[A-Za-z0-9]{20,}|AKIA[0-9A-Z]{16}|BEGIN .*PRIVATE KEY|/[U]sers/|[C]:\\\\" \
  "${BACKEND_LOG}" frontend/playwright-report frontend/test-results samples/output/e2e 2>/dev/null; then
  echo "Unsafe provider-backed artifact content detected." >&2
  exit 1
fi

echo "Checking diff whitespace hygiene..."
git diff --check

echo "Third-layer provider-backed E2E complete."
echo "Backend log: ${BACKEND_LOG}"
echo "Playwright report: frontend/playwright-report/index.html"

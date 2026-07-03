#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-${ATLAS_MODE:-mock}}"

if [ -f ".env" ]; then
  set -a
  # shellcheck disable=SC1091
  . ".env"
  set +a
fi

require_var() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    echo "Missing required config: ${name}" >&2
    exit 1
  fi
}

case "${MODE}" in
  mock)
    : "${ATLAS_SAMPLE_INPUT:=samples/input/e2e}"
    : "${ATLAS_SAMPLE_OUTPUT:=samples/output/e2e}"
    : "${ATLAS_MARKDOWN_OUTPUT:=samples/output/e2e/markdown}"
    ;;
  configured)
    require_var "ATLAS_API_BASE_URL"
    require_var "ATLAS_DATABASE_URL"
    require_var "ATLAS_MODEL_PROVIDER"
    require_var "ATLAS_MODEL_ENDPOINT"
    require_var "ATLAS_MODEL_API_KEY"
    ;;
  *)
    echo "Unknown E2E mode: ${MODE}. Use mock or configured." >&2
    exit 1
    ;;
esac

echo "Atlas E2E config check passed for mode=${MODE}."

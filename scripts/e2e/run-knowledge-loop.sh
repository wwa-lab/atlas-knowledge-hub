#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-mock}"
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

if [ -f ".env" ]; then
  set -a
  # shellcheck disable=SC1091
  . ".env"
  set +a
fi

export ATLAS_MODE="${ATLAS_MODE:-${MODE}}"
export ATLAS_SAMPLE_INPUT="${ATLAS_SAMPLE_INPUT:-samples/input/e2e}"
export ATLAS_SAMPLE_OUTPUT="${ATLAS_SAMPLE_OUTPUT:-samples/output/e2e}"
export ATLAS_MARKDOWN_OUTPUT="${ATLAS_MARKDOWN_OUTPUT:-samples/output/e2e/markdown}"

bash scripts/e2e/check-required-config.sh "${MODE}"
bash scripts/e2e/reset-local-state.sh
bash scripts/e2e/seed-sample-data.sh
rm -rf frontend/playwright-report frontend/test-results

echo "Building frontend before automated knowledge-loop E2E..."
npm --prefix frontend run build

echo "Running automated knowledge-loop E2E in ${MODE} mode..."
npm --prefix frontend run e2e:acceptance

echo "Knowledge-loop E2E complete."
echo "Report: frontend/playwright-report/index.html"
echo "JUnit: frontend/test-results/e2e-junit.xml"

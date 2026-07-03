#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

export ATLAS_MODE="${ATLAS_MODE:-mock}"
export ATLAS_SAMPLE_INPUT="${ATLAS_SAMPLE_INPUT:-samples/input/e2e}"
export ATLAS_SAMPLE_OUTPUT="${ATLAS_SAMPLE_OUTPUT:-samples/output/e2e}"
export ATLAS_MARKDOWN_OUTPUT="${ATLAS_MARKDOWN_OUTPUT:-samples/output/e2e/markdown}"

echo "Checking first-layer mock E2E configuration..."
bash scripts/e2e/check-required-config.sh mock

echo "Resetting and seeding local sample state..."
bash scripts/e2e/reset-local-state.sh
bash scripts/e2e/seed-sample-data.sh
rm -rf frontend/playwright-report frontend/test-results

echo "Building frontend..."
npm --prefix frontend run build

echo "Running frontend Playwright E2E..."
npm --prefix frontend run e2e

echo "Running backend API, adapter, graph, and Ask contract verification..."
mvn -f backend/pom.xml verify

echo "Checking diff whitespace hygiene..."
git diff --check

echo "Checking generated E2E outputs are not tracked..."
tracked_generated="$(
  git ls-files \
    backend/target \
    frontend/playwright-report \
    frontend/test-results \
    samples/output/e2e
)"
if [ -n "${tracked_generated}" ]; then
  echo "Generated output is tracked and must not be committed:" >&2
  echo "${tracked_generated}" >&2
  exit 1
fi

echo "First-layer E2E gate complete."
echo "Playwright report: frontend/playwright-report/index.html"
echo "Playwright JUnit: frontend/test-results/e2e-junit.xml"
echo "Backend reports: backend/target/surefire-reports and backend/target/failsafe-reports"

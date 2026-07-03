#!/usr/bin/env bash
set -euo pipefail

INPUT_ROOT="${ATLAS_SAMPLE_INPUT:-samples/input/e2e}"
mkdir -p "${INPUT_ROOT}"

if [ ! -f "${INPUT_ROOT}/README.md" ]; then
  cat > "${INPUT_ROOT}/README.md" <<'README'
# E2E Sample Input

Place mock-only, non-confidential sample packages here for configured local testing.
The committed repository intentionally keeps this directory lightweight.
README
fi

echo "Sample E2E input directory is ready at ${INPUT_ROOT}."

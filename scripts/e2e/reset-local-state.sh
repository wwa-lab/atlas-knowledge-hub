#!/usr/bin/env bash
set -euo pipefail

OUTPUT_ROOT="${ATLAS_SAMPLE_OUTPUT:-samples/output/e2e}"

case "${OUTPUT_ROOT}" in
  samples/output/e2e|./samples/output/e2e)
    rm -rf "${OUTPUT_ROOT}"
    mkdir -p "${OUTPUT_ROOT}/markdown" "${OUTPUT_ROOT}/artifacts"
    ;;
  *)
    echo "Refusing to reset non-sample output root: ${OUTPUT_ROOT}" >&2
    exit 1
    ;;
esac

echo "Reset local E2E output at ${OUTPUT_ROOT}."

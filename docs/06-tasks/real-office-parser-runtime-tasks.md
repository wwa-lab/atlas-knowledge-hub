# Tasks: Real Office Parser Runtime

## Status

Accepted and implemented. User acceptance was recorded in chat on 2026-07-05 before product code changes.

## Task Checklist

| ID | Task | Requirements | Verification |
|---|---|---|---|
| T-REAL-OFFICE-PARSER-RUNTIME-001 | Record user acceptance of this bilingual SDD before product code changes. | REQ-REAL-OFFICE-PARSER-RUNTIME-001 | Traceability contains accepted gate before implementation. |
| T-REAL-OFFICE-PARSER-RUNTIME-002 | Add adapter-internal runtime executor abstraction with 120 second timeout and 65536 byte captured-output limit. | REQ-REAL-OFFICE-PARSER-RUNTIME-004, REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Unit tests cover success, timeout, oversized output, and safe failure using fake executor. |
| T-REAL-OFFICE-PARSER-RUNTIME-003 | Implement configured `trinity-office` runtime path behind `ConverterAdapter`, replacing throw-only behavior without changing product service callers. | REQ-REAL-OFFICE-PARSER-RUNTIME-002, REQ-REAL-OFFICE-PARSER-RUNTIME-010 | Converter adapter contract/service tests pass with fake runtime output. |
| T-REAL-OFFICE-PARSER-RUNTIME-004 | Implement configured `document-normalize` runtime path behind `ParserAdapter`, replacing throw-only behavior without changing product service callers. | REQ-REAL-OFFICE-PARSER-RUNTIME-003, REQ-REAL-OFFICE-PARSER-RUNTIME-011, REQ-REAL-OFFICE-PARSER-RUNTIME-012 | Parser adapter contract/service tests pass with fake runtime manifest. |
| T-REAL-OFFICE-PARSER-RUNTIME-005 | Make runtime selection explicit and deterministic when mock and configured adapters coexist. | REQ-REAL-OFFICE-PARSER-RUNTIME-005, REQ-REAL-OFFICE-PARSER-RUNTIME-006 | Registry tests prove default CI path remains mock-safe and configured mode is opt-in. |
| T-REAL-OFFICE-PARSER-RUNTIME-006 | Extend capability masking so runtime config exposes only status-only summaries. | REQ-REAL-OFFICE-PARSER-RUNTIME-005, REQ-REAL-OFFICE-PARSER-RUNTIME-009 | API contract tests prove no raw command path, endpoint, host, token, password, or private path appears. |
| T-REAL-OFFICE-PARSER-RUNTIME-007 | Validate and sanitize runtime outputs before persistence. | REQ-REAL-OFFICE-PARSER-RUNTIME-008, REQ-REAL-OFFICE-PARSER-RUNTIME-009, REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Tests cover absolute path, traversal, URI path, hostname, stack trace, secret-like output, invalid confidence, and duplicate chunk ids. |
| T-REAL-OFFICE-PARSER-RUNTIME-008 | Preserve trust boundaries and prove no Wiki publish, approval, graph, Ask, OCR, or LLM side effects occur. | REQ-REAL-OFFICE-PARSER-RUNTIME-012, REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Focused tests assert review status remains review-required and no downstream tables/services are touched. |
| T-REAL-OFFICE-PARSER-RUNTIME-009 | Update adapter seam guard for the allowed runtime implementation scope and product-layer bans. | REQ-REAL-OFFICE-PARSER-RUNTIME-004, REQ-REAL-OFFICE-PARSER-RUNTIME-015 | `cd backend && mvn -Dtest=AdapterSeamGuardTest test` plus negative scans over non-adapter product layers. |
| T-REAL-OFFICE-PARSER-RUNTIME-010 | Add optional runtime smoke tests that self-skip when approved runtime config is absent. | REQ-REAL-OFFICE-PARSER-RUNTIME-006, REQ-REAL-OFFICE-PARSER-RUNTIME-007 | Normal `mvn verify` passes without binaries; opt-in smoke emits skipped/pass evidence without raw paths. |
| T-REAL-OFFICE-PARSER-RUNTIME-011 | Run full backend verification and safety scans. | REQ-REAL-OFFICE-PARSER-RUNTIME-006 to 015 | `cd backend && mvn verify`, `git diff --check`, focused secret/private-path scan, focused network/dependency scan. |
| T-REAL-OFFICE-PARSER-RUNTIME-012 | Update traceability, roadmap, and residual risk notes after implementation. | REQ-REAL-OFFICE-PARSER-RUNTIME-001, REQ-REAL-OFFICE-PARSER-RUNTIME-016 | Context files list changed docs/code, verification evidence, skipped checks, and next slice. |

## Implementation Order

1. Complete acceptance gate and reread this task file plus the spec.
2. Implement runtime executor and fake test harness.
3. Implement configured converter path.
4. Implement configured parser path.
5. Harden capability masking and sanitizer coverage.
6. Update seam guard and optional smoke test behavior.
7. Run verification and update traceability/roadmap.

## Implementation Result

All tasks T-REAL-OFFICE-PARSER-RUNTIME-001 through T-REAL-OFFICE-PARSER-RUNTIME-012 are complete.

Evidence:

- Runtime executor abstraction added under `backend/src/main/java/com/atlas/metadata/adapter/runtime/`.
- `TrinityOfficeConverterAdapter` and `DocumentNormalizeParserAdapter` now execute configured runtime mode behind existing adapter contracts.
- `ConverterAdapterRegistry` and `ParserAdapterRegistry` resolve `mock` and `configured` modes deterministically when keys overlap.
- Capability masking exposes only status-only config values.
- Runtime outputs are bounded, sanitized, JSON-translated, and path/confidence-checked before product-facing results are returned.
- Adapter seam guard allows `ProcessBuilder` only in adapter/runtime scope and continues to block product-layer references.
- Optional runtime smoke IT self-skips unless `ATLAS_RUNTIME_SMOKE_ENABLED=true` and approved command env vars are present.
- Backend verification passed with `mvn verify`.
- Frontend checks were not run because no frontend files were changed.

## Required Verification Commands After Implementation

```bash
cd backend && mvn verify
cd backend && mvn -Dtest=AdapterSeamGuardTest test
git diff --check
```

Also run focused secret/private-path and network/dependency scans over changed files. Frontend checks are not required unless implementation touches frontend files.

## Constraints

- No product code before SDD acceptance.
- No default CI dependency on real `trinity-office` or `document-normalize`.
- No external cloud calls.
- No real company documents, raw logs, private paths, credentials, or internal hostnames in repository files.
- Runtime names and command/process APIs are allowed only inside adapter/runtime implementation scope and tests.
- Generated content remains review-required; this slice does not publish Wiki pages or update Ask/Graph.

## Recommended Codex Handoff After Acceptance

```text
Implement the real-office-parser-runtime slice strictly against docs/03-spec/real-office-parser-runtime-spec.md and docs/06-tasks/real-office-parser-runtime-tasks.md: complete every task in ID order, respect adapter/runtime boundaries, keep normal CI mock-safe, do not expand into Wiki publish/Graph/Ask/OCR/LLM/RBAC, and stop if implementation would diverge from the accepted spec.
```

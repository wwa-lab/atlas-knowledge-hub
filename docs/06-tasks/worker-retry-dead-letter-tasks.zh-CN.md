# 任务：worker-retry-dead-letter

状态：已实现并通过验证
最后更新：2026-07-07

## Workstreams

- SDD gate and manifest confirmation。
- Backend retry/dead-letter domain、persistence、API 与 tests。
- Frontend Processing Center operations UI、API bindings 与 tests。
- Roadmap、traceability、closeout、scans、commit 与 push。

## Task Details

### T-WORKER-RETRY-DEAD-LETTER-001: Confirm SDD and execution manifest [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-013
- Spec: FR-WORKER-RETRY-DEAD-LETTER-013
- Owner: docs
- Priority: Must
- Verification: `npm run agent:check-sdd -- --slice worker-retry-dead-letter --require-api-guide --report docs/00-context/worker-retry-dead-letter-sdd-completion-report.md`

### T-WORKER-RETRY-DEAD-LETTER-002: Add backend retry/dead-letter domain model and migration [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-001, REQ-WORKER-RETRY-DEAD-LETTER-002, REQ-WORKER-RETRY-DEAD-LETTER-006, REQ-WORKER-RETRY-DEAD-LETTER-008
- Spec: FR-WORKER-RETRY-DEAD-LETTER-001, FR-WORKER-RETRY-DEAD-LETTER-002, FR-WORKER-RETRY-DEAD-LETTER-006, FR-WORKER-RETRY-DEAD-LETTER-008
- Owner: backend
- Priority: Must
- Verification: backend compile and persistence/API tests。

### T-WORKER-RETRY-DEAD-LETTER-003: Implement deterministic local retry policy and transitions [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-003, REQ-WORKER-RETRY-DEAD-LETTER-004, REQ-WORKER-RETRY-DEAD-LETTER-005
- Spec: FR-WORKER-RETRY-DEAD-LETTER-003, FR-WORKER-RETRY-DEAD-LETTER-004, FR-WORKER-RETRY-DEAD-LETTER-005
- Owner: backend
- Priority: Must
- Verification: service tests for retryable failure、exhaustion 与 non-retryable failure。

### T-WORKER-RETRY-DEAD-LETTER-004: Implement safe error snapshot and source trace preservation [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-007, REQ-WORKER-RETRY-DEAD-LETTER-008
- Spec: FR-WORKER-RETRY-DEAD-LETTER-007, FR-WORKER-RETRY-DEAD-LETTER-008
- Owner: backend
- Priority: Must
- Verification: tests assert redaction and source trace copying to job, attempt, and dead-letter records。

### T-WORKER-RETRY-DEAD-LETTER-005: Implement worker recovery APIs [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-009, REQ-WORKER-RETRY-DEAD-LETTER-010
- Spec: FR-WORKER-RETRY-DEAD-LETTER-009, FR-WORKER-RETRY-DEAD-LETTER-010
- Owner: backend
- Priority: Must
- Verification: API contract tests for list/detail/manual retry/acknowledge/not found/conflict。

### T-WORKER-RETRY-DEAD-LETTER-006: Add backend tests [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-012
- Spec: FR-WORKER-RETRY-DEAD-LETTER-012
- Owner: backend
- Priority: Must
- Verification: `cd backend && mvn verify`

### T-WORKER-RETRY-DEAD-LETTER-007: Add frontend types and API bindings [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-011, REQ-WORKER-RETRY-DEAD-LETTER-012
- Spec: FR-WORKER-RETRY-DEAD-LETTER-011, FR-WORKER-RETRY-DEAD-LETTER-012
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run typecheck`

### T-WORKER-RETRY-DEAD-LETTER-008: Add Processing Center failed/dead-letter UI and tests [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-011, REQ-WORKER-RETRY-DEAD-LETTER-012
- Spec: FR-WORKER-RETRY-DEAD-LETTER-011, FR-WORKER-RETRY-DEAD-LETTER-012
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run test`; `cd frontend && npm run build`

### T-WORKER-RETRY-DEAD-LETTER-009: Update evidence, roadmap, scans, closeout, commit, and push [Done]
- Requirements: REQ-WORKER-RETRY-DEAD-LETTER-013
- Spec: FR-WORKER-RETRY-DEAD-LETTER-013
- Owner: full-stack
- Priority: Must
- Verification: `git status --short`; `git diff --check`; focused secret/private-path/network dependency scan; `npm run agent:closeout`; commit `feat: add worker retry and dead letter handling`; push to `develop-leo`。

## Dependency Plan

Critical path: T-WORKER-RETRY-DEAD-LETTER-001 -> T-WORKER-RETRY-DEAD-LETTER-002 -> T-WORKER-RETRY-DEAD-LETTER-003 -> T-WORKER-RETRY-DEAD-LETTER-004 -> T-WORKER-RETRY-DEAD-LETTER-005 -> T-WORKER-RETRY-DEAD-LETTER-006 -> T-WORKER-RETRY-DEAD-LETTER-007 -> T-WORKER-RETRY-DEAD-LETTER-008 -> T-WORKER-RETRY-DEAD-LETTER-009。

## Risks

- Local metadata 不提供 production queue recovery。
- Manual retry 只记录安全恢复基础，不 dispatch 真实工作。
- Operator action metadata 不是 production audit。

## Open Questions

预授权目标范围内无阻塞问题。

## Implementation Evidence

- Backend domain、migration、service、API 与 focused tests 已实现。
- Frontend API bindings、Processing Center dead-letter panel 与 UI flow test 已实现。
- Focused backend test passed: `cd backend && mvn -Dtest=WorkerJobServiceTest,WorkerRetryDeadLetterApiContractIT test`。
- Frontend typecheck and test passed: `cd frontend && npm run typecheck`；`cd frontend && npm run test`。
- Full verification passed: SDD gate、`cd backend && mvn verify`、`cd frontend && npm run typecheck && npm run test && npm run build`、`git diff --check`、focused scans 与 `npm run agent:closeout`。

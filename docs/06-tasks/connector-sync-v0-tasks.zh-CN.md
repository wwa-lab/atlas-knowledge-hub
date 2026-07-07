# 任务：connector-sync-v0

状态：本地实现与验证已完成
最后更新：2026-07-07

## Workstreams

- SDD gate 和 manifest confirmation。
- Backend connector adapter boundary、persistence、API 和 tests。
- Frontend connector sync UI/API binding 和 tests。
- Roadmap、traceability、closeout、scans、commit 和 push。

## Task Details

### T-CONNECTOR-SYNC-V0-001：确认 SDD 和 execution manifest
- Requirements: REQ-CONNECTOR-SYNC-V0-012
- Spec: FR-CONNECTOR-SYNC-V0-012
- Owner: docs
- Priority: Must
- Verification: `npm run agent:check-sdd -- --slice connector-sync-v0 --require-api-guide --report docs/00-context/connector-sync-v0-sdd-completion-report.md`
- 状态：Done。SDD gate 已于 2026-07-07 通过。

### T-CONNECTOR-SYNC-V0-002：新增 connector domain model 和 Flyway migration
- Requirements: REQ-CONNECTOR-SYNC-V0-002, REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006
- Spec: FR-CONNECTOR-SYNC-V0-002, FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-004, FR-CONNECTOR-SYNC-V0-005, FR-CONNECTOR-SYNC-V0-006
- Owner: backend
- Priority: Must
- Verification: backend compile 和 repository/integration tests。
- 状态：Done。Backend compile 和完整 `mvn verify` 已于 2026-07-07 通过。

### T-CONNECTOR-SYNC-V0-003：实现 connector adapter boundary 和 mock/local fixture adapter
- Requirements: REQ-CONNECTOR-SYNC-V0-001, REQ-CONNECTOR-SYNC-V0-009
- Spec: FR-CONNECTOR-SYNC-V0-001, FR-CONNECTOR-SYNC-V0-009
- Owner: backend
- Priority: Must
- Verification: unit tests 断言 adapter output 确定性，且不暴露真实 provider fields。
- 状态：Done。`MockLocalFixtureConnectorAdapterTest` 已在 `mvn verify` 中通过。

### T-CONNECTOR-SYNC-V0-004：实现 connector sync service 和 deterministic transitions
- Requirements: REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006
- Spec: FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-004, FR-CONNECTOR-SYNC-V0-005, FR-CONNECTOR-SYNC-V0-006
- Owner: backend
- Priority: Must
- Verification: unit/integration tests 覆盖 QUEUED -> RUNNING -> REVIEW_REQUIRED、FAILED、item persistence 和 review-required artifacts。
- 状态：代码已完成并由 connector API contract test 覆盖；完整 backend verify 已通过。

### T-CONNECTOR-SYNC-V0-005：实现 connector sync API contracts
- Requirements: REQ-CONNECTOR-SYNC-V0-002, REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-007
- Spec: FR-CONNECTOR-SYNC-V0-002, FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-007
- Owner: backend
- Priority: Must
- Verification: API contract tests 覆盖 definition list、create job/run、get run、list items、not found、validation 和 safe redaction。
- 状态：已在 `ConnectorSyncV0ApiContractIT` 中完成；完整 backend verify 已通过。

### T-CONNECTOR-SYNC-V0-006：新增后端测试
- Requirements: REQ-CONNECTOR-SYNC-V0-010
- Spec: FR-CONNECTOR-SYNC-V0-010
- Owner: backend
- Priority: Must
- Verification: `cd backend && mvn verify`
- 状态：Done。`cd backend && mvn verify` 已于 2026-07-07 通过。

### T-CONNECTOR-SYNC-V0-007：新增前端 types 和 API bindings
- Requirements: REQ-CONNECTOR-SYNC-V0-008, REQ-CONNECTOR-SYNC-V0-011
- Spec: FR-CONNECTOR-SYNC-V0-008, FR-CONNECTOR-SYNC-V0-011
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run typecheck`
- 状态：Done。Typecheck 已于 2026-07-07 通过。

### T-CONNECTOR-SYNC-V0-008：新增前端 connector sync UI 和 tests
- Requirements: REQ-CONNECTOR-SYNC-V0-008, REQ-CONNECTOR-SYNC-V0-011
- Spec: FR-CONNECTOR-SYNC-V0-008, FR-CONNECTOR-SYNC-V0-011
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run test`; `cd frontend && npm run build`
- 状态：Done。Frontend tests 已通过（3 files, 22 tests），build 已于 2026-07-07 通过。

### T-CONNECTOR-SYNC-V0-009：更新 evidence、运行 closeout、扫描、commit 和 push
- Requirements: REQ-CONNECTOR-SYNC-V0-012
- Spec: FR-CONNECTOR-SYNC-V0-012
- Owner: full-stack
- Priority: Must
- Verification: `npm run agent:closeout`; `git diff --check`; secret/private path/network dependency scan；commit `feat: add connector sync v0`；push 到 `develop-leo`。
- 状态：验证完成。`git diff --check` 与 `npm run agent:closeout` 已通过；commit/push 等待 scoped staging。

## Dependency Plan

Critical path: T-CONNECTOR-SYNC-V0-001 -> T-CONNECTOR-SYNC-V0-002 -> T-CONNECTOR-SYNC-V0-003 -> T-CONNECTOR-SYNC-V0-004 -> T-CONNECTOR-SYNC-V0-005 -> T-CONNECTOR-SYNC-V0-006 -> T-CONNECTOR-SYNC-V0-007 -> T-CONNECTOR-SYNC-V0-008 -> T-CONNECTOR-SYNC-V0-009。

## 风险

- v0 不证明真实 provider behavior。
- 同步执行不是 production worker recovery。
- Review-required artifacts 是 metadata handoffs，不是 approved Wiki pages。

## 开放问题

在当前 goal 的预授权边界内没有阻塞问题。

# 实现任务：Ask Session Citations

## 概述

实现 Ask session persistence、session read APIs、更丰富的 citation snapshot fields、frontend session/citation display、测试与 closeout documentation。

## Task Details

### T-ASK-SESSION-CITATIONS-001: Accept SDD Gate
- **Objective:** 确认完整双语 SDD set 完成，且在自主 prompt 预授权边界内。
- **Dependencies:** None。
- **Owner type:** docs
- **Priority:** Must
- **Verification:** `npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide`

### T-ASK-SESSION-CITATIONS-002: Add Persistence Model
- **Objective:** 新增 `AskSession`、`AskRun.session_id`、`AskEvidence` citation metadata fields、repositories，以及 Flyway migration `V15__ask_session_citations.sql`。
- **Dependencies:** T-ASK-SESSION-CITATIONS-001
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=AskDomainInvariantTest,AskStateImmutabilityTest test`

### T-ASK-SESSION-CITATIONS-003: Extend Ask Service
- **Objective:** 在 Ask 创建时 create/reuse sessions，派生 safe citation labels/status，列出 sessions，并读取 session detail。
- **Dependencies:** T-ASK-SESSION-CITATIONS-002
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=AskServiceTest,AskRequestValidationTest test`

### T-ASK-SESSION-CITATIONS-004: Extend API DTOs And Controller
- **Objective:** 增加 session request fields、session response DTOs、更丰富 citation DTO fields，以及 session endpoints。
- **Dependencies:** T-ASK-SESSION-CITATIONS-003
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dit.test=AskSessionCitationsApiContractIT verify`

### T-ASK-SESSION-CITATIONS-005: Preserve Safe Evidence Boundaries
- **Objective:** 确保 citations 只暴露 safe labels 和 review eligibility，绝不暴露 raw source content、private paths、endpoints、secrets 或 stack traces。
- **Dependencies:** T-ASK-SESSION-CITATIONS-003
- **Owner type:** security
- **Priority:** Must
- **Verification:** 对 diff 做 secret/private-path/network focused scan。

### T-ASK-SESSION-CITATIONS-006: Update Frontend Types And API Client
- **Objective:** 增加 Ask sessions 与更丰富 citation fields 的 TypeScript types 和 client helpers。
- **Dependencies:** T-ASK-SESSION-CITATIONS-004
- **Owner type:** frontend
- **Priority:** Must
- **Verification:** `cd frontend && npm run typecheck`

### T-ASK-SESSION-CITATIONS-007: Render Session History And Citation Detail
- **Objective:** 更新 Trusted Ask UI 展示 recent sessions、selected session answer history 和 citation metadata，同时保持现有 answer behavior。
- **Dependencies:** T-ASK-SESSION-CITATIONS-006
- **Owner type:** frontend
- **Priority:** Must
- **Verification:** `cd frontend && npm run test`

### T-ASK-SESSION-CITATIONS-008: Update Frontend E2E Fixtures
- **Objective:** 扩展 API mocks 和 E2E coverage，覆盖 session list/detail 与 citation fields。
- **Dependencies:** T-ASK-SESSION-CITATIONS-007
- **Owner type:** QA
- **Priority:** Should
- **Verification:** `cd frontend && npm run build`

### T-ASK-SESSION-CITATIONS-009: Run Full Verification
- **Objective:** 运行 required backend、frontend、closeout、diff 与 safety checks。
- **Dependencies:** T-ASK-SESSION-CITATIONS-002 through T-ASK-SESSION-CITATIONS-008
- **Owner type:** QA
- **Priority:** Must
- **Verification:** `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`

### T-ASK-SESSION-CITATIONS-010: Update Traceability And Roadmaps
- **Objective:** 记录 completed tasks、verification evidence、residual risks 与 Wave 4 status。
- **Dependencies:** T-ASK-SESSION-CITATIONS-009
- **Owner type:** docs
- **Priority:** Must
- **Verification:** traceability 和 roadmap files 包含最终 evidence。

## Dependency Plan

Critical path: T-ASK-SESSION-CITATIONS-001 -> T-ASK-SESSION-CITATIONS-002 -> T-ASK-SESSION-CITATIONS-003 -> T-ASK-SESSION-CITATIONS-004 -> T-ASK-SESSION-CITATIONS-006 -> T-ASK-SESSION-CITATIONS-007 -> T-ASK-SESSION-CITATIONS-009 -> T-ASK-SESSION-CITATIONS-010。

## Risks / Blockers

- 如果实现需要改变 provider/model、auth/RBAC、audit、secret 或 rate-limit 语义，必须停止。
- 如果 citation detail 会暴露 raw source content 或 unsafe labels，必须停止。

## Open Questions

- None。

## Completion Evidence

| Task | Status | Evidence |
|---|---|---|
| T-ASK-SESSION-CITATIONS-001 | Complete | SDD gate 已通过：`npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide`。 |
| T-ASK-SESSION-CITATIONS-002 | Complete | 已新增 session/citation persistence model 与 `V15__ask_session_citations.sql`；由 `mvn verify` 覆盖。 |
| T-ASK-SESSION-CITATIONS-003 | Complete | `AskServiceTest` 覆盖 session history 与 review-aware citation status。 |
| T-ASK-SESSION-CITATIONS-004 | Complete | `AskApiContractIT` 覆盖 create/get run、session list/detail 与 citation fields。 |
| T-ASK-SESSION-CITATIONS-005 | Complete | Citation labels/locators 使用 sanitized bounded fields；最终 diff review 纳入 safety scan。 |
| T-ASK-SESSION-CITATIONS-006 | Complete | `npm run typecheck` 通过。 |
| T-ASK-SESSION-CITATIONS-007 | Complete | `npm run test` 通过，并覆盖 Ask UI contract。 |
| T-ASK-SESSION-CITATIONS-008 | Complete | Frontend P0 mocks 已加入 session list/detail 与 richer citation fields；`npm run build` 通过。 |
| T-ASK-SESSION-CITATIONS-009 | Complete | Backend 与 frontend verification 通过；closeout 与 diff checks 已记录到 traceability。 |
| T-ASK-SESSION-CITATIONS-010 | Complete | Traceability 与 roadmap/status docs 已更新。 |

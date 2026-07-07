# 任务：Answer Review Governance

最后更新：2026-07-07

## 概览

按 task ID 顺序实现已接受 SDD 的 Ask answer governance。变更范围仅限 Ask answer status、metadata、UI display、tests 和 traceability。

Implementation status：已按接受的 prototype 范围完成。Backend、frontend 与 documentation evidence 记录在 `docs/00-context/answer-review-governance-traceability.zh-CN.md`。

## Task Breakdown

### T-ANSWER-REVIEW-GOVERNANCE-001：添加 Answer Governance Domain Model

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-001, REQ-ANSWER-REVIEW-GOVERNANCE-002
- Scope: 添加 `AnswerReviewStatus`、扩展 `AskRun`、更新 generated status 默认值。
- Verification: backend domain/unit tests。

### T-ANSWER-REVIEW-GOVERNANCE-002：添加 Flyway Persistence Metadata

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-003
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001
- Scope: 添加 migration，包含 status constraint、reason、reviewer、reviewed timestamp 和 index。
- Verification: `cd backend && mvn verify`。

### T-ANSWER-REVIEW-GOVERNANCE-003：添加 Review Action API

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-003, REQ-ANSWER-REVIEW-GOVERNANCE-004, REQ-ANSWER-REVIEW-GOVERNANCE-006
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001, T-ANSWER-REVIEW-GOVERNANCE-002
- Scope: 添加 request DTO、service method、controller route、validation、safe errors 和 response mapping。
- Verification: backend service 和 integration/API contract tests。

### T-ANSWER-REVIEW-GOVERNANCE-004：保留 Evidence 和既有 Ask Behavior

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-005, REQ-ANSWER-REVIEW-GOVERNANCE-008
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-003
- Scope: 确保 review updates 只更新 answer governance fields，既有 Ask create/read tests 继续通过。
- Verification: existing Ask tests plus new preservation assertions。

### T-ANSWER-REVIEW-GOVERNANCE-005：更新 Frontend Types 和 API Client

- Status: Complete
- Owner type: frontend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-001, REQ-ANSWER-REVIEW-GOVERNANCE-007
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-003
- Scope: 添加 answer governance types、Ask response fields 和 review action API helper。
- Verification: `cd frontend && npm run typecheck`。

### T-ANSWER-REVIEW-GOVERNANCE-006：更新 Trusted Ask UI Display

- Status: Complete
- Owner type: frontend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-004, REQ-ANSWER-REVIEW-GOVERNANCE-007
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-005
- Scope: 显示 governance label、reason、reviewer、reusable hint 和 citations，避免暗示未审核答案可信。
- Verification: frontend unit tests。

### T-ANSWER-REVIEW-GOVERNANCE-007：更新 Traceability 和 Roadmaps

- Status: Complete；documentation edits 后 closeout gate 已通过
- Owner type: docs
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-008
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001 到 T-ANSWER-REVIEW-GOVERNANCE-006
- Scope: 更新 traceability、slice roadmap、repo status roadmap 和 verification evidence。
- Verification: SDD gate 和 closeout gate。

## Required Verification

- `npm run agent:check-sdd -- --slice answer-review-governance`
- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `npm run agent:closeout`
- `git diff --check`
- Focused changed-file secret/private-path/real-data scan。
- Focused changed-file network/dependency scan。

## SDD Skill Chain Evidence

Entry skill used: `atlas-sdd-generate-all`。Downstream skills applied：`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`、`review-doc-quality`、`architecture-review` 用于 architecture/API/persistence awareness。Review verdict：ready for implementation，无 blocking SDD issues。

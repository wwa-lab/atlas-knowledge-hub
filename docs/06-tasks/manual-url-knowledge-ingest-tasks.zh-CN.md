# 任务：manual-url-knowledge-ingest

## Overview

按 ID 顺序实现本切片。`docs/03-spec/manual-url-knowledge-ingest-spec.md` 是行为 source of truth。不要新增真实 URL fetch、crawl、connector sync、browser automation 或 production compliance 行为。

## Task List

### `T-MANUAL-URL-KNOWLEDGE-INGEST-001` 确认 SDD Gate 与 Manifest

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` through `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013`
- **Scope:** 确认 bilingual SDD artifacts、execution manifest、API guide 与 traceability 存在。
- **Verification:** `npm run agent:check-sdd -- --slice manual-url-knowledge-ingest --require-api-guide --report docs/00-context/manual-url-knowledge-ingest-sdd-completion-report.md`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-002` 添加 Backend Data Model 与 Migration

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005`
- **Scope:** 添加 `manual_url_source` entity/repository、status enums、DTOs、mapper 与 Flyway migration。用 URL values 扩展 `SourceKind` 与 `SourceType`。
- **Verification:** Backend compile 与 focused tests。

### `T-MANUAL-URL-KNOWLEDGE-INGEST-003` 实现 Validation 与 Service

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007`
- **Scope:** 实现 metadata-only URL validation、sanitization、duplicate handling、batch/file/chunk metadata creation 与 review-required defaults。
- **Verification:** Backend unit tests 覆盖 accepted 与 rejected URLs。

### `T-MANUAL-URL-KNOWLEDGE-INGEST-004` 添加 Backend API Contract

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`
- **Scope:** 添加 create/list/get controller endpoints，使用 `ApiEnvelope<T>`。
- **Verification:** Integration/API contract tests 覆盖 create/list/get。

### `T-MANUAL-URL-KNOWLEDGE-INGEST-005` 证明 Safe Errors 与 Redaction

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007`
- **Scope:** 测试必须证明 unsafe URL submissions 不回显 credential、query strings、fragments、private hosts、raw stack traces 或 private endpoint values。
- **Verification:** Integration tests 覆盖 unsafe URL cases。

### `T-MANUAL-URL-KNOWLEDGE-INGEST-006` 添加 Frontend Types、API Client 与 Mock Data

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009`
- **Scope:** 扩展 `frontend/src/types.ts`、`frontend/src/api.ts` 与 mock data，加入 manual URL source types 与 sample-safe fallback state。
- **Verification:** `cd frontend && npm run typecheck`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-007` 添加 Frontend UI 与 Tests

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010`
- **Scope:** 在 Knowledge Space 或 Processing Center surface 添加 manual URL form/status/source trace display。添加 Vitest 与 E2E 覆盖。
- **Verification:** `cd frontend && npm run test`, `cd frontend && npm run build`

### `T-MANUAL-URL-KNOWLEDGE-INGEST-008` Closeout、Roadmap 与 Safety Gates

- **Requirements:** `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010`
- **Scope:** 更新 traceability、slice roadmap、repo status roadmap 与 completion evidence。运行 closeout 与 hygiene scans。
- **Verification:** `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`。

## Dependency Plan

Critical path: `T-MANUAL-URL-KNOWLEDGE-INGEST-001` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-002` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-003` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-004` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-005` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-006` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-007` -> `T-MANUAL-URL-KNOWLEDGE-INGEST-008`。

## Stop Conditions

如实现需要真实 URL fetch、production connector credentials、auth/RBAC/audit/secret/rate-limit/provider semantic changes，或把 unreviewed URL content 作为 approved Wiki/Ask/Graph knowledge 暴露，则停止。

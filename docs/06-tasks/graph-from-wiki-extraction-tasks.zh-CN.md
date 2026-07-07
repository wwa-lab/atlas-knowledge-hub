# 实现任务：graph-from-wiki-extraction

## Task Details

### T-GRAPH-FROM-WIKI-EXTRACTION-001: Extend graph projection contracts
- Owner: backend
- Scope: 为 adapter contract 和 DTOs 增加 Wiki page descriptors 与 Wiki evidence fields。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-003, REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- Verification: backend unit compile 与 focused tests。

### T-GRAPH-FROM-WIKI-EXTRACTION-002: Implement Wiki eligibility filtering
- Owner: backend
- Scope: 加载 space 内所有 Wiki pages，选择 `APPROVED`/`PUBLISHED`、有 trace 且 confidence >= 0.800 的页面，并记录安全 skip reasons。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-001, REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- Verification: unit/API tests 覆盖 approved、published、review-required、low-confidence 和 missing-trace pages。

### T-GRAPH-FROM-WIKI-EXTRACTION-003: Implement deterministic Wiki extraction
- Owner: backend
- Scope: 从 eligible Wiki metadata 生成稳定 Wiki page、document、concept 与 relationship candidates。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-002, REQ-GRAPH-FROM-WIKI-EXTRACTION-003
- Verification: idempotency tests 与 stable ID assertions。

### T-GRAPH-FROM-WIKI-EXTRACTION-004: Persist Wiki-derived graph evidence
- Owner: backend
- Scope: 保存带 `evidence_wiki_page_ids` 的 nodes/edges，保留 chunk IDs，并统计 created vs updated records。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-004, REQ-GRAPH-FROM-WIKI-EXTRACTION-006
- Verification: repository/API contract tests。

### T-GRAPH-FROM-WIKI-EXTRACTION-005: Extend graph detail evidence response
- Owner: backend
- Scope: 返回安全 mixed `SOURCE_CHUNK` 和 `WIKI_PAGE` evidence references。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-005, REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: API contract test 断言 safe evidence 且无 raw unsafe strings。

### T-GRAPH-FROM-WIKI-EXTRACTION-006: Render Wiki-derived evidence in Vue Graph UI
- Owner: frontend
- Scope: 扩展 TypeScript evidence type 和 Graph detail panel rendering。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: `cd frontend && npm run typecheck`、component tests。

### T-GRAPH-FROM-WIKI-EXTRACTION-007: Add E2E coverage
- Owner: QA/frontend
- Scope: 覆盖 Graph tab evidence detail 展示 Wiki-derived evidence，同时现有 filters 继续工作。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-006, REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- Verification: `cd frontend && npm run test`、`cd frontend && npm run build`、E2E。

### T-GRAPH-FROM-WIKI-EXTRACTION-008: Closeout and docs
- Owner: full-stack
- Scope: 更新 traceability、roadmaps、status evidence，并运行全部 verification。
- Maps to: REQ-GRAPH-FROM-WIKI-EXTRACTION-008
- Verification: `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`。

## Sequencing

Critical path: T-GRAPH-FROM-WIKI-EXTRACTION-001 -> T-GRAPH-FROM-WIKI-EXTRACTION-002 -> T-GRAPH-FROM-WIKI-EXTRACTION-003 -> T-GRAPH-FROM-WIKI-EXTRACTION-004 -> T-GRAPH-FROM-WIKI-EXTRACTION-005 -> T-GRAPH-FROM-WIKI-EXTRACTION-006 -> T-GRAPH-FROM-WIKI-EXTRACTION-007 -> T-GRAPH-FROM-WIKI-EXTRACTION-008。

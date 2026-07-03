# Tasks: Parser Adapter

## Status

Implemented on 2026-07-03. T-PA-001 through T-PA-010 were executed against `docs/03-spec/parser-adapter-spec.md` with mock parser verification; the real parser runtime contract remains deferred.

## Source Design

- Spec: `docs/03-spec/parser-adapter-spec.md`
- Design: `docs/05-design/parser-adapter-design.md`
- API guide: `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model: `docs/04-architecture/parser-adapter-data-model.md`

## Constraints For Every Task

- Use mock/fake parser engines for automated tests.
- Keep parser execution behind product-facing adapter contracts.
- Do not call `document-normalize`, OCR engines, command runners, or outbound HTTP clients from controller/service/repository/domain layers.
- Do not introduce external network dependencies or cloud calls.
- Preserve source trace, confidence, and review status.
- Mask secrets, private paths, raw parser logs, hostnames, stack traces, and private endpoints.
- Use mock/sample metadata only.

## Workstreams

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-PA-001, T-PA-002 |
| Adapter contract | T-PA-003, T-PA-004 |
| Service behavior | T-PA-005, T-PA-006, T-PA-007 |
| API contract | T-PA-008 |
| Verification and guards | T-PA-009, T-PA-010 |

## Task Details

### T-PA-001: Add parser run domain model and migration

- **Maps to:** REQ-PA-004, REQ-PA-013, REQ-PA-014; spec sections "Parser Run", "Metadata Write-Back And Validation"; data model `parser_run`, `parser_file_result`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add parser run status enum, parser run entity, parser file result entity, repositories, and Flyway migration for parser execution evidence. Reuse existing `FileStatus`, `SourceType`, `ReviewStatus`; do not add new file statuses.
- **Constraints:** Adapter boundary; mock-only test data; source trace/review preservation; secret/path safety.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-PA-002: Add parser DTOs and mapping contracts

- **Maps to:** REQ-PA-003, REQ-PA-013; spec sections "Capability Metadata", "API / Interface Surface"; API guide response shapes.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-001
- **Scope:** Add parser capability, create-run request, run response, summary response, file result response, and chunk response mapping. All responses use `ApiEnvelope` through controllers.
- **Constraints:** Secret-masked capability summaries; no raw runtime details.
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-PA-003: Define parser adapter interface and capability model

- **Maps to:** REQ-PA-001, REQ-PA-002, REQ-PA-003; spec sections "Adapter Boundary", "Capability Metadata"; design "Parser Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-002
- **Scope:** Add product-facing parser adapter interface, capability record, request record, result record, and chunk result record. Include adapter key, safe status, supported input/output types, default marker, low-confidence threshold, and masked config summary.
- **Constraints:** Product concepts only; no direct parser engine call in interface; no network/client dependency.
- **Verification:**

```bash
cd backend && mvn -Dtest=ParserAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter
```

### T-PA-004: Add mock and configured parser adapter implementations

- **Maps to:** REQ-PA-002, REQ-PA-012; spec sections "Adapter Boundary", "Status Mapping And Failure Behavior"; design "Parser Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-003
- **Scope:** Add deterministic mock parser adapter for CI and a configured `document-normalize` adapter boundary that reports safe capability metadata. Real parser execution remains behind the adapter and may be a safe placeholder until runtime topology is decided.
- **Constraints:** Mock-only verification; no external network; no raw command/path/secret in capability response.
- **Verification:**

```bash
cd backend && mvn -Dtest=ParserAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-PA-005: Implement parser adapter registry and capability listing

- **Maps to:** REQ-PA-001, REQ-PA-003; spec sections "Adapter Boundary", "Capability Metadata"; API guide `GET /api/parser-adapters`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-003, T-PA-004
- **Scope:** Add registry resolution for explicit/default parser adapter keys and unavailable/misconfigured statuses. Add capability listing service behavior with masked config.
- **Constraints:** Adapter boundary; secret masking; no hardcoded single implementation as only future option.
- **Verification:**

```bash
cd backend && mvn -Dtest=ParserAdapterRegistryTest test
git diff --check
```

### T-PA-006: Implement parser run service and status mapping

- **Maps to:** REQ-PA-004, REQ-PA-005, REQ-PA-006, REQ-PA-007, REQ-PA-008, REQ-PA-013, REQ-PA-014; spec sections "Parser Run", "Status Mapping And Failure Behavior"; design "Parser Service".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-001, T-PA-003, T-PA-005
- **Scope:** Implement parser run creation, target eligibility filtering, active-run conflict handling, adapter execution, `MARKDOWN_GENERATED` / `LOW_CONFIDENCE` / `OCR_REQUIRED` / `FAILED` mapping, summary calculation, and safe adapter-fault handling.
- **Constraints:** Mock-engine execution in tests; review status remains `REVIEW_REQUIRED`; no OCR execution.
- **Verification:**

```bash
cd backend && mvn -Dtest=ParserServiceTest,ParserSummaryCalculatorTest test
git diff --check
```

### T-PA-007: Implement metadata write-back and source chunk persistence

- **Maps to:** REQ-PA-009, REQ-PA-010, REQ-PA-011, REQ-PA-014; spec sections "Markdown, Assets, And Chunks", "Metadata Write-Back And Validation"; design "Data Design".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-006
- **Scope:** Update file items with safe Markdown/assets paths, confidence, status, and safe error. Persist validated source chunks. Reject unsafe paths, unknown file ids, cross-batch results, invalid pages, duplicate chunk ids, and confidence outside `[0,1]`.
- **Constraints:** Preserve source trace, confidence, review status; secret/path masking; no `wiki_page` creation.
- **Verification:**

```bash
cd backend && mvn -Dtest=ParserDomainInvariantTest,ParserServiceTest test
git diff --check
```

### T-PA-008: Add parser REST API endpoints

- **Maps to:** REQ-PA-003, REQ-PA-004, REQ-PA-013; spec section "API / Interface Surface"; API guide all endpoints.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PA-005, T-PA-006, T-PA-007
- **Scope:** Add parser controller endpoints `GET /api/parser-adapters`, `POST /api/batches/{batchId}/parser-runs`, and `GET /api/parser-runs/{runId}`. Responses must use `ApiEnvelope` and match the API guide.
- **Constraints:** Internal-only; no auth/RBAC implementation; no raw parser output.
- **Verification:**

```bash
cd backend && mvn -Dit.test=ParserApiContractIT verify
git diff --check
```

### T-PA-009: Extend adapter seam and safety guards

- **Maps to:** REQ-PA-001, REQ-PA-008, REQ-PA-011, REQ-PA-012; spec sections "Adapter Boundary", "Metadata Write-Back And Validation"; design "Testing Considerations".
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-PA-003, T-PA-004, T-PA-008
- **Scope:** Extend guard tests so parser engine names, command runners, and outbound network clients are forbidden in non-adapter product layers. Add secret/private-path scans for parser implementation and docs.
- **Constraints:** No direct parser calls outside adapter; secret-masked output only.
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

### T-PA-010: Run final parser-adapter verification

- **Maps to:** REQ-PA-012, REQ-PA-013, REQ-PA-014; spec acceptance matrix AC-PA-01 through AC-PA-09.
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-PA-001 through T-PA-009
- **Scope:** Run the complete verification set, review the diff, and update traceability with evidence if implementation changes docs/status later.
- **Constraints:** Mock-only; adapter only; no external network/cloud calls from product code; secret-masked; trace/review preserved.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

## Dependency Plan

- Critical path: T-PA-001 -> T-PA-002 -> T-PA-003 -> T-PA-004 -> T-PA-005 -> T-PA-006 -> T-PA-007 -> T-PA-008 -> T-PA-009 -> T-PA-010
- Parallel opportunities after T-PA-003: adapter contract tests and DTO mapper tests may be built alongside service tests.

## Open Questions / Risks

- OQ-PA-001: Real parser runtime topology is deferred; mock contract must not depend on it.
- OQ-PA-002: `wiki_page` creation remains deferred to publish/review.
- OQ-PA-003: Low-confidence threshold defaults to `< 0.800`; implemented as specified and can be changed in a future product decision.

# Tasks: Ask RAG

## Status

Draft implementation checklist generated from `docs/05-design/ask-rag-design.md` using `design-to-tasks`.

## Overview

Implement `ask-rag` strictly against `docs/03-spec/ask-rag-spec.md`. Complete tasks in ID order unless dependencies allow parallel work. Do not implement external provider calls, streaming, answer publication, graph extraction, or production SSO/RBAC beyond the accepted scope.

## Workstreams

- Backend persistence and API.
- Adapter-bound Ask orchestration.
- Frontend Trusted Ask states.
- Verification, seam guards, and documentation close-out.

## Task Details

### T-ASKRAG-001: Add Ask Domain Model And Migration

- **Maps to:** REQ-ASKRAG-005, REQ-ASKRAG-006, REQ-ASKRAG-009; spec sections `Evidence And Audit`, `State Model`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add Ask run/evidence domain records, statuses, repositories, and Flyway migration for safe audit evidence. Store references and safe summaries only.
- **Constraints:** No raw prompt, raw source text, raw vectors, secrets, private paths, provider payloads, or stack traces.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskDomainInvariantTest test
  cd backend && mvn verify -DskipITs=false
  ```

### T-ASKRAG-002: Add Ask DTOs And API Contract Mapping

- **Maps to:** REQ-ASKRAG-003, REQ-ASKRAG-007, REQ-ASKRAG-008; spec sections `Ask Request`, `API / Interface Surface`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-001
- **Scope:** Add request/response DTOs, envelope mapping, validation annotations/helpers, and safe evidence response shape.
- **Constraints:** Responses use `ApiEnvelope`; errors are user-safe; filters stay space-scoped.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskRequestValidationTest test
  ```

### T-ASKRAG-003: Implement Ask Service Orchestration

- **Maps to:** REQ-ASKRAG-001, REQ-ASKRAG-002, REQ-ASKRAG-004, REQ-ASKRAG-012, REQ-ASKRAG-013; spec sections `Retrieval Policy`, `Answer Generation`, `Failure Behavior`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-001, T-ASKRAG-002
- **Scope:** Validate scope, query vector evidence through product-facing contracts, defensively enforce review policy on returned evidence, skip model generation with a safe no-answer message when no approved evidence exists, call model service through model-adapter contract when evidence exists, persist final status.
- **Constraints:** Mock adapters only in tests; product service must not call provider SDKs, vector DBs, CLIs, or outbound HTTP clients directly.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskServiceTest,AskSummaryCalculatorTest test
  ```

### T-ASKRAG-004: Add Ask Controller And API Contract Tests

- **Maps to:** REQ-ASKRAG-007, REQ-ASKRAG-008, REQ-ASKRAG-012; spec sections `API / Interface Surface`, `Failure Behavior`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-002, T-ASKRAG-003
- **Scope:** Add `POST /api/spaces/{spaceId}/ask` and `GET /api/ask-runs/{runId}` with validation, envelope responses, and safe errors.
- **Constraints:** No adapter execution after validation failure; no raw internals in error bodies.
- **Verification:**
  ```bash
  cd backend && mvn -Dit.test=AskApiContractIT verify
  ```

### T-ASKRAG-005: Preserve Source State Immutability

- **Maps to:** REQ-ASKRAG-006, REQ-ASKRAG-009; spec sections `Evidence And Audit`, `Acceptance Matrix`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-003
- **Scope:** Add integration coverage proving Ask does not mutate source chunk, file item, Wiki page, graph, or review status.
- **Constraints:** Generated answer remains `REVIEW_REQUIRED`.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskStateImmutabilityTest test
  ```

### T-ASKRAG-006: Implement Frontend Trusted Ask State Mapping

- **Maps to:** REQ-ASKRAG-001, REQ-ASKRAG-003, REQ-ASKRAG-010, REQ-ASKRAG-013; spec sections `UI Behavior`.
- **Owner type:** frontend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-002
- **Scope:** Add data-driven UI state for loading, answered, no-evidence, review-required warning, and safe error while preserving FE baseline layout.
- **Constraints:** Mock/sample data only; no new external network dependency; no real credentials.
- **Verification:**
  ```bash
  cd frontend && npm run typecheck
  cd frontend && npm run test
  cd frontend && npm run build
  ```

### T-ASKRAG-007: Add Ask E2E Coverage

- **Maps to:** REQ-ASKRAG-010, REQ-ASKRAG-011, REQ-ASKRAG-014; spec sections `UI Behavior`, `Acceptance Matrix`.
- **Owner type:** QA/frontend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-006
- **Scope:** Cover answer success with evidence, no-approved-evidence refusal, and review-required evidence warning in Playwright.
- **Constraints:** E2E uses mock data and no external network calls.
- **Verification:**
  ```bash
  cd frontend && npm run e2e
  ```

### T-ASKRAG-008: Add Adapter Seam And Data Safety Guards

- **Maps to:** REQ-ASKRAG-004, REQ-ASKRAG-011, REQ-ASKRAG-014; spec sections `Constraints`, `Acceptance Matrix`.
- **Owner type:** security/backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-003
- **Scope:** Extend seam guard tests to block direct model/vector provider references outside adapter packages and scan for unsafe secrets/private paths.
- **Constraints:** Adapter packages may contain safe labels only under guard coverage; no outbound clients in product layers.
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AdapterSeamGuardTest test
  ! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|pgvector|Milvus|Qdrant|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
  ! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src frontend/src docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md
  ```

### T-ASKRAG-009: Run Full Phase 4 Verification

- **Maps to:** REQ-ASKRAG-014; spec sections `Acceptance Matrix`.
- **Owner type:** QA
- **Priority:** Must
- **Dependencies:** T-ASKRAG-004, T-ASKRAG-005, T-ASKRAG-007, T-ASKRAG-008
- **Scope:** Run final backend, frontend, E2E, diff hygiene, dependency/network, and secret scans.
- **Constraints:** Name any skipped check with reason; do not imply unrun checks passed.
- **Verification:**
  ```bash
  cd backend && mvn verify
  cd frontend && npm run typecheck
  cd frontend && npm run test
  cd frontend && npm run build
  cd frontend && npm run e2e
  git diff --check
  rg -n "https?://|fetch\\(|XMLHttpRequest|WebSocket|EventSource" frontend/src backend/src/main/java || true
  ```

### T-ASKRAG-010: Update Traceability And Close Out

- **Maps to:** REQ-ASKRAG-014; spec sections `Acceptance Matrix`.
- **Owner type:** docs/QA
- **Priority:** Must
- **Dependencies:** T-ASKRAG-009
- **Scope:** Update `docs/00-context/ask-rag-traceability.md` with implementation evidence, verification results, residual risks, and any lessons learned.
- **Constraints:** Maintain English and Chinese document parity.
- **Verification:**
  ```bash
  git diff --check
  for f in docs/00-context/ask-rag-traceability.md docs/00-context/ask-rag-traceability.zh-CN.md; do test -s "$f" || exit 1; done
  ```

## Dependency Plan

Critical path: T-ASKRAG-001 -> T-ASKRAG-002 -> T-ASKRAG-003 -> T-ASKRAG-004 -> T-ASKRAG-009 -> T-ASKRAG-010.

Parallel work: T-ASKRAG-005 can follow T-ASKRAG-003; T-ASKRAG-006 can start after DTO shape is stable; T-ASKRAG-008 can run once orchestration exists.

## Open Questions

- OQ-ASKRAG-001: Role policy for review-required evidence.
- OQ-ASKRAG-002: Future review queue integration.
- OQ-ASKRAG-003: Reranking timing.

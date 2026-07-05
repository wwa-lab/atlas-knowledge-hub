# 任务：Ask RAG

## 状态

草稿实现清单，由 `docs/05-design/ask-rag-design.md` 通过 `design-to-tasks` 生成。

## 概览

严格依据 `docs/03-spec/ask-rag-spec.md` 实现 `ask-rag`。按 ID 顺序完成任务，除非依赖允许并行。不得实现外部 provider 调用、流式输出、answer publication、graph extraction，或超出范围的生产 SSO/RBAC。

## 工作流

- Backend persistence and API。
- Adapter-bound Ask orchestration。
- Frontend Trusted Ask states。
- Verification、seam guards 和 documentation close-out。

## 任务详情

### T-ASKRAG-001：添加 Ask Domain Model And Migration

- **映射到:** REQ-ASKRAG-005, REQ-ASKRAG-006, REQ-ASKRAG-009；spec sections `Evidence And Audit`, `State Model`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** 添加 Ask run/evidence domain records、statuses、repositories 和 Flyway migration，用于安全 audit evidence。只存 references 与 safe summaries。
- **Constraints:** 不保存 raw prompt、raw source text、raw vectors、secrets、private paths、provider payloads 或 stack traces。
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskDomainInvariantTest test
  cd backend && mvn verify -DskipITs=false
  ```

### T-ASKRAG-002：添加 Ask DTOs And API Contract Mapping

- **映射到:** REQ-ASKRAG-003, REQ-ASKRAG-007, REQ-ASKRAG-008；spec sections `Ask Request`, `API / Interface Surface`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-001
- **Scope:** 添加 request/response DTOs、envelope mapping、validation annotations/helpers 和 safe evidence response shape。
- **Constraints:** 响应使用 `ApiEnvelope`；错误用户安全；filters 限定在 space 内。
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskRequestValidationTest test
  ```

### T-ASKRAG-003：实现 Ask Service Orchestration

- **映射到:** REQ-ASKRAG-001, REQ-ASKRAG-002, REQ-ASKRAG-004, REQ-ASKRAG-012, REQ-ASKRAG-013；spec sections `Retrieval Policy`, `Answer Generation`, `Failure Behavior`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-001, T-ASKRAG-002
- **Scope:** 校验 scope，通过产品侧 contract 查询 vector evidence，对返回 evidence 防御性执行 review policy，无 approved evidence 时带安全 no-answer 文案跳过 model generation，有 evidence 时通过 model-adapter contract 调用 model service，并持久化最终状态。
- **Constraints:** 测试只用 mock adapters；product service 不得直接调用 provider SDK、vector DB、CLI 或 outbound HTTP client。
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskServiceTest,AskSummaryCalculatorTest test
  ```

### T-ASKRAG-004：添加 Ask Controller And API Contract Tests

- **映射到:** REQ-ASKRAG-007, REQ-ASKRAG-008, REQ-ASKRAG-012；spec sections `API / Interface Surface`, `Failure Behavior`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-002, T-ASKRAG-003
- **Scope:** 添加 `POST /api/spaces/{spaceId}/ask` 和 `GET /api/ask-runs/{runId}`，包含 validation、envelope responses 和 safe errors。
- **Constraints:** validation failure 后不得执行 adapter；error body 不含 raw internals。
- **Verification:**
  ```bash
  cd backend && mvn -Dit.test=AskApiContractIT verify
  ```

### T-ASKRAG-005：保护 Source State Immutability

- **映射到:** REQ-ASKRAG-006, REQ-ASKRAG-009；spec sections `Evidence And Audit`, `Acceptance Matrix`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-003
- **Scope:** 添加 integration coverage，证明 Ask 不会修改 source chunk、file item、Wiki page、graph 或 review status。
- **Constraints:** Generated answer 保持 `REVIEW_REQUIRED`。
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AskStateImmutabilityTest test
  ```

### T-ASKRAG-006：实现 Frontend Trusted Ask State Mapping

- **映射到:** REQ-ASKRAG-001, REQ-ASKRAG-003, REQ-ASKRAG-010, REQ-ASKRAG-013；spec sections `UI Behavior`。
- **Owner type:** frontend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-002
- **Scope:** 添加 loading、answered、no-evidence、review-required warning 和 safe error 的 data-driven UI state，同时保持 FE baseline layout。
- **Constraints:** 仅 mock/sample data；不新增外部网络依赖；无真实 credential。
- **Verification:**
  ```bash
  cd frontend && npm run typecheck
  cd frontend && npm run test
  cd frontend && npm run build
  ```

### T-ASKRAG-007：添加 Ask E2E Coverage

- **映射到:** REQ-ASKRAG-010, REQ-ASKRAG-011, REQ-ASKRAG-014；spec sections `UI Behavior`, `Acceptance Matrix`。
- **Owner type:** QA/frontend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-006
- **Scope:** 用 Playwright 覆盖 answer success with evidence、no-approved-evidence refusal 和 review-required evidence warning。
- **Constraints:** E2E 使用 mock data，无外部网络调用。
- **Verification:**
  ```bash
  cd frontend && npm run e2e
  ```

### T-ASKRAG-008：添加 Adapter Seam And Data Safety Guards

- **映射到:** REQ-ASKRAG-004, REQ-ASKRAG-011, REQ-ASKRAG-014；spec sections `Constraints`, `Acceptance Matrix`。
- **Owner type:** security/backend
- **Priority:** Must
- **Dependencies:** T-ASKRAG-003
- **Scope:** 扩展 seam guard tests，阻止 adapter package 外直接 model/vector provider references，并扫描 unsafe secrets/private paths。
- **Constraints:** Adapter packages 只有在 guard coverage 下可包含 safe labels；product layers 不得出现 outbound clients。
- **Verification:**
  ```bash
  cd backend && mvn -Dtest=AdapterSeamGuardTest test
  ! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|pgvector|Milvus|Qdrant|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
  ! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src frontend/src docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md
  ```

### T-ASKRAG-009：运行完整 Phase 4 Verification

- **映射到:** REQ-ASKRAG-014；spec sections `Acceptance Matrix`。
- **Owner type:** QA
- **Priority:** Must
- **Dependencies:** T-ASKRAG-004, T-ASKRAG-005, T-ASKRAG-007, T-ASKRAG-008
- **Scope:** 运行最终 backend、frontend、E2E、diff hygiene、dependency/network 和 secret scans。
- **Constraints:** 任何 skipped check 必须说明原因；不得暗示未运行检查已通过。
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

### T-ASKRAG-010：更新 Traceability And Close Out

- **映射到:** REQ-ASKRAG-014；spec sections `Acceptance Matrix`。
- **Owner type:** docs/QA
- **Priority:** Must
- **Dependencies:** T-ASKRAG-009
- **Scope:** 更新 `docs/00-context/ask-rag-traceability.md`，记录 implementation evidence、verification results、residual risks 和任何 lessons learned。
- **Constraints:** 保持英文与中文文档同步。
- **Verification:**
  ```bash
  git diff --check
  for f in docs/00-context/ask-rag-traceability.md docs/00-context/ask-rag-traceability.zh-CN.md; do test -s "$f" || exit 1; done
  ```

## 依赖计划

关键路径：T-ASKRAG-001 -> T-ASKRAG-002 -> T-ASKRAG-003 -> T-ASKRAG-004 -> T-ASKRAG-009 -> T-ASKRAG-010。

并行工作：T-ASKRAG-005 可在 T-ASKRAG-003 后进行；T-ASKRAG-006 可在 DTO shape 稳定后开始；T-ASKRAG-008 可在 orchestration 存在后进行。

## 待确认问题

- OQ-ASKRAG-001：review-required evidence 的角色策略。
- OQ-ASKRAG-002：未来 review queue integration。
- OQ-ASKRAG-003：reranking 时机。

## Product Goal Batch 3 Task Addendum

| ID | Status | Evidence |
|---|---|---|
| T-ASKRAG-011 | Complete | Product Goal Batch 3 增加真实 Vue 全局可信问答产品表面，包含多空间上下文选择、问题输入、模型选择器、答案面板、evidence citations、no-approved-evidence refusal 与 review-required warning 状态。证据：`frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`、`docs/00-context/evidence/phase-g-trusted-ask.png`、frontend typecheck/test/build/E2E。 |

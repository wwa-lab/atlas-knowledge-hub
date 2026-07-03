# 设计：Ask RAG

## 状态

草稿。由 `docs/04-architecture/ask-rag-architecture.md` 通过 `architecture-to-design` 推导。

## 设计范围

本设计覆盖 `ask-rag` 切片的 Trusted Ask 前端状态、后端 Ask API、应用编排、持久化、adapter 边界、校验、错误处理和验证计划。

## 模块设计

### Frontend Trusted Ask

- 在 Knowledge Space Ask tab 内渲染。
- 保持当前原型形态：title、policy note、input row、answer panel、confidence label、source rows、approved Wiki pages、evidence chunks 和 review-required count。
- 增加 data-driven UI states：idle、loading、answered、no evidence、review-required evidence included 和 safe error。
- Evidence rows 提供稳定 selectors 供 E2E 测试使用。
- UI 不暗示生产 RBAC 或生产模型连接已经具备。

### Backend Ask API

- `AskController` 负责 request/response envelope shape。
- `AskService` 负责 validation、retrieval policy、adapter orchestration、persistence 和 state transitions。
- `AskMapper` 将 entities 与 adapter results 映射为 safe DTO。
- `AskSummaryCalculator` 或等价 pure helper 计算 status 与 evidence summaries。

### Adapter Integration

- 通过现有 vector service/adapter boundary 使用 vector query 行为。
- 通过现有 model service/adapter boundary 使用 model run 行为。
- 传递 product references 与 safe descriptors，不传 raw source text 或 raw prompt。
- 自动化验证只使用 mock adapters。

### Persistence

- 新增 `AskRun` 与 `AskEvidence` 逻辑实体和 repositories。
- 实现阶段才添加 Flyway migration。
- 通过 id 引用已有 source chunks 与 model runs。
- 不修改 source chunk、file item、Wiki page、graph 或 review records。

## API / Interface Design

本切片新增后端 endpoints，因此 API guide 必需。完整 payload 见 `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md`。

| Endpoint | Purpose |
|---|---|
| `POST /api/spaces/{spaceId}/ask` | 运行一个 scoped Ask request 并返回 answer/evidence/no-evidence payload。 |
| `GET /api/ask-runs/{runId}` | 读取持久化 Ask run 与 evidence detail。 |

## 校验与错误处理

| Case | 行为 |
|---|---|
| Blank question | `400 VALIDATION_ERROR`；不执行 adapter。 |
| Oversized question | `400 VALIDATION_ERROR`；不执行 adapter。 |
| Unknown space | `404 NOT_FOUND`；不执行 adapter。 |
| Invalid policy or limit | `400 VALIDATION_ERROR`；不执行 adapter。 |
| No approved evidence | `200` 且 `status=NO_EVIDENCE`；不执行 model generation。 |
| Vector failure | 安全 failed response，并持久化 safe error。 |
| Model failure | 安全 failed 或 partial response；如有 evidence 则保留。 |

## UI / User Flow Design

1. 用户进入 Knowledge Space 并选择 Trusted Ask。
2. 用户输入问题并提交。
3. UI 展示 loading state。
4. 成功回答展示 answer text、confidence、review status 和 evidence rows。
5. No-evidence state 说明需要 approved evidence。
6. Review-required inclusion state 展示 warning 和 separated evidence。
7. Safe error state 展示有边界消息，并让之前的成功回答保持视觉区分。

## 安全 / 审计 / 可靠性设计

- 只存 safe summaries 与 references。
- 不存 raw prompt、source text、vector values、provider payloads、secrets、private endpoints 或 stack traces。
- 使用 append-only Ask run/evidence records 做 audit。
- 保持 generated answer status 为 `REVIEW_REQUIRED`。
- 增加 seam guard，覆盖 adapter package 外直接 provider/vector 调用。

## 测试考虑

- Backend unit tests 覆盖 validation、policy branching、no-evidence refusal、status transitions 和 safe error masking。
- Backend integration/API tests 覆盖 envelope shape、persistence 和 state immutability。
- Frontend unit/component tests 覆盖 UI state mapping 和 evidence rendering。
- Playwright E2E 覆盖 answer success、no-evidence 和 review-required warning。
- `git diff --check`、dependency/network scan 和 secret/private-path scan。

## 风险 / 取舍

- 首版实现可能只使用 mock query 和 mock model answers；这符合本仓库 Phase 4 hardening 的安全边界。
- Reranking 延后，除非产品明确接受进入本切片。
- Production auth/RBAC 仅以 scope validation 与 audit fields 表达，除非单独 security slice 接受完整 enforcement。

## 待确认问题

- OQ-ASKRAG-001：review-required evidence 的角色策略。
- OQ-ASKRAG-002：未来 review queue integration。
- OQ-ASKRAG-003：reranking 时机。

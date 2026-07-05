# 规格：Ask RAG

## 状态

草稿。Phase 4 hardening 切片。`ask-rag` 的行为源。

## 概览

Ask RAG 允许用户在 Knowledge Space 内提问，并从 approved evidence 得到带来源的回答。本切片通过 adapter 隔离检索与生成、拒绝无依据回答、保留 source trace 和 review status、记录安全审计证据来强化可信边界。

## 来源故事

| Story | 能力 |
|---|---|
| US-ASKRAG-001 | 限定范围的 Ask UI 与请求行为 |
| US-ASKRAG-002 | 带来源的回答与 evidence 展示 |
| US-ASKRAG-003 | 无 evidence 时拒绝回答 |
| US-ASKRAG-004 | adapter-bound 编排 |
| US-ASKRAG-005 | review-required 输出与审计证据 |
| US-ASKRAG-006 | 校验与安全失败 |

## 范围

范围内：

- 选定 Knowledge Space 的 Trusted Ask UI 行为。
- `POST /api/spaces/{spaceId}/ask` 以及 Ask run evidence 的读取 endpoint。
- Ask 请求校验与 review policy enforcement。
- 通过 vector adapter/service contract 查询 vector evidence。
- 通过 model adapter/service contract 生成回答。
- Ask run 与 evidence 记录，保留 source trace、confidence、review status、score、model run id、safe answer summary 和 requested-by。
- no-evidence、partial-evidence、review-required、validation、adapter-failure 状态。
- unit、integration、API contract、E2E、seam guard、dependency/network 与 secret/private-path 验证。

范围外：

- 真实外部 model/vector provider 调用、流式输出、raw prompt retention、answer 发布到 Wiki、图谱抽取、parser/converter/storage 行为、完整生产 SSO/RBAC、provider 成本治理和跨空间 chat history。

## 约束

- 默认 retrieval policy 为 `APPROVED_ONLY`。
- `INCLUDE_REVIEW_REQUIRED` 只能作为显式请求策略使用，且必须明显标记。
- Ask 不得改变 source chunk、file、Wiki、graph 或 review status。
- 生成回答输出为 `REVIEW_REQUIRED`。
- 产品层不得直接调用 vector DB、model provider、SDK、CLI 或外部 HTTP client。
- 自动化验证必须使用 mock engine；不需要网络或 credential。
- 不返回或持久化 secret、private path、raw prompt、机密 source text、provider payload 或 stack trace。

## 参与者

| Actor | 角色 |
|---|---|
| Knowledge user | 提问并阅读可信回答。 |
| SME reviewer | 核验回答 evidence 和 review-required warning。 |
| Delivery lead | 使用 Ask audit evidence 判断知识准备度。 |
| Platform administrator | 验证 adapter health 和安全配置。 |
| Codex implementation agent | 严格按照本 spec 与 `docs/06-tasks/ask-rag-tasks.md` 实现。 |

## 功能需求

### Ask Request

- **FR-ASKRAG-001:** Ask request 限定到一个 `spaceId`。 (US-ASKRAG-001, REQ-ASKRAG-001)
- **FR-ASKRAG-002:** 请求字段包含 `question`、`requestedBy`、可选 `reviewPolicy`、可选 `limit`、可选 `filters` 和可选 `mode`；自动化测试使用 `mode=mock`。 (US-ASKRAG-006, REQ-ASKRAG-008)
- **FR-ASKRAG-003:** 空、超长、不安全或跨 scope 请求必须在 adapter 执行前校验失败。 (US-ASKRAG-006, REQ-ASKRAG-008)

### Retrieval Policy

- **FR-ASKRAG-004:** 默认 review policy 为 `APPROVED_ONLY`。 (US-ASKRAG-002, REQ-ASKRAG-002)
- **FR-ASKRAG-005:** 只有 `reviewPolicy=INCLUDE_REVIEW_REQUIRED` 时才包含 review-required evidence，并在响应和 UI 中标记。 (US-ASKRAG-002, REQ-ASKRAG-013)
- **FR-ASKRAG-006:** Retrieval result 有边界、按 score 降序排序，并以 source chunk id 做稳定 tie-break。 (US-ASKRAG-002, REQ-ASKRAG-003)

### Answer Generation

- **FR-ASKRAG-007:** 如果所选 policy 下没有 approved evidence，Ask 返回 `NO_APPROVED_EVIDENCE`，且不调用 model adapter 生成回答。 (US-ASKRAG-003, REQ-ASKRAG-012)
- **FR-ASKRAG-008:** 有 evidence 时，Ask 使用 safe product references 和 bounded context descriptors 调用 model adapter，不传 raw provider payload 或 private path。 (US-ASKRAG-004, REQ-ASKRAG-004)
- **FR-ASKRAG-009:** 生成回答响应包含 answer text 或 safe no-answer message、confidence、answer review status、safe message 与 evidence references。 (US-ASKRAG-002, REQ-ASKRAG-003)
- **FR-ASKRAG-010:** 生成回答 review status 始终为 `REVIEW_REQUIRED`，直到未来 review workflow 验证。 (US-ASKRAG-005, REQ-ASKRAG-006)

### Evidence And Audit

- **FR-ASKRAG-011:** Ask run record 保留 space id、status、question summary、requested-by、review policy、result limit、vector query reference、model run reference、timestamps、safe answer summary 和 safe error。 (US-ASKRAG-005, REQ-ASKRAG-009)
- **FR-ASKRAG-012:** Evidence record 保留 source chunk id、file item id、source file、page、section、score、confidence、review status 和可用 safe excerpt label。 (US-ASKRAG-002, REQ-ASKRAG-005)
- **FR-ASKRAG-013:** Ask record 不得保存 raw source text、raw prompt、raw vector、provider payload、secret、private endpoint、private path 或 stack trace。 (US-ASKRAG-005, REQ-ASKRAG-009)

### UI Behavior

- **FR-ASKRAG-014:** Ask tab 展示 question input、submit affordance、answer panel、confidence badge、evidence list、policy note、review-required warning、no-evidence state、loading state 和 safe error state。 (US-ASKRAG-001, REQ-ASKRAG-010)
- **FR-ASKRAG-015:** Evidence row 展示 source file 与 page 或 section、score 或 confidence、review status。 (US-ASKRAG-002, REQ-ASKRAG-003)
- **FR-ASKRAG-016:** UI 行为与当前 FE 基线一致，响应式布局中不发生文字重叠。 (US-ASKRAG-001, REQ-ASKRAG-010)

### Failure Behavior

- **FR-ASKRAG-017:** Validation error 返回 `VALIDATION_ERROR`；unknown space 返回 `NOT_FOUND`；no evidence 返回含 `status=NO_EVIDENCE` 的成功 no-answer payload；adapter failure 返回 `FAILED` 和 sanitized safe message。 (US-ASKRAG-006, REQ-ASKRAG-007)
- **FR-ASKRAG-018:** partial retrieval/model outcome 保留成功 evidence，且只有可安全展示 partial answer 时才将 Ask run 标为 `PARTIAL_FAILED`。 (US-ASKRAG-006, REQ-ASKRAG-007)

## 状态模型

Ask run status：

```text
REQUESTED -> RETRIEVING -> NO_EVIDENCE
                     \-> GENERATING -> SUCCEEDED
                                  \-> PARTIAL_FAILED
                                  \-> FAILED
```

Answer review status：

| 输出 | Review Status |
|---|---|
| Generated answer | `REVIEW_REQUIRED` |
| No-evidence response | `REVIEW_REQUIRED` |
| Failed run safe message | `REVIEW_REQUIRED` |

## API / Interface Surface

完整请求/响应细节见 `docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md`。

| Interface | 行为 |
|---|---|
| `POST /api/spaces/{spaceId}/ask` | 校验问题、检索 evidence、可选通过 model adapter 生成回答、存储安全 Ask run evidence，并返回 answer payload。 |
| `GET /api/ask-runs/{runId}` | 返回 Ask run status、safe answer summary、evidence list、review policy、model run reference 和 safe error。 |
| Internal vector query interface | 返回带 score、confidence、review status 的有界 source evidence。 |
| Internal model run interface | 基于 evidence references 生成 safe mock answer text，并将输出标为 review-required。 |

## 验收矩阵

| Check | Requirements | 可观察结果 |
|---|---|---|
| AC-ASKRAG-01 | REQ-ASKRAG-001, 010 | UI 和 API 将所有 Ask request 限定到一个 Knowledge Space。 |
| AC-ASKRAG-02 | REQ-ASKRAG-002, 013 | 默认 approved-only；review-required evidence 仅显式策略出现，并被标记。 |
| AC-ASKRAG-03 | REQ-ASKRAG-003, 005 | Answer payload 和 Ask run detail 包含 source references、score/confidence、review status、model/vector references。 |
| AC-ASKRAG-04 | REQ-ASKRAG-004, 011 | Seam guard 阻止 adapter package 外直接调用 model/vector provider；测试使用 mock adapter。 |
| AC-ASKRAG-05 | REQ-ASKRAG-006, 009 | 生成输出为 `REVIEW_REQUIRED`；source/Wiki/graph/review 状态不变。 |
| AC-ASKRAG-06 | REQ-ASKRAG-007, 008, 012 | invalid/no-evidence/adapter-failure 返回安全 envelope，且无 raw internals。 |
| AC-ASKRAG-07 | REQ-ASKRAG-014 | Task verification 包含精确 unit、integration、API、E2E、seam、diff、network 和 secret scan 命令。 |

## 待确认问题

- OQ-ASKRAG-001：review-required evidence 的角色可见性。
- OQ-ASKRAG-002：Ask answer 未来进入 review queue 的路径。
- OQ-ASKRAG-003：reranking 是否作为独立后续切片。

## Product Goal Batch 3 Vue Parity Addendum

Product Goal Batch 3 将可信问答扩展到真实 Vue 产品壳，不调用真实 model 或 vector provider。

| Phase | Vue Product Acceptance |
|---|---|
| Phase G 可信问答 | 真实 Vue 全局 Chat 表面提供多知识空间上下文选择、问题输入、模型选择器、答案面板、evidence citations、review-required warning，以及 no-approved-evidence refusal 状态，且仅使用 mock/sample-safe 数据。 |

该 addendum 仅更新 Vue 产品界面成熟度，不新增生产 RAG 优化、raw prompt 持久化、raw vector/provider payload 存储、真实外部模型调用或新的 backend/API 行为。

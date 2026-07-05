# 规格：模型适配器

## 状态

草稿。Phase 3 适配器切片。`model-adapter` 的行为唯一真相源。由 `docs/02-user-stories/model-adapter-stories.md` 派生。

## 来源文档

- `docs/01-requirements/model-adapter-requirements.md`
- `docs/02-user-stories/model-adapter-stories.md`
- `docs/01-requirements/requirement.md`
- `docs/00-context/slice-roadmap.md`
- `docs/03-spec/metadata-api-spec.md`
- 现有 converter/parser adapter specs 与 storage adapter SDD，用于 adapter contract 形态
- FE 模型设置基线：`frontend/public/atlas-prototype.html`、`prototypes/index.html`

## 范围

Atlas 必须通过产品面的模型 adapter contract 支持模型 provider 能力发现与 mock 模型执行。本切片包括模型能力、model run 生命周期、mock operation 输出、source trace references、review-required 输出状态、usage summary、安全错误处理、adapter seam guard、内部 API/adapter contract。不实现真实模型 provider 调用、Ask/RAG、向量索引、图谱抽取、Wiki 发布、前端 UI 变更、生产 secret 管理或生产 auth/RBAC。

## 约束

- **只走 adapter：** 产品工作流代码依赖模型 adapter interface 和 registry contract，而不是直接依赖 provider SDK、HTTP client、本地 runtime 或模型 CLI（REQ-MODA-001）。
- **不硬编码单一实现：** 内置/mock 模型可对齐 FE 基线，但真实 provider 保持可替换（REQ-MODA-002）。
- **mock-engine 验证：** 自动化测试使用 mock/fake model adapter，不需要凭据、网络、本地模型 daemon 或 provider 账号（REQ-MODA-013）。
- **secret/endpoint 安全：** 原始 provider secret、endpoint、hostname、私有路径、provider payload、原始 prompt、stack trace 不得返回或持久化（REQ-MODA-004, REQ-MODA-012）。
- **保留溯源/审核：** 模型输出保留 source references，默认 `REVIEW_REQUIRED`；不会成为已批准知识（REQ-MODA-008, REQ-MODA-009）。
- **边界纪律：** embedding metadata 不写 vector；chat output 不成为 Ask/RAG；model output 不发布 Wiki 或 graph data（REQ-MODA-010, REQ-MODA-015）。

## 角色

| 角色 | 作用 |
|---|---|
| 平台管理员 | 查看脱敏模型能力与 provider readiness。 |
| 交付负责人 | 触发或检查 mock model run，形成工作流集成证据。 |
| SME reviewer | 在后续切片中依据 source references 与 review status 评估生成输出。 |
| Codex 实现 agent | SDD 接受后严格依据本 spec 与任务清单实现。 |

## 功能需求

### Adapter Boundary

- **FR-MODA-001:** 模型工作流必须在任何模型操作开始前，通过 registry/capability contract 解析模型 adapter。（US-MODA-001）
- **FR-MODA-002:** Non-adapter 产品层不得引用 provider SDK、本地 runtime 命令、直接模型 HTTP client 或 provider-specific execution API。（US-MODA-001, US-MODA-005）
- **FR-MODA-003:** Adapter registry 必须支持显式模型选择、按 model type 的默认模型选择、unavailable 状态和 misconfigured 状态。（US-MODA-001, US-MODA-004）

### Capability Metadata

- **FR-MODA-004:** 能力元数据必须包含 adapter key、model key、display name、provider family、model type、supported operations、default marker、status、context limit 和 masked configuration summary。（US-MODA-001）
- **FR-MODA-005:** 能力元数据不得暴露原始 provider endpoint、本地 runtime path、组织标识、credential、hostname 或私有配置值。（US-MODA-001, US-MODA-004）
- **FR-MODA-006:** Model type 必须包含 `CHAT`、`EMBEDDING`、`RERANK`、`VISION`、`SPEECH`；不支持的 operation 在 adapter 执行前校验失败。（US-MODA-002）

### Model Run Lifecycle

- **FR-MODA-007:** Model run request 必须包含 operation type、requested-by identity、mode、purpose、可选 adapter/model selector，以及 safe mock input 或 input reference。（US-MODA-002）
- **FR-MODA-008:** Model run 可通过 id 引用 source chunks、file items、Wiki pages、graph nodes 或未来 Ask context；被引用的原文不复制到 run record。（US-MODA-003）
- **FR-MODA-009:** Service 必须在 adapter 执行前校验 operation/model 兼容性、mode、input reference 形态、source reference scope 与 safe text limits。（US-MODA-002, US-MODA-004）
- **FR-MODA-010:** Run status 为 `REQUESTED`、`RUNNING`、`SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`。（US-MODA-002, US-MODA-004）
- **FR-MODA-011:** 完成的 run report 必须包含 adapter/model identity、operation type、status、safe message、usage summary、outputs、source references、timestamps 与 review status。（US-MODA-002, US-MODA-003）

### Operation Outputs

- **FR-MODA-012:** Chat output 返回安全 generated summary/reference 与 review status，而不是 Ask 已批准答案。（US-MODA-002, US-MODA-003）
- **FR-MODA-013:** Embedding output 只返回 embedding dimension、item count 与 output reference metadata；vector 不在 API JSON 中返回，也不写入向量数据库。（US-MODA-002）
- **FR-MODA-014:** Rerank output 只为安全 mock input/reference 返回 ordered item ids 和 scores。（US-MODA-002）
- **FR-MODA-015:** Vision output 只返回安全 description/reference 与 evidence metadata；不持久化原始 image bytes 或机密截图。（US-MODA-002, US-MODA-003）
- **FR-MODA-016:** Speech output 只返回安全 transcript summary/reference；真实转写不在范围内。（US-MODA-002）
- **FR-MODA-017:** 所有生成输出默认 `REVIEW_REQUIRED`，并在可用时保留 confidence/evidence。（US-MODA-003）

### Validation And Failure Behavior

- **FR-MODA-018:** 未知 adapter key、未知 model key、不兼容 operation/model type、无效 mode、不安全 input reference、safe input 过大或跨 scope source references，以 `VALIDATION_ERROR` 失败。（US-MODA-004）
- **FR-MODA-019:** Unavailable 或 misconfigured adapter 安全失败，且不暴露原始 provider 配置或私有 runtime 细节。（US-MODA-004）
- **FR-MODA-020:** 未预期 adapter fault 返回已脱敏、有界的 safe message，不持久化原始 prompt、provider payload、stack trace、endpoint、credential 或私有路径。（US-MODA-004）

## 非功能需求

| 类别 | 需求 |
|---|---|
| 安全 | 响应、持久化 summary、测试 fixture 中不得出现原始 provider secret、endpoint、credential、私有路径、原始 prompt、原始 provider payload 或机密文档文本。 |
| 可靠性 | Mock-engine 测试覆盖能力列表、默认解析、每种支持的 model type、不兼容 operation 拒绝、unavailable adapter、adapter fault 与安全错误处理。 |
| 可扩展性 | 新 provider family 和 model type 可通过 adapter capability metadata 添加，无需改变产品工作流调用方。 |
| 可审计性 | Model run 记录保留 adapter/model identity、operation、requested-by、timestamps、source references、usage summary、output review status 与 safe messages。 |
| 数据安全 | 仅 mock/sample data；无真实公司文档、截图、凭据、私有 endpoint 或外部云调用。 |

## 工作流

```text
+---------------------------+
| Model run request          |
| operation + references     |
+-------------+-------------+
              |
              v
+---------------------------+        invalid selector/input
| Validate request and scope |------------------------------+
+-------------+-------------+                              |
              | valid                                      v
              v                                    +----------------+
+---------------------------+                      | Safe error     |
| Resolve model adapter      |-- unavailable -----> | no raw leak    |
+-------------+-------------+                      +----------------+
              |
              v
+---------------------------+
| Execute model adapter      |
| mock/fake engine in tests  |
+-------------+-------------+
              |
              v
+---------------------------+
| Validate safe output       |
| set REVIEW_REQUIRED        |
+-------------+-------------+
              |
              v
+---------------------------+
| Persist run evidence       |
| return API envelope        |
+---------------------------+
```

## 状态模型

### Model Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`：请求校验通过且 adapter 已解析。
- `RUNNING -> SUCCEEDED`：所有请求的 mock operation output 均安全产出。
- `RUNNING -> PARTIAL_FAILED`：至少一个 output 成功，且至少一个失败或跳过。
- `RUNNING -> FAILED`：adapter-level failure、创建后的 validation failure，或没有 output 成功。

### Model Output Review Status

| 结果 | Review Status | 说明 |
|---|---|---|
| 任意生成的 chat/vision/speech output | `REVIEW_REQUIRED` | 后续切片中的 SME review 或确定性校验可改变它。 |
| Embedding metadata | `REVIEW_REQUIRED` | Embedding vector 本身不是可信知识。 |
| Rerank output | `REVIEW_REQUIRED` | Ranking evidence 在消费工作流验证前只是建议。 |
| Failed output | `REVIEW_REQUIRED` | 只有 safe error；没有可信内容。 |

## 校验规则

- `operationType` 必须是 `CHAT`、`EMBEDDING`、`RERANK`、`VISION`、`SPEECH` 之一。
- `mode` 必须是 `mock` 或 `configured`；自动化测试使用 `mock`。
- `adapterKey` 和 `modelKey` 必须解析到已注册 capability；也可省略并使用 default resolution。
- 选中的 model type 必须支持请求的 operation。
- Safe mock input 有长度边界，且不得包含原始 credential、私有路径或真实公司内容。
- Input reference 必须是相对产品 reference，不得是本地路径、URL 或 provider payload。
- 提供 scope 时，source references 不得跨已知 workspace/batch scope。
- Usage counts 如存在，必须非负。
- Confidence/evidence values 如存在，必须在 `[0,1]`。
- Safe messages 与 safe output summaries 必须有界且已脱敏。

## API / Interface Surface

完整契约见 `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`。

| Interface | 行为 |
|---|---|
| `GET /api/model-adapters` | 以脱敏配置列出已配置 model adapter/model capabilities。 |
| `POST /api/model-runs` | 通过 adapter contract 执行 mock/configured model operation，并记录 run evidence。 |
| `GET /api/model-runs/{runId}` | 返回 model run summary、source references、usage summary 与 safe outputs。 |
| Internal model adapter interface | 在产品面 contract 后执行模型操作；测试必须提供 mock/fake implementation。 |

## 验收矩阵

| 检查 | 需求 | 可观察结果 |
|---|---|---|
| AC-MODA-01 | REQ-MODA-001, REQ-MODA-013 | Guard tests 在 adapter 实现之外发现直接 model provider/client 引用时失败。 |
| AC-MODA-02 | REQ-MODA-002, REQ-MODA-003, REQ-MODA-004 | Capability endpoint 返回支持 model type 的可替换、脱敏模型元数据。 |
| AC-MODA-03 | REQ-MODA-005, REQ-MODA-006, REQ-MODA-007 | Mock model run 对支持的 operation/model 组合成功，并返回安全 output descriptor。 |
| AC-MODA-04 | REQ-MODA-008, REQ-MODA-009 | 输出默认 `REVIEW_REQUIRED`，保留 source references 且不含原始源文本。 |
| AC-MODA-05 | REQ-MODA-010 | Embedding run 只返回 dimension/count metadata，且不调用 vector database code。 |
| AC-MODA-06 | REQ-MODA-011, REQ-MODA-012 | Error message 与 output 不含原始 prompt、secret、endpoint、私有路径、provider payload 或 stack trace。 |
| AC-MODA-07 | REQ-MODA-013 | `cd backend && mvn verify` 只用 mock/fake model adapter 通过。 |
| AC-MODA-08 | REQ-MODA-014 | Run status 与 summary 正确表示 success、partial failure、failure 和 unavailable adapter。 |
| AC-MODA-09 | REQ-MODA-015 | 未引入 Ask/RAG、vector write、graph extraction、Wiki publish、frontend 或生产 auth/RBAC 行为。 |

## 范围外

- 真实模型 provider 调用、provider SDK wiring、本地 runtime invocation、streaming、credential rotation、生产 secret manager。
- Ask/RAG 回答、向量索引写入、图谱抽取、Markdown 标准化、Wiki 发布、OCR/转写流水线、前端 UI。
- 生产 auth/RBAC、限流、成本/额度策略、provider 账号管理。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-MODA-001 | mock implementation 后首个真实 provider。 | 影响 adapter 实现选择，不影响 model adapter contract。 |
| OQ-MODA-002 | 原始 prompt 保留策略。 | 本 spec 承诺只保留 reference 与 safe summary。 |
| OQ-MODA-003 | Usage/cost governance 的时机。 | 本 spec 只记录 usage counts，额度/成本策略延后。 |

## Product Goal Batch 4 Vue Parity Addendum

Product Goal Batch 4 将模型与适配器管理扩展到真实 Vue 设置表面。

| Phase | Vue Product Acceptance |
|---|---|
| Phase H 设置与管理 | 真实 Vue settings modal 为常规设置、成员、注册策略、API 信息、模型管理、向量适配器、解析适配器和存储适配器提供非空企业管理面板。模型管理支持 list、add、edit、cancel、save、masked API key state、replace/remove，以及 mock-safe 测试连接反馈。 |

该 addendum 仅更新 Vue 产品界面成熟度，不新增生产 secret management、生产 RBAC、真实 provider 调用、外部网络调用或新的 backend/API contract 行为。

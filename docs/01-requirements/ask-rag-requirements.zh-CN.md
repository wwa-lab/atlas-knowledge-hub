# 需求：Ask RAG

## 状态

草稿。Phase 4 hardening 切片。使用 `atlas-sdd-generate-all` 生成。

## 切片契约

| 字段 | 值 |
|---|---|
| Goal | Atlas 用户可以在 Knowledge Space 内提问，并基于已审核知识获得带来源、Review 状态、confidence 与安全失败行为的可信回答。 |
| Slice | `ask-rag` |
| Phase | 4 hardening |
| Scope | Trusted Ask UI、Ask API 契约、基于 approved vector evidence 的检索编排、model-adapter 回答生成、answer evidence bundle、review-aware 策略、审计、安全错误、测试与验证。 |
| Exclusions | 真实外部模型调用、真实外部向量服务、新 parser/converter 行为、图谱抽取、Wiki 发布状态机、完整生产 SSO/RBAC、原始 prompt/source 留存、流式输出和 provider 成本治理。 |
| Verification | Phase 4 hardening：触及层的完整 unit + integration + E2E。另运行 `git diff --check`、依赖/网络扫描、secret/private-path 扫描。 |
| Constraints | 所有 Markdown/metadata 保留 source trace、confidence、review status；LLM 输出在验证前保持 review-required；model/vector/search 通过 adapter；secret 仅 masked/status-only；仅 mock engine；无外部网络调用。 |

## 来源上下文

- `README.md`
- `PROJECT_RULES.md`
- `AGENTS.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/00-context/slice-roadmap.md`
- `docs/01-requirements/requirement.md`
- `docs/product-vision.md`
- `docs/mvp-scope.md`
- `docs/markdown-standard.md`
- `docs/review-workflow.md`
- `docs/knowledge-graph-design.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/03-spec/vector-adapter-spec.md`
- `docs/03-spec/model-adapter-spec.md`
- FE 基线：`frontend/public/atlas-prototype.html`、`prototypes/index.html`

## 产品需求

| ID | 需求 | 优先级 | 来源 |
|---|---|---|---|
| REQ-ASKRAG-001 | Ask 必须在选定 Knowledge Space 内回答问题，不得跨未限定范围的原始上传或无关空间检索。 | Must | REQ-PROD-039, roadmap |
| REQ-ASKRAG-002 | Ask 检索默认只能使用 approved 或 published knowledge evidence。只有显式请求时才可包含 review-required evidence，且必须明显标记。 | Must | REQ-PROD-041, REQ-PROD-042 |
| REQ-ASKRAG-003 | 每个回答必须展示 source references，包括 source file、page 或 section、source chunk id、review status 和可用 confidence。 | Must | REQ-PROD-040, Markdown standard |
| REQ-ASKRAG-004 | Ask 编排必须使用产品侧 vector 和 model adapter 契约；产品服务不得直接调用 vector DB、model provider、SDK、CLI 或外部 HTTP client。 | Must | Adapter rules |
| REQ-ASKRAG-005 | 回答生成必须保留 evidence bundle，包含 retrieval matches、model run reference、review policy、answer confidence 与 safe message。 | Must | Phase 4 hardening |
| REQ-ASKRAG-006 | LLM 生成的回答文本默认必须为 `REVIEW_REQUIRED`，不得改变 Wiki、source chunk、graph、file 或 review 状态。 | Must | Review workflow |
| REQ-ASKRAG-007 | Ask API 响应必须使用 Atlas envelope，且只暴露用户安全错误。 | Must | Metadata API |
| REQ-ASKRAG-008 | Ask 请求必须在任何 adapter 执行前校验 space id、question length、review policy、result limit、可选 filters 和不安全输入模式。 | Must | Security standards |
| REQ-ASKRAG-009 | Ask 审计记录必须记录 request metadata、所选策略、safe answer summary、evidence references、status、timestamps 和 requested-by，且不保存 raw secret 或机密 source text。 | Must | Phase 4 hardening |
| REQ-ASKRAG-010 | Trusted Ask UI 必须展示 answer、confidence、evidence list、review-required warning、empty/no-evidence state 和 safe error state，并与当前 FE 基线一致。 | Must | FE baseline |
| REQ-ASKRAG-011 | Ask 必须提供确定性 mock 行为用于自动化测试，不得需要 provider account、外部网络调用或真实公司文档。 | Must | Project data rules |
| REQ-ASKRAG-012 | 无 approved evidence 时，Ask 必须拒绝基于不可信内容作答，并说明需要 approved evidence。 | Must | Product principle |
| REQ-ASKRAG-013 | Ask 必须在 API 和 UI 中区分 review-required evidence 与 trusted evidence。 | Should | REQ-PROD-042 |
| REQ-ASKRAG-014 | Ask 实现任务必须包含 unit、integration、API contract、E2E、seam guard、依赖/网络与 secret/private-path 验证命令。 | Must | Roadmap verification |

## 非功能需求

| 类别 | 需求 |
|---|---|
| Security | 响应、持久化 safe summary、日志和测试中不得出现 raw credential、provider payload、private endpoint、private path、prompt dump、stack trace 或机密 source text。 |
| Reliability | Mock vector 与 model adapter 必须支持 deterministic success、no-evidence、review-required evidence、validation failure、adapter failure 和 partial evidence 场景。 |
| Traceability | 每个回答都能追踪到 requirement、evidence chunks、review status、confidence、model run、vector query policy 和 task verification。 |
| Extensibility | 未来 model、vector、search、rerank 与 storage engine 仍通过 adapter contract 替换。 |
| Accessibility | Trusted Ask UI 状态必须可键盘访问，在 day/night mode 下可读，并在响应式布局中不出现文字重叠。 |

## 假设

- 实现前 Phase 3 metadata、vector、storage、model adapter 基础已经可用。
- `review-publish` 和 `knowledge-graph` 仍可作为独立 Phase 4 切片；`ask-rag` 消费 approved evidence，不实现 publish 或 graph extraction。
- 本切片可包含内部 guard check 与 audit 字段，但完整 SSO 与企业权限策略不在本 SDD 范围内，除非另行接受。

## 范围外

- 真实外部云模型或向量 provider 调用。
- 真实文档 ingest、解析、转换、存储后端或图谱抽取。
- 将回答发布回 Wiki 或批准生成回答文本。
- 原始 prompt/source 留存、流式输出、跨空间 chat history、成本/配额治理和 provider 管理。

## 待确认问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-ASKRAG-001 | review-required evidence 是否应允许非 reviewer 用户查看？ | 影响未来 RBAC 策略；默认仍为 approved-only。 |
| OQ-ASKRAG-002 | Ask answer 是否应在后续切片进入 review queue？ | 当前切片只存 answer evidence，生成输出保持 review-required。 |
| OQ-ASKRAG-003 | 首版 Ask 是否包含 reranking，还是后续补充？ | 当前切片可使用 vector score 排序和 model-adapter mock answer generation，不需要单独 reranker。 |

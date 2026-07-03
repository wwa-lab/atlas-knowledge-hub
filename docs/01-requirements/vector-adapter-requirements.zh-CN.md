# 需求：Vector Adapter

## 状态

草稿。Phase 3 adapter 切片。本轮只产出 SDD，不实现产品代码。

## 切片契约

- **Goal：** Atlas 能把已批准或带审核状态的 source chunk 通过可替换的 vector adapter 建入索引，并执行保留来源溯源的相似度查询，同时不让产品工作流耦合到 pgvector、Milvus、Qdrant 或任何单一向量库。
- **Slice：** `vector-adapter`
- **Phase：** 3 adapter
- **Sources：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/00-context/slice-roadmap.md`、`docs/01-requirements/requirement.md`、`docs/architecture.md`、`docs/markdown-standard.md`、`docs/knowledge-graph-design.md`、`docs/03-spec/parser-adapter-spec.md`、`docs/03-spec/metadata-api-spec.md`，以及已做 grounding 的现有后端 metadata 代码。
- **Verification 行：** Phase 3 adapter 需要基于 mock engine 的单元测试与集成测试。
- **硬约束：** parser/converter/model/vector/storage 只能通过产品面的 adapter；禁止直连工具；不得硬编码单一实现；secret 与私有 endpoint 必须脱敏；必须保留 source trace、confidence、review status。

## 范围内

- 面向产品的 vector adapter contract：index upsert、delete、query 与 capability metadata。
- 带脱敏配置和默认标记的可替换向量引擎 capability metadata。
- 按 Knowledge Space、batch、file item、source chunk 作用域组织的 vector collection/index 策略。
- 基于现有 source trace metadata 的可索引 chunk descriptor：source file、file item、page/section、chunk id、confidence、review status、text reference。
- vector run 记录与逐 chunk index result 记录，用于审计和排障。
- 相似度查询结果返回 source chunk 引用、score、confidence、review status 与安全 metadata。
- mock/in-memory vector adapter、单元/集成测试、adapter seam guard 更新。
- 面向实现的内部 API 和 adapter contract 指南。

## 排除项

- 真实 pgvector/Milvus/Qdrant 执行、真实 embedding 生成、模型供应商调用、RAG answer 生成、Ask UI、图谱抽取、Wiki 发布、ranking/re-ranking、生产 auth/RBAC、生产 secret manager 集成、前端改动。
- 把未审核内容作为可信知识建索引。review-required chunk 只有在 metadata 和 query result 中清晰标记为 review-required 时才可被纳入。
- 真实公司文档、私有 endpoint、明文凭证、日志、外部云调用或外部网络依赖。

## 需求

| ID | 需求 | 优先级 | 来源 / 理由 |
|---|---|---|---|
| REQ-VA-001 | 产品工作流必须通过 vector adapter registry 解析向量操作，非 adapter scope 不得直接调用 pgvector、Milvus、Qdrant、JDBC vector extension、vector SDK 或 outbound vector client。 | Must | REQ-PROD-015, REQ-PROD-017, Adapter Standards |
| REQ-VA-002 | pgvector/vector DB 必须表现为一个可替换的 vector adapter 选项，而不是唯一实现；未来引擎必须保持可配置。 | Must | REQ-PROD-017, REQ-PROD-060, REQ-PROD-061 |
| REQ-VA-003 | Vector capability metadata 必须暴露 adapter key、display name、version/status、supported dimensions、supported operations、default marker 和脱敏配置摘要；不得暴露 raw endpoint、DSN、collection name、credential、token 或私有路径。 | Must | REQ-PROD-049, REQ-PROD-058, REQ-PROD-061 |
| REQ-VA-004 | Index input 必须来自 Atlas source chunk 或 approved/review-aware Markdown metadata，并保留 workspace、batch、file item、source file、page/section、chunk id、confidence、review status。 | Must | REQ-PROD-023, REQ-PROD-034, REQ-PROD-041 |
| REQ-VA-005 | Vector indexing 必须支持按 space/batch/file/chunk 选择的有界 upsert run，并记录 run status、adapter key、mode、counts、timestamps、safe message。 | Must | Phase 3 adapter evidence |
| REQ-VA-006 | 逐 chunk vector index result 必须记录 chunk id、file item id、source trace、review status、vector item key、status、score/index metadata、safe error。 | Must | Source trace preservation |
| REQ-VA-007 | Similarity query 必须返回有界结果，包含 chunk id、file item id、source file、page/section、score、confidence、review status 与安全 metadata；不得返回 raw vector 或 raw engine internals。 | Must | REQ-PROD-040, REQ-PROD-042 |
| REQ-VA-008 | Delete/deindex 操作应支持按 space、batch、file item 或 chunk id 安全移除索引项，且不得删除 source metadata 或改变 review state。 | Should | Lifecycle hygiene |
| REQ-VA-009 | Vector adapter failure 必须返回脱敏错误，不能包含 raw SDK output、stack trace、credential、endpoint、DSN、hostname、collection name 或私有/绝对路径。 | Must | Security/Data Standards |
| REQ-VA-010 | 自动化验证必须使用 mock/in-memory vector engine，不得需要 pgvector、Milvus、Qdrant、真实 embedding、外部网络或凭证。 | Must | Phase 3 verification row |
| REQ-VA-011 | Query 和 index 行为必须区分 approved evidence 与 review-required evidence；review-required 内容必须继续清晰标注为 review-required，不得提升为可信 answer material。 | Must | REQ-PROD-026, REQ-PROD-041, REQ-PROD-042 |
| REQ-VA-012 | Vector run 不得创建 graph nodes/edges、发布 Wiki pages 或调用 model adapters；它只准备可追溯的 vector index metadata。 | Must | Phase discipline |
| REQ-VA-013 | Adapter seam guard 必须只允许 vector-engine 名称出现在 vector adapter package 内，并继续禁止 controller/service/repository/domain 产品层直连。 | Must | Adapter boundary |
| REQ-VA-014 | SDD 与实现任务必须保持 backend/API contract 明确、可由 Codex 执行，并带确切验证命令。 | Must | Goal-driven SDD |

## 验收

- requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability 的完整双语 SDD artifact 存在。
- 英文与简体中文副本的 REQ/US/T ID 一致。
- Tasks 映射到 requirement ID 与 spec 章节，并包含确切验证命令。
- API guide 已包含，因为该 Phase 3 adapter 切片定义内部 backend/API 与 adapter contract。
- 本轮 SDD pass 不修改产品代码。

## 开放问题

| ID | 问题 | 影响 |
|---|---|---|
| OQ-VA-001 | 首个真实 vector engine 应为 PostgreSQL/pgvector 还是独立向量库？ | 部署细节；不阻塞 mock adapter contract。 |
| OQ-VA-002 | Embedding 应由未来 model-adapter 切片提供，还是由离线 worker 传入预计算向量？ | 本 SDD 使用预计算 vector payload 或 deterministic mock vector，不调用模型供应商。 |
| OQ-VA-003 | review-required chunk 默认是否可查询，还是必须显式 include-review-required？ | 本 SDD 默认 approved-only，除非显式请求。 |

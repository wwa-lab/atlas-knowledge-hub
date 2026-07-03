# 用户故事：Vector Adapter

## 状态

草稿。来源于 `docs/01-requirements/vector-adapter-requirements.md`。

## Story Map

| Story | 标题 | 需求 |
|---|---|---|
| US-VA-001 | 通过 vector adapter boundary 索引 source chunks | REQ-VA-001, REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-010 |
| US-VA-002 | 安全查看 vector adapter capabilities | REQ-VA-002, REQ-VA-003, REQ-VA-009 |
| US-VA-003 | 查询带 trace 和 review status 的 vector evidence | REQ-VA-007, REQ-VA-011 |
| US-VA-004 | 在不改变 source metadata 的情况下 deindex vector entries | REQ-VA-008, REQ-VA-012 |
| US-VA-005 | 守护 vector seam 与验证 | REQ-VA-001, REQ-VA-009, REQ-VA-010, REQ-VA-013, REQ-VA-014 |

## US-VA-001: 通过 vector adapter boundary 索引 source chunks

**Story：**
作为 delivery lead，
我希望通过可替换 vector adapter 索引 approved 或 review-aware source chunks，
以便后续 Ask 与 retrieval 工作流能使用可搜索证据，同时不绑定到单一 vector engine。

### 验收标准

1. **Given** batch 中存在 source chunks
   **When** 请求 vector indexing run
   **Then** Atlas 在任何 index 操作执行前解析 vector adapter。
2. **Given** 某个 chunk 被索引
   **When** 记录逐 chunk result
   **Then** result 包含 chunk id、file item id、source file、page/section、confidence、review status、adapter key、vector item key、status，以及 safe error（如有）。
3. **Given** 某个 chunk 为 review-required
   **When** 它被索引
   **Then** review status 仍为 review-required，index result 不得把它标记为 trusted 或 approved。
4. **Given** 自动化测试运行
   **When** 验证 vector indexing
   **Then** 测试使用 mock/in-memory vector engine，不需要真实向量库、embedding provider、网络或凭证。

### 说明 / 假设

- 现有 parser metadata 提供 source chunks 和 review status。
- 本切片索引可追溯 chunk metadata 和 vector payload；不生成 embeddings。

### 依赖

- Metadata API 与 parser adapter 的 source chunk 持久化。

### 范围外

- 真实 vector DB 执行。
- 模型/embedding provider 调用。
- Ask answer 生成。

### 开放问题

- OQ-VA-002：真实 embedding 来源仍是未来 model/worker 决策。

## US-VA-002: 安全查看 vector adapter capabilities

**Story：**
作为 platform administrator，
我希望查看 vector adapter capability 与健康 metadata，
以便确认 retrieval infrastructure 已配置，同时不暴露 secret 或私有 endpoint。

### 验收标准

1. **Given** vector adapters 已配置
   **When** capabilities 被列出
   **Then** 响应包含 adapter key、display name、version/status、supported dimensions、supported operations、default marker、masked configuration。
2. **Given** adapter unavailable 或 misconfigured
   **When** capabilities 被列出
   **Then** Atlas 返回 safe status，不包含 raw endpoint、DSN、collection name、credential、token、hostname 或私有路径。
3. **Given** 存在多个 adapters
   **When** registry 解析 adapter
   **Then** 显式 adapter key 优先，否则选择 default adapter。

### 说明 / 假设

- pgvector/vector DB 是目标类别名称，不是硬编码产品依赖。

### 依赖

- 后端 API envelope 与 adapter registry pattern。

### 范围外

- 生产 secret manager 集成。

### 开放问题

- OQ-VA-001：首个真实 vector engine 选择。

## US-VA-003: 查询带 trace 和 review status 的 vector evidence

**Story：**
作为 knowledge user，
我希望 vector similarity results 包含 source references 与 review status，
以便后续 Ask 工作流能区分可信证据与仍需 SME review 的材料。

### 验收标准

1. **Given** indexed chunks 存在
   **When** similarity query 运行
   **Then** 响应返回有界结果，包含 chunk id、file item id、source file、page/section、score、confidence、review status 与 safe metadata。
2. **Given** 没有匹配 chunks
   **When** query 运行
   **Then** Atlas 返回空结果集和 safe message，不暴露 raw vector internals。
3. **Given** 存在 review-required chunks
   **When** query 未显式 include review-required evidence
   **Then** 默认只返回 approved evidence。
4. **Given** query 显式 include review-required evidence
   **When** results 包含这些 chunks
   **Then** 结果仍清晰标记为 `REVIEW_REQUIRED`。

### 说明 / 假设

- 本切片的 query input 可使用 deterministic mock vector 或 precomputed vector。

### 依赖

- Vector index records 与 source chunk metadata。

### 范围外

- 自然语言问答、reranking、answer synthesis 或 citation formatting。

### 开放问题

- OQ-VA-003：Ask hardening 后 review-required query inclusion 的产品默认值。

## US-VA-004: 在不改变 source metadata 的情况下 deindex vector entries

**Story：**
作为 knowledge base administrator，
我希望按 space、batch、file 或 chunk 移除 vector index entries，
以便清理过期 retrieval entries，同时不删除 source metadata 或 review history。

### 验收标准

1. **Given** batch 中存在 vector entries
   **When** 请求 deindex 操作
   **Then** Atlas 只移除 vector index entries，并记录 vector run/result summary。
2. **Given** source chunks 与 file items 存在
   **When** deindex 完成
   **Then** source chunks、file item status、confidence、review status 保持不变。
3. **Given** adapter 在 deindex 期间失败
   **When** run 被报告
   **Then** Atlas 记录 safe failure details，不包含 raw engine output。

### 说明 / 假设

- Deindex 只是 metadata/index 生命周期清理。

### 依赖

- Vector run 与 per-item result records。

### 范围外

- 删除 source chunks、Wiki pages、graph nodes 或 files。

### 开放问题

- 无。

## US-VA-005: 守护 vector seam 与验证

**Story：**
作为 Codex implementation agent，
我希望有可执行 tasks 和 seam guards 支撑 vector adapter 工作，
以便实现时避免直接 engine coupling 或 secret 泄露。

### 验收标准

1. **Given** implementation tasks 被执行
   **When** guard tests 扫描非 adapter 产品层
   **Then** vector engine 名称与 SDK/client reference 在 adapter scope 外被禁止。
2. **Given** 扫描 vector adapter package
   **When** seam guard 运行
   **Then** vector-engine 名称只可出现在 vector adapter boundary 或 implementation 中；除非未来 worker contract 明确批准，否则 outbound network clients 仍被禁止。
3. **Given** final verification 运行
   **When** `mvn verify`、seam scans、diff hygiene、secret/private-path scans 完成
   **Then** 可带 evidence 报告结果，且不得暗示未运行检查已通过。

### 说明 / 假设

- 现有 `AdapterSeamGuardTest` 已全局禁止 `pgvector`、`Milvus`、`Qdrant`；本切片必须有意地仅为 adapter scope 更新它。

### 依赖

- 已接受的 SDD docs 与后端测试 harness。

### 范围外

- 远程服务、付费任务、外部云依赖或生产凭证。

### 开放问题

- 无。

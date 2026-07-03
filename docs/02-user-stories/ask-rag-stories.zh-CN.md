# 用户故事：Ask RAG

## 状态

草稿。由 `docs/01-requirements/ask-rag-requirements.md` 通过 `req-to-user-story` 推导。

## 故事映射

| Story | 标题 | 需求 |
|---|---|---|
| US-ASKRAG-001 | 在 Knowledge Space 内限定范围提问 | REQ-ASKRAG-001, REQ-ASKRAG-010 |
| US-ASKRAG-002 | 查看带来源的回答和证据 | REQ-ASKRAG-002, REQ-ASKRAG-003, REQ-ASKRAG-005, REQ-ASKRAG-013 |
| US-ASKRAG-003 | 无 approved evidence 时拒绝回答 | REQ-ASKRAG-012 |
| US-ASKRAG-004 | 通过 adapter 边界执行 Ask | REQ-ASKRAG-004, REQ-ASKRAG-011 |
| US-ASKRAG-005 | 保留 review-required 输出和审计证据 | REQ-ASKRAG-006, REQ-ASKRAG-009 |
| US-ASKRAG-006 | 校验并安全失败 | REQ-ASKRAG-007, REQ-ASKRAG-008, REQ-ASKRAG-014 |

## US-ASKRAG-001：在 Knowledge Space 内限定范围提问

**Story:** 作为知识使用者，我希望在选定 Knowledge Space 内提问，以便回答基于我正在查看的空间。

### 验收标准

1. **Given** 我正在查看某个 Knowledge Space 的 Ask tab
   **When** 我提交问题
   **Then** 请求限定到该 space id，且不会搜索无关空间。
2. **Given** Ask tab 已渲染
   **When** 我使用键盘导航或窄屏视口
   **Then** 问题输入、提交控件、回答面板和证据列表仍可访问且可读。
3. **Given** UI 处于 day 或 night mode
   **When** answer、empty state 或 error state 出现
   **Then** 状态符合当前 Atlas FE 基线，且不会重叠文字或隐藏证据。

## US-ASKRAG-002：查看带来源的回答和证据

**Story:** 作为开发者或 SME reviewer，我希望每个 Ask 回答都展示 source references 和 confidence，以便核验回答来源。

### 验收标准

1. **Given** 存在 approved evidence
   **When** Ask 返回回答
   **Then** 回答包含 confidence、review status，以及带 source file、page 或 section、source chunk id 和 score 的 evidence list。
2. **Given** review-required evidence 被显式包含
   **When** Ask 返回混合 evidence
   **Then** API 和 UI 分离 trusted evidence 与 review-required evidence，并展示警告。
3. **Given** 返回了生成回答
   **When** 回答被展示或作为 evidence 持久化
   **Then** answer output 仍为 `REVIEW_REQUIRED`。

## US-ASKRAG-003：无 approved evidence 时拒绝回答

**Story:** 作为知识使用者，我希望 Atlas 拒绝无依据回答，以免把未审核或缺失证据误认为可信知识。

### 验收标准

1. **Given** 没有 approved evidence 匹配问题
   **When** 我使用默认 review policy 提交问题
   **Then** Ask 返回 no-evidence 响应，且不调用 model adapter 合成回答。
2. **Given** 只有 review-required evidence 匹配
   **When** 我使用默认 approved-only policy
   **Then** Ask 说明需要 approved evidence。
3. **Given** 显式启用了 review-required evidence inclusion
   **When** 返回 evidence
   **Then** 所有 answer 和 evidence 都清晰标记为 review-required。

## US-ASKRAG-004：通过 adapter 边界执行 Ask

**Story:** 作为平台管理员，我希望 Ask 使用 vector 和 model adapter 契约，以便模型与检索引擎可替换且可安全 mock。

### 验收标准

1. **Given** Ask 请求通过校验
   **When** retrieval 开始
   **Then** 服务使用 vector query contract，且不直接调用 vector engine。
2. **Given** 有可用 evidence
   **When** answer generation 开始
   **Then** 服务使用 model-adapter contract，且不直接调用 provider SDK、CLI 或外部 HTTP client。
3. **Given** 自动化测试运行
   **When** Ask 行为被验证
   **Then** 测试使用确定性 mock adapter，不需要 credential 或网络访问。

## US-ASKRAG-005：保留 review-required 输出和审计证据

**Story:** 作为 delivery lead，我希望 Ask activity 可审计且不泄漏机密数据，以便后续 review 可信回答。

### 验收标准

1. **Given** Ask 成功完成
   **When** answer evidence record 被存储
   **Then** 它包含 safe question summary、answer status、review policy、evidence references、model run id、timestamps 与 requested-by。
2. **Given** 存在生成回答
   **When** 检查记录
   **Then** Wiki、source chunk、graph、file 和 review 状态都不变。
3. **Given** 检查日志或响应
   **When** Ask 已处理请求
   **Then** 不存在 raw secret、provider payload、private path、stack trace 或机密 source text。

## US-ASKRAG-006：校验并安全失败

**Story:** 作为 operator，我希望无效或失败的 Ask 请求返回安全、可操作错误，以便用户理解发生了什么且不泄漏内部信息。

### 验收标准

1. **Given** 请求含 invalid space、empty question、oversized question、invalid policy、unsafe filter 或 invalid limit
   **When** 请求提交
   **Then** API 在 adapter 执行前以 `VALIDATION_ERROR` envelope 拒绝。
2. **Given** vector 或 model adapter 不可用或失败
   **When** Ask 处理失败
   **Then** 响应包含 sanitized safe message，且没有 raw adapter diagnostics。
3. **Given** 实现完成
   **When** 运行验证
   **Then** unit、integration、API contract、E2E、seam guard、dependency/network 和 secret/private-path 检查通过，或明确报告 blocked。

## 依赖

- Metadata API 的 source chunk、review status 和 envelope 行为。
- Vector adapter query evidence contract。
- Model adapter mock run contract。
- 静态原型和 Vue shell 中的 FE Ask tab 基线。

## 范围外

- 真实 provider credential、真实外部网络调用、流式输出、answer publication、graph extraction 和生产 SSO/RBAC 配置。

## 待确认问题

- OQ-ASKRAG-001：review-required evidence 对非 reviewer 的可见性。
- OQ-ASKRAG-002：Ask answer 后续是否进入 review queue。
- OQ-ASKRAG-003：首版实现是否包含 reranking。

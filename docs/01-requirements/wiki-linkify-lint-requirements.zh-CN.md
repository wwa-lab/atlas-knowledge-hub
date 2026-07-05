# 需求：Wiki 自动互链与质量检查

## 状态

供用户审阅的草稿。在这套双语 SDD 被接受前，不得开始产品代码实现。

## 目标

增加一个确定性的 Wiki 维护流程，用于安全注入 Wiki 链接、刷新页面链接元数据、发现基础 Wiki 质量问题，并在不改变信任或发布规则的前提下展示面向审核的问题统计。

## 范围

范围内：

- 针对单个知识空间内现有 Wiki 页面的 linkify/lint 运行。
- 基于 slug 和 aliases 注入 `[[wiki-link]]`。
- 跳过 YAML frontmatter、fenced code block、inline code、已有 Wiki link、Markdown link 和图片。
- 更新 `inLinks` 与 `outLinks` 元数据。
- 检测 broken Wiki link、orphan page、missing source ref、stale source ref、thin content。
- 将安全问题写入 `wiki_page_issue`。
- 通过现有 Wiki run/log 表记录运行和日志证据。
- 在 Processing Center 展示 issue 统计，并在 Wiki 页面展示 issue 详情。

范围外：

- SME 审核门、可信发布流程变更、refresh/retract、connector sync、model-assisted linting、生产 RBAC、外部调用、真实公司数据。

## 需求

| ID | 需求 | 验收 |
|---|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | 实现前必须具备并接受完整双语 SDD。 | EN 与 `.zh-CN.md` 产物存在，REQ/US/T ID 一致；SDD 阶段不改产品代码。 |
| REQ-WIKI-LINKIFY-LINT-002 | 运行必须限定在单个知识空间。 | API 拒绝未知空间，且不会读写空间外页面。 |
| REQ-WIKI-LINKIFY-LINT-003 | link target 必须来自同空间当前页面的 slug 和 aliases。 | 测试证明不会链接跨空间页面。 |
| REQ-WIKI-LINKIFY-LINT-004 | 每个 source page 对每个 target slug 最多注入一个链接。 | 同页内重复 alias 只产生一个 outbound link。 |
| REQ-WIKI-LINKIFY-LINT-005 | linkify 必须跳过受保护 Markdown 区域。 | 测试覆盖 YAML frontmatter、fenced code、inline code、已有 Wiki link、Markdown link 和图片。 |
| REQ-WIKI-LINKIFY-LINT-006 | link metadata 必须确定性且幂等。 | 对同一内容重复运行不会重复链接，也不会非预期改变链接顺序。 |
| REQ-WIKI-LINKIFY-LINT-007 | lint 必须检测 broken Wiki links。 | 断开的 `[[slug]]` 引用会生成 `BROKEN_LINK` issue。 |
| REQ-WIKI-LINKIFY-LINT-008 | lint 必须检测 orphan pages。 | 没有 incoming links 的非 index/root 页面会生成 `ORPHAN_PAGE` issue。 |
| REQ-WIKI-LINKIFY-LINT-009 | lint 必须检测 missing source refs 和 thin content。 | 无安全 source/chunk refs 的页面生成 `MISSING_SOURCE_REF`；内容过薄页面生成 `THIN_CONTENT`。 |
| REQ-WIKI-LINKIFY-LINT-010 | lint 必须只用确定性元数据检测 stale source refs。 | 缺失的 source document 或 source chunk 引用生成 `STALE_SOURCE`；不需要 parser/model 调用。 |
| REQ-WIKI-LINKIFY-LINT-011 | run、log、issue 证据必须安全。 | 响应和日志只包含计数、ID、标签、安全 locator；不包含 raw source text、secret、私有路径、stack trace、prompt 或 provider payload。 |
| REQ-WIKI-LINKIFY-LINT-012 | 现有 trusted publish 和 generated draft 行为不得回退。 | 现有 Wiki list/detail/publish 行为，以及 generated `REVIEW_REQUIRED` draft 展示继续通过测试。 |
| REQ-WIKI-LINKIFY-LINT-013 | Processing Center 与 Wiki UI 必须展示 issue counts，但不能暗示生产就绪。 | Vue 将 issue count/status 展示为面向审核的 warning，而非自动批准。 |
| REQ-WIKI-LINKIFY-LINT-014 | 验证必须覆盖后端契约、持久化、前端展示、幂等性和安全扫描。 | 任务验证包含 backend tests、frontend tests/build/E2E（实现涉及时）、`git diff --check`、secret/path scan、network/dependency scan。 |

## 约束

- 默认确定性；不调用模型供应商、parser、converter、vector、search 或外部网络。
- 所有 Wiki 页面保留 source trace、confidence、review status。
- 使用现有 adapter 边界和 local artifact storage 模式。
- 仅使用 mock/sample-safe 数据。
- 本切片完成只代表 Wiki Foundation 的 linkify/lint 能力，不代表 internal beta 或 production readiness。

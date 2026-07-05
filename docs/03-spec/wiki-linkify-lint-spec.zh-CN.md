# 规格：Wiki 自动互链与质量检查

## 状态

当前用户已接受，并已按 Wave 1 `wiki-linkify-lint` 的 deterministic 成熟度目标实现。本状态不代表 production readiness。

## 概述

`wiki-linkify-lint` 在 `wiki-ingest-v0` 之后增加确定性的 Wiki 维护 run。它读取单个空间的当前 Wiki 页面，构建 slug/alias target index，安全地把 `[[slug]]` 链接插入 Markdown artifact，更新 `inLinks` 和 `outLinks`，检测质量问题，并记录安全 run/log/issue 证据。它不批准、不发布、不信任化、不 refresh/retract，也不使用模型生成内容。

## 角色

| Actor | Role |
|---|---|
| Knowledge manager | 启动 run 并查看 issue counts。 |
| SME reviewer | 使用 issue 和 link metadata 决定编辑。 |
| Knowledge user | 看到 review warning，但不会看到新的信任声明。 |
| Implementation agent | 仅在 SDD 被接受后实现。 |

## 行为

### S1. SDD 接受门

- 实现前必须存在双语 SDD 集。
- 用户接受本 SDD 前，implementation 被阻塞。
- 切片状态不得宣称 production readiness。

### S2. 运行选择

- run 限定在一个 `spaceId`。
- 默认页面范围包含 published 与 generated review-required pages。
- 可选 request filters 可以限定 page ids。
- 跨空间 page ids 以安全 validation error 拒绝。

### S3. Target Index

- link targets 来自同一空间的 pages。
- 主 target key 为 `slug`。
- Alias keys 会规范化，但解析回 canonical target slug。
- 有歧义的 aliases 不自动 link；相关页面使用现有 issue taxonomy 记录 `REVIEW_REQUIRED` issue。

### S4. Linkify 规则

- 对 source page 中每个 target 的第一个合格 mention 插入 `[[target-slug]]`。
- 不把页面链接到自己。
- 同一 source page 对同一 target slug 不插入超过一个 outbound link。
- 除插入链接外保留 Markdown 内容。
- 跳过：
  - YAML frontmatter；
  - fenced code blocks；
  - inline code；
  - 已有 `[[wiki-link]]`；
  - 标准 Markdown links；
  - Markdown images。
- 如果 Markdown artifact 无法安全读取，不重写该页面；记录安全 issue/log entry。

### S5. Link Metadata

- `outLinks` 存储 source page 指向的 canonical target slugs。
- `inLinks` 存储 target page 的 canonical source slugs。
- Link arrays 按确定性顺序排序且不重复。
- 对同一内容重复运行保持幂等。

### S6. Lint 规则

| Issue | Issue type | Rule |
|---|---|---|
| Broken Wiki link | `BROKEN_LINK` | 已有 `[[slug]]` 或计算出的 outbound link 在同空间没有 target。 |
| Orphan page | `ORPHAN_PAGE` | 页面没有 incoming links，且不是 index/root page。 |
| Missing source trace | `MISSING_SOURCE_REF` | 页面没有 `sourceRefs` 且没有 `chunkRefs`。 |
| Thin content | `THIN_CONTENT` | Markdown body 在去除 markup 后为空或低于接受的字数/字符阈值。 |
| Stale source | `STALE_SOURCE` | 引用的 source document 或 source chunk 已不在 metadata 中。 |
| Ambiguous alias or unsafe rewrite | `REVIEW_REQUIRED` | 确定性 linkify 无法安全选择 target 或无法读写 artifact。 |

### S7. 证据与安全

- schema contract 扩展后，每个 run 写入 mode 为 `linkify-lint` 的 `wiki_generation_run`。
- 生命周期事件使用 `wiki_log_entry`。
- Issues 使用 `wiki_page_issue`，状态为 `OPEN`。
- 重复 run 应更新或替换同一 page/type/evidence 的 open lint issue，避免无限重复。
- Logs、issues、API responses 只包含安全 metadata。

### S8. UI 行为

- Processing Center 展示 Wiki issue totals 以及 blocking/non-blocking 状态。
- Wiki page detail 为所选页面展示 issue badges 或 warnings。
- UI 文案必须把 issues 表达为面向审核的 quality warnings。
- Generated `REVIEW_REQUIRED` pages 保持 draft/review-required。

## API Surface

完整 payload 位于 `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md`。

| Interface | Behavior |
|---|---|
| `POST /api/spaces/{spaceId}/wiki-linkify-lint-runs` | 为一个空间启动确定性 linkify/lint。 |
| `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}` | mode support 扩展后复用安全 run summary read shape。 |
| `GET /api/spaces/{spaceId}/wiki-page-issues` | 按过滤条件列出空间级 Wiki issues。 |
| `GET /api/wiki-pages/{wikiPageId}/issues` | 继续支持已有 page issue endpoint。 |
| `GET /api/spaces/{spaceId}/review-queues` | Processing Center 可包含 Wiki issue summary counts。 |

## 验收矩阵

| Requirement | Observable check |
|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | 完整双语 SDD 存在，且代码前记录用户接受。 |
| REQ-WIKI-LINKIFY-LINT-002 | API/service tests 拒绝跨空间操作。 |
| REQ-WIKI-LINKIFY-LINT-003 | 测试证明 target index 基于同空间 slug/alias。 |
| REQ-WIKI-LINKIFY-LINT-004 | 测试证明每页每 target 只有一个 outbound link。 |
| REQ-WIKI-LINKIFY-LINT-005 | Markdown protected-region tests 通过。 |
| REQ-WIKI-LINKIFY-LINT-006 | 幂等性测试通过。 |
| REQ-WIKI-LINKIFY-LINT-007 | Broken link issue 测试通过。 |
| REQ-WIKI-LINKIFY-LINT-008 | Orphan page issue 测试通过。 |
| REQ-WIKI-LINKIFY-LINT-009 | Missing source 和 thin content issue 测试通过。 |
| REQ-WIKI-LINKIFY-LINT-010 | Stale source metadata 测试通过，且无 parser/model call。 |
| REQ-WIKI-LINKIFY-LINT-011 | 安全扫描和 log/response 断言通过。 |
| REQ-WIKI-LINKIFY-LINT-012 | 现有 publish/list/ingest 测试通过。 |
| REQ-WIKI-LINKIFY-LINT-013 | Vue tests/E2E 展示 issue counts 与 warnings。 |
| REQ-WIKI-LINKIFY-LINT-014 | 最终验证证据列出已运行与跳过的检查。 |

## 已解决决策

- Linkify/lint 是确定性且本地的。
- Issue taxonomy 复用现有 `wiki_page_issue` enum values。
- `wiki_generation_run.mode` 需要增量 schema 更新以允许 `linkify-lint`。
- Linkify 更新页面 link metadata；不改变 review status 或 trust state。

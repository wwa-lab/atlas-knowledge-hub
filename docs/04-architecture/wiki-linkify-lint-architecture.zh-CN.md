# 架构：Wiki 自动互链与质量检查

## 状态

供用户审阅的草稿。

## 组件归属

| Component | Responsibility |
|---|---|
| `WikiLinkifyLintController` | 接收空间级 run request，并返回 Atlas envelope。 |
| `WikiLinkifyLintService` | 编排 target indexing、Markdown rewrite、lint、run evidence、logs 和 issues。 |
| Markdown linkifier utility | 执行确定性、感知受保护区域的链接插入。 |
| Lint rule utilities | 评估 broken links、orphan pages、missing refs、stale refs、thin content、ambiguous aliases。 |
| Existing repositories | 读写 `wiki_page`、`wiki_generation_run`、`wiki_log_entry`、`wiki_page_issue`、source documents 和 chunks。 |
| Existing artifact storage | 通过当前本地 storage abstraction 读写 generated/published Markdown artifacts。 |
| Vue Processing Center / Wiki tab | 展示 issue totals 和 page-level warnings。 |

## 边界

- 不直接调用 parser、converter、model、vector、storage engine 或 search provider。
- Artifact 读写使用现有 local artifact storage service 或产品侧 wrapper。
- 不引入 external cloud call 或 provider-backed generation。
- 现有 review/publish 与 ingest APIs 保持兼容。

## 持久化变化

实现预计只增加一个小的 Flyway migration：

- 扩展 `ck_wiki_generation_run_mode`，加入 `linkify-lint`。
- 如需要，增加按空间列出 issue 和 issue 去重的 repository 支持。

除非实现证明现有 issue/run/log tables 无法安全支撑契约，否则不新增表。

## 安全与数据安全

- 响应包含 ids、counts、issue types、severity、status 和 safe locators。
- Raw Markdown body content 不应复制到 logs 或 issue messages。
- 私有绝对路径、stack traces、secrets、prompts、provider payloads、真实公司数据均禁止。
- Validation errors 应可操作但已脱敏。

## 架构评审说明

- API contract changes 为增量。
- Persistence change 限于 run mode constraint 和可选 query support。
- 保留 source trace、confidence、review status。
- Generated pages 保持 `REVIEW_REQUIRED`；published pages 仅在原本可信时保持 published。
- 本切片不引入生产 auth/RBAC。

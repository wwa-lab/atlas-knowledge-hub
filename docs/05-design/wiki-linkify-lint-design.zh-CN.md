# 设计：Wiki 自动互链与质量检查

## 状态

供用户审阅的草稿。

## 用户体验

本切片为现有 Wiki 表面增加维护可见性：

- Processing Center 按类型展示 Wiki issue totals。
- Wiki index/page detail 继续展示 slug、source mode、refresh policy、confidence、review status、link counts。
- 选中 Wiki page 可以显示 broken links 或 missing source trace 等 review warnings。
- Warnings 不改变 approval state，也不暗示 production readiness。

## 后端设计

### Run Service

Run service 应：

1. 校验 space 和可选 page scope。
2. 加载该空间当前 pages。
3. 由 slug 与 aliases 构建 target index。
4. 通过安全 storage 读取 Markdown artifacts。
5. 应用感知 protected region 的 linkify。
6. 重新计算 inbound/outbound link arrays。
7. 执行 lint rules。
8. 持久化 page metadata、issues、logs、run summary。

### Linkifier

Linkifier 应是纯确定性 utility，并有聚焦单元测试。它应返回：

- rewritten Markdown；
- 已插入的 target slugs；
- 已存在 Wiki link slugs；
- 对诊断有用时返回 skipped/protected segments count。

### Lint Rules

Rules 应是小函数，接收已加载 metadata 并返回 issue candidates。它们不应读取文件、调用 provider 或直接 mutate entities。

## 前端设计

Processing Center：

- 增加 Wiki issue row 或 group，展示 broken links、orphan pages、source issues、thin content 的 counts。
- 使用现有 issue visual language 和 review-facing wording。

Wiki page：

- 显示 page issue count。
- 为所选页面显示安全 issue type、severity、status。
- 保持 source trace 和 review status 可见。

## 错误设计

| Error | User-facing result |
|---|---|
| Unknown space | 安全 not-found envelope。 |
| Cross-space page filter | Validation error。 |
| Unreadable Markdown artifact | Partial failure run，并生成 `REVIEW_REQUIRED` issue。 |
| Unexpected failure | 尽可能写入脱敏 `FAILED` run；response 不包含 stack trace。 |

## 实现边界

- 除链接插入外，不实现 auto-fix suggestions。
- 不批准或发布 generated pages。
- 不引入新的 Markdown parser dependency，除非测试证明本地确定性 utility 不足且该 dependency 被明确接受。

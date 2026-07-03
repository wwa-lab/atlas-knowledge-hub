# 架构：Review Publish

## 状态

草稿。通过 `spec-to-architecture` 与 `architecture-to-design` 推导。

## 系统上下文

Review Publish 位于 metadata ingestion/review 与可信下游知识消费之间。它加固已有 metadata API 能力，并为 Wiki metadata 增加发布专用读写边界。

```text
┌──────────────┐
│ SME / Admin  │
└──────┬───────┘
       ▼
┌──────────────────────┐
│ FE Processing Center │
│ Wiki publish states  │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ Metadata API         │
│ Review + Publish     │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ Domain Services      │
│ Eligibility + Audit  │
└──────┬───────────────┘
       ▼
┌──────────────────────┐
│ PostgreSQL Metadata  │
│ files/chunks/wiki    │
└──────────────────────┘
```

## 组件边界

| Component | 职责 | 边界 |
|---|---|---|
| Frontend Processing Center | 展示 blocked queues、publish-ready counts 和 publish outcomes。 | 只消费 API metadata；不直连 engine。 |
| Review API | 追加 review records 并更新 review status。 | 已有 file review 行为保持 append-only。 |
| Publish API | 校验发布资格，并创建/更新 Wiki page metadata。 | 不修改 raw parser output 或 source chunks。 |
| Review/Publish Domain Service | 负责状态转换、eligibility rules 和用户安全失败。 | 依赖 repositories 和 metadata DTOs，不依赖 adapter engines。 |
| Metadata Persistence | 存储 file items、source chunks、review records 和 wiki pages。 | 仅相对路径；无 secret 或真实文档内容。 |

## 现有代码校验

- `ReviewStatus` 已包含 `REVIEW_REQUIRED`、`APPROVED`、`NEED_FIX`、`OCR_REQUIRED`、`PUBLISHED`，见 `backend/src/main/java/com/atlas/metadata/enums/ReviewStatus.java:4`。
- `ReviewAction.resultingStatus()` 已将 review actions 映射到非 published review states，见 `backend/src/main/java/com/atlas/metadata/enums/ReviewAction.java:10`。
- `ReviewService` 已追加 file review records 并更新 file review status，见 `backend/src/main/java/com/atlas/metadata/service/ReviewService.java:48`。
- `WikiPage` 已作为 deferred metadata 存在，包含 `markdownPath`、`sourceDocumentIds`、`confidence`、`reviewStatus`，见 `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:15`。

## 架构决策

| Decision | 理由 |
|---|---|
| Publish 是 metadata transition，不是 parser/converter execution。 | 保留 adapter boundary，并让 Phase 4 hardening 聚焦。 |
| `PUBLISHED` 只能由 publish 设置。 | 防止 review action 静默发布内容。 |
| Wiki page metadata 与 raw source chunks 分离。 | 保留 source trace 和原始证据。 |
| Graph 和 Ask 是下游消费者。 | 避免与 `knowledge-graph` 和 `ask-rag` 切片冲突。 |
| API guide 必需。 | Phase 4 引入新 endpoints。 |

## 安全与数据安全

- 不引入外部云调用或新网络依赖。
- 不引入 raw secrets、private absolute paths、logs 或真实公司文档。
- Error responses 必须用户安全并使用 envelope。
- 本切片不实现生产 RBAC；在安全切片接受真实 enforcement 前，endpoints 记录为 internal/trusted。

## 风险

- Bulk publish 可能让实现超过一次可评审范围。
- 如果 SME review 发生在 file 以下粒度，可能需要 chunk-level publication。
- 生产暴露前必须完成 Auth/RBAC。

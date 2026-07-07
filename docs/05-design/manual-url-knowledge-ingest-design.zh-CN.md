# 设计：manual-url-knowledge-ingest

## Overview

实现会围绕现有 Atlas metadata flows 增加 manual URL source domain。Registration 验证并脱敏 URL，存储 `manual_url_source`，并创建 review-required batch/file/chunk metadata，让 URL 出现在既有 operational surfaces 中，但不会成为 trusted content。

## Backend Design

| Module | Design |
|---|---|
| `ManualUrlSource` domain | JPA entity，使用 factory method，避免 public setters。 |
| `ManualUrlSourceRepository` | Space-scoped list 与 id lookup。 |
| `ManualUrlSourceService` | 负责 validation、sanitization、duplicate prevention、metadata creation 与 response mapping。 |
| `ManualUrlSourceController` | 在 `/api` 下暴露 create/list/get endpoints。 |
| DTOs | Request 与 response records 使用现有 `ApiEnvelope<T>`。 |

## Validation Design

- 使用 `java.net.URI` 解析。
- 要求 `https`。
- 拒绝 userinfo、query 与 fragment。
- 拒绝 missing host、`localhost`、internal suffixes、private IPv4 ranges、loopback、link-local 与 unique-local IPv6。
- 从 scheme、host、可选 port 与有边界 path 构造 `displayUrl`。
- 返回 `RequestValidationException`，field-level messages 不回显 raw unsafe URLs。

## Metadata Creation Design

成功登记时：

1. 创建 `manual_url_source`。
2. 创建 `SourceKind.url` 的 `Batch`。
3. 创建 `SourceType.url`、`FileStatus.REVIEW_REQUIRED`、`ReviewStatus.REVIEW_REQUIRED`、confidence `0.300` 的 `FileItem`。
4. 创建 `SourceChunk`，source file 为 safe display URL，section 为 `Manual URL metadata`，`ReviewStatus.REVIEW_REQUIRED`。
5. 把 manual source 关联到 `batchId` 与 `fileItemId`。

该设计满足 `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` 与 `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006`。

## Frontend Design

| Area | Design |
|---|---|
| Types | 增加 `ManualUrlSource`、status unions 与 create request types。 |
| API client | 使用现有 envelope 与 safe error handling 添加 list/create/get helpers。 |
| Mock fallback | 增加 sample-safe URL source，且只保持 `REVIEW_REQUIRED` status。 |
| UI | 在 `App.vue` Documents/Processing Center 区域增加紧凑 form 与 status list。 |
| Tests | 覆盖 rendering、submit path、validation-safe error state 与 E2E smoke。 |

## Error Handling

Backend validation failures 使用现有 safe error infrastructure。Frontend 展示通用安全文案与 field summaries，不展示 raw rejected URLs。

## Testing Considerations

- `T-MANUAL-URL-KNOWLEDGE-INGEST-001` 至 `T-MANUAL-URL-KNOWLEDGE-INGEST-008` 定义可执行实现顺序。
- Backend tests 覆盖 service validation 与 API contract behavior。
- Frontend tests 覆盖 rendering 与 state mapping。
- E2E 覆盖 sample-safe URL 的 manual URL registration happy path。

## Risks And Tradeoffs

- 本切片仅存 metadata，用户可能期待 content fetch。UI copy 与 `fetchPolicy` 必须说明 metadata-only。
- 现有 review queue 基于 file item；URL sources 通过 review-required file metadata 进入队列，而不是新增 queue type。

## Acceptance IDs

`AC-MANUAL-URL-KNOWLEDGE-INGEST-001`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-003`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-005`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`.

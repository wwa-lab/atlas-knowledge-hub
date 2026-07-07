# 架构：manual-url-knowledge-ingest

## Overview

`manual-url-knowledge-ingest` 是 additive metadata slice。它把 manual URL source registration 放在 Atlas backend API 之后，验证并脱敏 URL metadata，持久化 source state，并创建可被现有 Processing Center 与 Wiki surfaces 消费的 review-required batch/file/chunk metadata。

## Architecture Drivers

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` 需要用户可见 registration path。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` 需要持久化 URL source metadata。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` 禁止真实 external fetch。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` 需要 trace preservation。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` 要求仅 additive behavior。

## High-Level Architecture

```text
User
  |
  v
Vue Knowledge Space / Processing Center
  |
  | REST / JSON
  v
Spring Boot API
  |
  +-- Manual URL Controller
  |     validates request and returns ApiEnvelope
  |
  +-- Manual URL Service
  |     sanitizes URL, records fetch intent, creates review-required metadata
  |
  +-- Existing Batch/File/SourceChunk Services
  |     exposes URL source to Processing Center and future Wiki ingest
  |
  v
PostgreSQL + Flyway
```

## Components

| Component | Responsibility |
|---|---|
| Manual URL UI panel | 提交 sample-safe URL 并展示 source status/trace。 |
| Manual URL API client | 调用新 REST endpoints 并映射 safe error states。 |
| Manual URL controller | 在 `/api` 下暴露 additive endpoints，使用 `ApiEnvelope<T>`。 |
| Manual URL service | 验证 URL、脱敏 display metadata、创建 source record 与 review-required metadata artifacts。 |
| Manual URL repository | 持久化 URL source state。 |
| Existing batch/file/chunk repositories | 存储 downstream review-required metadata artifacts。 |
| Existing review/Wiki surfaces | 读取 review-required metadata，但不绕过审核。 |

## Boundary Rules

- 本切片不调用 parser/converter/model/vector/storage adapter。
- 不执行外部网络调用。
- 现有 auth guard、rate limit interceptor、safe error handler 与 API envelope conventions 不变。
- URL source registration 为 additive，不改变 Wiki publish、Ask 或 Graph eligibility。

## Security And Data Safety

- Raw submitted URL 经验证，validation failure 时不返回。
- Accepted URL metadata 去除 query 与 fragment，并拒绝 userinfo。
- 拒绝 internal-looking hosts。
- 不持久化 credential、cookie、private endpoint 或 raw source content。
- Review status 默认为 `REVIEW_REQUIRED`。

## Risks

| Risk | Mitigation |
|---|---|
| Manual URL metadata 可能被误认为 approved content。 | 持久化并展示 `REVIEW_REQUIRED` 与 eligibility metadata；不自动发布。 |
| URL path 可能包含敏感 token。 | Query/fragment 被拒绝；path 有边界；validation tests 覆盖 redaction。 |
| 未来 connector-sync-v0 可能需要更丰富 fetch semantics。 | 现在只记录 fetch intent 与 policy，真实 fetch 保持范围外。 |

## Traceability

本架构支持 `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` 至 `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`。

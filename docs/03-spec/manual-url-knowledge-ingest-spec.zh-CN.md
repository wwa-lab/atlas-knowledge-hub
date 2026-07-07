# 规格：manual-url-knowledge-ingest

## 范围

`manual-url-knowledge-ingest` 增加 metadata-only manual URL source registration path。它创建安全 URL source record 与 review-required metadata artifacts，可展示在 Knowledge Space / Processing Center / Wiki surfaces。本切片不抓取远程内容。

## Actors

- 登记 URL 的 Knowledge Space contributor。
- 审核 URL-derived metadata 的 SME reviewer。
- 验证无 unsafe data 或 downstream approval bypass 的 Atlas maintainer。

## 功能需求

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` 系统应为选定 Knowledge Space 暴露 manual URL registration action。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` 系统应持久化 URL source metadata，包括 `sourceId`、`spaceId`、`displayUrl`、`host`、`title`、`description`、`fetchIntent`、`fetchPolicy`、`ingestStatus`、`reviewStatus`、`confidence`、`eligibilityStatus`、`sourceTrace`、`batchId` 与 `fileItemId`。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` 系统应在持久化前验证 URL，并用 field-specific validation errors 拒绝 unsafe input。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` 系统在登记时不得执行 external HTTP calls 或获取页面内容。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` 系统应创建映射到既有 batch/file/source-chunk models 的 metadata artifacts，且 review status 为 `REVIEW_REQUIRED`。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` 系统应从 URL source 到 metadata artifacts 保留 source trace。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` 系统应脱敏 unsafe URL components，且绝不返回 raw credentials、query strings、fragments、cookies 或 private endpoint values。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` 系统应保持 additive，不改变现有 upload、review、Wiki、Ask、Graph、auth、rate-limit 或 safe-error 语义。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` 前端应展示 registration controls、source status、review-required output 与 source trace。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` Backend、frontend 与 E2E tests 应覆盖已接受行为。

## URL Validation Rules

为满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-004`，接受规则刻意保守。

| Input aspect | Rule |
|---|---|
| Scheme | 仅接受 `https`。 |
| Userinfo | 任何 username/password component 均拒绝。 |
| Query/fragment | 拒绝；不存储、不回显。 |
| Host | 必填；归一化为小写。 |
| Local/private host | 拒绝 `localhost`、`.local`、`.internal`、`.corp`、private IPv4 ranges、loopback、link-local 与 unique-local IPv6。 |
| Display path | 可保存短路径；query 与 fragment 永远剥离。 |

Edge trace：

- `https://example.com/reference/page` -> 接受并展示为 `https://example.com/reference/page`。
- `http://example.com` -> 因 unsupported scheme 拒绝。
- `https://user:pass@example.com/a` -> 因 credential-bearing URL 拒绝。
- `https://localhost/wiki` -> 因 private/internal-looking host 拒绝。

## 状态映射

| Manual URL status | Meaning | Downstream mapping |
|---|---|---|
| `REGISTERED` | Metadata 已保存且未 fetch。 | Batch/file metadata 存在，且为 review-required。 |
| `FETCH_INTENT_RECORDED` | 用户的未来 fetch intent 已记录。 | 本切片不执行真实 fetch。 |
| `REVIEW_REQUIRED` | URL-derived output 需要 SME review。 | Processing Center 可展示 review-required item。 |
| `REJECTED` | Validation 在持久化前失败。 | 不创建 source record。 |

## UI Behavior

- Knowledge Space Documents 或 Processing Center surface 包含紧凑 manual URL ingest form。
- Form 接受 URL、可选 title、可选 description 与 fetch intent。
- 成功登记后，UI 展示 safe display URL、host、ingest status、review status、confidence、eligibility status 与 source trace。
- UI 不得渲染 URL query、fragment、credential userinfo、cookies 或 private endpoint values。
- Mock-safe fallback data 只可展示 sample URLs。

## API Behavior

- `POST /api/spaces/{spaceId}/manual-url-sources` 登记一个 URL source。
- `GET /api/spaces/{spaceId}/manual-url-sources` 列出某 Knowledge Space 的 URL sources。
- `GET /api/manual-url-sources/{sourceId}` 返回一个 source。
- Responses 使用 `ApiEnvelope<T>`。
- Validation errors 使用现有 safe error envelopes。

## 验收矩阵

| Acceptance | Requirements | Tests |
|---|---|---|
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` | API contract + frontend submit test |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | API contract field assertions |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | Service/integration tests |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` | Validation/redaction tests |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` | Vitest + Playwright |
| `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008`, `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` | Full verification commands |

## 非功能需求

- 无外部网络依赖。
- Response、log、test、doc 中无 raw URL secret 或 private endpoint。
- 现有 safe errors 与 rate limits 保持生效。
- API contract 为 additive。

## 待解问题

本切片无待解问题。未来 connector-sync-v0 将定义 real fetch、robots/compliance、connector credentials 与 scheduled refresh 语义。

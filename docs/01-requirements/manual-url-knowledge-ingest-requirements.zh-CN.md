# 需求：manual-url-knowledge-ingest

## 切片契约

- **切片：** `manual-url-knowledge-ingest`
- **Wave：** Wave 5 / Connector And Operations
- **目标：** 允许用户手动登记一个 URL 作为知识来源，并以安全、可审核、可追踪的方式把 URL metadata、fetch intent、source trace、ingest status 和 review-required output 接入现有 Knowledge Space、Wiki、Processing Center 与审核流程。
- **成熟度：** 原型级 metadata/API-backed foundation；不是 connector sync、爬虫、headless rendering、browser automation、生产合规或生产 connector readiness。

## 范围内

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` Knowledge Space 用户可以从产品 UI 入口登记一个 manual URL source。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` 后端持久化安全 URL metadata、normalized display URL、source trace、fetch intent、fetch policy、ingest status、review status、confidence 与 eligibility metadata。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` URL validation 拒绝不安全 scheme、URL userinfo 中的凭据、query/fragment 存储、localhost、private IP 与内部 hostname 特征。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` 本切片的 manual URL ingest 仅 metadata-only：不执行真实外部 HTTP fetch、crawl、scheduled sync、browser automation 或 connector provider call。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` 登记的 URL source 创建或暴露 review-required artifacts，可出现在 Processing Center 与 Wiki generation metadata 中，但不会成为 approved Wiki、Ask 或 Graph knowledge。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` Source trace 从 URL source 到 batch/file/chunk metadata 与 review-required generated artifact 保持保留。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` API response 与 frontend state 不暴露 credential、cookie、token、private endpoint、raw stack trace、raw provider payload 或 confidential source content。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` 既有 file/folder ingest、Wiki、Ask、Graph、review/publish、auth guard、safe error 与 rate limit 行为不得回归。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` 前端展示 manual URL ingest 入口、状态摘要、source trace 与 review-required output，并使用 API-backed data 与 mock-safe fallback pattern。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` 测试证明 URL validation、status mapping、source trace preservation、redaction 与 review-required behavior。

## 范围外

- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-011` Connector sync v0、recursive crawl、scheduled crawl、sitemap crawl、browser automation、headless rendering 与 full web crawling 均排除。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-012` Production legal/compliance review、robots policy engine、copyright policy、DLP scan、malware scan、connector secret management 与 production connector authentication 均排除。
- `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013` 不改变 auth/RBAC/audit/secret/rate-limit/provider/migration 语义；仅允许本切片描述的 additive metadata 与 API 行为。

## 验收标准

- `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` 可以为 Knowledge Space 登记一个安全的 public HTTPS sample URL。
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` Source record 包含 safe URL metadata、source trace、ingest status、review status、confidence 与 eligibility metadata。
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` 任何 derived batch/file/chunk 或 Wiki ingest metadata 在现有流程明确审核前保持 `REVIEW_REQUIRED`。
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` Unsafe URL 返回用户安全 validation error，且不泄漏原始不安全值。
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` Processing Center 或 Knowledge Space UI 能展示 manual URL ingest status 与 source trace。
- `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` 既定验证命令通过。

## 约束

- 不执行外部网络调用。
- 仅使用 mock/sample URL。
- 不存储 raw credential、cookie、query string、fragment、private endpoint、private path 或 confidential content。
- Adapter boundary 保持不变。
- 本切片中 URL-derived artifact 必须保持 review-required。

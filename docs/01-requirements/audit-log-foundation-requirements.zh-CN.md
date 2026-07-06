# 需求：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06
切片：`audit-log-foundation`
Wave：Wave 3 / Trust And Governance

## 目标

在 `auth-space-rbac` 之后，为 Atlas 增加安全、append-only 的审计日志基础，让安全审阅者和空间负责人可以检查谁尝试或执行了敏感操作，同时不暴露 secret、原始文档内容、私有路径、provider payload 或 stack trace。

## 范围

范围内：

- 面向核心 Atlas 操作的通用 audit event 数据模型。
- 对 auth decision、membership 变更、review/publish、Wiki generation/linkify/lint、graph/Ask、model configuration 与 adapter/runtime operation summary 记录 append-only audit event。
- 面向 space 与 target 的只读 audit API。
- 现有 Atlas 产品壳中的前端 audit view。
- 从现有窄范围 `graph_audit_record` 模式迁移或桥接到通用 audit event foundation。
- 仅使用 mock/sample-safe seed 与测试。

范围外：

- Production SIEM export、alert、retention automation、legal hold、tamper-evident signing、rate limiting、secret manager integration、live SSO/OIDC 与 external cloud calls。
- 原始 request/response body、raw prompts、source document text、parser/runtime logs、stack traces、provider payloads、tokens、credentials 或 private absolute paths。
- 替换 `review_record` 等 domain history tables；audit 提供治理证据，不替代领域事实来源。

## 需求

| ID | 需求 | 优先级 | 验证 |
|---|---|---|---|
| REQ-AUDIT-LOG-FOUNDATION-001 | 系统必须用 append-only 表持久化 audit event，包含 stable id、timestamp、actor、action、result、target、space、request correlation 与 safe summary 字段。 | Must | Migration 与 repository tests。 |
| REQ-AUDIT-LOG-FOUNDATION-002 | Audit event 不得保存 raw secrets、tokens、passwords、API keys、provider payloads、raw prompts、raw document text、private paths、stack traces 或 raw runtime logs。 | Must | Secret/private-path scans 与 redaction tests。 |
| REQ-AUDIT-LOG-FOUNDATION-003 | Auth boundary 必须为 protected API 的 denied requests 与高价值 allowed governance actions 产生 audit events。 | Must | `401`、`403` 与 allowed action event integration tests。 |
| REQ-AUDIT-LOG-FOUNDATION-004 | Membership create、update、suspend、remove 与 last-owner conflict attempts 必须产生包含 actor 与 target user references 的 audit events。 | Must | Membership API contract tests。 |
| REQ-AUDIT-LOG-FOUNDATION-005 | Review、publish、Wiki ingest、Wiki linkify/lint、graph projection/review、Ask create、vector/model/storage/parser/converter operation starts 或 completions 必须产生 bounded audit summaries。 | Must | 代表性 domain service tests。 |
| REQ-AUDIT-LOG-FOUNDATION-006 | 现有 graph audit 行为必须保留或迁移，不能丢失 graph governance evidence。 | Must | Graph tests 继续通过，且 graph audit mapping 已记录。 |
| REQ-AUDIT-LOG-FOUNDATION-007 | Audit read APIs 必须支持按 space、actor、action、result、target type/id 与 time window 过滤，并分页。 | Must | API contract tests。 |
| REQ-AUDIT-LOG-FOUNDATION-008 | Audit APIs 必须执行 RBAC：`AUDITOR`、`SPACE_OWNER`、`KNOWLEDGE_MANAGER` 与 `PLATFORM_ADMIN` 可读取被授权 audit scopes；`VIEWER` 与 `EDITOR` 默认不能读取 governance audit logs。 | Must | Role-specific integration tests。 |
| REQ-AUDIT-LOG-FOUNDATION-009 | Cross-space audit access 必须返回 safe denied 或 not-found responses，不能泄露 protected resource names、scope 外 user emails、source paths 或 content。 | Must | Cross-space API tests。 |
| REQ-AUDIT-LOG-FOUNDATION-010 | Frontend 必须使用 backend capabilities 与 safe audit DTOs 提供只读 audit panel。 | Should | Frontend unit 与 E2E coverage。 |
| REQ-AUDIT-LOG-FOUNDATION-011 | Audit event action/result enums 必须足够稳定，以支撑后续 retention、export 与 alerting slices。 | Should | Enum coverage tests 与 API guide。 |
| REQ-AUDIT-LOG-FOUNDATION-012 | 实现必须使用现有 Spring Boot、PostgreSQL/Flyway、API envelope 与 auth/RBAC patterns，不引入 external dependencies。 | Must | `mvn verify` 与 dependency scan。 |

## 假设

- 实现依赖 `auth-space-rbac`，因为 audit 需要 `CurrentUserContext`、role/capability decision 与 safe denied responses。
- Local/test identities 只使用 mock/sample users。
- Audit records 创建后不可变；未来 retention/export 行为属于独立切片。

## 待确认问题

| ID | 问题 | Owner |
|---|---|---|
| OQ-AUDIT-LOG-FOUNDATION-001 | `AUDITOR` 默认应看到 space 中全部 events，还是只看到 security/governance categories？ | Product / Security |
| OQ-AUDIT-LOG-FOUNDATION-002 | 现有 `graph_audit_record` 应在 migration 中 backfill 到通用表，还是作为 legacy companion 保留到 cleanup slice？ | Engineering |

# 用户故事：audit-log-foundation

状态：待人工审阅的草案
最后更新：2026-07-06

## Story Map

| Story | Requirement IDs | Capability |
|---|---|---|
| US-AUDIT-LOG-FOUNDATION-001 | REQ-AUDIT-LOG-FOUNDATION-001, 002, 011, 012 | Append-only safe audit event foundation |
| US-AUDIT-LOG-FOUNDATION-002 | REQ-AUDIT-LOG-FOUNDATION-003, 004, 005, 006 | Audit event capture for core operations |
| US-AUDIT-LOG-FOUNDATION-003 | REQ-AUDIT-LOG-FOUNDATION-007, 008, 009 | Secure audit read APIs |
| US-AUDIT-LOG-FOUNDATION-004 | REQ-AUDIT-LOG-FOUNDATION-010 | Frontend audit inspection |
| US-AUDIT-LOG-FOUNDATION-005 | REQ-AUDIT-LOG-FOUNDATION-002, 009, 012 | Verification and data-safety guardrails |

## User Story 1

**Title:** Persist safe audit events

**Story:**
作为 security reviewer，
我想让敏感 Atlas 操作创建不可变 audit events，
以便治理审阅能检查谁做了什么，同时不暴露受保护内容。

### Acceptance Criteria

1. **Given** 敏感 Atlas action 完成或被拒绝
   **When** backend 记录 audit event
   **Then** record 包含 actor id、action、result、target、space、timestamp、request id 与 safe summary。

2. **Given** audit event 已记录
   **When** 它被持久化
   **Then** product APIs 不能更新它。

3. **Given** operation 包含敏感输入
   **When** audit fields 被构建
   **Then** raw secrets、raw source text、raw prompts、private paths、provider payloads 与 stack traces 被排除。

### Notes / Assumptions

- Audit records 补充 `review_record` 等 existing domain records，不替代它们。

### Dependencies

- `auth-space-rbac` current-user 与 authorization decision foundation。

### Out of Scope

- SIEM export、alerting、retention automation 与 tamper-evident signing。

### Open Questions

- Graph audit history 应 backfill 还是 lazy bridge？

## User Story 2

**Title:** Capture core governance actions

**Story:**
作为 space owner，
我想让 membership、review、publish、Wiki、graph、Ask 与 settings actions 可审计，
以便调查 knowledge space 的变化。

### Acceptance Criteria

1. **Given** membership change 被创建、更新、移除、拒绝或被 last-owner protection 拒绝
   **When** operation 结束
   **Then** audit event 捕获 actor、target user、space、action、result 与 safe reason。

2. **Given** review、publish、Wiki ingest/linkify/lint、graph projection/review、Ask 或 adapter operation 执行
   **When** representative operation 完成
   **Then** audit event 捕获 safe counts、ids 与 result status，不包含 raw content。

3. **Given** protected request 被拒绝
   **When** auth boundary 返回 `401`、`403` 或 safe `404`
   **Then** 在 target scope 可安全解析时可以审计该 denial。

### Notes / Assumptions

- v0 优先覆盖代表性核心操作，并保留后续切片扩展点。

### Dependencies

- Existing backend services 与 RBAC path policy。

### Out of Scope

- 默认审计所有 read requests。

### Open Questions

- 哪些 read events 若有，应成为 internal beta 必审事件？

## User Story 3

**Title:** Read audit events safely

**Story:**
作为 auditor，
我想按 space、actor、action、target、result 与 time range 过滤 audit events，
以便在权限范围内检查 governance history。

### Acceptance Criteria

1. **Given** 授权 auditor 请求 space audit events
   **When** 使用支持的 filters 和 pagination
   **Then** API 返回 safe DTOs 与 page metadata。

2. **Given** 用户缺少目标 space 的 governance-read permission
   **When** 请求 audit data
   **Then** backend 返回 safe denied 或 not-found response，不泄露 event details。

3. **Given** audit target id 属于另一个 space
   **When** 用户按该 target 过滤
   **Then** response 不披露 cross-space metadata。

### Dependencies

- Backend RBAC 与 safe API envelope behavior。

### Out of Scope

- 超出 mock/internal `PLATFORM_ADMIN` 能力的 global cross-tenant audit console。

### Open Questions

- `KNOWLEDGE_MANAGER` 应读取所有 audit categories，还是仅知识操作 categories？

## User Story 4

**Title:** Inspect audit events in the product UI

**Story:**
作为 space owner，
我想在 Atlas 中使用只读 audit panel，
以便从空间 UI 快速查看最近的治理操作。

### Acceptance Criteria

1. **Given** 当前用户有 audit-read capability
   **When** 打开 audit panel
   **Then** 可见 recent safe audit events、filters 与 empty/error states。

2. **Given** 当前用户没有 audit-read capability
   **When** UI 渲染 governance surfaces
   **Then** audit entry points 被隐藏或禁用，且不暗示 backend access 成功。

3. **Given** backend 返回 denied response
   **When** UI 处理该响应
   **Then** 不保留 stale audit data。

### Out of Scope

- Audit event editing、deletion、export 与 alert creation。

### Open Questions

- 第一版 UI surface 应放在 Settings、Processing Center，还是 space Governance tab？

## User Story 5

**Title:** Verify audit safety

**Story:**
作为 security reviewer，
我想用自动化测试和扫描验证 audit data safety，
以便 audit logging 不成为新的泄露通道。

### Acceptance Criteria

1. **Given** audit events 来自敏感操作
   **When** tests 检查 stored events
   **Then** 只出现 safe ids、enums、counts、timestamps 与 safe summaries。

2. **Given** changed files 被扫描
   **When** secret/private-path 与 network/dependency checks 运行
   **Then** 不发现 new real data、raw credentials、external cloud calls 或 unapproved dependencies。

3. **Given** SDD 与 implementation 出现偏差
   **When** closeout 运行
   **Then** traceability 记录 mismatch 或阻塞完成。

### Dependencies

- Existing workflow gates 与 test harnesses。

### Out of Scope

- Formal compliance certification。

### Open Questions

- None。


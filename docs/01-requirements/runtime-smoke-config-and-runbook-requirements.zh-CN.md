# 需求：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 切片契约

- **Slice:** `runtime-smoke-config-and-runbook`
- **Wave:** Wave 3 / 面向 runtime 运维的 Trust And Governance readiness
- **目标:** 让真实 runtime smoke 检查具备安全、可重复、可解释的配置与运行手册，同时不改变默认 mock-safe CI 行为。
- **成熟度目标:** 面向 approved local runtime verification 的 L4 readiness preparation；不是 production readiness。

## 范围

### 范围内

- 记录 `trinity-office` 与 `document-normalize` 的 approved local runtime smoke 配置契约。
- 定义 optional smoke checks 如何 self-skip、pass、fail，并保留受限诊断。
- 定义后续实现 runbook 应覆盖 setup、execution、evidence capture、troubleshooting、rollback 与安全边界。
- 保留 `real-office-parser-runtime` 已建立的 adapter boundaries。
- 当缺少 approved local runtime binaries 时，默认 CI 与普通 `mvn verify` 继续保持 mock-safe。
- 所有测试夹具保持 mock/sample-safe。

### 范围外

- 生产部署 runbook、SLO、告警、监控看板或部署环境回滚。
- Secret manager integration、生产 RBAC、audit-log foundation、rate limiting 或 safe-error envelope 改动。
- 新公开 API endpoint。
- 真实公司文档、私有路径、原始 binary logs、凭据、内部 hostname、截图或 provider logs。
- 改变 parser/converter runtime 行为、manifest shape 或 adapter registry 行为；除非仅为安全 smoke evidence 所必需。

## 需求

| ID | 需求 | 优先级 | 验证 |
|---|---|---|---|
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | 完整双语 SDD set 必须先被用户接受，才能开始实现或 runbook 改动。 | Must | Traceability 记录 Draft 状态与用户接受门。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | 本切片必须记录当前 smoke 环境变量：`ATLAS_RUNTIME_SMOKE_ENABLED`、`ATLAS_TRINITY_OFFICE_COMMAND`、`ATLAS_TRINITY_OFFICE_SMOKE_ARGS`、`ATLAS_DOCUMENT_NORMALIZE_COMMAND`、`ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS`。 | Must | Spec 与 API guide 列出精确变量与默认值。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | 本切片必须区分 Spring runtime adapter properties 与 optional smoke-test environment variables。 | Must | Design 解释 adapter runtime configuration 与 smoke-only command checks 的差异。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | Optional runtime smoke tests 只有在显式启用且配置 approved command 时才能执行，否则必须 self-skip。 | Must | Tasks 要求提供 skip behavior 的聚焦测试证据。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | 成功 smoke execution 必须产出安全 pass/fail evidence，且不得暴露 raw command paths。 | Must | Tasks 要求 diagnostics assertions 与 safety scans。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | Smoke diagnostics 必须保持 bounded 与 sanitized。 | Must | Design 引用现有 bounded capture 与 sanitizer 行为；tasks 要求测试或静态检查。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | Runbook 只能使用 mock/sample-safe verification inputs，并必须禁止真实公司文档。 | Must | Runbook task 包含明确 fixture/data safety checklist。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | Adapter boundaries 必须保持不变；product services、controllers、Wiki、Graph、Ask 与 frontend code 不得直接调用真实 runtimes。 | Must | Tasks 包含 seam guard verification。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | 默认 CI 不得依赖真实 `trinity-office` 或 `document-normalize` binaries。 | Must | 当环境变量缺失时，`mvn verify` 必须在 smoke tests skipped 状态下通过。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010 | Runbook 必须解释 skip、pass、fail、timeout、missing command 与 unsafe diagnostic outcomes。 | Must | Design 与 tasks 包含 outcome interpretation matrix。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011 | 实现必须包含 closeout evidence checklist，覆盖 backend verification、smoke skip/pass evidence、diff hygiene、secret/private-path scan 与 network/dependency scan。 | Must | Tasks 定义必要验证命令与扫描。 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012 | Roadmap 与 traceability 必须说明本切片只是 runtime smoke readiness，不是 production operations readiness。 | Must | Traceability 与 roadmap update 使用成熟度限定语言。 |

## 假设

- Approved local binaries 如可用，已安装在仓库之外。
- 现有 smoke test 当前执行 command-level checks，并默认将 smoke args 设为 `--version`。
- 完整 upload-to-Wiki 真实文档处理仍不属于本切片。

## 待确认问题

| ID | 问题 | 负责人 |
|---|---|---|
| OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | 被接受后的 runbook 应放在 `docs/00-context/runbooks/` 还是 `docs/07-acceptance/`？ | Product / Platform |
| OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | 后续 smoke check 应验证一个很小的 approved sample conversion/parse fixture，还是继续只做 command health checks？ | Platform / Security |

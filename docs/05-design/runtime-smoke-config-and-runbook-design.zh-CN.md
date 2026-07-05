# 详细设计：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 来源架构

本设计派生自 `docs/03-spec/runtime-smoke-config-and-runbook-spec.md` 与 `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.md`。

## 已核实现有代码上下文

| Existing element | Verified anchor | Design relevance |
|---|---|---|
| Optional smoke enable flag | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:18` | 当前 smoke tests 使用 `ATLAS_RUNTIME_SMOKE_ENABLED`。 |
| Trinity smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:24` | 当前 smoke test 读取 `ATLAS_TRINITY_OFFICE_COMMAND` 与 smoke args。 |
| Document Normalize smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:32` | 当前 smoke test 读取 `ATLAS_DOCUMENT_NORMALIZE_COMMAND` 与 smoke args。 |
| Smoke default args | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:58` | Args 缺失时默认为 `--version`。 |
| Smoke raw command assertion | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:52` | 现有 test sanitize diagnostics，并断言 command 不出现。 |
| Runtime config defaults | `backend/src/main/java/com/atlas/metadata/adapter/runtime/RuntimeAdapterConfiguration.java:9` | Adapter runtime 默认 timeout/capture limit 是 120 seconds 与 65536 bytes。 |
| Smoke capture limit | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:19` | Smoke command capture limit 是 4096 bytes。 |
| Spring Trinity runtime properties | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:45` | Adapter runtime properties 不同于 smoke env vars。 |
| Spring Document Normalize runtime properties | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:46` | Adapter runtime properties 不同于 smoke env vars。 |
| Seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Direct runtime/process references 仍受 guard。 |

## 设计假设

- 被接受后的实现默认在 `docs/00-context/runbooks/runtime-smoke-config-and-runbook.md` 和 `.zh-CN.md` 创建 runbook，除非用户在实现前选择其他路径。
- 第一版 runbook 只记录 command-level smoke checks。
- Local command env var values 可能是 private paths，必须作为敏感本地配置处理。
- 默认 CI 不设置 `ATLAS_RUNTIME_SMOKE_ENABLED=true`。

## 设计范围

### 范围内

- 双语 runbook 文档。
- Focused optional smoke test 文档，以及如需要的小型断言更新，且保持现有行为。
- Traceability/roadmap evidence language。
- Verification commands 与 safety scans。

### 范围外

- 新 endpoints、migrations、frontend UI、production runbook、deployment monitoring、secret manager、RBAC、audit、rate limit 或 sample document processing smoke。

## 模块设计

### Runbook Module

Runbook 必须包含以下章节：

1. Purpose and maturity statement。
2. Prerequisites and approvals。
3. Smoke env var matrix。
4. Spring adapter runtime property matrix。
5. Default skip verification。
6. Approved local command smoke verification。
7. Outcome matrix。
8. Troubleshooting。
9. Disable and rollback。
10. Evidence checklist。
11. Safety bans。

### Smoke Test Evidence Module

实现必须保留当前 evidence behavior：

- `ATLAS_RUNTIME_SMOKE_ENABLED` 控制 smoke 是否运行。
- 缺少 command env vars 时 self-skip。
- 缺少 args 时默认 `--version`。
- Diagnostics 进入 evidence 前被 sanitized。
- Raw command values 不进入 assertions 或 documentation evidence。

### Configuration Distinction Module

Runbook 必须包含并列表：

| Purpose | Smoke env vars | Spring adapter runtime properties |
|---|---|---|
| Command health check | Smoke tests 读取的 `ATLAS_*` env vars | 不需要 |
| Configured adapter execution | 不足以启用 | `atlas.runtime.*` Spring properties |
| Default CI | 未设置；smoke skipped | 默认 disabled |

### Closeout Evidence Module

Traceability 必须记录：

- SDD accepted 或 Draft 状态。
- Default skip evidence 是否运行。
- Approved pass evidence 是已运行还是因缺少 approved binaries 跳过。
- 实际运行的 verification commands。
- Sanitized safety scan results。
- Residual risks 与 next recommended slice。

## API / Interface Design

不引入新公开 API。

面向实现的契约是文档与测试配置：

- 除非更新本 SDD，smoke test env var names 保持稳定。
- Spring adapter runtime property names 被记录为与 smoke env vars 不同。
- 所有 command values 都保持本地，不 raw 出现在 API responses、docs、traceability 或 final response。

## 数据设计

不引入 database schema、DTO 或 persisted entity。

Markdown documentation 是本切片的 durable artifact：

- SDD docs 位于 `docs/01-*` 到 `docs/06-*`。
- Runbook 位于已接受的 runbook path。
- Traceability 位于 `docs/00-context/runtime-smoke-config-and-runbook-traceability.md`。
- Roadmap status 位于 `ROADMAP.md`、`ROADMAP.zh-CN.md` 与 slice roadmap companions。

## 工作流 / 执行设计

### Default Skip Verification

1. 确保 command environment 中没有 approved runtime smoke env vars。
2. 运行 `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`。
3. 确认两个 smoke tests 都 self-skip。
4. 只记录 safe skip evidence。

### Approved Local Smoke Verification

1. 确认 local binaries 已被批准用于测试，并安装在仓库外。
2. 在仓库外设置 smoke env vars。
3. 运行 `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`。
4. 确认每个 configured runtime exit zero 且无 timeout。
5. 记录 pass/fail summary，不包含 raw command values。

### Full Closeout Verification

1. 运行 `cd backend && mvn verify`。
2. 运行 seam guard focused test。
3. 运行 `git diff --check`。
4. 对 changed files 运行 focused secret/private-path scan。
5. 对 changed files 运行 focused network/dependency scan。
6. 用成熟度限定语言更新 traceability 与 roadmap。

## 校验与错误处理

| Case | Expected behavior |
|---|---|
| Smoke disabled | Tests skip；不是 failure。 |
| Smoke enabled but command missing | 相关 test 以 safe missing-command reason skip。 |
| Smoke enabled and command exits zero | Test pass；报告 generic runtime family pass。 |
| Smoke enabled and command exits non-zero | Test fail；只报告 sanitized diagnostic。 |
| Smoke command times out | Test fail；报告 timeout summary，不含 raw command。 |
| Safety scan finds raw command/private path | Closeout 前修复 evidence/docs。 |

## Edge Case Trace

| Rule | Case | Result |
|---|---|---|
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED` unset | skip |
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED=TRUE` | enabled |
| Enable only on exact true | `ATLAS_RUNTIME_SMOKE_ENABLED=1` | skip |
| Args defaulting | args env var missing | `--version` |
| Args defaulting | args env var blank | `--version` |
| Args defaulting | args env var has two flags | two whitespace-split args |

## 测试考虑

- `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test`
- `cd backend && mvn -Dtest=AdapterSeamGuardTest test`
- `cd backend && mvn verify`
- `git diff --check`
- focused secret/private-path scan
- focused network/dependency scan

除非 implementation 触及 frontend files，否则不要求 frontend checks。

## 风险 / 设计权衡

| Risk | Decision |
|---|---|
| Runbook path 可能需要用户偏好 | 除非用户另选，否则默认 `docs/00-context/runbooks/`。 |
| Command-level smoke 有限 | 明确说明限制；不暗示 sample document processing。 |
| Local pass evidence 可能不可用 | Closeout 必须报告 approved pass evidence 是已运行还是 skipped with reason。 |

## 待确认问题

- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001：最终 runbook path。
- OQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002：未来 sample processing smoke。

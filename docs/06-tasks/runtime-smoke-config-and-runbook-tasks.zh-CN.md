# 任务：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 概述

本任务清单为 runtime smoke 配置与 runbook evidence 提供 implementation-ready 路径。它刻意聚焦文档/测试，不新增公开 API、持久化、前端 UI 或生产运维能力。

## 来源设计

- **System name:** Runtime Smoke Config And Runbook
- **Design scope summary:** 双语 runbook、smoke env var contract、safe evidence checklist、optional focused smoke verification、seam guard preservation，以及成熟度限定的 roadmap/traceability updates。

## 工作流

- **Acceptance and SDD closeout:** implementation 前记录用户接受。
- **Runbook documentation:** 创建含 outcome matrix 与 safety bans 的双语 runbook。
- **Verification:** 保留 smoke self-skip/pass behavior，运行 backend checks 与 safety scans。
- **Context status:** implementation 后更新 traceability 与 roadmaps。

## 按领域任务拆解

### Documentation / Runbook

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001：记录 SDD acceptance。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002：创建双语 runbook。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003：加入 env var 与 Spring property 区分。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004：加入 outcome 与 troubleshooting matrix。

### Backend Verification

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005：验证默认 smoke self-skip。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006：在可用时验证 optional approved local smoke path。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007：保留 seam guard 与 adapter boundaries。

### Safety / Closeout

- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008：运行完整 verification 与 safety scans。
- T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009：更新 traceability 与 roadmap maturity status。

## 任务详情

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001：记录 SDD acceptance

- **目标:** 防止用户接受前实现。
- **范围:** 仅在用户明确接受本 SDD 后，把 traceability 从 Draft 更新为 Accepted。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012
- **依赖:** 无
- **Owner type:** product / implementation
- **优先级:** Must
- **验证:** Traceability 在 implementation changes 前包含 acceptance record。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002：创建双语 runtime smoke runbook

- **目标:** 提供 local runtime smoke checks 的持久 operator guide。
- **范围:** 在已接受的 runbook path 下创建英文与简体中文 runbook 文件。包含 purpose、maturity statement、prerequisites、commands、outcome matrix、troubleshooting、disable/rollback、evidence checklist 与 safety bans。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** platform / docs
- **优先级:** Must
- **验证:** Runbook files 存在；扫描未发现真实公司数据、raw commands、private paths、secrets、screenshots 或 provider logs。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003：记录配置差异

- **目标:** 防止混淆 smoke-test env vars 与 Spring adapter runtime properties。
- **范围:** 在 runbook 中加入 side-by-side env/property tables 与 safe-value rules。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
- **Owner type:** platform
- **优先级:** Must
- **验证:** Runbook 精确列出 API guide 指定的所有 smoke env vars 与 Spring properties。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004：加入 outcome 与 troubleshooting matrix

- **目标:** 让 smoke 结果容易解释，同时不泄露本地细节。
- **范围:** 记录 skip、missing command、pass、non-zero exit、timeout、unsafe diagnostic、blocked approval、troubleshooting 与 rollback/disable actions。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002
- **Owner type:** platform / security
- **优先级:** Must
- **验证:** Outcome matrix 覆盖 spec 中每个 status，且不包含 raw command/path examples。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005：验证默认 smoke self-skip

- **目标:** 证明普通 verification 保持 mock-safe。
- **范围:** 在没有 approved smoke env vars 的情况下运行 focused smoke tests，并记录 skip evidence。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** QA / backend
- **优先级:** Must
- **验证:** `cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test` 通过，并显示 optional smoke tests skipped。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006：在可用时验证 approved local smoke path

- **目标:** 为 approved local runtime binaries 捕获安全 pass/fail evidence。
- **范围:** 如果 approved local binaries 可用，在仓库外设置 env vars 并运行 focused smoke。若不可用，则在 traceability 记录 skipped with reason。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005
- **Owner type:** platform / QA
- **优先级:** Should
- **验证:** Focused smoke pass/fail evidence 是安全的，或 traceability 说明 approved binaries unavailable。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007：保留 seam guard 与 adapter boundaries

- **目标:** 确保 runtime smoke readiness 不把 direct runtime calls 泄露到 product layers。
- **范围:** 运行 seam guard 并避免 product-layer runtime references。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001
- **Owner type:** backend / architecture
- **优先级:** Must
- **验证:** `cd backend && mvn -Dtest=AdapterSeamGuardTest test` 通过。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008：运行完整 verification 与 safety scans

- **目标:** 用具体 evidence 收尾切片。
- **范围:** 运行 backend verification、diff hygiene、changed files 的 focused secret/private-path scan 与 focused network/dependency scan。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 到 T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007
- **Owner type:** QA / security
- **优先级:** Must
- **验证:** 报告 `cd backend && mvn verify`、`git diff --check`、focused secret/private-path scan 与 focused network/dependency scan。

### T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009：更新 traceability 与 roadmap

- **目标:** 保持 durable context 当前有效且不夸大 readiness。
- **范围:** Implementation 后更新 traceability、slice roadmap 与 product roadmap，记录 changed docs/code、verification、skipped checks、residual risks 与 next slice。
- **Requirements:** REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012
- **依赖:** T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008
- **Owner type:** docs / product
- **优先级:** Must
- **验证:** Roadmap 与 traceability 只表述 runtime smoke readiness，不表述 production operations readiness。

## 依赖计划

- **Critical path:** T-001 -> T-002 -> T-003/T-004 -> T-005/T-007 -> T-008 -> T-009
- **Parallel workstreams:** T-003 与 T-004 可在 T-002 后并行。T-005 与 T-007 可在 acceptance 后运行。
- **Optional branch:** T-006 仅在 approved local binaries 可用时运行。

## 风险 / 阻塞

- Approved local binaries 可能不可用；pass evidence 可跳过，但必须报告。
- Runbook path 可能在实现前由用户调整。
- 后续 sample document smoke 需要单独 accepted scope。

## 实现结果

T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 到 T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 已全部完成，范围为 command-level runtime smoke readiness。

| Task | Result |
|---|---|
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | 用户 acceptance 已在 runbook implementation 前记录到 traceability。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | 已在 `docs/00-context/runbooks/` 下创建双语 runbooks。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | Runbooks 已包含 smoke env var 与 Spring adapter property 区分表。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | Runbooks 已包含 outcome、troubleshooting、disable/rollback 与 safety-ban matrices。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | 默认 self-skip verification 通过：2 个 focused smoke tests 按设计 skipped。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | Approved local pass evidence 已跳过，因为本轮未提供 approved local runtime command values。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | Seam guard verification 通过：3 tests passed。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | Backend verification、diff hygiene 与 focused safety scans 通过。 |
| T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | Traceability 与 roadmap status 已用 runtime smoke readiness 语言更新。 |

## 验证证据

| Check | Result |
|---|---|
| `cd backend && env -u ... mvn -Dtest=ConfiguredRuntimeSmokeIT test` | 通过：2 tests run，0 failures，0 errors，2 skipped。 |
| `cd backend && mvn -Dtest=AdapterSeamGuardTest test` | 通过：3 tests run，0 failures，0 errors。 |
| `cd backend && mvn verify` | 通过：120 unit tests 与 48 integration tests；2 个 optional runtime smoke tests 按设计 skipped。 |
| `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook` | 通过，有非阻塞 companion-doc/report warnings。 |
| `git diff --check` | 通过。 |
| Focused file existence check | 20 个双语 SDD 文件与 2 个双语 runbook 文件通过。 |
| Focused REQ/US/T bilingual ID parity check | 通过。 |
| Focused deferred-decision marker scan | 通过。 |
| Focused secret/private-path scan | 通过。 |
| Focused network/dependency scan | 通过。 |

## 实现后必跑验证命令

```bash
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
cd backend && mvn -Dtest=AdapterSeamGuardTest test
cd backend && mvn verify
git diff --check
```

还要对 changed files 运行 focused secret/private-path 与 network/dependency scans。除非实现触及 frontend files，否则不要求 frontend checks。

## SDD 接受后的推荐 Codex 交接命令

```text
严格依据 docs/03-spec/runtime-smoke-config-and-runbook-spec.md 与 docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md 实现 runtime-smoke-config-and-runbook：创建双语 runbook，保留 optional smoke self-skip behavior，只记录 safe evidence，保持默认 CI mock-safe，不新增 public APIs 或 product runtime behavior；若实现会偏离已接受 spec，停下报告。
```

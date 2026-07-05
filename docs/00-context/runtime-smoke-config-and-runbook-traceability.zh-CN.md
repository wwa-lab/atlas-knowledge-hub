# 溯源：Runtime Smoke Config And Runbook

## 状态

已于 2026-07-05 接受并实现。仅代表 runtime smoke readiness，不代表 production operations readiness。

## 切片契约

- **Slice:** `runtime-smoke-config-and-runbook`
- **Wave:** Wave 3 / 面向 runtime operations 的 Trust And Governance readiness
- **目标:** 让 approved local real-runtime smoke checks 安全、可重复、可解释，同时不改变默认 mock-safe CI 行为。
- **成熟度目标:** Runtime smoke readiness preparation；不是 production operations readiness。

## 来源文档

| Source | 状态 |
|---|---|
| Goal objective attachment | 已读。 |
| `README.md` | 已读。 |
| `PROJECT_RULES.md` | 已读。 |
| `DEVELOPMENT_STANDARDS.md` | 已读。 |
| `AGENTS.md` | 已读。 |
| `docs/00-context/sdd-profile.md` | 已读。 |
| `docs/01-requirements/requirement.md` | 已读。 |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | 已读。 |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | 已读。 |
| `ROADMAP.md` / `.zh-CN.md` | 已读并更新。 |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | 已读并更新。 |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | 已读。 |
| `real-office-parser-runtime` SDD docs and traceability | 已读。 |
| Existing runtime smoke and adapter code anchors | 已核实。 |
| User acceptance | 2026-07-05 在聊天中接受，且早于 runbook implementation。 |

## SDD 技能链证据

| Skill | 已读文件 |
|---|---|
| `atlas-sdd-generate-all` | `.agents/skills/atlas-sdd-generate-all/SKILL.md` |
| `req-to-user-story` | `.agents/skills/req-to-user-story/SKILL.md` |
| `user-story-to-spec` | `.agents/skills/user-story-to-spec/SKILL.md` |
| `spec-to-architecture` | `.agents/skills/spec-to-architecture/SKILL.md` |
| `architecture-to-design` | `.agents/skills/architecture-to-design/SKILL.md` |
| `design-to-tasks` | `.agents/skills/design-to-tasks/SKILL.md` |
| `review-doc-quality` | `.agents/skills/review-doc-quality/SKILL.md` |
| `architecture-review` | `.agents/skills/architecture-review/SKILL.md` |
| Shared grounding rules | `.agents/skills/_shared/grounding-rules.md` |
| Review completeness reference | `.agents/skills/review-doc-quality/references/completeness-criteria.md` |
| Review phase-scope reference | `.agents/skills/review-doc-quality/references/phase-scope-guide.md` |

SDD skill chain used: yes.

## 已核实现有代码锚点

| Area | Verified anchor | Finding |
|---|---|---|
| Smoke enable env var | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:18` | Optional smoke checks 使用 `ATLAS_RUNTIME_SMOKE_ENABLED`。 |
| Trinity smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:24` | Trinity smoke 读取 command 与 smoke args env vars。 |
| Document Normalize smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:32` | Document Normalize smoke 读取 command 与 smoke args env vars。 |
| Smoke args default | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:58` | Args 缺失时默认 `--version`。 |
| Smoke diagnostic safety | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:52` | Smoke diagnostics 在 assertion 前 sanitized。 |
| Runtime adapter defaults | `backend/src/main/java/com/atlas/metadata/adapter/runtime/RuntimeAdapterConfiguration.java:9` | Adapter runtime timeout/capture defaults 与 smoke-only env vars 分开记录。 |
| Spring Trinity properties | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:45` | Configured adapter runtime 使用 Spring properties。 |
| Spring Document Normalize properties | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:46` | Configured adapter runtime 使用 Spring properties。 |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | 已存在 adapter/runtime boundary verification guard。 |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/runtime-smoke-config-and-runbook-requirements.md` | `docs/01-requirements/runtime-smoke-config-and-runbook-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/runtime-smoke-config-and-runbook-stories.md` | `docs/02-user-stories/runtime-smoke-config-and-runbook-stories.zh-CN.md` |
| Spec | `docs/03-spec/runtime-smoke-config-and-runbook-spec.md` | `docs/03-spec/runtime-smoke-config-and-runbook-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/runtime-smoke-config-and-runbook-data-flow.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/runtime-smoke-config-and-runbook-data-model.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-data-model.zh-CN.md` |
| Design | `docs/05-design/runtime-smoke-config-and-runbook-design.md` | `docs/05-design/runtime-smoke-config-and-runbook-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/runtime-smoke-config-and-runbook-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/runtime-smoke-config-and-runbook-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md` | `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.zh-CN.md` |
| Traceability | `docs/00-context/runtime-smoke-config-and-runbook-traceability.md` | `docs/00-context/runtime-smoke-config-and-runbook-traceability.zh-CN.md` |

## SDD 质量评审结果

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Draft 前已读取 context 与 code anchors。 |
| Skill chain evidence | Passed | 已读取必需 project-local SDD skills 与 references。 |
| Expected artifacts exist | Passed | 聚焦 file-existence check 找到全部 20 个双语 SDD 文件。 |
| Bilingual ID parity | Passed | 聚焦 REQ/US/T parity check 无差异。 |
| Deferred-decision scan | Passed | 聚焦扫描未发现 deferred-decision 或 placeholder markers。 |
| Architecture/API review | Draft pass | 未引入 public API 或 persistence；adapter boundaries 明确。 |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook` 通过；warnings 为 architecture/design companion docs 未额外嵌入 IDs，且未提供 external report。 |
| Diff hygiene | Passed | `git diff --check` 通过。 |
| Secret/private-path scan | Passed | 聚焦扫描未在新切片文档中发现 raw credentials、private local paths 或真实数据。 |
| Network/dependency scan | Passed | 聚焦扫描未在新切片文档中发现 external URLs、install commands 或 network-client references。 |
| Product-code gate | Passed for SDD pass | 本 SDD-only pass 未改产品代码。 |

## SDD Draft 已执行验证

- `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook`
- 聚焦检查 20 个双语 artifacts 是否存在
- 聚焦 REQ/US/T ID parity check
- 聚焦 deferred-decision placeholder scan
- `git diff --check`
- 对 changed docs 运行 focused secret/private-path scan
- 对 changed docs 运行 focused network/dependency scan

## 残留风险

- 本切片只记录 command-level smoke readiness；sample document conversion/parser smoke 仍属于 future accepted scope。
- Implementation 时 approved local binaries 可能不可用，因此 pass evidence 可能以 skipped with reason 记录。

## 实现证据

### 文档改动

- `docs/00-context/runbooks/runtime-smoke-config-and-runbook.md`
- `docs/00-context/runbooks/runtime-smoke-config-and-runbook.zh-CN.md`
- `docs/00-context/runtime-smoke-config-and-runbook-traceability.md`
- `docs/00-context/runtime-smoke-config-and-runbook-traceability.zh-CN.md`
- `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md`
- `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.zh-CN.md`
- `ROADMAP.md`
- `ROADMAP.zh-CN.md`
- `docs/00-context/slice-roadmap.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- 双语 SDD artifact set 的状态行已从 Draft 更新为 accepted/implemented。

### 代码改动

无。本 implementation pass 未修改 backend、frontend、migration 或 test source files。

### 验证

| Check | Result | Evidence |
|---|---|---|
| Focused default smoke self-skip | Passed | `env -u ... mvn -Dtest=ConfiguredRuntimeSmokeIT test`：2 tests run，2 skipped。 |
| Adapter seam guard | Passed | `mvn -Dtest=AdapterSeamGuardTest test`：3 tests passed。 |
| Full backend verification | Passed | `mvn verify`：120 unit tests passed；48 integration tests passed；2 个 optional runtime smoke tests 按设计 skipped。 |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook`；warnings 是既有 companion-doc ID 与缺少 external report 提示。 |
| Diff hygiene | Passed | `git diff --check` 通过。 |
| File existence | Passed | 20 个双语 SDD 文件与 2 个双语 runbook 文件存在。 |
| Bilingual ID parity | Passed | 聚焦 REQ/US/T parity check 无差异。 |
| Deferred-decision marker scan | Passed | 聚焦扫描未发现 deferred-decision 或 placeholder markers。 |
| Secret/private-path scan | Passed | 聚焦扫描未在 changed docs 中发现 raw credentials、private local paths 或真实数据。 |
| Network/dependency scan | Passed | 聚焦扫描未在 changed docs 中发现 external URLs、install commands 或 network-client references。 |
| Approved local runtime smoke pass | Skipped with reason | 本轮未提供 approved local runtime command values。 |

## 最终残留风险

- 本 runbook 只验证 command-level runtime health；不处理 sample document。
- Approved local runtime pass evidence 仍需 operator 在仓库外提供 approved local command values 后才能获得。
- Production monitoring、deployment rollback、audit、RBAC 与 secret-manager flows 不属于本切片范围。

## 下一门禁

下一推荐切片应继续按已接受队列推进，且不得把本 runbook 视为 production operations readiness。

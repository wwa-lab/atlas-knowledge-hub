# Runbook：Runtime Smoke Config And Runbook

## 状态

已接受并实现 command-level runtime smoke readiness。本 runbook 不代表 production operations readiness。

## 目的

本 runbook 说明如何验证 optional local `trinity-office` 与 `document-normalize` runtime command health checks，同时不改变 Atlas 默认 mock-safe CI 路径。本范围只覆盖 approved local smoke checks。

## 成熟度说明

- **包含：** optional command-level smoke checks、safe evidence capture、skip/pass/fail interpretation，以及 disable/rollback instructions。
- **不包含：** production deployment monitoring、SLO、alerting、secret manager integration、真实公司文档处理，或 sample document conversion/parser smoke。

## 前置条件与批准

运行 approved local smoke check 前：

1. 确认 runtime binary 已获准用于 local smoke verification。
2. 在本仓库之外安装或暴露 command。
3. 只在运行 smoke 的本地 shell/session 中设置 command environment variables。
4. 不提交 command paths、local env files、shell history、raw diagnostics、screenshots 或 machine-specific logs。
5. 只使用 mock/sample-safe commands and arguments。不得使用真实公司文档。

## 配置矩阵

### Optional Smoke Test Environment Variables

| Variable | Purpose | Default / Behavior | Evidence rule |
|---|---|---|---|
| `ATLAS_RUNTIME_SMOKE_ENABLED` | 启用 optional smoke tests | 除大小写不敏感的 `true` 外均 self-skip | 可报告变量名。 |
| `ATLAS_TRINITY_OFFICE_COMMAND` | Trinity smoke test 的 approved command | 缺失时 Trinity smoke self-skip | 不得报告 raw value。 |
| `ATLAS_TRINITY_OFFICE_SMOKE_ARGS` | Trinity smoke test 的 command args | 默认 `--version` | Evidence 中避免机器特定值。 |
| `ATLAS_DOCUMENT_NORMALIZE_COMMAND` | Document Normalize smoke test 的 approved command | 缺失时 Document Normalize smoke self-skip | 不得报告 raw value。 |
| `ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS` | Document Normalize smoke test 的 command args | 默认 `--version` | Evidence 中避免机器特定值。 |

### Spring Adapter Runtime Properties

这些 properties 用于 configured adapter execution，不用于 optional command-health smoke test：

| Property | Default | Scope |
|---|---|---|
| `atlas.runtime.trinity-office.enabled` | `false` | Backend configured converter adapter runtime。 |
| `atlas.runtime.trinity-office.command` | blank | Backend configured converter adapter runtime。 |
| `atlas.runtime.document-normalize.enabled` | `false` | Backend configured parser adapter runtime。 |
| `atlas.runtime.document-normalize.command` | blank | Backend configured parser adapter runtime。 |

### 区分表

| Purpose | Smoke env vars | Spring adapter runtime properties |
|---|---|---|
| Command-health smoke | 只对 focused smoke tests 必需 | 不需要 |
| Configured adapter execution | 仅靠它们不足以启用 | 运行 configured adapter mode 时必需 |
| Default CI | 未设置；smoke tests self-skip | 默认 disabled |

## Default Skip Verification

无 approved local runtime binaries 时使用此检查：

```bash
unset ATLAS_RUNTIME_SMOKE_ENABLED
unset ATLAS_TRINITY_OFFICE_COMMAND
unset ATLAS_TRINITY_OFFICE_SMOKE_ARGS
unset ATLAS_DOCUMENT_NORMALIZE_COMMAND
unset ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

期望结果：

- Focused test command 成功退出。
- 两个 optional runtime smoke tests 按设计 skipped。
- 缺少真实 runtime binaries 不被视为产品失败。

安全 evidence 文案：

```text
ConfiguredRuntimeSmokeIT passed with optional runtime smoke tests skipped because approved local runtime env vars were absent.
```

## Approved Local Smoke Verification

仅在 local binary 获批后运行。此处只使用占位符；真实值只在本地 shell 设置，不记录。

```bash
export ATLAS_RUNTIME_SMOKE_ENABLED=true
export ATLAS_TRINITY_OFFICE_COMMAND="<approved-trinity-command>"
export ATLAS_TRINITY_OFFICE_SMOKE_ARGS="--version"
export ATLAS_DOCUMENT_NORMALIZE_COMMAND="<approved-document-normalize-command>"
export ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS="--version"
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

期望结果：

- 每个 configured runtime command exit zero。
- 没有 timeout。
- Sanitized diagnostics 不包含 raw command value。

安全 evidence 文案：

```text
ConfiguredRuntimeSmokeIT passed for approved local Trinity and Document Normalize command-health smoke checks; raw command values were not recorded.
```

如果只有一个 runtime 获批，只设置对应 command，并将另一个 runtime 记录为 skipped with reason。

## Outcome Matrix

| Outcome | Meaning | Action |
|---|---|---|
| Skipped by disabled smoke | `ATLAS_RUNTIME_SMOKE_ENABLED` 缺失或不是 `true` | 对默认 CI 可接受；记录 skip evidence。 |
| Skipped by missing command | Smoke 已启用但某个 command env var 缺失 | 仅当该 runtime 未获准 local smoke 时可接受；记录原因。 |
| Passed | Approved command exits zero 且不 timeout | 记录 safe pass evidence，不记录 raw command values。 |
| Failed | Approved command exits non-zero | 记录 sanitized summary；不要粘贴 raw output。 |
| Timed out | Command 未在允许时间内完成 | 记录 safe timeout summary，并在普通 verification 前关闭 smoke。 |
| Unsafe diagnostic | Output 包含 private path、host、credential-like value 或 raw local details | Closeout 前 sanitize 或丢弃 evidence。 |
| Blocked | Approved local binary 不可用 | 以 reason 跳过 approved-pass evidence；不要伪造成功。 |

## Troubleshooting

| Symptom | Likely cause | Safe next step |
|---|---|---|
| All smoke tests skipped | Smoke enable flag 缺失或不是 `true` | 这是默认 CI 的预期行为。 |
| One runtime skipped | Command env var 缺失 | 确认该 runtime 是否获准 local smoke。 |
| Non-zero exit | Binary 不可用、args 不兼容或 runtime error | 在 committed evidence 外做本地诊断；只记录 sanitized summary。 |
| Timeout | Command hang 或启动了 long-running process | 关闭 smoke env vars，使用 `--version`，并保持普通 verification mock-safe。 |
| Diagnostic includes local path or host | Runtime 输出了机器细节 | 不粘贴 raw output；只使用 sanitized summary。 |

## Disable And Rollback

回到默认 mock-safe 状态：

```bash
unset ATLAS_RUNTIME_SMOKE_ENABLED
unset ATLAS_TRINITY_OFFICE_COMMAND
unset ATLAS_TRINITY_OFFICE_SMOKE_ARGS
unset ATLAS_DOCUMENT_NORMALIZE_COMMAND
unset ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS
cd backend && mvn -Dtest=ConfiguredRuntimeSmokeIT test
```

期望 rollback result：

- Optional runtime smoke tests self-skip。
- 默认 backend verification 可在没有真实 binaries 时运行。

## Closeout Evidence Checklist

关闭本切片或 local smoke check 前，记录：

- SDD acceptance 是否在 implementation 前记录。
- `ConfiguredRuntimeSmokeIT` 是以 skips 通过还是以 approved pass evidence 通过。
- `AdapterSeamGuardTest` 是否通过。
- `mvn verify` 是否通过。
- `git diff --check` 是否通过。
- Focused secret/private-path scan 是否通过。
- Focused network/dependency scan 是否通过。
- Approved local runtime pass evidence 是否跳过，以及原因。
- Residual risks，尤其是 command-level smoke limitations。

## Safety Bans

不得提交、粘贴或附加：

- 真实公司文档；
- raw command paths 或 local binary paths；
- raw stdout/stderr diagnostics；
- secrets、credentials 或 provider logs；
- private absolute paths；
- internal hostnames 或 endpoints；
- shell history；
- local env files；
- 暴露 local paths、credentials 或 confidential content 的 screenshots。

## Next Scope Boundary

未来 accepted slice 可以增加 mock/sample-safe document processing smoke。该工作必须在实现前定义 fixtures、expected converter/parser output、evidence handling 与 verification。

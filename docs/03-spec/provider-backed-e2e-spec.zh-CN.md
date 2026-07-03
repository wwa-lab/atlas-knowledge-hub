# 规格：Provider-Backed E2E

## 状态

草案。`provider-backed-e2e` 的轻量行为源。本轮只定义 slice，不实现脚本、adapter、测试或 package commands。

## 概述

`provider-backed-e2e` 增加第三层验收，用于显式 provider-backed Ask journey。它不同于 first-layer mock E2E 和 second-layer local full-stack E2E：第三层允许调用 DeepSeek，但必须显式 opt-in，并且只能经过 ModelAdapter 边界。Journey 仍然使用本地服务和 mock/sample knowledge data，用来证明真实 Ask 页面可以使用 provider-generated text，同时不削弱 Atlas 数据安全规则。

## 验收分层模型

| 层级 | 目的 | Provider 调用 | 数据 |
|---|---|---:|---|
| First-layer | 快速 local/mock gate，覆盖已实现页面和后端合约。 | 否 | 仅 mock/sample |
| Second-layer | 本地 full-stack browser + Spring Boot API + 临时 PostgreSQL gate。 | 否 | 仅 mock/sample |
| Third-layer provider-backed | Opt-in 本地 provider-backed Ask browser journey。 | 是，只能通过 ModelAdapter 调用 DeepSeek | 仅 mock/sample |

第三层行为不得接入 first-layer、second-layer、`npm run e2e`、普通 backend verification 或默认 CI。

## 范围

范围内：

- 后续一个 opt-in 命令，启动本地 stack 并执行 provider-backed Ask E2E。
- 对显式 opt-in 和必需 DeepSeek 环境配置的 preflight checks。
- 浏览器 journey：从 Atlas Ask 页面提问，并验证 source-grounded output。
- 后端路径：provider 执行必须通过 ModelAdapter。
- 缺凭据、网络错误、provider 429、provider 5xx、timeout、provider 输出格式异常的安全处理。
- 日志、截图、trace、视频、报告、generated output 和 git 的 artifact hygiene。
- 确认 first-layer 和 second-layer 仍然通过的回归检查。

范围外：

- 本轮代码实现。
- 修改默认 first-layer/second-layer 行为。
- 真实公司数据、真实私有 endpoints、生产 auth/RBAC、成本治理、provider 账户管理、streaming，以及 DeepSeek 以外的 provider 选择。

## 参与者

| 参与者 | 角色 |
|---|---|
| 本地开发者 | 提供本地环境变量并运行 opt-in 命令。 |
| Knowledge user | 在浏览器里基于 sample knowledge 使用 Ask 页面。 |
| Platform engineer | 验证 adapter 隔离、安全失败和 artifact hygiene。 |
| Codex implementation agent | 后续严格按照本 spec 和任务清单实现。 |

## 功能需求

### Layer Isolation

- **FR-PBE2E-001:** 第三层 E2E 必须拥有独立命令，并且不得从 first-layer、second-layer、默认 frontend E2E、默认 backend verification 或 setup commands 中运行。 (REQ-PBE2E-001, REQ-PBE2E-010)
- **FR-PBE2E-002:** 第三层执行除了 provider credentials 外，还必须要求显式 opt-in 信号。 (REQ-PBE2E-001, REQ-PBE2E-008)
- **FR-PBE2E-003:** 文档和命令输出必须把第三层描述为 local/developer acceptance，而不是默认 CI gate。 (REQ-PBE2E-011)

### Local Stack And Browser Journey

- **FR-PBE2E-004:** 后续命令必须启动或复用 Ask 所需的本地 frontend、Spring Boot backend 和 PostgreSQL stack。 (REQ-PBE2E-002)
- **FR-PBE2E-005:** Journey 必须 seed 或复用 mock/sample approved knowledge evidence，不得 ingest 真实公司文档。 (REQ-PBE2E-003)
- **FR-PBE2E-006:** Playwright 必须驱动可见 Ask 页面，提交问题，等待 provider-backed result，并断言 answer、citations/evidence、review-required status 和 safe provider status。 (REQ-PBE2E-002, REQ-PBE2E-006)

### Provider Configuration

- **FR-PBE2E-007:** DeepSeek credentials 只能在运行时从环境变量读取。 (REQ-PBE2E-004)
- **FR-PBE2E-008:** 当 key 缺失、opt-in 缺失或配置明显无效时，preflight 必须在 provider 执行前失败。 (REQ-PBE2E-007, REQ-PBE2E-008)
- **FR-PBE2E-009:** Frontend source 和浏览器可见状态永远不得接收 raw provider credentials 或 raw auth headers。 (REQ-PBE2E-004, REQ-PBE2E-009)

### ModelAdapter Boundary

- **FR-PBE2E-010:** Ask orchestration 必须通过现有 model service/ModelAdapter contract 调用 provider generation。 (REQ-PBE2E-005)
- **FR-PBE2E-011:** Provider-specific HTTP client、endpoint、request signing 和 response parsing 只能位于 DeepSeek ModelAdapter implementation 或 provider adapter package 中。 (REQ-PBE2E-005, REQ-PBE2E-012)
- **FR-PBE2E-012:** Controller、generic service、repository、vector、graph、frontend 和 test orchestration layers 不得包含直接 provider execution logic。 (REQ-PBE2E-005, REQ-PBE2E-012)

### Safe Failure Behavior

- **FR-PBE2E-013:** 缺少 key 或缺少 opt-in 时，必须返回 skipped/preflight-failed result，而不是部分启动 provider run。 (REQ-PBE2E-007, REQ-PBE2E-008)
- **FR-PBE2E-014:** 网络失败、timeout、provider 429、provider 5xx 和 provider 输出格式异常，必须在 API、UI 和 test output 中产生 sanitized failure states。 (REQ-PBE2E-007)
- **FR-PBE2E-015:** Safe failures 必须保留足够高层状态用于排查，但不得暴露 secrets、raw provider responses、raw request headers、stack traces、private paths 或 confidential source text。 (REQ-PBE2E-007, REQ-PBE2E-009)

### Artifact Hygiene

- **FR-PBE2E-016:** Provider-backed E2E 必须关闭或清洗 Playwright trace/video/screenshot retention，除非实现可以证明不会捕获 secrets 或 raw provider payloads。 (REQ-PBE2E-009)
- **FR-PBE2E-017:** Backend logs、frontend console logs、reports、generated output 和 git diff 必须扫描 secret-like values、private absolute paths、raw provider payload markers 和 real company data markers。 (REQ-PBE2E-009, REQ-PBE2E-012)
- **FR-PBE2E-018:** First-layer 和 second-layer verification 必须保留为实现 close-out 的一部分。 (REQ-PBE2E-010)

## 状态模型

Provider-backed E2E run status:

```text
PREFLIGHT -> SKIPPED_MISSING_OPT_IN
          -> SKIPPED_MISSING_KEY
          -> STARTING_LOCAL_STACK -> SEEDING_SAMPLE_EVIDENCE
          -> ASKING_PROVIDER -> SUCCEEDED
                            -> FAILED_SAFE
```

Provider call outcome:

| 结果 | 期望行为 |
|---|---|
| 缺少 opt-in | 在 stack/provider execution 前停止；报告 skipped。 |
| 缺少 key | 在 provider execution 前停止；报告 skipped 或 preflight failed。 |
| 网络失败 / timeout | 显示 sanitized safe error；不包含 raw request/response material。 |
| Provider 429 | 显示 sanitized rate-limit state；不包含 raw provider payload。 |
| Provider 5xx | 显示 sanitized provider-unavailable state；不包含 raw provider payload。 |
| Provider 输出格式异常 | 标记 run failed safe；不持久化 raw payload。 |
| 成功 | 浏览器显示 answer、citations、source evidence、review-required status 和 safe provider status。 |

## API / Interface Surface

这个轻量 SDD 不新增 API guide。后续实现可以扩展现有 model-adapter 和 ask-rag contracts；但如果需要新的 backend API，任何 provider-specific endpoint 或配置行为都必须在代码变更前进入已接受的 SDD/API guide。

预期实现 surface：

| Surface | 期望行为 |
|---|---|
| E2E shell script 或 npm command | Opt-in 第三层本地编排。 |
| DeepSeek ModelAdapter | 在 server-side 读取环境配置、调用 provider、返回 sanitized adapter result。 |
| Ask service | 只使用 model service/ModelAdapter；保留 Ask/RAG evidence 和 review status。 |
| Playwright provider-backed spec | 用 sample evidence 驱动浏览器 Ask 页面。 |
| Safety scans | 验证没有 secrets、raw provider responses、private paths 或真实数据进入 artifacts/git。 |

## 验收矩阵

| 检查 | 需求 | 可观察结果 |
|---|---|---|
| AC-PBE2E-01 | REQ-PBE2E-001, 010, 011 | 第三层有独立 opt-in 命令，且不在 first-layer、second-layer、默认 frontend E2E、默认 backend verification 和 CI 中出现。 |
| AC-PBE2E-02 | REQ-PBE2E-002, 003, 006 | 浏览器 Ask journey 使用 sample approved evidence，并显示 provider-backed answer、citations 和 review-required status。 |
| AC-PBE2E-03 | REQ-PBE2E-004, 008 | 缺 opt-in 或缺 key 时，在 provider execution 前停止。 |
| AC-PBE2E-04 | REQ-PBE2E-005, 012 | Provider-specific execution 被隔离在 ModelAdapter/provider adapter code；seam checks 阻止其他地方直接调用。 |
| AC-PBE2E-05 | REQ-PBE2E-007 | 缺 key、网络失败、429、5xx、timeout 和 malformed output 都以 sanitized messages 安全失败。 |
| AC-PBE2E-06 | REQ-PBE2E-009, 012 | 日志、截图、trace、报告、generated outputs 和 git diff 不包含 raw secrets、raw provider responses、auth headers、private paths 或真实公司数据。 |
| AC-PBE2E-07 | REQ-PBE2E-010 | 实现后 `npm run e2e:first-layer` 和 `npm run e2e:second-layer` 仍然通过。 |

## 后续实现验证计划

后续实现至少必须包含：

```bash
npm run e2e:first-layer
npm run e2e:second-layer
# future opt-in command, name to be finalized
git diff --check
```

还必须对 provider-backed E2E docs、scripts、backend adapter code、frontend artifacts、Playwright output、backend logs 和 generated sample output 做定向扫描。

## 未决问题

- OQ-PBE2E-001: DeepSeek key 的精确环境变量名。
- OQ-PBE2E-002: 第三层失败时 trace/video retention policy。
- OQ-PBE2E-003: 未来是否需要带托管 secrets 的受保护 CI。

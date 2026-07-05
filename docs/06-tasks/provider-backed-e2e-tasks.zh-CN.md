# 任务：Provider-Backed E2E

## 状态

后续实现任务清单草案。本轮文档 pass 不实现代码、脚本、测试或 package commands。

## 来源规格

- Requirements: `docs/01-requirements/provider-backed-e2e-requirements.md`
- Spec: `docs/03-spec/provider-backed-e2e-spec.md`
- Traceability: `docs/00-context/provider-backed-e2e-traceability.md`

## 每个任务的约束

- 第三层 provider-backed E2E 必须 opt-in，并与 first-layer、second-layer、默认 frontend E2E、backend verification、setup、默认 CI 隔离。
- 继续只使用 mock/sample knowledge data。
- Provider API key 只能来自环境变量。
- Provider execution 必须停留在 ModelAdapter 边界内。
- 不得记录、持久化、截图、trace、返回或提交 raw keys、raw auth headers、raw provider responses、private paths 或真实公司数据。
- 缺 key、网络失败、provider 429、provider 5xx、timeout 和 malformed output 必须安全失败。
- First-layer 和 second-layer 必须继续通过。

## 工作流

| Workstream | Tasks |
|---|---|
| Preflight and local orchestration | T-PBE2E-001, T-PBE2E-002 |
| Provider adapter path | T-PBE2E-003, T-PBE2E-004 |
| Browser E2E | T-PBE2E-005 |
| Safe failures and hygiene | T-PBE2E-006, T-PBE2E-007 |
| Regression and documentation close-out | T-PBE2E-008 |

## 任务详情

### T-PBE2E-001: 定义第三层命令契约

- **映射到:** REQ-PBE2E-001, REQ-PBE2E-002, REQ-PBE2E-008, REQ-PBE2E-011；spec sections `Layer Isolation`, `Local Stack And Browser Journey`。
- **Owner type:** QA/devex
- **Priority:** Must
- **Dependencies:** None
- **范围:** 为 provider-backed E2E 增加后续 opt-in command name 和 orchestration contract。该命令不得被 first-layer、second-layer、默认 `npm run e2e`、setup、backend verify 或默认 CI 调用。
- **约束:** 只能 opt-in；没有显式 opt-in 和 key preflight 时不得 provider execution。
- **验证:**
  ```bash
  npm run e2e:first-layer
  npm run e2e:second-layer
  git diff --check
  ```

### T-PBE2E-002: 增加 Preflight 环境检查

- **映射到:** REQ-PBE2E-004, REQ-PBE2E-007, REQ-PBE2E-008；spec sections `Provider Configuration`, `Safe Failure Behavior`。
- **Owner type:** QA/devex
- **Priority:** Must
- **Dependencies:** T-PBE2E-001
- **范围:** 在 provider execution 前验证显式 opt-in、必需 DeepSeek 环境变量、安全本地模式和 sample-data-only mode。缺 key 或缺 opt-in 必须在 provider calls 前停止。
- **约束:** 不得 echo secret values；只能打印 configured/missing status。
- **验证:**
  ```bash
  git diff --check
  # 实现必须包含 missing-key preflight test，证明不会启动 provider call。
  ```

### T-PBE2E-003: 在 ModelAdapter 后实现 configured provider

- **映射到:** REQ-PBE2E-004, REQ-PBE2E-005, REQ-PBE2E-006, REQ-PBE2E-012；spec sections `ModelAdapter Boundary`, `Provider Configuration`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-002
- **范围:** 新增或扩展支持 configured provider 的 ModelAdapter implementation，在 server-side 读取运行时环境配置，调用 DeepSeek 或 GitHub Models，将输出归一化为现有 model result shape，并保留 review-required output status。
- **约束:** Provider-specific client/request/response parsing 只能位于 adapter implementation；不得持久化 raw provider payload。
- **验证:**
  ```bash
  cd backend && mvn -Dtest=ModelAdapterContractTest,AdapterSeamGuardTest test
  git diff --check
  ```

### T-PBE2E-004: 保持 Ask Orchestration 经过 Adapter

- **映射到:** REQ-PBE2E-005, REQ-PBE2E-006, REQ-PBE2E-012；spec sections `ModelAdapter Boundary`, `Local Stack And Browser Journey`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-003
- **范围:** 确保 Ask 使用现有 model service/ModelAdapter path 做 provider-backed generation，同时保留 scoped sample evidence、citations、confidence 或 safe status、review-required output。
- **约束:** Ask service、controllers、repositories、graph、vector 和 frontend 不得直接引用 provider execution logic。
- **验证:**
  ```bash
  cd backend && mvn -Dtest=AskServiceTest,AskApiContractIT,AdapterSeamGuardTest test
  ```

### T-PBE2E-005: 增加 Provider-Backed Playwright Ask Journey

- **映射到:** REQ-PBE2E-002, REQ-PBE2E-003, REQ-PBE2E-006, REQ-PBE2E-009；spec sections `Local Stack And Browser Journey`, `Artifact Hygiene`。
- **Owner type:** QA/frontend
- **Priority:** Must
- **Dependencies:** T-PBE2E-004
- **范围:** 增加 opt-in Playwright spec，使用 sample approved evidence 驱动可见 Ask 页面，提交稳定 sample question，并断言 provider-backed answer、citations/evidence、review-required status 和 safe provider status。
- **约束:** 只使用 mock/sample knowledge；关闭或清洗 traces、videos、screenshots、console logs 和 reports。
- **验证:**
  ```bash
  # future opt-in provider-backed E2E command
  git diff --check
  ```

### T-PBE2E-006: 增加 Safe Failure 覆盖

- **映射到:** REQ-PBE2E-007, REQ-PBE2E-008, REQ-PBE2E-009；spec sections `Safe Failure Behavior`, `State Model`。
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-PBE2E-003, T-PBE2E-005
- **范围:** 覆盖缺 opt-in、缺 key、网络失败、timeout、provider 429、provider 5xx 和 malformed provider output。每种情况都必须安全失败，并只暴露 sanitized status。
- **约束:** 当失败可以在 adapter boundary 模拟时，测试不得依赖真实失败 provider calls。
- **验证:**
  ```bash
  cd backend && mvn -Dtest=ModelAdapterContractTest,AskServiceTest test
  # future provider-backed E2E failure-mode command or targeted test
  git diff --check
  ```

### T-PBE2E-007: 增加 Artifact And Secret Hygiene Gates

- **映射到:** REQ-PBE2E-004, REQ-PBE2E-009, REQ-PBE2E-012；spec sections `Artifact Hygiene`, `Acceptance Matrix`。
- **Owner type:** security/QA
- **Priority:** Must
- **Dependencies:** T-PBE2E-005, T-PBE2E-006
- **范围:** 为 provider-backed docs、scripts、backend adapter code、frontend artifacts、Playwright reports、test results、backend logs、generated samples 和 git diff 增加扫描或约束。
- **约束:** 不得出现 raw secrets、raw auth headers、raw provider responses、private absolute paths 或真实公司数据。
- **验证:**
  ```bash
  git diff --check
  rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" docs backend frontend scripts samples || true
  ```

### T-PBE2E-008: 运行回归并 Close Out

- **映射到:** REQ-PBE2E-001, REQ-PBE2E-010, REQ-PBE2E-011, REQ-PBE2E-012；spec sections `Acceptance Matrix`, `Verification Plan For Future Implementation`。
- **Owner type:** QA/docs
- **Priority:** Must
- **Dependencies:** T-PBE2E-001 through T-PBE2E-007
- **范围:** 证明 first-layer 和 second-layer 仍然通过，运行 provider-backed opt-in E2E，运行 hygiene scans，review diff，并用 implementation evidence 和 residual risks 更新 traceability。
- **约束:** 任何跳过的检查必须说明原因；不得暗示未运行检查已通过。
- **验证:**
  ```bash
  npm run e2e:first-layer
  npm run e2e:second-layer
  # future opt-in provider-backed E2E command
  git diff --check
  ```

## 依赖计划

Critical path: T-PBE2E-001 -> T-PBE2E-002 -> T-PBE2E-003 -> T-PBE2E-004 -> T-PBE2E-005 -> T-PBE2E-006 -> T-PBE2E-007 -> T-PBE2E-008。

Adapter shape 稳定后，T-PBE2E-006 failure simulations 可以和 T-PBE2E-005 并行开发。

## 未决问题

- OQ-PBE2E-001: Provider key 的精确环境变量名。
- OQ-PBE2E-002: 第三层失败时 trace/video retention policy。
- OQ-PBE2E-003: 未来是否需要带托管 secrets 的受保护 CI。

# 实现任务：Converter Adapter

## 状态

草稿。Phase 3 adapter 切片。仅在 SDD review/acceptance 后执行。

## 实现契约

- **Spec 真相源：** `docs/03-spec/converter-adapter-spec.md`。
- **Design 契约：** `docs/05-design/converter-adapter-design.md`。
- **API/adapter 契约：** `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md`。
- **约束：** 仅 adapter、产品层禁止直连工具、无外部云/网络依赖、mock-engine tests、secret 脱敏配置、仅相对路径、保留 source trace/confidence/review status。

## 验证命令

完成前运行：

```bash
cd backend && mvn verify
git diff --check
rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md
```

Command/network scan 在非 adapter 产品层必须无匹配。若 adapter implementation code 包含 command-runner boundaries，必须由 seam guard tests 覆盖，且不得泄漏到 controller/service/repository/domain。

## 任务拆分

| Task ID | Task | Owner | Priority | Dependencies | Requirement / Spec Mapping | Verification |
|---|---|---|---|---|---|---|
| T-CA-001 | 添加 `conversion_run` 和 `conversion_file_result` 的 converter domain types 与 persistence model，包括 `ConversionRunStatus`、repositories、migrations 和 DTOs。字段与 data model 对齐，并对含路径字段使用相对路径校验。 | Codex | Must | None | REQ-CA-004, REQ-CA-007, REQ-CA-008 / Spec "Metadata Write-Back", "State Model"; Data Model "New Logical Entities" | `cd backend && mvn -q test -Dtest=*Conversion*` 通过；Flyway validation 包含在 `mvn verify` 中。 |
| T-CA-002 | 在 adapter boundary 实现 converter adapter contracts：capability、request、result、per-file result、adapter status 和 `ConverterAdapter` interface。产品面 records 不暴露 vendor-specific command flags。 | Codex | Must | T-CA-001 | REQ-CA-001, REQ-CA-002, REQ-CA-003 / Spec "Adapter Boundary", "Capability Metadata"; Design "Converter Adapter Interface" | 单元测试实例化 contract records 并验证 supported source type/status mapping。 |
| T-CA-003 | 实现 converter adapter registry 和脱敏 capability metadata，包括 default adapter resolution 以及 `AVAILABLE` / `DISABLED` / `MISCONFIGURED` 状态。 | Codex | Must | T-CA-002 | REQ-CA-002, REQ-CA-003, REQ-CA-009 / Spec "Capability Metadata"; API guide "Adapter Capability Contract" | `GET /api/converter-adapters` 的 API contract test；secret/path scan 不显示原始 command path 或 secret。 |
| T-CA-004 | 实现 CI 和测试用 mock/fake converter adapter。覆盖 Office success、Office failure、PDF pass-through、image `OCR_REQUIRED`、unsupported `UNSUPPORTED` 和 misconfigured adapter 行为。 | Codex | Must | T-CA-002, T-CA-003 | REQ-CA-005, REQ-CA-006, REQ-CA-010 / Spec "Conversion Run", "Per-File Status Mapping"; Design "Mock/Fake Converter Adapter" | 所有 mock outcome 单元测试通过；不要求真实 `trinity-office`。 |
| T-CA-005 | 实现 conversion application service：校验 batch/file targets、创建 run、解析 adapter、执行 adapter interface、校验/清洗 results、持久化 result rows、更新 file item status/PDF/confidence/error，并派生 summary。 | Codex | Must | T-CA-001..T-CA-004 | REQ-CA-004, REQ-CA-005, REQ-CA-006, REQ-CA-007, REQ-CA-008 / Spec "Metadata Write-Back", "Validation Rules", "Reporting And Failure Behavior" | Service tests 覆盖 success、partial failure、adapter unavailable、unsafe path rejection 和 review status preservation。 |
| T-CA-006 | 添加 converter capabilities、create conversion run 和 retrieve conversion run 的 API endpoints。使用 `ApiEnvelope` 和用户安全错误；不新增前端 routes。 | Codex | Must | T-CA-005 | REQ-CA-003, REQ-CA-004, REQ-CA-009, REQ-CA-012 / Spec "API / Interface Surface"; API guide endpoint sections | MockMvc/API contract tests 覆盖 status code、envelope shape、payload fields 和 safe errors。 |
| T-CA-007 | 在 adapter implementation 后添加可选 `trinity-office` wrapper，并使用配置门控。缺少必要配置时报告 `MISCONFIGURED`，且清洗 command output。自动化测试不得要求真实 binary。 | Codex | Should | T-CA-002, T-CA-003, T-CA-004 | REQ-CA-002, REQ-CA-009, REQ-CA-010 / Design "Trinity-office Adapter Boundary"; Spec "Constraints" | Tests 使用 fake command runner；capability tests 证明缺少配置返回 `MISCONFIGURED`；response 无原始路径/secret。 |
| T-CA-008 | 将 adapter seam guard 从 Phase 2 空包规则更新为 Phase 3 scoped rule：concrete engine names 和 command execution APIs 只允许出现在 adapter implementation packages 和 test fakes。 | Codex | Must | T-CA-002, T-CA-007 | REQ-CA-001, REQ-CA-011 / Spec "Adapter Boundary"; Architecture "Layer Boundaries" | Static guard test 对 controller/service/repository/domain 中注入的 direct reference 会失败；command/network `rg` 在非 adapter 层无匹配。 |
| T-CA-009 | 使用 mock converter adapter 和 seeded metadata 添加 PostgreSQL/test profile 集成测试。验证 file item updates、result persistence、summary counts 和 report retrieval。 | Codex | Must | T-CA-005, T-CA-006 | REQ-CA-004, REQ-CA-005, REQ-CA-006, REQ-CA-010 / Spec "Acceptance Matrix"; API guide "Contract Tests" | `cd backend && mvn verify` 通过。 |
| T-CA-010 | 执行最终验证并更新 slice traceability/status evidence；除非所有检查通过，否则不得标记实现完成。跳过的检查需写明原因。 | Codex | Must | T-CA-001..T-CA-009 | REQ-CA-010, REQ-CA-012 / Spec "Acceptance Matrix"; Project Rules "Quality Gates" | 运行 "Verification Commands" 中所有命令；更新 `docs/00-context/converter-adapter-traceability.md` 和 `.zh-CN.md` 的 evidence。 |

## 依赖计划

关键路径：

```text
T-CA-001 -> T-CA-002 -> T-CA-003 -> T-CA-004 -> T-CA-005 -> T-CA-006 -> T-CA-009 -> T-CA-010
```

并行机会：

- T-CA-007 可在 T-CA-004 后推进，因为它使用 fake command-runner tests。
- T-CA-008 可在 T-CA-002 后推进，并在 T-CA-007 后最终确认。

## 完成定义

- 所有 Must tasks 完成。
- `cd backend && mvn verify` 通过。
- Static guard 证明产品层不直接调用 converter engines。
- 未引入原始 secret、私有路径、真实公司数据或外部云调用。
- Conversion results 保留 source path、PDF path、confidence、review status、adapter identity 和 safe errors。
- Traceability files 记录 verification evidence 和 residual risks。

## 残留风险 / 待确认问题

- OQ-CA-001：local process wrapper vs external worker。
- OQ-CA-002：PDF pass-through copy/reference policy。
- OQ-CA-003：确切 `trinity-office` command-line contract。

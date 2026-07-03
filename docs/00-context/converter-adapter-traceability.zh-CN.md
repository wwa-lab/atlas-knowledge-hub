# 溯源：Converter Adapter

## 状态

已于 2026-07-03 实现。Phase 3 adapter 切片。T-CA-001 至 T-CA-010 已严格依据 `docs/03-spec/converter-adapter-spec.md` 和 `docs/06-tasks/converter-adapter-tasks.md` 完成，并使用 mock-engine 验证。

## 切片契约

- **Goal：** Atlas 能通过产品面 converter adapter 把受支持 Office 文件转换为可追溯 PDF 产物，同时保持 `trinity-office` 可替换并被隔离。
- **范围：** converter adapter contract、capability metadata、conversion run lifecycle、result/status metadata write-back、mock-engine verification、seam guard update、API/adapter guide。
- **排除：** parser/PDF-to-Markdown、OCR execution、storage adapter、vector/model adapters、Wiki publish、graph、Ask、frontend UI、production auth/RBAC、真实公司数据、外部云调用。
- **Phase 准入：** `metadata-api` 已在 `docs/00-context/slice-roadmap.md` 记录为 implemented，满足 Phase 3 entry dependency。

## 来源文档

| Source | 用途 |
|---|---|
| `README.md` | 产品工作流和内部工具方向。 |
| `PROJECT_RULES.md` | Adapter、parser-neutral、trace/review 和 data safety 规则。 |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter standards 和 verification expectations。 |
| `docs/00-context/sdd-profile.md` | Required SDD chain 和 ID 规则。 |
| `docs/01-requirements/requirement.md` | 产品需求 REQ-PROD-015 至 REQ-PROD-019 及相关 trace/review 规则。 |
| `docs/00-context/slice-roadmap.md` | Phase 3 verification 和 constraints row。 |
| `docs/architecture.md` | Control plane、worker plane 和 adapter rules。 |
| `docs/batch-processing-design.md` | File status set 和 batch processing model。 |
| `docs/markdown-standard.md` | Source trace、confidence、review status、relative path requirements。 |
| `docs/03-spec/metadata-api-spec.md` | 现有 metadata API behavior 和 adapter-neutral baseline。 |
| `docs/04-architecture/metadata-api-data-model.md` | 现有 file item fields 和 status model。 |
| `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | 现有 envelope、validation 和 file metadata API conventions。 |

## 生成的 SDD 集

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/converter-adapter-requirements.md` | `docs/01-requirements/converter-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/converter-adapter-stories.md` | `docs/02-user-stories/converter-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/converter-adapter-spec.md` | `docs/03-spec/converter-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/converter-adapter-architecture.md` | `docs/04-architecture/converter-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/converter-adapter-data-flow.md` | `docs/04-architecture/converter-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/converter-adapter-data-model.md` | `docs/04-architecture/converter-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/converter-adapter-design.md` | `docs/05-design/converter-adapter-design.zh-CN.md` |
| API/adapter guide | `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/converter-adapter-tasks.md` | `docs/06-tasks/converter-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/converter-adapter-traceability.md` | `docs/00-context/converter-adapter-traceability.zh-CN.md` |

## 使用的技能链

- `atlas-sdd-generate-all` 用于编排和双语 SDD 契约。
- `req-to-user-story` 用于从 requirements 推导 `US-CA-*` stories。
- `user-story-to-spec` 用于推导行为、workflow、state、validation 和 acceptance。
- `spec-to-architecture` 用于推导 architecture 和 data flow。
- `architecture-to-design` 用于推导 detailed design、data model 和 API/adapter guide。
- `design-to-tasks` 用于推导 Codex 可执行 tasks。
- 因本切片引入 adapter boundary 和 data flow，应用了 `architecture-review` 标准。
- 本 SDD pass 末尾应用了 `review-doc-quality` gate。

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-CA-001 | US-CA-001, US-CA-005 | Adapter Boundary | T-CA-002, T-CA-008 |
| REQ-CA-002 | US-CA-001 | Adapter Boundary, Capability Metadata | T-CA-002, T-CA-003, T-CA-007 |
| REQ-CA-003 | US-CA-002 | Capability Metadata | T-CA-003, T-CA-006 |
| REQ-CA-004 | US-CA-003 | Conversion Run, Metadata Write-Back | T-CA-001, T-CA-005, T-CA-006, T-CA-009 |
| REQ-CA-005 | US-CA-001 | Conversion Run, Per-File Status Mapping | T-CA-004, T-CA-005, T-CA-009 |
| REQ-CA-006 | US-CA-004 | Reporting And Failure Behavior | T-CA-004, T-CA-005, T-CA-009 |
| REQ-CA-007 | US-CA-003 | Metadata Write-Back | T-CA-001, T-CA-005 |
| REQ-CA-008 | US-CA-003 | Validation Rules | T-CA-001, T-CA-005 |
| REQ-CA-009 | US-CA-002, US-CA-004 | Constraints, Validation And Error Handling | T-CA-003, T-CA-006, T-CA-007 |
| REQ-CA-010 | US-CA-005 | Non-Functional Requirements, Acceptance Matrix | T-CA-004, T-CA-009, T-CA-010 |
| REQ-CA-011 | US-CA-005 | Adapter Boundary | T-CA-008 |
| REQ-CA-012 | US-CA-005 | API / Interface Surface | T-CA-006, T-CA-010 |

## API Guide Decision

API/adapter guide **已包含**，因为 Phase 3 要求每个 adapter slice 提供 adapter contract，且本切片引入内部 endpoint 和 adapter interface contract。

## 验证计划

来自 `docs/00-context/slice-roadmap.md` Phase 3 行：

- 针对 **mock engines** 的单元 + 集成测试。
- Parser/converter/model/vector/storage **只**走产品面适配器。
- 禁止直连工具。
- 禁止硬编码单一实现。
- 每个 adapter slice 需要 adapter contract。

确切实现验证命令列在 `docs/06-tasks/converter-adapter-tasks.md`。

## 实现证据

已实现文件覆盖 converter adapter 持久化模型、Flyway migration、adapter contract、mock `trinity-office` adapter、状态型真实 adapter wrapper 边界、registry/service/controller 层、DTO mapping、单元测试、集成测试，以及 seam guard 更新。

实现保留本切片约束：

- Converter 行为只通过产品面的 adapter interface 访问。
- Mock adapter 执行确定性逻辑，不依赖外部网络或本地二进制。
- Adapter capability response 只暴露状态和脱敏配置。
- Conversion result 回写 file status、PDF artifact path、confidence、error message，以及 conversion run/file-result 记录用于 source trace。
- Parser/PDF-to-Markdown、OCR execution、storage adapter、vector/model adapters、Wiki publish、graph、Ask、frontend UI、production auth/RBAC 和真实公司数据仍在范围外。

## 验证证据

2026-07-03 已执行命令：

- `cd backend && mvn -q test -Dtest='*Conversion*,ConverterAdapterContractTest'` — 通过。
- `cd backend && mvn verify` — 通过。
- `git diff --check` — 通过。
- `rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\.getRuntime\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain` — 无命中。
- `rg -n 'api[_-]?[k]ey\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[p]assword\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[t]oken\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\' backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md` — 无命中。

## Review-Doc-Quality Gate

结果：可进入人工 SDD review，带少量已知开放问题。

已执行检查：

- 每个 touched SDD artifact 都存在英文和中文文件。
- REQ/US/T ID 跨语言一致。
- Requirements 映射到 stories、spec、design、API/adapter guide 和 tasks。
- Tasks 可由 Codex 执行，并包含确切命令。
- Adapter/no-network/secret-masked/source-trace 约束在 spec、design、API guide 和 tasks 中重复声明。
- API guide 已包含并记录。

已知开放问题对 mock-engine 实现不构成阻塞，但在生产化或类生产环境依赖真实 `trinity-office` runtime 前必须回答。

## 待确认问题

- OQ-CA-001：local process wrapper vs external worker。
- OQ-CA-002：PDF pass-through copy vs reference policy。
- OQ-CA-003：确切 `trinity-office` command-line contract。

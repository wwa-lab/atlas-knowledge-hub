# 溯源：真实 Office 解析 Runtime

## 状态

已接受并实现。用户在 2026-07-05 的 chat 中接受本 SDD 后，才开始产品代码改动。

## 切片契约

- **Slice:** `real-office-parser-runtime`
- **Wave:** Wave 2 / Runtime Integration
- **Goal:** 在现有 Atlas converter/parser adapter boundaries 后启用受控内部 `trinity-office` 和 `document-normalize` runtime execution。
- **Maturity target:** 已验证的 runtime integration slice；不是 production readiness。

## 来源文档

| Source | Status |
|---|---|
| Goal objective attachment | 已读。 |
| `README.md` | 已读。 |
| `PROJECT_RULES.md` | 已读。 |
| `DEVELOPMENT_STANDARDS.md` | 已读。 |
| `AGENTS.md` | 已读。 |
| `docs/00-context/sdd-profile.md` | 已读。 |
| `docs/01-requirements/requirement.md` | 已读。 |
| `ROADMAP.md` / `.zh-CN.md` | 已读并更新。 |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | 已读。 |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | 已读并更新。 |
| Converter/parser adapter SDD docs | 已读。 |
| Existing converter/parser adapter code anchors | 已验证。 |
| User acceptance | 用户在 2026-07-05 的 chat 中接受，之后才开始产品代码改动。 |

## SDD Skill Chain Evidence

| Skill | File read |
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

SDD skill chain used: yes.

## 已依据的实现锚点

| Area | Verified anchor | Finding |
|---|---|---|
| Converter interface | `backend/src/main/java/com/atlas/metadata/adapter/ConverterAdapter.java:4` | Product-facing conversion adapter contract 已存在。 |
| Parser interface | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4` | Product-facing parser adapter contract 已存在。 |
| Real converter wrapper | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java` | Wrapper 现在在 `ConverterAdapter` 后支持 configured runtime mode；disabled/misconfigured states 保持安全。 |
| Real parser wrapper | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java` | Wrapper 现在在 `ParserAdapter` 后支持 configured runtime mode；disabled/misconfigured states 保持安全。 |
| Conversion service | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:138` | Service 只调用 adapter contract。 |
| Parser service | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:157` | Service 只调用 adapter contract。 |
| Path validation | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | Safe relative path rule 已存在。 |
| Seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Static guard 已保护 adapter boundaries。 |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-REAL-OFFICE-PARSER-RUNTIME-001 | US-REAL-OFFICE-PARSER-RUNTIME-001 | T-REAL-OFFICE-PARSER-RUNTIME-001, T-REAL-OFFICE-PARSER-RUNTIME-012 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-002 | US-REAL-OFFICE-PARSER-RUNTIME-002 | T-REAL-OFFICE-PARSER-RUNTIME-003 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-003 | US-REAL-OFFICE-PARSER-RUNTIME-003 | T-REAL-OFFICE-PARSER-RUNTIME-004 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-004 | US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-002, T-REAL-OFFICE-PARSER-RUNTIME-009 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-005 | US-REAL-OFFICE-PARSER-RUNTIME-004 | T-REAL-OFFICE-PARSER-RUNTIME-005, T-REAL-OFFICE-PARSER-RUNTIME-006 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-006 | US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-005, T-REAL-OFFICE-PARSER-RUNTIME-010, T-REAL-OFFICE-PARSER-RUNTIME-011 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-007 | US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-010, T-REAL-OFFICE-PARSER-RUNTIME-011 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-008 | US-REAL-OFFICE-PARSER-RUNTIME-002, US-REAL-OFFICE-PARSER-RUNTIME-003 | T-REAL-OFFICE-PARSER-RUNTIME-007 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-009 | US-REAL-OFFICE-PARSER-RUNTIME-004, US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-006, T-REAL-OFFICE-PARSER-RUNTIME-007 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-010 | US-REAL-OFFICE-PARSER-RUNTIME-002 | T-REAL-OFFICE-PARSER-RUNTIME-003 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-011 | US-REAL-OFFICE-PARSER-RUNTIME-003 | T-REAL-OFFICE-PARSER-RUNTIME-004 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-012 | US-REAL-OFFICE-PARSER-RUNTIME-003 | T-REAL-OFFICE-PARSER-RUNTIME-004, T-REAL-OFFICE-PARSER-RUNTIME-008 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-013 | US-REAL-OFFICE-PARSER-RUNTIME-003, US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-008 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-014 | US-REAL-OFFICE-PARSER-RUNTIME-002, US-REAL-OFFICE-PARSER-RUNTIME-003 | T-REAL-OFFICE-PARSER-RUNTIME-002, T-REAL-OFFICE-PARSER-RUNTIME-007 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-015 | US-REAL-OFFICE-PARSER-RUNTIME-005 | T-REAL-OFFICE-PARSER-RUNTIME-009, T-REAL-OFFICE-PARSER-RUNTIME-011 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-016 | US-REAL-OFFICE-PARSER-RUNTIME-001 | T-REAL-OFFICE-PARSER-RUNTIME-012 |

## SDD 产物集

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/real-office-parser-runtime-requirements.md` | `docs/01-requirements/real-office-parser-runtime-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/real-office-parser-runtime-stories.md` | `docs/02-user-stories/real-office-parser-runtime-stories.zh-CN.md` |
| Spec | `docs/03-spec/real-office-parser-runtime-spec.md` | `docs/03-spec/real-office-parser-runtime-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/real-office-parser-runtime-architecture.md` | `docs/04-architecture/real-office-parser-runtime-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/real-office-parser-runtime-data-flow.md` | `docs/04-architecture/real-office-parser-runtime-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/real-office-parser-runtime-data-model.md` | `docs/04-architecture/real-office-parser-runtime-data-model.zh-CN.md` |
| Design | `docs/05-design/real-office-parser-runtime-design.md` | `docs/05-design/real-office-parser-runtime-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/real-office-parser-runtime-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/real-office-parser-runtime-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/real-office-parser-runtime-tasks.md` | `docs/06-tasks/real-office-parser-runtime-tasks.zh-CN.md` |
| Traceability | `docs/00-context/real-office-parser-runtime-traceability.md` | `docs/00-context/real-office-parser-runtime-traceability.zh-CN.md` |

## SDD 质量评审结果

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | 已读取 context files 和现有 adapter code anchors。 |
| Skill chain evidence | Passed | 已读取 required SDD skills 和 grounding rules。 |
| Expected artifacts exist | Passed | focused file-existence check 找到全部 20 个双语 SDD 文件。 |
| Bilingual ID parity | Passed | focused `rg` check 确认英中文 REQ、US、T IDs 一致。 |
| Deferred-decision scan | Passed | focused scan 未发现 `TBD`、`TODO`、`FIXME` 或 coding-time decision placeholders。 |
| Architecture/API review | Passed with open questions | Adapter boundaries、API reuse、runtime topology open questions、安全约束已明确。 |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice real-office-parser-runtime` passed；warnings 为若干 companion docs 未嵌入额外 IDs，且未提供 external report。 |
| Diff hygiene | Passed | `git diff --check` passed。 |
| Secret/private-path scan | Passed | focused scan 未发现 raw credentials 或 private local paths。 |
| Network/dependency scan | Passed with expected hits | focused scan 仅命中禁止 external cloud calls 的负向约束和一个 rejected URI 示例。 |
| Product-code gate | Passed | 产品代码只在用户接受后改动。 |

## 实现证据

### 代码改动

- `backend/src/main/java/com/atlas/metadata/adapter/runtime/`
- `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java`
- `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java`
- `backend/src/main/java/com/atlas/metadata/service/ConverterAdapterRegistry.java`
- `backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java`
- `backend/src/main/java/com/atlas/metadata/service/ConversionService.java`
- `backend/src/main/java/com/atlas/metadata/service/ParserService.java`

### 测试改动

- `backend/src/test/java/com/atlas/metadata/adapter/RealOfficeRuntimeAdapterTest.java`
- `backend/src/test/java/com/atlas/metadata/adapter/runtime/RuntimeOutputSanitizerTest.java`
- `backend/src/test/java/com/atlas/metadata/service/ConverterAdapterRegistryTest.java`
- `backend/src/test/java/com/atlas/metadata/service/ParserAdapterRegistryTest.java`
- `backend/src/test/java/com/atlas/metadata/integration/ConversionApiContractIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/ParserApiContractIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java`

### 验证

| Check | Result | Evidence |
|---|---|---|
| Focused runtime/registry/seam tests | Passed | `mvn -Dtest=RuntimeOutputSanitizerTest,RealOfficeRuntimeAdapterTest,ConverterAdapterRegistryTest,ParserAdapterRegistryTest,ConverterAdapterContractTest,ParserAdapterContractTest,AdapterSeamGuardTest test` passed：23 tests。 |
| Optional runtime smoke default behavior | Passed with skips | `mvn -Dtest=ConfiguredRuntimeSmokeIT test` passed；runtime smoke env vars 缺失时 2 tests skipped。 |
| Full backend verification | Passed | `mvn verify` passed：120 unit tests、48 integration tests；2 optional smoke tests 按设计 skipped。 |
| Diff hygiene | Passed | `git diff --check` passed。 |
| Secret/private-path scan | Passed with expected synthetic hits | focused scan 只命中 sanitizer 和 safe-failure tests 中的 fake secret/path strings。 |
| Network/dependency scan | Passed with expected hits | focused scan 命中 rejected URI fixtures、seam-guard strings，以及只在 adapter/runtime scope 出现的 allowed `ProcessBuilder`。 |
| Frontend checks | Not run | 本实现没有修改 frontend files。 |

## 接受记录

用户在 2026-07-05 的 chat 中接受本 SDD。随后实现严格依据已接受的 spec、design、API guide 和 tasks 执行。

## 残留风险

- Runtime stdout JSON contract 已作为 adapter-internal contract 实现，后续可能需要与最终 `trinity-office` 和 `document-normalize` CLIs 对齐。
- Local process execution 后续可能被 worker topology 替代。
- Optional runtime smoke tests 默认 self-skip，只有存在 approved local runtime env vars 时才执行真实 binaries。
- 本切片不代表 production readiness。

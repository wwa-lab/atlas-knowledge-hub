# 任务：真实 Office 解析 Runtime

## 状态

已接受并实现。用户在 2026-07-05 的 chat 中接受本 SDD 后，才开始产品代码改动。

## 任务清单

| ID | Task | Requirements | Verification |
|---|---|---|---|
| T-REAL-OFFICE-PARSER-RUNTIME-001 | 在产品代码改动前记录用户对本双语 SDD 的接受。 | REQ-REAL-OFFICE-PARSER-RUNTIME-001 | Traceability 在实现前包含 accepted gate。 |
| T-REAL-OFFICE-PARSER-RUNTIME-002 | 添加 adapter-internal runtime executor abstraction，timeout 为 120 秒，captured-output limit 为 65536 bytes。 | REQ-REAL-OFFICE-PARSER-RUNTIME-004, REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Unit tests 使用 fake executor 覆盖 success、timeout、oversized output 和 safe failure。 |
| T-REAL-OFFICE-PARSER-RUNTIME-003 | 在 `ConverterAdapter` 后实现 configured `trinity-office` runtime path，替换 throw-only behavior，但不改变 product service callers。 | REQ-REAL-OFFICE-PARSER-RUNTIME-002, REQ-REAL-OFFICE-PARSER-RUNTIME-010 | 使用 fake runtime output 的 converter adapter contract/service tests 通过。 |
| T-REAL-OFFICE-PARSER-RUNTIME-004 | 在 `ParserAdapter` 后实现 configured `document-normalize` runtime path，替换 throw-only behavior，但不改变 product service callers。 | REQ-REAL-OFFICE-PARSER-RUNTIME-003, REQ-REAL-OFFICE-PARSER-RUNTIME-011, REQ-REAL-OFFICE-PARSER-RUNTIME-012 | 使用 fake runtime manifest 的 parser adapter contract/service tests 通过。 |
| T-REAL-OFFICE-PARSER-RUNTIME-005 | 当 mock 和 configured adapters 共存时，让 runtime selection 明确且确定。 | REQ-REAL-OFFICE-PARSER-RUNTIME-005, REQ-REAL-OFFICE-PARSER-RUNTIME-006 | Registry tests 证明 default CI path 保持 mock-safe，configured mode 为 opt-in。 |
| T-REAL-OFFICE-PARSER-RUNTIME-006 | 扩展 capability masking，使 runtime config 只暴露 status-only summaries。 | REQ-REAL-OFFICE-PARSER-RUNTIME-005, REQ-REAL-OFFICE-PARSER-RUNTIME-009 | API contract tests 证明不出现 raw command path、endpoint、host、token、password 或 private path。 |
| T-REAL-OFFICE-PARSER-RUNTIME-007 | 持久化前验证并脱敏 runtime outputs。 | REQ-REAL-OFFICE-PARSER-RUNTIME-008, REQ-REAL-OFFICE-PARSER-RUNTIME-009, REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Tests 覆盖 absolute path、traversal、URI path、hostname、stack trace、secret-like output、invalid confidence 和 duplicate chunk ids。 |
| T-REAL-OFFICE-PARSER-RUNTIME-008 | 保留 trust boundaries，并证明不会出现 Wiki publish、approval、graph、Ask、OCR 或 LLM side effects。 | REQ-REAL-OFFICE-PARSER-RUNTIME-012, REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Focused tests 断言 review status 保持 review-required，且不触碰 downstream tables/services。 |
| T-REAL-OFFICE-PARSER-RUNTIME-009 | 更新 adapter seam guard，允许 runtime implementation scope，同时继续禁止 product-layer 直接引用。 | REQ-REAL-OFFICE-PARSER-RUNTIME-004, REQ-REAL-OFFICE-PARSER-RUNTIME-015 | `cd backend && mvn -Dtest=AdapterSeamGuardTest test` 加 non-adapter product layers 的 negative scans。 |
| T-REAL-OFFICE-PARSER-RUNTIME-010 | 添加 optional runtime smoke tests，缺少 approved runtime config 时 self-skip。 | REQ-REAL-OFFICE-PARSER-RUNTIME-006, REQ-REAL-OFFICE-PARSER-RUNTIME-007 | 普通 `mvn verify` 在没有 binaries 时通过；opt-in smoke 输出 skipped/pass evidence 且不暴露 raw paths。 |
| T-REAL-OFFICE-PARSER-RUNTIME-011 | 运行完整 backend verification 和 safety scans。 | REQ-REAL-OFFICE-PARSER-RUNTIME-006 to 015 | `cd backend && mvn verify`、`git diff --check`、focused secret/private-path scan、focused network/dependency scan。 |
| T-REAL-OFFICE-PARSER-RUNTIME-012 | 实现后更新 traceability、roadmap 和 residual risk notes。 | REQ-REAL-OFFICE-PARSER-RUNTIME-001, REQ-REAL-OFFICE-PARSER-RUNTIME-016 | Context files 列出 changed docs/code、verification evidence、skipped checks 和 next slice。 |

## 实现顺序

1. 完成 acceptance gate，并重读本 task file 和 spec。
2. 实现 runtime executor 和 fake test harness。
3. 实现 configured converter path。
4. 实现 configured parser path。
5. 加固 capability masking 和 sanitizer coverage。
6. 更新 seam guard 和 optional smoke test behavior。
7. 运行 verification，并更新 traceability/roadmap。

## 实现结果

T-REAL-OFFICE-PARSER-RUNTIME-001 到 T-REAL-OFFICE-PARSER-RUNTIME-012 已全部完成。

证据：

- 已在 `backend/src/main/java/com/atlas/metadata/adapter/runtime/` 添加 runtime executor abstraction。
- `TrinityOfficeConverterAdapter` 和 `DocumentNormalizeParserAdapter` 已在现有 adapter contracts 后支持 configured runtime mode。
- `ConverterAdapterRegistry` 和 `ParserAdapterRegistry` 在 key 重叠时确定性解析 `mock` 与 `configured` modes。
- Capability masking 只暴露 status-only config values。
- Runtime outputs 在返回 product-facing results 前经过 bounded capture、sanitization、JSON translation、path/confidence checks。
- Adapter seam guard 只允许 adapter/runtime scope 使用 `ProcessBuilder`，并继续禁止 product-layer 直接引用。
- Optional runtime smoke IT 在缺少 `ATLAS_RUNTIME_SMOKE_ENABLED=true` 和 approved command env vars 时 self-skip。
- Backend verification 已通过 `mvn verify`。
- 未运行 frontend checks，因为本实现没有修改 frontend files。

## 实现后的必跑验证命令

```bash
cd backend && mvn verify
cd backend && mvn -Dtest=AdapterSeamGuardTest test
git diff --check
```

另外对 changed files 运行 focused secret/private-path 和 network/dependency scans。若实现未触碰 frontend files，不要求 frontend checks。

## 约束

- SDD acceptance 前不改产品代码。
- 默认 CI 不依赖真实 `trinity-office` 或 `document-normalize`。
- 不引入外部 cloud calls。
- 仓库文件中不得出现真实公司文档、raw logs、private paths、credentials 或 internal hostnames。
- Runtime names 和 command/process APIs 只允许在 adapter/runtime implementation scope 和 tests 中出现。
- Generated content 保持 review-required；本切片不 publish Wiki pages，也不更新 Ask/Graph。

## 接受后的推荐 Codex 交接

```text
Implement the real-office-parser-runtime slice strictly against docs/03-spec/real-office-parser-runtime-spec.md and docs/06-tasks/real-office-parser-runtime-tasks.md: complete every task in ID order, respect adapter/runtime boundaries, keep normal CI mock-safe, do not expand into Wiki publish/Graph/Ask/OCR/LLM/RBAC, and stop if implementation would diverge from the accepted spec.
```

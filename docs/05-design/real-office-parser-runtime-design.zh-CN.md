# 详细设计：真实 Office 解析 Runtime

## 状态

已接受并实现。用户在 2026-07-05 的 chat 中接受本 SDD 后，才开始产品代码改动。

## Source Architecture

本设计来自 `docs/03-spec/real-office-parser-runtime-spec.md` 和 `docs/04-architecture/real-office-parser-runtime-architecture.md`。

## Grounded Existing Code Context

| Existing element | Verified anchor | Runtime use |
|---|---|---|
| Converter adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ConverterAdapter.java:4` | Runtime conversion 仍必须实现 `ConverterAdapter`。 |
| Parser adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4` | Runtime parsing 仍必须实现 `ParserAdapter`。 |
| Current real converter wrapper disabled behavior | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:37` | 接受后用 configured runtime execution 替换 throw-only 行为。 |
| Current real parser wrapper disabled behavior | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:39` | 接受后用 configured runtime execution 替换 throw-only 行为。 |
| Converter service adapter call | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:138` | Product service 已只调用 `ConverterAdapter.convert`。 |
| Parser service adapter call | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:157` | Product service 已只调用 `ParserAdapter.parse`。 |
| Converter status and path validation | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:211` | Runtime converter results 必须通过现有 status/path checks。 |
| Parser status/path/chunk validation | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:265` | Runtime parser results 必须通过现有 status/path/chunk checks。 |
| Safe relative path validator | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | 复用于 runtime output path rejection。 |
| Artifact storage root safety | `backend/src/main/java/com/atlas/metadata/service/LocalArtifactStorageService.java:104` | Runtime artifact materialization 必须留在 safe root 内。 |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | 更新 guard，允许 runtime executor scope，同时阻止 product layers。 |

## Design Assumptions

- **Runtime timeout default:** 每次 adapter invocation 120 秒。
- **Captured output limit:** 每次 invocation 65536 bytes，之后再 sanitization/truncation。
- **Default CI behavior:** mock/fake adapters 保持默认，不要求内部 binaries。
- **Opt-in runtime smoke:** 仅在未来 environment flag 和 safe sample artifacts 存在时启用。
- **No raw config persistence:** command paths 或 worker endpoints 只保存在 server-side configuration，绝不 raw 存储在 product metadata。

## Design Scope

### In Scope

- Adapter implementation scope 内的 runtime executor abstraction。
- `ConverterAdapter` 后面的 configured `trinity-office` converter behavior。
- `ParserAdapter` 后面的 configured `document-normalize` parser behavior。
- Runtime manifest/result 到现有 product-facing result records 的翻译。
- Sanitization、timeout、max-output、safe relative path validation、seam guard 和 tests。

### Out of Scope

- Production secret manager、auth/RBAC、external cloud calls、connector sync、worker queue/dead-letter、Wiki publish/review gate、graph extraction、Ask indexing、OCR execution、LLM enrichment。

## Module Design

### Runtime Executor Boundary

Runtime executor 是 adapter-internal utility。它接受 sanitized invocation descriptor，并返回 bounded execution result：

- exit status 或 success flag
- bounded stdout/stderr text
- safe working artifact root
- timeout flag

它不得注入 controllers 或 product services。如果实现选择 local process，process launching 保持在 adapter/runtime scope 内。如果实现选择 worker client，该 client 也保持在 adapter/runtime scope 内，并返回相同 bounded result object。

### Converter Runtime Adapter

Configured converter adapter：

1. Required runtime config 缺失时报告 `MISCONFIGURED`。
2. 仅在 status-only config checks 通过时报告 `AVAILABLE`。
3. 将 Atlas converter request file descriptors 转为 approved runtime input manifest 或 command arguments。
4. 在 timeout/output limits 内运行。
5. 将 output 翻译为现有 converter result records。
6. 只返回 safe relative `pdfPath` values 和 sanitized safe messages。

Adapter key 保持兼容现有 `trinity-office` adapter key。如果 mock 和 configured adapters 同时注册，default selection 必须明确且确定，保证正常 CI 仍选择 mock path。

### Parser Runtime Adapter

Configured parser adapter：

1. Required runtime config 缺失时报告 `MISCONFIGURED`。
2. 仅在 status-only config checks 通过时报告 `AVAILABLE`。
3. 将 Atlas parser request file descriptors 转为 approved runtime input manifest 或 command arguments。
4. 将 runtime output manifest 解析为 Markdown path、assets path、confidence、safe error 和 chunk descriptors。
5. 将 runtime output 验证或规范化为现有 parser result records。
6. 除非后续切片接受 deterministic validation，否则返回 source chunks 为 review-required。

Adapter key 保持兼容现有 `document-normalize` adapter key。如果 local PDF text parser 也被注册，default selection 必须保持明确并有测试覆盖。

### Sanitizer

Sanitizer 必须移除或替换：

- token、password、api key patterns
- absolute POSIX 和 Windows paths
- URI/endpoint-like values
- hostnames
- stack frames
- captured output beyond 65536 bytes

Sanitized messages 仍不作为用户内容可信，只作为 diagnostics。

## API / Interface Design

复用现有 API surface：

- `GET /api/converter-adapters`
- `POST /api/batches/{batchId}/conversion-runs`
- `GET /api/conversion-runs/{runId}`
- `GET /api/parser-adapters`
- `POST /api/batches/{batchId}/parser-runs`
- `GET /api/parser-runs/{runId}`

首版实现不需要新增 public endpoint。如实现需要 runtime smoke endpoint，则不在本切片范围内，必须单独走 SDD。

## Data Design

- 不新增 file status values。
- 不新增 review status values。
- 不在 product tables 中 raw 持久化 runtime config。
- 现有 run/result/file/chunk records 继续作为 evidence model。
- Optional runtime-attempt fields 只有在存储 safe status、elapsed time、timeout flag、sanitized message 时才可添加。

## Workflow / Execution Design

### Configured Conversion

1. API 收到带 mode `configured` 或 configured adapter key 的 conversion request。
2. Service 验证 batch 和 files。
3. Registry 解析 adapter。
4. Adapter capability 返回 `AVAILABLE`；否则 run 安全失败。
5. Adapter 在 safe artifact root 下创建 runtime invocation。
6. Runtime executor 返回 bounded output。
7. Adapter 将 output 翻译为 converter result records。
8. Service 验证 result status/path/confidence/error 并持久化 evidence。

### Configured Parsing

1. API 收到带 mode `configured` 或 configured adapter key 的 parser request。
2. Service 验证 batch，并过滤 eligible `PDF_CONVERTED` files。
3. Registry 解析 adapter。
4. Adapter capability 返回 `AVAILABLE`；否则 run 安全失败。
5. Adapter 在 safe artifact roots 下创建 runtime invocation。
6. Runtime executor 返回 bounded output/manifest。
7. Adapter 将 output 翻译为 parser result 和 chunk records。
8. Service 验证 paths、confidence、statuses、chunk uniqueness，并持久化 evidence。

## Validation And Error Handling

| Case | Expected behavior |
|---|---|
| Missing runtime config | Capability `MISCONFIGURED`；requested configured run 安全失败。 |
| Runtime disabled | Capability `DISABLED`；requested configured run 安全失败。 |
| Timeout | Run 失败或 partial fail，包含 safe timeout summary；无关 file metadata 不变。 |
| Exit code failure | 如 manifest 存在则 per-file failure；否则 adapter-level safe failure。 |
| Unsafe output path | Unsafe metadata 持久化前 validation error。 |
| Oversized output | 捕获前 65536 bytes，sanitize，再截断到现有 service limit。 |
| Raw secret/path in output | 持久化/response 前 mask。 |

## Edge Case Trace

### Capability Masking

| Input config | Capability output |
|---|---|
| command path present | `command=configured` |
| command missing | `command=missing`, status `MISCONFIGURED` |
| runtime disabled flag | `command=disabled`, status `DISABLED` |

### Runtime Path Validation

| Runtime output | Result |
|---|---|
| `generated/pdf/a.pdf` | accepted |
| `/tmp/a.pdf` | rejected |
| `https://internal.example/a.pdf` | rejected |

### Parser Confidence

| Runtime confidence | Result |
|---|---|
| `0.799` | `LOW_CONFIDENCE` |
| `0.800` | `MARKDOWN_GENERATED` |
| `null` with successful Markdown | `MARKDOWN_GENERATED` 加 missing-confidence safe evidence |

## Testing Considerations

- Fake executor 覆盖 runtime executor timeout/output limits。
- Unit tests 覆盖 capability masking 和 missing config。
- Service tests 覆盖 configured conversion/parser status mapping。
- Adapter contract tests 覆盖 manifest translation。
- API contract tests 证明现有 endpoints 保持稳定。
- Seam guard tests 证明 runtime calls 留在 adapter/runtime implementation scope 内。
- 完整 `cd backend && mvn verify`。
- `git diff --check`。
- Focused secret/private-path scan 和 network/dependency scan。

## Risks / Design Tradeoffs

| Risk | Decision |
|---|---|
| Runtime contract details are not final. | 使用 fake manifest/command tests，并保持真实 execution config-gated。 |
| Local process may be replaced by worker. | Runtime executor boundary 对 product services 隐藏 topology。 |
| Capability metadata needs enough diagnostics but no secrets. | 暴露 status-only fields，不暴露 raw values。 |
| Two adapters may share existing keys if mock and configured variants coexist. | Implementation 必须让 default selection 明确且有测试。 |

## Open Questions

- OQ-REAL-OFFICE-PARSER-RUNTIME-001: local process vs internal worker topology。
- OQ-REAL-OFFICE-PARSER-RUNTIME-002: exact `trinity-office` command/manifest contract。
- OQ-REAL-OFFICE-PARSER-RUNTIME-003: exact `document-normalize` command/manifest contract。
- OQ-REAL-OFFICE-PARSER-RUNTIME-004: 是否需要在现有 run/result safe summaries 之外添加 optional runtime-attempt metadata。

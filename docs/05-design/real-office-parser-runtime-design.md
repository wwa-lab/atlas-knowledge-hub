# Detailed Design: Real Office Parser Runtime

## Status

Accepted and implemented. User acceptance was recorded in chat on 2026-07-05 before product code changes.

## Source Architecture

This design derives from `docs/03-spec/real-office-parser-runtime-spec.md` and `docs/04-architecture/real-office-parser-runtime-architecture.md`.

## Grounded Existing Code Context

| Existing element | Verified anchor | Runtime use |
|---|---|---|
| Converter adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ConverterAdapter.java:4` | Runtime conversion must still implement `ConverterAdapter`. |
| Parser adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4` | Runtime parsing must still implement `ParserAdapter`. |
| Current real converter wrapper disabled behavior | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:37` | Replace throw-only behavior with configured runtime execution after acceptance. |
| Current real parser wrapper disabled behavior | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:39` | Replace throw-only behavior with configured runtime execution after acceptance. |
| Converter service adapter call | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:138` | Product service already calls only `ConverterAdapter.convert`. |
| Parser service adapter call | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:157` | Product service already calls only `ParserAdapter.parse`. |
| Converter status and path validation | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:211` | Runtime converter results must pass existing status/path checks. |
| Parser status/path/chunk validation | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:265` | Runtime parser results must pass existing status/path/chunk checks. |
| Safe relative path validator | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | Reuse for runtime output path rejection. |
| Artifact storage root safety | `backend/src/main/java/com/atlas/metadata/service/LocalArtifactStorageService.java:104` | Runtime artifact materialization must stay inside safe root. |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Update guard for allowed runtime executor scope while blocking product layers. |

## Design Assumptions

- **Runtime timeout default:** 120 seconds per adapter invocation.
- **Captured output limit:** 65536 bytes per invocation before sanitization/truncation.
- **Default CI behavior:** mock/fake adapters remain default and require no internal binaries.
- **Opt-in runtime smoke:** enabled only when a future environment flag and safe sample artifacts are present.
- **No raw config persistence:** command paths or worker endpoints remain server-side configuration and are never stored raw in product metadata.

## Design Scope

### In Scope

- Runtime executor abstraction inside adapter implementation scope.
- Configured `trinity-office` converter behavior behind `ConverterAdapter`.
- Configured `document-normalize` parser behavior behind `ParserAdapter`.
- Runtime manifest/result translation into existing product-facing result records.
- Sanitization, timeout, max-output, safe relative path validation, seam guard, and tests.

### Out of Scope

- Production secret manager, auth/RBAC, external cloud calls, connector sync, worker queue/dead-letter, Wiki publish/review gate, graph extraction, Ask indexing, OCR execution, and LLM enrichment.

## Module Design

### Runtime Executor Boundary

The runtime executor is an adapter-internal utility. It accepts a sanitized invocation descriptor and returns a bounded execution result:

- exit status or success flag
- bounded stdout/stderr text
- safe working artifact root
- timeout flag

It must not be injected into controllers or product services. If the implementation chooses a local process, process launching remains inside adapter/runtime scope. If the implementation chooses a worker client, that client remains inside adapter/runtime scope and still returns the same bounded result object.

### Converter Runtime Adapter

The configured converter adapter:

1. Reports `MISCONFIGURED` when required runtime config is absent.
2. Reports `AVAILABLE` only when status-only config checks pass.
3. Converts Atlas converter request file descriptors into the approved runtime input manifest or command arguments.
4. Runs within the timeout/output limits.
5. Translates output into existing converter result records.
6. Returns only safe relative `pdfPath` values and sanitized safe messages.

The adapter key remains compatible with the existing `trinity-office` adapter key. If both mock and configured adapters are registered, default selection must be explicit and deterministic so normal CI still selects the mock path.

### Parser Runtime Adapter

The configured parser adapter:

1. Reports `MISCONFIGURED` when required runtime config is absent.
2. Reports `AVAILABLE` only when status-only config checks pass.
3. Converts Atlas parser request file descriptors into approved runtime input manifest or command arguments.
4. Parses the runtime output manifest into Markdown path, assets path, confidence, safe error, and chunk descriptors.
5. Validates or normalizes runtime output into existing parser result records.
6. Returns source chunks as review-required unless the runtime has deterministic validation accepted by a later slice.

The adapter key remains compatible with the existing `document-normalize` adapter key. If a local PDF text parser is also registered, default selection must remain explicit and test-covered.

### Sanitizer

The sanitizer must remove or replace:

- token, password, api key patterns
- absolute POSIX and Windows paths
- URI/endpoint-like values
- hostnames
- stack frames
- captured output beyond 65536 bytes

Sanitized messages are still not trusted as user-authored content and remain diagnostic only.

## API / Interface Design

The existing API surface is reused:

- `GET /api/converter-adapters`
- `POST /api/batches/{batchId}/conversion-runs`
- `GET /api/conversion-runs/{runId}`
- `GET /api/parser-adapters`
- `POST /api/batches/{batchId}/parser-runs`
- `GET /api/parser-runs/{runId}`

No new public endpoint is required for the first implementation. If implementation needs a runtime smoke endpoint, it is out of scope for this slice and must go through a separate SDD.

## Data Design

- No new file status values.
- No new review status values.
- No raw runtime config persisted in product tables.
- Existing run/result/file/chunk records remain the evidence model.
- Optional runtime-attempt fields may be added only if they store safe status, elapsed time, timeout flag, and sanitized message.

## Workflow / Execution Design

### Configured Conversion

1. API receives conversion request with mode `configured` or configured adapter key.
2. Service validates batch and files.
3. Registry resolves adapter.
4. Adapter capability returns `AVAILABLE`; otherwise run fails safely.
5. Adapter creates runtime invocation under safe artifact root.
6. Runtime executor returns bounded output.
7. Adapter translates output into converter result records.
8. Service validates result status/path/confidence/error and persists evidence.

### Configured Parsing

1. API receives parser request with mode `configured` or configured adapter key.
2. Service validates batch and filters eligible `PDF_CONVERTED` files.
3. Registry resolves adapter.
4. Adapter capability returns `AVAILABLE`; otherwise run fails safely.
5. Adapter creates runtime invocation under safe artifact roots.
6. Runtime executor returns bounded output/manifest.
7. Adapter translates output into parser result and chunk records.
8. Service validates paths, confidence, statuses, chunk uniqueness, and persists evidence.

## Validation And Error Handling

| Case | Expected behavior |
|---|---|
| Missing runtime config | Capability `MISCONFIGURED`; requested configured run fails safely. |
| Runtime disabled | Capability `DISABLED`; requested configured run fails safely. |
| Timeout | Run fails or partially fails with safe timeout summary; unrelated file metadata unchanged. |
| Exit code failure | Per-file failure if manifest exists; otherwise adapter-level safe failure. |
| Unsafe output path | Validation error before unsafe metadata persists. |
| Oversized output | Capture first 65536 bytes, sanitize, then truncate safe message to existing service limit. |
| Raw secret/path in output | Mask before persistence/response. |

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
| `null` with successful Markdown | `MARKDOWN_GENERATED` plus missing-confidence safe evidence |

## Testing Considerations

- Unit tests for runtime executor timeout/output limits using fake executor.
- Unit tests for capability masking and missing config.
- Service tests for configured conversion/parser status mapping.
- Adapter contract tests for manifest translation.
- API contract tests proving existing endpoints stay stable.
- Seam guard tests proving runtime calls stay inside adapter/runtime implementation scope.
- Full `cd backend && mvn verify`.
- `git diff --check`.
- Focused secret/private-path scan and network/dependency scan.

## Risks / Design Tradeoffs

| Risk | Decision |
|---|---|
| Runtime contract details are not final. | Use fake manifest/command tests and keep real execution config-gated. |
| Local process may be replaced by worker. | Runtime executor boundary hides topology from product services. |
| Capability metadata needs enough diagnostics but no secrets. | Expose status-only fields, never raw values. |
| Two adapters may share existing keys if mock and configured variants coexist. | Implementation must make default selection explicit and tested. |

## Open Questions

- OQ-REAL-OFFICE-PARSER-RUNTIME-001: local process vs internal worker topology.
- OQ-REAL-OFFICE-PARSER-RUNTIME-002: exact `trinity-office` command/manifest contract.
- OQ-REAL-OFFICE-PARSER-RUNTIME-003: exact `document-normalize` command/manifest contract.
- OQ-REAL-OFFICE-PARSER-RUNTIME-004: whether optional runtime-attempt metadata is needed beyond existing run/result safe summaries.

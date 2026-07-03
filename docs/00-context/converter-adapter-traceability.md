# Traceability: Converter Adapter

## Status

Implemented on 2026-07-03. Phase 3 adapter slice. T-CA-001 through T-CA-010 are complete against `docs/03-spec/converter-adapter-spec.md` and `docs/06-tasks/converter-adapter-tasks.md` using mock-engine verification.

## Slice Contract

- **Goal:** Atlas can convert supported Office files into traceable PDF artifacts through a product-facing converter adapter while keeping `trinity-office` replaceable and isolated.
- **Scope:** converter adapter contract, capability metadata, conversion run lifecycle, result/status metadata write-back, mock-engine verification, seam guard update, API/adapter guide.
- **Exclusions:** parser/PDF-to-Markdown, OCR execution, storage adapter, vector/model adapters, Wiki publish, graph, Ask, frontend UI, production auth/RBAC, real company data, external cloud calls.
- **Phase entry:** `metadata-api` is recorded as implemented in `docs/00-context/slice-roadmap.md`, satisfying the Phase 3 entry dependency.

## Source Documents

| Source | Use |
|---|---|
| `README.md` | Product workflow and internal tool direction. |
| `PROJECT_RULES.md` | Adapter, parser-neutral, trace/review, and data safety rules. |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter standards and verification expectations. |
| `docs/00-context/sdd-profile.md` | Required SDD chain and ID rules. |
| `docs/01-requirements/requirement.md` | Product requirements REQ-PROD-015 through REQ-PROD-019 and related trace/review rules. |
| `docs/00-context/slice-roadmap.md` | Phase 3 verification and constraints row. |
| `docs/architecture.md` | Control plane, worker plane, and adapter rules. |
| `docs/batch-processing-design.md` | File status set and batch processing model. |
| `docs/markdown-standard.md` | Source trace, confidence, review status, relative path requirements. |
| `docs/03-spec/metadata-api-spec.md` | Existing metadata API behavior and adapter-neutral baseline. |
| `docs/04-architecture/metadata-api-data-model.md` | Existing file item fields and status model. |
| `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` | Existing envelope, validation, and file metadata API conventions. |

## Generated SDD Set

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

## Skill Chain Used

- `atlas-sdd-generate-all` for orchestration and bilingual SDD contract.
- `req-to-user-story` to derive `US-CA-*` stories from requirements.
- `user-story-to-spec` to derive behavior, workflows, state, validation, and acceptance.
- `spec-to-architecture` to derive architecture and data flow.
- `architecture-to-design` to derive detailed design, data model, and API/adapter guide.
- `design-to-tasks` to derive Codex-actionable tasks.
- `architecture-review` criteria applied because the slice introduces adapter boundaries and data flow.
- `review-doc-quality` gate applied at the end of this SDD pass.

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

API/adapter guide is **included** because Phase 3 requires an adapter contract per adapter slice and this slice introduces internal endpoints plus an adapter interface contract.

## Verification Plan

From `docs/00-context/slice-roadmap.md` Phase 3 row:

- Unit + integration tests against **mock engines**.
- Parser/converter/model/vector/storage go **only** through product-facing adapters.
- Never call a tool directly.
- Never hardcode one implementation.
- Adapter contract required per adapter slice.

Exact implementation verification commands are listed in `docs/06-tasks/converter-adapter-tasks.md`.

## Implementation Evidence

Implemented files cover the converter adapter persistence model, Flyway migration, adapter contract, mock `trinity-office` adapter, status-only real adapter wrapper boundary, registry/service/controller layer, DTO mapping, unit tests, integration tests, and seam guard updates.

The implementation preserves the slice constraints:

- Converter behavior is accessed only through product-facing adapter interfaces.
- Mock adapter execution is deterministic and requires no external network or local binary.
- Adapter capability responses expose status and masked configuration only.
- Conversion results write back file status, PDF artifact path, confidence, error message, and conversion run/file-result records for source trace.
- Parser/PDF-to-Markdown, OCR execution, storage adapter, vector/model adapters, Wiki publish, graph, Ask, frontend UI, production auth/RBAC, and real company data remain out of scope.

## Verification Evidence

Commands run on 2026-07-03:

- `cd backend && mvn -q test -Dtest='*Conversion*,ConverterAdapterContractTest'` — passed.
- `cd backend && mvn verify` — passed.
- `git diff --check` — passed.
- `rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\.getRuntime\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain` — no matches.
- `rg -n 'api[_-]?[k]ey\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[p]assword\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[t]oken\s*[:=]\s*['\''\"]?[^$\s{][^\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\' backend/src docs/01-requirements/converter-adapter-requirements.md docs/02-user-stories/converter-adapter-stories.md docs/03-spec/converter-adapter-spec.md docs/04-architecture/converter-adapter-architecture.md docs/04-architecture/converter-adapter-data-flow.md docs/04-architecture/converter-adapter-data-model.md docs/05-design/converter-adapter-design.md docs/05-design/contracts/converter-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/converter-adapter-tasks.md` — no matches.

## Review-Doc-Quality Gate

Result: Ready for human SDD review with minor known open questions.

Checks performed:

- English and Chinese files exist for every touched SDD artifact.
- REQ/US/T IDs match across languages.
- Requirements map to stories, spec, design, API/adapter guide, and tasks.
- Tasks are Codex-actionable and include exact commands.
- Adapter/no-network/secret-masked/source-trace constraints are repeated in spec, design, API guide, and tasks.
- API guide is included and recorded.

Known open questions remain non-blocking for mock-engine implementation but must be answered before relying on a real `trinity-office` runtime in production-like environments.

## Open Questions

- OQ-CA-001: local process wrapper vs external worker.
- OQ-CA-002: PDF pass-through copy vs reference policy.
- OQ-CA-003: exact `trinity-office` command-line contract.

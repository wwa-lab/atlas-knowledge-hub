# Traceability: Parser Adapter

## Status

Implemented on 2026-07-03 against `docs/03-spec/parser-adapter-spec.md` and `docs/06-tasks/parser-adapter-tasks.md`. The slice uses a mock `document-normalize` parser adapter boundary for verification and does not enable a real parser runtime.

## Slice Contract

| Field | Value |
|---|---|
| Goal | Atlas can parse converted PDFs into traceable Markdown, extracted assets, and source chunks through a replaceable parser adapter without product-layer coupling to `document-normalize`. |
| Slice | `parser-adapter` |
| Phase | 3 adapter |
| Scope | Parser adapter contract, capabilities, parser run behavior, Markdown/assets/source chunk output contracts, metadata write-back, mock-engine verification, API guide, tasks, backend implementation, and contract tests. |
| Exclusions | Real parser runtime topology, OCR execution, LLM enrichment, graph, Ask/RAG, publish-to-Wiki, frontend UI, storage object operations, production auth/RBAC. |
| Verification row | Unit + integration tests against mock engines. |
| Constraints row | Parser/converter/model/vector/storage only through product-facing adapters; no direct tool calls; no hardcoded single implementation; secret masked; trace/confidence/review preserved. |

## Source Documents

| Source | Use |
|---|---|
| `README.md` | Product workflow and stack direction. |
| `PROJECT_RULES.md` | SDD, adapter, security, data, phase discipline. |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter standards and verification. |
| `docs/00-context/sdd-profile.md` | Required SDD artifact chain and ID format. |
| `docs/00-context/slice-roadmap.md` | Phase 3 verification/constraints and slice backlog. |
| `docs/01-requirements/requirement.md` | Product-level parser/converter, Markdown, trace/review requirements. |
| `docs/markdown-standard.md` | Front matter and source trace requirements. |
| `docs/batch-processing-design.md` | File lifecycle and reports. |
| `docs/architecture.md` | Adapter-based architecture and worker plane. |
| `docs/03-spec/converter-adapter-spec.md` | Same-phase adapter pattern. |
| `docs/04-architecture/converter-adapter-data-model.md` | Same-phase run/result data model pattern. |
| Existing backend metadata code | Grounded current entities, enums, validators, and seam guard. |

## Gate Note

Phase 2 `metadata-api` and Phase 3 `converter-adapter` are implemented, so the parser adapter entry gate is satisfied. `docs/00-context/slice-roadmap.md` now marks `parser-adapter` as implemented with the real runtime contract deferred.

## SDD Artifacts

| Stage | English | Simplified Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/parser-adapter-requirements.md` | `docs/01-requirements/parser-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/parser-adapter-stories.md` | `docs/02-user-stories/parser-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/parser-adapter-spec.md` | `docs/03-spec/parser-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/parser-adapter-architecture.md` | `docs/04-architecture/parser-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/parser-adapter-data-flow.md` | `docs/04-architecture/parser-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/parser-adapter-data-model.md` | `docs/04-architecture/parser-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/parser-adapter-design.md` | `docs/05-design/parser-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/parser-adapter-tasks.md` | `docs/06-tasks/parser-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/parser-adapter-traceability.md` | `docs/00-context/parser-adapter-traceability.zh-CN.md` |

## API Guide Decision

API guide is included. Parser-adapter is a Phase 3 backend/API + adapter contract slice, so `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md` is required before implementation.

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-PA-001 | US-PA-001, US-PA-005 | Adapter Boundary | T-PA-003, T-PA-005, T-PA-009 |
| REQ-PA-002 | US-PA-001, US-PA-002 | Adapter Boundary, Capability Metadata | T-PA-003, T-PA-004 |
| REQ-PA-003 | US-PA-002 | Capability Metadata, API / Interface Surface | T-PA-002, T-PA-005, T-PA-008 |
| REQ-PA-004 | US-PA-001 | Parser Run | T-PA-001, T-PA-006, T-PA-008 |
| REQ-PA-005 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-006, T-PA-007 |
| REQ-PA-006 | US-PA-004 | Status Mapping And Failure Behavior | T-PA-006 |
| REQ-PA-007 | US-PA-004 | Status Mapping And Failure Behavior | T-PA-006 |
| REQ-PA-008 | US-PA-004, US-PA-005 | Status Mapping, Metadata Write-Back | T-PA-006, T-PA-009 |
| REQ-PA-009 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-007 |
| REQ-PA-010 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-007 |
| REQ-PA-011 | US-PA-005 | Metadata Write-Back And Validation | T-PA-007, T-PA-009 |
| REQ-PA-012 | US-PA-001, US-PA-005 | Constraints, Acceptance Matrix | T-PA-003, T-PA-004, T-PA-009, T-PA-010 |
| REQ-PA-013 | US-PA-004 | Parser Run, Reporting | T-PA-001, T-PA-002, T-PA-006, T-PA-008, T-PA-010 |
| REQ-PA-014 | US-PA-003, US-PA-005 | Metadata Write-Back And Validation | T-PA-001, T-PA-006, T-PA-007, T-PA-010 |

## Grounding Evidence

| Claim | Evidence |
|---|---|
| Existing file items support PDF/Markdown/assets path metadata. | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` |
| Existing source chunks support page/section/confidence/review status. | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` |
| Existing statuses include parser-relevant values. | `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` |
| Existing path validator rejects unsafe paths. | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` |
| Existing converter API pattern provides capability/run/report precedent. | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:30` |
| Existing seam guard already scans parser engine names and outbound clients. | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` |

## Implementation Evidence

| Task range | Evidence |
|---|---|
| T-PA-001 → T-PA-002 | `ParserRun`, `ParserFileResult`, Flyway V4 migration, parser DTOs, and schema contract assertions. |
| T-PA-003 → T-PA-004 | `ParserAdapter`, `ParserCapability`, `ParserRequest`, `ParserResult`, `MockDocumentNormalizeParserAdapter`, `DocumentNormalizeParserAdapter`. |
| T-PA-005 → T-PA-008 | `ParserController`, `ParserService`, `ParserAdapterRegistry`, `ParserSummaryCalculator`, `ParserMapper`, parser API contract tests. |
| T-PA-009 → T-PA-010 | `AdapterSeamGuardTest`, secret/path sanitization tests, full `mvn verify` evidence. |

Verification run:

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n -e "api[_-]?[k]ey\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[p]assword\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[t]oken\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[A]KIA" -e "BEGIN .*PRIVATE [K]EY" -e "/[U]sers/" -e "[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

Result: passed on 2026-07-03.

## Generated Skill Chain

- `atlas-sdd-generate-all`
- `req-to-user-story`
- `user-story-to-spec`
- `spec-to-architecture`
- `architecture-to-design`
- `design-to-tasks`
- `architecture-review` gate considered for adapter boundary quality
- `review-doc-quality` gate applied as final SDD quality review

## Open Questions

| ID | Question | Owner | Blocks Implementation? |
|---|---|---|---|
| OQ-PA-001 | Real `document-normalize` process vs worker topology. | Architecture / platform | No; mock contract can proceed. |
| OQ-PA-002 | Future ownership of draft `wiki_page` rows. | Product / architecture | No; parser defaults to no `wiki_page` creation. |
| OQ-PA-003 | Future change to low-confidence threshold `< 0.800`, if product wants a different cutoff. | Product | No; implemented exactly as specified. |

## Verification Plan

Documentation-pass checks:

```bash
git diff --check
for f in docs/01-requirements/parser-adapter-requirements.md docs/01-requirements/parser-adapter-requirements.zh-CN.md docs/02-user-stories/parser-adapter-stories.md docs/02-user-stories/parser-adapter-stories.zh-CN.md docs/03-spec/parser-adapter-spec.md docs/03-spec/parser-adapter-spec.zh-CN.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-architecture.zh-CN.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-flow.zh-CN.md docs/04-architecture/parser-adapter-data-model.md docs/04-architecture/parser-adapter-data-model.zh-CN.md docs/05-design/parser-adapter-design.md docs/05-design/parser-adapter-design.zh-CN.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/parser-adapter-tasks.md docs/06-tasks/parser-adapter-tasks.zh-CN.md docs/00-context/parser-adapter-traceability.md docs/00-context/parser-adapter-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md docs/00-context/parser-adapter-traceability.md
```

Implementation-pass checks are listed in `docs/06-tasks/parser-adapter-tasks.md`.

## Recommended Codex Handoff

```text
Implement the parser-adapter slice strictly against docs/03-spec/parser-adapter-spec.md and docs/06-tasks/parser-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

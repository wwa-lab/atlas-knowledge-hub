# Traceability: Real Office Parser Runtime

## Status

Accepted and implemented. User acceptance was recorded in chat on 2026-07-05 before product code changes.

## Slice Contract

- **Slice:** `real-office-parser-runtime`
- **Wave:** Wave 2 / Runtime Integration
- **Goal:** Enable controlled internal `trinity-office` and `document-normalize` runtime execution behind existing Atlas converter/parser adapter boundaries.
- **Maturity target:** Verified runtime integration slice; not production readiness.

## Source Documents

| Source | Status |
|---|---|
| Goal objective attachment | Read. |
| `README.md` | Read. |
| `PROJECT_RULES.md` | Read. |
| `DEVELOPMENT_STANDARDS.md` | Read. |
| `AGENTS.md` | Read. |
| `docs/00-context/sdd-profile.md` | Read. |
| `docs/01-requirements/requirement.md` | Read. |
| `ROADMAP.md` / `.zh-CN.md` | Read and updated. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | Read and updated. |
| Converter/parser adapter SDD docs | Read. |
| Existing converter/parser adapter code anchors | Verified. |
| User acceptance | Accepted in chat on 2026-07-05 before product code changes. |

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

## Grounded Implementation Anchors

| Area | Verified anchor | Finding |
|---|---|---|
| Converter interface | `backend/src/main/java/com/atlas/metadata/adapter/ConverterAdapter.java:4` | Product-facing conversion adapter contract exists. |
| Parser interface | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4` | Product-facing parser adapter contract exists. |
| Real converter wrapper | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java` | Wrapper now runs configured runtime mode behind `ConverterAdapter`; disabled/misconfigured states remain safe. |
| Real parser wrapper | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java` | Wrapper now runs configured runtime mode behind `ParserAdapter`; disabled/misconfigured states remain safe. |
| Conversion service | `backend/src/main/java/com/atlas/metadata/service/ConversionService.java:138` | Service calls only adapter contract. |
| Parser service | `backend/src/main/java/com/atlas/metadata/service/ParserService.java:157` | Service calls only adapter contract. |
| Path validation | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` | Safe relative path rule exists. |
| Seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Static guard already protects adapter boundaries. |

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

## SDD Artifact Set

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

## SDD Quality Review Result

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Context files and existing adapter code anchors were read. |
| Skill chain evidence | Passed | Required SDD skills and grounding rules were read. |
| Expected artifacts exist | Passed | Focused file-existence check found all 20 bilingual SDD files. |
| Bilingual ID parity | Passed | Focused `rg` check found matching English and Chinese REQ, US, and T IDs. |
| Deferred-decision scan | Passed | Focused scan found no `TBD`, `TODO`, `FIXME`, or coding-time decision placeholders. |
| Architecture/API review | Passed with open questions | Adapter boundaries, API reuse, runtime topology open questions, and safety constraints are explicit. |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice real-office-parser-runtime` passed; warnings noted that several companion docs do not embed additional IDs and no external report was provided. |
| Diff hygiene | Passed | `git diff --check` passed. |
| Secret/private-path scan | Passed | Focused scan found no raw credentials or private local paths in the new slice docs. |
| Network/dependency scan | Passed with expected hits | Focused scan only matched negative constraints against external cloud calls and a rejected URI example. |
| Product-code gate | Passed | Product code changed only after user acceptance. |

## Implementation Evidence

### Code Changed

- `backend/src/main/java/com/atlas/metadata/adapter/runtime/`
- `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java`
- `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java`
- `backend/src/main/java/com/atlas/metadata/service/ConverterAdapterRegistry.java`
- `backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java`
- `backend/src/main/java/com/atlas/metadata/service/ConversionService.java`
- `backend/src/main/java/com/atlas/metadata/service/ParserService.java`

### Tests Changed

- `backend/src/test/java/com/atlas/metadata/adapter/RealOfficeRuntimeAdapterTest.java`
- `backend/src/test/java/com/atlas/metadata/adapter/runtime/RuntimeOutputSanitizerTest.java`
- `backend/src/test/java/com/atlas/metadata/service/ConverterAdapterRegistryTest.java`
- `backend/src/test/java/com/atlas/metadata/service/ParserAdapterRegistryTest.java`
- `backend/src/test/java/com/atlas/metadata/integration/ConversionApiContractIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/ParserApiContractIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java`
- `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java`

### Verification

| Check | Result | Evidence |
|---|---|---|
| Focused runtime/registry/seam tests | Passed | `mvn -Dtest=RuntimeOutputSanitizerTest,RealOfficeRuntimeAdapterTest,ConverterAdapterRegistryTest,ParserAdapterRegistryTest,ConverterAdapterContractTest,ParserAdapterContractTest,AdapterSeamGuardTest test` passed: 23 tests. |
| Optional runtime smoke default behavior | Passed with skips | `mvn -Dtest=ConfiguredRuntimeSmokeIT test` passed with 2 skipped tests when runtime smoke env vars were absent. |
| Full backend verification | Passed | `mvn verify` passed: 120 unit tests and 48 integration tests; 2 optional smoke tests skipped by design. |
| Diff hygiene | Passed | `git diff --check` passed. |
| Secret/private-path scan | Passed with expected synthetic hits | Focused scan only found fake secret/path strings in sanitizer and safe-failure tests. |
| Network/dependency scan | Passed with expected hits | Focused scan found rejected URI fixtures, seam-guard strings, and allowed `ProcessBuilder` usage in adapter/runtime scope only. |
| Frontend checks | Not run | No frontend files were changed. |

## Acceptance Record

The user accepted the SDD in chat on 2026-07-05. Implementation then proceeded against the accepted spec, design, API guide, and tasks.

## Residual Risks

- The runtime stdout JSON contract is implemented as an adapter-internal contract and may later need alignment with final `trinity-office` and `document-normalize` CLIs.
- Local process execution may later be replaced by worker topology.
- Optional runtime smoke tests self-skip by default and require approved local runtime env vars before executing real binaries.
- This slice does not imply production readiness.

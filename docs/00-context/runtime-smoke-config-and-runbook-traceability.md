# Traceability: Runtime Smoke Config And Runbook

## Status

Accepted and implemented on 2026-07-05. Runtime smoke readiness only; not production operations readiness.

## Slice Contract

- **Slice:** `runtime-smoke-config-and-runbook`
- **Wave:** Wave 3 / Trust And Governance readiness for runtime operations
- **Goal:** Make approved local real-runtime smoke checks safe, repeatable, and explainable without changing default mock-safe CI behavior.
- **Maturity target:** Runtime smoke readiness preparation; not production operations readiness.

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
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | Read. |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | Read. |
| `ROADMAP.md` / `.zh-CN.md` | Read and updated. |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | Read and updated. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `real-office-parser-runtime` SDD docs and traceability | Read. |
| Existing runtime smoke and adapter code anchors | Verified. |
| User acceptance | Accepted in chat on 2026-07-05 before runbook implementation. |

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
| Review completeness reference | `.agents/skills/review-doc-quality/references/completeness-criteria.md` |
| Review phase-scope reference | `.agents/skills/review-doc-quality/references/phase-scope-guide.md` |

SDD skill chain used: yes.

## Grounded Existing Code Anchors

| Area | Verified anchor | Finding |
|---|---|---|
| Smoke enable env var | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:18` | Optional smoke checks use `ATLAS_RUNTIME_SMOKE_ENABLED`. |
| Trinity smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:24` | Trinity smoke reads command and smoke args env vars. |
| Document Normalize smoke env vars | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:32` | Document Normalize smoke reads command and smoke args env vars. |
| Smoke args default | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:58` | Missing args default to `--version`. |
| Smoke diagnostic safety | `backend/src/test/java/com/atlas/metadata/integration/ConfiguredRuntimeSmokeIT.java:52` | Smoke diagnostics are sanitized before assertion. |
| Runtime adapter defaults | `backend/src/main/java/com/atlas/metadata/adapter/runtime/RuntimeAdapterConfiguration.java:9` | Adapter runtime timeout/capture defaults are documented separately from smoke-only env vars. |
| Spring Trinity properties | `backend/src/main/java/com/atlas/metadata/adapter/TrinityOfficeConverterAdapter.java:45` | Configured adapter runtime uses Spring properties. |
| Spring Document Normalize properties | `backend/src/main/java/com/atlas/metadata/adapter/DocumentNormalizeParserAdapter.java:46` | Configured adapter runtime uses Spring properties. |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:43` | Guard exists for adapter/runtime boundary verification. |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-006 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-007 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-002, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-010 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-011 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-003, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-004 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-008 |
| REQ-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-012 | US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, US-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-005 | T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-001, T-RUNTIME-SMOKE-CONFIG-AND-RUNBOOK-009 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/runtime-smoke-config-and-runbook-requirements.md` | `docs/01-requirements/runtime-smoke-config-and-runbook-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/runtime-smoke-config-and-runbook-stories.md` | `docs/02-user-stories/runtime-smoke-config-and-runbook-stories.zh-CN.md` |
| Spec | `docs/03-spec/runtime-smoke-config-and-runbook-spec.md` | `docs/03-spec/runtime-smoke-config-and-runbook-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/runtime-smoke-config-and-runbook-data-flow.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/runtime-smoke-config-and-runbook-data-model.md` | `docs/04-architecture/runtime-smoke-config-and-runbook-data-model.zh-CN.md` |
| Design | `docs/05-design/runtime-smoke-config-and-runbook-design.md` | `docs/05-design/runtime-smoke-config-and-runbook-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/runtime-smoke-config-and-runbook-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/runtime-smoke-config-and-runbook-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md` | `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.zh-CN.md` |
| Traceability | `docs/00-context/runtime-smoke-config-and-runbook-traceability.md` | `docs/00-context/runtime-smoke-config-and-runbook-traceability.zh-CN.md` |

## SDD Quality Review Result

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Context and code anchors were read before drafting. |
| Skill chain evidence | Passed | Required project-local SDD skills and references were read. |
| Expected artifacts exist | Passed | Focused file-existence check found all 20 bilingual SDD files. |
| Bilingual ID parity | Passed | Focused REQ/US/T parity check had no differences. |
| Deferred-decision scan | Passed | Focused scan found no deferred-decision or placeholder markers. |
| Architecture/API review | Draft pass | No public API or persistence introduced; adapter boundaries explicit. |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook` passed; warnings noted that architecture/design companion docs do not embed extra IDs and no external report was provided. |
| Diff hygiene | Passed | `git diff --check` passed. |
| Secret/private-path scan | Passed | Focused scan found no raw credentials, private local paths, or real data in the new slice docs. |
| Network/dependency scan | Passed | Focused scan found no external URLs, install commands, or network-client references in the new slice docs. |
| Product-code gate | Passed for SDD pass | No product code changed in this SDD-only pass. |

## Verification Performed For SDD Draft

- `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook`
- focused file existence check for all 20 bilingual artifacts
- focused REQ/US/T ID parity check
- focused deferred-decision placeholder scan
- `git diff --check`
- focused secret/private-path scan over changed docs
- focused network/dependency scan over changed docs

## Residual Risks

- This slice documents command-level smoke readiness only; sample document conversion/parser smoke remains a future accepted scope.
- Approved local binaries may be unavailable during implementation, so pass evidence may remain skipped with reason.

## Implementation Evidence

### Docs Changed

- `docs/00-context/runbooks/runtime-smoke-config-and-runbook.md`
- `docs/00-context/runbooks/runtime-smoke-config-and-runbook.zh-CN.md`
- `docs/00-context/runtime-smoke-config-and-runbook-traceability.md`
- `docs/00-context/runtime-smoke-config-and-runbook-traceability.zh-CN.md`
- `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.md`
- `docs/06-tasks/runtime-smoke-config-and-runbook-tasks.zh-CN.md`
- `ROADMAP.md`
- `ROADMAP.zh-CN.md`
- `docs/00-context/slice-roadmap.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- Status lines in the bilingual SDD artifact set were updated from Draft to accepted/implemented.

### Code Changed

None. No backend, frontend, migration, or test source files were modified for this implementation pass.

### Verification

| Check | Result | Evidence |
|---|---|---|
| Focused default smoke self-skip | Passed | `env -u ... mvn -Dtest=ConfiguredRuntimeSmokeIT test`: 2 tests run, 2 skipped. |
| Adapter seam guard | Passed | `mvn -Dtest=AdapterSeamGuardTest test`: 3 tests passed. |
| Full backend verification | Passed | `mvn verify`: 120 unit tests passed; 48 integration tests passed; 2 optional runtime smoke tests skipped by design. |
| Project SDD gate | Passed with warnings | `npm run agent:check-sdd -- --slice runtime-smoke-config-and-runbook`; warnings are the existing companion-doc ID and missing external report notices. |
| Diff hygiene | Passed | `git diff --check` passed. |
| File existence | Passed | 20 bilingual SDD files and 2 bilingual runbook files exist. |
| Bilingual ID parity | Passed | Focused REQ/US/T parity check had no differences. |
| Deferred-decision marker scan | Passed | Focused scan found no deferred-decision or placeholder markers. |
| Secret/private-path scan | Passed | Focused scan found no raw credentials, private local paths, or real data in changed docs. |
| Network/dependency scan | Passed | Focused scan found no external URLs, install commands, or network-client references in changed docs. |
| Approved local runtime smoke pass | Skipped with reason | No approved local runtime command values were provided for this run. |

## Final Residual Risks

- The runbook verifies command-level runtime health only; it does not process a sample document.
- Approved local runtime pass evidence remains unavailable until an operator provides approved local command values outside the repository.
- Production monitoring, deployment rollback, audit, RBAC, and secret-manager flows remain out of scope for this slice.

## Next Gate

Next recommended slice should stay in the accepted queue and should not treat this runbook as production operations readiness.

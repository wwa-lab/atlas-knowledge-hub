# Traceability: manual-url-knowledge-ingest

Status: Implemented and verified under the preauthorized SDD boundary.
Last updated: 2026-07-07
Maturity: Metadata-only manual URL ingest foundation; not connector-sync-v0 or production crawl readiness.

## Source Documents

- User goal: autonomous single-slice full-delivery prompt for `manual-url-knowledge-ingest`.
- Execution manifest: `docs/00-context/execution-manifests/manual-url-knowledge-ingest-20260707.yaml`.
- SDD profile: `docs/00-context/sdd-profile.md`.
- Workflow docs: `docs/00-context/agent-goal-loop-workflow.md`, `docs/00-context/agent-goal-loop-workflow.zh-CN.md`, `docs/00-context/agent-goal-loop-quickstart.md`, `docs/00-context/agent-goal-loop-quickstart.zh-CN.md`.
- Existing anchors: batch/file/source chunk metadata, Wiki ingest, review/publish, safe errors, rate limiting, frontend Knowledge Space and Processing Center surfaces.

## Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/manual-url-knowledge-ingest-requirements.md` | `docs/01-requirements/manual-url-knowledge-ingest-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/manual-url-knowledge-ingest-stories.md` | `docs/02-user-stories/manual-url-knowledge-ingest-stories.zh-CN.md` |
| Specification | `docs/03-spec/manual-url-knowledge-ingest-spec.md` | `docs/03-spec/manual-url-knowledge-ingest-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/manual-url-knowledge-ingest-architecture.md` | `docs/04-architecture/manual-url-knowledge-ingest-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/manual-url-knowledge-ingest-data-flow.md` | `docs/04-architecture/manual-url-knowledge-ingest-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/manual-url-knowledge-ingest-data-model.md` | `docs/04-architecture/manual-url-knowledge-ingest-data-model.zh-CN.md` |
| Design | `docs/05-design/manual-url-knowledge-ingest-design.md` | `docs/05-design/manual-url-knowledge-ingest-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/manual-url-knowledge-ingest-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/manual-url-knowledge-ingest-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/manual-url-knowledge-ingest-tasks.md` | `docs/06-tasks/manual-url-knowledge-ingest-tasks.zh-CN.md` |

## Requirement To Task Map

| Requirement | Stories | Acceptance | Tasks |
|---|---|---|---|
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-001` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001`, `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` | `T-MANUAL-URL-KNOWLEDGE-INGEST-004`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-002` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` | `T-MANUAL-URL-KNOWLEDGE-INGEST-002`, `T-MANUAL-URL-KNOWLEDGE-INGEST-004` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-003` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003`, `T-MANUAL-URL-KNOWLEDGE-INGEST-005` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-004` | `US-MANUAL-URL-KNOWLEDGE-INGEST-001` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-005` | `US-MANUAL-URL-KNOWLEDGE-INGEST-002` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `T-MANUAL-URL-KNOWLEDGE-INGEST-002`, `T-MANUAL-URL-KNOWLEDGE-INGEST-003` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-006` | `US-MANUAL-URL-KNOWLEDGE-INGEST-002` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`, `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003`, `T-MANUAL-URL-KNOWLEDGE-INGEST-006` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-007` | `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` | `T-MANUAL-URL-KNOWLEDGE-INGEST-005`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-008` | `US-MANUAL-URL-KNOWLEDGE-INGEST-004` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-009` | `US-MANUAL-URL-KNOWLEDGE-INGEST-003` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-005` | `T-MANUAL-URL-KNOWLEDGE-INGEST-006`, `T-MANUAL-URL-KNOWLEDGE-INGEST-007` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-010` | `US-MANUAL-URL-KNOWLEDGE-INGEST-004` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-006` | `T-MANUAL-URL-KNOWLEDGE-INGEST-003` through `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-011` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-012` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |
| `REQ-MANUAL-URL-KNOWLEDGE-INGEST-013` | All | Exclusion | `T-MANUAL-URL-KNOWLEDGE-INGEST-001`, `T-MANUAL-URL-KNOWLEDGE-INGEST-008` |

## SDD Skill Chain Evidence

SDD skill chain used: yes

Skill files read:

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`
- `.agents/skills/architecture-review/SKILL.md`
- `.agents/skills/_shared/grounding-rules.md`
- `.agents/skills/review-doc-quality/references/completeness-criteria.md`
- `.agents/skills/review-doc-quality/references/phase-scope-guide.md`

Review-doc-quality result: Ready for implementation under the explicit preauthorization boundary; no critical or major findings.

## Implementation Evidence

- Backend adds metadata-only manual URL source registration APIs for create, list, and get.
- Backend stores safe URL metadata in `manual_url_source`, links each URL source to existing batch/file/source chunk metadata, and keeps file and review state as `REVIEW_REQUIRED`.
- Backend validation rejects non-HTTPS URLs, userinfo, query strings, fragments, local/private/internal hosts, and secret-like title/description/path values without echoing raw unsafe inputs.
- Frontend adds an API-backed manual URL ingest form and status/source-trace display in the Knowledge Space metadata surface, plus a Processing Center review-required count.
- Frontend and E2E mocks use `https://example.com/...` sample-safe fixtures only.
- No real URL fetch, crawl, connector sync, browser automation, external provider call, auth/RBAC/audit/secret/rate-limit semantic change, or production compliance behavior is introduced.

## Completed Task IDs

`T-MANUAL-URL-KNOWLEDGE-INGEST-001` through `T-MANUAL-URL-KNOWLEDGE-INGEST-008`.

## Verification Evidence

- `npm run agent:check-sdd -- --slice manual-url-knowledge-ingest --require-api-guide --report docs/00-context/manual-url-knowledge-ingest-sdd-completion-report.md`
- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `cd frontend && npx playwright test tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`
- `npm run agent:closeout`
- `git diff --check`
- Focused secret/private-path/real-data scan.

## Residual Risks

- Real URL fetch, crawl, connector sync, compliance policy, and connector secret management are deferred to future slices.
- URL-derived metadata remains review-required and is not approved knowledge in this slice.

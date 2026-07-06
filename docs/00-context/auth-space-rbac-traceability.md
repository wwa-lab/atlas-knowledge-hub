# Traceability: auth-space-rbac

Status: Complete for the accepted auth-space-rbac slice
Last updated: 2026-07-07
Maturity: Closeout verified for controlled internal beta auth/RBAC scope; not production SSO/OIDC readiness.

## Source Documents

- Goal objective: Codex goal objective file for SDD-first slice execution.
- Product requirements: `docs/01-requirements/requirement.md`.
- Wave plan: `docs/00-context/wiki-foundation-goal-plan.zh-CN.md`, Slice 3.1 `auth-space-rbac`.
- Roadmaps: `ROADMAP.md`, `ROADMAP.zh-CN.md`, `docs/00-context/slice-roadmap.md`, `docs/00-context/slice-roadmap.zh-CN.md`.
- Existing implementation anchors:
  - `backend/src/main/java/com/atlas/metadata/controller/GraphController.java:35`
  - `backend/src/main/java/com/atlas/metadata/controller/SpaceController.java:35`
  - `backend/src/main/java/com/atlas/metadata/controller/ReviewPublishController.java:37`
  - `backend/src/main/java/com/atlas/metadata/controller/AskController.java:29`
  - `backend/src/main/resources/db/migration/V1__init_schema.sql:3`
  - `frontend/src/api.ts:37`

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/auth-space-rbac-requirements.md` | `docs/01-requirements/auth-space-rbac-requirements.zh-CN.md` |
| User Stories | `docs/02-user-stories/auth-space-rbac-stories.md` | `docs/02-user-stories/auth-space-rbac-stories.zh-CN.md` |
| Specification | `docs/03-spec/auth-space-rbac-spec.md` | `docs/03-spec/auth-space-rbac-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/auth-space-rbac-architecture.md` | `docs/04-architecture/auth-space-rbac-architecture.zh-CN.md` |
| Data Flow | `docs/04-architecture/auth-space-rbac-data-flow.md` | `docs/04-architecture/auth-space-rbac-data-flow.zh-CN.md` |
| Data Model | `docs/04-architecture/auth-space-rbac-data-model.md` | `docs/04-architecture/auth-space-rbac-data-model.zh-CN.md` |
| Design | `docs/05-design/auth-space-rbac-design.md` | `docs/05-design/auth-space-rbac-design.zh-CN.md` |
| API Guide | `docs/05-design/contracts/auth-space-rbac-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/auth-space-rbac-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/auth-space-rbac-tasks.md` | `docs/06-tasks/auth-space-rbac-tasks.zh-CN.md` |

## Requirement To Task Map

| Requirement | Story | Spec | Tasks |
|---|---|---|---|
| REQ-AUTH-SPACE-RBAC-001 | US-AUTH-SPACE-RBAC-001 | FR-AUTH-SPACE-RBAC-001 | T-AUTH-SPACE-RBAC-004, T-AUTH-SPACE-RBAC-005 |
| REQ-AUTH-SPACE-RBAC-002 | US-AUTH-SPACE-RBAC-001 | FR-AUTH-SPACE-RBAC-002 | T-AUTH-SPACE-RBAC-004 |
| REQ-AUTH-SPACE-RBAC-003 | US-AUTH-SPACE-RBAC-001 | FR-AUTH-SPACE-RBAC-003 | T-AUTH-SPACE-RBAC-004 |
| REQ-AUTH-SPACE-RBAC-004 | US-AUTH-SPACE-RBAC-001, US-AUTH-SPACE-RBAC-005 | FR-AUTH-SPACE-RBAC-004 | T-AUTH-SPACE-RBAC-001, T-AUTH-SPACE-RBAC-006, T-AUTH-SPACE-RBAC-008 |
| REQ-AUTH-SPACE-RBAC-005 | US-AUTH-SPACE-RBAC-002, US-AUTH-SPACE-RBAC-003 | FR-AUTH-SPACE-RBAC-017 | T-AUTH-SPACE-RBAC-002, T-AUTH-SPACE-RBAC-003 |
| REQ-AUTH-SPACE-RBAC-006 | US-AUTH-SPACE-RBAC-002, US-AUTH-SPACE-RBAC-006 | FR-AUTH-SPACE-RBAC-005 | T-AUTH-SPACE-RBAC-001, T-AUTH-SPACE-RBAC-005 |
| REQ-AUTH-SPACE-RBAC-007 | US-AUTH-SPACE-RBAC-002, US-AUTH-SPACE-RBAC-006 | FR-AUTH-SPACE-RBAC-012, FR-AUTH-SPACE-RBAC-016 | T-AUTH-SPACE-RBAC-005, T-AUTH-SPACE-RBAC-007 |
| REQ-AUTH-SPACE-RBAC-008 | US-AUTH-SPACE-RBAC-005 | FR-AUTH-SPACE-RBAC-021, FR-AUTH-SPACE-RBAC-022, FR-AUTH-SPACE-RBAC-023 | T-AUTH-SPACE-RBAC-008 |
| REQ-AUTH-SPACE-RBAC-009 | US-AUTH-SPACE-RBAC-004 | FR-AUTH-SPACE-RBAC-013 | T-AUTH-SPACE-RBAC-004, T-AUTH-SPACE-RBAC-005 |
| REQ-AUTH-SPACE-RBAC-010 | US-AUTH-SPACE-RBAC-004 | FR-AUTH-SPACE-RBAC-014 | T-AUTH-SPACE-RBAC-005, T-AUTH-SPACE-RBAC-007 |
| REQ-AUTH-SPACE-RBAC-011 | US-AUTH-SPACE-RBAC-004 | FR-AUTH-SPACE-RBAC-015 | T-AUTH-SPACE-RBAC-005, T-AUTH-SPACE-RBAC-007 |
| REQ-AUTH-SPACE-RBAC-012 | US-AUTH-SPACE-RBAC-003 | FR-AUTH-SPACE-RBAC-018, FR-AUTH-SPACE-RBAC-019 | T-AUTH-SPACE-RBAC-003, T-AUTH-SPACE-RBAC-006 |
| REQ-AUTH-SPACE-RBAC-013 | US-AUTH-SPACE-RBAC-003 | NFR Auditability | T-AUTH-SPACE-RBAC-001, T-AUTH-SPACE-RBAC-012 |
| REQ-AUTH-SPACE-RBAC-014 | US-AUTH-SPACE-RBAC-003, US-AUTH-SPACE-RBAC-006 | FR-AUTH-SPACE-RBAC-020 | T-AUTH-SPACE-RBAC-002, T-AUTH-SPACE-RBAC-011 |

## Acceptance Criteria Map

| Acceptance | Verification target |
|---|---|
| AC-AUTH-SPACE-RBAC-001 | Integration test for protected endpoint without auth -> `401`. |
| AC-AUTH-SPACE-RBAC-002 | Integration tests for `VIEWER` write denials -> `403`. |
| AC-AUTH-SPACE-RBAC-003 | Cross-space resource tests with no protected metadata disclosure. |
| AC-AUTH-SPACE-RBAC-004 | Role-specific tests for ordinary user, `KNOWLEDGE_MANAGER`, `SPACE_OWNER`. |
| AC-AUTH-SPACE-RBAC-005 | Frontend tests using `/api/auth/me` capabilities. |
| AC-AUTH-SPACE-RBAC-006 | Secret/private-path and network/dependency scans. |

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

## Review-Doc-Quality Result

Draft self-review result before verification:

- Quality rating: Good for SDD draft.
- Readiness verdict: Ready for human review.
- Critical findings: None identified.
- Major findings: None identified.
- Minor findings: Production SSO/OIDC mapping remains intentionally deferred and is tracked as OQ-AUTH-SPACE-RBAC-002.

## Implementation Evidence

Completed in the implementation pass:

- Persistence: `backend/src/main/resources/db/migration/V13__auth_space_rbac.sql` adds `atlas_user`, `space_membership`, constraints, indexes, and sample-safe mock users/memberships.
- Backend auth boundary: `CurrentUserService`, `AuthorizationService`, `AuthorizationPathPolicy`, `AtlasAuthInterceptor`, and `AtlasAuthWebConfig` enforce current-user and role/capability checks before controller execution.
- APIs: `/api/auth/me` and `/api/spaces/{spaceId}/members` were added with current-user, membership list/create/update/remove, and last active owner protection. Membership update/remove now use the API guide's membership-id member resource path.
- Existing domains: space, batch, file/chunk, review/publish, Wiki, graph, Ask, vector, conversion, parser, storage, model/settings, ingestion, and downstream refresh paths are guarded by centralized path policy.
- Graph API: removed the earlier graph-only `X-Atlas-Role` controller guard so graph authorization goes through the shared RBAC boundary.
- Frontend: all API calls include `X-Atlas-User`, `/api/auth/me` is loaded on startup, representative write controls are disabled from backend capabilities, and role-specific Playwright coverage exercises viewer, knowledge manager, and space owner paths.

## Verification Evidence

Completed for this implementation pass:

- `npm run agent:check-sdd -- --slice auth-space-rbac --require-api-guide`: PASS. The first run emitted only the expected warning that no completion report path was provided.
- File existence check: PASS, all 20 expected bilingual SDD files exist.
- Bilingual ID parity: PASS through the SDD gate for requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- Deferred-decision scan on `auth-space-rbac` SDD files: PASS, no deferred-decision keyword patterns found.
- `cd backend && mvn verify`: PASS, 191 tests reported across Surefire/Failsafe with 2 optional configured-runtime smoke tests skipped.
- `cd backend && mvn test -Dtest=AuthorizationServiceTest,AuthSpaceRbacApiContractIT`: PASS, 10 targeted RBAC tests.
- `npm --prefix frontend run typecheck`: PASS.
- `npm --prefix frontend test`: PASS, 3 test files and 20 tests.
- `npm --prefix frontend run build`: PASS, including lint, typecheck, and Vite production build.
- `npm --prefix frontend run e2e`: PASS, 16 Playwright tests including `frontend/tests/e2e/auth-space-rbac.spec.ts`.
- `git diff --check`: PASS.
- Focused secret/private-path scan on new auth/RBAC files: PASS with notes. Matches were `ask-runs` identifier text and the existing frontend model `apiKey` request field, not raw secrets.
- Focused network/dependency scan: PASS with notes. Matches were existing configured model/endpoint UI and adapter references; this slice did not add new package dependencies or external cloud calls.

## Residual Risks

- `/api/spaces` list remains authenticated-only in this prototype pass; individual space details and derived resources are space-guarded.
- Production SSO/OIDC, full audit retention, secret manager, and rate limiting remain separate Wave 3 slices.
- The current worktree also contains `audit-log-foundation` changes, so review packaging should keep auth/RBAC closeout changes distinct from the audit slice when preparing commits or PRs.

## Next Gate

Acceptance review should focus on role semantics, `/api/spaces` list visibility, and keeping production SSO/OIDC, secret manager, and rate limiting in their future slices.

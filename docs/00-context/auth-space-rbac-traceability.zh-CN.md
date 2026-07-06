# 溯源：auth-space-rbac

状态：已按已接受的 auth-space-rbac 切片完成
最后更新：2026-07-07
成熟度：已完成受控内部 beta auth/RBAC 范围的 closeout verification；不代表 production SSO/OIDC readiness。

## Source Documents

- Goal objective：Codex goal objective file for SDD-first slice execution。
- 产品需求：`docs/01-requirements/requirement.md`。
- Wave plan：`docs/00-context/wiki-foundation-goal-plan.zh-CN.md`，Slice 3.1 `auth-space-rbac`。
- Roadmaps：`ROADMAP.md`、`ROADMAP.zh-CN.md`、`docs/00-context/slice-roadmap.md`、`docs/00-context/slice-roadmap.zh-CN.md`。
- Existing implementation anchors：
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
| AC-AUTH-SPACE-RBAC-001 | Protected endpoint without auth -> `401` 的 integration test。 |
| AC-AUTH-SPACE-RBAC-002 | `VIEWER` write denials -> `403` 的 integration tests。 |
| AC-AUTH-SPACE-RBAC-003 | Cross-space resource tests，确认不泄露 protected metadata。 |
| AC-AUTH-SPACE-RBAC-004 | ordinary user、`KNOWLEDGE_MANAGER`、`SPACE_OWNER` 的 role-specific tests。 |
| AC-AUTH-SPACE-RBAC-005 | 使用 `/api/auth/me` capabilities 的 frontend tests。 |
| AC-AUTH-SPACE-RBAC-006 | Secret/private-path 与 network/dependency scans。 |

## SDD Skill Chain Evidence

SDD skill chain used: yes

Skill files read：

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

验证前 draft self-review result：

- Quality rating：Good for SDD draft。
- Readiness verdict：Ready for human review。
- Critical findings：None identified。
- Major findings：None identified。
- Minor findings：Production SSO/OIDC mapping 有意延后，并通过 OQ-AUTH-SPACE-RBAC-002 跟踪。

## Implementation Evidence

本轮实现已完成：

- Persistence：`backend/src/main/resources/db/migration/V13__auth_space_rbac.sql` 增加 `atlas_user`、`space_membership`、constraints、indexes 与 sample-safe mock users/memberships。
- Backend auth boundary：`CurrentUserService`、`AuthorizationService`、`AuthorizationPathPolicy`、`AtlasAuthInterceptor` 与 `AtlasAuthWebConfig` 在 controller 前执行 current-user 与 role/capability checks。
- APIs：新增 `/api/auth/me` 与 `/api/spaces/{spaceId}/members`，覆盖 current user、member list/create/update/remove 与 last active owner protection。Membership update/remove 现在使用 API guide 中基于 membership id 的成员资源路径。
- Existing domains：space、batch、file/chunk、review/publish、Wiki、graph、Ask、vector、conversion、parser、storage、model/settings、ingestion 与 downstream refresh paths 通过 centralized path policy 受保护。
- Graph API：移除早期 graph-only `X-Atlas-Role` controller guard，统一走 shared RBAC boundary。
- Frontend：所有 API 请求携带 `X-Atlas-User`，启动时加载 `/api/auth/me`，基于 backend capabilities 禁用代表性写操作，并通过 role-specific Playwright 覆盖 viewer、knowledge manager 与 space owner 路径。

## Verification Evidence

本 implementation pass 已完成：

- `npm run agent:check-sdd -- --slice auth-space-rbac --require-api-guide`：PASS。首次运行只出现预期 warning：未提供 completion report path。
- File existence check：PASS，20 个预期双语 SDD 文件全部存在。
- Bilingual ID parity：PASS，SDD gate 已覆盖 requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks 和 traceability。
- Deferred-decision scan on `auth-space-rbac` SDD files：PASS，未发现延期决策关键词模式。
- `cd backend && mvn verify`：PASS，Surefire/Failsafe 共报告 191 tests，2 个 optional configured-runtime smoke tests skipped。
- `cd backend && mvn test -Dtest=AuthorizationServiceTest,AuthSpaceRbacApiContractIT`：PASS，10 targeted RBAC tests。
- `npm --prefix frontend run typecheck`：PASS。
- `npm --prefix frontend test`：PASS，3 test files / 20 tests。
- `npm --prefix frontend run build`：PASS，包含 lint、typecheck 与 Vite production build。
- `npm --prefix frontend run e2e`：PASS，16 个 Playwright tests，包含 `frontend/tests/e2e/auth-space-rbac.spec.ts`。
- `git diff --check`：PASS。
- Focused secret/private-path scan on new auth/RBAC files：PASS with notes。命中项为 `ask-runs` identifier text 与既有 frontend model `apiKey` request field，不是 raw secrets。
- Focused network/dependency scan：PASS with notes。命中项为既有 configured model/endpoint UI 与 adapter references；本 slice 未新增 package dependencies 或 external cloud calls。

## Residual Risks

- `/api/spaces` list 当前仍为 authenticated-only prototype 行为；individual space detail 与 derived resources 已按 space guard 保护。
- Production SSO/OIDC、full audit retention、secret manager 与 rate limiting 仍是独立 Wave 3 slices。
- 当前 worktree 同时包含 `audit-log-foundation` 变更；准备 commit 或 PR 时应保持 auth/RBAC closeout 变更与 audit 切片变更可区分。

## Next Gate

验收 review 建议重点看 role semantics、`/api/spaces` list visibility，并继续把 production SSO/OIDC、secret manager 与 rate limiting 留在未来切片。

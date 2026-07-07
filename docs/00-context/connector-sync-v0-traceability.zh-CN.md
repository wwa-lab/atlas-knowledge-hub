# 追溯：connector-sync-v0

状态：本地实现与验证已完成
最后更新：2026-07-07
Manifest：`docs/00-context/execution-manifests/connector-sync-v0-20260707.yaml`

## 来源上下文

- 用户目标：交付 Wave 5 / Connector And Operations 的 connector-sync-v0 full single-slice delivery。
- 必读仓库上下文：`AGENTS.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、goal-loop docs、SDD bootstrap docs、roadmap docs。
- 已参考相关 slices：folder upload、wiki foundation、review publish、ask-session-citations、graph-from-wiki-extraction、rate-limit-safe-errors、secret-manager-integration、parser/converter adapter docs。

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

## Requirement Mapping

| Requirement | Stories | Spec | Tasks |
|---|---|---|---|
| REQ-CONNECTOR-SYNC-V0-001 | US-CONNECTOR-SYNC-V0-001, US-CONNECTOR-SYNC-V0-002 | FR-CONNECTOR-SYNC-V0-001 | T-CONNECTOR-SYNC-V0-003 |
| REQ-CONNECTOR-SYNC-V0-002 | US-CONNECTOR-SYNC-V0-001 | FR-CONNECTOR-SYNC-V0-002 | T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-005 |
| REQ-CONNECTOR-SYNC-V0-003 | US-CONNECTOR-SYNC-V0-002 | FR-CONNECTOR-SYNC-V0-003 | T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-004, T-CONNECTOR-SYNC-V0-005 |
| REQ-CONNECTOR-SYNC-V0-004 | US-CONNECTOR-SYNC-V0-002 | FR-CONNECTOR-SYNC-V0-004 | T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-004 |
| REQ-CONNECTOR-SYNC-V0-005 | US-CONNECTOR-SYNC-V0-003 | FR-CONNECTOR-SYNC-V0-005 | T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-004 |
| REQ-CONNECTOR-SYNC-V0-006 | US-CONNECTOR-SYNC-V0-003 | FR-CONNECTOR-SYNC-V0-006 | T-CONNECTOR-SYNC-V0-002, T-CONNECTOR-SYNC-V0-004 |
| REQ-CONNECTOR-SYNC-V0-007 | US-CONNECTOR-SYNC-V0-001, US-CONNECTOR-SYNC-V0-004 | FR-CONNECTOR-SYNC-V0-007 | T-CONNECTOR-SYNC-V0-005, T-CONNECTOR-SYNC-V0-006 |
| REQ-CONNECTOR-SYNC-V0-008 | US-CONNECTOR-SYNC-V0-001, US-CONNECTOR-SYNC-V0-003 | FR-CONNECTOR-SYNC-V0-008 | T-CONNECTOR-SYNC-V0-007, T-CONNECTOR-SYNC-V0-008 |
| REQ-CONNECTOR-SYNC-V0-009 | US-CONNECTOR-SYNC-V0-002 | FR-CONNECTOR-SYNC-V0-009 | T-CONNECTOR-SYNC-V0-003 |
| REQ-CONNECTOR-SYNC-V0-010 | US-CONNECTOR-SYNC-V0-004 | FR-CONNECTOR-SYNC-V0-010 | T-CONNECTOR-SYNC-V0-006 |
| REQ-CONNECTOR-SYNC-V0-011 | US-CONNECTOR-SYNC-V0-005 | FR-CONNECTOR-SYNC-V0-011 | T-CONNECTOR-SYNC-V0-007, T-CONNECTOR-SYNC-V0-008 |
| REQ-CONNECTOR-SYNC-V0-012 | US-CONNECTOR-SYNC-V0-005 | FR-CONNECTOR-SYNC-V0-012 | T-CONNECTOR-SYNC-V0-001, T-CONNECTOR-SYNC-V0-009 |

## 验证计划

- SDD gate：`npm run agent:check-sdd -- --slice connector-sync-v0 --require-api-guide --report docs/00-context/connector-sync-v0-sdd-completion-report.md`
- Backend：`cd backend && mvn verify`
- Frontend：`cd frontend && npm run typecheck`；`cd frontend && npm run test`；`cd frontend && npm run build`
- Closeout：`npm run agent:closeout`
- Hygiene：`git diff --check`、secret/private path scan、network dependency scan。

## 状态说明

- SDD changes 保持在用户提供的 Goal / Scope / Exclusions / Acceptance 边界内。
- 预授权接受：yes。
- 实现保持 adapter-first，仅使用 `mock-local-fixture` connector。
- connector-derived artifacts 作为 review-required output candidates 持久化；不会成为 approved Wiki、Ask 或 Graph knowledge。
- 本切片不引入真实 provider、credential、external network、company data 或 production worker。
- 2026-07-07 已记录的验证证据：
  - PASS: `npm run agent:check-sdd -- --slice connector-sync-v0 --require-api-guide --report docs/00-context/connector-sync-v0-sdd-completion-report.md`
  - PASS: `cd backend && mvn -q -DskipTests compile`
  - PASS: `cd backend && mvn verify`
  - PASS: `cd frontend && npm run typecheck`
  - PASS: `cd frontend && npm run test`（3 files, 22 tests）
  - PASS: `cd frontend && npm run build`
  - PASS: `git diff --check`
  - PASS: `npm run agent:closeout`

## 剩余风险

- v0 只证明 mock/local connector mechanics。
- Review-required output handoff 在未来 review/Wiki integration slice 前只是 metadata-only。
- Production connector scheduling、retries、dead-letter、OAuth 和 provider governance 保持为未来工作。
- 当前 worktree 仍包含其他 slice 的未提交改动；connector-sync-v0 必须只按 scoped paths staging 和提交。

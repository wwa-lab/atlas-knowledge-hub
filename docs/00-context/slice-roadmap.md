# Slice Roadmap & Two-Command SDD Loop

> Current canonical repository status: `docs/00-context/repo-status-roadmap.zh-CN.md`. This file keeps the slice backlog, SDD loop, and copy-paste prompt templates; it is no longer the only status entry point.

Operator playbook for driving Atlas Knowledge Hub with agent coding tools (Claude Code, Codex, or any agent that can read this repo's skills).

Each slice is two jobs:

- **Generate SDD** — one invocation produces the complete bilingual SDD document set (requirements → tasks + traceability).
- **Implement** — one sentence executes every task in `docs/06-tasks/{slice}-tasks.md` against the spec.

**Either agent can do either job.** The templates below are agent-agnostic — the `atlas-sdd-generate-all` skill is mirrored for both Claude Code (`.claude/skills/`) and Codex (`.agents/skills/`), so whichever agent is in front of you can generate the SDD, and whichever you prefer can implement. What matters is hitting the goal and passing the gates, not which tool runs which step. A common, effective default is Claude Code for SDD authoring and Codex for bulk implementation — but that's a convention, not a rule; swap freely when it gets you there faster.

This file is the single source for the slice backlog and the copy-paste prompt templates. It is meta-documentation about the workflow; it does not redefine behavior owned by `docs/03-spec/`. See `docs/00-context/sdd-profile.md` for the doc chain and IDs, and the `atlas-sdd-generate-all` skill for the generation contract.

Chinese companion: `docs/00-context/slice-roadmap.zh-CN.md`.

---

## The Loop (per slice)

```
1. Generate SDD:   atlas-sdd-generate-all       →  full bilingual SDD set for {slice}
2. Human:          review SDD (esp. spec + tasks), accept or request changes
3. Implement:      "implement {slice} against spec + tasks"  →  code + tests
4. Review:         review-code-against-design    →  fidelity check, lessons-learned
5. Human:          merge when quality gates pass
```

Steps 1, 3, and 4 can each be run by Claude Code or Codex — pick whichever agent you have open. Step 1 is one command; step 3 is one sentence. Steps 2 and 5 are the only mandatory human gates.

**Phase discipline is non-negotiable, whichever agent runs it.** Do not generate SDD for a later-phase slice, and do not let the implementing agent scaffold backend/DB/adapters, until the current phase's entry gate is met (see matrix). CLAUDE.md and the SDD profile gates override any convenience.

---

## Slice Backlog

Slugs are stable kebab-case identifiers. IDs follow the profile: `REQ-{SLICE}-###`, `US-{SLICE}-###`, `T-{SLICE}-###` (uppercase slug). Status legend: ✅ done · 🔨 active · ⬜ not started · 🔒 gated (entry conditions unmet).

| Phase | Slice slug | Scope summary | Owner boundary | Status |
|---|---|---|---|---|
| 1 (FE) | `knowledge-space` | Vue 3 shell reproducing accepted IA: home library, global multi-Knowledge-Space chat, space shell, documents, processing center, wiki, graph, settings — mock data only | FE only, no API | ✅ L2 Vue parity checkpoint for Product Goal Batch 1 plus Message/Space settings (T-KS-001→036; final product acceptance pending) |
| 1 (FE) | `folder-upload` | Folder/ZIP upload mock: file inventory, tree, batch creation, mock status/report | FE only, no API | ✅ L2 Vue parity checkpoint for Product Goal Batch 2 (T-FU-001→014; T-FU-011 deferred optional; final product acceptance pending) |
| 2 (API) | `metadata-api` | Spring Boot metadata service: batch/file/space entities, PostgreSQL + Flyway, mock-free contract | Backend + DB | ✅ implemented (T-MA-001→013; T-MA-014 partially consumed by Product Goal Batch 5 L3 Vue cutover for spaces/batches/files/chunks; remaining final acceptance pending) |
| 3 (adapter) | `converter-adapter` | `trinity-office` Office→PDF behind converter interface | Adapter, no direct call | ✅ implemented (T-CA-001→010; mock-engine verified; real runtime contract deferred) |
| 3 (adapter) | `parser-adapter` | `document-normalize` PDF→Markdown/images behind parser interface | Adapter, no direct call | ✅ implemented (T-PA-001→010; mock-engine verified; real runtime contract deferred) |
| 3 (adapter) | `storage-adapter` | S3-compatible object storage behind storage interface | Adapter, no direct call | ✅ implemented (T-SA-001→010; `mvn verify` passed with mock storage) |
| 3 (adapter) | `vector-adapter` | pgvector/vector DB behind vector interface | Adapter, no direct call | ✅ implemented (T-VA-001→010; mock-engine verified; real vector DB execution deferred) |
| 3 (adapter) | `model-adapter` | LLM/embedding provider behind model interface, secret-masked | Adapter, no direct call | ✅ L3 API-backed masked model capability checkpoint for Product Goal Batch 6 plus L2 Vue settings checkpoint for Batch 4 (T-MODA-001→011; production secret manager/RBAC/provider calls deferred; final product acceptance pending) |
| 4 (hardening) | `review-publish` | SME review state machine + approved-Markdown publish to Wiki | Full stack | ✅ L3 API-backed Wiki/review checkpoint for Product Goal Batches 5-6 plus L2 Vue Wiki checkpoint for Batch 3 (T-REVIEW-PUBLISH-001→011; production auth/RBAC and real Markdown generation deferred; final product acceptance pending) |
| 4 (hardening) | `knowledge-graph` | Node/edge extraction from approved pages + inspectable viz | Full stack | ✅ L3 API-backed Graph checkpoint for Product Goal Batch 6 plus L2 Vue Graph checkpoint for Batch 3 (T-KG-001→014; production graph layout/engine deferred; final product acceptance pending) |
| 4 (hardening) | `ask-rag` | Source-grounded, review-aware Ask over approved content | Full stack | ✅ L3 API-backed Ask checkpoint for Product Goal Batch 6 plus L2 Vue Trusted Ask checkpoint for Batch 3 (T-ASKRAG-001→011; production retrieval governance deferred; final product acceptance pending) |
| 4 (productization) | `full-stack-productization` | P0 browser-driven full-stack loop: space list, sample batch, review, publish Wiki, graph evidence, Ask citations | Full stack | ✅ Product Goal Batch 6 L3 API-backed checkpoint for I1-I7 (T-FSP-001→012; Phase J and final product acceptance pending; production upload/auth/provider hardening deferred) |
| 4+ (acceptance) | `provider-backed-e2e` | Opt-in local third-layer E2E with DeepSeek-backed Ask through ModelAdapter, live API graph/publish/vector flow, and mock/sample data only | Acceptance + model adapter | ✅ implemented (provider run requires local `ATLAS_MODEL_API_KEY`) |

## Current Next-Phase Wave Queue

This queue records the current SDD-driven roadmap state after the historical productization slices. It does not redefine behavior owned by each slice spec, and it must not be used to skip the human SDD acceptance gate.

| Wave | Slice slug | Scope summary | Current status | Next gate |
|---|---|---|---|---|
| Wave 1 / Wiki Foundation | `wiki-data-model` | Additive Wiki metadata fields, folder/run/log/issue read model, backend read APIs, repository/contract coverage, and Vue Wiki metadata rendering | ✅ Complete as Wiki Foundation data-model base; not Auto Wiki ingest completion and not production readiness | Closed for this maturity target |
| Wave 1 / Wiki Foundation | `wiki-ingest-v0` | Deterministic-only Auto Wiki page candidate generation from approved source chunks, merged into the existing Wiki data model as review-required generated Markdown artifacts | ✅ Implementation verified as Auto Wiki ingest v0 foundation; not linkify/lint, review-gate, connector, model-assisted generation, or production readiness | Current maturity target closed; next recommended slice is `wiki-linkify-lint` SDD only after user direction |
| Wave 1 / Wiki Foundation | `wiki-linkify-lint` | Deterministic Wiki link insertion, link metadata refresh, broken/orphan/source/thin-content linting, and review-facing warnings | ✅ Implementation verified as Wiki Foundation linkify/lint base; not review-gate, refresh/retract, connector, model-assisted generation, or production readiness | Closed for this maturity target |
| Wave 2 / Runtime Integration | `real-office-parser-runtime` | Real internal converter/parser runtime integration behind existing adapter boundaries | ✅ Implemented and verified; default CI remains mock-safe | Configure approved local runtime smoke env vars when real binaries are available, or move to the next accepted slice |
| Wave 3 / Runtime Readiness | `runtime-smoke-config-and-runbook` | Runtime smoke environment contract, safe evidence rules, and bilingual runbook for approved local `trinity-office` and `document-normalize` checks | ✅ Implemented as command-level runtime smoke readiness; default CI remains mock-safe and approved local pass evidence is skipped until commands are provided | Closed for this maturity target; does not represent production operations readiness |
| Wave 3 / Trust And Governance | `auth-space-rbac` | Backend-enforced current-user context, local mock auth, future SSO/OIDC boundary, space membership, role matrix, protected API guards, and permission-aware UI contract | ✅ Closeout verified for the accepted slice, including dedicated role E2E; not production SSO/OIDC readiness | Acceptance review of role semantics and `/api/spaces` list visibility; production SSO/OIDC remains future work |
| Wave 3 / Trust And Governance | `audit-log-foundation` | Safe append-only audit event capture, RBAC-protected audit read APIs, and read-only audit UI for core governance operations | ✅ Prototype implemented after accepted SDD | Production retention/SIEM/export and historical graph-audit backfill remain out of scope |
| Wave 3 / Trust And Governance | `secret-manager-integration` | Secret-reference and masked-status contract foundation across model configuration and adapter capability metadata | ✅ Implemented and closeout verified locally | Production secret manager storage, rotation, and policy automation remain future work |
| Wave 3 / Trust And Governance | `rate-limit-safe-errors` | Deterministic local API rate limiting plus typed user-safe error envelopes and frontend safe-error states | ✅ Prototype implemented and verified after accepted SDD | Production distributed quota, alerting/SLO dashboards, SSO/OIDC, SIEM/export, and production secret manager remain out of scope |
| Wave 4 / Ask And Graph Productization | `ask-session-citations` | Session-scoped Trusted Ask answer citation snapshots with safe source trace and review-aware evidence | ✅ Implemented and verified after accepted SDD | Answer governance, provider/model changes, and production cost/quota controls remain future work |
| Wave 4 / Ask And Graph Productization | `graph-from-wiki-extraction` | Deterministic graph nodes, edges, and evidence snapshots extracted from eligible reviewed/published Wiki pages with source trace | ✅ Prototype implemented and locally verified after accepted SDD | Model-assisted extraction, production graph layout/optimization, manual graph editing, and external graph services remain out of scope |
| Wave 4 / Ask And Graph Productization | `answer-review-governance` | Ask-answer-specific governance states, reviewer-safe metadata, reusable eligibility, review-action API, and Trusted Ask UI labels | ✅ Prototype implemented and locally verified after accepted SDD | Future answer reuse indexing, reviewer queues, production RBAC/audit workflow automation, and production approval operations remain out of scope |
| Wave 4 / Ask And Graph Productization | `retrieval-quality-metrics` | Deterministic quality metrics for Trusted Ask runs, evidence coverage, citation health, review eligibility, no-evidence refusal stats, and safe diagnostics | ✅ Implemented and verified after accepted SDD | Production answer governance, online evaluation, provider/model changes, and production cost/quota controls remain future work |
| Wave 5 / Connector And Operations | `manual-url-knowledge-ingest` | Metadata-only manual URL source registration with safe source trace, fetch intent, ingest status, eligibility metadata, and review-required output | ✅ Implemented and verified under preauthorized SDD | Connector sync, scheduled/recursive crawl, real external fetch, production compliance scanning, connector secrets, and approved Wiki/Ask/Graph use remain out of scope |
| Wave 5 / Connector And Operations | `connector-sync-v0` | Adapter-first mock/local connector sync foundation with connector registry, sync job/run/item status, source trace, safe errors, and review-required output handoff | ✅ Implemented and verified locally after accepted SDD | Real providers, OAuth/API keys, scheduled/background sync, webhooks, external network fetch, connector marketplace, and direct approved Wiki/Ask/Graph use remain out of scope |
| Wave 5 / Connector And Operations | `worker-retry-dead-letter` | Deterministic local worker retry and dead-letter reliability contract for batch ingest, connector sync, and future async processing | ✅ Implemented and verified locally after accepted SDD | Production MQ, distributed worker cluster, scheduled production worker, exactly-once semantics, alert automation, and real connector retry operations remain out of scope |
| Wave 5 / Connector And Operations | `deployment-monitoring-runbook` | Deployment readiness, post-delivery verification, monitoring signal catalog, alert severity, incident triage, rollback/recovery, safe evidence, and closeout runbook | ✅ Documentation-only operations contract delivered and locally verified | Real monitoring platforms, alert channels, production deployment automation, production health/SLO dashboards, connector/worker automation, and production incident tooling remain future work |
| Wave 6 / Frontend Maintainability | `frontend-componentization` | Behavior-preserving structural extraction of the monolithic Vue `App.vue` into feature components/composables | ✅ Locally verified after accepted SDD; T-001 through T-007 implemented | Review/commit checkpoint; future router, URL semantics, backend/API changes, dependencies, or visual redesign require a separate slice |

Slice boundaries are guidance, not law: split a slice if its task list would exceed a reviewable single implementation pass, and record the split in traceability. Adapter slices in Phase 3 can each be an independent generate-all unit so contracts stay small.

---

## Per-Phase Verification & Constraints Matrix

The generate-all prompt must bake the right row into the slice's `Verification` and `Constraints`, and the tasks doc must carry the exact commands.

| Phase | Verification (run before "done") | Hard constraints | API guide |
|---|---|---|---|
| 1 FE | `npm run typecheck` · `npm run test` · `npm run build` · `npm run e2e` | Mirror `prototypes/index.html` ↔ `frontend/public/atlas-prototype.html`; mock data only; **no** new network calls or external deps | Optional — tasks must state "no API contract in this slice" and traceability must record the omission |
| 2 API | `mvn verify` · Flyway migration validation · API contract tests | No hardcoded single DB as the only impl; secrets **masked/status-only**, never raw; no real company data | **Required** — data-model + API guide must be accepted *before* implementation starts |
| 3 adapter | Unit + integration tests against **mock engines** | Parser/converter/model/vector/storage go **only** through product-facing adapters; never call a tool directly; never hardcode one impl | Adapter contract required per adapter slice |
| 4 hardening | Full unit + integration + E2E for the touched layer | Preserve source trace, confidence, review status on all Markdown/metadata; LLM output stays review-required until verified | Required where new endpoints are introduced |

Every phase also runs the Phase 0/1 baseline checks from CLAUDE.md: static syntax check on edited HTML/CSS/JS, `git diff --check`, new-network/new-dependency scan, and secret/private-path scan. Name any skipped check and why — never imply an unrun check passed.

---

## Template A — Generate full SDD (one shot)

Paste into your SDD agent (**Claude Code or Codex**) **in this repo**. It fills the goal block and invokes `atlas-sdd-generate-all`, which chains `req-to-user-story → user-story-to-spec → spec-to-architecture → architecture-to-design → design-to-tasks → review-doc-quality` and writes every file (EN + `.zh-CN.md`) to Atlas paths.

```text
Use the atlas-sdd-generate-all skill to generate the complete bilingual SDD set for one slice.

Goal: <user-facing outcome in one sentence>
Slice: <kebab-case-slug>
Phase: <1 FE | 2 API | 3 adapter | 4 hardening>
Scope: <in-scope behavior> | Exclusions: <explicitly out of scope>
Sources: <prototype surfaces + docs/* to ground on>
Acceptance: <observable completion criteria>
Verification: <copy the matching row from docs/00-context/slice-roadmap.md>
Constraints: <copy the matching row: mock-only / adapter / secret-masked / trace-preserving>

Requirements:
- Read PROJECT_RULES.md, AGENTS.md, DEVELOPMENT_STANDARDS.md, docs/00-context/sdd-profile.md,
  docs/01-requirements/requirement.md, and the FE baseline files when the slice touches frontend.
- Write both EN and .zh-CN.md for every touched artifact; keep REQ/US/T IDs identical across languages.
- Tasks must be Codex-actionable: each maps to a REQ id + spec section, has verification with exact
  commands, and states mock-only / no-network / adapter / secret-masked constraints where they apply.
- If backend/API is not in scope, omit the API guide and record that decision in traceability.
- Do NOT implement product code in this pass. Produce SDD only.
- End with the review-doc-quality gate and the recommended Codex handoff command.
```

Output lands under `docs/01-requirements/`, `docs/02-user-stories/`, `docs/03-spec/`, `docs/04-architecture/`, `docs/05-design/` (+ `contracts/`), `docs/06-tasks/`, and `docs/00-context/{slice}-traceability.md`.

---

## Template B — Implement all tasks (one sentence)

After the SDD is human-accepted, hand your implementation agent (**Codex or Claude Code**) one sentence:

```text
Implement the {slice} slice strictly against docs/03-spec/{slice}-spec.md and docs/06-tasks/{slice}-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

Longer, safer variant when you want the guardrails spelled out:

```text
Task: implement the {slice} slice.
Contract: docs/06-tasks/{slice}-tasks.md is the checklist; docs/03-spec/{slice}-spec.md is the behavior source of truth; docs/05-design/{slice}-design.md and the data-model/API guide are the design contract.
Rules: complete tasks in ID order; run each task's Verification and report results; keep parser/converter/model/vector/storage behind adapters (Phase 3+); mock-only and no external network calls for Phase 1; secrets masked/status-only, never raw; preserve source trace, confidence, and review status on all Markdown/metadata; touch only files the tasks require.
Divergence: if a task cannot be met as written, stop and report the spec/task mismatch — do not silently redesign.
Done means: all tasks checked, verification run (or explicitly marked blocked with reason), and a final report naming changed files, checks run, and residual risks.
```

---

## Template C — Close-out review (optional but recommended)

Run in either agent after the implementation step reports done:

```text
Use review-code-against-design to check the {slice} implementation against docs/03-spec/{slice}-spec.md,
docs/05-design/{slice}-design.md, and docs/06-tasks/{slice}-tasks.md. Report fidelity gaps by severity.
Run the slice's Verification row. For any acceptance mismatch, update the artifact that prevents recurrence
(spec/design/task/standard/rule/test) and log it in docs/00-context/lessons-learned.md — not just chat.
```

---

## Guardrails Recap

- **One slice at a time.** Finish the loop (SDD → accept → implement → review) before opening the next.
- **Gates over convenience.** No backend before API guide + data-model accepted; no adapter work before Phase 2; no direct tool calls, ever.
- **Bilingual parity.** Every SDD artifact has EN + `.zh-CN.md`; IDs never translated.
- **Trace/confidence/review** survive every transformation of Markdown and metadata.
- **Mock-only & no-secrets** in prototype/Phase 1; no real company data, private paths, or raw credentials anywhere.
- **Surgical scope.** Every changed line traces to the goal, a REQ, a rule, or a verification step.

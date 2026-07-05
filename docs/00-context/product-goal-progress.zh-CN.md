# Atlas 产品 Goal 执行进度账本

本文件用于支持 Codex 持续 goal 的中断恢复。每个 batch、phase 或异常结束前后都应更新这里，避免依赖聊天上下文。

创建时间：2026-07-05  
当前计划：[product-goal-execution-plan.zh-CN.md](product-goal-execution-plan.zh-CN.md)

## 当前状态

| 字段 | 值 |
|---|---|
| 当前 batch | Batch 7 已完成，准备 Batch 8 |
| 当前 phase | Phase J completed |
| 当前 slice | internal-beta-hardening-readiness |
| 当前成熟度 | L4 readiness preparation checkpoint；最终产品验收待用户决定 |
| 最后完成 checkpoint | Batch 7 / Phase J / 2026-07-05 |
| 当前 blocker | 无 |
| 下一步 | 从 Batch 8 / Final 验收证据整理开始：统一整理 A-J 成熟度、证据、缺口、风险和验收建议 |

## Batch 记录

| Batch | 范围 | 状态 | 成熟度 | 证据 |
|---|---|---|---|---|
| 1 | Phase A + B | completed | L2 Vue parity | `docs/00-context/evidence/phase-a-product-home.png`; `docs/00-context/evidence/phase-b-space-detail.png`; `frontend/tests/e2e/phase-a-b-product-shell.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; focused scans |
| 2 | Phase C + D | completed | L2 Vue parity | `docs/00-context/evidence/phase-c-upload-batch.png`; `docs/00-context/evidence/phase-d-processing-center.png`; `frontend/tests/e2e/phase-c-d-upload-processing.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; focused scans |
| 3 | Phase E + F + G | completed | L2 Vue parity | `docs/00-context/evidence/phase-e-lm-wiki.png`; `docs/00-context/evidence/phase-f-knowledge-graph.png`; `docs/00-context/evidence/phase-g-trusted-ask.png`; `frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; focused scans |
| 4 | Phase H | completed | L2 Vue parity | `docs/00-context/evidence/phase-h-settings-administration.png`; `frontend/tests/e2e/phase-h-settings-administration.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; focused scans |
| 5 | Phase I1-I3 | completed | L3 API-backed | `docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png`; `frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; Batch 1-5 Playwright regression; `cd backend && mvn verify`; focused scans |
| 6 | Phase I4-I7 | completed | L3 API-backed | `docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`; `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`; `npm run typecheck`; `npm run test`; `npm run build`; Batch 1-6 Playwright regression; `cd backend && mvn verify`; focused scans |
| 7 | Phase J | completed | L4 readiness preparation | `docs/07-acceptance/internal-beta-readiness.md`; `docs/07-acceptance/internal-beta-readiness.zh-CN.md`; `npm run e2e:second-layer`; `git diff --check`; focused scans |
| 8 | Final 验收证据整理 | 未开始 | 未评估 | 待补 |

## Phase 记录模板

每完成或中断一个 phase，在本节追加记录。

```text
Date:
Batch:
Phase:
Slice:
Status: completed | in-progress | blocked | failed
Maturity: L1 prototype | L2 Vue parity | L3 API-backed | L4 readiness | L5 production-ready | not evaluated
SDD changed:
Docs changed:
Code changed:
Task IDs completed:
Verification run:
Evidence:
Skipped checks:
Blockers:
Resume from:
```

```text
Date: 2026-07-05
Batch: 1
Phase: Phase A + Phase B
Slice: knowledge-space
Status: completed
Maturity: L2 Vue parity
SDD changed: Added Simplified Chinese companion docs for knowledge-space requirements, stories, spec, architecture, data flow, data model, design, and tasks; updated English spec/design/tasks with Product Goal Batch 1 mapping.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/knowledge-space-traceability.md, docs/00-context/knowledge-space-traceability.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/styles.css, frontend/package.json, frontend/tests/e2e/phase-a-b-product-shell.spec.ts.
Task IDs completed: T-KS-032, T-KS-033, T-KS-034.
Verification run: cd frontend && npm run typecheck; cd frontend && npm run test; cd frontend && npm run build; cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts --project=chromium; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-a-product-home.png; docs/00-context/evidence/phase-b-space-detail.png; Playwright 1 passed; Vitest 3 files / 11 tests passed; build passed.
Skipped checks: Full frontend npm run e2e not run for Batch 1 because the phase-specific Playwright test covers the requested Phase A/B product path and full suite belongs to broader later batches; backend mvn verify not run because Batch 1 changed no backend/API contract or code.
Blockers: None.
Resume from: Batch 2 / Phase C + D.
```

```text
Date: 2026-07-05
Batch: 2
Phase: Phase C + Phase D
Slice: folder-upload; review-publish; knowledge-space detail product path
Status: completed
Maturity: L2 Vue parity
SDD changed: Updated folder-upload spec/tasks/traceability in English and Simplified Chinese for the Product Goal Batch 2 Vue parity checkpoint; updated review-publish spec/tasks/traceability in English and Simplified Chinese for the real Vue Processing Center checkpoint.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/styles.css, frontend/src/App.test.ts, frontend/package.json, frontend/tests/e2e/phase-a-b-product-shell.spec.ts, frontend/tests/e2e/phase-c-d-upload-processing.spec.ts.
Task IDs completed: T-FU-014, T-REVIEW-PUBLISH-010.
Verification run: cd frontend && npm run typecheck; cd frontend && npm run test; cd frontend && npm run build; cd frontend && npx playwright test tests/e2e/phase-c-d-upload-processing.spec.ts --project=chromium; cd frontend && npm run build && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts --project=chromium; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-c-upload-batch.png; docs/00-context/evidence/phase-d-processing-center.png; Playwright Phase C/D 1 passed; Playwright Phase A/B + C/D 2 passed; Vitest 3 files / 12 tests passed; build passed.
Skipped checks: Full frontend npm run e2e not run for Batch 2 because the phase-specific Playwright checks cover the requested Phase C/D product path and prior Phase A/B regression path; backend mvn verify not run because Batch 2 changed no backend/API contract or backend code.
Blockers: None.
Resume from: Batch 3 / Phase E + F + G.
```

```text
Date: 2026-07-05
Batch: 3
Phase: Phase E + Phase F + Phase G
Slice: review-publish; knowledge-graph; ask-rag; knowledge-space product surfaces
Status: completed
Maturity: L2 Vue parity
SDD changed: Updated review-publish spec/tasks/traceability in English and Simplified Chinese for Phase E LM Wiki Vue parity; updated knowledge-graph spec/tasks/traceability in English and Simplified Chinese for Phase F real Vue Graph parity; updated ask-rag spec/tasks/traceability in English and Simplified Chinese for Phase G real Vue Trusted Ask parity.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/styles.css, frontend/src/App.test.ts, frontend/package.json, frontend/tests/e2e/phase-a-b-product-shell.spec.ts, frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts.
Task IDs completed: T-REVIEW-PUBLISH-011, T-KG-014, T-ASKRAG-011.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; cd frontend && npx playwright test tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts --project=chromium; cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts --project=chromium; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-e-lm-wiki.png; docs/00-context/evidence/phase-f-knowledge-graph.png; docs/00-context/evidence/phase-g-trusted-ask.png; Playwright Phase E/F/G 1 passed; Playwright Phase A/B + C/D + E/F/G 3 passed; Vitest 3 files / 13 tests passed; build passed.
Skipped checks: Full frontend npm run e2e not run for Batch 3 because the phase-specific Playwright check covers Phase E/F/G product surfaces and the targeted regression covers prior Product Goal product paths; backend mvn verify not run because Batch 3 changed no backend/API contract or backend code.
Blockers: None.
Resume from: Batch 4 / Phase H.
```

```text
Date: 2026-07-05
Batch: 4
Phase: Phase H
Slice: model-adapter; settings-administration
Status: completed
Maturity: L2 Vue parity
SDD changed: Updated model-adapter spec/tasks/traceability in English and Simplified Chinese for Phase H real Vue settings administration parity.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/styles.css, frontend/src/App.test.ts, frontend/package.json, frontend/tests/e2e/phase-h-settings-administration.spec.ts.
Task IDs completed: T-MODA-011.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; cd frontend && npx playwright test tests/e2e/phase-h-settings-administration.spec.ts --project=chromium; cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts --project=chromium; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-h-settings-administration.png; Playwright Phase H 1 passed; Playwright Phase A/B + C/D + E/F/G + H 4 passed; Vitest 3 files / 14 tests passed; build passed.
Skipped checks: Full frontend npm run e2e not run for Batch 4 because the phase-specific Playwright check covers Phase H product settings and the targeted regression covers prior Product Goal product paths; backend mvn verify not run because Batch 4 changed no backend/API contract or backend code.
Blockers: None.
Resume from: Batch 5 / Phase I1-I3.
```

```text
Date: 2026-07-05
Batch: 5
Phase: Phase I1-I3
Slice: full-stack-productization; metadata-api; review-publish
Status: completed
Maturity: L3 API-backed
SDD changed: Updated full-stack-productization spec/tasks/traceability in English and Simplified Chinese for Phase I1-I3 API-backed Vue cutover; updated metadata-api traceability for partial T-MA-014 FE cutover; updated review-publish traceability for API-backed review queues. No new backend/API contract was introduced.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/styles.css, frontend/src/App.test.ts, frontend/package.json, frontend/tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts.
Task IDs completed: T-FSP-011; partial T-MA-014 coverage for spaces/batches/files/chunks; review queue API-backed product surface coverage.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; cd frontend && npx playwright test tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium; cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium; cd backend && mvn verify; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png; Playwright Phase I1-I3 1 passed; Playwright Batch 1-5 regression 5 passed; Vitest 3 files / 15 tests passed; frontend build passed; backend mvn verify passed with 86 unit tests and 37 integration tests.
Skipped checks: Full frontend npm run e2e not run for Batch 5 because the phase-specific Playwright check covers I1-I3 and the targeted Batch 1-5 regression covers all Product Goal paths completed so far; full final acceptance suite remains for later batches.
Blockers: None.
Resume from: Batch 6 / Phase I4-I7.
```

```text
Date: 2026-07-05
Batch: 6
Phase: Phase I4-I7
Slice: full-stack-productization; review-publish; knowledge-graph; ask-rag; model-adapter
Status: completed
Maturity: L3 API-backed
SDD changed: Updated full-stack-productization spec/tasks/traceability in English and Simplified Chinese for Phase I4-I7 API-backed Vue cutover; updated review-publish, knowledge-graph, ask-rag, and model-adapter traceability in English and Simplified Chinese. No new backend/API contract was introduced.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/slice-roadmap.md, docs/00-context/slice-roadmap.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/src/api.ts, frontend/src/types.ts, frontend/src/App.test.ts, frontend/package.json, frontend/tests/e2e/p0-api-mock.ts, frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts, frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts.
Task IDs completed: T-FSP-012; product-path API-backed coverage for Wiki pages, Graph evidence, Ask runs/citations, and model adapter masked capability metadata.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; cd frontend && npx playwright test tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium; cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium; cd backend && mvn verify; git diff --check; focused secret/private-path scan; focused network/dependency scan.
Evidence: docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png; Playwright Phase I4-I7 1 passed; Playwright Batch 1-6 regression 6 passed; Vitest 3 files / 16 tests passed; frontend build passed; backend mvn verify passed with 86 unit tests and 37 integration tests.
Skipped checks: Full frontend npm run e2e not run for Batch 6 because focused Phase I4-I7 E2E and targeted Batch 1-6 regression cover completed Product Goal paths; final acceptance suite remains for Batch 8.
Blockers: None.
Resume from: Batch 7 / Phase J.
```

```text
Date: 2026-07-05
Batch: 7
Phase: Phase J
Slice: internal-beta-hardening-readiness
Status: completed
Maturity: L4 readiness preparation
SDD changed: Added English and Simplified Chinese internal beta readiness reports under docs/07-acceptance; no product scope, API contract, security model, real data, secret, or external provider behavior changed.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: None.
Task IDs completed: Product Goal Phase J readiness checkpoint.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; npm run e2e:second-layer; git diff --check; focused secret/private-path/real-data scan; reused Batch 6 Batch 1-6 Playwright regression and backend mvn verify as implementation evidence because Batch 7 changed docs and second-layer specs only.
Evidence: docs/07-acceptance/internal-beta-readiness.md; docs/07-acceptance/internal-beta-readiness.zh-CN.md; npm run e2e:second-layer 2 passed.
Skipped checks: None for Phase J readiness scope. Third-layer provider-backed E2E remains opt-in and outside this batch because it requires a real approved provider key.
Blockers: None.
Resume from: Batch 8 / Final 验收证据整理.
```

```text
Date: 2026-07-05
Batch: 8
Phase: Final 验收证据整理
Slice: final-product-acceptance-readiness
Status: completed
Maturity: Acceptance-ready evidence package; final product acceptance pending user decision
SDD changed: None. Final evidence整理 did not redefine product scope, architecture, security model, API contract, real data handling, secrets, or external provider behavior.
Docs changed: ROADMAP.md, ROADMAP.zh-CN.md, docs/07-acceptance/product-acceptance-report.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: frontend/src/App.vue, frontend/tests/e2e/phase1-smoke.spec.ts. A Final E2E gap was fixed by making product graph node clicks load the matching API graph node detail/source trace, and by aligning the legacy smoke assertion with the current API-backed space header.
Task IDs completed: Product Goal Final 验收证据整理.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; cd frontend && npm run e2e; cd backend && mvn verify; npm run e2e:second-layer; git diff --check; focused secret/private-path/real-data scan.
Evidence: docs/07-acceptance/product-acceptance-report.zh-CN.md; frontend npm run e2e 13 passed; backend mvn verify passed with 86 unit tests and 37 integration tests; root npm run e2e:second-layer 2 passed.
Skipped checks: npm run e2e:third-layer was not run because it requires a user-approved real provider key, network availability, and provider quota; it remains opt-in and outside default Final readiness verification.
Blockers: None for Final 验收准备. Production acceptance remains blocked by user decision and by documented P0/P1 readiness gaps.
Resume from: Product Goal complete; user acceptance review pending.
```

```text
Date: 2026-07-05
Batch: Post-Final provider-backed verification
Phase: DeepSeek third-layer E2E
Slice: provider-backed-e2e
Status: completed
Maturity: Opt-in provider-backed validation passed; not default CI and not production acceptance
SDD changed: None. This was an approved local verification using mock/sample knowledge data and a user-provided local DeepSeek key from .env.
Docs changed: docs/07-acceptance/product-acceptance-report.zh-CN.md, docs/00-context/product-goal-progress.zh-CN.md.
Code changed: scripts/e2e/run-third-layer.sh now loads repository-root .env safely and treats placeholder API keys as invalid; third-layer Playwright spec now targets the current real Vue product graph path instead of the retired static prototype selector.
Verification run: cd frontend && npm run typecheck && npm run test && npm run build; ATLAS_E2E_BACKEND_PORT=18091 ATLAS_E2E_POSTGRES_PORT=55441 ATLAS_E2E_POSTGRES_CONTAINER=atlas-e2e-third-postgres-18091 npm run e2e:third-layer; bash -n scripts/e2e/run-third-layer.sh; provider-backed artifact hygiene scan; git diff --check.
Evidence: third-layer provider-backed Playwright 1 passed; configured Ask returned SUCCEEDED with DeepSeek adapterKey and configured mode; product graph Vue evidence displayed generated chunk id and APPROVED status. The wrapper initially failed after the passed E2E on a false-positive ROOT_DIR path match in Maven logs; scan logic was narrowed and the same artifacts passed hygiene.
Skipped checks: None for the opt-in third-layer verification scope.
Blockers: None for provider-backed local validation. Production acceptance remains subject to P0/P1 readiness gaps and user decision.
Resume from: Product Goal complete; user acceptance review pending.
```

## 恢复检查清单

恢复 session 时先做这些事：

- 读取本文件，确认当前 batch / phase / slice。
- 运行 `git status --short`。
- 运行 `git diff --stat`。
- 检查上一个 phase 是否已更新 roadmap、traceability、slice roadmap。
- 检查上一个 phase 是否已有验证证据。
- 若存在半成品代码，先补验证或修复，不跳 phase。
- 若存在 SDD/code 不一致，先修 SDD 或停止报告。

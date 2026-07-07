# Deployment Monitoring Runbook

Status: Operational contract delivered for `deployment-monitoring-runbook`
Last updated: 2026-07-07
Maturity: L4 readiness preparation / operations contract only. This is not L5 production observability.

## 1. Purpose

Use this runbook before PR, before push, after local/staging-like delivery, and during incident triage. It turns Atlas deployment monitoring into a repeatable checklist without connecting a real monitoring platform, alert channel, production deployment pipeline, or cloud service.

## 2. Quick Use

| Moment | Use |
|---|---|
| Before editing a delivery slice | Confirm manifest, SDD scope, branch, and stop conditions. |
| Before PR or push | Run pre-deployment checklist and record safe evidence. |
| After local delivery | Run post-deployment checklist for startup, UI/API behavior, safe errors, and product surfaces. |
| When a signal is abnormal | Classify severity, open the matching incident card, and record safe evidence. |
| Before closeout | Run closeout evidence checklist and `npm run agent:closeout`. |

## 3. Pre-Deployment Checklist

| Check | Applicability | Expected evidence |
|---|---|---|
| Repo status | Always | `git status --short` is clean or only scoped slice files are changed. |
| Branch and commit hygiene | Always | Target branch is correct; commit message is conventional; unrelated work is not staged. |
| Execution manifest | Tier 1+ | Manifest exists under `docs/00-context/execution-manifests/` and names the active slice. |
| SDD gate | Tier 1+ | `npm run agent:check-sdd -- --slice <slice>` passes, with report path when skill-chain evidence is required. |
| Closeout gate dry run | Before final report | `npm run agent:closeout` passes after traceability/roadmap updates. |
| Backend verification | Backend files changed | `cd backend && mvn verify` passes, or skipped because no backend files changed. |
| Frontend verification | Frontend files changed | `cd frontend && npm run typecheck && npm run test && npm run build` passes, or skipped because no frontend files changed. |
| Workflow script verification | Workflow scripts changed | Run the affected npm script and Node syntax check. |
| Secret/private-path scan | Always | Focused scan of changed files finds no raw secrets, private paths, internal endpoints, real logs, real company data, or credentials. |
| Network/dependency scan | Always | No dependency manifest change or new external monitoring/cloud/network integration unless a future accepted slice scopes it. |
| Mock/sample-only review | Always | Evidence uses only repository-safe mock/sample data. |
| Adapter boundary review | Adapter or operations docs changed | Product logic remains behind parser/converter/model/vector/storage/search/connector boundaries. |
| Rollback readiness | Always | Know which files to revert or which commit to back out; no destructive migration or production environment change is involved. |

Skipped checks must include a reason. Do not imply an unrun command passed.

## 4. Post-Deployment Checklist

| Check | Applicability | Expected evidence |
|---|---|---|
| Application starts | When local delivery includes app runtime | Startup command succeeds or failure is classified with an incident card. |
| Key UI routes load | Frontend in scope | Home, knowledge space detail, upload/processing, Wiki, Review, Graph, Ask, and Settings surfaces load when present. |
| API health/status behavior | API status endpoint present | Endpoint responds with expected safe status. If no endpoint exists, record "not present in current phase". |
| Safe error format | Backend/API in scope | Representative errors keep the safe envelope and do not expose internals. |
| Upload/ingest surface | Present | Batch/file statuses are not stuck unexpectedly and use safe messages. |
| Wiki/review/publish surface | Present | Review-required and publish states remain traceable and do not auto-approve unverified content. |
| Ask surface | Present | Citations/no-evidence states are safe and evidence-aware. |
| Graph surface | Present | Graph output remains derived from eligible reviewed/published Wiki evidence. |
| Connector/worker surface | Present | Connector sync and retry/dead-letter summaries are safe and bounded. |
| Logs/UI safety | Always | Evidence contains summaries only, not raw logs, real data, private paths, internal endpoints, screenshots, or credentials. |

## 5. Monitoring Signal Catalog

| Signal | Source | Safe collection | Healthy pattern | Action threshold |
|---|---|---|---|---|
| Request success/failure rate | API tests, local smoke, E2E | Count pass/fail summaries, not raw logs | Expected tests pass | P1 if key API path is unavailable; P2 if non-critical path degrades. |
| Safe error category distribution | API contract tests | Safe code counts only | Known validation/auth/not-found/rate-limit errors map to safe codes | P0 if unsafe internals leak; P2 if mapping drifts. |
| Rate limit events | Rate-limit tests or API responses | Count `RATE_LIMITED` responses and retry hints | Deterministic local behavior | P2 if unexpected local throttling blocks key flow; P3 for doc-only drift. |
| Ingest job status | Batch/ingest UI or API when present | Status counts and safe error summaries | Jobs finish or clearly require review | P1 if all ingest is stuck; P2 if partial backlog grows. |
| Connector sync run status | Connector APIs/UI when present | Run status counts and safe summaries | Sync completes or fails safely | P1 if connector sync blocks release path; P2 for degraded source. |
| Worker retry/dead-letter status | Worker evidence when present | Retry/dead-letter counts only | No unexplained spike | P1 for spike blocking processing; P2 for bounded known failures. |
| Review-required queue size | Review UI/API | Count only, no raw documents | Queue is visible and actionable | P2 if blocked; P3 if stale count only. |
| Wiki publish success/failure | Wiki/review tests | Publish counts and safe failures | Approved/source-traced content publishes | P1 if publish path unavailable; P2 for partial failure. |
| Ask citation/no-evidence | Ask tests/UI | Citation health/no-evidence counts | No-evidence refusal is safe; citations have source trace | P0 for unsafe citation; P2 for unexpected no-evidence spike. |
| Graph extraction success/failure | Graph tests/UI | Node/edge counts and safe mismatch summaries | Output matches eligible Wiki evidence | P1 if graph path unavailable; P2 for mismatch requiring review. |

## 6. Alert Severity Model

| Severity | Trigger examples | Owner | Immediate action | Escalation condition | Closeout evidence |
|---|---|---|---|---|---|
| P0 | Suspected secret/private data exposure; unsafe API error leakage; trusted evidence corruption | Security reviewer + delivery lead | Stop closeout, avoid committing evidence, inspect changed files, rotate exposed material outside repo if needed | Any confirmed exposure or inability to prove safety | Safe summary, affected file list, remediation, scan results, lesson/prevention if reusable |
| P1 | Backend cannot start; frontend build blocks release; core API-backed upload/Wiki/Ask/Graph path unavailable | Delivery lead + owning engineer | Reproduce locally, isolate changed slice, roll back docs/code change if safe | Failure persists after scoped fix or requires production credentials/infrastructure | Command summary, root cause, fix or rollback, skipped checks |
| P2 | Connector sync degraded; worker dead-letter spike; review queue blocked; graph/Ask quality signal abnormal | Owning engineer + product reviewer | Confirm scope, classify affected surface, record safe summaries, defer if future slice | Blocks acceptance or grows into P1 | Signal summary, decision, follow-up slice if needed |
| P3 | Documentation drift; missing evidence reason; stale roadmap wording | Slice owner | Update docs, rerun gates | Repeats across slices or causes wrong readiness claim | Updated docs and gate result |

## 7. Incident Runbooks

### Backend fails to start

- Default severity: P1.
- First checks: backend command used, Java/Maven availability, database/Testcontainers requirement, recent backend/migration changes.
- Mitigation: rerun with scoped command; inspect bounded error summary; do not paste raw stack traces into docs.
- Escalate: requires real credentials, destructive migration, or unclear repeated failure.
- Evidence: command, pass/fail summary, changed backend files if any, skipped checks.

### Frontend build fails

- Default severity: P1.
- First checks: `cd frontend && npm run typecheck && npm run test && npm run build`, dependency manifest changes, generated output.
- Mitigation: fix scoped TypeScript/test/build issue or roll back current slice frontend change.
- Escalate: dependency update or broad UI regression outside active slice is required.
- Evidence: failing command summary and fixed file list.

### API returns unsafe errors

- Default severity: P0.
- First checks: safe error code, message, path, correlation metadata, absence of raw exception/secret/private path/internal endpoint.
- Mitigation: stop closeout, inspect safe error boundary, add or update contract test in the owning slice.
- Escalate: any confirmed sensitive leakage.
- Evidence: sanitized summary only, never raw response if it contains sensitive content.

### Upload or ingest stuck

- Default severity: P2, P1 when core delivery is blocked.
- First checks: batch status counts, review-required queue, safe error summaries, adapter status.
- Mitigation: confirm whether current slice touched ingest; rerun existing ingest tests if applicable.
- Escalate: stuck state blocks acceptance or suggests data loss.
- Evidence: status counts and safe summaries.

### Connector sync stuck or failed

- Default severity: P2.
- First checks: connector run status, item status counts, safe error category, mock/local fixture state when present.
- Mitigation: isolate connector-specific failure and keep external systems out of evidence.
- Escalate: sync failure blocks the release goal or requires real source credentials.
- Evidence: run ID or count summary only, no source content.

### Worker dead-letter spike

- Default severity: P2, P1 when processing is blocked.
- First checks: retry counts, dead-letter counts, recent queue/worker changes when present.
- Mitigation: pause further changes and classify failed item categories.
- Escalate: repeated unknown failures or evidence of data loss.
- Evidence: counts and safe category summaries.

### Review queue blocked

- Default severity: P2.
- First checks: review-required count, reviewer action availability, publish eligibility, RBAC safe error.
- Mitigation: verify review workflow and publish gates with existing tests when applicable.
- Escalate: approved content cannot publish or unreviewed content appears trusted.
- Evidence: counts and state summaries.

### Ask no evidence or unsafe citation state

- Default severity: P2, P0 for unsafe citation leakage.
- First checks: no-evidence refusal, citation status, source trace presence, review policy.
- Mitigation: confirm Ask returns safe refusal and does not invent evidence.
- Escalate: citation leaks private data or answer bypasses evidence policy.
- Evidence: safe status and counts only.

### Graph extraction output mismatch

- Default severity: P2.
- First checks: eligible Wiki inputs, node/edge counts, evidence references, review status, confidence threshold.
- Mitigation: rerun graph tests if graph code changed; otherwise record future follow-up.
- Escalate: graph shows unreviewed or source-less knowledge as trusted.
- Evidence: count/mismatch summary only.

### Suspected secret or private data exposure

- Default severity: P0.
- First checks: changed files only; do not copy the suspected value into reports.
- Mitigation: stop, remove exposure, rotate affected secret outside repo if real, scan for similar patterns, update prevention artifact when reusable.
- Escalate: any real credential, company data, internal endpoint, or private path was committed or pushed.
- Evidence: safe description of category, affected file path, remediation, scan result.

## 8. Rollback And Recovery

| Case | Safe recovery |
|---|---|
| Docs-only issue | Revert or patch the affected docs and rerun SDD/closeout gates. |
| Local mock/sample state issue | Reset generated local outputs using the local runbook; do not commit generated artifacts. |
| Backend/frontend build issue | Fix within active slice or roll back the active slice change. |
| Migration issue | Stop unless a future accepted slice explicitly covers migration repair. |
| Production-like infrastructure issue | Stop and request user decision. |
| Secret/private data exposure | Stop, remediate, rotate outside repo when real, and rerun scans before continuing. |

## 9. Safe Logging And Data Handling

Never commit:

- raw secrets, API keys, passwords, tokens, credentials, or provider payloads;
- private local paths or internal endpoints;
- raw stack traces, raw logs, generated reports, screenshots, traces, or incident exports;
- real company documents, real customer content, or confidential screenshots;
- `.env` files or shell-history exports.

Use this safe evidence pattern:

```text
Check: <command or manual check>
Result: pass/fail/skipped
Summary: <bounded non-sensitive summary>
Skipped reason: <only when skipped>
Follow-up: <none or issue/slice>
```

## 10. Closeout Evidence

Before reporting complete:

1. Run `git status --short`.
2. Run `npm run agent:check-sdd -- --slice deployment-monitoring-runbook --report docs/00-context/deployment-monitoring-runbook-sdd-completion-report.md`.
3. Run `git diff --check`.
4. Run a focused secret/private-path/real-data scan over changed files.
5. Run a focused network/dependency scan over changed files.
6. Run `npm run agent:closeout`.
7. Confirm backend/frontend verification was skipped only because no backend/frontend files changed.
8. Review diff for scope creep and readiness overclaiming.
9. Commit and push only the active slice files.

## 11. CI And Manual Gate Alignment

- Local `npm run agent:closeout` runs the same workflow gate shape as the GitHub Actions Agent Workflow Gate for changed SDD slices.
- The CI gate does not replace manual product-surface checks; it verifies SDD, workflow docs, diff hygiene, and safety scans.
- A green runbook closeout means this documentation slice is delivered, not that Atlas is production monitored.

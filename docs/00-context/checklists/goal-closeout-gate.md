# Goal Closeout Gate Checklist

Use this checklist before marking any Atlas goal complete.

## Closeout Evidence

| Check | Pass Criteria | Status |
|---|---|---|
| Goal mode | Report states `master`, `single-slice`, or `light-maintenance`. | |
| Workflow tier | Report states Tier 0, Tier 1, Tier 2, or Tier 3 and why. | |
| Manifest | Report names the execution manifest used, or explains why Tier 0 did not require one. | |
| Slice | Active slice is named; master goals name the completed slice and next eligible slice. | |
| SDD gate | Full SDD work reports `SDD skill chain used: yes`, skill files read, and `review-doc-quality` result. | |
| User acceptance | Required acceptance was obtained before code, or the report explains why acceptance was not required. | |
| Docs changed | Report lists changed SDD, traceability, roadmap, progress, and checklist docs. | |
| Code changed | Report lists changed implementation and test files, or says no code changed. | |
| Task mapping | Completed task IDs map back to `docs/06-tasks/{slice}-tasks.md`. | |
| Verification | Required commands ran, or skipped checks include reasons. | |
| Workflow gate | `npm run agent:closeout` ran locally, and PR/push `Agent Workflow Gate` is green when applicable. | |
| Security/data | Secret, private-path, real-data, and external-network scans were run or explicitly skipped with reasons. | |
| Maturity | Report names the maturity level and does not overclaim product readiness. | |
| Traceability | `docs/00-context/{slice}-traceability.md` and `.zh-CN.md` are current when slice status changed. | |
| Lessons | Reusable mismatches update `docs/00-context/lessons-learned.md` and a prevention artifact. | |
| Resume point | Report says where to resume if the goal is not fully complete. | |

## Fail-Fast Conditions

Do not mark complete if:

- Required SDD skill-chain evidence is missing.
- Verification failed and no blocker is reported.
- `npm run agent:closeout` or `Agent Workflow Gate` is red.
- Product readiness is claimed without user-facing workflow evidence.
- Real data, secrets, private paths, or unapproved external calls were introduced.
- The work mixed unrelated slices.
- Traceability or roadmap/progress status is stale after a status change.

## Closeout Note Template

```text
Goal mode:
Workflow tier:
Slice:
Status:
Maturity:
Manifest:
SDD skill chain used:
Skill files read:
Docs changed:
Code changed:
Task IDs completed:
Verification run:
Skipped checks:
Security/data scans:
Evidence:
Residual risks:
Lessons recorded:
Next action:
Resume point:
```

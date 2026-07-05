# Design: Wiki Linkify And Lint

## Status

Draft for user review.

## User Experience

The slice adds maintenance visibility to existing Wiki surfaces:

- Processing Center shows Wiki issue totals by type.
- Wiki index/page detail keeps showing slug, source mode, refresh policy, confidence, review status, and link counts.
- Selected Wiki page can show review warnings such as broken links or missing source trace.
- Warnings do not change approval state and do not imply production readiness.

## Backend Design

### Run Service

The run service should:

1. Validate space and optional page scope.
2. Load current pages for the space.
3. Build a target index from slug and aliases.
4. Read Markdown artifacts through safe storage.
5. Apply protected-region-aware linkify.
6. Recompute inbound/outbound link arrays.
7. Run lint rules.
8. Persist page metadata, issues, logs, and run summary.

### Linkifier

The linkifier should be a pure deterministic utility with focused unit tests. It should return:

- rewritten Markdown;
- target slugs inserted;
- existing Wiki link slugs found;
- skipped/protected segments count when useful for diagnostics.

### Lint Rules

Rules should be small functions that accept already-loaded metadata and return issue candidates. They should not read files, call providers, or mutate entities directly.

## Frontend Design

Processing Center:

- Add a Wiki issue row or group with counts for broken links, orphan pages, source issues, and thin content.
- Use existing issue visual language and review-facing wording.

Wiki page:

- Show page issue count.
- Show safe issue type, severity, and status for selected page.
- Keep source trace and review status visible.

## Error Design

| Error | User-facing result |
|---|---|
| Unknown space | Safe not-found envelope. |
| Cross-space page filter | Validation error. |
| Unreadable Markdown artifact | Partial failure run with `REVIEW_REQUIRED` issue. |
| Unexpected failure | Sanitized `FAILED` run when possible; no stack trace in response. |

## Implementation Boundaries

- Do not implement auto-fix suggestions beyond link insertion.
- Do not approve or publish generated pages.
- Do not introduce a new Markdown parser dependency unless tests prove the local deterministic utility is insufficient and the dependency is explicitly accepted.

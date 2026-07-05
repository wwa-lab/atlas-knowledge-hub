# User Stories: Wiki Linkify And Lint

## Status

Draft for user review. Implementation remains blocked until acceptance.

## Stories

### US-WIKI-LINKIFY-LINT-001: Accept The SDD Gate

As the product owner, I want a complete bilingual SDD set before code changes so the slice scope is reviewable.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-001.
- Spec, API guide, tasks, and traceability all state that code waits for user acceptance.

### US-WIKI-LINKIFY-LINT-002: Run Space-Scoped Wiki Maintenance

As a knowledge manager, I want to start a linkify/lint run for one Knowledge Space so generated and published pages can be checked together.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-011.
- Run response includes status, counts, updated page ids, issue ids, and safe summary.

### US-WIKI-LINKIFY-LINT-003: Add Safe Wiki Links

As an SME reviewer, I want common page titles and aliases to become Wiki links without damaging Markdown formatting.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-003 through REQ-WIKI-LINKIFY-LINT-006.
- Linkify skips protected Markdown regions and remains idempotent.

### US-WIKI-LINKIFY-LINT-004: See Link Integrity Issues

As an SME reviewer, I want broken links and orphan pages flagged so I can decide whether to edit, merge, or ignore them.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-007 and REQ-WIKI-LINKIFY-LINT-008.
- Issues are visible through safe issue APIs and UI warnings.

### US-WIKI-LINKIFY-LINT-005: See Evidence Quality Issues

As a knowledge manager, I want missing trace, stale evidence, and thin pages flagged so generated Wiki content remains reviewable and trustworthy.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-009 and REQ-WIKI-LINKIFY-LINT-010.
- Issues preserve page id, issue type, severity, evidence refs, and safe message.

### US-WIKI-LINKIFY-LINT-006: Preserve Product Safety

As an implementation owner, I want existing Wiki, ingest, Graph, and Ask surfaces to keep their trust boundaries while linkify/lint adds warnings.

Acceptance:

- Covers REQ-WIKI-LINKIFY-LINT-012 through REQ-WIKI-LINKIFY-LINT-014.
- Existing published pages remain trusted only when already published; generated pages remain `REVIEW_REQUIRED`.

## Story To Requirement Map

| Story | Requirements |
|---|---|
| US-WIKI-LINKIFY-LINT-001 | REQ-WIKI-LINKIFY-LINT-001 |
| US-WIKI-LINKIFY-LINT-002 | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-011 |
| US-WIKI-LINKIFY-LINT-003 | REQ-WIKI-LINKIFY-LINT-003, REQ-WIKI-LINKIFY-LINT-004, REQ-WIKI-LINKIFY-LINT-005, REQ-WIKI-LINKIFY-LINT-006 |
| US-WIKI-LINKIFY-LINT-004 | REQ-WIKI-LINKIFY-LINT-007, REQ-WIKI-LINKIFY-LINT-008 |
| US-WIKI-LINKIFY-LINT-005 | REQ-WIKI-LINKIFY-LINT-009, REQ-WIKI-LINKIFY-LINT-010 |
| US-WIKI-LINKIFY-LINT-006 | REQ-WIKI-LINKIFY-LINT-012, REQ-WIKI-LINKIFY-LINT-013, REQ-WIKI-LINKIFY-LINT-014 |

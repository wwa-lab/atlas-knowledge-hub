# Architecture: Answer Review Governance

Last updated: 2026-07-07

## Overview

The slice extends the existing layered Trusted Ask path with an Ask-answer governance state. It is a modular monolith change inside the Spring Boot metadata API and Vue product shell; parser, converter, vector, model, storage, and search adapters remain unchanged.

## Architecture Drivers

- Additive Ask API contract and persistence fields.
- Ask answer status must not replace evidence/document review status.
- Safe review metadata must be validated before persistence.
- Existing auth path policy and audit semantics remain unchanged.

## System Context

```text
┌────────────────────────────────────────────┐
│ Trusted Ask Users / SME Reviewers          │
└──────────────────────┬─────────────────────┘
                       │ Vue UI
                       ▼
┌────────────────────────────────────────────┐
│ Trusted Ask View                           │
│ answer status · reason · reuse hint         │
└──────────────────────┬─────────────────────┘
                       │ REST / JSON envelope
                       ▼
┌────────────────────────────────────────────┐
│ AskController                              │
│ create/read Ask · review answer action      │
├────────────────────────────────────────────┤
│ AskService                                 │
│ validation · state transition · mapping     │
├────────────────────────────────────────────┤
│ AskRunRepository · AskEvidenceRepository   │
└──────────────────────┬─────────────────────┘
                       │ JPA / Flyway
                       ▼
┌────────────────────────────────────────────┐
│ atlas.ask_run · atlas.ask_evidence         │
└────────────────────────────────────────────┘
```

## Components

| Component | Responsibility |
|---|---|
| Frontend Trusted Ask panel | Displays governance label, reason, reusable hint, and citations. |
| Frontend API client | Calls existing Ask create/read endpoints and new review-action endpoint. |
| `AskController` | Exposes review action without coupling UI to persistence. |
| `AskService` | Validates review requests, enforces status transitions, computes reuse eligibility, and preserves evidence. |
| `AskRun` | Holds answer governance state and metadata. |
| `AskMapper` | Maps domain objects to additive safe response fields. |
| Flyway migration | Adds governance metadata columns and answer-specific status check. |

## State Model

```text
REVIEW_REQUIRED
  ├─ approve with eligible answer/evidence ─▶ APPROVED
  ├─ reject with reason ───────────────────▶ REJECTED
  └─ request revision with reason ─────────▶ NEEDS_REVISION

APPROVED / REJECTED / NEEDS_REVISION may be changed by a later reviewer action,
but only APPROVED with answer text and eligible evidence can be reusable.
```

## Security And Data Boundaries

- Review text is validated with the existing safe-text style used by Ask request validation.
- API responses expose safe status/reason metadata only.
- No provider payloads, raw source documents, private paths, internal endpoints, or secrets are added.
- No external cloud calls or adapter strategy changes are introduced.

## Risks And Tradeoffs

| Risk | Mitigation |
|---|---|
| Confusing answer governance with evidence review status | Use dedicated `AnswerReviewStatus` and keep evidence `ReviewStatus`. |
| Approving no-evidence answers | Service validation rejects approvals without terminal successful status, answer text, and evidence. |
| Future answer reuse semantics not yet implemented | Expose only boolean eligibility; no reuse index is created in this slice. |

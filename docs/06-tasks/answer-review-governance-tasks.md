# Tasks: Answer Review Governance

Last updated: 2026-07-07

## Overview

Implement the accepted SDD for Ask answer governance in task ID order. Keep changes scoped to Ask answer status, metadata, UI display, tests, and traceability.

Implementation status: complete for the accepted prototype scope. Backend, frontend, and documentation evidence is recorded in `docs/00-context/answer-review-governance-traceability.md`.

## Task Breakdown

### T-ANSWER-REVIEW-GOVERNANCE-001: Add Answer Governance Domain Model

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-001, REQ-ANSWER-REVIEW-GOVERNANCE-002
- Scope: Add `AnswerReviewStatus`, extend `AskRun`, update default generated status.
- Verification: backend domain/unit tests.

### T-ANSWER-REVIEW-GOVERNANCE-002: Add Flyway Persistence Metadata

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-003
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001
- Scope: Add migration for status constraint, reason, reviewer, reviewed timestamp, and index.
- Verification: `cd backend && mvn verify`.

### T-ANSWER-REVIEW-GOVERNANCE-003: Add Review Action API

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-003, REQ-ANSWER-REVIEW-GOVERNANCE-004, REQ-ANSWER-REVIEW-GOVERNANCE-006
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001, T-ANSWER-REVIEW-GOVERNANCE-002
- Scope: Add request DTO, service method, controller route, validation, safe errors, and response mapping.
- Verification: backend service and integration/API contract tests.

### T-ANSWER-REVIEW-GOVERNANCE-004: Preserve Evidence And Existing Ask Behavior

- Status: Complete
- Owner type: backend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-005, REQ-ANSWER-REVIEW-GOVERNANCE-008
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-003
- Scope: Ensure review updates only answer governance fields and existing Ask create/read tests still pass.
- Verification: existing Ask tests plus new preservation assertions.

### T-ANSWER-REVIEW-GOVERNANCE-005: Update Frontend Types And API Client

- Status: Complete
- Owner type: frontend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-001, REQ-ANSWER-REVIEW-GOVERNANCE-007
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-003
- Scope: Add answer governance types, Ask response fields, and review action API helper.
- Verification: `cd frontend && npm run typecheck`.

### T-ANSWER-REVIEW-GOVERNANCE-006: Update Trusted Ask UI Display

- Status: Complete
- Owner type: frontend
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-004, REQ-ANSWER-REVIEW-GOVERNANCE-007
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-005
- Scope: Display governance label, reason, reviewer, reusable hint, and citations without implying unreviewed answers are trusted.
- Verification: frontend unit tests.

### T-ANSWER-REVIEW-GOVERNANCE-007: Update Traceability And Roadmaps

- Status: Complete; closeout gate passed after documentation edits
- Owner type: docs
- Priority: Must
- Requirements: REQ-ANSWER-REVIEW-GOVERNANCE-008
- Dependencies: T-ANSWER-REVIEW-GOVERNANCE-001 through T-ANSWER-REVIEW-GOVERNANCE-006
- Scope: Update traceability, slice roadmap, repo status roadmap, and verification evidence.
- Verification: SDD gate and closeout gate.

## Required Verification

- `npm run agent:check-sdd -- --slice answer-review-governance`
- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `npm run agent:closeout`
- `git diff --check`
- Focused changed-file secret/private-path/real-data scan.
- Focused changed-file network/dependency scan.

## SDD Skill Chain Evidence

Entry skill used: `atlas-sdd-generate-all`. Downstream skills applied: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, `review-doc-quality`, `architecture-review` for architecture/API/persistence awareness. Review verdict: ready for implementation with no blocking SDD issues.

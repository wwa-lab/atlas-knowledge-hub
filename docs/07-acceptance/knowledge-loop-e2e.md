# Knowledge Loop Automated E2E

## Goal

Provide an out-of-the-box command that proves the Atlas knowledge loop can run with safe sample data.

Long-term target journey:

```text
sample folder or zip
  -> upload batch
  -> convert and parse
  -> standard Markdown with source trace
  -> local or PostgreSQL-backed metadata
  -> review approval
  -> Wiki publication
  -> graph evidence
  -> model-selected Ask answer with citations
```

## Commands

```bash
npm run setup
npm run e2e:loop:mock
```

Configured mode:

```bash
cp configs/atlas.company.example.env .env
# Fill values from approved local/company config.
npm run e2e:loop:configured
```

## Acceptance IDs

| ID | Behavior | Current status |
|---|---|---|
| UAT-KLOOP-001 | Start from a clean local sample state. | Implemented by `scripts/e2e/reset-local-state.sh`. |
| UAT-KLOOP-002 | Run with mock mode and no external network dependency. | Implemented for Phase 1 frontend acceptance. |
| UAT-KLOOP-003 | Simulate folder upload and batch creation. | Implemented in `frontend/tests/e2e/acceptance/knowledge-loop.spec.ts`. |
| UAT-KLOOP-004 | Verify generated report evidence includes source trace, low confidence, and review-required state. | Implemented against the Phase 1 prototype. |
| UAT-KLOOP-005 | Verify Wiki publication uses approved Markdown only. | Phase 4 extension required. |
| UAT-KLOOP-006 | Verify graph nodes and edges point back to evidence. | Phase 4 extension required. |
| UAT-KLOOP-007 | Verify Ask answers use selected knowledge spaces, selected model, and citations. | Phase 4 extension required. |
| UAT-KLOOP-008 | Verify unapproved content is excluded from graph and Ask evidence. | Phase 4 extension required. |

## Extension Rule

Each implementation phase should extend this same journey:

- Phase 1 keeps the loop mock-only and frontend-driven.
- Phase 2 replaces mock metadata with backend API and PostgreSQL checks.
- Phase 3 verifies configured adapter contracts with mock engines first.
- Phase 4 verifies approved Wiki, graph evidence, vector/query evidence, and cited Ask answers.

Do not create a competing acceptance workflow for the same product journey.

# Automated Acceptance

Atlas acceptance is designed for one-command local verification before company deployment.

The acceptance layer is separate from ordinary unit and component tests:

- Unit/component tests prove small implementation behavior.
- Playwright smoke tests prove critical UI paths.
- Automated acceptance tests prove product journeys with sample data and adapter modes.

## Modes

| Mode | Purpose | Command |
|---|---|---|
| `mock` | External development and demo without secrets, backend services, or company systems. | `npm run e2e:loop:mock` |
| `configured` | Company or local integration environment using configured backend, database, storage, and model adapters. | `npm run e2e:loop:configured` |

## Safety Rules

- Use mock/sample data only.
- Do not commit real company documents, screenshots, credentials, logs, exports, or private paths.
- Keep provider credentials in `.env` or an approved secret manager.
- Keep graph and Ask checks review-aware: unapproved content must not become graph or Ask evidence.

## Current Coverage

The current suite provides a Phase 1 automated acceptance path over the mock frontend shell:

1. Open the Knowledge Space library.
2. Enter the IBM i Modernization space.
3. Simulate folder upload and batch creation.
4. Verify report evidence such as `source_trace`, low confidence, and `REVIEW_REQUIRED`.
5. Verify the graph surface renders.
6. Verify global chat can target a knowledge space.

Phase 2-4 work should extend the same command rather than adding a second workflow.

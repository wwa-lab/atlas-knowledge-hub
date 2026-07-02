# Frontend

Phase 1 frontend implementation for Atlas Knowledge Hub.

The current Phase 1 FE baseline lives in `public/atlas-prototype.html`.

Phase 1 uses the current FE prototype as the fidelity baseline:

- `public/atlas-prototype.html` preserves the current FE experience.
- `../prototypes/index.html` mirrors the same HTML for direct static review.
- `src/App.vue` hosts that prototype inside the Vue application so the Phase 1 app looks and behaves like the current FE baseline before component extraction begins.

Future Phase 1 work should incrementally extract this prototype into Vue components while keeping visual and interaction parity with `public/atlas-prototype.html`. If the FE changes first, update the static mirror and SDD docs in the same slice.

## Stack

- Vue 3.
- Vite.
- TypeScript.
- Vitest for unit/component tests.
- Playwright for critical flow smoke tests.

## Setup

```bash
npm ci
npm run dev    # Vite dev server on http://127.0.0.1:5173
```

The `prepare` script automatically runs `../scripts/setup-hooks.sh` after `npm install`, which configures pre-commit and pre-push git hooks.

## Commands

```bash
npm run dev                # Start Vite dev server
npm run lint              # ESLint and Prettier check
npm run lint:fix          # ESLint and Prettier auto-fix
npm run format            # Format with Prettier
npm run typecheck         # Vue TSC type check
npm run test              # Vitest unit/component tests
npm run test:coverage     # Vitest with coverage report
npm run build             # Build (runs lint → typecheck → vite build)
npm run e2e               # Playwright critical-flow E2E tests
```

## Code Quality

All code must pass:
- **Linting**: `npm run lint` (ESLint + Prettier)
- **Type checking**: `npm run typecheck` (vue-tsc)
- **Tests**: `npm run test` with ≥80% coverage
- **Build**: `npm run build` succeeds

Git hooks enforce these automatically:
- **Pre-commit**: `lint:fix`, `format`, `test` (then stages changes)
- **Pre-push**: `typecheck`, `test`

See [FRONTEND_CODING_STANDARD.md](../docs/FRONTEND_CODING_STANDARD.md) for component patterns and testing standards.

All data in Phase 1 is mock-only. Do not add external API calls, real credentials, production auth, a production database, or concrete parser/vector/storage/model integrations here.

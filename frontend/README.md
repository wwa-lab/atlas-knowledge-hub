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

## Commands

```bash
npm install
npm run dev
npm run typecheck
npm run test:coverage
npm run build
npm run e2e
```

All data in Phase 1 is mock-only. Do not add external API calls, real credentials, production auth, a production database, or concrete parser/vector/storage/model integrations here.

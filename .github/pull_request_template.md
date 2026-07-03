## Slice and Scope

**Slice**: [e.g., knowledge-space-component-extraction]  
**Task ID(s)**: [e.g., T-KS-031, T-KS-032]  
**Goal**: [User-facing outcome, e.g., "Extract Home card component with mock data and tests"]

## Summary of Changes

- [Briefly describe what changed and why]
- [List key files modified]

## SDD Alignment

- [ ] Spec (`docs/03-spec/`) updated or verified to match implementation
- [ ] Design (`docs/05-design/`) updated or verified (if applicable)
- [ ] Tasks (`docs/06-tasks/`) reflect the work completed
- [ ] English and Simplified Chinese SDD documents are synchronized (if created/updated)

## Verification

- [ ] `npm run lint` passes
- [ ] `npm run typecheck` passes
- [ ] `npm run test` passes with ≥80% coverage
- [ ] `npm run build` succeeds
- [ ] `npm run e2e` passes (critical flows only for Phase 1)
- [ ] Code review checklist: scope, spec alignment, security, trace/confidence/review status, adapter boundaries

## Testing Plan

- Unit/Component tests added for: [e.g., "KnowledgeSpaceCard component state mapping"]
- E2E tests added for: [e.g., "Home navigation to Space detail via card click"]
- Manual testing: [e.g., "Verify dialogue input state persists across language/theme toggles"]

## Security & Data

- [ ] No real company data, credentials, or private paths introduced
- [ ] No external network calls added
- [ ] Input validation in place where applicable
- [ ] Source trace, confidence, and review status preserved where relevant

## Residual Risks or Open Questions

- [List any known limitations, deferred work, or uncertainty]
- [e.g., "E2E test coverage limited to smoke tests; full flow verification deferred to Phase 2"]

---

For detailed standards, see [DEVELOPMENT_STANDARDS.md](../DEVELOPMENT_STANDARDS.md), [FRONTEND_CODING_STANDARD.md](../docs/FRONTEND_CODING_STANDARD.md), [BACKEND_CODING_STANDARD.md](../docs/BACKEND_CODING_STANDARD.md), and [CLAUDE.md](../CLAUDE.md).

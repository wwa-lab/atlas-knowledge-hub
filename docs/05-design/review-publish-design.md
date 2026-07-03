# Detailed Design: Review Publish

## Status

Draft. Derived from the Review Publish architecture.

## Design Scope

This design covers the full-stack contract for review queue hardening, SME review state transitions, publish eligibility, Wiki page metadata publication, and FE status rendering. It does not implement graph extraction, Ask/RAG, production auth, or direct adapter execution.

## Module Design

### Review Queue Module

- Derives queue counts from file items, source chunks, and review statuses.
- Returns blocked categories: parser failure, OCR required, low confidence, missing trace, and LLM-generated review-required.
- Returns bounded representative item metadata for each queue using safe fields only: file id, file status, review status, confidence, and trace-presence flag. Queue representative items must not expose raw source paths, raw document content, private absolute paths, or secrets.
- Includes publish-ready candidates only when they satisfy the eligibility inputs except the final publish action.

### Review Decision Module

- Reuses append-only review record behavior.
- Keeps `PUBLISHED` outside review action results.
- Allows future chunk-level expansion without changing file-level history semantics.

### Publish Module

- Loads the candidate file and source trace metadata.
- Applies all eligibility rules before mutation.
- Creates or updates a Wiki page metadata record.
- Sets published metadata to `PUBLISHED`.
- Uses one transaction so failed eligibility or conflicts leave no partial result.

### Frontend Module

- Processing Center shows blocked counts and ready-to-publish count.
- Review actions are visible only as allowed workflow affordances; they must not imply production RBAC.
- Wiki surfaces published status and source/confidence metadata.
- Global Chat trust copy remains clear that only reviewed, source-traced, publishable assets are used.

## API / Interface Design

The API guide in `docs/05-design/contracts/review-publish-API_IMPLEMENTATION_GUIDE.md` is required for this slice.

## Validation And Error Handling

| Case | Expected Handling |
|---|---|
| Candidate not approved | Reject with user-safe validation/conflict response. |
| Missing source trace | Reject and name `sourceTrace` as the blocked field. |
| Absolute or traversal path | Reject; never echo private absolute paths. |
| Missing confidence | Reject and leave metadata unchanged. |
| Duplicate publish | Return existing published metadata or conflict based on implementation choice documented in tests. |

## UI / User Flow Design

1. User opens Processing Center.
2. User sees blocked queues and ready-to-publish content.
3. Reviewer resolves review-required items through review actions.
4. Admin publishes eligible approved content.
5. Wiki tab reflects the published page metadata.
6. Graph/Ask copy continues to indicate downstream consumption only.

## Testing Considerations

- Backend unit tests for eligibility and state transitions.
- Backend integration/API contract tests for queues, review history, publish success, and blocked publish.
- Queue API contract tests must assert both queue counts/categories and representative item shape, including that missing-trace examples carry `hasSourceTrace=false`.
- Frontend tests for Processing Center counts, action states, published Wiki display, and trust copy.
- E2E for approve-to-publish happy path and blocked missing-trace path.
- Scans for no direct adapter calls, no new network dependencies, no secrets, and no private paths.

## Risks / Tradeoffs

- Single-item publish keeps implementation reviewable; bulk publish is deferred unless accepted.
- File-level publish may be enough for MVP but chunk-level publish may be needed for fine-grained SME workflows.
- Production RBAC will need later hardening before external exposure.

## Open Questions

- Should duplicate publish be idempotent or conflict?
- Should publish update `file_item.review_status` to `PUBLISHED`, or only the `wiki_page.review_status`?

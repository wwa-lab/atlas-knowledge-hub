# Detailed Design: Wiki Ingest v0

## Status

Draft for user acceptance. Product code remains blocked until acceptance.

## Source Architecture

- `docs/03-spec/wiki-ingest-v0-spec.md`
- `docs/04-architecture/wiki-ingest-v0-architecture.md`
- `docs/04-architecture/wiki-ingest-v0-data-flow.md`
- `docs/04-architecture/wiki-ingest-v0-data-model.md`

## Grounded Existing Code

- `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:115` creates published Wiki metadata with source chunk refs.
- `backend/src/main/java/com/atlas/metadata/service/ReviewPublishService.java:138` publishes approved Markdown files and blocks missing source trace at `ReviewPublishService.java:144`.
- `backend/src/main/java/com/atlas/metadata/service/IngestionService.java:58` ingests PDF/ZIP uploads and creates parser runs, but its file metadata defaults to `REVIEW_REQUIRED` at `IngestionService.java:168`.
- `frontend/src/types.ts:365` defines the current `ApiWikiPage` shape with slug, refs, source mode, refresh policy, confidence, and published review status.

## Design Assumptions

- v0 deterministic generation is the only generation mode in this slice.
- Model-assisted generation is future work and must use ModelAdapter when introduced.
- v0 writes generated Markdown artifacts with deterministic safe summaries and safe source/chunk labels.
- Existing published Wiki read behavior remains the trusted default.
- Draft generated candidates can be exposed only when the API/UI explicitly requests generated or review-required pages.

## Design Scope

In scope:

- Backend Wiki ingest service and API contracts.
- Approved source chunk selection.
- Candidate builder, slug derivation, idempotent merge, safe run/log/issue records.
- Optional frontend status display for generated review-required candidates.
- Backend, frontend, E2E, and safety verification.

Out of scope:

- Parser/converter runtime changes, linkify/lint, review approval actions, refresh/retract, production RBAC, connector sync, model-assisted generation, external provider calls.

## Module Design

### Backend Wiki Ingest API

- Add a space-scoped start-run endpoint.
- Validate requested mode and space.
- Return an Atlas envelope with a safe run response.
- Keep existing published Wiki list/detail behavior unchanged.

### Backend Wiki Ingest Service

Responsibilities:

- Create a `wiki_generation_run` in `REQUESTED` then `RUNNING`.
- Load eligible chunks for a space.
- Build deterministic candidates.
- Merge candidates by slug.
- Record safe logs and issues.
- Complete run with `SUCCEEDED`, `PARTIAL_FAILED`, or `FAILED`.

### Input Selector

Rules:

- Include only `APPROVED` chunks.
- Require source file and page or section locator when available.
- Exclude failed, unsupported, OCR-required, review-required, and need-fix evidence.
- Record exclusion counts, not raw text.

### Candidate Builder

Default deterministic candidate:

- title from source section/topic label.
- slug from normalized title.
- page type `TOPIC`.
- generated Markdown artifact with deterministic safe summary.
- source mode `AUTO_GENERATED`.
- refresh policy `ON_SOURCE_CHANGE`.
- review status `REVIEW_REQUIRED`.
- confidence as minimum included chunk confidence.
- refs as safe file/chunk IDs and locators.

### Merge Policy

| Case | Behavior |
|---|---|
| New slug | Create generated review-required candidate. |
| Existing generated review-required slug | Merge refs and update safe summary. |
| Existing trusted/published `PUBLISHED_FILE` slug | Preserve page and record safe issue. |
| Existing slug in another space | No conflict. |

### Future Model Assistance

- v0 rejects or disables model-assisted requests.
- If model assistance is later enabled, service calls ModelAdapter only.
- Raw prompt/provider payload must not be stored in run/log/page records.
- Model-assisted output remains `REVIEW_REQUIRED`.

## API / Interface Design

The API guide is `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`.

Primary endpoints:

- `POST /api/spaces/{spaceId}/wiki-ingest-runs`
- `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}`
- `GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true`

## Data Design

- Prefer reusing existing Wiki data model tables.
- Add migration only if implementation needs fields not present in V10.
- Candidate page records must include safe refs, source mode, refresh policy, confidence, and review status.
- Run/log/issue records must be safe summaries only.

## UI / User Flow Design

1. Knowledge manager starts a run from an accepted UI affordance or API client.
2. UI receives a safe run summary.
3. Wiki or Processing Center shows generated/review-required candidate status.
4. Existing published Wiki pages remain the trusted default view.
5. User can inspect refs/counts but cannot approve generated candidates in this slice.

## Validation and Error Handling

| Input | Rule |
|---|---|
| `spaceId` | Must exist. |
| mode | v0 accepts `deterministic`; model-assisted mode is rejected or disabled in this slice. |
| eligible chunks | Must be approved and traceable. |
| slug | Lowercase URL-safe, unique per space. |
| generated page status | Always `REVIEW_REQUIRED` in v0. |

Error behavior:

- no eligible evidence -> safe successful run with zero candidates.
- trusted slug collision -> safe issue, not overwrite.
- unexpected failure -> `FAILED` with sanitized safe error.

## Testing Considerations

- Backend contract tests for start/read run.
- Service tests for input eligibility, candidate metadata, idempotency, collision handling, and review-required defaulting.
- Repository tests for run/log/issue persistence and no duplicate pages on rerun.
- Frontend tests for draft/review-required display if UI is touched.
- E2E regression for existing Wiki, Graph, Ask, and publish flows.
- Safety scans for secrets/private paths and new network/dependency calls.

## Risks / Design Tradeoffs

| Risk | Mitigation |
|---|---|
| Deterministic pages are thin | Keep candidates review-required and label v0 clearly. |
| Draft candidates confuse users | Existing published view remains trusted default; generated status is explicit. |
| Existing dirty worktree contains prior slice changes | Implementation must touch only accepted wiki-ingest files after acceptance. |

## Resolved Decisions

- v0 creates `TOPIC` pages only.
- v0 writes generated Markdown artifacts with deterministic safe summaries.
- v0 aggregates confidence using the minimum included chunk confidence.

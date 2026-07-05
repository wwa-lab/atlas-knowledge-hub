# User Stories: Wiki Ingest v0

## Status

Draft for user acceptance.

## Story Map

| Story ID | Requirement IDs | Title |
|---|---|---|
| US-WIKI-INGEST-V0-001 | REQ-WIKI-INGEST-V0-001 | Accept SDD before implementation |
| US-WIKI-INGEST-V0-002 | REQ-WIKI-INGEST-V0-002, 003, 007 | Generate review-required Wiki candidates |
| US-WIKI-INGEST-V0-003 | REQ-WIKI-INGEST-V0-004, 012, 013 | Preserve adapter and safety boundaries |
| US-WIKI-INGEST-V0-004 | REQ-WIKI-INGEST-V0-005, 006 | Merge candidates idempotently |
| US-WIKI-INGEST-V0-005 | REQ-WIKI-INGEST-V0-008, 009, 010 | Inspect safe ingest run evidence |
| US-WIKI-INGEST-V0-006 | REQ-WIKI-INGEST-V0-011, 014 | Display and verify ingest status |

## User Story US-WIKI-INGEST-V0-001

**Title:** Accept SDD before implementation

**Story:**  
As a product owner,  
I want the Wiki ingest scope and tasks accepted before code changes,  
so that the team does not accidentally build linkify, review gate, connector, or production governance work in the wrong slice.

### Acceptance Criteria

1. **Given** this slice is selected  
   **When** the agent prepares delivery artifacts  
   **Then** all bilingual SDD files exist with matching REQ, US, and T IDs.

2. **Given** the SDD is not accepted  
   **When** implementation is considered  
   **Then** product code changes remain blocked.

### Notes / Assumptions

- `wiki-data-model` is already complete and is the prerequisite foundation.

### Dependencies

- `docs/00-context/wiki-data-model-traceability.md`

### Out of Scope

- Product code implementation before user acceptance.

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-002

**Title:** Generate review-required Wiki candidates

**Story:**  
As a knowledge manager,  
I want Atlas to create draft Wiki candidates from approved chunks,  
so that reviewed source evidence can start becoming durable Wiki pages.

### Acceptance Criteria

1. **Given** approved chunks with source trace exist  
   **When** a Wiki ingest run starts  
   **Then** Atlas creates or updates Wiki candidates with slug, title, source refs, chunk refs, confidence, and `REVIEW_REQUIRED`.

2. **Given** a chunk is not approved or lacks trace  
   **When** the run selects inputs  
   **Then** that chunk is excluded and counted in the safe run summary.

3. **Given** generated body text is produced  
   **When** the page is stored  
   **Then** Atlas writes a generated Markdown artifact with a safe relative path and the page remains untrusted until a later review-gate slice approves it.

### Notes / Assumptions

- Deterministic candidate text is enough for v0.

### Dependencies

- Source chunk review state and Wiki data model fields.

### Out of Scope

- Trusted publish, editorial workflow, linkify, lint, and refresh/retract.

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-003

**Title:** Preserve adapter and safety boundaries

**Story:**  
As an architect,  
I want v0 ingest to stay deterministic and keep any future model-assisted behavior behind Atlas adapters,  
so that the product remains provider-neutral and safe for internal data boundaries.

### Acceptance Criteria

1. **Given** deterministic mode is enabled  
   **When** the run generates candidates  
   **Then** no model, vector, parser, converter, storage, or search engine is called directly.

2. **Given** future model-assisted mode is requested  
   **When** v0 evaluates the request  
   **Then** the mode is rejected or disabled for this slice, with the future requirement documented behind ModelAdapter.

3. **Given** run logs are written  
   **When** a user inspects them  
   **Then** no raw prompt, source text, provider response, secret, or private path is exposed.

### Notes / Assumptions

- v0 uses deterministic generation only.

### Dependencies

- Future ModelAdapter boundary when a later slice enables model-assisted generation.

### Out of Scope

- New provider setup or production secret manager.

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-004

**Title:** Merge candidates idempotently

**Story:**  
As a delivery lead,  
I want repeated ingest runs over the same evidence to update the same Wiki slug,  
so that retries and reruns do not create duplicate pages.

### Acceptance Criteria

1. **Given** a candidate slug already exists in the same space  
   **When** the run emits the same slug  
   **Then** Atlas updates or merges the existing review-required candidate.

2. **Given** an existing page is `PUBLISHED_FILE` and trusted  
   **When** a generated candidate targets a colliding slug  
   **Then** Atlas preserves the trusted page and records a safe issue or conflict outcome.

3. **Given** the same approved chunk set is processed twice  
   **When** both runs finish  
   **Then** the Wiki page count does not increase after the second run.

### Notes / Assumptions

- Generated slugs are space scoped.

### Dependencies

- `wiki_page` `(space_id, slug)` uniqueness.

### Out of Scope

- Manual conflict-resolution UI.

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-005

**Title:** Inspect safe ingest run evidence

**Story:**  
As an SME reviewer,  
I want to see safe run summaries, counts, and issues,  
so that I can understand why a candidate page exists or why it failed without seeing sensitive payloads.

### Acceptance Criteria

1. **Given** a run succeeds  
   **When** run detail is requested  
   **Then** it shows created/updated page IDs, input reference counts, excluded counts, and safe summary.

2. **Given** a run partially fails  
   **When** run detail is requested  
   **Then** it shows `PARTIAL_FAILED` and safe error summaries only.

3. **Given** run logs exist  
   **When** they are listed  
   **Then** they include safe lifecycle events and no raw source content.

### Notes / Assumptions

- Existing `wiki_generation_run` and `wiki_log_entry` are sufficient for v0.

### Dependencies

- Wiki data model support tables.

### Out of Scope

- Long-term audit retention policy.

### Open Questions

- None.

## User Story US-WIKI-INGEST-V0-006

**Title:** Display and verify ingest status

**Story:**  
As a knowledge user,  
I want generated candidates to be visibly marked as review-required,  
so that I do not mistake draft Wiki content for trusted published knowledge.

### Acceptance Criteria

1. **Given** generated candidates exist  
   **When** the Wiki view renders them  
   **Then** they are visibly marked as generated and review-required.

2. **Given** no candidates exist  
   **When** the UI loads  
   **Then** existing API-backed Wiki pages and sample-safe fallback behavior remain intact.

3. **Given** verification runs  
   **When** the slice is closed  
   **Then** backend, frontend, E2E, safety, and diff checks are reported or explicitly skipped with reasons.

### Notes / Assumptions

- UI changes are status display only; no editor or review action is introduced in v0.

### Dependencies

- API run and page metadata responses.

### Out of Scope

- Review-gate actions and page editor.

### Open Questions

- None.

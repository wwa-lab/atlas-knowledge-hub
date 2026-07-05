# Data Flow: Wiki Ingest v0

## Status

Draft for user acceptance.

## End-To-End Flow

```text
┌──────────────┐
│ User/API     │
│ start run    │
└──────┬───────┘
       ▼
┌────────────────────────────┐
│ Wiki Ingest API             │
│ validate space + request    │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Input Selection             │
│ approved traced chunks only │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Candidate Builder           │
│ deterministic by default    │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Slug Merge                  │
│ create/update/conflict      │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Evidence Records            │
│ run + log + issue metadata  │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Vue Status Display          │
│ generated review-required   │
└────────────────────────────┘
```

## Input Selection Flow

| Input | Rule | Output |
|---|---|---|
| Space id | Must resolve to an existing Knowledge Space. | Run scoped to one space. |
| Source chunks | Must belong to selected space and be `APPROVED`. | Eligible evidence set. |
| Missing trace | Excluded. | Exclusion count and safe reason. |
| Non-approved chunks | Excluded. | Exclusion count and safe reason. |
| Failed/unsupported/OCR-required files | Excluded. | Exclusion count and safe reason. |

## Candidate Build Flow

```text
Approved chunks
  -> group by deterministic topic key
  -> derive title and slug
  -> create generated Markdown artifact with safe summary
  -> collect sourceRefs and chunkRefs
  -> aggregate confidence
  -> set sourceMode=AUTO_GENERATED
  -> set reviewStatus=REVIEW_REQUIRED
  -> set refreshPolicy=ON_SOURCE_CHANGE
```

## Merge Flow

| Existing page state | Action |
|---|---|
| No same slug | Create review-required candidate. |
| Same slug, `AUTO_GENERATED`, review-required | Merge refs and increment/update version per accepted implementation plan. |
| Same slug, `PUBLISHED_FILE` or trusted published page | Do not overwrite; record safe conflict issue. |
| Same slug in another space | No conflict; slug is space scoped. |

## Run State Flow

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

## Data Object Lifecycle

| Object | Created/Updated | Notes |
|---|---|---|
| `wiki_generation_run` | Created at run start; updated at finish. | Safe summary and counts only. |
| `wiki_page` | Created or updated for generated candidates. | Always review-required in v0. |
| `wiki_log_entry` | Appended for safe lifecycle events. | No raw source text or provider payload. |
| `wiki_page_issue` | Created for conflicts or excluded evidence requiring review. | Safe evidence refs only. |

## Safety Filters

- Strip raw source text from run/log/error summaries.
- Store safe IDs, labels, counts, page/section locators, and relative artifact refs only.
- Never store raw secret, raw prompt, raw provider response, private absolute path, or stack trace.

## Adapter Flow

Default deterministic mode:

```text
Wiki Ingest Service -> local deterministic candidate builder -> repositories
```

Future model-assisted mode, out of scope for v0:

```text
Wiki Ingest Service -> ModelAdapter -> safe summarized output -> review-required candidate
```

Parser, converter, model, vector, storage, and search engines are not invoked directly by this slice.

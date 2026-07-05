# Data Flow: Wiki Data Model

## Status

Draft for user acceptance.

## Overview

This document describes how Wiki metadata moves through the `wiki-data-model` slice. The flows are metadata-only and mock/sample-safe; no raw document ingestion, linkification, linting, model generation, or external provider call occurs in this slice.

## Flow 1: Existing Publish To Extended Wiki Page

```text
Approved file metadata
  │
  │ POST /api/files/{fileId}/publish
  ▼
Review Publish Service
  │ validates approved status, markdown path, confidence, source chunks
  │ preserves source_document_ids compatibility
  ▼
WikiPage entity
  │ sets defaults:
  │ slug, pageType, aliases, refs, links, version, sourceMode, refreshPolicy
  ▼
wiki_page table
  │
  ▼
WikiPageResponse with legacy + new fields
```

Field mapping:

| Source | Target | Rule |
|---|---|---|
| `FileItem.id` | `sourceDocumentIds[0]` | Preserve existing compatibility behavior. |
| `FileItem.markdownPath` | `markdownPath`, default slug source | Must remain safe relative path. |
| `SourceChunk.id` | `chunkRefs[]` | Store safe chunk IDs only. |
| Publish title | `title` | Existing request field. |
| Publish owner | `owner` | Existing request field. |
| Existing confidence | `confidence` | Preserve value; no recalculation. |
| Publish operation | `sourceMode=PUBLISHED_FILE` | Default for review-publish-created pages. |

## Flow 2: Space-Scoped Wiki Reads

```text
Vue Wiki tab or API test
  │ GET /api/spaces/{spaceId}/wiki-pages
  │ GET /api/spaces/{spaceId}/wiki-pages/by-slug/{slug}
  ▼
Wiki Metadata API
  │ validates space scope
  ▼
Wiki Metadata Service
  │ queries by space/status or space/slug
  ▼
DTO Mapper
  │ returns safe refs, links, version, source mode, refresh policy
  ▼
ApiEnvelope response
```

Error paths:

| Case | Result |
|---|---|
| Unknown space | `NOT_FOUND` safe envelope. |
| Unknown slug in known space | `NOT_FOUND` safe envelope. |
| Slug exists in another space | `NOT_FOUND` for current space. |
| Non-published page under trusted read contract | Hidden unless endpoint explicitly supports it in a future accepted slice. |

## Flow 3: Folder Reads

```text
GET /api/spaces/{spaceId}/wiki-folders
  ▼
WikiFolderRepository by space
  ▼
Service builds parent-child DTO ordering
  ▼
Folder list/tree response
```

Rules:

- Folder read is scoped by `spaceId`.
- `parentFolderId` may be null.
- Pages without folders remain readable.
- No permission inheritance or folder write workflow is introduced.

## Flow 4: Generation Runs, Logs, And Issues

```text
Delivery lead / tests
  │
  ├─ GET /api/spaces/{spaceId}/wiki-generation-runs
  ├─ GET /api/wiki-pages/{wikiPageId}/logs
  └─ GET /api/wiki-pages/{wikiPageId}/issues
        ▼
Wiki Metadata Service
        │ reads bounded safe records
        ▼
ApiEnvelope response
```

Safety filters:

- Return IDs, enum values, counts, timestamps, safe summaries, and evidence IDs.
- Do not return raw source text, raw prompts, provider payloads, stack traces, private paths, or secrets.
- No rule engine executes while reading issues.

## Flow 5: Vue Display With Fallback

```text
Space detail load
  │ calls listWikiPages(spaceId)
  ▼
wikiPages state
  ├─ non-empty API pages -> map to product Wiki pages with new metadata
  └─ empty/failure -> use deterministic sample-safe fallback pages
  ▼
Wiki tab rendering
  │ shows slug, type, aliases, refs, link counts, version, source mode, policy
  ▼
Graph/Ask flows continue using existing approved/published evidence
```

Regression-sensitive behavior:

- Existing `data-testid="wiki-pages"` and API-backed Wiki/Graph/Ask E2E expectations must continue to pass or be updated according to accepted tasks.
- Fallback data must not contain real company content, private paths, raw secrets, or provider logs.

## State Transitions

Generation runs are stored for future workflows:

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

Issue lifecycle:

```text
OPEN -> ACKNOWLEDGED -> RESOLVED
OPEN -> IGNORED
ACKNOWLEDGED -> RESOLVED
```

This slice reads and stores these states; it does not evaluate or transition them through background workflows.

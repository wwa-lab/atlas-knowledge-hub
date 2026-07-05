# Wiki Linkify And Lint API Implementation Guide

## Status

Draft for user review. Required before backend implementation.

## Base Contract

- Backend stack: Spring Boot.
- Envelope: `ApiEnvelope<T>`.
- Auth model: existing local/API path; production auth/RBAC remains out of scope.
- Data safety: safe metadata only.

## Endpoints

| Operation | Method | Endpoint |
|---|---|---|
| Start linkify/lint run | POST | `/api/spaces/{spaceId}/wiki-linkify-lint-runs` |
| Get run | GET | `/api/spaces/{spaceId}/wiki-generation-runs/{runId}` |
| List space issues | GET | `/api/spaces/{spaceId}/wiki-page-issues` |
| List page issues | GET | `/api/wiki-pages/{wikiPageId}/issues` |
| Review queues | GET | `/api/spaces/{spaceId}/review-queues` |

## `CreateWikiLinkifyLintRunRequest`

| Field | Type | Required | Rule |
|---|---|---:|---|
| `pageIds` | string[] | No | Optional page scope; every page must belong to `spaceId`. |
| `requestedBy` | string | No | Safe actor label. |
| `dryRun` | boolean | No | Default false; if true, return summary without writing pages or issues. |
| `linkify` | boolean | No | Default true. |
| `lint` | boolean | No | Default true. |

## `WikiLinkifyLintRunResponse`

| Field | Type | Description |
|---|---|---|
| `runId` | string | Run id. |
| `spaceId` | string | Owning space. |
| `status` | string | `SUCCEEDED`, `PARTIAL_FAILED`, or `FAILED`. |
| `mode` | string | `linkify-lint`. |
| `scannedPageCount` | number | Pages evaluated. |
| `updatedPageIds` | string[] | Pages changed. |
| `issueIds` | string[] | Issues opened or refreshed. |
| `insertedLinkCount` | number | Count of links inserted. |
| `brokenLinkCount` | number | Broken link issues. |
| `orphanPageCount` | number | Orphan issues. |
| `sourceIssueCount` | number | Missing or stale source issues. |
| `thinContentCount` | number | Thin content issues. |
| `safeSummary` | string | Safe counts-only summary. |
| `safeError` | string or null | Sanitized error. |
| `startedAt` | string or null | ISO timestamp. |
| `finishedAt` | string or null | ISO timestamp. |

## Example Request

```json
{
  "pageIds": ["wiki-auto-modernization-scope"],
  "requestedBy": "knowledge-manager",
  "dryRun": false,
  "linkify": true,
  "lint": true
}
```

## Example Response

```json
{
  "success": true,
  "data": {
    "runId": "wiki-linkify-lint-run-001",
    "spaceId": "space-ibm-i-modernization",
    "status": "SUCCEEDED",
    "mode": "linkify-lint",
    "scannedPageCount": 4,
    "updatedPageIds": ["wiki-auto-modernization-scope"],
    "issueIds": ["wiki-issue-broken-link-001"],
    "insertedLinkCount": 3,
    "brokenLinkCount": 1,
    "orphanPageCount": 0,
    "sourceIssueCount": 0,
    "thinContentCount": 0,
    "safeSummary": "Scanned 4 Wiki pages, inserted 3 links, and recorded 1 issue.",
    "safeError": null,
    "startedAt": "2026-07-05T00:00:00Z",
    "finishedAt": "2026-07-05T00:00:01Z"
  },
  "error": null,
  "meta": null
}
```

## Validation

- `spaceId` must exist.
- `pageIds`, when present, must all belong to the selected space.
- At least one of `linkify` or `lint` must be true.
- Unknown run ids and cross-space run ids return safe not found responses.

## Side Effects

- Updates `wiki_page.in_links`, `wiki_page.out_links`, `version`, and `last_updated` when changed.
- May rewrite Markdown artifacts through safe storage when linkify is enabled.
- Writes `wiki_generation_run` with mode `linkify-lint`.
- Appends safe `wiki_log_entry` events.
- Opens, refreshes, resolves, or replaces safe `wiki_page_issue` records.

## Testing Contract

- Contract tests cover start run, cross-space validation, dry run, and safe run read.
- Service tests cover protected Markdown regions, idempotency, broken links, orphan pages, missing refs, stale refs, and thin content.
- Response assertions verify no raw Markdown body, secret, private path, prompt, provider payload, or stack trace is returned.

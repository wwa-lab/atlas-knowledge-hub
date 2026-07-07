# Data Model: Ask Session Citations

## Overview

The data model adds `ask_session`, adds nullable `session_id` ownership to `ask_run`, and enriches `ask_evidence` as the answer citation snapshot table.

## Entity Relationship

```text
ask_session 1 ── N ask_run 1 ── N ask_evidence
space       1 ── N ask_session
source_chunk 1 ── N ask_evidence
file_item    1 ── N ask_evidence
```

## Entity Definitions

### ask_session

| Field | Type | Required | Description |
|---|---|---|---|
| id | String | Yes | Stable session id. |
| space_id | String | Yes | Owning Knowledge Space. |
| title | String | Yes | Safe display title. |
| created_by | String | Yes | Safe actor label. |
| created_at | Timestamp | Yes | Creation time. |
| updated_at | Timestamp | Yes | Last run/session update time. |

### ask_run

New field:

| Field | Type | Required | Description |
|---|---|---|---|
| session_id | String | No for legacy rows, Yes for new rows | Owning Ask session. |

Existing fields remain unchanged: question, status, review policy, mode, requested by, answer, answer confidence, answer review status, model run id, safe message, created/completed timestamps.

### ask_evidence

New fields:

| Field | Type | Required | Description |
|---|---|---|---|
| citation_id | String | Yes | Stable citation id exposed to clients; defaults to evidence id for compatibility. |
| evidence_label | String | Yes | Safe display label derived from source file/page/section. |
| source_locator | String | Yes | Safe page/section/chunk locator. |
| citation_status | String | Yes | `ELIGIBLE`, `REVIEW_REQUIRED`, `LOW_CONFIDENCE`, or `MISSING_SOURCE_TRACE`. |
| review_eligible | Boolean | Yes | True only when evidence can support trusted inspection. |
| excluded_reason | String | No | Safe reason when not eligible. |

Existing fields remain unchanged: source chunk id, file item id, source file, page, section, review status, confidence, vector item key, score, created at.

## State Rules

| Condition | citationStatus | reviewEligible | excludedReason |
|---|---|---|---|
| Review status is APPROVED or PUBLISHED and source trace exists | ELIGIBLE | true | null |
| Review status is REVIEW_REQUIRED | REVIEW_REQUIRED | false | `Evidence requires review.` |
| Confidence is below accepted Ask threshold when available | LOW_CONFIDENCE | false | `Evidence confidence is low.` |
| Source chunk/file trace is missing | MISSING_SOURCE_TRACE | false | `Source trace is missing.` |

## Migration

Use additive Flyway migration `V15__ask_session_citations.sql`.

## Compatibility

- Existing `ask_run` rows may have null `session_id`; service-created rows after the migration must have a session.
- Existing `ask_evidence` rows receive safe default values through migration.

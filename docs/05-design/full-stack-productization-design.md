# Design: Full-Stack Productization

## Overview

The P0 UI becomes the first screen of the Vue app. It should feel like an operational knowledge workspace, not a marketing page or a static prototype host. The design keeps the current Atlas visual direction but replaces iframe/mock-first behavior with real API state.

## View Model

| Area | Design |
|---|---|
| Space list | Left rail or top section with API-backed cards and status. |
| Space detail | Selected space header with owner/status/counts and tabs. |
| Documents | Sample upload action, batch list, file list, selected file chunks/source trace. |
| Review | Queue summary, selected file review action, review result status. |
| Wiki | Published Wiki page list and selected page metadata. |
| Graph | Existing API-backed graph panel, scoped to selected space, with evidence detail. |
| Ask | Question input, submit button, answer status, answer text, citations. |
| Coming soon | Disabled buttons for production upload, auth/member/admin actions, real provider setup, and unsupported prototype-only affordances. |

## Interaction Rules

- Space selection drives all downstream tabs.
- Sample upload is enabled only when a space is selected and no create request is running.
- Approve is enabled only when a selected file has chunks and is not already approved.
- Publish is enabled only when selected file review status is `APPROVED`.
- Graph/Ask refresh buttons are enabled after publish and when chunk ids exist.
- Ask submit is enabled only when a non-empty question and selected space exist.

## API State Design

Use one explicit state per domain:

```text
{ loading: boolean, error: string, data: T }
```

Domain states:

- spaces
- selected space
- batches
- files
- chunks
- review queues
- wiki pages
- graph view/detail
- ask run

## Stable Test Selectors

| Selector | Purpose |
|---|---|
| `data-testid="space-list"` | API-backed space list. |
| `data-testid="space-card"` | Space selection. |
| `data-testid="create-sample-batch"` | Metadata-only upload trigger. |
| `data-testid="batch-list"` | Batch list. |
| `data-testid="file-list"` | File list. |
| `data-testid="chunk-list"` | Source trace chunk list. |
| `data-testid="approve-file"` | Review action. |
| `data-testid="publish-file"` | Publish action. |
| `data-testid="wiki-pages"` | Published Wiki list. |
| `data-testid="refresh-graph-evidence"` | Post-publish graph/vector refresh. |
| `data-testid="ask-question"` | Ask input. |
| `data-testid="ask-submit"` | Ask action. |
| `data-testid="ask-answer"` | Answer panel. |
| `data-testid="coming-soon"` | Disabled mock-only controls. |

## Error Copy

Errors should be short, safe, and actionable. They must not include raw stack traces, private absolute paths, endpoints with credentials, provider payloads, or source document text.

## Responsive Design

The P0 app should use stable grid/flex dimensions for tab panels, lists, buttons, and graph canvas. Text should wrap inside controls without overlapping. Cards should stay restrained and operational, with radius no larger than the existing design language.

## Testing Considerations

- Unit tests should mock `fetch` and verify API sequencing, disabled states, and answer/evidence rendering.
- UI-driven E2E should operate through browser controls for the main loop.
- Existing graph E2E tests must remain compatible with the Graph panel selectors.

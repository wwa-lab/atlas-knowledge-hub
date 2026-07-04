# Data Flow: Full-Stack Productization

## Main Browser Flow

```text
1. App loads spaces
   Vue -> GET /api/spaces -> Space list

2. User opens space
   Vue -> GET /api/spaces/{spaceId} -> selected context

3. User creates sample batch
   Vue -> POST /api/spaces/{spaceId}/batches -> Batch
   Vue -> GET /api/spaces/{spaceId}/batches -> Batches
   Vue -> GET /api/batches/{batchId}/files -> Files
   Vue -> GET /api/files/{fileId}/chunks -> Source chunks

4. User approves file
   Vue -> GET /api/spaces/{spaceId}/review-queues -> Queues
   Vue -> POST /api/files/{fileId}/reviews -> ReviewRecord
   Vue refreshes files/chunks/queues

5. User publishes Wiki
   Vue -> POST /api/files/{fileId}/publish -> WikiPage PUBLISHED
   Vue -> GET /api/spaces/{spaceId}/wiki-pages -> Wiki list

6. App refreshes downstream evidence
   Vue -> POST /api/spaces/{spaceId}/graph/projection-runs -> GraphProjectionRun
   Vue -> POST /api/spaces/{spaceId}/vector-runs -> VectorRun
   Vue -> GET /api/spaces/{spaceId}/graph -> GraphView

7. User asks
   Vue -> POST /api/spaces/{spaceId}/ask -> AskRun
   Vue -> GET /api/ask-runs/{runId} -> Answer + evidence
```

## Data Safety Flow

- Sample batch request contains only safe relative paths and mock/sample labels.
- Review request contains reviewer/comment and affected chunk ids; no raw source text.
- Publish request contains title/owner; backend derives source document ids and preserves metadata.
- Ask request contains bounded question text and safe filters; backend stores evidence references, not raw provider payloads.

## Failure Flow

| Failure | UI Behavior |
|---|---|
| Spaces fail | Show API error and retry. |
| Batch create fails | Keep current data, show safe error, do not advance workflow. |
| Review fails | Keep file pending and show safe error. |
| Publish blocked | Show backend conflict message and keep publish disabled until prerequisites are fixed. |
| Graph refresh fails | Show graph error; do not claim connected state. |
| Ask no evidence | Show no-evidence answer state with no citations. |

# Ask RAG — API Implementation Guide

## Status

Draft. Required for Phase 4 `ask-rag` because the slice introduces backend/API behavior.

## Base Conventions

- Base path: `/api`.
- Envelope: `ApiEnvelope<T>` with `success`, `data`, `error`, and optional `meta`.
- Auth/RBAC: internal/mock guard only in this slice unless a separate security slice accepts full production enforcement.
- Mode: automated tests use `mock`.
- Secrets: masked/status-only; never return raw credentials, endpoints, provider payloads, private paths, raw prompts, raw source text, raw vectors, or stack traces.

## `POST /api/spaces/{spaceId}/ask`

Purpose: run one scoped Trusted Ask request.

Request:

```json
{
  "question": "Explain what Source Trace means in the modernization workflow.",
  "requestedBy": "delivery-lead",
  "reviewPolicy": "APPROVED_ONLY",
  "limit": 5,
  "mode": "mock",
  "filters": {
    "fileItemIds": ["file-003"],
    "sourceTypes": ["pdf", "pptx"]
  }
}
```

Validation:

- `spaceId` must reference an existing space.
- `question` is required, trimmed, and bounded.
- `requestedBy` is required for audit.
- `reviewPolicy` is `APPROVED_ONLY` or `INCLUDE_REVIEW_REQUIRED`; default is `APPROVED_ONLY`.
- `limit` is positive and bounded.
- filters, when present, must stay within the selected space.
- Request text and filters must not contain raw credentials, private paths, URLs used as provider endpoints, or real company content in tests.

Success response with answer:

```json
{
  "success": true,
  "data": {
    "runId": "ask-run-20260703-001",
    "spaceId": "ibm-i-modernization",
    "status": "SUCCEEDED",
    "question": "Explain what Source Trace means in the modernization workflow.",
    "answer": "Source Trace is the evidence chain that links Atlas answers back to approved Wiki sections and source chunks.",
    "answerConfidence": 0.86,
    "answerReviewStatus": "REVIEW_REQUIRED",
    "reviewPolicy": "APPROVED_ONLY",
    "modelRunId": "model-run-20260703-001",
    "safeMessage": "Answer generated from approved evidence.",
    "evidence": [
      {
        "sourceChunkId": "chunk-file-003-p12-b02",
        "fileItemId": "file-003",
        "sourceFile": "BRD_Methodology.pdf",
        "page": 12,
        "section": "Source Trace",
        "score": 0.92,
        "confidence": 0.91,
        "reviewStatus": "APPROVED",
        "safeExcerptLabel": "BRD page 12 / Source Trace"
      }
    ],
    "createdAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:02Z"
  },
  "error": null,
  "meta": null
}
```

No-evidence response:

```json
{
  "success": true,
  "data": {
    "runId": "ask-run-20260703-002",
    "spaceId": "ibm-i-modernization",
    "status": "NO_EVIDENCE",
    "question": "Which unapproved document changed the policy?",
    "answer": "No approved evidence was found for this question. Review or publish relevant evidence before using Trusted Ask.",
    "answerConfidence": null,
    "answerReviewStatus": "REVIEW_REQUIRED",
    "reviewPolicy": "APPROVED_ONLY",
    "modelRunId": null,
    "safeMessage": "No approved evidence found.",
    "evidence": []
  },
  "error": null,
  "meta": null
}
```

## `GET /api/ask-runs/{runId}`

Purpose: return persisted Ask run detail and evidence.

Response shape: same `data` body as `POST /api/spaces/{spaceId}/ask`.

Errors:

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Invalid request payload, policy, limit, or unsafe filter. |
| 404 | `NOT_FOUND` | Unknown space or Ask run. |
| 409 | `CONFLICT` | A conflicting active Ask run exists for the same scoped request, if implemented. |
| 500 | `INTERNAL_ERROR` | Unexpected safe server fault; no raw details in response. |

## Internal Orchestration Contract

```text
AskService
  validate request and scope
  create AskRun REQUESTED
  query vector evidence through product-facing vector contract
  if no approved evidence: complete NO_EVIDENCE without model call
  create model run through product-facing model contract
  persist AskEvidence rows and final AskRun status
  return safe Ask response
```

## Contract Tests

Implementation must run:

```bash
cd backend && mvn -Dtest=AskServiceTest,AskSummaryCalculatorTest test
cd backend && mvn -Dit.test=AskApiContractIT verify
cd frontend && npm run typecheck
cd frontend && npm run test
cd frontend && npm run build
cd frontend && npm run e2e
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|pgvector|Milvus|Qdrant|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src frontend/src docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md
```

The seam scan must return no matches in non-adapter product layers. Adapter implementation packages may contain safe provider labels only when covered by guard tests and no outbound network clients leak into product layers.

# API Implementation Guide: Knowledge Space

## Status

Draft. Required before backend implementation.

## Scope

This guide defines future internal API expectations for the Knowledge Space slice. It is not an instruction to scaffold backend code during Phase 0.

## Principles

- Use consistent response envelopes when backend implementation begins.
- Keep converter and parser tools behind adapter services.
- Preserve source trace, confidence, and review status in all content APIs.
- Return user-safe errors.
- Do not expose private absolute paths.

## Candidate Endpoints

### Knowledge Spaces

```text
GET /api/spaces
GET /api/spaces/{spaceId}
POST /api/spaces
```

Returns Knowledge Space cards and detail metadata.

### Batches

```text
GET /api/spaces/{spaceId}/batches
GET /api/batches/{batchId}
POST /api/spaces/{spaceId}/batches
```

Creates and reads upload/processing batches. Upload behavior may be implemented later through multipart or pre-staged internal storage.

### File Items

```text
GET /api/batches/{batchId}/files
GET /api/files/{fileId}
```

Returns file tree, generated artifact paths, statuses, confidence, and review state.

### Wiki

```text
GET /api/spaces/{spaceId}/wiki-pages
GET /api/wiki-pages/{pageId}
```

Returns normalized Markdown content, source documents, source chunks, confidence, and review status.

### Review

```text
GET /api/spaces/{spaceId}/review-tasks
GET /api/review-tasks/{taskId}
POST /api/review-tasks/{taskId}/actions
```

Actions:

- `APPROVE`
- `NEED_FIX`
- `MARK_OCR_REQUIRED`

### Graph

```text
GET /api/spaces/{spaceId}/graph
```

Returns graph nodes, edges, type counts, and evidence links.

### Ask

```text
POST /api/spaces/{spaceId}/ask
```

Accepts:

- question.
- selected knowledge context.
- optional file/page filters.

Returns:

- answer.
- source references.
- confidence.
- review status or trust label.

### UI Preferences

Prototype mode may keep language and theme local. If preferences become persistent, add:

```text
GET /api/me/preferences/ui
PUT /api/me/preferences/ui
```

Fields:

- `language`: `zh-CN` or `en-US`.
- `theme`: `day` or `night`.

These endpoints are optional and should not be implemented during static prototype validation.

### Model Management

Prototype mode is visual only. If model management becomes persistent, add:

```text
GET /api/admin/models
GET /api/admin/models/{modelConfigId}
POST /api/admin/models
PUT /api/admin/models/{modelConfigId}
POST /api/admin/models/{modelConfigId}/test
```

Model config responses must mask secrets:

```json
{
  "id": "model-github-gpt",
  "provider": "GitHub Models",
  "source_type": "api",
  "model_type": "chat",
  "model_name": "openai/gpt-4.1",
  "display_name": "GitHub GPT-4.1",
  "base_url": "https://models.github.ai/inference",
  "api_key_status": "configured",
  "supports_multimodal": true,
  "thinking_parameter_format": "provider_default",
  "enabled": true
}
```

Raw API keys must be accepted only through write-only secret fields and stored through a secret manager or encrypted backend storage.

GitHub provider implementations should distinguish:

- GitHub Models API/catalog integration.
- GitHub Copilot supported models, which are subject to Copilot plan, client, and organization policy availability.

### Registration

Prototype mode is visual only. If registration becomes real, add:

```text
GET /api/auth/registration-policy
POST /api/auth/register
```

Registration requests should validate name, email, password policy, allowed domains, and invite token when required. Do not return sensitive credential details.

### Space Members

Prototype mode is visual only. If Knowledge Space member management becomes real, add:

```text
GET /api/spaces/{spaceId}/members
POST /api/spaces/{spaceId}/members/invitations
PATCH /api/spaces/{spaceId}/members/{membershipId}
DELETE /api/spaces/{spaceId}/members/{membershipId}
GET /api/spaces/{spaceId}/members/audit-log
```

Future implementation must enforce RBAC server-side:

- Owner/Admin can invite members.
- Owner/Admin can change roles except protected owner transfer rules.
- Owner/Admin can remove members except the last owner.
- Reviewer and Viewer cannot manage membership.

### Account And API Information

Prototype mode is visual only. If account settings become persistent, add:

```text
GET /api/me/settings
PUT /api/me/settings
GET /api/me/profile
PUT /api/me/profile
GET /api/me/api-access
POST /api/me/api-access/rotate
POST /api/me/api-access/revoke
```

API access responses must mask secrets and return status/scopes only.

### Data And Extension Engines

Prototype mode is visual only. If engine settings become persistent, add:

```text
GET /api/admin/engines
GET /api/admin/engines/{engineConfigId}
POST /api/admin/engines
PUT /api/admin/engines/{engineConfigId}
POST /api/admin/engines/{engineConfigId}/test
```

Engine configs must stay adapter-oriented:

- Vector database engines behind vector store adapters.
- Parsing engines behind parser adapters.
- Storage engines behind storage adapters.

Do not expose raw credentials or private endpoints in client responses.

## Response Envelope

```json
{
  "success": true,
  "data": {},
  "error": null,
  "pagination": null
}
```

Error responses should avoid leaking internal command output, credentials, absolute paths, or raw parser logs.

## Adapter Boundary

Product APIs should call application services, not parser binaries directly.

```text
BatchService
  -> ConverterAdapter
  -> ParserAdapter
  -> MarkdownNormalizer
  -> ReviewService
```

Adapters may wrap:

- `trinity-office`
- `document-normalize`
- future MinerU, Docling, PaddleOCR, internal OCR, or Copilot Vision

## Implementation Timing

- Do not implement these endpoints during static prototype validation.
- Implement only when the project enters the backend phase described in `docs/technology-decisions.md`.
- Create ADRs for endpoint shape, auth model, storage model, and graph technology when those decisions become real.

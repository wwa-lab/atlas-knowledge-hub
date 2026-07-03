# Requirements: Storage Adapter

## Status

Draft. Phase 3 adapter slice. SDD only; no product code is implemented in this pass.

## Slice Contract

- **Goal:** Atlas can store and retrieve workspace-separated artifacts (raw inputs, generated PDFs, Markdown, extracted assets, batch reports, published Wiki) through a product-facing storage adapter, without coupling product workflows to S3/MinIO or any single object-storage engine.
- **Slice:** `storage-adapter`
- **Phase:** 3 adapter
- **Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/00-context/slice-roadmap.md`, `docs/01-requirements/requirement.md`, `docs/architecture.md`, `docs/batch-processing-design.md`, `docs/03-spec/converter-adapter-spec.md`, `docs/03-spec/parser-adapter-spec.md`, `docs/04-architecture/converter-adapter-data-model.md`, and the existing backend metadata entities verified during grounding.
- **Verification row:** Phase 3 adapter requires unit + integration tests against mock engines.
- **Hard constraints:** Parser/converter/model/vector/storage must go only through product-facing adapters; tools must never be called directly; one implementation must not be hardcoded; secrets and private paths must be masked; source trace, confidence, and review status must be preserved.

## In Scope

- A product-facing storage adapter contract for object put/get/list/delete/exists behavior with a stored-object descriptor.
- Capability metadata for a default S3-compatible storage adapter with masked configuration (endpoint, bucket, region, credentials shown as status-only, never raw).
- Workspace-separated layer policy: raw inputs, generated PDF, Markdown, assets, reports, and published Wiki stored under distinct layer prefixes.
- Safe relative object-key rules under a workspace/batch namespace.
- Stored-object descriptors: object key, layer, content type, size, checksum, status, safe error, and timestamps.
- Storage operation records that summarize store/delete/list outcomes for audit.
- Metadata pointer write-back that records the returned storage key into the correct `file_item` artifact field (or a report/wiki pointer) through the metadata boundary.
- Mock/in-memory storage engine, unit/integration tests, and adapter seam guard updates.
- Internal API and adapter contract guidance for implementation.

## Exclusions

- Real S3/MinIO/cloud object-storage execution, real bucket provisioning, presigned URL issuance against real endpoints, lifecycle/retention policies, encryption-key management, CDN, and cross-region replication beyond the adapter seam contract.
- Converter/parser execution, OCR, LLM enrichment, graph extraction, Ask/RAG, Wiki publication semantics, vector/model adapters, frontend screens, production authentication/RBAC, and production secret manager integration.
- Real company documents, private paths, raw logs, credentials, external cloud calls, or external network dependencies.

## Requirements

| ID | Requirement | Priority | Source / Rationale |
|---|---|---|---|
| REQ-SA-001 | Product workflow code must resolve object storage through a storage adapter registry and must not call S3 SDKs, MinIO clients, filesystem object stores, or outbound HTTP/S3 clients directly outside adapter scope. | Must | REQ-PROD-015, REQ-PROD-017 |
| REQ-SA-002 | An S3-compatible object store must be represented as one replaceable storage adapter, not the only possible storage implementation; future filesystem or other S3-compatible engines must remain configurable. | Must | REQ-PROD-017, REQ-PROD-060, REQ-PROD-061 |
| REQ-SA-003 | Storage capability metadata must expose adapter key, display name, version/status, supported layers, default marker, and masked configuration summary; endpoint, bucket, region, and credentials must be status-only (`configured`) and never raw. | Must | Adapter Standards, REQ-PROD-049, REQ-PROD-058, REQ-PROD-061 |
| REQ-SA-004 | The adapter must enforce workspace-separated layers: raw inputs, generated PDF, Markdown, assets, reports, and published Wiki are stored under distinct layer prefixes and must not be mixed. | Must | REQ-PROD-014, Workspace Separation |
| REQ-SA-005 | A store (put) operation must accept a workspace/batch scope, target layer, safe relative object key, content type, and a content reference, and must return a stored-object descriptor with key, layer, size, checksum, content type, and timestamps. | Must | REQ-PROD-014, REQ-PROD-018 |
| REQ-SA-006 | A retrieve (get)/exists operation must resolve an object by layer and key and return a safe descriptor or a safe not-found result, without exposing raw storage internals. | Must | REQ-PROD-014 |
| REQ-SA-007 | A list operation must enumerate objects within a workspace/batch/layer prefix using bounded, paginated results. | Should | REQ-PROD-013, REQ-PROD-014 |
| REQ-SA-008 | A delete operation must remove an object by layer and key, return a safe outcome, and must never traverse outside the workspace/batch namespace. | Must | Metadata/API safety |
| REQ-SA-009 | Storage keys and layer prefixes must be safe relative paths; absolute paths, drive/URI/host prefixes, and traversal must be rejected before any storage operation. | Must | Security/Data Standards |
| REQ-SA-010 | Storage failures must return a sanitized user-safe error with no raw SDK output, stack trace, secret, endpoint, bucket ARN, credential, hostname, or private/absolute path. | Must | Security/Data Standards |
| REQ-SA-011 | Metadata pointer write-back must record the returned storage key/descriptor into the correct `file_item` artifact field or report/wiki pointer through the metadata boundary, preserving source trace, confidence, and review status. | Must | REQ-PROD-014, Trace/review preservation |
| REQ-SA-012 | Automated verification must use a mock/in-memory storage engine and must not require a real S3/MinIO endpoint, real buckets, external network calls, or cloud credentials. | Must | Phase 3 verification row |
| REQ-SA-013 | Storage operation records must summarize operation type, layer, object counts, total bytes, succeeded/failed/skipped outcomes, and a safe message for audit. | Should | REQ-PROD-013, REQ-PROD-018 |
| REQ-SA-014 | The slice must preserve existing artifact metadata and review status; storing or deleting an object must not silently change file status, confidence, or review status unless a workflow explicitly maps it. | Must | Trace/review preservation |

## Acceptance

- Complete bilingual SDD artifacts exist for requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- REQ/US/T IDs match across English and Simplified Chinese copies.
- Tasks map to requirement IDs and spec sections and include exact verification commands.
- API guide is included because this Phase 3 adapter slice defines internal API/adapter contracts.
- No product code is changed in this SDD pass.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-SA-001 | Should the first real storage engine be S3-compatible object storage (MinIO/S3) only, or should a local filesystem adapter ship first for on-prem/air-gapped installs? | Deployment detail; does not block the mock adapter contract. |
| OQ-SA-002 | Should object bytes ever transit the metadata API, or must the adapter always exchange content references/streams so the API layer only handles descriptors? | This SDD commits to descriptor-only API responses; bytes stay behind the adapter. Confirm before implementation if a byte-passthrough endpoint is required. |
| OQ-SA-003 | Should presigned/short-lived access URLs be part of this slice or deferred to a later delivery/access slice? | This SDD defers presigned URL issuance; only masked capability and descriptors are in scope. |

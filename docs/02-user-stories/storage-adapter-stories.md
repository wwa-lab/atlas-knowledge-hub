# User Stories: Storage Adapter

## Status

Draft. Derived from `docs/01-requirements/storage-adapter-requirements.md`.

## Story Map

| Story ID | Title | Requirements |
|---|---|---|
| US-SA-001 | Store and retrieve artifacts through an adapter boundary | REQ-SA-001, REQ-SA-002, REQ-SA-005, REQ-SA-006, REQ-SA-012 |
| US-SA-002 | Inspect storage capabilities safely | REQ-SA-002, REQ-SA-003 |
| US-SA-003 | Keep artifact layers separated and safe | REQ-SA-004, REQ-SA-007, REQ-SA-009 |
| US-SA-004 | Write artifact pointers back to metadata | REQ-SA-005, REQ-SA-011, REQ-SA-013, REQ-SA-014 |
| US-SA-005 | Protect storage operations and adapter seams | REQ-SA-001, REQ-SA-008, REQ-SA-009, REQ-SA-010, REQ-SA-012 |

## US-SA-001: Store and retrieve artifacts through an adapter boundary

**Story**
As a knowledge base administrator,
I want batch artifacts to be stored and retrieved through a storage adapter,
so that Atlas can persist generated files without hard-coupling product workflows to one object-storage engine.

### Acceptance Criteria

1. **Given** a batch has generated artifacts (PDF, Markdown, assets, report)
   **When** a storage operation is requested
   **Then** Atlas resolves a registered storage adapter before any object is written.
2. **Given** non-adapter product layers are scanned
   **When** direct S3 SDK, MinIO client, or outbound storage-client references appear outside the adapter package
   **Then** seam guard verification fails.
3. **Given** tests run in CI
   **When** storage behavior is verified
   **Then** a mock/in-memory storage engine is used and no real S3/MinIO endpoint or cloud credentials are required.

### Notes / Assumptions

- The existing converter/parser adapter slices are the pattern for registry/capability/run behavior.
- Storage implementation starts after Phase 2 metadata API, which the current roadmap marks as implemented.

### Dependencies

- Metadata API file, batch, and artifact-path records.
- Converter/parser output that produces artifacts to persist.

### Out of Scope

- Real object-storage execution, presigned URLs, retention/lifecycle policy, frontend UI, graph, Ask, and publish behavior.

### Open Questions

- OQ-SA-001: First real storage engine choice (S3-compatible vs filesystem) remains deferred beyond mock-engine verification.

## US-SA-002: Inspect storage capabilities safely

**Story**
As a platform administrator,
I want to see configured storage adapter capabilities and status,
so that I can understand which store is available without exposing endpoints, buckets, or credentials.

### Acceptance Criteria

1. **Given** a storage adapter is configured
   **When** capabilities are listed
   **Then** the response includes adapter key, display name, version/status, supported layers, default marker, and masked configuration.
2. **Given** the adapter is disabled or misconfigured
   **When** capabilities are listed or selected
   **Then** Atlas exposes only a safe status and does not leak raw endpoints, bucket names, regions, access keys, secrets, hostnames, or absolute paths.

### Notes / Assumptions

- An S3-compatible store is the first named adapter, but future stores (filesystem, other S3-compatible engines) must remain replaceable.

### Dependencies

- Adapter registry and capability metadata contract.

### Out of Scope

- Production secret-manager integration.

### Open Questions

- None.

## US-SA-003: Keep artifact layers separated and safe

**Story**
As a delivery lead,
I want raw inputs, PDFs, Markdown, assets, reports, and Wiki output stored in separate layers,
so that artifacts stay organized, auditable, and never overwrite each other.

### Acceptance Criteria

1. **Given** an artifact is stored
   **When** the target layer and object key are validated
   **Then** the object is placed under the correct layer prefix within the workspace/batch namespace.
2. **Given** an object key is absolute, URI-prefixed, drive-prefixed, or uses traversal
   **When** the storage operation is validated
   **Then** the operation is rejected before any object is written.
3. **Given** a list operation is requested for a layer prefix
   **When** results are returned
   **Then** results are bounded and paginated and stay within the requested workspace/batch/layer scope.

### Notes / Assumptions

- Layers are `raw`, `pdf`, `markdown`, `assets`, `reports`, and `wiki`.
- Layer separation follows `PROJECT_RULES.md` Workspace Separation and REQ-PROD-014.

### Dependencies

- Relative path validation rules.

### Out of Scope

- Cross-workspace object copy and replication.

### Open Questions

- None.

## US-SA-004: Write artifact pointers back to metadata

**Story**
As an SME reviewer,
I want stored artifacts to be reachable from file metadata,
so that I can trace each generated file back to its stored object without losing review context.

### Acceptance Criteria

1. **Given** a store operation succeeds
   **When** metadata is written back
   **Then** the returned storage key/descriptor is recorded in the correct `file_item` artifact field or report/wiki pointer through the metadata boundary.
2. **Given** a stored-object descriptor is persisted
   **When** it is inspected
   **Then** it records object key, layer, content type, size, checksum, adapter key, status, and timestamps with no raw storage internals.
3. **Given** an artifact is stored or deleted
   **When** metadata is updated
   **Then** source path, source type, confidence, and review status are preserved unless a workflow explicitly maps a change.

### Notes / Assumptions

- Storage-adapter records pointers/descriptors but does not change conversion or review semantics.
- Generated content remains review-required until a later review/publish flow changes it.

### Dependencies

- Existing `file_item` artifact path fields and review status rules.

### Out of Scope

- Wiki publication and graph projection.

### Open Questions

- OQ-SA-002: Whether object bytes ever transit the API or only descriptors are exchanged.

## US-SA-005: Protect storage operations and adapter seams

**Story**
As an implementation owner,
I want storage operations and adapter seams to be guarded,
so that malformed keys or storage errors cannot escape the namespace or leak sensitive information.

### Acceptance Criteria

1. **Given** a storage operation contains an unsafe key, an out-of-namespace target, or an unknown layer
   **When** Atlas validates the operation
   **Then** it fails safely with field-level validation and no object is written or deleted.
2. **Given** storage errors contain SDK output, secrets, endpoints, or private paths
   **When** the error is returned or stored
   **Then** only a bounded sanitized summary is kept.
3. **Given** automated verification runs
   **When** seam guard and secret scans execute
   **Then** non-adapter product layers contain no direct storage-engine calls and generated SDD/code contain no raw secrets, endpoints, or private paths.

### Notes / Assumptions

- The existing `AdapterSeamGuardTest` asserts adapter source `.doesNotContain("S3")`; this slice must relax that inside the storage adapter package and add storage-engine names to the non-adapter forbidden list.
- Relative-key validation should reuse the existing metadata path validator where possible.

### Dependencies

- Existing relative-path validator, API error envelope, and adapter seam guard test.

### Out of Scope

- Full production credential management.

### Open Questions

- None.

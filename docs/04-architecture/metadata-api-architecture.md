# Architecture: Metadata API

## Status

Draft. Phase 2. Slice `metadata-api`. Derived from `docs/03-spec/metadata-api-spec.md`.

## Overview

The metadata service is the Atlas **control plane**: a Spring Boot application that owns product metadata and state in PostgreSQL and serves it over an internal REST API. It is the first component to move Atlas from FE-only mock to a persisted contract. It deliberately owns *no* processing — conversion, parsing, model, vector, storage, and search execution belong to a separate adapter/worker plane introduced in Phase 3.

## Component Ownership & Boundaries

```
┌──────────────────────────────────────────────────────────────┐
│ Frontend (Vue) — CONSUMER                                     │
│  reads/writes metadata over HTTP; owns no persistence          │
└───────────────▲──────────────────────────────────────────────┘
                │ REST (JSON envelope)
┌───────────────┴──────────────────────────────────────────────┐
│ metadata-api (Spring Boot) — THIS SLICE                       │
│                                                                │
│  package-by-feature: space/ batch/ file/ review/ wiki/ graph/ │
│    each = controller + service + entity + repository + mapper  │
│  common/    envelope, error handler, validators, shared enums │
│  db/        Flyway migrations (schema + seed)                  │
│  adapter/   RESERVED SEAM — empty in this slice (Phase 3)      │
└───────────────▲──────────────────────────────────────────────┘
                │ JDBC (config-driven datasource)
┌───────────────┴──────────────────────────────────────────────┐
│ PostgreSQL — schema `atlas`, Flyway-managed                   │
└──────────────────────────────────────────────────────────────┘
```

- **Feature packages** (`space/`, `batch/`, `file/`, `review/`) each own their controller, service, entity, repository, and mapper. Inside a feature the role dependency direction holds: controller → service → entity, service → repository. No business logic in controllers (REQ-MA-008). Cross-feature access is service→service only.
- **`common/`** owns HTTP cross-cutting: `ApiEnvelope`, `ErrorBody`, `PageMeta`, the `GlobalExceptionHandler` (failures → user-safe envelopes), the `@RelativePath` validator, and cross-feature enums (`FileStatus`, `ReviewStatus`, `SourceKind`, `SourceType`). Feature-local enums (`SpaceType`, `IndexStrategy`, `SpaceStatus`) stay with their feature.
- **Batch metrics** are computed in `batch/MetricsCalculator` from file items (derived, not stored), reading file status via `FileService`.
- **Invariants** (e.g. generated content is never `APPROVED` by default) live on the entities/services that own them; product logic depends on repository interfaces, not JDBC details.
- **db/** owns versioned Flyway migrations. `ddl-auto=validate` — no runtime schema mutation for shared environments.
- **adapter/** is a reserved, empty package. No converter/parser/model/vector/storage engine is referenced anywhere in this slice (REQ-MA-013).

## Adapter Boundary (critical rule)

This slice **hardcodes no engine**. The `adapter/` package exists only to mark the seam where Phase 3 will introduce converter/parser/storage/model/vector adapters. Those adapters will later call *into* this service to record file/chunk metadata and status; they will never be called *from* this slice, and product logic will depend on product-facing adapter interfaces rather than concrete engines (`document-normalize`, `trinity-office`, MinerU, pgvector, Ollama, S3, etc.).

## Datasource Neutrality

The datasource is fully externalized (`spring.datasource.*` via env/profile). No database URL, username, or password is a hardcoded literal, and PostgreSQL is the default but not the only possible target (REQ-MA-012). Tests run against an ephemeral instance (Testcontainers PostgreSQL or an in-memory Postgres-compatible profile) so contract/integration tests do not require a shared server.

## Trace, Confidence & Review Preservation

Source trace, confidence, and review status are first-class columns on `file_item`, `source_chunk`, and `wiki_page`, and are never dropped in mapping. The application layer enforces the invariant that generated/low-confidence content persists as `REVIEW_REQUIRED`; only an explicit review action moves a target to `APPROVED`/`NEED_FIX`/`OCR_REQUIRED` (REQ-MA-011). `review_record` is append-only — history rows are never mutated or deleted.

## Security & Data-Safety Constraints

- User-safe errors only: a global exception handler strips stack traces, SQL, secrets, internal hostnames, and private absolute paths from responses; details are logged server-side (REQ-MA-009, REQ-PROD-077).
- Input validation at the boundary (Bean Validation on DTOs); `source_path` must be relative and traversal-free (REQ-MA-009).
- No auth/RBAC yet — this is an internal-only service in Phase 2; RBAC is a Phase 4 slice. This must be stated explicitly and not implied as production-secure.
- Mock/sample seed only; no real company data, credentials, or private paths (REQ-MA-012).

## Scope Boundaries vs Other Slices

- **knowledge-space / folder-upload (Phase 1 FE):** define the mock shapes this service persists. Enum and field names must reconcile with `frontend/src/types.ts` and `docs/batch-processing-design.md`, not diverge.
- **converter/parser/storage/vector/model-adapter (Phase 3):** own engine execution; this slice only reserves the seam.
- **review-publish / knowledge-graph / ask-rag (Phase 4):** own the publish state machine, graph query, and Ask. This slice creates `wiki_page`, `graph_node`, `graph_edge` tables (REQ-PROD-071) but exposes no endpoints for them (REQ-MA-014).

## Technology Decisions

- Java + Spring Boot (Spring Web, Spring Data JPA, Bean Validation).
- PostgreSQL + Flyway; `ddl-auto=validate`.
- Build/verify: Maven (`mvn verify`); Testcontainers (or equivalent) for integration/contract tests.
- These follow `PROJECT_RULES.md` "Technology Decisions" and `DEVELOPMENT_STANDARDS.md` Backend/Database standards; no new external network dependency beyond the JDBC datasource.

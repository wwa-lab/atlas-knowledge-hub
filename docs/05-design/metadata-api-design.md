# Design: Metadata API

## Status

Draft. Phase 2. Slice `metadata-api`. Derived from `docs/04-architecture/metadata-api-architecture.md` and the data model. This is a backend service design — no UI. The FE is the consumer; see the API implementation guide for the wire contract.

## Module Layout (Maven / Spring Boot)

Package-by-layer — conventional Spring Boot (see `docs/BACKEND_CODING_STANDARD.md` § File & Package Organization): classes grouped by technical role.

```
backend/                         # promoted from placeholder in this slice
  pom.xml
  src/main/java/com/atlas/metadata/
    MetadataApiApplication.java
    controller/                  # SpaceController, BatchController, FileController, ReviewController
    service/                     # SpaceService, BatchService, FileService, ReviewService
      MetricsCalculator.java     # derives BatchMetrics from file items
    repository/                  # SpaceRepository, BatchRepository, FileItemRepository,
                                 #   SourceChunkRepository, ReviewRecordRepository
    domain/                      # Space, Batch, FileItem, SourceChunk, ReviewRecord,
                                 #   WikiPage, GraphNode, GraphEdge  (JPA entities)
    enums/                       # FileStatus, ReviewStatus, ReviewAction, SourceKind, SourceType,
                                 #   SpaceType, IndexStrategy, SpaceStatus, GraphNodeType, GraphEdgeType
    dto/                         # *Request / *Response records, ApiEnvelope, ErrorBody, PageMeta
      mapping/                   # entity <-> DTO mappers
    exception/                   # GlobalExceptionHandler, NotFoundException, ConflictException
    validation/                  # @RelativePath validator
    adapter/                     # RESERVED, EMPTY (Phase 3 seam) — package-info.java only
  src/main/resources/
    application.yml              # config-driven datasource (no literals)
    db/migration/
      V1__init_schema.sql
      V2__seed_mock_metadata.sql
  src/test/java/com/atlas/metadata/
    controller/ (contract tests, @WebMvcTest)
    service/    (unit tests: MetricsCalculator, invariants, mappers)
    integration/ (Testcontainers PostgreSQL: repositories + migration validation)
```

The pre-existing `backend/README.md` placeholder is superseded; this slice is where `backend/` becomes buildable, per Phase 2 discipline.

## Layer Contracts

Dependency direction: `controller → service → repository → domain`; controller → dto. Never the reverse.

### controller/
- One controller per resource; thin — parse, validate (`@Valid`), delegate to a service, wrap in the envelope.
- No JPA entity is ever serialized directly; controllers return DTOs only.
- `exception/GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions → `{400,404,409,500}` user-safe envelopes.
- Pagination via `page`/`size` query params (bounded default size, e.g. 20; max 200).

### service/
- Own transactions (`@Transactional`), orchestration, and entity↔DTO mapping (via `dto/mapping`).
- `MetricsCalculator` computes `BatchMetrics` from a batch's file items — the *only* place metrics are produced (no stored counter).
- Enforce invariants: default `review_status=REVIEW_REQUIRED`; only `ReviewService` advances it via an action; never set `APPROVED` on create.

### domain/ + enums/
- JPA entities mirror the data model tables exactly. `FileStatus` values are identical strings to `frontend/src/types.ts`; `ReviewStatus` intentionally follows the wider REQ-PROD-030 set and does **not** match the narrower FE `ReviewStatus` (see data model "Relationship To FE Types"). Do not collapse it to the FE type.
- Entities hold invariant helpers (e.g. `FileStatus.isGenerated()`), not DB-query or HTTP concerns; enums live in `enums/`.

### repository/
- Spring Data JPA interfaces; custom queries for paged/filter reads (`findByBatchIdAndStatus`, `findBySpaceId`, chronological `findByTargetTypeAndTargetIdOrderByCreatedAt`).

### adapter/ (reserved)
- `package-info.java` documenting the seam; **no class references any engine**. Presence is asserted-empty by a test so future accidental coupling is caught.

## DTO & Envelope Design

- `ApiEnvelope<T>` record: `{ success, data, error, meta }`.
- `ErrorBody`: `{ code, message, fields?, timestamp, path }`. `code` ∈ {`VALIDATION_ERROR`,`NOT_FOUND`,`CONFLICT`,`INTERNAL_ERROR`}. `timestamp` (epoch millis) + `path` are on error responses only (mirrors Spring's `DefaultErrorAttributes`); success responses stay lean. See `docs/BACKEND_CODING_STANDARD.md` § API Response Envelope.
- `PageMeta`: `{ page, size, total }`, present only on list responses.
- Request DTOs are records with Bean Validation annotations (`@NotBlank`, `@Pattern`, `@DecimalMin/@DecimalMax`, custom `@RelativePath`).
- A `@RelativePath` validator rejects absolute paths, drive/host prefixes, and `..` traversal.

## Configuration Design

- `application.yml` reads `spring.datasource.url|username|password` from environment/profile placeholders (`${ATLAS_DB_URL}` etc.) — no literals, no committed secrets.
- `spring.jpa.hibernate.ddl-auto: validate`; Flyway enabled; `spring.flyway.locations: classpath:db/migration`.
- A `local` profile may point at a developer Postgres; a `test` profile uses Testcontainers — neither commits credentials.

## Error & Security Design

- Exception→envelope mapping table (see spec Error Behavior). `INTERNAL_ERROR` returns a generic message; the real cause is logged with a correlation id, never returned.
- No response, log, or seed contains secrets, credentials, private endpoints, internal hostnames, or absolute paths.
- No authentication in this slice — documented as internal-only; a `SECURITY.md` note or class comment states RBAC is deferred to Phase 4 and the service must not be exposed publicly as-is.

## Testing Design (maps to `mvn verify`)

- **Unit (app/):** `MetricsCalculator` derivation; review-action→status mapping; `REVIEW_REQUIRED` default invariant; mappers; `@RelativePath` validator.
- **Contract (web/):** MockMvc/WebTestClient per endpoint asserting status codes, envelope shape, pagination meta, validation 400s, 404s, and user-safe error bodies.
- **Integration:** Testcontainers PostgreSQL — repository CRUD, Flyway `V1`+`V2` apply cleanly, `ddl-auto=validate` passes, seed rows match FE-baseline expectations.
- **Adapter-seam guard:** a test asserts the `adapter/` package has no engine dependency and no outbound network client is wired.

## Consumer (FE) Integration Notes

- The FE currently reads `frontend/src/data/atlasMock.ts`. Cutover (replacing the mock provider with an HTTP client hitting these endpoints) is a **separate task**, tracked but not implemented here. The envelope + enum-value parity is designed so cutover needs no visual change (REQ-MA-015).

## Open Questions

- Enable `POST` writes now vs. read-only + seed (default: enable space/batch/review create).
- Enum persistence as native PG enum types vs. text + CHECK (default: text + CHECK for migration flexibility).

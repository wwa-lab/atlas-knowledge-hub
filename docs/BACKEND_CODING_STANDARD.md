# Backend Coding Standard

Atlas Knowledge Hub Phase 2+ backend development standards for Java + Spring Boot + PostgreSQL + Flyway. 中文版：[BACKEND_CODING_STANDARD.zh-CN.md](BACKEND_CODING_STANDARD.zh-CN.md).

This document extends [DEVELOPMENT_STANDARDS.md](../DEVELOPMENT_STANDARDS.md) §§ Backend / API / Database / Adapter Standards with concrete patterns and examples specific to Spring Boot. It is the backend counterpart to [docs/FRONTEND_CODING_STANDARD.md](FRONTEND_CODING_STANDARD.md). Where a slice design (e.g. `docs/05-design/metadata-api-design.md`) is more specific, the slice design wins for that slice.

### Referenced guides (cross-checked)

Rules here are cross-checked against, and cite where they came from: the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html), the [Spring](https://spring.io/guides) / [Spring Boot](https://docs.spring.io/spring-boot/reference/) official guides, and the [Alibaba Java Development Manual (P3C)](https://github.com/alibaba/p3c). Where those guides disagree or don't fit Atlas' mock-first, PostgreSQL, small-team profile, the deviation is stated explicitly under **Deliberate Deviations** below — do not "correct" a documented deviation in review.

### Deliberate deviations from the referenced guides

- **Timestamps:** columns are `created_at` / `updated_at`, **not** Alibaba's `gmt_create` / `gmt_modified`. Rationale: matches the FE domain model (`frontend/src/types.ts`) and the wider industry convention.
- **No forced interface + `Impl`:** Alibaba mandates a `*Service` interface + `*ServiceImpl`. We use a single concrete `*Service` class unless a second implementation genuinely exists — forcing an interface for one impl violates the global "avoid speculative abstractions" rule (`~/.claude/rules` coding style). Introduce the interface when the second impl arrives.
- **Success envelope carries no `timestamp`/`status`/`path`:** Spring's `{timestamp,status,error,message,path}` shape is its *default error* body, not a success wrapper. We keep success responses lean (`{success,data,error,meta}`) and instead enrich `ErrorBody` with `timestamp` + `path` (see API Response Envelope).

## Scope & Phase Discipline

- Applies once a slice reaches **Phase 2** (backend metadata API + persistence) or later. Do not scaffold Spring Boot, a database, or migrations before the slice's accept gate (data model + API guide accepted — REQ-PROD-073).
- The backend is a **metadata control plane**. It owns product metadata and state, not processing. Converter/parser/model/vector/storage execution belongs to the Phase 3 adapter plane and must stay behind adapter interfaces.

## File & Package Organization

**Package-by-layer** — the conventional Spring Boot structure. Group classes by technical role; keep API/controller, application/service, repository, domain/entity, and migration responsibilities separate (`DEVELOPMENT_STANDARDS.md` § Backend).

```
backend/
├── pom.xml
├── src/main/java/com/atlas/metadata/
│   ├── MetadataApiApplication.java
│   ├── controller/             # thin controllers — one per resource
│   │   ├── SpaceController.java   BatchController.java
│   │   └── FileController.java    ReviewController.java
│   ├── service/                # application services: orchestration, mapping, derived values
│   │   ├── SpaceService.java   BatchService.java  FileService.java  ReviewService.java
│   │   └── MetricsCalculator.java  # derives batch metrics from file items
│   ├── repository/             # Spring Data JPA repositories
│   │   ├── SpaceRepository.java   BatchRepository.java  FileItemRepository.java
│   │   └── SourceChunkRepository.java  ReviewRecordRepository.java
│   ├── domain/                 # JPA entities + invariants
│   │   ├── Space.java  Batch.java  FileItem.java  SourceChunk.java
│   │   └── ReviewRecord.java  WikiPage.java  GraphNode.java  GraphEdge.java
│   ├── enums/                  # FileStatus, ReviewStatus, ReviewAction, SourceKind, SourceType,
│   │                           #   SpaceType, IndexStrategy, SpaceStatus, GraphNodeType, GraphEdgeType
│   ├── dto/                    # *Request / *Response records, ApiEnvelope, ErrorBody, PageMeta
│   │   └── mapping/            # entity <-> DTO mappers
│   ├── exception/              # GlobalExceptionHandler, NotFoundException, ConflictException
│   ├── validation/             # @RelativePath validator
│   └── adapter/                # RESERVED SEAM — empty until Phase 3 (package-info.java only)
├── src/main/resources/
│   ├── application.yml         # config-driven; no literal secrets
│   └── db/migration/           # Flyway V<n>__<desc>.sql
└── src/test/java/com/atlas/metadata/
    ├── controller/             # contract tests (@WebMvcTest)
    ├── service/                # unit tests (services, mappers, MetricsCalculator, validators)
    └── integration/            # Testcontainers PostgreSQL: repositories + migration validation
```

- **Package-by-layer, one role per package.** Controllers in `controller/`, services in `service/`, entities in `domain/`, and so on. Do not mix a controller and a repository in the same package.
- **Cross-cutting code by kind:** the response envelope + DTOs in `dto/`, the exception handler and custom exceptions in `exception/`, validators in `validation/`, enums in `enums/`.
- **Keep files focused:** 200–400 lines typical, 800 max; methods <50 lines; nesting <4 levels (global coding style).

## Layering Rules (enforced)

Dependencies point inward: `controller → service → repository → domain`. Never the reverse.

| Layer | May depend on | Must NOT |
|---|---|---|
| `controller/` | `service/`, `dto/` | touch repositories or entities directly; hold business logic; be `@Transactional` |
| `service/` | `domain/`, `repository/`, `dto` mapping, other services | build HTTP responses; know about `HttpServletRequest` |
| `domain/` (entities) | `enums/` | import Spring Web, JPA-query logic, or DTOs |
| `repository/` | `domain/` | contain orchestration or mapping |
| `dto/` `exception/` `validation/` | framework + `domain`/`enums` only | hold business logic |
| `adapter/` | product-facing interfaces only | reference a concrete engine or open a network client (Phase 2: empty) |

- **Controllers are thin:** parse → `@Valid` → delegate to a service → wrap in the envelope. No `@Transactional`, no queries.
- **Services own transactions and mapping:** `@Transactional` lives here; derived values (e.g. batch metrics) are produced here, never stored. Do not over-apply `@Transactional` (Alibaba P3C) — annotate the specific service methods that mutate across ≥2 statements; read-only reads use `@Transactional(readOnly = true)` or none. Blanket class-level transactions hurt throughput and hide boundaries.
- **Never serialize a JPA entity** over HTTP — always map to a DTO.

## Naming

- Packages: lowercase, singular role nouns (`controller`, `service`, `repository`, `domain`, `dto`, `enums`, `exception`, `validation`, `adapter`).
- Classes: `PascalCase`; suffix by role — `*Controller`, `*Service`, `*Repository`, `*Request`, `*Response`, `*Mapper`.
- Enums and their values match `frontend/src/types.ts` **exactly for `FileStatus`** (identical strings). `ReviewStatus` intentionally follows the wider REQ-PROD-030 set and diverges from the FE type — do not collapse it (see `metadata-api-data-model.md`).
- Methods: verb phrases (`createSpace`, `findByBatchIdAndStatus`, `computeMetrics`).
- Constants: `UPPER_SNAKE_CASE`; no magic numbers/strings that encode product behavior.
- **Boolean naming (Alibaba P3C, mandatory):** a Java boolean field/record component must **not** start with `is` (some serializers, e.g. Jackson, drop the `is` and mismatch the getter) — use `enabled`, `default`, `multimodalSupported`. The corresponding DB column *may* use the `is_xxx` form (e.g. `is_default`); the mapper bridges the two names.
- No special prefixes/suffixes on fields (`m`, `s_`, `_name`) — Google Java Style bans them.

## Formatting & Imports

- **Line length:** 100 characters (Google Java Style). Wrap continuation lines at +4 spaces; long URLs in comments are exempt.
- **Indentation:** 2 spaces per level (no tabs), UTF-8, K&R braces.
- **No wildcard imports** (`import java.util.*;`) — always import explicitly (Google Java Style; also avoids ambiguity for reviewers).
- **`@Override` is mandatory** on every method that overrides an interface/superclass method (Alibaba P3C).
- **Static members** are referenced by class name, not via an instance (Alibaba P3C).
- Formatting is enforced by Spotless/`google-java-format` in CI, not by hand.

## Documentation (Javadoc)

- **Every public class, method, and record component has Javadoc** (Google Java Style minimum). Self-evident getters/setters may be omitted.
- Javadoc opens with a summary fragment (noun/verb phrase), then documents params, return, and thrown exceptions for non-trivial/public methods.
- Comments explain **why**, not what (global coding style). Update comments when the logic changes; never leave stale commented-out code.
- Class-level `@author`/date tags are optional for this small team (a deliberate lightening of the Alibaba mandate) — Git history is the source of authorship.

## DTOs, Entities & Mapping

- **Request/response DTOs are Java `record`s** with Bean Validation annotations. Entities are JPA `@Entity` classes. They are separate types — never share one class across the boundary.
- Mappers live in `dto/mapping/` (e.g. `SpaceMapper`). Keep them pure and unit-tested.
- Expose `camelCase` JSON; persist `snake_case` columns. The mapper bridges casing.
- **Typed query objects (Alibaba P3C):** for any read with 2+ filter conditions, use a typed `*Query` record — never a `Map<String,Object>` of loosely-typed filters. E.g. file-list filters become `record FileQuery(String batchId, FileStatus status, int page, int size)`.

```java
// web/dto/CreateSpaceRequest.java
public record CreateSpaceRequest(
    @NotBlank @Size(max = 200) String name,
    String description,
    @NotNull SpaceType type,
    @NotNull IndexStrategy indexStrategy,
    String owner
) {}
```

## API Response Envelope (mandatory)

Every endpoint — success and error — returns the single envelope (`docs/03-spec/metadata-api-spec.md`, API guide):

```java
public record ApiEnvelope<T>(boolean success, T data, ErrorBody error, PageMeta meta) {
  public static <T> ApiEnvelope<T> ok(T data)            { return new ApiEnvelope<>(true, data, null, null); }
  public static <T> ApiEnvelope<T> ok(T data, PageMeta m){ return new ApiEnvelope<>(true, data, null, m); }
  public static <T> ApiEnvelope<T> fail(ErrorBody e)     { return new ApiEnvelope<>(false, null, e, null); }
}

public record ErrorBody(
    String code,                 // machine-readable, e.g. VALIDATION_ERROR
    String message,              // user-safe text
    Map<String, String> fields,  // field-level detail on validation errors, else null
    long timestamp,              // epoch millis — aligns with Spring's default error body
    String path                  // request path, e.g. /api/spaces/xyz
) {}
public record PageMeta(int page, int size, long total) {}
```

- `error.code` ∈ `VALIDATION_ERROR` (400) · `NOT_FOUND` (404) · `CONFLICT` (409) · `INTERNAL_ERROR` (500).
- **`timestamp` and `path` appear on error responses only** (mirrors Spring Boot's `DefaultErrorAttributes`), giving clients and observability tooling audit context. Success responses stay lean — no timestamp/status/path. The HTTP status line already carries the status code, so it is not duplicated in the success envelope.
- List endpoints always include `meta`; `total` is the full filtered count.
- Pagination via `?page=&size=` (0-based; default 20; max 200). Filtering via documented query params.

## Validation (at the boundary)

- Validate every inbound DTO with Bean Validation (`@Valid` on controller params). Invalid input never reaches persistence.
- Custom `@RelativePath` validator rejects absolute paths, drive/host prefixes, and `..` traversal — no `source_path` or artifact path is ever absolute (REQ-MA-009, data safety).
- Fail fast; return field-level, user-safe messages.

## Error Handling & Security

- One `@RestControllerAdvice` (`GlobalExceptionHandler`) maps exceptions → envelope:

```java
@RestControllerAdvice
class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiEnvelope<Void>> onValidation(MethodArgumentNotValidException ex) {
    Map<String,String> fields = ex.getBindingResult().getFieldErrors().stream()
        .collect(toMap(FieldError::getField, FieldError::getDefaultMessage, (a,b) -> a));
    return ResponseEntity.badRequest()
        .body(ApiEnvelope.fail(new ErrorBody("VALIDATION_ERROR", "Invalid request.", fields)));
  }
  // NotFoundException -> 404; ConflictException -> 409;
  // Exception -> 500 INTERNAL_ERROR with a generic message + correlation id
}
```

**Exception discipline (Alibaba P3C):**
- Throw purpose-built exceptions (`NotFoundException`, `ConflictException`, `ValidationException`) — the handler maps them to codes. Do not throw bare `RuntimeException`.
- Never swallow an exception: handle it, or rethrow with context. No empty `catch` blocks.
- Do not catch broad `RuntimeException`/`Exception` to mask a bug that a pre-check (null/bounds/state) should have prevented.
- Prefer `Optional<T>` over returning `null`; a method that can return null documents it in Javadoc.
- Never `return` from a `finally` block; use try-with-resources for closeables.

- **User-safe errors only.** Responses never contain stack traces, SQL, secrets, internal hostnames, or private absolute paths. `INTERNAL_ERROR` returns a generic message; the real cause is logged server-side with a correlation id (REQ-PROD-077).
- **No secrets in source.** Datasource URL/username/password come from externalized config (`${ATLAS_DB_URL}` etc.). No literal credentials in `application.yml`, code, logs, or seed data. Any future key field is status-only (`configured` / `not_configured`), never raw.
- **No auth in Phase 2** — the service is internal-only; RBAC is Phase 4. State this in a `SECURITY.md`/class note; do not deploy publicly as-is.
- **When auth arrives (Phase 4):** authentication/authorization failures are handled by a custom `AuthenticationEntryPoint` / `AccessDeniedHandler` (returning the same envelope with `401`/`403`), **separate** from the business `GlobalExceptionHandler` — this is the Spring Security convention for CSRF, Bearer-token, and OAuth2 flows. Do not fold auth failures into the validation/business exception path.

## Trace, Confidence & Review Invariants

- `source_path`, `confidence`, and `review_status` are preserved on every persisted and returned file/chunk record.
- Generated/low-confidence content persists as `REVIEW_REQUIRED`; only an explicit review action advances it. **Never** default to `APPROVED`. `PUBLISHED` is set only by a later publish slice.
- `review_record` is append-only — history rows are never updated or deleted.
- Derived values (batch metrics) are computed from persisted rows, never stored as a drift-prone counter.

## Persistence & Flyway

- **Flyway owns schema.** Configure both the Hibernate guard and Flyway validation (Spring Boot production guidance):

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate        # Hibernate never mutates the schema
  flyway:
    locations: classpath:db/migration
    validate-on-migrate: true   # fail fast if a migration checksum drifted
    baseline-on-migrate: true   # safe against a pre-existing database
    out-of-order: false         # migrations apply in strict version order
```
- Migration naming: `V<n>__<snake_case_description>.sql` (e.g. `V1__init_schema.sql`, `V2__seed_mock_metadata.sql`). Repeatable migrations use `R__`.
- **Migrations are immutable once merged.** A released migration is never edited; new changes are new versions.
- Seed data is **mock/sample only** — no real company content, credentials, or private paths; relative paths only.
- Add indexes intentionally for accepted query paths (e.g. `(batch_id, status)`); document why.
- Datasource is config-driven; PostgreSQL is the default target, not a hardcoded sole implementation.

**Schema conventions (Alibaba P3C, adapted to PostgreSQL):**
- Table and column names are `lower_snake_case`; table names are **singular** (`space`, `file_item`), never plural.
- Avoid SQL reserved words as identifiers (`desc`, `order`, `user`, `range`).
- Index naming: primary key `pk_<table>`, unique `uk_<table>_<cols>`, normal `idx_<table>_<cols>`.
- Use exact-precision `numeric`/`decimal` for confidence and money-like values — never `float`/`double`.
- **Deviation (noted above):** timestamps are `created_at`/`updated_at`, not `gmt_create`/`gmt_modified`.
- Queries select explicit columns/projections — never `SELECT *`; all parameters are bound (JPA/JPQL parameters), never string-concatenated (SQL-injection safety).

## Adapter Boundary (critical)

- Product logic must not call parser/converter/model/vector/storage engines directly. The `adapter/` package is a **reserved, empty seam** in Phase 2 and carries only `package-info.java`.
- A guard test asserts `adapter/` references no engine and wires no outbound network client, so accidental coupling fails CI.
- When Phase 3 adds adapters, they expose Atlas product concepts (capability metadata, config shape, success/failure output, retry behavior) — never vendor-specific detail — and call *into* the metadata service, not the reverse.

## Testing (maps to `mvn verify`)

Three tiers, all required as the layer becomes real (`DEVELOPMENT_STANDARDS.md` § Testing; global 80% minimum):

- **Unit (`service/`):** services, mappers, `MetricsCalculator` derivation, action→status mapping, the `REVIEW_REQUIRED` default invariant, `@RelativePath` validator. Fast, no Spring context where avoidable.
- **Contract (`controller/`):** `@WebMvcTest(XController.class)` with the service mocked (`@MockitoBean`) — fast, web-layer-only. Assert status codes, envelope shape, pagination `meta`, `400` field-level validation, `404`, and user-safe error bodies (no stack/SQL/secret/absolute path).
- **Integration (`integration/`):** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers PostgreSQL — real beans end to end: repository CRUD + paging + filter, Flyway `V1`+`V2` apply cleanly, `ddl-auto=validate` passes, seed rows match FE-baseline expectations. Name integration tests `*IT`.

Pick the narrowest tier that proves the behavior: `@WebMvcTest` for controller logic, `@DataJpaTest` for repository queries, `@SpringBootTest` only when the full wiring matters. Use **AssertJ** fluent assertions (`assertThat(...)`) for readability.
- **Adapter-seam guard:** asserts `adapter/` has no engine/network dependency.

```java
// integration/MigrationValidationIT.java
@SpringBootTest
@Testcontainers
class MigrationValidationIT {
  @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");
  @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", pg::getJdbcUrl);
    r.add("spring.datasource.username", pg::getUsername);
    r.add("spring.datasource.password", pg::getPassword);
  }
  @Test void flywayMigratesAndSchemaValidates() { /* context load == migrate + validate pass */ }
}
```

> Note: Testcontainers pulls a Postgres image at test time (network in CI). This is an intentional, phase-appropriate dependency — distinct from the Phase 0/1 "no external network" rule, which governs the prototype/frontend runtime, not the backend test harness.

## Code Quality Checklist (Before Commit)

- [ ] **Layering**: `controller → service → repository → domain`; one role per package; controllers thin; no entity serialized over HTTP.
- [ ] **Envelope**: every endpoint returns `ApiEnvelope`; lists include `PageMeta`.
- [ ] **Validation**: all DTOs `@Valid`; paths relative + traversal-checked; invalid input → 400 with fields.
- [ ] **Errors/secrets**: no stack/SQL/secret/hostname/absolute path in responses or logs; datasource from config, no literals.
- [ ] **Trace/review**: `source_path`+`confidence`+`review_status` preserved; generated content `REVIEW_REQUIRED`, never `APPROVED`; `review_record` append-only.
- [ ] **Enums**: `FileStatus` identical to FE + `batch-processing-design.md`; `ReviewStatus` follows REQ-PROD-030 (documented divergence).
- [ ] **Flyway**: versioned, immutable migrations; `ddl-auto=validate`; seed is mock-only, relative paths.
- [ ] **Adapter seam**: `adapter/` empty; no engine call; guard test present.
- [ ] **Tests**: unit + contract + integration for touched behavior; ≥80% coverage of modified code.
- [ ] **Size/naming**: files <800 lines, methods <50 lines, nesting <4; role-suffixed class names.
- [ ] **Boolean/import/format**: no boolean Java field starts with `is`; no wildcard imports; lines ≤100 chars; `@Override` on all overrides.
- [ ] **Javadoc**: every public class/method/record component documented; comments explain why.
- [ ] **Error body**: error responses carry `code`, user-safe `message`, `fields` (on validation), `timestamp`, `path`; success responses stay lean.
- [ ] **Exceptions**: purpose-built exceptions, none swallowed, `Optional` over null, no `return` in `finally`.
- [ ] **DB conventions**: singular snake_case tables, `pk_/uk_/idx_` index names, `numeric` not float, explicit columns (no `SELECT *`), bound parameters.
- [ ] **Transactions**: `@Transactional` scoped to methods that need it, not blanket class-level.

## Build, Verify & CI

```bash
cd backend
mvn -q compile          # fast compile check
mvn verify              # compile + Flyway validate + unit + contract + integration
mvn -q flyway:migrate   # optional explicit migration against a local/test datasource
git diff --check        # whitespace/conflict hygiene
```

Recommended `.github/workflows/backend-ci.yml` (grows per `DEVELOPMENT_STANDARDS.md` § CI Direction):

1. Format / diff hygiene (Spotless or equivalent) + `git diff --check`.
2. `mvn -q compile`.
3. `mvn verify` (unit + contract + integration via Testcontainers) with coverage report (JaCoCo, ≥80% on changed code).
4. Flyway migration validation.
5. Secret / dependency scan (no raw credentials, no new external runtime deps beyond the JDBC datasource).

## Examples & References

- `docs/05-design/metadata-api-design.md` — module layout, layer contracts, DTO/envelope design.
- `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md` — endpoint request/response contracts.
- `docs/04-architecture/metadata-api-data-model.md` — persisted schema, enums, invariants.
- `docs/03-spec/metadata-api-spec.md` — behavior source of truth (envelope, errors, state model).
- `DEVELOPMENT_STANDARDS.md` §§ Backend / API / Database / Adapter / Testing / CI — the governing standard this document makes concrete.

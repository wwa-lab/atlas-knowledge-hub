# Backend

Spring Boot metadata/control-plane API for Atlas Knowledge Hub.

Stack:

- Java 21 + Spring Boot (artifact `metadata-api`, package `com.atlas.metadata`).
- PostgreSQL for persistent metadata, with Flyway schema migrations under `src/main/resources/db/migration`.
- Adapter boundaries for conversion, parser, storage, vector, model, and connector integrations.

The backend is already implemented (controllers, services, repositories, Flyway migrations, and tests). Do not expand backend scope, schema, authentication/RBAC, or permission systems beyond what accepted SDD slices and `PROJECT_RULES.md` allow. Backend APIs cover workspaces, batches, files, reviews, Wiki pages, source chunks, and graph metadata, among others.

See [BACKEND_CODING_STANDARD.md](../docs/BACKEND_CODING_STANDARD.md) for Java/Spring layering, envelope, Flyway, and testing standards. The metadata-api slice is specified under `docs/**/metadata-api*` (spec, data model, and API guide).

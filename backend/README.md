# Backend

Backend implementation placeholder.

Selected backend direction:

- Java + Spring Boot.
- PostgreSQL for persistent metadata.
- Flyway for schema migrations.

Do not add a production backend, authentication, database, or permission system until explicitly requested or until the roadmap phase calls for it. Future backend work should expose internal APIs for workspaces, batches, files, reviews, Wiki pages, source chunks, and graph metadata.

See [BACKEND_CODING_STANDARD.md](../docs/BACKEND_CODING_STANDARD.md) for Java/Spring layering, envelope, Flyway, and testing standards. The first backend slice is specified under `docs/**/metadata-api*` (spec, data model, and API guide).

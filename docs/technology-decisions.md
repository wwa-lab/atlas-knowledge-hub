# Technology Decisions

This document records current stack decisions without scaffolding implementation code yet.

## Delivery Method

Use lightweight SDD when implementation begins. Each meaningful feature phase should have:

- A short spec or change brief.
- Design notes for affected architecture.
- A small implementation task list.

Small documentation and prototype-only changes do not need heavyweight SDD.

Development standards are staged by phase in `DEVELOPMENT_STANDARDS.md`. Phase 0 uses lightweight prototype checks; later phases add frontend tests, backend contract tests, database migration discipline, adapter tests, and CI gates as the implementation layers become real.

## Selected Stack

- Frontend: Vue 3 + Vite + TypeScript after static prototype validation.
- Backend: Java + Spring Boot.
- Database: PostgreSQL.
- Migration management: Flyway.

## Timing

Do not scaffold the full stack during Phase 0. Add it when the project enters implementation phases:

- Phase 1: mock upload workflow and SDD artifacts.
- Phase 2: Spring Boot backend and converter adapter integration.
- Phase 2 or Phase 3: PostgreSQL and Flyway when persistent batch/review metadata is needed.

## Backend Shape

The backend should keep clear layers:

- API/controller layer for internal endpoints.
- Application/service layer for workflow orchestration.
- Adapter layer for converter/parser integrations.
- Repository layer for PostgreSQL persistence.
- Migration layer managed by Flyway.

## Database Scope

Initial PostgreSQL entities are expected to include:

- workspace
- batch
- file_item
- wiki_page
- source_chunk
- review_record
- graph_node
- graph_edge

Schema changes should be explicit Flyway migrations and documented with the behavior they support.

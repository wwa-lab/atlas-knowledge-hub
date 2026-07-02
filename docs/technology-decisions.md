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
- Backend: Java + Spring Boot as the Atlas control plane.
- Database: PostgreSQL.
- Migration management: Flyway.
- Agent, parser, model, vector, and storage integrations: external adapter or worker plane behind product-facing contracts.

## Backend Direction

Atlas should keep Java + Spring Boot as the primary backend choice.

Reasoning:

- The team is most familiar with Java and Spring Boot.
- Atlas needs a reliable internal control plane for metadata, workflow state, review state, membership, audit, and API contracts.
- Agent and document-processing integrations should be isolated behind adapter or worker boundaries instead of forcing the core backend to run every runtime directly.
- Future Python, Node.js, Go, or other workers can integrate with Spring Boot through HTTP, gRPC, message queues, or job records without changing the product-facing API.

Spring Boot owns:

- Knowledge Space, batch, file item, Wiki, review, graph, member, setting, and model metadata.
- Internal APIs and contract validation.
- Workflow state and status transitions.
- Authentication and RBAC when production security is introduced.
- Audit records for user actions, agent runs, tool calls, and publication decisions.
- Adapter registry and policy decisions.

Spring Boot should not directly become the agent runtime by default. It may call agent workers, parser workers, model adapters, vector adapters, and storage adapters, but product workflows should depend on stable Atlas interfaces rather than concrete SDKs.

## Agent And Worker Integration

Atlas uses a control-plane / worker-plane model:

- Control plane: Spring Boot owns state, policy, API contracts, validation, audit, and persistence.
- Worker plane: external workers execute parser, OCR, document AI, model calls, agent runs, tool calls, vectorization, and storage operations.

Worker implementations may use the best runtime for the job, such as Python for document AI/OCR, Node.js or Python for agent runtimes and MCP/tool integrations, or Java for internal services. This avoids limiting future agent integration while preserving a stable Java backend.

Recommended integration progression:

1. Phase 2: Start with Spring Boot metadata APIs and explicit job records.
2. Phase 3: Add adapter/worker contracts for parser, converter, model, vector, storage, and agent runs.
3. Phase 3 or later: Introduce HTTP/gRPC worker calls or a queue when real asynchronous processing needs it.
4. Phase 4: Add production policies for credentials, permissions, rate limits, audit, observability, and retries.

Agent-generated or LLM-generated outputs must be treated as untrusted until reviewed. They must retain source trace, prompt/model/tool metadata when available, confidence or evidence markers, and review status.

## Timing

Do not scaffold the full stack during Phase 0. Add it when the project enters implementation phases:

- Phase 1: mock upload workflow and SDD artifacts.
- Phase 2: Spring Boot backend and converter adapter integration.
- Phase 2 or Phase 3: PostgreSQL and Flyway when persistent batch/review metadata is needed.

## Backend Shape

The backend should keep clear layers:

- API/controller layer for internal endpoints.
- Application/service layer for workflow orchestration.
- Adapter layer for converter/parser/model/vector/storage/search/agent integrations.
- Repository layer for PostgreSQL persistence.
- Migration layer managed by Flyway.

The backend domain model should include product-level records for agent and tool activity before integrating a concrete agent framework:

- agent_run
- tool_invocation
- tool_result
- artifact
- review_required_output
- audit_event

These names are directional, not committed database table names. Final schemas should be defined in the relevant SDD data model and Flyway migrations.

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

# Architecture: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Architectural Drivers

- REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 require one safe backend error boundary, deterministic local throttling, frontend classification, tests, and traceability.
- The slice extends existing Spring Boot and Vue paths; it does not add external services or change auth/audit semantics.

## System Context

```
Users
  |
Vue Product Shell + API Client
  |
Spring Boot /api/**
  |
Local Rate Limiter -> Auth/RBAC Interceptor -> Controllers/Services
  |
Safe Error Mapper + Global Exception Handler
  |
ApiEnvelope(success=false, error=safe body)
```

## Components

- Safe error model: extends the existing error object with safe code, message, fields, timestamp, path, correlation id, and retry metadata.
- Safe error sanitizer: redacts forbidden strings before response serialization.
- Global exception handler: maps validation, not found, conflict, and unexpected errors to safe codes.
- Auth/RBAC interceptor: keeps current auth decisions but emits safe `AUTHENTICATION_REQUIRED` and `PERMISSION_DENIED` envelopes.
- Local rate-limit interceptor: deterministic in-memory limiter for `/api/**`, ordered before or alongside auth without external stores.
- Frontend API client: converts safe codes into typed UI categories.
- Product shell: shows representative safe states.

## Boundaries

- Rate limit storage is local in-memory only and not a production distributed quota store.
- Adapter boundaries for parser, converter, model, vector, storage, and search remain unchanged.
- Audit behavior is not redefined. Server logs keep safe troubleshooting context.
- No external network calls, cloud providers, Redis, gateway, service mesh, or real secrets.

## Security

AC-RATE-LIMIT-SAFE-ERRORS-002 is enforced by a sanitizer and tests. The API must not expose raw stack traces, raw exception class names, secrets, credentials, private paths, internal endpoints, or raw source content.

## Architecture Review Result

Architecture-review applied: no P0/P1 violations identified in the SDD design. P2 residual risk: local in-memory rate limiting is intentionally not production-grade and must remain labeled as prototype/local-safe.

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-001 through T-RATE-LIMIT-SAFE-ERRORS-008 implement this architecture and preserve REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012.

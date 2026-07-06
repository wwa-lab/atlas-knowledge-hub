# System Architecture: audit-log-foundation

Status: Draft for human review
Last updated: 2026-07-06

## Overview

- **Architecture summary:** The slice adds a centralized audit domain inside the existing Spring Boot metadata service. Domain services and the auth interceptor emit safe `AuditEvent` records through an `AuditLogService`; read APIs expose paginated, RBAC-protected audit DTOs to the Vue frontend.
- **Design objective:** Capture high-value governance evidence without storing raw sensitive payloads.
- **Architectural style:** Layered Spring Boot service with PostgreSQL/Flyway persistence and Vue read-only UI integration.

## Source Specification

- **Feature / system name:** audit-log-foundation
- **Scope summary:** General append-only audit event capture and safe audit read surfaces for core Atlas operations.

## Architectural Drivers

### Functional Drivers

- Central event shape for auth, membership, review/publish, Wiki, graph, Ask, settings, model, and adapter operations.
- RBAC-protected read APIs.
- Safe summary and metadata allowlist.
- Preservation of current graph audit evidence.

### Non-Functional Drivers

- No raw secrets, raw content, private paths, provider payloads, stack traces, or raw logs.
- Append-only persistence.
- Paginated and indexed read access.
- No external cloud calls or new dependencies.

## System Context

| Actor/System | Role |
|---|---|
| Atlas frontend | Displays audit history when capability allows it. |
| Auth/RBAC boundary | Supplies current user, role/capability result, and safe denied outcomes. |
| Domain services | Emit audit events for sensitive operations. |
| PostgreSQL | Persists append-only audit events. |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Auditor · Space Owner · Knowledge Manager · Platform Admin   │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTPS
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Frontend                                                  │
│ Audit panel · filters · empty/error/denied states             │
└────────────────────────────┬─────────────────────────────────┘
                             │ REST / JSON ApiEnvelope
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                      │
│ Auth interceptor · Audit controller · Domain controllers       │
├──────────────────────────────────────────────────────────────┤
│ AuditLogService                                                │
│ Safe event builder · metadata allowlist · read policy checks    │
├──────────────────────────────────────────────────────────────┤
│ Domain services emit safe events                               │
│ Membership · Review/Publish · Wiki · Graph · Ask · Adapters    │
└────────────────────────────┬─────────────────────────────────┘
                             │ JPA / Flyway
                             ▼
┌──────────────────────────────────────────────────────────────┐
│ PostgreSQL atlas.audit_event                                  │
│ Append-only records · space/time and target/time indexes       │
└──────────────────────────────────────────────────────────────┘
```

## Component Breakdown

### Backend Components

- **Audit event domain:** Immutable entity and enums for action, category, result, and severity.
- **AuditLogService:** Single write boundary that accepts safe event commands and persists append-only events.
- **AuditEventRepository:** Query support by space/time, target/time, actor/action/result/category, and pagination.
- **AuditLogController:** Read-only API for space and target audit queries.
- **Auth audit hook:** Records denied decisions from the existing `AtlasAuthInterceptor` when a safe scope can be derived.
- **Domain audit hooks:** Representative emitters in membership, review/publish, Wiki, graph, Ask, model/settings, and adapter services.

### Frontend Components

- **Audit panel:** Read-only list and filters.
- **Audit API client/types:** Typed DTOs and query parameters.
- **Capability gate:** Uses `/api/auth/me` capabilities and safe API errors rather than hardcoded local role checks.

## Data Architecture

| Entity | Responsibility |
|---|---|
| AuditEvent | Append-only governance record. |
| AuditEventMetadata | Safe bounded JSON for counts, reason codes, and correlation ids. |
| AuditEventQuery | Read model filter and pagination contract. |

Existing `graph_audit_record` is a narrow graph-governance record. New implementation should either write both graph legacy and general audit events for graph actions, or expose graph legacy records through a documented bridge until a cleanup slice. The SDD does not require destructive migration.

## Integration Architecture

- **Auth/RBAC:** Reuses `CurrentUserContext`, `AuthDecision`, and role/capability checks from `auth-space-rbac`.
- **Domain services:** Emit through the centralized audit service; product logic does not write directly to repositories.
- **Adapters:** Parser/converter/model/vector/storage remain behind existing adapter boundaries; audit stores only safe operation summaries.

## Security / Reliability / Observability

- Audit writes use safe identifiers and summaries only.
- Audit read APIs require governance-read capability.
- Audit logging must avoid recursive logging of audit-read requests in v0 unless explicitly enabled later.
- Audit write failures must be handled deliberately per category and tested.

## Risks And Tradeoffs

- Capturing every read is noisy; v0 focuses on denied access and high-value writes.
- A bridge from graph audit to general audit avoids destructive migration but may temporarily duplicate graph evidence.
- If audit write is non-blocking for some operations, operational safe logs must still make write failures visible.

## Traceability

- REQ-AUDIT-LOG-FOUNDATION-001, REQ-AUDIT-LOG-FOUNDATION-002, and T-AUDIT-LOG-FOUNDATION-001 through T-AUDIT-LOG-FOUNDATION-003 map to the audit domain, service, repository, and migration architecture.
- REQ-AUDIT-LOG-FOUNDATION-003 through REQ-AUDIT-LOG-FOUNDATION-006 and T-AUDIT-LOG-FOUNDATION-004 through T-AUDIT-LOG-FOUNDATION-005 map to auth/domain audit emitters and graph audit preservation.
- REQ-AUDIT-LOG-FOUNDATION-007 through REQ-AUDIT-LOG-FOUNDATION-010 and T-AUDIT-LOG-FOUNDATION-006 through T-AUDIT-LOG-FOUNDATION-007 map to read APIs, RBAC checks, and frontend audit inspection.

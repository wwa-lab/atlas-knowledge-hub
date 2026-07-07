# Architecture: secret-manager-integration

Status: Preauthorized accepted
Last updated: 2026-07-07

## Overview

The slice introduces a shared secret status contract in the Spring Boot API layer and consumes it in the Vue settings surface. It does not add a new secret manager runtime. Raw values remain inside the existing runtime/model adapter boundaries, while API responses expose only status and logical references.

## Architecture Diagram

```text
┌──────────────────────────────────────────────────────────────┐
│ Platform Admin                                                │
└──────────────────────────────┬───────────────────────────────┘
                               │ HTTPS / JSON
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue Settings Surface                                          │
│ model · parser · converter · storage · vector status display  │
└──────────────────────────────┬───────────────────────────────┘
                               │ REST / ApiEnvelope
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                      │
│ model config endpoints · capability endpoints · DTO mappers   │
├──────────────────────────────────────────────────────────────┤
│ Secret Status Contract                                        │
│ SecretReferenceResponse · SecretStatusResponse                │
├──────────────────────────────────────────────────────────────┤
│ Adapter Boundaries                                            │
│ model · parser · converter · storage · vector                 │
└──────────────────────────────┬───────────────────────────────┘
                               │ raw values stay internal
                               ▼
┌──────────────────────────────────────────────────────────────┐
│ Runtime / Environment Configuration                           │
│ process env and in-memory runtime config only                 │
└──────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility |
|---|---|
| Shared DTOs | Represent logical secret references and status-only configuration. |
| ModelRuntimeConfigurationService | Accept write-only replacement input, retain/clear runtime state, return masked model configuration. |
| Adapter capability mappers | Attach typed secret statuses to existing masked capability summaries. |
| Adapter implementations | Keep raw provider/runtime values private to execution boundaries. |
| Vue settings | Render status chips and replace/remove affordances without displaying raw values. |

## Architectural Constraints

- No direct UI dependency on provider secret formats.
- No new external provider or secret-manager call.
- `maskedConfigSummary` remains a compatibility field.
- Typed secret status is the preferred contract for new UI and tests.

## Architecture Review Result

Architecture review required: yes, because the slice touches secret handling, API contracts, and adapter boundaries.
Result: no architecture blockers identified in the SDD scope; the production secret manager adapter remains explicitly deferred.

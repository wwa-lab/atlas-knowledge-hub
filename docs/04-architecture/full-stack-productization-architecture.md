# Architecture: Full-Stack Productization

## Overview

Atlas P0 productization is a layered full-stack metadata workflow. Vue owns user interaction and state presentation; Spring Boot owns product APIs, validation, persistence, and orchestration; adapters own parser/converter/model/vector/storage/graph engine execution behind backend boundaries.

## Architecture Diagram

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge user · Delivery lead · SME reviewer                │
└─────────────────────────────┬────────────────────────────────┘
                              │ Browser
                              ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue P0 App                                                   │
│ Space list · Batch/files · Review · Wiki · Graph · Ask       │
└─────────────────────────────┬────────────────────────────────┘
                              │ REST / JSON envelope
                              ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                     │
│ Space · Batch/File · Review/Publish · Graph · Vector · Ask   │
├──────────────────────────────────────────────────────────────┤
│ Domain Services                                               │
│ Source trace · review status · publish eligibility · evidence │
├─────────────────────────────┬────────────────────────────────┤
│ Repositories / Flyway DB     │ Adapter Registries             │
│ PostgreSQL metadata          │ graph/vector/model/mock engines │
└─────────────────────────────┴────────────────────────────────┘
```

## Component Boundaries

| Component | Responsibility |
|---|---|
| Vue P0 app | Drives browser workflow, calls Atlas APIs, renders loading/empty/error/success states, disables unconnected affordances. |
| API client layer | Normalizes base URL, unwraps envelopes, classifies safe errors, sends required graph headers. |
| Space/Batch/File services | Persist and expose safe metadata-only P0 upload inventory. |
| Review/Publish services | Enforce review transitions and publish eligibility. |
| Graph service | Projects and reads trusted evidence through graph adapter boundaries. |
| Vector service | Indexes/query approved source chunks through vector adapter boundaries. |
| Ask service | Queries vector evidence and generates answer through model adapter boundaries. |

## Architectural Constraints

- Frontend calls only Atlas backend endpoints, never engines or providers.
- Backend controller/service/repository layering remains intact.
- Sample upload is metadata-only; no file bytes, production storage, or real documents.
- P0 graph/vector refresh may be synchronous because the current backend endpoints are synchronous mock-safe operations.
- Production auth/RBAC remains out of scope; graph header guard remains the existing local hardening pattern.

## Architecture Review Notes

- The slice composes existing implemented endpoints instead of adding a parallel mock backend.
- The only acceptable backend additions are minimal API gaps discovered during implementation.
- Frontend fallback mock behavior must not masquerade as connected product behavior.

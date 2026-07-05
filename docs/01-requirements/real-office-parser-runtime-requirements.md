# Requirements: Real Office Parser Runtime

## Status

Draft for user review. Product code must wait for explicit user acceptance.

## Slice Contract

- **Slice:** `real-office-parser-runtime`
- **Wave:** Wave 2 / Runtime Integration
- **Goal:** Enable controlled internal runtime execution for Office conversion and PDF parsing behind the existing Atlas converter and parser adapter boundaries.
- **Maturity target:** SDD-ready runtime integration slice. This is not production readiness, RBAC, connector sync, worker scale-out, or unrestricted real-data ingestion.

## Source Context

This slice builds on the accepted and implemented adapter foundation:

- `converter-adapter` already defines `ConverterAdapter`, capability metadata, conversion runs, safe status mapping, and a mock `trinity-office` adapter.
- `parser-adapter` already defines `ParserAdapter`, capability metadata, parser runs, safe status mapping, source chunks, and a mock `document-normalize` adapter.
- Existing optional real wrappers currently expose masked capability status but do not execute real tools.
- `wiki-ingest-v0` and `wiki-linkify-lint` consume approved/generated metadata only after review-safe processing.

## Requirements

| ID | Requirement | Priority | Source |
|---|---|---|---|
| REQ-REAL-OFFICE-PARSER-RUNTIME-001 | Implementation must be blocked until this bilingual SDD set is accepted by the user. | Must | Goal-driven SDD gate |
| REQ-REAL-OFFICE-PARSER-RUNTIME-002 | Atlas must enable real `trinity-office` conversion only through the existing product-facing converter adapter contract. | Must | REQ-PROD-015, REQ-PROD-016 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-003 | Atlas must enable real `document-normalize` parsing only through the existing product-facing parser adapter contract. | Must | REQ-PROD-015, REQ-PROD-016 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-004 | Product controllers, services, repositories, domains, frontend code, and Wiki/Ask/Graph workflows must not call runtime commands, SDKs, HTTP clients, or tool-specific APIs directly. | Must | Adapter boundary |
| REQ-REAL-OFFICE-PARSER-RUNTIME-005 | Runtime selection must be configuration-gated with safe capability metadata: `AVAILABLE`, `DISABLED`, or `MISCONFIGURED`, plus masked status-only config summaries. | Must | Adapter capability pattern |
| REQ-REAL-OFFICE-PARSER-RUNTIME-006 | Automated verification must continue to pass without installed real binaries by using mocks/fakes; real runtime execution must be opt-in by configuration or test harness. | Must | CI safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-007 | Runtime execution must use mock/sample-safe files only in repository tests and fixtures; no real company documents, logs, private paths, or confidential screenshots may be committed. | Must | Data safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-008 | Runtime inputs and outputs must remain inside safe relative artifact roots and must reject traversal, absolute paths, URI-prefixed paths, hostnames, and raw local paths in responses. | Must | Path safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-009 | Runtime stdout/stderr, exception messages, and adapter safe messages must be sanitized before persistence or API response. | Must | Secret/path safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-010 | Conversion runtime results must map only to accepted file statuses such as `PDF_CONVERTED`, `PDF_CONVERT_FAILED`, `OCR_REQUIRED`, `FAILED`, and `UNSUPPORTED`. | Must | Converter contract |
| REQ-REAL-OFFICE-PARSER-RUNTIME-011 | Parser runtime results must map only to accepted file statuses such as `MARKDOWN_GENERATED`, `LOW_CONFIDENCE`, `OCR_REQUIRED`, `FAILED`, and `UNSUPPORTED`. | Must | Parser contract |
| REQ-REAL-OFFICE-PARSER-RUNTIME-012 | Parser runtime output must preserve source trace, PDF path, Markdown path, assets path, confidence, parser key, source chunks, and review status. | Must | Trace/review rules |
| REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Runtime integration must not auto-publish Wiki pages, auto-approve review-required content, call LLMs, create graph edges, or write Ask indexes. | Must | Trust boundary |
| REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Runtime failure, timeout, missing binary, invalid output, and partial file failure must produce bounded safe run evidence and leave unrelated metadata unchanged. | Must | Reliability |
| REQ-REAL-OFFICE-PARSER-RUNTIME-015 | Adapter seam guard and safety scans must prove tool-specific names and command execution APIs appear only in allowed adapter/runtime packages and tests. | Must | Static guard |
| REQ-REAL-OFFICE-PARSER-RUNTIME-016 | The SDD must record open runtime contract questions, including command invocation shape, timeout policy, artifact materialization, and local process versus worker topology. | Must | Architecture readiness |

## Explicit Exclusions

- Production authentication, RBAC, audit policy, secret-manager integration, and rate limiting.
- External cloud parsing or model calls.
- Connector sync, large-scale worker queue, dead-letter queue, and operations runbook.
- Real company document ingestion in repository tests or fixtures.
- Wiki review gate, refresh/retract automation, graph extraction, and trusted Ask indexing.

## Acceptance Criteria

| Requirement | Observable acceptance |
|---|---|
| REQ-REAL-OFFICE-PARSER-RUNTIME-001 | Traceability records user acceptance before implementation begins. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-002 to 004 | Spec/design/tasks require all runtime execution through adapter interfaces and static guard tests. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-005 to 006 | API guide and tasks define masked capability behavior and mock-safe CI verification. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-007 to 009 | Tasks include focused secret/private-path and command-output sanitization checks. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-010 to 012 | Spec defines status mapping and trace/review preservation for conversion and parsing. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Out-of-scope and state model prohibit publish/trust/Ask/Graph side effects. |
| REQ-REAL-OFFICE-PARSER-RUNTIME-014 to 016 | Data flow/design/tasks include failure handling, seam guard, and open questions. |

## Assumptions

- The first implementation target remains Java/Spring Boot backend adapter code.
- Real binaries or workers are optional and configuration-gated; absence of those binaries must not fail normal CI.
- The existing conversion and parser API shapes are reused unless implementation reveals a documented mismatch.

## Open Questions

| ID | Question | Owner |
|---|---|---|
| OQ-REAL-OFFICE-PARSER-RUNTIME-001 | Is the preferred first runtime topology local process execution, a sidecar worker, or a remote internal worker endpoint? | Architecture |
| OQ-REAL-OFFICE-PARSER-RUNTIME-002 | What is the exact approved `trinity-office` command contract, including input/output directory expectations? | Platform |
| OQ-REAL-OFFICE-PARSER-RUNTIME-003 | What is the exact approved `document-normalize` contract, including Markdown/assets/report output layout? | Platform |
| OQ-REAL-OFFICE-PARSER-RUNTIME-004 | What timeout and max-output policy should be used for controlled internal runs? | Platform / Security |

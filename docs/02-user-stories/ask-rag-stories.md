# User Stories: Ask RAG

## Status

Draft. Derived from `docs/01-requirements/ask-rag-requirements.md` using `req-to-user-story`.

## Story Map

| Story | Title | Requirements |
|---|---|---|
| US-ASKRAG-001 | Ask scoped questions in a Knowledge Space | REQ-ASKRAG-001, REQ-ASKRAG-010 |
| US-ASKRAG-002 | See source-grounded answers and evidence | REQ-ASKRAG-002, REQ-ASKRAG-003, REQ-ASKRAG-005, REQ-ASKRAG-013 |
| US-ASKRAG-003 | Refuse answers without approved evidence | REQ-ASKRAG-012 |
| US-ASKRAG-004 | Execute Ask through adapter boundaries | REQ-ASKRAG-004, REQ-ASKRAG-011 |
| US-ASKRAG-005 | Preserve review-required output and audit evidence | REQ-ASKRAG-006, REQ-ASKRAG-009 |
| US-ASKRAG-006 | Validate and fail safely | REQ-ASKRAG-007, REQ-ASKRAG-008, REQ-ASKRAG-014 |

## US-ASKRAG-001: Ask Scoped Questions In A Knowledge Space

**Story:** As a knowledge user, I want to ask a question inside a selected Knowledge Space, so that the answer is grounded in the space I am currently inspecting.

### Acceptance Criteria

1. **Given** I am viewing a Knowledge Space Ask tab
   **When** I submit a question
   **Then** the request is scoped to that space id and does not search unrelated spaces.
2. **Given** the Ask tab is rendered
   **When** I use keyboard navigation or a narrow viewport
   **Then** the question input, submit control, answer panel, and evidence list remain reachable and readable.
3. **Given** the UI is in day or night mode
   **When** an answer, empty state, or error state appears
   **Then** the state follows the current Atlas FE baseline and does not overlap text or hide evidence.

## US-ASKRAG-002: See Source-Grounded Answers And Evidence

**Story:** As a developer or SME reviewer, I want every Ask answer to show source references and confidence, so that I can verify where the answer came from.

### Acceptance Criteria

1. **Given** approved evidence exists
   **When** Ask returns an answer
   **Then** the answer includes confidence, review status, and an evidence list with source file, page or section, source chunk id, and score.
2. **Given** review-required evidence is explicitly included
   **When** Ask returns mixed evidence
   **Then** the API and UI separate trusted evidence from review-required evidence and show a warning.
3. **Given** a generated answer is returned
   **When** the answer is displayed or persisted as evidence
   **Then** the answer output remains `REVIEW_REQUIRED`.

## US-ASKRAG-003: Refuse Answers Without Approved Evidence

**Story:** As a knowledge user, I want Atlas to refuse unsupported answers, so that I do not mistake unreviewed or missing evidence for trusted knowledge.

### Acceptance Criteria

1. **Given** no approved evidence matches the question
   **When** I submit the question with the default review policy
   **Then** Ask returns a no-evidence response and does not call the model adapter to synthesize an answer.
2. **Given** only review-required evidence matches
   **When** I use the default approved-only policy
   **Then** Ask explains that approved evidence is required.
3. **Given** review-required evidence inclusion is explicitly enabled
   **When** evidence is returned
   **Then** any answer and evidence are clearly marked as review-required.

## US-ASKRAG-004: Execute Ask Through Adapter Boundaries

**Story:** As a platform administrator, I want Ask to use vector and model adapter contracts, so that model and retrieval engines remain replaceable and safe to mock.

### Acceptance Criteria

1. **Given** an Ask request passes validation
   **When** retrieval starts
   **Then** the service uses vector query contracts and does not call vector engines directly.
2. **Given** evidence is available
   **When** answer generation starts
   **Then** the service uses model-adapter contracts and does not call provider SDKs, CLIs, or external HTTP clients directly.
3. **Given** automated tests run
   **When** Ask behavior is verified
   **Then** tests use deterministic mock adapters without credentials or network access.

## US-ASKRAG-005: Preserve Review-Required Output And Audit Evidence

**Story:** As a delivery lead, I want Ask activity to be auditable without leaking confidential data, so that trusted answers can be reviewed later.

### Acceptance Criteria

1. **Given** Ask completes successfully
   **When** the answer evidence record is stored
   **Then** it includes safe question summary, answer status, review policy, evidence references, model run id, timestamps, and requested-by identity.
2. **Given** a generated answer exists
   **When** records are inspected
   **Then** Wiki, source chunk, graph, file, and review statuses are unchanged.
3. **Given** logs or responses are inspected
   **When** Ask has processed a request
   **Then** raw secrets, provider payloads, private paths, stack traces, and confidential source text are absent.

## US-ASKRAG-006: Validate And Fail Safely

**Story:** As an operator, I want invalid or failing Ask requests to return safe, actionable errors, so that users understand what happened without leaking internals.

### Acceptance Criteria

1. **Given** a request has an invalid space, empty question, oversized question, invalid policy, unsafe filter, or invalid limit
   **When** it is submitted
   **Then** the API rejects it before adapter execution with a `VALIDATION_ERROR` envelope.
2. **Given** a vector or model adapter is unavailable or fails
   **When** Ask handles the failure
   **Then** the response contains a sanitized safe message and no raw adapter diagnostics.
3. **Given** implementation is complete
   **When** verification runs
   **Then** unit, integration, API contract, E2E, seam guard, dependency/network, and secret/private-path checks pass or are explicitly reported as blocked.

## Dependencies

- Metadata API source chunk, review status, and envelope behavior.
- Vector adapter query evidence contract.
- Model adapter mock run contract.
- FE Ask tab baseline in the static prototype and Vue shell.

## Out Of Scope

- Real provider credentials, real external network calls, streaming, answer publication, graph extraction, and production SSO/RBAC setup.

## Open Questions

- OQ-ASKRAG-001: Review-required evidence visibility for non-reviewers.
- OQ-ASKRAG-002: Whether Ask answers become review queue items later.
- OQ-ASKRAG-003: Whether first implementation includes reranking.

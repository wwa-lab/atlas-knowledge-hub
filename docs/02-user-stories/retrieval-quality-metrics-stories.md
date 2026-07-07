# User Stories: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

## Story Set

### US-RETRIEVAL-QUALITY-METRICS-001: Inspect Ask run quality

As a knowledge operator, I want safe retrieval quality metrics for a Trusted Ask run, so that I can judge whether an answer was evidence-grounded enough for review.

Acceptance:
- Given a stored Ask run with evidence, when metrics are requested, then evidence coverage, citation health, confidence, and review eligibility are returned.
- Given the same stored records, when metrics are requested repeatedly, then the output is deterministic.
- Given an Ask run with no evidence, when metrics are requested, then the run is counted as a no-evidence refusal and not review-eligible.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-004.

### US-RETRIEVAL-QUALITY-METRICS-002: Inspect space-level retrieval reliability

As a product lead, I want safe aggregate retrieval quality statistics for a Knowledge Space, so that I can spot weak coverage and no-evidence patterns without reading confidential content.

Acceptance:
- Given multiple Ask runs in a space, when aggregate metrics are requested, then counts and ratios are returned by safe quality category.
- Given failed or no-evidence runs, when aggregate metrics are requested, then they are represented as refusal/failure statistics rather than healthy retrieval.
- Given raw questions or answers exist in storage, when aggregate metrics are returned, then they are not included in the response.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004.

### US-RETRIEVAL-QUALITY-METRICS-003: Display safe quality signals

As a reviewer, I want the Trusted Ask and knowledge surfaces to show safe quality signals, so that I can decide whether to inspect evidence or request more source material.

Acceptance:
- Given a successful Ask response, when the UI renders it, then quality chips distinguish evidence coverage, citation health, and review eligibility.
- Given quality metrics are unavailable, when the UI renders the state, then it shows a safe unavailable state and preserves the existing answer/citation behavior.
- Given diagnostics are displayed, when a user inspects them, then no raw prompt, raw provider payload, private path, stack trace, or raw source content is shown.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-006.

### US-RETRIEVAL-QUALITY-METRICS-004: Preserve safety and API contracts

As an API consumer, I want retrieval metrics APIs to use Atlas safe envelopes, so that callers get predictable responses and safe error handling.

Acceptance:
- Given a valid metrics request, when the backend responds, then it uses `ApiEnvelope`.
- Given an unknown run or space, when the backend responds, then it uses safe `NOT_FOUND` behavior.
- Given metrics are computed, when DTOs are serialized, then private diagnostics and raw content are absent.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-007.

### US-RETRIEVAL-QUALITY-METRICS-005: Close the slice with evidence

As a maintainer, I want SDD, tests, traceability, roadmap, and closeout evidence updated, so that future agents can resume from durable project state.

Acceptance:
- Given implementation completes, when closeout runs, then SDD, task completion, verification, and residual risks are recorded.
- Given changes are staged, when commit and push happen, then only retrieval-quality-metrics related changes are included.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-008.

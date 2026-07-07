# User Stories: secret-manager-integration

Status: Preauthorized SDD accepted for implementation
Last updated: 2026-07-07

## US-SECRET-MANAGER-INTEGRATION-001: View Secret Status Safely

As a platform administrator,
I want provider and adapter settings to show only configured/missing/masked states,
so that I can understand readiness without seeing raw credentials or private infrastructure.

### Acceptance Criteria

1. Given model, runtime, storage, or vector capability data exists, when the admin reads the API or UI, then only status and reference metadata is visible.
2. Given a secret is configured from runtime or environment state, when the API serializes the response, then the raw value, endpoint value, command path, and private path are absent.
3. Given an adapter is disabled or missing configuration, when the status is displayed, then the UI shows a safe missing/disabled state.

Dependencies: existing model/parser/converter/storage/vector capability endpoints.
Out of scope: real secret manager reads.
Open questions: none within this goal boundary.

## US-SECRET-MANAGER-INTEGRATION-002: Replace Or Clear Model Credential

As a platform administrator,
I want to replace or clear a model provider credential without the backend echoing it,
so that Atlas can support safer future secret manager integration.

### Acceptance Criteria

1. Given a request includes replacement secret material, when it is saved, then the response contains only `CONFIGURED` style status and a secret reference.
2. Given an update omits replacement secret material, when an existing runtime or environment credential exists, then Atlas retains the effective credential status.
3. Given clear is requested, when runtime configuration is removed, then the response falls back to environment configured or missing status without exposing values.

Dependencies: existing `/api/model-configurations/deepseek` endpoints.
Out of scope: persistent encrypted storage or rotation automation.
Open questions: none within this goal boundary.

## US-SECRET-MANAGER-INTEGRATION-003: Preserve Adapter Boundaries

As an implementation maintainer,
I want secret metadata to be represented through product DTOs and adapter capability summaries,
so that parser, converter, model, storage, and vector engines remain replaceable.

### Acceptance Criteria

1. Given any capability endpoint, when configuration status is returned, then it uses the shared secret status DTO shape.
2. Given product services call adapters, when execution occurs, then raw secret resolution remains inside adapter/provider boundaries.
3. Given tests inspect responses, when raw-looking values are present in inputs or environment, then they are not present in serialized read responses.

Dependencies: adapter registries and capability mappers.
Out of scope: changing engine execution semantics.
Open questions: none within this goal boundary.

## US-SECRET-MANAGER-INTEGRATION-004: Verify No Leak

As a security reviewer,
I want tests and scans covering secret-bearing configuration behavior,
so that the slice can close without relying on visual inspection only.

### Acceptance Criteria

1. Backend unit/API tests verify save/read/clear redaction and capability status DTOs.
2. Frontend tests verify the settings surface renders masked/status-only values.
3. Secret/private-path scans and `git diff --check` pass before closeout.

Dependencies: existing backend and frontend test suites.
Out of scope: provider-backed live connection verification.
Open questions: none within this goal boundary.

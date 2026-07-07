# connector-sync-v0 API Implementation Guide

Status: Accepted by autonomous single-slice preauthorization
Last updated: 2026-07-07
Base path: `/api`

## Endpoint Summary

| Operation | Method | Path | Response |
|---|---|---|---|
| List connector definitions | GET | `/api/connector-definitions` | `ApiEnvelope<List<ConnectorDefinitionResponse>>` |
| Create sync job/run | POST | `/api/spaces/{spaceId}/connector-sync-jobs` | `ApiEnvelope<ConnectorSyncRunResponse>` |
| Get sync run | GET | `/api/connector-sync-runs/{runId}` | `ApiEnvelope<ConnectorSyncRunResponse>` |
| List sync items | GET | `/api/connector-sync-runs/{runId}/items` | `ApiEnvelope<List<ConnectorSyncItemResponse>>` |

## Create Sync Request

```json
{
  "connectorKey": "mock-local-fixture",
  "requestedBy": "p0-browser",
  "sourceScope": "sample-fixture"
}
```

Validation:
- `connectorKey` is required and must resolve to a registered connector definition.
- `requestedBy` is optional but must be safe text when present.
- `sourceScope` is optional and must remain fixture/local scope in v0.

## Connector Definition Response

```json
{
  "id": "connector-definition-mock-local",
  "connectorKey": "mock-local-fixture",
  "name": "Mock Local Fixture",
  "connectorType": "LOCAL_FIXTURE",
  "status": "AVAILABLE",
  "version": "v0",
  "capabilitySummary": "Sample-safe connector fixture with source trace and review-required handoff.",
  "configurationState": "MOCK_CONFIGURED",
  "reviewPolicy": "REVIEW_REQUIRED"
}
```

Traceability: REQ-CONNECTOR-SYNC-V0-002, AC-CONNECTOR-SYNC-V0-001.

## Sync Run Response

```json
{
  "runId": "connector-run-20260707-0001",
  "jobId": "connector-job-20260707-0001",
  "spaceId": "ibm-i-modernization",
  "connectorKey": "mock-local-fixture",
  "status": "REVIEW_REQUIRED",
  "itemCount": 3,
  "reviewRequiredCount": 2,
  "failedCount": 0,
  "safeMessage": "Connector sync completed with review-required output.",
  "startedAt": "2026-07-07T00:00:00Z",
  "completedAt": "2026-07-07T00:00:01Z"
}
```

Traceability: REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004.

## Sync Item Response

```json
{
  "id": "connector-item-modernization-overview",
  "runId": "connector-run-20260707-0001",
  "externalId": "fixture-modernization-overview",
  "title": "Modernization Overview",
  "itemStatus": "REVIEW_REQUIRED",
  "sourceReference": "fixture://modernization/overview",
  "sourceTrace": {
    "sourceName": "Mock Local Fixture",
    "sourceLocator": "fixture-modernization-overview",
    "section": "overview",
    "page": 1,
    "itemExternalId": "fixture-modernization-overview",
    "connectorKey": "mock-local-fixture"
  },
  "provenance": {
    "connectorKey": "mock-local-fixture",
    "adapterVersion": "v0",
    "fixtureId": "connector-fixture-v0",
    "contentHash": "sample-hash-modernization-overview",
    "syncRunId": "connector-run-20260707-0001"
  },
  "confidence": 0.91,
  "reviewEligible": false,
  "safeErrorCategory": "NONE",
  "safeErrorMessage": null,
  "outputArtifacts": [
    {
      "id": "connector-artifact-modernization-overview",
      "artifactType": "MARKDOWN_CANDIDATE",
      "reviewStatus": "REVIEW_REQUIRED",
      "title": "Modernization Overview",
      "targetPath": "generated/connectors/mock-local-fixture/modernization-overview.md"
    }
  ]
}
```

Traceability: REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006, AC-CONNECTOR-SYNC-V0-004, AC-CONNECTOR-SYNC-V0-005.

## Error Rules

- Errors use the existing safe `ApiEnvelope` error body.
- Unknown runs return `NOT_FOUND`.
- Invalid requests return `VALIDATION_FAILED`.
- Adapter failures become safe run/item status and may also return `SAFE_SYSTEM_ERROR` if no response can be produced.
- Responses must not contain secrets, tokens, private paths, raw provider payloads, stack traces, internal endpoints, or real URLs.

Traceability: REQ-CONNECTOR-SYNC-V0-007, AC-CONNECTOR-SYNC-V0-006.

## Verification

- T-CONNECTOR-SYNC-V0-006: backend integration/API contract tests.
- T-CONNECTOR-SYNC-V0-008: frontend typecheck/test/build.
- T-CONNECTOR-SYNC-V0-009: closeout, scans, and roadmap/traceability update.

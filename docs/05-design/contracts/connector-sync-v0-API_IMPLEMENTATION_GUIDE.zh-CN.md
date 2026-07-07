# connector-sync-v0 API 实现指南

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07
Base path：`/api`

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

校验：
- `connectorKey` 必填，且必须解析到已注册 connector definition。
- `requestedBy` 可选，但存在时必须是安全文本。
- `sourceScope` 可选，v0 中必须保持 fixture/local scope。

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

追溯：REQ-CONNECTOR-SYNC-V0-002、AC-CONNECTOR-SYNC-V0-001。

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

追溯：REQ-CONNECTOR-SYNC-V0-003、REQ-CONNECTOR-SYNC-V0-004。

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

追溯：REQ-CONNECTOR-SYNC-V0-005、REQ-CONNECTOR-SYNC-V0-006、AC-CONNECTOR-SYNC-V0-004、AC-CONNECTOR-SYNC-V0-005。

## Error Rules

- 错误使用现有 safe `ApiEnvelope` error body。
- 未知 runs 返回 `NOT_FOUND`。
- 无效请求返回 `VALIDATION_FAILED`。
- Adapter failures 变成 safe run/item status；如果无法生成响应，也可返回 `SAFE_SYSTEM_ERROR`。
- 响应不得包含 secrets、tokens、private paths、raw provider payloads、stack traces、internal endpoints 或真实 URLs。

追溯：REQ-CONNECTOR-SYNC-V0-007、AC-CONNECTOR-SYNC-V0-006。

## 验证

- T-CONNECTOR-SYNC-V0-006：backend integration/API contract tests。
- T-CONNECTOR-SYNC-V0-008：frontend typecheck/test/build。
- T-CONNECTOR-SYNC-V0-009：closeout、scans 和 roadmap/traceability update。

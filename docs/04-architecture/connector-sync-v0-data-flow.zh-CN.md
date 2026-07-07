# 数据流：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07

## Flow 1：Connector Discovery

```text
Frontend Connector panel
  -> GET /api/connector-definitions
  -> ConnectorSyncController
  -> ConnectorAdapterRegistry.capabilities()
  -> ApiEnvelope<List<ConnectorDefinitionResponse>>
  -> UI connector list
```

追溯：REQ-CONNECTOR-SYNC-V0-001、REQ-CONNECTOR-SYNC-V0-002、AC-CONNECTOR-SYNC-V0-001。

## Flow 2：Start Sync Run

```text
User action
  -> POST /api/spaces/{spaceId}/connector-sync-jobs
  -> validate space and connector definition
  -> persist ConnectorSyncJob
  -> persist ConnectorSyncRun as QUEUED
  -> mark run RUNNING
  -> execute MockLocalFixtureConnectorAdapter
  -> persist ConnectorSyncItem rows
  -> persist ConnectorOutputArtifact rows as REVIEW_REQUIRED
  -> mark run REVIEW_REQUIRED or FAILED
  -> return ConnectorSyncRunResponse
```

追溯：REQ-CONNECTOR-SYNC-V0-003、REQ-CONNECTOR-SYNC-V0-004、REQ-CONNECTOR-SYNC-V0-006、AC-CONNECTOR-SYNC-V0-002、AC-CONNECTOR-SYNC-V0-003。

## Flow 3：Inspect Run And Items

```text
Frontend
  -> GET /api/connector-sync-runs/{runId}
  -> GET /api/connector-sync-runs/{runId}/items
  -> render run summary
  -> render item source reference, trace, provenance, review eligibility, output artifact
```

追溯：REQ-CONNECTOR-SYNC-V0-005、REQ-CONNECTOR-SYNC-V0-008、AC-CONNECTOR-SYNC-V0-004、AC-CONNECTOR-SYNC-V0-005。

## 状态转换

Run：
- QUEUED -> RUNNING：service 开始本地执行。
- RUNNING -> REVIEW_REQUIRED：创建至少一个 review-required artifact，且没有 blocking run failure。
- RUNNING -> COMPLETED：所有 items 完成且没有 review-required artifact。
- RUNNING -> FAILED：adapter unavailable、validation fault 或所有 items fail。

Item：
- DISCOVERED -> FETCHED -> OUTPUT_CREATED -> REVIEW_REQUIRED。
- 当 fixture source unsupported 或 unreadable 时 DISCOVERED -> FAILED。

## 错误流

```text
Adapter exception or unsafe adapter message
  -> ConnectorSyncService sanitizes with safe-error sanitizer
  -> item.safe_error_category + item.safe_error_message
  -> run.safe_message
  -> ApiEnvelope safe response
```

追溯：REQ-CONNECTOR-SYNC-V0-007、AC-CONNECTOR-SYNC-V0-006。

## 数据安全说明

- Source references 是 sample-safe fixture locators，不是真实 URL 或 private paths。
- Provenance 记录 connector key、fixture id、source locator、content hash 和 adapter version，不记录 raw payload。
- v0 output artifacts 只是 metadata-only review handoffs。

## 相关任务

T-CONNECTOR-SYNC-V0-002、T-CONNECTOR-SYNC-V0-003、T-CONNECTOR-SYNC-V0-004、T-CONNECTOR-SYNC-V0-005。

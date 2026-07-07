# 数据模型：connector-sync-v0

状态：已由 autonomous single-slice 预授权接受
最后更新：2026-07-07

## 概览

connector-sync-v0 存储 connector metadata、用户可见 sync jobs、确定性 sync runs、每个 source 的 sync items，以及 review-required output artifacts。该模型是增量的，并将 connector-derived content 与 approved Wiki、Ask、Graph knowledge 分离。

## 实体关系

```text
space 1:N connector_sync_job
connector_definition 1:N connector_sync_job
connector_sync_job 1:N connector_sync_run
connector_sync_run 1:N connector_sync_item
connector_sync_item 1:N connector_output_artifact
```

## 实体

### ConnectorDefinition

追溯：REQ-CONNECTOR-SYNC-V0-002。

字段：id、connector_key、name、connector_type、status、version、capability_summary、configuration_state、review_policy、created_at、updated_at。

规则：
- `connector_key` 稳定且唯一。
- v0 seed row 是 `mock-local-fixture`。
- Configuration state 仅为状态化字段。

### ConnectorSyncJob

追溯：REQ-CONNECTOR-SYNC-V0-003。

字段：id、space_id、connector_definition_id、requested_by、requested_at、source_scope、status、safe_message。

规则：
- Space 必须存在。
- v0 中 job status 镜像 latest run terminal status。

### ConnectorSyncRun

追溯：REQ-CONNECTOR-SYNC-V0-004。

字段：id、job_id、connector_definition_id、status、started_at、completed_at、item_count、review_required_count、failed_count、safe_message。

Run statuses：QUEUED、RUNNING、COMPLETED、FAILED、REVIEW_REQUIRED。

### ConnectorSyncItem

追溯：REQ-CONNECTOR-SYNC-V0-005。

字段：id、run_id、external_id、title、item_status、source_reference、source_trace、provenance、confidence、review_eligible、safe_error_category、safe_error_message、discovered_at。

Item statuses：DISCOVERED、FETCHED、OUTPUT_CREATED、REVIEW_REQUIRED、FAILED。

Safe error categories：NONE、VALIDATION、CONNECTOR_UNAVAILABLE、UNSUPPORTED_SOURCE、SOURCE_UNREADABLE、SAFE_SYSTEM。

### ConnectorOutputArtifact

追溯：REQ-CONNECTOR-SYNC-V0-006。

字段：id、item_id、artifact_type、review_status、title、target_path、source_trace、provenance、created_at。

规则：
- v0 artifacts 使用 review_status REVIEW_REQUIRED。
- v0 artifacts 不是 published Wiki pages，也不是 Ask/Graph trust inputs。

## JSON 字段

- `source_trace`：包含 sourceName、sourceLocator、section、page、itemExternalId、connectorKey 的对象。
- `provenance`：包含 connectorKey、adapterVersion、fixtureId、contentHash、syncRunId 的对象。

## 相关任务

T-CONNECTOR-SYNC-V0-002、T-CONNECTOR-SYNC-V0-003、T-CONNECTOR-SYNC-V0-004、T-CONNECTOR-SYNC-V0-005。

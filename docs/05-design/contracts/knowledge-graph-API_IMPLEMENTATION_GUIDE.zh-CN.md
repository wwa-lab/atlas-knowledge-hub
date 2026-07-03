# 知识图谱 API 实现指南

日期：2026-07-03
版本：草稿
Base path：`/api`
后端栈：Spring Boot metadata API
Auth model：Phase 4 后端强制 authentication 与 authorization

## Envelope

所有端点返回：

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {}
}
```

错误必须用户安全，不得包含 stack trace、SQL、secret、私有绝对路径、原始 prompt、原始向量或引擎诊断。

## 端点

### GET `/api/spaces/{spaceId}/graph`

返回有界 graph view。

查询参数：

- `q` 可选搜索文本。
- `nodeType` 可选枚举。
- `edgeType` 可选枚举。
- `reviewStatus` 可选枚举。
- `evidenceOnly` 可选 boolean，默认 `true`。
- `limit` 可选 integer，默认 `200`，最大 `500`。

响应 data：

```json
{
  "spaceId": "ibm-i-modernization",
  "nodes": [
    {
      "id": "node-concept-rpgle",
      "label": "RPGLE",
      "type": "CONCEPT",
      "reviewStatus": "APPROVED",
      "confidence": 0.91,
      "evidenceCount": 4
    }
  ],
  "edges": [
    {
      "id": "edge-page-rpgle-defines",
      "sourceNodeId": "node-page-modernization-overview",
      "targetNodeId": "node-concept-rpgle",
      "type": "DEFINES",
      "reviewStatus": "APPROVED",
      "confidence": 0.88,
      "evidenceCount": 2
    }
  ],
  "counts": {
    "nodes": 172,
    "edges": 248,
    "excluded": 94
  }
}
```

### GET `/api/spaces/{spaceId}/graph/nodes/{nodeId}`

返回选中 node detail 与相邻 evidence。

响应 data 包含 node fields、adjacent nodes、adjacent edges，以及带 `wikiPageId`、`sourceChunkId`、`sourceFile`、`page`、`section`、`confidence`、`reviewStatus` 的 `evidenceReferences`。

### POST `/api/spaces/{spaceId}/graph/projection-runs`

为 approved/published evidence 启动 deterministic 或 configured projection run。

请求：

```json
{
  "scope": "APPROVED_ONLY",
  "adapterId": "deterministic",
  "dryRun": false
}
```

响应 data 包含 `runId`、`status`、`summary` 和 safe message。

### GET `/api/graph/projection-runs/{runId}`

返回 run summary、item outcomes、安全 exclusion reason codes 和 timestamps。

### POST `/api/spaces/{spaceId}/graph/edges/{edgeId}/review-actions`

记录 edge 的 SME review action。

请求：

```json
{
  "action": "APPROVE",
  "comment": "Relationship is supported by cited modernization overview."
}
```

允许 action：`APPROVE`、`NEED_FIX`、`OCR_REQUIRED`。

## 校验

- ID 必须是非空稳定标识符。
- `limit` 必须在 `1..500`。
- 枚举值必须匹配数据模型。
- Review comments 必须有长度边界并被清理。
- Projection 拒绝 unapproved、missing-source-trace、unsafe-path 和 no-evidence candidates。

## Contract Tests

运行：

```bash
cd backend && mvn verify
```

测试必须覆盖 success envelopes、validation errors、`401`/`403`、`404`、projection exclusions、evidence preservation、audit append-only behavior 和 safe error bodies。

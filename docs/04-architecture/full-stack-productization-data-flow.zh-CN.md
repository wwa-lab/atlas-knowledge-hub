# 数据流：全栈产品化

## 主浏览器流程

```text
1. App loads spaces
   Vue -> GET /api/spaces -> Space list

2. User opens space
   Vue -> GET /api/spaces/{spaceId} -> selected context

3. User creates sample batch
   Vue -> POST /api/spaces/{spaceId}/batches -> Batch
   Vue -> GET /api/spaces/{spaceId}/batches -> Batches
   Vue -> GET /api/batches/{batchId}/files -> Files
   Vue -> GET /api/files/{fileId}/chunks -> Source chunks

4. User approves file
   Vue -> GET /api/spaces/{spaceId}/review-queues -> Queues
   Vue -> POST /api/files/{fileId}/reviews -> ReviewRecord
   Vue refreshes files/chunks/queues

5. User publishes Wiki
   Vue -> POST /api/files/{fileId}/publish -> WikiPage PUBLISHED
   Vue -> GET /api/spaces/{spaceId}/wiki-pages -> Wiki list

6. App refreshes downstream evidence
   Vue -> POST /api/spaces/{spaceId}/graph/projection-runs -> GraphProjectionRun
   Vue -> POST /api/spaces/{spaceId}/vector-runs -> VectorRun
   Vue -> GET /api/spaces/{spaceId}/graph -> GraphView

7. User asks
   Vue -> POST /api/spaces/{spaceId}/ask -> AskRun
   Vue -> GET /api/ask-runs/{runId} -> Answer + evidence
```

## 数据安全流

- Sample batch request 只包含安全相对路径和 mock/sample labels。
- Review request 包含 reviewer/comment 与 affected chunk ids；不包含 raw source text。
- Publish request 包含 title/owner；后端推导 source document ids 并保留 metadata。
- Ask request 包含有界 question text 和安全 filters；后端存储 evidence references，不存 raw provider payloads。

## 失败流

| Failure | UI Behavior |
|---|---|
| Spaces fail | 展示 API error 和 retry。 |
| Batch create fails | 保留当前数据，展示安全错误，不推进 workflow。 |
| Review fails | 保持 file pending 并展示安全错误。 |
| Publish blocked | 展示后端 conflict message，直到前置条件满足前保持 publish disabled。 |
| Graph refresh fails | 展示 graph error；不声称 connected。 |
| Ask no evidence | 展示 no-evidence answer state 且无 citations。 |

# 规格：全栈产品化

## 状态

实现草案。切片 `full-stack-productization` 的行为真相源。

## 概述

本切片将 prototype-first Vue shell 替换为 API 驱动的 P0 工作流，同时保持 mock/sample-only 安全边界。后端仍为 Spring Boot metadata/control plane；parser、converter、model、vector、storage、graph projection 行为继续位于后端 service/adapter 边界之后。

## 功能需求

### Space Navigation

- FR-FSP-001：前端启动时加载 `GET /api/spaces` 并渲染 API spaces。
- FR-FSP-002：选择 space 后加载 `GET /api/spaces/{spaceId}`，并将其作为 Documents、Review、Wiki、Graph、Ask 的上下文。
- FR-FSP-003：Space list 和 detail 失败时展示用户安全错误与可行的 retry。

### Metadata-Only Upload And Batch

- FR-FSP-004：Documents tab 提供 sample upload action，将安全 metadata POST 到 `POST /api/spaces/{spaceId}/batches`。
- FR-FSP-005：创建的 batch 使用 `sourceKind=folder`、review-required 文件元数据、安全相对路径、markdown path、confidence、source chunks。
- FR-FSP-006：UI 刷新 `GET /api/spaces/{spaceId}/batches`、`GET /api/batches/{batchId}/files`、`GET /api/files/{fileId}/chunks`。

### Review And Publish

- FR-FSP-007：Review tab 加载 `GET /api/spaces/{spaceId}/review-queues`。
- FR-FSP-008：Approve action 通过 `POST /api/files/{fileId}/reviews` 提交 action `APPROVE`、reviewer、comment、affected chunks。
- FR-FSP-009：Publish action 仅在所选文件已 approved 后调用 `POST /api/files/{fileId}/publish`。
- FR-FSP-010：Wiki tab 刷新 `GET /api/spaces/{spaceId}/wiki-pages` 并展示已发布 metadata。

### Downstream Evidence Refresh

- FR-FSP-011：发布后，浏览器流程可调用现有 adapter-backed 后端 endpoints，为已发布文件 chunks 刷新 graph projection 与 vector index。
- FR-FSP-012：Graph projection 使用 `POST /api/spaces/{spaceId}/graph/projection-runs`、后端 graph adapter policy 与必要 graph headers。
- FR-FSP-013：Vector indexing 使用 `POST /api/spaces/{spaceId}/vector-runs`、`mock-vector`、`APPROVED_ONLY` 与已审核文件的 source chunk ids。

### Graph

- FR-FSP-014：Graph tab 调用 `GET /api/spaces/{spaceId}/graph` 和 `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}`。
- FR-FSP-015：Graph evidence detail 展示 source chunk id、source file、section/page、confidence、review status。
- FR-FSP-016：Graph unauthorized 或失败状态必须可见，不能静默声称 graph 已连接。

### Ask

- FR-FSP-017：Ask tab 接受问题并调用 `POST /api/spaces/{spaceId}/ask`。
- FR-FSP-018：Ask 创建后，UI 在渲染最终答案前读取 `GET /api/ask-runs/{runId}`。
- FR-FSP-019：Ask answer 展示 status、answer text 或 safe message、confidence、`REVIEW_REQUIRED` answer review status、evidence rows。
- FR-FSP-020：Ask no-evidence 或 failure 状态必须可见且安全。

### Disabled / Coming Soon

- FR-FSP-021：任何可见但未接通真实 P0 API loop 的功能必须 disabled、标注 coming soon，或移出主要可用流程。
- FR-FSP-022：已接受静态原型可保留为 reference artifact，但不能作为 P0 的主要操作 UI。

## 状态规则

```text
No space -> loading spaces -> space selected
space selected -> sample batch created -> file/chunks loaded
file REVIEW_REQUIRED -> APPROVE -> file APPROVED -> PUBLISH -> wiki PUBLISHED
wiki PUBLISHED -> graph/vector refresh -> graph evidence ready -> ask SUCCEEDED/NO_EVIDENCE/FAILED
```

## 错误与空状态

- API 请求失败时显示 endpoint-specific 安全文案，并在可行处提供 retry。
- Spaces、batches、files、chunks、queues、wiki pages、graph、ask evidence 的空状态应彼此区分。
- 不安全请求文本、无效发布顺序或缺少前置条件时，相应 action 保持 disabled。

## 验收矩阵

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FSP-001 | Space Navigation | 浏览器显示 API space list。 |
| REQ-FSP-002 | Space Navigation | Space detail 被加载且 tab context 更新。 |
| REQ-FSP-003 | Metadata-Only Upload And Batch | 浏览器创建 sample batch 并加载 files/chunks。 |
| REQ-FSP-004 | Metadata-Only Upload And Batch | 文件/chunk source trace、confidence、review status 可见。 |
| REQ-FSP-005 | Review And Publish | Review queues 加载且 approve action 通过 API 提交。 |
| REQ-FSP-006 | Review And Publish | Publish 创建/刷新 `PUBLISHED` Wiki metadata。 |
| REQ-FSP-007 | Downstream Evidence Refresh | 发布后 graph/vector refresh 通过后端 endpoint 成功。 |
| REQ-FSP-008 | Graph | Graph 展示已发布 chunk 的 evidence。 |
| REQ-FSP-009 | Ask | Ask 展示 answer、citations 和 `REVIEW_REQUIRED`。 |
| REQ-FSP-010 | Disabled / Coming Soon | 未接通 mock 控件 disabled/coming soon。 |
| REQ-FSP-011 | Verification | 现有 E2E commands 保持。 |
| REQ-FSP-012 | Verification | 必需 commands/scans 被运行或报告 skipped。 |

## 范围外

- 生产文件字节上传和存储。
- 生产认证/RBAC。
- 前端直接调用 provider/vector/parser/storage/converter。
- 真实外部云调用。
- 真实公司数据。

## SDD 质量说明

本文档使用 Atlas SDD 链模型生成：`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`，并以 `review-doc-quality` checklist 做一致性审查。

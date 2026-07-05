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

## Product Goal Batch 5 补充：Phase I1-I3 API-backed 切换

状态：2026-07-05 已完成 L3 API-backed 验收准备证据；不代表最终产品验收通过。

| Phase | API-backed 产品界面 | 既有 API 契约 |
|---|---|---|
| I1 Knowledge Space metadata | 真实 Vue 首页卡片与空间页头消费 Knowledge Space 元数据；仅在 API 数据不可用时保留安全 sample fallback。 | `GET /api/spaces`；`GET /api/spaces/{spaceId}` |
| I2 Batch/file/chunk metadata | 真实 Vue Documents tab 展示 API batch、file 与 source chunk metadata，并通过 Atlas API 创建安全 sample batch。 | `GET /api/spaces/{spaceId}/batches`；`POST /api/spaces/{spaceId}/batches`；`GET /api/batches/{batchId}/files`；`GET /api/files/{fileId}/chunks` |
| I3 Review queues | 真实 Vue Processing Center 展示 API review queues，并与既有 Wiki/Graph/Ask eligibility 处理门禁并列。 | `GET /api/spaces/{spaceId}/review-queues` |

本补充不新增后端 endpoint、生产上传、生产认证/RBAC、真实公司数据、外部 provider，或前端直连 parser/converter/storage/vector/model engine。剩余 Phase I 工作覆盖 Wiki pages、Graph evidence、Ask runs/citations 与 model configuration metadata。

## Product Goal Batch 6 补充：Phase I4-I7 API-backed 切换

状态：2026-07-05 已完成 L3 API-backed 验收准备证据；不代表最终产品验收通过。

| Phase | API-backed 产品界面 | 既有 API 契约 |
|---|---|---|
| I4 Wiki pages | 真实 Vue Wiki tab 在 API review/publish action 后，从 Atlas API 读取已发布 Wiki metadata。 | `GET /api/spaces/{spaceId}/wiki-pages`；`POST /api/files/{fileId}/publish` |
| I5 Graph evidence | 真实 Vue Graph tab 将 Atlas graph nodes、edges 与 source-trace evidence 映射到产品图谱界面。 | `GET /api/spaces/{spaceId}/graph`；`GET /api/spaces/{spaceId}/graph/nodes/{nodeId}` |
| I6 Ask runs and citations | 真实 Vue 全局 Ask 界面可通过 Atlas Ask API 提交问题，并展示 answer status、model run id 与 citations。 | `POST /api/spaces/{spaceId}/ask`；`GET /api/ask-runs/{runId}` |
| I7 Model configuration metadata | 真实 Vue model settings 界面读取 masked model adapter capability metadata。 | `GET /api/model-adapters` |

本补充不新增生产 provider 调用、明文 secret 展示、生产 RBAC、真实公司数据、streaming Ask 或新的模型管理写契约。Phase J hardening 仍待执行。

## Core Knowledge Loop v1 补充：真实上传与运行时模型配置

状态：计划在 2026-07-05 后续实现切片中落地。本切片目标是用户可跑通的闭环，不是完整 L5 生产加固。

### 范围

- FR-FSP-023：普通用户可以通过 Atlas UI/API 配置 DeepSeek chat model key，无需编辑 shell script 或进程环境变量。
- FR-FSP-024：Atlas API 只返回 masked model configuration state；raw secret 不返回前端、不进入日志、不写入文档。
- FR-FSP-025：普通用户可以通过主产品 UI 上传一个或多个 PDF，或包含 PDF 的 ZIP。
- FR-FSP-026：上传文件存储在 Atlas backend 配置控制的本地 artifact 区域，metadata records 保留相对 source trace path。
- FR-FSP-027：Atlas 通过 adapter boundary 将上传 PDF 解析为 `REVIEW_REQUIRED` source chunks，并保留 page、section、source trace metadata。
- FR-FSP-028：File review 更新会传播到所选 source chunks，确保 review queue、publish gate、graph、vector、Ask 使用一致 review state。
- FR-FSP-029：Atlas 提供 backend downstream refresh action，从 approved 或 published chunks 重建 graph/vector evidence，前端不直连 engine。
- FR-FSP-030：Ask 在可用时使用已配置 model capability，同时保留安全的 no-key 与 no-evidence 状态。
- FR-FSP-031：v1 闭环范围外的可见能力必须 disabled 或标注 coming soon，不能表现为已可生产使用。

### 验收矩阵补充

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FSP-013 | Runtime Model Configuration | 用户可通过 Atlas API/UI 保存、读取 masked、清除 model key。 |
| REQ-FSP-014 | Real Upload | 用户通过 UI 上传 PDF 或 ZIP-of-PDF，并获得真实 batch id。 |
| REQ-FSP-015 | Parser Adapter | 后端 parser run 从上传 PDF 内容创建 review-required chunks。 |
| REQ-FSP-016 | Review Consistency | Approve file 会同步更新 selected chunks 与 review queues。 |
| REQ-FSP-017 | Downstream Refresh | Backend refresh 创建 graph/vector evidence，前端不直连 engine。 |
| REQ-FSP-018 | Ask | Publish/refresh 后 Ask 可运行，并显示 citations 或安全可操作状态。 |
| REQ-FSP-019 | Disabled / Coming Soon | Office/OCR/RBAC/vector-store/admin surfaces 在实现前 disabled。 |
| REQ-FSP-020 | Verification | 记录 backend tests、frontend checks、safety scans 与 manual closed-loop evidence。 |

### v1 明确禁用

- Office 文档转换，除非配置了内部 converter runtime。
- Image OCR、tables-as-structured-data extraction、incremental re-indexing、streaming Ask。
- 生产认证、RBAC 管理、审计留存、限流、多租户隔离、secret-manager integration。
- pgvector、Milvus、Qdrant 等生产向量库。
- 前端直接调用 parser、converter、model、vector 或 storage engines。

## SDD 质量说明

本文档使用 Atlas SDD 链模型生成：`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`，并以 `review-doc-quality` checklist 做一致性审查。

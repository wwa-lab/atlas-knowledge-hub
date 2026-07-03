# 架构：知识图谱

## 状态

草稿。Phase 4 hardening。来源：`docs/03-spec/knowledge-graph-spec.md`。

## 概述

知识图谱架构将现有 graph metadata placeholder 提升为可信图谱表面。后端负责授权、投影编排、API 契约、审计和持久化；前端负责渲染与交互；图谱抽取保持在可替换投影边界之后。

## 架构驱动

| 驱动 | 影响 |
|---|---|
| 证据优先图谱 | 节点和边只从已批准/已发布 Wiki 与 source trace 派生。 |
| Phase 4 加固 | 必须包含后端 auth/RBAC、audit、用户安全错误和完整层级测试。 |
| 适配器中立 | 投影逻辑是产品面的、可替换的；不在产品工作流中硬编码 graph/model/vector engine。 |
| FE baseline 一致 | Graph tab 保留已接受原型的 canvas、legend、search、hover、click detail 和 evidence path 行为。 |
| 数据安全 | API 响应展示证据元数据，而非原始机密文档正文或 secret。 |

## 系统上下文

| 边界 | 职责 |
|---|---|
| Frontend Graph tab | 渲染有界 graph data、过滤/搜索和 evidence detail。 |
| Backend Graph API | 校验请求、强制 auth、返回 Atlas envelope、隐藏敏感内部信息。 |
| Graph projection service | 从符合条件的 Wiki/source-trace 元数据构建/刷新 graph records。 |
| Projection adapter boundary | 未来 graph/model extraction engine 只存在于此。 |
| PostgreSQL metadata | 存储 graph nodes、edges、projection runs、evidence references 和 audit/review records。 |

## 高层架构

```text
+-------------------------------------------------------------+
| Users                                                        |
| Knowledge user · SME reviewer · Delivery lead · Admin        |
+------------------------------+------------------------------+
                               |
                               | HTTPS / JSON
                               v
+-------------------------------------------------------------+
| Vue Frontend                                                  |
| Graph tab · filters/search · canvas · evidence detail         |
+------------------------------+------------------------------+
                               |
                               | REST / Atlas envelope
                               v
+-------------------------------------------------------------+
| Spring Boot Metadata API                                      |
| GraphController · auth/RBAC · validation · safe errors        |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Graph Domain Services                                         |
| Query service · projection service · review/audit service     |
+------------------------------+------------------------------+
                               |
             product-facing projection/adapter seam
                               v
+------------------------------+       +-----------------------+
| Metadata Persistence         |       | Future worker/engine   |
| wiki/source chunks/graph/audit|       | Optional, replaceable  |
+------------------------------+       +-----------------------+
```

## 组件拆分

### 前端组件

- **GraphViewer：** 渲染 graph canvas、selected state、legend 和 detail panel。
- **GraphFilters：** 搜索、类型过滤、review-status 过滤、confidence threshold 和 evidence-only toggle。
- **GraphEvidencePanel：** 展示选中 node/edge 的 evidence references、confidence、review status 和安全来源摘要。
- **GraphApiClient/Store：** 获取 graph envelope，并暴露 loading/error/unauthorized/empty 状态。

### 后端服务

- **GraphController：** 拥有 HTTP endpoints 与 envelope responses。
- **GraphQueryService：** 返回有界 graph views、node detail、counts 和 filters。
- **GraphProjectionService：** 从 eligible approved/published metadata 创建或刷新 graph records。
- **GraphReviewService：** 对 graph edges 应用 review actions，并写 append-only review/audit records。
- **GraphAuditService：** 用脱敏 summary 记录 projection 与 governance events。

### 投影适配器边界

- **GraphProjectionAdapter：** 未来 extraction engine 的产品面 contract。接收 Atlas source descriptors，返回 candidate graph descriptors，绝不返回供应商特定 payload。
- **DeterministicGraphProjectionAdapter：** 初始 mock/deterministic adapter，用于 CI 与本地验证。
- 未来 graph/model engine 仅允许在此边界之后。

## 安全与数据安全

- 所有 graph endpoints 必须强制后端授权。
- `401` 与 `403` 响应使用 Atlas envelope，且不暴露内部策略细节。
- API 响应不得包含明文 secret、私有绝对路径、原始向量、原始 prompt、stack trace、SQL 或机密文档正文。
- Source references 使用相对路径与 chunk/page identifiers。

## 状态策略

Projection run 状态：

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Graph trust 状态复用 `ReviewStatus`：`REVIEW_REQUIRED`、`APPROVED`、`NEED_FIX`、`OCR_REQUIRED`、`PUBLISHED`。可信图谱结果默认只包含 approved/published。

## 架构风险

| ID | 风险 | 缓解 |
|---|---|---|
| R-KG-001 | `review-publish` 可能在本切片前尚未存在。 | 使用 approved fixture 数据并记录依赖；绝不绕过 review status。 |
| R-KG-002 | 图谱可视化可能过密。 | API 返回有界 views 和 counts；UI 支持 filters 与 select detail。 |
| R-KG-003 | 未来 extraction engine 可能泄露到产品代码。 | 添加 seam guard 测试，并保持 adapter package 为唯一 engine boundary。 |

# 详细设计：知识图谱

## 概述

本文定义 Phase 4 `knowledge-graph` 切片的 UI、后端服务、投影、API、校验和测试契约。

## 设计范围

- 全栈 graph query 与 projection contract。
- Graph tab 由 API 数据驱动，同时保留原型交互模式。
- 后端 authorization、audit、安全错误和 adapter boundary 纪律。
- 不包含生产图数据库、真实外部引擎、Ask 答案生成。

## 模块设计

### 前端

| 模块 | 设计 |
|---|---|
| `GraphViewer` | 使用 API 返回的有界 `nodes` 与 `edges`；canvas/legend/detail 交互对齐 `frontend/public/atlas-prototype.html`。 |
| `GraphFilters` | 搜索文本、node type filter、edge type filter、review status filter 和 evidence-only toggle。 |
| `GraphEvidencePanel` | 展示 evidence summaries：Wiki page id/title、source chunk id、source file display name、page/section、confidence、review status。 |
| `graphApi` | graph view、node detail、projection run、projection run read、edge review action 的 typed client。 |
| Graph store | 拥有 loading、error、unauthorized、selected node/edge、filters 和 refresh state。 |

### 后端

| 模块 | 设计 |
|---|---|
| Graph controller | 薄 REST 层、请求校验、envelope responses、auth annotations/policy hooks。 |
| Graph query service | 应用 filters/limits，并把 records 映射为安全 graph DTO。 |
| Graph projection service | 校验 eligible evidence，调用 projection adapter，持久化 graph records 与 item outcomes。 |
| Graph review service | 校验 review action，并写 append-only review/audit records。 |
| Graph projection adapter | 产品面 seam；先做 deterministic adapter，真实引擎延后。 |

## API / Interface Design

本切片引入 graph endpoints，因此包含 API guide。

主要端点：

- `GET /api/spaces/{spaceId}/graph`
- `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}`
- `POST /api/spaces/{spaceId}/graph/projection-runs`
- `GET /api/graph/projection-runs/{runId}`
- `POST /api/spaces/{spaceId}/graph/edges/{edgeId}/review-actions`

所有响应使用 Atlas `ApiEnvelope`。列表有界并包含 `meta`。

## 校验与错误处理

- `spaceId`、`nodeId`、`edgeId`、`runId` 必须是稳定标识符。
- `limit` 有上限；无效 filters 返回 `400` 字段级错误。
- 未知 space/node/edge/run 返回 `404`。
- 未认证返回 `401`；无权限返回 `403`。
- Projection 拒绝 unapproved、missing-trace、unsupported、unsafe-path 或 no-evidence candidates。
- 用户可见错误必须脱敏。

## UI / 用户流程设计

1. 用户打开 Knowledge Space Graph tab。
2. 前端加载 graph view 并显示 loading state。
3. Empty graph state 说明需要 approved/published evidence。
4. 用户搜索或过滤图谱。
5. 用户选择 node 或 edge。
6. Detail panel 展示 type、review status、confidence、相邻关系和 evidence references。
7. 如授权允许，SME reviewer 可对 eligible edge 提交 review action。

## 可访问性与响应式行为

- Graph nodes 和 list equivalents 必须可通过键盘访问。
- Detail panel 必须以文本展示选中 graph object，而不只依赖视觉。
- Tablet/mobile 可使用内部 graph 滚动，同时保留可读 filters 和 evidence details。
- 支持宽度下不得出现文字重叠或控件裁切。

## 安全 / 审计 / 可靠性设计

- 后端服务端强制 auth/RBAC。
- Projection 与 review actions 追加写审计。
- 确定性 projection 测试不使用外部网络调用。
- Adapter seam guard 防止 graph/model/vector engine 泄露到产品层。
- Graph DTO 不返回原始机密内容。

## 测试考虑

- 后端单元：eligibility、幂等 projection ids、exclusion reason codes、安全错误脱敏。
- 后端集成/API：endpoint contracts、auth failures、persistence、audit append-only。
- 前端单元/组件：graph mapping、filters、selected detail、error/empty/unauthorized states。
- E2E：打开 Graph、过滤/搜索、选择 evidence-backed node/edge、验证 trace/confidence/review fields。
- 扫描：`git diff --check`、无新增外部网络/依赖、无明文 secret/私有路径。

## 实现后评审防护

- API-backed 前端验收面必须是 Knowledge Space Graph tab 本身。
- Shell-level 或 host-level hardening panel 可以作为过渡辅助，但不能单独满足 `GraphViewer`、`GraphFilters` 或 Graph tab E2E 验收。
- 后续 close-out 必须包含 `[data-tab="graph"]` 内部的 E2E 断言，覆盖 search/filter controls、selection、evidence detail，以及 unauthorized/empty/error states。

## 风险 / 设计取舍

- 通过有界 API 响应和 filters 控制密集图谱渲染。
- 真实 extraction engine 延后，以保留 adapter neutrality。
- `review-publish` 依赖在实现时可能需要 approved fixture data。

## 待确认问题

- OQ-KG-001 到 OQ-KG-003 仍为产品排序问题，但不阻塞确定性 SDD 交接。

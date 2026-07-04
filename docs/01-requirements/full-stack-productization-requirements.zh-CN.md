# 需求：全栈产品化

## 状态

实现草案。切片 `full-stack-productization`。Phase 4 加固 / P0 产品化。

## 目标

将 Atlas Knowledge Hub 从静态/mock UI 推进到真实 API 驱动的浏览器闭环：

```text
Knowledge Space 列表 -> Space 详情 -> metadata-only 示例上传/批次 -> 文件/chunk 溯源
-> SME 审核 -> 发布 Wiki -> 图谱投影 -> 可信 Ask
```

## 范围

### 范围内

- Vue UI 针对 P0 工作流调用真实 Atlas 后端 API。
- metadata-only 示例上传通过现有 batch API 创建安全 mock/sample 批次清单。
- Review、Publish、Wiki、Graph、Vector indexing 与 Ask 使用后端服务和 adapter 边界。
- 可见展示 loading、empty、error、disabled/coming soon 状态。
- 新增 UI 驱动 Playwright E2E，证明浏览器用户可完成闭环。

### 范围外

- 生产文件字节上传、真实公司文档、外部云调用、生产认证/RBAC、生产存储或生产向量/模型供应商。
- 复制 WeKnora 代码、结构、资产或专有 UI 细节。
- 替换现有 first-layer、second-layer 或 provider-backed 测试策略。

## 需求

| ID | 需求 | 优先级 |
|---|---|---|
| REQ-FSP-001 | 首页必须通过 `GET /api/spaces` 列出 Knowledge Spaces，并展示 loading、empty、error 状态。 | Must |
| REQ-FSP-002 | 选择 Knowledge Space 必须通过 `GET /api/spaces/{spaceId}` 加载详情，并在 P0 tabs 中展示所选 space 上下文。 | Must |
| REQ-FSP-003 | Documents tab 必须通过 `POST /api/spaces/{spaceId}/batches` 创建 metadata-only 示例批次，并从后端 API 加载 batches、files、source chunks。 | Must |
| REQ-FSP-004 | 文件和 chunk 视图必须保留后端响应中的 source trace、confidence、status、review status。 | Must |
| REQ-FSP-005 | Review tab 必须通过 `GET /api/spaces/{spaceId}/review-queues` 加载审核队列，并通过 `POST /api/files/{fileId}/reviews` 提交 SME 审核。 | Must |
| REQ-FSP-006 | Publish 流程必须调用 `POST /api/files/{fileId}/publish`，刷新 `GET /api/spaces/{spaceId}/wiki-pages`，并展示 `PUBLISHED` Wiki 元数据。 | Must |
| REQ-FSP-007 | 发布后，浏览器流程必须通过现有 graph/vector API 刷新可信下游证据，使 Graph tab 和 Ask tab 能引用已发布证据。 | Must |
| REQ-FSP-008 | Graph tab 必须保持 API-backed，并展示包含已发布文件/chunk 的图谱证据。 | Must |
| REQ-FSP-009 | Ask tab 必须调用 `POST /api/spaces/{spaceId}/ask` 和 `GET /api/ask-runs/{runId}`，并展示 answer、citations/evidence 与 `REVIEW_REQUIRED` answer status。 | Must |
| REQ-FSP-010 | 未真实接通后端行为的 UI 控件必须 disabled 并标注 coming soon，不能表现为可用。 | Must |
| REQ-FSP-011 | 实现不得破坏现有 first-layer、second-layer、provider-backed E2E 命令与语义。 | Must |
| REQ-FSP-012 | 验证必须尽可能覆盖前端 typecheck/unit/build/E2E、后端 `mvn verify`、`git diff --check`、secret/private-path 扫描、新增网络/依赖扫描。 | Must |

## 约束

- 仅使用 mock/sample data。
- 不引入真实公司文档、截图、凭证、日志、私有路径或机密内容。
- Parser、converter、model、vector、storage、graph projection 逻辑必须留在产品侧 adapter/service 边界之后。
- 前端不得直接调用 provider、parser、vector DB、storage 或 converter engine。
- 生成的 Ask answer 保持 `REVIEW_REQUIRED`。

## 假设

- 除非本切片发现阻塞缺口，现有 Spring Boot endpoints 就是 P0 API contract。
- metadata-only 示例批次创建可作为 P0 浏览器触发上传的替代。
- 发布后可调用现有 adapter-backed 后端 endpoint 刷新 graph/vector，使同一证据进入下游面板。

## 开放问题

- P1 文件字节上传 UX 与存储策略延后。
- 生产认证/RBAC 与组织权限延后。

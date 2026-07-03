# 需求：Review Publish

## 状态

草稿。切片 `review-publish`。Phase 4 hardening。

## 目标

让 SME 和交付负责人能够审核可追溯 Markdown，并且只把已批准内容发布为 Wiki 元数据，同时将被阻断、低置信度或 LLM 生成待审内容排除在可信 Wiki、Graph 和 Ask 之外。

## 切片契约

- **范围：** SME 审核状态机加固、发布资格门禁、已批准 Markdown 发布元数据、审核历史、API/UI 契约，以及全栈实现验证计划。
- **排除项：** 真实认证/SSO、生产 RBAC enforcement、外部模型/向量/搜索调用、图谱抽取实现、Ask/RAG 答案生成、真实文档上传、真实公司文档、生产 secret 存储落地。
- **来源：** `docs/01-requirements/requirement.md`、`docs/review-workflow.md`、`docs/markdown-standard.md`、`docs/knowledge-graph-design.md`、`docs/00-context/slice-roadmap.md`、`docs/03-spec/knowledge-space-spec.md`、`docs/04-architecture/metadata-api-data-model.md`、`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`、`frontend/public/atlas-prototype.html`、`prototypes/index.html`。
- **验证行：** Phase 4 hardening 要求对所改层运行完整单元、集成和 E2E。
- **约束行：** 所有 Markdown/元数据保留 source trace、confidence、review status；LLM 产物在核验前保持 review-required；新增端点必须有 API guide。

## 需求

| ID | 需求 | 优先级 | 阶段 |
|---|---|---:|---|
| REQ-REVIEW-PUBLISH-001 | 系统必须暴露发布阻断条件的审核队列：缺少 `source_trace`、低置信度、需要 OCR、解析失败、LLM 生成待审核内容。 | Must | 4 |
| REQ-REVIEW-PUBLISH-002 | 系统必须支持 SME 审核动作 `APPROVE`、`NEED_FIX`、`OCR_REQUIRED`，并映射到 `APPROVED`、`NEED_FIX`、`OCR_REQUIRED` 状态，且不允许未审核内容直接发布。 | Must | 4 |
| REQ-REVIEW-PUBLISH-003 | 审核历史必须追加写入，记录 reviewer、action、timestamp、comment、affected chunks 和目标身份。 | Must | 4 |
| REQ-REVIEW-PUBLISH-004 | 发布资格必须要求 `APPROVED` review status、相对 Markdown 路径、至少一个来源文档引用、source trace 覆盖和非空 confidence。 | Must | 4 |
| REQ-REVIEW-PUBLISH-005 | 发布必须创建或更新 Wiki page 元数据，并设置 `PUBLISHED` review status，同时保留 source document IDs、Markdown path、confidence、owner 和 last updated timestamp。 | Must | 4 |
| REQ-REVIEW-PUBLISH-006 | 发布不得修改原始 parser output、source chunks 或原始 source paths；修正和发布元数据必须与原始产物分离。 | Must | 4 |
| REQ-REVIEW-PUBLISH-007 | 发布资格失败必须返回用户安全的 validation errors，且不得泄露 stack trace、secret、private path 或原始机密内容。 | Must | 4 |
| REQ-REVIEW-PUBLISH-008 | 前端必须基于已接受 FE 基线展示 Processing Center 发布准备状态、阻断数量、审核动作和已发布 Wiki 状态，且不得暗示生产 RBAC 已经落地。 | Must | 4 |
| REQ-REVIEW-PUBLISH-009 | 可信下游表面只能消费本切片产出的 published 或明确 approved/source-traced 内容；图谱抽取和 Ask 生成保持为独立切片。 | Must | 4 |
| REQ-REVIEW-PUBLISH-010 | 实现必须使用已有产品面后端边界，不得直接调用 parser、converter、model、vector、storage 或 search engine。 | Must | 4 |
| REQ-REVIEW-PUBLISH-011 | 测试必须覆盖审核转换、发布资格门禁、追加审核历史、API envelope、前端发布状态渲染、无网络行为和 secret/private-path 安全。 | Must | 4 |

## 假设

- Phase 3 model、storage、parser、converter adapters 视为已通过产品面 seam 可用，但本切片不执行这些 adapter。
- Phase 2 metadata API 已引入 review status enum、review history、source chunks、file items 和 deferred `wiki_page` 元数据。
- 授权以 role-aware contract 和测试表示；生产 auth/RBAC 实现不在本切片范围，除非未来接受的 SDD 另行加入。

## 待确认问题

- Chunk 级审核和 file 级审核应共用一个 endpoint family，还是在实现切片中拆成独立目标？
- 本切片是否支持批量发布，还是仅支持一次发布一个页面/文件？
- 生产 RBAC 接受后应使用哪些角色标签：仅 Owner/Admin/Reviewer/Viewer，还是组织特定角色？

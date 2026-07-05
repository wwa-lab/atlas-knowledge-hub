# 规格：Review Publish

## 状态

草稿。切片 `review-publish`。Phase 4 hardening。由 `user-story-to-spec` 推导。

## 概览

Review Publish 加固 SME 审核与可信 Wiki 发布之间的桥。该切片把已批准、可追溯的 Markdown 元数据变成已发布 Wiki page 记录，并阻断未审核、低置信、缺 trace、失败或 LLM 生成待审内容进入可信 Wiki、Graph 和 Ask 上下文。

## 来源故事

| Story | 能力 |
|---|---|
| US-REVIEW-PUBLISH-001 | 分诊发布阻断审核队列。 |
| US-REVIEW-PUBLISH-002 | 记录 SME 审核决策。 |
| US-REVIEW-PUBLISH-003 | 发布已批准 Markdown 到 Wiki 元数据。 |
| US-REVIEW-PUBLISH-004 | 保留原始证据和适配器边界。 |

## 角色

- **SME Reviewer：** 审核生成 Markdown 或 chunks，并决定内容是否可推进。
- **Delivery Lead：** 监控 batch readiness 和 blocked quality queues。
- **Knowledge Space Admin：** 将已批准内容发布到 Wiki metadata layer。
- **Knowledge Consumer：** 只在 Wiki、Graph、Ask 中看到可信/已发布知识。

## 功能范围

### Review Queue 与 Processing Center

- 系统列出发布阻断类别：parser failure、OCR required、low confidence、missing `source_trace`、LLM-generated review-required content。
- Publish-ready 内容与 blocked content 分开。
- Queue counts 必须与 API response 使用同一套 metadata 对齐。
- UI action 在 auth 切片存在前不得暗示生产 RBAC enforcement 已落地。

### SME Review 状态机

- 有效 review actions 为 `APPROVE`、`NEED_FIX`、`OCR_REQUIRED`。
- 目标结果状态为 `APPROVED`、`NEED_FIX`、`OCR_REQUIRED`。
- `PUBLISHED` 不是 review action 的结果；只能由 publish 设置。
- Review history 追加写入并按时间排序。

### 发布资格

候选项仅在全部条件满足时可发布：

- Review status 为 `APPROVED`。
- Markdown path 存在且为相对路径。
- Confidence 存在。
- 至少有一个 source document id。
- 发布目标存在 source trace coverage。
- 目标不是 parser-failed、unsupported、OCR-required、need-fix 或 missing-trace。

### 发布结果

- Publish 创建或更新 Wiki page metadata record。
- Published record 的 `reviewStatus=PUBLISHED`。
- 记录保留 title、space id、Markdown path、source document ids、confidence、owner 和 last updated timestamp。
- Publish 不修改 raw parser output、source chunks 或 original source paths。

### 下游可信边界

- Wiki 读取本切片发布的 page metadata。
- Graph extraction 和 Ask/RAG 使用 published/approved/source-traced content 作为输入，但由后续切片实现。
- 未解决的 Processing Center issues 继续被排除在可信下游上下文之外。

## 非功能需求

- API response 使用已有 Atlas envelope 和用户安全 error style。
- 不引入外部网络调用。
- 不引入 raw secrets、private absolute paths 或真实公司数据。
- Parser、converter、model、vector、storage 和 search engines 保持在产品面 adapter 后，publish logic 不直接调用。
- 测试覆盖 touched layers 的完整 unit、integration 和 E2E 路径。

## 状态模型

```text
REVIEW_REQUIRED --APPROVE--> APPROVED --PUBLISH--> PUBLISHED
REVIEW_REQUIRED --NEED_FIX--> NEED_FIX
REVIEW_REQUIRED --OCR_REQUIRED--> OCR_REQUIRED
APPROVED --NEED_FIX--> NEED_FIX
APPROVED --OCR_REQUIRED--> OCR_REQUIRED

PUBLISHED 对已发布 Wiki metadata 为终态，直到未来 revision workflow 被接受。
```

## API 行为

- `GET /api/spaces/{spaceId}/review-queues` 返回队列计数和代表性条目。
- `POST /api/files/{fileId}/reviews` 继续追加 review records 并更新 review status。
- `POST /api/files/{fileId}/publish` 将合格 file 发布到 Wiki metadata。
- `GET /api/spaces/{spaceId}/wiki-pages` 列出已发布 Wiki page metadata。
- `GET /api/wiki-pages/{wikiPageId}` 返回单个已发布 Wiki page metadata record。

## 验收矩阵

| Requirement | Spec Section | 可观察检查 |
|---|---|---|
| REQ-REVIEW-PUBLISH-001 | Review Queue And Processing Center | Queue API/UI 区分 blocked issue categories。 |
| REQ-REVIEW-PUBLISH-002 | SME Review State Machine | Review transition tests 证明 action/status mapping。 |
| REQ-REVIEW-PUBLISH-003 | SME Review State Machine | History list 追加写入且按时间排序。 |
| REQ-REVIEW-PUBLISH-004 | Publish Eligibility | 不合格候选返回 400/409 且不修改数据。 |
| REQ-REVIEW-PUBLISH-005 | Publish Result | Wiki page response 展示 `PUBLISHED` 和保留 metadata。 |
| REQ-REVIEW-PUBLISH-006 | Publish Result | Publish 后 raw file/chunk metadata 不变。 |
| REQ-REVIEW-PUBLISH-007 | API Behavior | Error responses 不含 stack trace、secret 或 absolute path。 |
| REQ-REVIEW-PUBLISH-008 | Review Queue And Processing Center | FE 渲染 readiness、blocked counts 和 published status。 |
| REQ-REVIEW-PUBLISH-009 | Downstream Trust Boundary | Graph/Ask 仅作为消费者保留，不实现生成。 |
| REQ-REVIEW-PUBLISH-010 | Non-Functional Requirements | Dependency scan 未发现 publish logic 直连 engine。 |
| REQ-REVIEW-PUBLISH-011 | Non-Functional Requirements | Unit、integration、E2E、no-network 和 secret scans 通过。 |

## 范围外

- 真实生产 auth/RBAC enforcement。
- Graph extraction implementation。
- Ask/RAG generation。
- 真实 parser/converter/model/vector/storage/search execution。
- Bulk publish，除非实现规划中另行接受。

## 风险与待确认问题

- 如果 file-level review 过粗，chunk-level review 可能需要扩展 endpoint。
- Processing Center 面向 batch-scale，stakeholder 可能要求 bulk publish。
- 在向 trusted internal 之外暴露这些 endpoints 前，生产 RBAC 需要后续切片。

## Product Goal Batch 2 Vue 对齐补充

Product Goal Batch 2 将本切片的 Processing Center 部分延展到真实 Vue 知识空间详情页。

| Phase | Vue 产品验收 |
|---|---|
| Phase D 处理中心 | 真实 Vue `IBM i Modernization` Processing Center 展示文档总数、解析失败、需要 OCR、低置信度、缺少 `source_trace`、LLM 生成需审核、待发布计数，并用队列行解释内容为什么被 Wiki、Graph 和 Ask 排除。 |

本补充只改变 Vue 产品界面成熟度；不新增 backend/API 行为、生产 RBAC、真实 remediation worker 或外部 provider 调用。

## Product Goal Batch 3 Vue Parity Addendum

Product Goal Batch 3 将本切片中的已发布 Wiki 能力扩展到真实 Vue 知识空间详情页。

| Phase | Vue Product Acceptance |
|---|---|
| Phase E LM Wiki | 真实 Vue `IBM i Modernization` Wiki 标签页提供可浏览 Wiki 索引、密集的 Markdown-like 内容段落、页面 metadata、实体链接、confidence、review status，以及针对 published、approved、review-required 样例页面的可见 `source_trace` blocks。 |

该 addendum 仅更新 Vue 产品界面成熟度，不新增真实 Markdown generator、真实文档内容、新 backend/API 行为或外部 provider 调用。

# 需求：Wiki 数据模型

## 状态

待用户接受的草案。切片 `wiki-data-model`。Phase 4 hardening / Wiki Foundation。

## 目标

将 Atlas Wiki 从“已发布 metadata 展示面”升级为未来 Auto Wiki 生成、刷新、互链、lint 和审核工作流所需的数据模型底座；本切片不实现这些后续工作流。

## 切片契约

- **范围：** 扩展 `wiki_page` metadata；新增最小 `wiki_folder`、`wiki_generation_run`、`wiki_log_entry`、`wiki_page_issue` 支撑；提供按 slug 查询页面、读取 folder/log/issue 的 API 契约；Vue Wiki tab 展示新增可见 metadata。
- **排除：** Auto Wiki ingest pipeline、linkify/lint 规则引擎、生产 auth/RBAC、真实公司文档、外部 cloud call、新模型 provider、raw parser/converter/model/vector/storage 执行。
- **已 grounding 的基线：** 现有后端 `wiki_page` 包含 `id`、`space_id`、`title`、`markdown_path`、`source_document_ids`、`confidence`、`review_status`、`owner`、`last_updated`；现有 API 支持发布、列表和按 id 读取；现有 Vue Wiki tab 映射 API pages，并保留 sample-safe fallback。
- **验证行：** Phase 4 hardening 需要后端验证、前端 typecheck/test/build/E2E、可行时 second-layer E2E、diff hygiene、secret/private-path scan、network/dependency scan。
- **人工门：** 当前用户接受本 SDD 集之前，不开始产品代码实现。

## 需求

| ID | Requirement | Priority | Phase |
|---|---|---:|---|
| REQ-WIKI-DATA-MODEL-001 | `wiki-data-model` 的 SDD 集必须同时存在英文和简体中文版本，并保持 REQ/US/T ID 一致，之后才能开始产品代码变更。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-002 | `wiki_page` 必须新增 `slug`、`page_type`、`aliases`、`source_refs`、`chunk_refs`、`in_links`、`out_links`、`version`、`source_mode`、`refresh_policy`，同时保留现有 publish/list/id-detail 行为。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-003 | Slug 查询必须按 Knowledge Space 隔离，不能暴露其他 space 的页面。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-004 | `wiki_folder` 必须提供按 space 组织 Wiki page 所需的最小层级，不实现权限继承或生产 RBAC。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-005 | `wiki_generation_run` 必须记录未来生成/刷新 run metadata，但本切片不执行 ingest pipeline。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-006 | `wiki_log_entry` 必须记录安全的 Wiki 生命周期事件，不得存储 raw document text、prompt、secret、provider payload 或 private path。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-007 | `wiki_page_issue` 必须表达未来维护发现，例如 stale source、broken link、orphan、thin content、review required，但不实现规则评估。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-008 | API responses 必须通过 Atlas `ApiEnvelope` 约定和用户安全错误暴露新增 Wiki page 字段以及最小 folder/log/issue read models。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-009 | 现有 `POST /api/files/{fileId}/publish`、`GET /api/spaces/{spaceId}/wiki-pages`、`GET /api/wiki-pages/{wikiPageId}` 必须对当前测试和 E2E 流程保持向后兼容。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-010 | Vue Wiki 视图必须在 API 数据提供时展示新增可见 metadata，包括 slug、page type、aliases、source/chunk refs、link counts 或 link ids、version、source mode、refresh policy。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-011 | Vue Wiki fallback sample safety 必须保留：API pages 不可用时，UI 只能使用确定性的 mock/sample-safe 内容。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-012 | 本切片必须在 Wiki page responses、Graph/Ask downstream evidence 和前端展示中保留 source trace、confidence、review status。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-013 | 数据库迁移必须是 additive，且不得破坏现有 V1-V9 契约、seeded mock/sample data 或 review-publish 行为。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-014 | Parser、converter、model、vector、storage、search 行为必须继续位于 Atlas adapter 边界之后；本切片不得直接调用 engine。 | Must | 4 |
| REQ-WIKI-DATA-MODEL-015 | 测试必须覆盖后端页面新增字段、slug lookup、folder/log/issue 最小读取、前端 metadata 展示、现有 review-publish/graph/Ask 回归和安全扫描。 | Must | 4 |

## 假设

- 现有 `source_document_ids` 继续作为兼容字段保留，同时由 `source_refs` 和 `chunk_refs` 增加更适合未来演进的引用。
- `slug` 在每个 `space_id` 内唯一；不同 space 可复用相同 slug。
- `page_type`、`source_mode`、`refresh_policy`、issue type/status、generation run status 在数据库和 domain model 中使用受约束字符串枚举。
- 为避免破坏 seed 数据，folder membership 对现有 pages 可为空。
- 因为 `WEKNORA_ANALYSIS_DIR` 未设置，本轮未读取 WeKnora 分析文件；仅使用 Atlas 自有文档和代码。

## 待确认问题

- OQ-WIKI-DATA-MODEL-001: 未来 ingest 生成的页面应直接进入 `REVIEW_REQUIRED`，还是先作为 draft records 再发布？
- OQ-WIKI-DATA-MODEL-002: `source_refs` 未来只保留 ID/label，还是加入稳定 checksum 用于 freshness checks？
- OQ-WIKI-DATA-MODEL-003: Folder ordering 应手动维护、按 slug 生成，还是推迟到 Wiki navigation UX 切片？

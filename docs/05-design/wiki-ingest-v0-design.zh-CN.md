# 详细设计：Wiki Ingest v0

## 状态

已接受并完成 Auto Wiki ingest v0 implementation verification。生成 candidates 仍保持 review-required；本设计不完成 linkify/lint、review-gate approval、connector sync、model-assisted generation 或 production readiness。

## Source Architecture

- `docs/03-spec/wiki-ingest-v0-spec.md`
- `docs/04-architecture/wiki-ingest-v0-architecture.md`
- `docs/04-architecture/wiki-ingest-v0-data-flow.md`
- `docs/04-architecture/wiki-ingest-v0-data-model.md`

## Grounded Existing Code

- `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:115` 创建带 source chunk refs 的 published Wiki metadata。
- `backend/src/main/java/com/atlas/metadata/service/ReviewPublishService.java:138` 发布 approved Markdown files，并在 `ReviewPublishService.java:144` 阻止 missing source trace。
- `backend/src/main/java/com/atlas/metadata/service/IngestionService.java:58` 摄取 PDF/ZIP uploads 并创建 parser runs，但其 file metadata 在 `IngestionService.java:168` 默认 `REVIEW_REQUIRED`。
- `frontend/src/types.ts:365` 定义当前 `ApiWikiPage` shape，包含 slug、refs、source mode、refresh policy、confidence 和 published review status。

## Design Assumptions

- v0 deterministic generation 是本 slice 的唯一 generation mode。
- Model-assisted generation 是未来工作，引入时必须使用 ModelAdapter。
- v0 写入带确定性安全摘要和安全 source/chunk labels 的 generated Markdown artifacts。
- 现有 published Wiki read behavior 仍是 trusted default。
- Draft generated candidates 仅在 API/UI 明确请求 generated 或 review-required pages 时暴露。

## Design Scope

范围内：

- Backend Wiki ingest service 和 API contracts。
- Approved source chunk selection。
- Candidate builder、slug derivation、idempotent merge、safe run/log/issue records。
- 可选 frontend status display，用于 generated review-required candidates。
- Backend、frontend、E2E 和 safety verification。

范围外：

- Parser/converter runtime changes、linkify/lint、review approval actions、refresh/retract、production RBAC、connector sync、model-assisted generation、外部 provider calls。

## Module Design

### Backend Wiki Ingest API

- 新增 space-scoped start-run endpoint。
- 校验 requested mode 和 space。
- 返回带 safe run response 的 Atlas envelope。
- 保持现有 published Wiki list/detail behavior 不变。

### Backend Wiki Ingest Service

职责：

- 创建 `wiki_generation_run`，状态从 `REQUESTED` 到 `RUNNING`。
- 加载 eligible chunks。
- 构建 deterministic candidates。
- 按 slug 合并 candidates。
- 记录 safe logs 和 issues。
- 以 `SUCCEEDED`、`PARTIAL_FAILED` 或 `FAILED` 完成 run。

### Input Selector

规则：

- 只包含 `APPROVED` chunks。
- 如可用，要求 source file 和 page 或 section locator。
- 排除 failed、unsupported、OCR-required、review-required 和 need-fix evidence。
- 记录 exclusion counts，不记录 raw text。

### Candidate Builder

默认 deterministic candidate：

- title 来自 source section/topic label。
- slug 来自 normalized title。
- page type `TOPIC`。
- generated Markdown artifact，内容为确定性安全摘要。
- source mode `AUTO_GENERATED`。
- refresh policy `ON_SOURCE_CHANGE`。
- review status `REVIEW_REQUIRED`。
- confidence 为 included chunk confidence 的最低值。
- refs 为 safe file/chunk IDs 与 locators。

### Merge Policy

| Case | Behavior |
|---|---|
| New slug | 创建 generated review-required candidate。 |
| Existing generated review-required slug | 合并 refs 并更新 safe summary。 |
| Existing trusted/published `PUBLISHED_FILE` slug | 保留 page 并记录 safe issue。 |
| Existing slug in another space | 无冲突。 |

### Future Model Assistance

- v0 拒绝或禁用 model-assisted requests。
- 如果后续启用 model assistance，service 只调用 ModelAdapter。
- Raw prompt/provider payload 不得存入 run/log/page records。
- Model-assisted output 仍为 `REVIEW_REQUIRED`。

## API / Interface Design

API guide 位于 `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`。

Primary endpoints：

- `POST /api/spaces/{spaceId}/wiki-ingest-runs`
- `GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}`
- `GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true`

## Data Design

- 优先复用现有 Wiki data model tables。
- 只有当实现需要 V10 不具备的 fields 时才添加 migration。
- Candidate page records 必须包含 safe refs、source mode、refresh policy、confidence 和 review status。
- Run/log/issue records 只能是 safe summaries。

## UI / User Flow Design

1. Knowledge manager 从已接受 UI affordance 或 API client 启动 run。
2. UI 收到 safe run summary。
3. Wiki 或 Processing Center 展示 generated/review-required candidate status。
4. 现有 published Wiki pages 保持 trusted default view。
5. 用户可查看 refs/counts，但本 slice 不能 approve generated candidates。

## Validation and Error Handling

| Input | Rule |
|---|---|
| `spaceId` | 必须存在。 |
| mode | v0 接受 `deterministic`；model-assisted mode 在本 slice 中被拒绝或禁用。 |
| eligible chunks | 必须 approved 且 traceable。 |
| slug | lowercase URL-safe，per space unique。 |
| generated page status | v0 中始终 `REVIEW_REQUIRED`。 |

Error behavior：

- no eligible evidence -> safe successful run，candidate 数为 0。
- trusted slug collision -> safe issue，不覆盖。
- unexpected failure -> `FAILED`，带 sanitized safe error。

## Testing Considerations

- Backend contract tests 覆盖 start/read run。
- Service tests 覆盖 input eligibility、candidate metadata、idempotency、collision handling 和 review-required defaulting。
- Repository tests 覆盖 run/log/issue persistence 以及 rerun 不重复建页。
- 如触碰 UI，则添加 frontend tests 覆盖 draft/review-required display。
- E2E regression 覆盖现有 Wiki、Graph、Ask 和 publish flows。
- Safety scans 覆盖 secrets/private paths 与新增 network/dependency calls。

## Risks / Design Tradeoffs

| Risk | Mitigation |
|---|---|
| Deterministic pages 偏薄 | 保持 candidates review-required，并明确标注 v0。 |
| Draft candidates 让用户困惑 | 现有 published view 保持 trusted default；generated status 明确。 |
| Existing dirty worktree contains prior slice changes | 实现必须在接受后只触碰 wiki-ingest 文件。 |

## 已解决决策

- v0 只创建 `TOPIC` pages。
- v0 写入带确定性安全摘要的 generated Markdown artifacts。
- v0 使用 included chunk confidence 的最低值聚合 confidence。

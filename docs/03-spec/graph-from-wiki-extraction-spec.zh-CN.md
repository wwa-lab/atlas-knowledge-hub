# 功能规格：graph-from-wiki-extraction

规格状态：已由 standard-preauthorized prompt 接受。

## 概览

Graph projection 必须变成 Wiki-driven。现有 graph projection endpoint 保持入口不变，但 eligible Wiki pages 成为主要抽取来源，source chunks 继续作为 evidence anchors。

## 功能需求

### Eligibility

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001：Wiki page 只有在 review status 为 `APPROVED` 或 `PUBLISHED`、至少有一个 source reference 或 chunk reference、且 confidence 为空或不低于 `0.800` 时，才是 trusted graph input。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-001：Ineligible Wiki pages 不作为 trusted graph input。Projection items 记录安全 skip reasons：`UNAPPROVED_WIKI_PAGE`、`LOW_CONFIDENCE_WIKI_PAGE` 或 `MISSING_WIKI_SOURCE_TRACE`。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005：Evidence responses 只暴露 IDs、title/section labels、review status、confidence、page、section 和安全 source file names。

### Extraction Rules

- REQ-GRAPH-FROM-WIKI-EXTRACTION-002：Graph IDs 是 deterministic：从 space ID、Wiki page ID/slug、source document ID、source chunk ID 和 normalized labels 派生。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003：每个 eligible Wiki page 生成一个 `WIKI_PAGE` node。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003：Wiki page 上每个安全 source document ID 生成一个 `DOCUMENT` node，以及 Wiki page 到 document 的 `DERIVED_FROM` edge。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003：每个 chunk reference 生成一个 `CONCEPT` node，label 来自 Wiki reference label 或 section，并生成 Wiki page 到 concept 的 `MENTIONS` edge。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004：Wiki `outLinks` 指向其他 eligible pages 时生成 Wiki page nodes 之间的 `RELATED_TO` edges。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004：每个 node 和 edge 存储 `evidence_wiki_page_ids`；chunk-derived objects 同时存储 `evidence_chunk_ids`。

### API Behavior

- REQ-GRAPH-FROM-WIKI-EXTRACTION-006：`POST /api/spaces/{spaceId}/graph/projection-runs` 保持 `scope: APPROVED_ONLY` 和 `adapterId: deterministic`。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006：现有 graph query filters 继续工作。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007：`GET /api/spaces/{spaceId}/graph/nodes/{nodeId}` 返回带 `referenceType` discriminator 的 mixed evidence references。

### Frontend Behavior

- REQ-GRAPH-FROM-WIKI-EXTRACTION-007：Graph detail panel 按 reference type 区分 Wiki page evidence 与 source chunk evidence。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006：现有 graph search、node/edge filters、evidence-only toggle、empty、unauthorized 和 error states 保持不变。

## Acceptance Matrix

| ID | Verification |
|---|---|
| AC-GRAPH-FROM-WIKI-EXTRACTION-001 | Backend unit/API tests 证明 eligible Wiki pages 被抽取，ineligible pages 被跳过。 |
| AC-GRAPH-FROM-WIKI-EXTRACTION-002 | Backend tests 证明重复 projection 不重复生成 graph records。 |
| AC-GRAPH-FROM-WIKI-EXTRACTION-003 | API contract tests 证明 node detail 包含 Wiki 与 chunk evidence 且无 unsafe content。 |
| AC-GRAPH-FROM-WIKI-EXTRACTION-004 | Frontend tests 和 E2E 证明 Wiki-derived graph evidence 可见。 |

## 约束

- 不做 model-assisted graph extraction。
- 不调用外部 graph service、cloud、provider payload 或真实数据。
- 不改变 auth/RBAC/audit/secret/rate-limit 语义。
- Existing Graph product surface 不得回归。

## Task IDs

- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008

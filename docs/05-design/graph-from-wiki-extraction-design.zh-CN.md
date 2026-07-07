# 详细设计：graph-from-wiki-extraction

## Backend Design

- T-GRAPH-FROM-WIKI-EXTRACTION-001：扩展 graph projection descriptors，包含 Wiki page metadata、chunk references、source document IDs、links、review status 和 confidence。
- T-GRAPH-FROM-WIKI-EXTRACTION-002：`GraphService` 加载 space 内所有 Wiki pages，过滤 eligible pages，记录 skipped pages，并把 eligible descriptors 传给 deterministic adapter。
- T-GRAPH-FROM-WIKI-EXTRACTION-003：`DeterministicGraphProjectionAdapter` 输出稳定的 Wiki page、document、concept 和 relationship candidates。
- T-GRAPH-FROM-WIKI-EXTRACTION-004：`GraphService` 持久化 candidates，并写入 chunk evidence 与 Wiki page evidence arrays。
- T-GRAPH-FROM-WIKI-EXTRACTION-005：Node detail 将 evidence arrays 映射为安全 mixed evidence DTOs。

## Frontend Design

- T-GRAPH-FROM-WIKI-EXTRACTION-006：`ApiGraphEvidenceReference` 增加 `referenceType`、optional `wikiPageId` 和 `label`。
- T-GRAPH-FROM-WIKI-EXTRACTION-006：Graph detail panel 在同一个 detail area 中以清晰 label 渲染 Wiki evidence 和 source chunk evidence。
- T-GRAPH-FROM-WIKI-EXTRACTION-007：Component/unit 和 E2E tests 验证 Wiki-derived evidence 可见，且现有 graph filters 继续工作。

## Validation And Error Handling

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001：不支持的 `scope` 或 `adapterId` 仍为 validation failure。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005：Evidence mapping 必须只使用安全 metadata。
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006：现有 safe error envelope 行为保持不变。

## Review-Doc-Quality Result

Verdict: Ready. SDD set 完整、双语、可追溯，并且处于 prompt preauthorization boundary 内。SDD 变化没有引入 external providers、real data、production auth/RBAC/audit/secret/rate-limit semantics 或 destructive migration。

## IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008

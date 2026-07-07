# 用户故事：graph-from-wiki-extraction

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-001

**标题：** 从已审核 Wiki pages 抽取可信图谱

作为知识使用者，我希望 Graph 节点和边来自已审核 Wiki pages，从而让图谱探索反映可信 Atlas 知识。

### 验收标准

1. **Given** Wiki pages 为 `APPROVED` 或 `PUBLISHED` 且具备 source trace，**when** graph projection run 执行，**then** 从这些页面抽取 graph nodes 和 edges。
2. **Given** Wiki pages 为 `REVIEW_REQUIRED`、`NEED_FIX`、低置信度或缺少 source trace，**when** extraction 执行，**then** 这些页面被跳过并记录安全 reason codes。
3. **Given** 相同 eligible Wiki input，**when** extraction 多次运行，**then** graph IDs 和持久化 records 保持稳定。

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-002

**标题：** 检查 Wiki-derived evidence

作为 SME reviewer，我希望每个 graph object 显示 Wiki page 与 source chunk evidence，从而审计关系为何存在。

### 验收标准

1. **Given** 选中 graph node，**when** detail API 加载，**then** 返回安全的 source chunk 与 Wiki page evidence metadata。
2. **Given** UI 中选中 graph object，**when** evidence 展示，**then** UI 区分 Wiki page evidence 与 chunk evidence。
3. **Given** evidence metadata，**when** 检查 API/UI responses，**then** 不暴露 raw source content、private path、secret、endpoint 或 provider payload。

## User Story US-GRAPH-FROM-WIKI-EXTRACTION-003

**标题：** 保持现有 graph 产品行为

作为 delivery lead，我希望现有 Graph 和 downstream refresh 行为继续工作，从而在不回归当前产品流程的情况下升级 evidence 质量。

### 验收标准

1. **Given** 当前 Graph APIs 和 frontend filters，**when** 本 slice 实现，**then** 现有 graph query、detail、projection 和 edge review contracts 保持兼容。
2. **Given** downstream refresh，**when** 它调用 graph projection，**then** projection 仍保持 deterministic 和 local。
3. **Given** verification commands，**when** 它们运行，**then** backend、frontend、E2E 和 closeout gates 通过。

## Trace IDs

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007
- REQ-GRAPH-FROM-WIKI-EXTRACTION-008

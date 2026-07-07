# 详细设计：Ask Session Citations

## 概述

本设计通过扩展现有 Ask domain，增加 session-aware Trusted Ask history 和安全 answer citations。实现使用增量 backend DTOs、repositories、service methods、Flyway migration，以及 frontend typed rendering。

## 模块设计

### Backend Domain

- `AskSession` 存储一组相关问题的 session。
- `AskRun` 增加 `sessionId`；新的 runs 必须属于一个 session。
- `AskEvidence` 增加 API responses 和 UI 使用的 citation fields。
- Citation status derivation 是确定性的，基于 review status、confidence 和 source trace presence。

### Backend Service

- `AskService.createRun` 校验可选 `sessionId` 和 `sessionTitle`。
- 当 `sessionId` 缺失时，service 使用安全 title 创建 session。
- 当 `sessionId` 存在时，service 在创建 run 前验证 same-space ownership。
- `AskService.listSessions(spaceId)` 返回有界 recent summaries。
- `AskService.getSession(sessionId)` 返回带 ordered runs 和 citations 的 session detail。

### API / Interface Design

- 现有 Ask endpoints 保持兼容。
- 新 session endpoints 返回 `ApiEnvelope`。
- 所有 DTO 只暴露安全展示字段。

### Frontend Design

- 增加 `ApiAskSessionSummary`、`ApiAskSessionDetail` 和更丰富 `ApiAskEvidence` TypeScript types。
- 增加 frontend API helpers `listAskSessions` 和 `getAskSession`。
- Trusted Ask UI 根据可用宽度在 question composer 旁边或下方展示 recent sessions。
- Selected session detail 展示 answer history 和 citation detail。
- 当前 answer view 在 ask submission 后继续即时渲染。

## Validation And Safety Rules

- `sessionTitle`、`requestedBy`、citation labels 和 source locators 必须拒绝或脱敏 secret-like、URL-like 或 private path-like text。
- Unknown 或 cross-space session ids 安全失败。
- Missing source trace 不得标记为 eligible。
- Generated answers 保持 `REVIEW_REQUIRED`。

## Testing Considerations

- Backend contract test 隐式创建 session、复用 session、列出 session 并读取 detail。
- Backend unit/domain tests 覆盖 citation status derivation 和 safe defaults。
- Frontend tests 覆盖 session/citation rendering。
- 现有 Ask tests 必须继续通过。

## Risks / Tradeoffs

- 本切片存储 safe labels，而不是 raw source snippets；这会限制 preview richness，但保护数据安全。
- 现有 rows 可能缺少 session ids；read paths 支持 legacy rows，而新 writes 创建 sessions。

## Open Questions

- None。

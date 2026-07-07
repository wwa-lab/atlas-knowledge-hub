# 需求：graph-from-wiki-extraction

状态：已由 2026-07-07 prompt 预授权接受。

## 目标

从符合条件的 Wiki pages 中确定性抽取知识图谱节点、边和 evidence snapshots，让 Graph 由已审核、具备 source trace 的 Wiki 内容驱动，而不是独立 mock/demo surface。

## 范围

- 仅从 `APPROVED` 或 `PUBLISHED`、具备 source trace、满足 confidence 规则的 Wiki pages 抽取图谱。
- 保留 Wiki page metadata、source trace、section/chunk evidence、review status、confidence 和安全 evidence summary。
- 扩展现有 Spring Boot Graph API、deterministic adapter、Flyway schema、DTO 和 Vue Graph UI。
- 所有抽取保持本地、确定性、幂等，并只使用 mock/sample-safe 数据。

## 不在范围内

- retrieval-quality-metrics、answer-review-governance、ask-session-citations。
- model-assisted extraction、embedding、外部 graph service、外部 cloud provider、真实公司数据。
- auth/RBAC/audit/secret/rate-limit 语义变更。
- 生产 graph layout、graph ML dedup、manual graph editor、SIEM/export。

## 需求

| ID | 需求 | 优先级 | 验收 |
|---|---|---|---|
| REQ-GRAPH-FROM-WIKI-EXTRACTION-001 | 抽取 MUST 只使用 approved、published 或 SDD 明确 eligible 的 Wiki pages。 | Must | `REVIEW_REQUIRED`、`NEED_FIX`、`OCR_REQUIRED`、低置信度、缺少 trace 的页面被跳过或仅 warning。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-002 | 相同 Wiki 输入下抽取 MUST 确定性且幂等。 | Must | 重跑 projection 产生稳定 graph IDs，且不重复生成节点、边或 run items。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-003 | 抽取节点 MUST 在 deterministic metadata 支持时包含 Wiki-derived `WIKI_PAGE`、`DOCUMENT`、`CONCEPT` 和 `ENTITY` candidates。 | Must | 节点包含安全 label、review status、confidence、chunk evidence 和 Wiki page evidence。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-004 | 抽取边 MUST 保留 source trace 和 evidence snapshots。 | Must | 边包含 chunk IDs、Wiki page IDs、confidence、review status 和安全 evidence detail。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-005 | Evidence responses MUST NOT 暴露 raw secrets、private paths、raw documents、internal endpoints、provider payloads 或 confidential content。 | Must | API 和 UI 只显示安全 ID 与 source trace metadata。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-006 | 现有 graph query、node detail、projection run、review action 和 downstream refresh surfaces MUST 保持兼容。 | Must | 现有 graph tests 继续通过。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-007 | 前端 Graph UI MUST 以可检查方式显示 Wiki-derived evidence。 | Must | Detail panel 区分 source chunk evidence 与 Wiki page evidence。 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-008 | 验证 MUST 覆盖 extraction、idempotency、eligibility filtering 和 evidence preservation。 | Must | 后端 unit/API tests、前端 tests、E2E 和 closeout gate 通过。 |

## SDD Skill Chain Evidence

SDD skill chain used: yes.

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`
- `.agents/skills/architecture-review/SKILL.md`

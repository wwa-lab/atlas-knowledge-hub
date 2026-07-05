# SDD Generation Gate Checklist

在接受任何新生成或实质更新的 Atlas SDD 文档集前，使用本 checklist。

本 checklist 检查 SDD 是否准备好进入实现。它不检查代码是否正确。

## 必需证据

Agent completion report 必须包含：

- `SDD skill chain used: yes`
- 已读取入口 skill：`.agents/skills/atlas-sdd-generate-all/SKILL.md`
- 已读取下游 skill files：
  - `.agents/skills/req-to-user-story/SKILL.md`
  - `.agents/skills/user-story-to-spec/SKILL.md`
  - `.agents/skills/spec-to-architecture/SKILL.md`
  - `.agents/skills/architecture-to-design/SKILL.md`
  - `.agents/skills/design-to-tasks/SKILL.md`
  - `.agents/skills/review-doc-quality/SKILL.md`
- 当 architecture、API、persistence、security、adapter boundary 或 data-flow 变化在范围内时，必须包含 `architecture-review` 结果，或明确说明 `not applicable`。
- `review-doc-quality` 结果，或明确的 blocked reason。

## Gate Checklist

| Check | Pass Criteria | Status |
|---|---|---|
| Goal contract | Goal、slice、scope、exclusions、acceptance、verification、constraints 明确。 | |
| Required context | `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`AGENTS.md`、SDD profile、bootstrap docs、manifest、相关 SDD docs 已读取，或缺失项已报告。 | |
| Skill chain | 完整 SDD 生成使用项目本地 skill chain，没有临时手写全部文档。 | |
| Bilingual artifacts | 每个被触碰的必需 SDD artifact 都有英文和简体中文版本。 | |
| Stable IDs | Requirement IDs、user story IDs、task IDs、acceptance IDs 跨语言一致。 | |
| Requirements coverage | Requirements 覆盖当前阶段行为、未来排除项、数据安全、adapter boundaries、review/source-trace 预期。 | |
| Story coverage | User stories 映射到 requirements，并包含 Given/When/Then 验收标准。 | |
| Spec quality | Spec 定义 happy path、empty state、loading state、error state、edge cases 和 acceptance matrix。 | |
| Architecture quality | Architecture 明确模块归属、adapter boundaries、安全/数据约束、API/persistence 范围。 | |
| Data flow quality | Data flow 定义状态流转、review status、source trace、失败/重试行为和延期行为。 | |
| Data model quality | Data model 列出 entities、fields、validation、lifecycle states、相关 indexes 和 migration 约束。 | |
| Design quality | Design 定义 UI 行为、component state、必要的 accessibility、test selectors 和前后端集成方式。 | |
| API guide | 后端/API 在范围内时 API guide 存在；前端-only 时 deferral 已记录。 | |
| Task quality | Tasks 有顺序、范围清晰、可验证、映射到 requirements/spec，并在已知时包含具体验证命令。 | |
| Traceability | Traceability 映射 sources -> requirements -> stories -> spec/design -> tasks -> verification。 | |
| Open questions | 歧义明确记录；高风险歧义阻止实现，而不是被静默决定。 | |
| User acceptance | 当范围、架构、安全、API、持久化、真实数据或外部 provider 行为变化时，实现前要求用户接受。 | |

## Fail-Fast Conditions

出现以下情况时，拒绝或阻断 SDD handoff：

- `SDD skill chain used` 缺失或为 `no`。
- 项目本地 required skill files 不可用，但 agent 仍然生成了 SDD。
- 英文和中文文档在 scope、IDs 或 acceptance 上不一致。
- Requirements 无法追溯到 tasks。
- 后端/API 在范围内但缺少 API guide，且没有记录 deferral。
- 必需 SDD gate 和 acceptance gate 前已经开始实现。

## Acceptance Note Template

```text
SDD gate result:
Slice:
Skill chain evidence:
Documents checked:
Issues found:
User acceptance required before code: yes/no
Decision: accepted | needs revision | blocked
```


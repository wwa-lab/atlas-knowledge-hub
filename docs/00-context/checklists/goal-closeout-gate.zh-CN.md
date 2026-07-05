# Goal Closeout Gate Checklist

在把任何 Atlas goal 标记为完成前，使用本 checklist。

## Closeout Evidence

| Check | Pass Criteria | Status |
|---|---|---|
| Goal mode | 报告说明 `master`、`single-slice` 或 `light-maintenance`。 | |
| Workflow tier | 报告说明 Tier 0、Tier 1、Tier 2 或 Tier 3，并说明原因。 | |
| Manifest | 报告列出使用的 execution manifest；如果 Tier 0 不需要 manifest，说明原因。 | |
| Slice | 命名 active slice；master goal 还要说明已完成 slice 和下一个 eligible slice。 | |
| SDD gate | 完整 SDD 工作报告 `SDD skill chain used: yes`、已读取 skill files 和 `review-doc-quality` 结果。 | |
| User acceptance | 需要 acceptance 时已在写代码前获得；或说明为什么不需要 acceptance。 | |
| Docs changed | 报告列出变更的 SDD、traceability、roadmap、progress 和 checklist docs。 | |
| Code changed | 报告列出变更的实现和测试文件，或说明无代码变更。 | |
| Task mapping | 完成的 task IDs 可映射回 `docs/06-tasks/{slice}-tasks.md`。 | |
| Verification | 必需命令已运行；跳过的检查包含原因。 | |
| Workflow gate | 本地已运行 `npm run agent:closeout`，且适用时 PR/push 的 `Agent Workflow Gate` 为绿灯。 | |
| Security/data | secret、private-path、real-data、external-network scans 已运行，或明确说明跳过原因。 | |
| Maturity | 报告说明成熟度等级，且没有夸大 product readiness。 | |
| Traceability | Slice 状态变化时，`docs/00-context/{slice}-traceability.md` 和 `.zh-CN.md` 已更新。 | |
| Lessons | 可复用 mismatch 已更新 `docs/00-context/lessons-learned.md` 和 prevention artifact。 | |
| Resume point | 如果 goal 没有完全完成，报告说明从哪里恢复。 | |

## Fail-Fast Conditions

出现以下情况时，不得标记 complete：

- 缺少必需 SDD skill-chain evidence。
- 验证失败但没有报告 blocker。
- `npm run agent:closeout` 或 PR/push 上的 `Agent Workflow Gate` 是红灯。
- 没有用户可见 workflow 证据却宣称 product readiness。
- 引入真实数据、secrets、私有路径或未批准外部调用。
- 混入无关 slice。
- 状态变化后 traceability 或 roadmap/progress 仍然过期。

## Closeout Note Template

```text
Goal mode:
Workflow tier:
Slice:
Status:
Maturity:
Manifest:
SDD skill chain used:
Skill files read:
Docs changed:
Code changed:
Task IDs completed:
Verification run:
Skipped checks:
Security/data scans:
Evidence:
Residual risks:
Lessons recorded:
Next action:
Resume point:
```

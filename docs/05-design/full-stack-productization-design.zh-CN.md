# 设计：全栈产品化

## 概述

P0 UI 成为 Vue app 的第一屏。它应像一个可操作的知识工作台，而不是营销页或静态原型宿主。设计保留当前 Atlas 视觉方向，但用真实 API 状态替换 iframe/mock-first 行为。

## View Model

| Area | Design |
|---|---|
| Space list | 左侧栏或顶部区域展示 API-backed cards 和 status。 |
| Space detail | 所选 space header 展示 owner/status/counts 和 tabs。 |
| Documents | Sample upload action、batch list、file list、selected file chunks/source trace。 |
| Review | Queue summary、selected file review action、review result status。 |
| Wiki | Published Wiki page list 和 selected page metadata。 |
| Graph | 现有 API-backed graph panel，按 selected space scoped，并展示 evidence detail。 |
| Ask | Question input、submit button、answer status、answer text、citations。 |
| Coming soon | Production upload、auth/member/admin actions、real provider setup、unsupported prototype-only affordances 使用 disabled buttons。 |

## 交互规则

- Space selection 驱动所有下游 tabs。
- Sample upload 仅在 space 已选择且 create request 未运行时 enabled。
- Approve 仅在 selected file 有 chunks 且尚未 approved 时 enabled。
- Publish 仅在 selected file review status 为 `APPROVED` 时 enabled。
- Graph/Ask refresh buttons 在 publish 后且 chunk ids 存在时 enabled。
- Ask submit 仅在 question 非空且 space 已选择时 enabled。

## API 状态设计

每个 domain 使用一个显式状态：

```text
{ loading: boolean, error: string, data: T }
```

Domain states:

- spaces
- selected space
- batches
- files
- chunks
- review queues
- wiki pages
- graph view/detail
- ask run

## 稳定测试选择器

| Selector | Purpose |
|---|---|
| `data-testid="space-list"` | API-backed space list。 |
| `data-testid="space-card"` | Space selection。 |
| `data-testid="create-sample-batch"` | Metadata-only upload trigger。 |
| `data-testid="batch-list"` | Batch list。 |
| `data-testid="file-list"` | File list。 |
| `data-testid="chunk-list"` | Source trace chunk list。 |
| `data-testid="approve-file"` | Review action。 |
| `data-testid="publish-file"` | Publish action。 |
| `data-testid="wiki-pages"` | Published Wiki list。 |
| `data-testid="refresh-graph-evidence"` | Post-publish graph/vector refresh。 |
| `data-testid="ask-question"` | Ask input。 |
| `data-testid="ask-submit"` | Ask action。 |
| `data-testid="ask-answer"` | Answer panel。 |
| `data-testid="coming-soon"` | Disabled mock-only controls。 |

## 错误文案

错误应短、 安全、可操作。不得包含 raw stack traces、private absolute paths、带 credentials 的 endpoints、provider payloads 或 source document text。

## 响应式设计

P0 app 应为 tab panels、lists、buttons 和 graph canvas 使用稳定 grid/flex 尺寸。文本应在控件内换行且不重叠。Cards 应克制、偏操作台风格，圆角不超过现有设计语言。

## 测试考虑

- Unit tests 应 mock `fetch`，验证 API sequencing、disabled states、answer/evidence rendering。
- UI-driven E2E 应通过浏览器控件完成主闭环。
- 现有 graph E2E tests 必须继续兼容 Graph panel selectors。

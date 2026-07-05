# 架构：真实 Office 解析 Runtime

## 状态

供用户审阅的草稿。用户接受前 implementation 阻塞。

## 概述

- **Architecture summary:** 本切片启用 configured internal converter/parser runtime execution，同时让 Atlas product logic 继续依赖 converter/parser adapter contracts。Runtime-specific execution 被隔离在 adapter/runtime implementation scope 内；metadata control plane 继续负责 validation、run state、safe summaries、trace/review persistence。
- **Design objective:** 在不削弱 adapter neutrality、data safety、review boundaries 的前提下，从 mock-only adapter execution 推进到受控内部 runtime execution。
- **Architectural style:** Layered Spring Boot control plane with adapter-isolated runtime execution。

## 架构驱动

### Functional Drivers

- 只通过 converter adapter 进行 configured conversion。
- 只通过 parser adapter 进行 configured parsing。
- 安全 capability metadata 和 run reports。
- 现有 file、run、result、source chunk metadata 继续作为 product control plane。
- Runtime failure 必须安全、有界、无泄漏。

### Non-Functional Drivers

- 默认 CI 不得要求真实内部 binaries。
- Runtime output 不得泄漏 secrets、hosts、private paths 或 raw logs。
- Runtime integration 必须保留 source trace、confidence、review status。
- 架构必须支持后续 worker/queue topology，而不要求 product-layer rewrite。

## 系统上下文

| Actor / System | Role |
|---|---|
| Knowledge administrator | 启动 conversion/parser runs。 |
| Platform administrator | 配置 runtime availability 并检查 safe capability status。 |
| Atlas metadata API | 验证 requests、解析 adapters、记录 run evidence。 |
| Converter runtime | Converter adapter 后的内部 Office-to-PDF 工具。 |
| Parser runtime | Parser adapter 后的内部 PDF-to-Markdown/assets 工具。 |
| Artifact root | 安全 local 或 storage-backed relative artifact location。 |

## High-Level Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge admin · Platform admin · Delivery lead             │
└─────────────────────────┬────────────────────────────────────┘
                          │ REST / JSON
                          ▼
┌──────────────────────────────────────────────────────────────┐
│ Atlas API Control Plane                                      │
│ capability endpoints · conversion runs · parser runs          │
├──────────────────────────────────────────────────────────────┤
│ Metadata Services                                             │
│ target validation · status mapping · safe summaries           │
├──────────────────────────────────────────────────────────────┤
│ Adapter Registries                                            │
│ default resolution · masked capabilities · safe statuses       │
└──────────────┬───────────────────────────────┬───────────────┘
               │ Adapter contract              │ Adapter contract
               ▼                               ▼
┌──────────────────────────────┐  ┌────────────────────────────┐
│ Converter Adapter Runtime    │  │ Parser Adapter Runtime     │
│ trinity-office isolated      │  │ document-normalize isolated│
└──────────────┬───────────────┘  └──────────────┬─────────────┘
               │ safe relative artifacts          │ safe relative artifacts
               ▼                                  ▼
┌──────────────────────────────────────────────────────────────┐
│ Metadata + Artifact Evidence                                  │
│ file status · run/result rows · source chunks · safe paths     │
└──────────────────────────────────────────────────────────────┘
```

## 组件职责

### API Control Plane

- 暴露现有 converter/parser capability 和 run endpoints。
- 仅在 SDD acceptance 和 implementation 后接受 configured runtime requests。
- 返回标准 Atlas API envelope 和 safe error responses。

### Metadata Services

- 验证 batch 和 file scope。
- 通过 registry 解析 adapter。
- 执行 eligible target rules。
- 将 adapter results 映射为现有 file statuses。
- 验证 safe relative paths 和 confidence。
- 持久化 run/result/file/chunk evidence。

### Adapter Registries

- 负责 default adapter resolution。
- 暴露 safe capability status。
- 只返回 masked configuration summaries。
- Capability responses 不暴露 raw command paths、endpoints、hostnames 或 credentials。

### Converter Adapter Runtime

- 将 Atlas converter requests 翻译为已批准的内部 conversion runtime contract。
- 生成 product-facing converter results。
- 返回 safe messages 前脱敏 runtime output。
- 可以在 adapter boundary 后使用 local process 或 worker topology。

### Parser Adapter Runtime

- 将 Atlas parser requests 翻译为已批准的内部 parser runtime contract。
- 生成 product-facing parser results 和 source chunk descriptors。
- 返回 safe messages 前验证或规范化 runtime manifest output。
- 可以在 adapter boundary 后使用 local process 或 worker topology。

### Artifact Evidence Boundary

- 通过安全相对路径存储或引用 generated PDFs、Markdown、assets。
- 拒绝 traversal、absolute、host-prefixed 或 URI-prefixed paths。
- 不暴露 raw storage root paths。

## 数据架构

| Entity | Ownership | Role |
|---|---|---|
| File item | Metadata control plane | 最新 product-visible file status 和 artifact path metadata。 |
| Conversion run/result | Metadata control plane | Conversion execution evidence 和 per-file status。 |
| Parser run/result | Metadata control plane | Parser execution evidence 和 per-file status。 |
| Source chunk | Metadata control plane | Parser-emitted trace evidence，供 review、Wiki、graph、Ask 使用。 |
| Runtime configuration | Server-side configuration | 选择 available runtime adapter behavior，不 raw 暴露。 |

## 集成架构

### Runtime Interaction Pattern

- **Default path:** CI 和 deterministic local verification 使用 mock/fake adapters。
- **Configured path:** 存在批准配置时 opt-in runtime adapter execution。
- **Failure path:** missing config、timeout、invalid output 或 adapter fault 映射为 safe failed/partial-failed evidence。

### Adapter Boundary Rules

- Runtime command/process/worker code 只允许出现在 adapter/runtime implementation scope 和 tests。
- Product layers 不直接 import runtime libraries、执行 commands 或调用 runtime endpoints。
- Runtime names 只在 seam guards 允许的 docs 和 adapter packages 中出现。

## 状态模型

```text
Capability:
  DISABLED -> MISCONFIGURED -> AVAILABLE

Run:
  REQUESTED -> RUNNING -> SUCCEEDED
                     \--> PARTIAL_FAILED
                     \--> FAILED
```

## Security, Reliability, And Observability

- Safe messages 在持久化前有界化和脱敏。
- Capability metadata 只 status-only 且 masked。
- 默认 verification 保持 mock-safe。
- Optional runtime smoke tests 在缺少 runtime config 时 self-skip。
- Run/result rows 仍是 troubleshooting 的主要 observable evidence。

## Risks And Tradeoffs

| ID | Risk / Tradeoff | Architectural response |
|---|---|---|
| R-ARCH-001 | Local process topology 后续可能变化。 | 将 process/worker concerns 保持在 adapter runtime executor 后面。 |
| R-ARCH-002 | Runtime manifests 可能包含 unsafe paths。 | 所有 paths 在持久化前由 product services 验证。 |
| R-ARCH-003 | Runtime logs 可能泄漏内部信息。 | 返回 safe messages 前截断并脱敏。 |
| R-ARCH-004 | 默认 CI 不能依赖内部 binaries。 | mock/fake path 保持默认；只添加 opt-in smoke tests。 |

## Open Questions

1. 第一版 configured implementation 使用 local process、sidecar worker，还是 internal worker endpoint？
2. `trinity-office` 和 `document-normalize` 的 exact approved command/manifest contracts。
3. Runtime execution 的 timeout、max-output 和 artifact materialization policy。

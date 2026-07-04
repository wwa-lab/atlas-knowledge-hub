# 架构：全栈产品化

## 概述

Atlas P0 产品化是分层 full-stack metadata 工作流。Vue 负责用户交互和状态展示；Spring Boot 负责产品 API、校验、持久化和编排；adapter 在后端边界之后负责 parser/converter/model/vector/storage/graph engine 执行。

## 架构图

```text
┌──────────────────────────────────────────────────────────────┐
│ Users                                                        │
│ Knowledge user · Delivery lead · SME reviewer                │
└─────────────────────────────┬────────────────────────────────┘
                              │ Browser
                              ▼
┌──────────────────────────────────────────────────────────────┐
│ Vue P0 App                                                   │
│ Space list · Batch/files · Review · Wiki · Graph · Ask       │
└─────────────────────────────┬────────────────────────────────┘
                              │ REST / JSON envelope
                              ▼
┌──────────────────────────────────────────────────────────────┐
│ Spring Boot Metadata API                                     │
│ Space · Batch/File · Review/Publish · Graph · Vector · Ask   │
├──────────────────────────────────────────────────────────────┤
│ Domain Services                                               │
│ Source trace · review status · publish eligibility · evidence │
├─────────────────────────────┬────────────────────────────────┤
│ Repositories / Flyway DB     │ Adapter Registries             │
│ PostgreSQL metadata          │ graph/vector/model/mock engines │
└─────────────────────────────┴────────────────────────────────┘
```

## 组件边界

| Component | Responsibility |
|---|---|
| Vue P0 app | 驱动浏览器工作流、调用 Atlas APIs、渲染 loading/empty/error/success 状态、禁用未接通 affordances。 |
| API client layer | 规范化 base URL、解包 envelope、分类安全错误、发送必要 graph headers。 |
| Space/Batch/File services | 持久化并暴露安全 metadata-only P0 upload inventory。 |
| Review/Publish services | 执行 review transitions 与 publish eligibility。 |
| Graph service | 通过 graph adapter boundary 投影并读取可信证据。 |
| Vector service | 通过 vector adapter boundary 索引/查询 approved source chunks。 |
| Ask service | 查询 vector evidence，并通过 model adapter boundary 生成 answer。 |

## 架构约束

- 前端只调用 Atlas 后端 endpoints，绝不调用 engines 或 providers。
- 后端 controller/service/repository 分层保持不变。
- Sample upload 仅 metadata-only；无文件字节、生产存储或真实文档。
- P0 graph/vector refresh 可同步执行，因为当前后端 endpoint 是同步且 mock-safe 的。
- 生产认证/RBAC 不在范围内；graph header guard 继续作为现有本地加固模式。

## 架构评审说明

- 本切片组合现有已实现 endpoints，而不是增加平行 mock backend。
- 唯一可接受后端新增是实现中发现的最小 API 阻塞缺口。
- 前端 fallback mock 行为不得伪装为已连接产品行为。

# 数据流：知识图谱

## 概述

本流程描述已批准 Atlas 知识如何成为图谱数据，以及用户如何在不暴露未审核或不安全来源材料的前提下检查图谱。

## 投影流程

1. 读取某知识空间的已批准/已发布 Wiki pages 与 source chunks。
2. 校验 eligible 条件：review status、source trace 是否存在、confidence policy、安全相对来源引用。
3. 将符合条件的 descriptors 传入 graph projection boundary。
4. 创建或刷新稳定 graph nodes 与 evidence-backed edges。
5. 持久化 projection run summary、item outcomes、graph records 和 audit evidence。
6. 用安全 reason codes 排除 unsafe/unapproved records。

## 查询流程

1. 前端带 type/search/review/evidence filters 请求 graph view。
2. 后端校验访问权、pagination/limits 和 filter values。
3. Query service 加载有界 nodes/edges 与 counts。
4. 响应将 graph records 映射为带 evidence summaries 的安全 DTO。
5. 前端渲染 graph、empty/error/unauthorized states 和 selected detail。

## 审核流程

1. SME 选择一条带证据的 graph edge。
2. SME 提交 review action 与 comment。
3. 后端校验 authorization、target edge、action 和 comment safety。
4. 更新 Graph edge review status。
5. 写入 append-only review/audit record。

## 状态转换

```text
ProjectionRun: REQUESTED -> RUNNING -> SUCCEEDED
ProjectionRun: REQUESTED -> RUNNING -> PARTIAL_FAILED
ProjectionRun: REQUESTED -> RUNNING -> FAILED

GraphEdge: REVIEW_REQUIRED -> APPROVED
GraphEdge: REVIEW_REQUIRED -> NEED_FIX
GraphEdge: REVIEW_REQUIRED -> OCR_REQUIRED
GraphEdge: APPROVED -> PUBLISHED
```

## 错误与排除代码

| Code | 含义 |
|---|---|
| `UNAPPROVED_SOURCE` | 来源不是 approved/published。 |
| `MISSING_SOURCE_TRACE` | 缺少必要 trace reference。 |
| `LOW_CONFIDENCE_UNAPPROVED` | 置信度低且没有 SME approval。 |
| `UNSAFE_REFERENCE` | 路径/引用违反安全规则。 |
| `NO_EVIDENCE` | Candidate edge 缺少 source evidence。 |
| `ADAPTER_MISCONFIGURED` | Projection boundary 未安全配置。 |

## 验证钩子

- eligibility 与 exclusion logic 单元测试。
- projection run persistence 与 item outcomes 集成测试。
- graph view/detail/review action API 测试。
- Graph tab 可检查证据行为 E2E 测试。

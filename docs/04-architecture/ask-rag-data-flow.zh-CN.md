# 数据流：Ask RAG

## 状态

草稿。`docs/04-architecture/ask-rag-architecture.md` 的配套文档。

## 主 Ask 流程

```text
User submits question
  -> Frontend validates non-empty question
  -> POST /api/spaces/{spaceId}/ask
  -> Ask API validates scope, policy, limit, filters
  -> Ask service creates REQUESTED run
  -> Vector query returns bounded evidence
  -> If no approved evidence under policy: NO_EVIDENCE response
  -> If evidence exists: model run generates mock answer from references
  -> Ask service persists answer evidence and final status
  -> API returns answer payload
  -> Frontend renders answer, confidence, evidence, review warning
```

## Review Policy 分支

| Policy | Evidence 行为 | Answer 行为 |
|---|---|---|
| `APPROVED_ONLY` | 只能使用 `APPROVED` 或 `PUBLISHED` evidence。 | 如果没有 evidence，返回 no-evidence 并跳过 model generation。 |
| `INCLUDE_REVIEW_REQUIRED` | 可返回 approved 与 review-required evidence，但必须明确标记。 | 生成回答仍为 `REVIEW_REQUIRED`，UI 展示 warning。 |

## 失败流程

```text
Invalid request
  -> VALIDATION_ERROR envelope
  -> no adapter execution

Unknown space
  -> NOT_FOUND envelope
  -> no adapter execution

Vector adapter unavailable
  -> FAILED Ask run with safe error
  -> no model generation

Model adapter unavailable after evidence
  -> FAILED or PARTIAL_FAILED Ask run
  -> evidence retained, safe no-answer shown
```

## 数据保留规则

- 本流程中 source chunks 只读。
- 不修改 file item、Wiki page、graph node、graph edge 和 review state。
- Ask run record 可引用 model run 与 vector evidence id，但不复制 raw source text。
- Safe question 与 answer summary 有长度边界。

## 验证触点

| Flow | Verification |
|---|---|
| Approved evidence success | Backend integration 与 frontend E2E 断言 answer 和 evidence。 |
| No approved evidence | Unit、API contract 和 E2E 断言无 model generation 且显示 no-answer state。 |
| Review-required inclusion | Unit/API/UI tests 断言 warning 与 separated evidence。 |
| Adapter failure | Unit/API tests 断言 sanitized safe message。 |
| Data immutability | Integration tests 断言 source/Wiki/graph/review records 不变。 |

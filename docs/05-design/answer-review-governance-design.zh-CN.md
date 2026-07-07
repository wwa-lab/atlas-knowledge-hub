# 设计：Answer Review Governance

最后更新：2026-07-07

## 设计范围

使用 additive backend、API 和 frontend changes，为既有 Trusted Ask runs 实现 answer governance。本设计不添加 production workflow queues、notification、legal sign-off、provider changes 或 answer reuse indexing。

## 后端设计

| Module | Design |
|---|---|
| `AnswerReviewStatus` enum | Ask answer governance 专用 enum。 |
| `AskRun` | 保存 status、reason、reviewer 和 reviewed timestamp；生成答案默认 `REVIEW_REQUIRED`。 |
| `ReviewAskAnswerRequest` | 包含 `status`、`reviewer`、`reason` 的 request DTO。 |
| `AskService.reviewAnswer` | 校验 safe text、校验 run 属于 space、拒绝无效 approval、更新 governance fields。 |
| `AskMapper` | 添加 reviewer-safe metadata、`answerReviewLabel` 和 `answerReusable`。 |
| Flyway | 添加 columns 和 status constraint。 |

## API 设计

- `POST /api/spaces/{spaceId}/ask/{runId}/review-actions`
- Request body:

```json
{
  "status": "APPROVED",
  "reviewer": "sme.alex",
  "reason": "Answer is supported by approved evidence."
}
```

- Response：既有 `AskRunResponse` 加 governance fields。
- Validation errors 使用既有 `ApiEnvelope` safe error shape。

## 前端设计

| Area | Design |
|---|---|
| Types | 添加 `ApiAnswerReviewStatus`；`ApiReviewStatus` 继续用于 evidence/document state。 |
| API client | 添加 `reviewAskAnswer(spaceId, runId, payload)`。 |
| Trusted Ask answer computed state | 从 answer governance fields 派生 label/reuse hint/reason。 |
| Product Ask panel | 显示 answer status、governance label、reviewer-safe reason、reusable hint 和 citations。 |
| Space Ask tab | 对 API-backed Ask results 显示同样 governance hint。 |

## 校验和错误处理

- `status` 必填。
- `reviewer` 必填且安全。
- `REJECTED` 和 `NEEDS_REVISION` 必须提供 `reason`。
- Approval 需要 terminal `SUCCEEDED` 或 `PARTIAL_FAILED`、answer text 和 evidence。
- 不安全文本以 `VALIDATION_FAILED` 和安全 field messages 失败。

## 测试设计

- Backend domain test：generated answer 默认 review-required；review transitions 持久化 metadata。
- Backend service test：invalid approvals 和 unsafe reasons 在持久化前失败。
- Backend integration test：review API 更新 status、保留 citations、返回安全字段。
- Frontend tests：Trusted Ask 显示 governance label/reason/reuse hint；approved 和 rejected states 可区分。

## 残留风险

Future answer reuse 和 retrieval quality metrics 有意延后；本切片只创建后续切片所需的安全治理 contract。

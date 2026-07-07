# 需求：Answer Review Governance

最后更新：2026-07-07
Slice：`answer-review-governance`
Wave：Wave 4 / Ask And Graph Productization
状态：已按本 goal 预授权接受，可进入实现

## 目标

Trusted Ask 答案必须暴露专用治理状态，让用户区分模型生成草稿、已审核答案、被拒绝答案和需要修订的答案。本切片必须保留 Ask evidence、source trace、confidence 和既有 review/publish 行为，并保持 mock/sample-safe。

## 范围

- 定义 Ask answer 专用状态模型：`REVIEW_REQUIRED`、`APPROVED`、`REJECTED`、`NEEDS_REVISION`。
- 持久化并暴露 review metadata：reviewer、reason、reviewed timestamp、reusable knowledge eligibility 和 reviewer-safe display fields。
- 添加创建和读取 answer governance state 的稳定 API contract。
- 更新 Trusted Ask UI，显示治理状态，并避免暗示未审核答案可信。
- 添加 backend unit tests、backend API contract tests、frontend tests 和 traceability evidence。

## 范围外

- `ask-session-citations`、retrieval quality metrics、graph extraction、provider/model adapter 策略变化、production approval workflow、legal/compliance sign-off、notification、assignment queue、SLA、真实公司数据和外部 cloud/provider 调用。
- 将 `REJECTED`、`REVIEW_REQUIRED`、low-confidence、missing-source-trace 或 `NEEDS_REVISION` answer 当作 approved reusable knowledge。
- 改变 production auth/RBAC/audit/secret/rate-limit 语义。

## 需求

| ID | Requirement | Priority | Acceptance |
|---|---|---|---|
| REQ-ANSWER-REVIEW-GOVERNANCE-001 | Ask answers 必须携带独立于 source document 和 evidence review status 的 answer governance status。 | Must | API responses 能区分 `REVIEW_REQUIRED`、`APPROVED`、`REJECTED` 和 `NEEDS_REVISION`。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-002 | 新生成的模型答案必须默认 `REVIEW_REQUIRED`。 | Must | 创建 Ask run 时，即使生成成功，答案也返回 review-required。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-003 | Review actions 必须持久化 reviewer、reason、reviewed timestamp 和 safe reuse eligibility。 | Must | Review API response 和 read API response 只暴露安全 metadata。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-004 | 只有 approved 且具备合格证据的答案才能标记为 reusable knowledge。 | Must | Rejected、review-required、needs-revision、no-evidence、missing-answer 或 missing-evidence answer 都不是 reusable。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-005 | Answer governance 必须保留 Ask evidence、source trace、confidence 和 model run metadata。 | Must | Review update 不修改 evidence rows，也不隐藏 citations。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-006 | API 和 UI 字段必须 reviewer-safe。 | Must | Responses 不暴露 raw secrets、private paths、raw provider payloads、raw stack traces、internal endpoints 或 raw source documents。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-007 | Trusted Ask UI 必须清楚说明答案是已审核、被拒绝、需修订还是 review-required。 | Must | UI tests 验证 governance label、reason、reuse hint 和 citations。 |
| REQ-ANSWER-REVIEW-GOVERNANCE-008 | 既有 Ask 和 citation behavior 不得回归。 | Must | 既有 Ask tests 和 API-backed frontend tests 继续通过。 |

## 假设

- 既有 prototype auth path policy 仍是权限边界；本切片不新增 production RBAC 语义。
- `NEEDS_REVISION` 是 Ask answer 治理术语，不替代文档 review 的 `NEED_FIX` 状态。
- Review reasons 作为用户安全文本保存，并在 service boundary 进行校验和清洗。

## 验证

- `cd backend && mvn verify`
- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `npm run agent:check-sdd -- --slice answer-review-governance`
- `npm run agent:closeout`
- `git diff --check`

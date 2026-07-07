# 用户故事：Answer Review Governance

最后更新：2026-07-07

## US-ANSWER-REVIEW-GOVERNANCE-001：查看答案可信状态

作为 Trusted Ask 用户，  
我想看到每个答案是 review-required、approved、rejected 还是 needs revision，  
以免把模型生成草稿误认为可信可复用知识。

### 验收标准

1. Given 一个生成的 Ask answer，when 我读取 response，then 在 reviewer 修改前它的状态为 `REVIEW_REQUIRED`。
2. Given 一个已审核答案，when 我读取 response，then response 包含 reviewer-safe metadata 和清晰 trust label。
3. Given 一个非 approved answer，when 我在 UI 查看，then UI 提醒它不是 approved reusable knowledge。

## US-ANSWER-REVIEW-GOVERNANCE-002：安全审核答案

作为 SME reviewer，  
我想用原因将 Ask answer 标记为 approved、rejected 或 needs revision，  
以便下游用户理解该答案是否以及为什么可以复用。

### 验收标准

1. Given 一个已有 Ask run，when 我提交有效 review status 和 reason，then status 和 metadata 被持久化。
2. Given 对缺少 answer text 或 evidence 的答案执行 approval，when 我提交 review，then 系统安全拒绝该 approval。
3. Given 不安全的 reviewer 或 reason 文本，when 我提交 review，then request 在持久化前失败且不泄露 raw input。

## US-ANSWER-REVIEW-GOVERNANCE-003：保留证据和来源追溯

作为 delivery lead，  
我想 answer governance updates 保留 citations、source trace、confidence 和 model run metadata，  
这样答案审核不会抹掉信任判断所需证据。

### 验收标准

1. Given 一个已审核 answer，when 我获取 Ask run，then citations 和 evidence review statuses 仍存在。
2. Given 一个 rejected 或 needs-revision answer，when 我检查它，then evidence 仍可见但 answer 不可复用。
3. Given 既有 Ask behavior，when 本切片实现后，then retrieval 和 citation display 继续工作。

## US-ANSWER-REVIEW-GOVERNANCE-004：避免不安全复用

作为 knowledge governance owner，  
我想只有 approved answer governance states 可复用，  
以避免未审核或被拒绝的生成内容成为可信知识。

### 验收标准

1. Given `REVIEW_REQUIRED`、`REJECTED` 或 `NEEDS_REVISION`，when API 返回 answer，then `answerReusable` 为 false。
2. Given `APPROVED` 且有 answer text 和 eligible evidence，when API 返回 answer，then `answerReusable` 为 true。
3. Given no-evidence 或 failed Ask runs，when 被审核，then 它们不能被标记为 approved reusable knowledge。

## 依赖

- 既有 Ask run persistence 和 API。
- 既有 vector/model adapter boundaries。
- 既有 Trusted Ask frontend surface。

## 范围外

Production assignment queues、notifications、legal approval、`ask-session-citations`、retrieval metrics、graph extraction 和 provider strategy changes。

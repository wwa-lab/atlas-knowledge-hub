# 用户故事：Ask Session Citations

## 用户故事 1

**ID:** US-ASK-SESSION-CITATIONS-001  
**标题:** 创建并延续 Ask session

**故事：**  
作为知识用户，  
我希望每次 Trusted Ask 问题都归属于可见 session，  
以便回看一组相关答案及其证据。

## 验收标准

1. **Given** 我在没有 `sessionId` 的情况下提问  
   **When** Ask API 接受请求  
   **Then** 系统为该 Knowledge Space 创建 session，并返回 session id。
2. **Given** 我使用已有 session id 再提问  
   **When** 该 session 属于同一 Knowledge Space  
   **Then** 新答案被附加到该 session。
3. **Given** session id 属于其他 Knowledge Space  
   **When** 我在 Ask 请求中使用它  
   **Then** API 返回安全 validation error。

## 说明 / 假设

- Session 创建保持本地和确定性。

## 依赖

- 现有 `ask-rag` Ask create/read API。

## 范围外

- 协作、分享、权限和生产 session retention policy。

## Open Questions

- None。

---

## 用户故事 2

**ID:** US-ASK-SESSION-CITATIONS-002  
**标题:** 检查 answer citation snapshots

**故事：**  
作为 reviewer 或知识用户，  
我希望每个 answer citation 展示 source trace、confidence 和 review status，  
以便判断答案是否可依赖。

## 验收标准

1. **Given** 一个 answer 有 eligible evidence  
   **When** 我读取 Ask run 或 session  
   **Then** 每个 citation 包含 safe label、source chunk、file id、page/section locator、confidence、review status 和 score。
2. **Given** evidence 为 review-required 且被显式包含  
   **When** citation 返回  
   **Then** 它被标记为 `REVIEW_REQUIRED`，且 `reviewEligible=false`。
3. **Given** citation source 包含 unsafe text  
   **When** 创建 citation snapshot  
   **Then** unsafe raw values 被拒绝或脱敏为 safe labels。

## 说明 / 假设

- Citation snapshot 扩展现有 `AskEvidence` records。

## 依赖

- 现有 vector query result 字段与 review-aware retrieval policy。

## 范围外

- 存储 raw source snippets 或 provider payloads。

## Open Questions

- None。

---

## 用户故事 3

**ID:** US-ASK-SESSION-CITATIONS-003  
**标题:** 在 UI 中查看 session history

**故事：**  
作为知识用户，  
我希望 Trusted Ask 页面展示 session history 和 citation detail，  
以便在不丢失 evidence context 的情况下回看历史答案。

## 验收标准

1. **Given** 某个 Knowledge Space 有 recent sessions  
   **When** 我打开 Trusted Ask  
   **Then** 我可以看到包含 title、answer count、latest status 和 time 的 session summaries。
2. **Given** 我选择一个 session  
   **When** session detail 加载  
   **Then** 我可以检查有序 answers 和 citations。
3. **Given** session APIs 安全失败  
   **When** UI 捕获错误  
   **Then** 它展示安全 fallback message，不暴露内部细节。

## 说明 / 假设

- UI 与 mock E2E fixtures 保持兼容。

## 依赖

- Session list/detail APIs 与 frontend API client。

## 范围外

- 完整 conversation composer 重设计。

## Open Questions

- None。

---

## 用户故事 4

**ID:** US-ASK-SESSION-CITATIONS-004  
**标题:** 保持现有 Ask 行为

**故事：**  
作为 Atlas 维护者，  
我希望现有 Ask 测试和流程继续工作，  
以便 session citations 不回归当前产品行为。

## 验收标准

1. **Given** 现有代码发送没有 session 字段的 Ask request  
   **When** 它运行在更新后的 API 上  
   **Then** 请求仍然成功。
2. **Given** 现有 UI 从 Ask run 读取 `evidence`  
   **When** 增加更丰富 citation fields  
   **Then** 现有字段仍存在且类型稳定。
3. **Given** backend verification 运行  
   **When** contract tests 执行  
   **Then** legacy create/read 与新的 session read 行为都通过。

## 说明 / 假设

- 优先添加字段，而不是重命名现有 response fields。

## 依赖

- 现有 AskService 与 frontend API surfaces。

## 范围外

- 移除或版本化现有 Ask endpoints。

## Open Questions

- None。

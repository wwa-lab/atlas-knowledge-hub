# 架构：Answer Review Governance

最后更新：2026-07-07

## 概览

本切片在既有 Trusted Ask 分层路径上扩展 Ask-answer governance state。它是 Spring Boot metadata API 和 Vue product shell 内的模块化单体变更；parser、converter、vector、model、storage 和 search adapters 保持不变。

## 架构驱动

- Additive Ask API contract 和 persistence fields。
- Ask answer status 不能替代 evidence/document review status。
- Safe review metadata 必须在持久化前校验。
- 既有 auth path policy 和 audit 语义保持不变。

## 系统上下文

```text
┌────────────────────────────────────────────┐
│ Trusted Ask Users / SME Reviewers          │
└──────────────────────┬─────────────────────┘
                       │ Vue UI
                       ▼
┌────────────────────────────────────────────┐
│ Trusted Ask View                           │
│ answer status · reason · reuse hint         │
└──────────────────────┬─────────────────────┘
                       │ REST / JSON envelope
                       ▼
┌────────────────────────────────────────────┐
│ AskController                              │
│ create/read Ask · review answer action      │
├────────────────────────────────────────────┤
│ AskService                                 │
│ validation · state transition · mapping     │
├────────────────────────────────────────────┤
│ AskRunRepository · AskEvidenceRepository   │
└──────────────────────┬─────────────────────┘
                       │ JPA / Flyway
                       ▼
┌────────────────────────────────────────────┐
│ atlas.ask_run · atlas.ask_evidence         │
└────────────────────────────────────────────┘
```

## 组件

| Component | Responsibility |
|---|---|
| Frontend Trusted Ask panel | 显示 governance label、reason、reusable hint 和 citations。 |
| Frontend API client | 调用既有 Ask create/read endpoints 和新的 review-action endpoint。 |
| `AskController` | 暴露 review action，不让 UI 耦合 persistence。 |
| `AskService` | 校验 review requests、执行 state transitions、计算 reuse eligibility 并保留 evidence。 |
| `AskRun` | 持有 answer governance state 和 metadata。 |
| `AskMapper` | 将 domain objects 映射为 additive safe response fields。 |
| Flyway migration | 添加 governance metadata columns 和 answer-specific status check。 |

## 状态模型

```text
REVIEW_REQUIRED
  ├─ approve with eligible answer/evidence ─▶ APPROVED
  ├─ reject with reason ───────────────────▶ REJECTED
  └─ request revision with reason ─────────▶ NEEDS_REVISION

APPROVED / REJECTED / NEEDS_REVISION 可被后续 reviewer action 调整，
但只有带 answer text 和 eligible evidence 的 APPROVED 才可复用。
```

## 安全与数据边界

- Review text 使用既有 Ask request validation 风格进行安全校验。
- API responses 只暴露 safe status/reason metadata。
- 不新增 provider payloads、raw source documents、private paths、internal endpoints 或 secrets。
- 不引入 external cloud calls 或 adapter strategy changes。

## 风险与权衡

| Risk | Mitigation |
|---|---|
| Answer governance 与 evidence review status 混淆 | 使用专用 `AnswerReviewStatus`，evidence 继续使用 `ReviewStatus`。 |
| 批准 no-evidence answers | Service validation 拒绝无 terminal successful status、answer text 或 evidence 的 approval。 |
| Future answer reuse 语义尚未实现 | 只暴露 boolean eligibility；本切片不创建 reuse index。 |

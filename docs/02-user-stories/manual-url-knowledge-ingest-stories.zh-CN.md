# 用户故事：manual-url-knowledge-ingest

## Story Map

### `US-MANUAL-URL-KNOWLEDGE-INGEST-001` 登记 Manual URL Source

作为 Knowledge Space contributor，我希望手动登记一个 public URL，以便 Atlas 在不启动不可控 crawl 的情况下把它作为潜在知识来源追踪。

**验收标准**

1. Given 一个 Knowledge Space 与安全 HTTPS sample URL，when 我提交 manual URL form，then Atlas 创建 URL source record 并满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-001`。
2. Given 带 credentials、unsafe scheme、private host、query string 或 fragment 的 URL，when 我提交它，then Atlas 以 `AC-MANUAL-URL-KNOWLEDGE-INGEST-004` 的安全错误拒绝。
3. Given 已登记 URL source，when 我查看 source list，then 我能看到 safe display URL、host、status、review status 与 source trace，并满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-002`。

**依赖：** 现有 Knowledge Space API、safe error envelope、auth guard、rate-limit guard。
**范围外：** 真实 URL fetch、connector sync、crawl、browser automation。

### `US-MANUAL-URL-KNOWLEDGE-INGEST-002` 保留 Trace 与 Review Governance

作为 SME reviewer，我希望 URL-derived metadata 保持 review-required 且可追踪，以便未经验证的 URL material 不会成为 trusted knowledge。

**验收标准**

1. Given 已登记 URL source，when Atlas 创建 supporting batch/file/chunk metadata，then 这些 artifacts 按 `AC-MANUAL-URL-KNOWLEDGE-INGEST-003` 保留 URL source trace。
2. Given 任意 URL-derived artifact，when 它进入 Processing Center，then review status 保持 `REVIEW_REQUIRED`。
3. Given Wiki/Ask/Graph flows，when URL-derived metadata 尚未 approved，then 它不会作为 approved downstream knowledge 暴露。

**依赖：** 现有 batch、file item、source chunk、review queue、Wiki ingest 与 publish contracts。
**范围外：** 新 review state machine 或 production legal/compliance review。

### `US-MANUAL-URL-KNOWLEDGE-INGEST-003` 展示安全 URL Ingest Status

作为 Knowledge Space operator，我希望在产品界面看到 manual URL ingest status，以便判断是否有待审核工作。

**验收标准**

1. Given 前端加载 Knowledge Space，when 存在 manual URL sources，then UI 展示其 ingest status、review status、eligibility 与 source trace，并满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-005`。
2. Given API 不可用，when 前端回退 mock data，then 不会伪造 approved URL content。
3. Given unsafe URL validation 失败，when UI 显示反馈，then 使用安全错误文案，不展示 raw secret 或 private endpoint 值。

**依赖：** 现有 `frontend/src/api.ts`、`frontend/src/types.ts`、product shell state 与 E2E fixture patterns。
**范围外：** 样式大改、新导航模型、production connector admin。

### `US-MANUAL-URL-KNOWLEDGE-INGEST-004` 验证无回归

作为 Atlas maintainer，我希望有聚焦的 backend、frontend 与 E2E tests，以便 URL source registration 不回归现有 ingest 与 review flows。

**验收标准**

1. Given 后端测试运行，when URL validation 与 registration paths 被覆盖，then validation、redaction、source trace、status mapping 与 review-required behavior 满足 `AC-MANUAL-URL-KNOWLEDGE-INGEST-006`。
2. Given 前端测试运行，when manual URL state 渲染，then status 与 trace 可见且不暴露 unsafe values。
3. Given 完整验证运行，when 现有命令完成，then file/folder ingest、Wiki、Ask、Graph 与 review/publish flows 不回归。

**依赖：** Maven、Vite/Vitest、Playwright fixtures、workflow closeout gate。
**范围外：** Provider-backed tests 或真实 network calls。

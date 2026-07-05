# 任务：Knowledge Space

## 状态

Phase 1 前端历史任务 `T-KS-001` 至 `T-KS-031` 已完成。此中文 companion 补齐双语任务视图，并新增 Product Goal Batch 1 的 Phase A/B 收口任务。

## 实现门禁

- `docs/03-spec/knowledge-space-spec.md` 与 `.zh-CN.md` 是行为真相源。
- `docs/05-design/knowledge-space-design.md` 与 `.zh-CN.md` 是设计契约。
- 当前 Batch 1 只允许真实 Vue 产品页改动，不把 iframe、静态原型、workbench 或 debug panel 当主路径验收。
- 只使用 mock/sample 数据。
- 不新增外部网络调用、真实 provider 调用、真实公司数据、真实密钥或私有路径。

## 历史任务摘要

| ID | 状态 | 中文说明 |
|---|---|---|
| T-KS-001→T-KS-027 | Complete | 静态原型阶段覆盖知识空间、文档、Wiki、图谱、审核、Ask、设置、响应式和安全扫描。 |
| T-KS-028 | Complete | 启动 Vue 3 + Vite + TypeScript 前端壳，使用 mock 数据。 |
| T-KS-029 | Complete | 建立前端 typecheck、build、unit/component、E2E 验证基线。 |
| T-KS-030 | Complete | 对齐当前 FE baseline 与静态原型镜像。 |
| T-KS-031 | Complete | 对齐 Global Chat、Documents、Processing Center、Wiki、Graph IA。 |

## Batch 1 新增任务

| ID | Task | Depends On | Verification |
|---|---|---|---|
| T-KS-032 | 收口 Phase A：确认真实 Vue 应用默认进入产品页，首页、侧边栏、Global Chat、设置浮层、知识空间卡片可见可交互，主路径无 `iframe.product-frame`。 | T-KS-028, T-KS-031 | `npm run typecheck`; `npm run test`; `npm run build`; Playwright Phase A/B test; `git diff --check`; secret/network scan. |
| T-KS-033 | 收口 Phase B：确认点击 `IBM i Modernization` 进入真实 Vue 详情页，默认 Wiki，Documents、Processing Center、Wiki、Graph 四个标签均可切换并展示真实 Vue 内容。 | T-KS-032 | Playwright Phase A/B test generates `phase-b-space-detail.png`; screenshot/manual review confirms no iframe/workbench dependency. |
| T-KS-034 | 更新 Product Goal Batch 1 状态：roadmap、traceability、slice roadmap、progress ledger 与 E2E/screenshot evidence 对齐。 | T-KS-032, T-KS-033 | 文档 diff 检查；progress ledger 记录 Batch 1 checkpoint。 |
| T-KS-035 | 添加消息管理设置页：展示消息索引开关、Embedding 模型依赖、未配置统计空态、启用后的 mock-safe 索引统计，以及不执行真实 provider/vector 写入的边界说明。 | T-KS-032 | `npm run test`; Playwright Phase H settings test; `npm run typecheck`; `git diff --check`; secret/network scan. |
| T-KS-036 | 添加空间信息设置页：展示当前空间 ID、名称、描述、状态、创建时间、存储配额、已使用存储和使用率，并支持名称/描述的本地 mock 编辑。 | T-KS-026, T-KS-032 | `npm run test`; Playwright Phase H settings test; `npm run typecheck`; `git diff --check`; secret/network scan. |

## 测试计划

- `cd frontend && npm run typecheck`
- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts --project=chromium`
- `cd frontend && npx playwright test tests/e2e/phase-h-settings-administration.spec.ts --project=chromium`
- Phase H settings test 覆盖 General、User Info、Space Information、Member Management、Message Management、API Info 和 Model Management。
- `git diff --check`
- 扫描新增网络调用、依赖、secret、私有路径和真实公司数据。

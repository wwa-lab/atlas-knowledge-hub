# Atlas Knowledge Hub 新手入门指南

这份指南面向第一次接触 Atlas Knowledge Hub 的开发者、产品同学或测试同学。目标是让你知道这个项目在做什么、先看哪些文件、怎么本地跑起来、怎么验证，以及改代码时最容易踩哪些边界。

## 1. 先理解这个项目

Atlas Knowledge Hub 是一个内部知识产品，不是单个文档转换脚本。

它的核心闭环是：

```text
上传文件夹或 ZIP
  -> 创建批次
  -> 转换 Office/PDF
  -> 解析成 Markdown 和图片
  -> 保留 source trace、confidence、review status
  -> SME 审核
  -> 发布到 Wiki
  -> 生成知识图谱
  -> 基于已审核知识进行 Ask/RAG
```

第一原则：

- 先让知识可信，再让知识可问。
- 只有经过审核或确定性校验的内容，才应该进入 Wiki、Graph、Ask。
- 所有 parser、converter、storage、vector、model 能力都必须走 adapter，不要在产品代码里直连具体工具。

## 2. 第一次打开仓库先看什么

建议按这个顺序读：

1. `README.md`：项目是什么、怎么跑原型。
2. `PROJECT_RULES.md`：项目边界、SDD 规则、安全规则。
3. `DEVELOPMENT_STANDARDS.md`：不同阶段的工程标准。
4. `docs/product-vision.md`：产品愿景。
5. `docs/mvp-scope.md`：MVP 包含什么、不包含什么。
6. `docs/00-context/slice-roadmap.md`：Phase 1-4 的 slice 路线图。
7. `docs/markdown-standard.md`：Atlas Markdown 标准。
8. `docs/knowledge-graph-design.md`：轻量知识图谱设计。
9. `docs/07-acceptance/knowledge-loop-e2e.md`：自动化验收闭环。

如果你只想快速上手，先读 `README.md`、`PROJECT_RULES.md`、`docs/00-context/slice-roadmap.md` 和这份指南。需要按命令把项目跑起来时，看 `docs/local-runbook.md`；如果你在 VS Code 里操作，直接看中文版 `docs/local-runbook.zh-CN.md`。

## 3. 本地环境准备

更完整的本地运行步骤见 `docs/local-runbook.md`。VS Code 中文操作手册见 `docs/local-runbook.zh-CN.md`。

仓库根目录：

```bash
npm run setup
```

这会安装 frontend 依赖。

如果只想看静态原型：

```bash
open prototypes/index.html
```

如果想跑 Vue 前端：

```bash
cd frontend
npm run dev
```

默认地址：

```text
http://127.0.0.1:5173
```

## 4. 一键自动验收

外部开发和普通演示默认使用 mock mode，不需要真实 API key、公司环境、数据库或外部服务：

```bash
npm run e2e:loop:mock
```

这个命令会做这些事：

1. 检查 E2E 配置。
2. 清理本地 sample 输出。
3. 准备 mock sample 输入目录。
4. 构建 frontend。
5. 跑 Playwright acceptance 测试。
6. 生成 HTML/JUnit 报告。

报告位置：

```text
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

公司或本地集成环境可以从模板开始：

```bash
cp configs/atlas.company.example.env .env
```

填好配置后运行：

```bash
npm run e2e:loop:configured
```

注意：`configured` 模式是给真实 backend/database/model adapter 接入后用的。当前如果缺少必要环境变量，会先失败在配置检查，而不是假装通过。

## 5. 常用命令

根目录：

```bash
npm run setup
npm run e2e:loop:mock
npm run e2e:loop:configured
```

Frontend：

```bash
cd frontend
npm run lint
npm run typecheck
npm run test
npm run build
npm run e2e
npm run e2e:acceptance
```

Git diff 基础检查：

```bash
git diff --check
```

## 6. 目录怎么理解

```text
prototypes/
  静态 HTML 原型。Phase 0/早期产品体验基线。

frontend/
  Vue 3 + Vite + TypeScript 前端。Phase 1 起逐步承接原型。

backend/
  Java + Spring Boot 后端。Phase 2 起承接 metadata API、DB、adapter control plane。

docs/
  产品、架构、SDD、任务、验收、标准文档。

docs/00-context/
  SDD profile、slice roadmap、traceability、lessons learned。

docs/01-requirements/ 到 docs/06-tasks/
  每个 slice 的 SDD 文档链。

docs/07-acceptance/
  自动化验收闭环说明。

configs/
  mock/configured 配置模板。不要提交真实密钥。

samples/input/
  mock/sample 输入。不要放真实公司资料。

samples/output/
  本地生成输出。默认不提交。

scripts/e2e/
  一键验收脚本。
```

## 7. Phase 1-4 怎么理解

### Phase 1：前端产品壳

目标：

- Vue 前端复现已接受的 Knowledge Space 体验。
- 支持 mock 上传、批次、文档、Wiki、Graph、Review、Ask 表面。
- 不接真实 backend、database、model、storage。

验证重点：

- `npm run build`
- `npm run e2e`
- `npm run e2e:loop:mock`

### Phase 2：Metadata API 和持久化

目标：

- Spring Boot metadata API。
- PostgreSQL + Flyway。
- Knowledge Space、batch、file、page、chunk、review 等核心 metadata。

原则：

- API 行为先写进 SDD 和 API implementation guide。
- 输入要 validation。
- 错误响应不能泄露内部路径、密钥或 stack trace。

### Phase 3：Adapter 层

目标：

- converter adapter
- parser adapter
- storage adapter
- vector adapter
- model adapter

原则：

- 产品逻辑只依赖 Atlas adapter interface。
- 不要把 DeepSeek、Copilot、pgvector、S3、document-normalize 等具体实现写死到产品层。
- mock engine 先跑通 contract tests，再接 configured mode。

### Phase 4：可信知识闭环

目标：

- SME review/publish。
- Approved Markdown 发布到 Wiki。
- 从 approved content 生成可解释 graph。
- Ask/RAG 只基于 approved knowledge，答案必须带 citation。

最终验收目标：

```text
sample package
  -> Markdown
  -> Review
  -> Wiki
  -> Graph
  -> Ask with selected knowledge space and model
  -> cited answer
```

## 8. 做需求前的工作流

新功能或跨文件改动不要直接开写。先确认：

1. 当前 slice 是什么。
2. 对应 spec 在 `docs/03-spec/` 是否存在。
3. 对应 tasks 在 `docs/06-tasks/` 是否存在。
4. 行为是否已经被 SDD 接受。
5. 是否需要中英文 SDD 同步。

实现时按这个顺序：

1. 读 `PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、相关 SDD。
2. 找到 `docs/06-tasks/{slice}-tasks.md`。
3. 按 task ID 顺序实现。
4. 如果实现会偏离 spec，先停下来更新或报告 SDD mismatch。
5. 跑对应验证命令。
6. 更新 traceability 或 roadmap 状态，如果 slice 状态发生变化。

## 9. 安全和数据红线

不要提交：

- 真实公司文档。
- 真实截图。
- 客户数据。
- API key、password、token。
- 内部 endpoint、hostname、私有路径。
- 运行日志或导出文件。

允许提交：

- mock data。
- sample data。
- masked config。
- `.example.env`。
- 不含真实内容的 README 或 schema。

模型配置规则：

- API key 只放 `.env` 或公司批准的 secret store。
- 前端不能出现真实 API key。
- API response 只能显示 masked/status-only config。

## 10. 新手第一天建议路线

第一步，看产品：

```bash
open prototypes/index.html
```

第二步，跑自动验收：

```bash
npm run setup
npm run e2e:loop:mock
```

第三步，看 Phase 路线：

```text
docs/00-context/slice-roadmap.md
```

第四步，看核心知识资产标准：

```text
docs/markdown-standard.md
docs/knowledge-graph-design.md
```

第五步，如果要改 frontend：

```bash
cd frontend
npm run dev
npm run test
npm run build
```

第六步，如果要做 backend/adapter：

先找到对应 slice 的 spec、design、API guide、tasks，再开始改代码。

## 11. 常见问题

### 我可以直接接真实 DeepSeek API 吗？

不要直接在前端或产品逻辑里接。必须走 `model-adapter`，并通过 `.env` 或 secret store 提供密钥。

### 我可以把真实项目文件放到 samples/input 吗？

不可以。`samples/input` 只允许 mock/sample 数据。

### 我可以跳过 SDD 直接实现吗？

小的 prototype-only 修补可以轻量处理。真实产品行为、backend、adapter、graph、Ask 都必须先对齐 SDD。

### 图谱一定要上 Neo4j 吗？

不是。MVP 可以先用 PostgreSQL 表表达 node/edge/evidence。重点是可解释、可追溯，不是数据库炫技。

### Ask 可以直接问所有解析内容吗？

不可以。Ask 应该默认只使用 approved knowledge，并返回 citations。

## 12. 提交前检查

提交前至少确认：

```bash
git status --short
git diff --check
```

Frontend 改动：

```bash
cd frontend
npm run lint
npm run typecheck
npm run test
npm run build
```

Acceptance 改动：

```bash
npm run e2e:loop:mock
```

如果有检查没跑，要在最终说明里明确写出原因。

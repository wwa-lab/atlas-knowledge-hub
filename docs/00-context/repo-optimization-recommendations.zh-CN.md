# Atlas Knowledge Hub 仓库优化建议

> 生成日期：2026-07-08
> 来源：仓库整体审查
> 状态：建议稿，待人工评审优先级

---

## 总览

Atlas 当前整体评分 **8/10**。主要扣分在前端组件化缺失和产品成熟度。以下建议按优先级排列，标注了是否需先走 SDD slice。

| 优先级 | 项 | 类型 | 需 SDD? | 预计工作量 |
|---|---|---|---|---|
| P0 | 提交 Maven Wrapper | 工具 | 否 | 5 分钟 |
| P0 | 新增 backend CI | CI | 否 | 30 分钟 |
| P0 | 拆分 App.vue + 引入 vue-router | 实现 | **是** | 1-2 天 |
| P1 | 前端测试翻转（单元↑ E2E↓比例） | 测试 | 随 P0#3 | 随组件拆分 |
| P1 | 文档瘦身与健康检查脚本 | 工具 | 否 | 半天 |
| P1 | 轻量状态管理（provide/inject + composables） | 实现 | 随 P0#3 | 随组件拆分 |
| P2 | docker-compose.yml 全栈开发 | DevOps | 否 | 1 小时 |
| P2 | 夜间 provider-backed E2E CI | CI | 否 | 1 小时 |
| — | `dev:fullstack` / `dev:mock` 脚本区分 | 工具 | 否 | 5 分钟 |

---

## P0 — 立即/本季度应该做

### 1. 拆分 `App.vue`：最紧迫的技术债务

**现状：**
- 单文件 5332 行 Vue SFC
- 无路由（手工枚举切换视图）
- 无状态管理（所有状态在根组件）
- 无 composables
- 所有组件逻辑内联在一个 `<script setup>` 中

**为什么现在就要做：**
- 每次加新功能都要触碰这个文件，冲突风险高
- 新人无法快速定位代码
- 单元测试只能测整个 App（`App.test.ts` 1400 行），无法测到组件级别
- 项目文档自己也知道这个问题（README: "split into feature components once the relevant SDD slice is accepted"）

**具体方案（建议作为 `app-componentization` SDD slice 推进）：**

```
Step 1: 引入 vue-router
  路由设计（轻量，初期就 3 条）:
    /              → HomeView
    /space/:id     → SpaceShell（内嵌 Tab 子路由或组件切换）
    /chat          → GlobalChatView

Step 2: 提取组件树
  src/
    components/
      layout/
        AppShell.vue           ← 全局壳：sidebar + header + settings drawer
        Sidebar.vue            ← 空间列表 + 设置入口 + 新建空间
        SettingsDrawer.vue     ← 全量设置 Modal/Sheet（不替换当前视图）
      home/
        HomeView.vue           ← 首页知识空间列表 + 搜索
      space/
        SpaceShell.vue         ← 空间详情壳 + TabBar
        DocsTab.vue            ← 文档/文件面板
        ProcessingCenter.vue   ← 处理中心（上传、批次、死信队列）
        WikiTab.vue            ← Wiki 面板
        GraphTab.vue           ← 知识图谱面板
        ReviewTab.vue          ← 审核面板
        ConnectorsTab.vue      ← 连接器面板
      chat/
        GlobalChat.vue         ← 全局多空间对话
      settings/
        ModelSettings.vue      ← 模型配置面板
        GeneralSettings.vue    ← 通用设置面板
      shared/
        FileTree.vue           ← 文件树
        BatchStatus.vue        ← 批次状态
        Pagination.vue         ← 分页
        StatusBadge.vue        ← 状态徽章
        ...
    composables/
      useSpaces.ts             ← 空间 CRUD
      useBatches.ts            ← 批次/文件
      useWiki.ts               ← Wiki 页面
      useGraph.ts              ← 知识图谱
      useAsk.ts                ← Trusted Ask
      useAuth.ts               ← 当前用户/权限
      useSettings.ts           ← 设置/主题/语言
      ...

Step 3: 按组件边界重写 Vitest 测试
  每个提取出的组件 1 个 .test.ts
  每个 composable 1 个 .test.ts
  形成 单元测试 > 组件测试 > E2E 的正三角结构
```

**预期收益：**
- 新功能开发不再畏惧触碰巨型文件
- 新人可以按路由/组件名定位代码
- 组件级单元测试可以写得小且快
- 为后续引入 Pinia 做铺垫（如果需要）
- E2E 测试可以保持，但不再承担单元测试的责任

> **注意：** 项目规则要求 "Do not componentize until SDD slice accepted"。所以这应该作为下一个高优先级 slice 排队，先走 SDD（generate-all → 人工接受 → 实现）。

---

### 2. 补上后端 CI（`mvn verify` 进入 GitHub Actions）

**现状：**
- `.github/workflows/frontend-ci.yml` 存在且覆盖前端
- `agent-workflow-gate.yml` 覆盖 workflow gate
- 后端 73 个测试类没有任何 CI 保护

**方案：** 新增 `.github/workflows/backend-ci.yml`：

```yaml
name: Backend CI

on:
  push:
    branches: [main, develop-leo]
  pull_request:
    branches: [main, develop-leo]

jobs:
  verify:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: atlas_test
          POSTGRES_USER: atlas
          POSTGRES_PASSWORD: atlas
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Verify
        run: ./mvnw -f backend verify
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/atlas_test
          SPRING_DATASOURCE_USERNAME: atlas
          SPRING_DATASOURCE_PASSWORD: atlas
```

**预期收益：** 每次 PR 自动跑 73 个后端测试，防止合并回归。

---

### 3. 提交 Maven Wrapper

**现状：** README 说 "no wrapper is committed"，依赖系统安装的 Maven。对新人/CI 不够友好。

**方案：**
```bash
cd backend
mvn wrapper:wrapper
git add mvnw mvnw.cmd .mvn/
git commit -m "chore: add Maven wrapper"
```

**预期收益：** 任何人 clone 后 `cd backend && ./mvnw spring-boot:run` 即可，无需安装 Maven。CI 脚本也改用 `./mvnw`。

---

## P1 — 下 2-3 个 slice 应该覆盖

### 4. 将前端测试从倒三角翻转为正三角

**现状（倒三角）：**

```
        /\   E2E: 16 spec 文件（多，但慢且脆弱）
       /  \
      /    \
     /______\
    /        \
   /  单元测试: 3 个 vitest 文件（太少）
  /____________\
```

**目标（正三角）：**

```
   E2E: 保留 16 个 spec，覆盖关键用户旅程
  ─────────────────────────────────
  组件测试: 每个提取出的 Vue 组件 1 个 spec
  ─────────────────────────────────────────
  单元测试: composables、utils、mappers、状态转换
  ───────────────────────────────────────────────
```

**具体动作（随 P0 组件拆分同步进行）：**
- 每提取一个组件 → 写一个组件测试（mount + 交互断言）
- 每提取一个 composable → 写一个 composable 测试（纯逻辑，不 mount）
- 工具函数移到 `utils/` 并测试
- E2E 保留但精简（去掉可以通过组件测试覆盖的细节断言）

---

### 5. 文档瘦身与健康检查

**现状：** 646 篇 MD 文档。大量切片完成后留下的 SDD 产物可能不再被阅读但仍在维护负担中。

**方案 A：新增 `npm run docs:health` 脚本**

检查项：
- 每对双语文件（`.md` + `.zh-CN.md`）是否都存在
- 双语文件的 ID（REQ-xxx、US-xxx、T-xxx）是否一致
- 标记 90 天未更新的文档
- 检查 traceability 文件中的状态与 `repo-status-roadmap.zh-CN.md` 是否一致

**方案 B：为已完成切片归档 SDD 产物**

已完成切片列表：`knowledge-space`、`folder-upload`、`metadata-api`、`converter-adapter`、`parser-adapter`、`storage-adapter`、`vector-adapter`、`model-adapter`、`review-publish`、`knowledge-graph`、`ask-rag`、`full-stack-productization`、`provider-backed-e2e`，以及 Wave 1-5 的 20 个切片。

为这些已完成切片的 01-06 目录文档打上归档标记，或移到 `docs/archive/`，减少活跃文档数量。

**方案 C：合并冗余 roadmap 文档**

当前有 4+ 个 roadmap 入口：
- `ROADMAP.md` / `.zh-CN.md`
- `docs/00-context/slice-roadmap.md` / `.zh-CN.md`
- `docs/00-context/repo-status-roadmap.md` / `.zh-CN.md`
- `docs/00-context/product-goal-progress.md` / `.zh-CN.md`

`repo-status-roadmap.zh-CN.md` 已声明为"唯一总览入口"。建议：
- 其他 roadmap 文件头部加醒目的指向链接
- 考虑合并为一个文件，按 section 区分

---

### 6. 轻量状态管理（provide/inject + composables）

**不需要 Pinia（至少初期不需要）。**

组件拆分（P0#1）时同步引入：

```typescript
// src/composables/useAtlasStore.ts
import { ref, provide, inject, type InjectionKey } from 'vue'

interface AtlasStore {
  spaces: Ref<ApiSpace[]>
  currentSpace: Ref<ApiSpace | null>
  currentView: Ref<ProductView>
  fetchSpaces: () => Promise<void>
  // ...
}

const KEY: InjectionKey<AtlasStore> = Symbol('atlas')

export function provideAtlasStore() {
  const store = { /* ... */ }
  provide(KEY, store)
  return store
}

export function useAtlasStore() {
  const store = inject(KEY)
  if (!store) throw new Error('useAtlasStore() must be used inside <AppShell>')
  return store
}
```

在 `AppShell.vue` 中调用 `provideAtlasStore()`，子组件通过 `useAtlasStore()` 消费。

**优势：**
- 零依赖
- 类型安全（InjectionKey）
- 等状态真正复杂时再迁移到 Pinia（几乎零成本，API 相似）

---

## P2 — 锦上添花

### 7. 增加 `docker-compose.yml` 用于开箱即用的全栈开发

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: atlas
      POSTGRES_USER: atlas
      POSTGRES_PASSWORD: atlas
    ports:
      - '5432:5432'
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ['CMD-SHELL', 'pg_isready -U atlas']
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    ports:
      - '8080:8080'
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/atlas
      SPRING_DATASOURCE_USERNAME: atlas
      SPRING_DATASOURCE_PASSWORD: atlas
      SPRING_JPA_HIBERNATE_DDL_AUTO: validate

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - '5173:5173'
    depends_on:
      - backend
    environment:
      VITE_API_BASE: http://localhost:8080

volumes:
  pgdata:
```

一条 `docker compose up` 全栈启动，对 demo、新人、第二层 E2E 都更友好。

---

### 8. 将第三层 E2E 加入夜间 CI

**现状：** `npm run e2e:third-layer` 是 opt-in，需要本地配置 provider key。

**方案：** 新增 `.github/workflows/nightly-provider-e2e.yml`：

```yaml
name: Nightly Provider-Backed E2E

on:
  schedule:
    - cron: '0 2 * * *'  # 每天凌晨 2 点 UTC
  workflow_dispatch:       # 也支持手动触发

jobs:
  third-layer:
    runs-on: ubuntu-latest
    # ... (同 backend-ci 的 postgres + java + frontend 设置)
    steps:
      # ...
      - name: Run third-layer E2E
        run: npm run e2e:third-layer
        env:
          ATLAS_MODEL_API_KEY: ${{ secrets.ATLAS_MODEL_API_KEY }}
          ATLAS_MODEL_PROVIDER: ${{ vars.ATLAS_MODEL_PROVIDER }}

  notify-failure:
    needs: third-layer
    if: failure()
    runs-on: ubuntu-latest
    steps:
      - name: Notify
        run: echo "Provider-backed E2E failed — check logs"
      # 可接入 Slack/邮件/webhook
```

**关键：** 这个 workflow 不阻塞任何 PR，只做监控。失败时通知但不影响开发。

---

## ⚡ 不需要 SDD 就能立刻做的（5 分钟级）

### A. `dev:fullstack` / `dev:mock` 脚本区分

在 `frontend/package.json` 中：

```json
{
  "scripts": {
    "dev": "vite",
    "dev:mock": "vite",
    "dev:fullstack": "VITE_API_BASE=http://localhost:8080 vite"
  }
}
```

并在 `frontend/src/api.ts` 中读取：

```typescript
const API_BASE = import.meta.env.VITE_API_BASE || ''
```

这样开发者不会困惑于"为什么前端总是显示 mock 数据"——`npm run dev:fullstack` 连接后端，`npm run dev` 用 mock。

### B. 在 `vite.config.ts` 中加 proxy（可选替代方案）

```typescript
export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

这样前端始终请求同源 `/api/*`，Vite 在 dev 模式下自动 proxy 到后端。无需 `VITE_API_BASE` 环境变量。

---

## 执行建议

1. **立刻做（无 SDD 依赖）：** #2 Maven Wrapper + #3 后端 CI + #A dev 脚本区分（总计 < 1 小时）
2. **下一个 SDD slice 排队：** `app-componentization`（#1 组件拆分 + #4 测试翻转 + #6 状态管理，合并为一个 slice）
3. **Slice 完成后顺手做：** #5 文档健康检查
4. **有空再做：** #7 docker-compose + #8 夜间 CI

不要同时推进多个 slice。Master roadmap 可以管理队列，但执行必须一次一个 slice。

---

## 改动追踪

| 日期 | 变更 | 来源 |
|---|---|---|
| 2026-07-08 | 初始建议稿 | 仓库全面审查 |

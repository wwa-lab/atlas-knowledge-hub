# 本地运行手册（VS Code 版）

这份 runbook 面向在 VS Code 里本地运行 Atlas Knowledge Hub 的同学。目标是：照着命令做，可以先把项目跑起来，再逐步跑自动化验收、前端测试、后端测试和本地 API。

## 0. 你可以本地跑哪些东西

| 路径 | 用来验证什么 | 需要什么 |
|---|---|---|
| 静态原型 | 产品形态和 mock UX。 | 浏览器。 |
| 前端开发服务 | Vue 前端壳和前端交互。 | Node.js/npm。 |
| Mock E2E 闭环 | 本地第一层自动验收，不需要真实公司环境。 | Node.js/npm、Playwright 浏览器。 |
| 后端测试 | API、adapter、Flyway、PostgreSQL 合约。 | Java 21、Maven、Docker Desktop。 |
| 第二层 E2E | 本地浏览器 + Spring Boot API + PostgreSQL 全栈闭环。 | Node.js/npm、Java 21、Maven、Docker Desktop。 |
| 后端本地 API | Spring Boot API 连接你自己的 PostgreSQL。 | Java 21、Maven、本地 PostgreSQL 配置。 |

最快看产品：打开静态原型。

最推荐的第一层本地验收：运行 `npm run e2e:first-layer`。

如果你要验证前端浏览器确实连到本地 Spring Boot API：运行 `npm run e2e:second-layer`。

## 1. 在 VS Code 打开项目

在终端里：

```bash
cd <your-workspace>/atlas-knowledge-hub
code .
```

或者在 VS Code 里：

```text
File -> Open Folder... -> atlas-knowledge-hub
```

打开后，使用 VS Code 内置终端：

```text
Terminal -> New Terminal
```

后续命令默认都在 VS Code 的 integrated terminal 里运行。

## 2. 检查本地依赖

在 VS Code 终端运行：

```bash
node --version
npm --version
java --version
mvn --version
docker --version
```

推荐：

- Node.js + npm。
- Java 21。
- Maven。
- Docker Desktop 已启动，后端集成测试需要 Testcontainers。

如果你只是看静态原型，不需要 Java/Maven/Docker。

## 3. 安装前端依赖

在仓库根目录运行：

```bash
npm run setup
```

这个命令等价于：

```bash
npm --prefix frontend ci
```

如果失败，可以在 VS Code 终端里手动执行：

```bash
cd frontend
npm ci
```

## 4. 打开静态原型

如果你只想先看产品：

```bash
open prototypes/index.html
```

预期结果：

- 浏览器打开 Atlas Knowledge Hub。
- 可以进入 IBM i Modernization demo space。
- 可以看到 Documents、Wiki、Graph、Review、Ask 等 mock 页面。

这条路径不启动任何服务，也不需要 backend/database。

## 5. 在 VS Code 里跑前端

打开一个 VS Code 终端：

```bash
cd frontend
npm run dev
```

浏览器打开：

```text
http://127.0.0.1:5173
```

预期结果：

- Vite 成功启动。
- 页面显示 Atlas 前端壳。

停止服务：

```text
Ctrl+C
```

## 6. 跑第一层 Mock E2E 闭环

回到仓库根目录：

```bash
cd <your-workspace>/atlas-knowledge-hub
npm run e2e:first-layer
```

这个命令会自动完成：

1. 检查 mock E2E 配置。
2. 清理 `samples/output/e2e`。
3. 准备 `samples/input/e2e`。
4. 构建 frontend。
5. 启动完整 frontend Playwright E2E。
6. 运行后端 `mvn -f backend/pom.xml verify` 合约验证。
7. 检查 diff whitespace 和生成文件是否被误跟踪。

预期输出包含：

```text
First-layer E2E gate complete.
```

报告位置：

```text
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

你可以在 VS Code 文件树里右键 `frontend/playwright-report/index.html`，选择在浏览器中打开。

如果你只想跑最小的 mock knowledge-loop acceptance：

```bash
npm run e2e:loop:mock
```

## 7. 跑完整前端检查

在 VS Code 终端里：

```bash
cd frontend
npm run lint
npm run typecheck
npm run test
npm run build
npm run e2e
```

预期结果：

- ESLint 通过。
- TypeScript 通过。
- Vitest 通过。
- Vite build 成功。
- Playwright E2E 成功。

当前前端 E2E 覆盖：

- Phase 1 smoke。
- Folder upload mock flow。
- Knowledge loop acceptance。
- Review publish 页面。
- Knowledge graph 页面。

## 8. 跑后端验证

后端验证需要 Java 21、Maven 和 Docker Desktop。

在仓库根目录运行：

```bash
mvn -f backend/pom.xml verify
```

预期结果：

```text
BUILD SUCCESS
```

说明：

- 后端集成测试使用 Testcontainers。
- Docker Desktop 必须是运行状态。
- Maven 会生成 `backend/target/`，这是构建输出，不要提交。

清理后端构建输出：

```bash
rm -rf backend/target
```

## 9. 跑第二层本地全栈 E2E

第二层用于验证：浏览器里的前端不是只看 mock/fallback，而是真的连到了本地 Spring Boot API 和临时 PostgreSQL。

在仓库根目录运行：

```bash
npm run e2e:second-layer
```

这个命令会自动完成：

1. 检查 mock 配置。
2. 清理并准备 sample 输入输出。
3. 启动临时 Docker PostgreSQL，默认端口 `55432`。
4. 启动 Spring Boot API，默认端口 `18080`。
5. 用 `VITE_ATLAS_API_BASE_URL` 指向本地 API 构建 frontend。
6. 通过 API 创建 sample batch、审批、发布 Wiki、投影 Graph、写入 mock vector、发起 mock Ask。
7. 打开浏览器验证 Graph 页面显示 `Graph API connected`，并且不是 fallback mock graph。
8. 检查 diff whitespace。

预期输出包含：

```text
Second-layer E2E complete.
```

报告和日志位置：

```text
samples/output/e2e/second-layer-backend.log
frontend/playwright-report/index.html
frontend/test-results/e2e-junit.xml
```

安全边界：

- 只使用 mock/sample data。
- 不调用 DeepSeek、Copilot 或任何真实 model provider。
- 不使用真实公司文档、真实 API key、真实对象存储或真实向量数据库。
- 默认结束时会删除临时 PostgreSQL Docker 容器。

如果端口冲突，可以换端口：

```bash
ATLAS_E2E_POSTGRES_PORT=55433 ATLAS_E2E_BACKEND_PORT=18081 npm run e2e:second-layer
```

如果你想测试结束后保留临时 PostgreSQL 容器用于排查：

```bash
KEEP_ATLAS_E2E_STACK=1 npm run e2e:second-layer
```

## 10. 在 VS Code 里启动后端 API

只有当你想手动调用 Spring Boot API 时才需要这一步。

### 10.1 用 Docker 准备本地 PostgreSQL（推荐）

确认 Docker Desktop 已启动，然后在 VS Code 终端运行：

```bash
docker run --name atlas-postgres \
  -e POSTGRES_DB=atlas_knowledge_hub \
  -e POSTGRES_USER=atlas_user \
  -e POSTGRES_PASSWORD=change-me-local-only \
  -p 5432:5432 \
  -d postgres:16
```

检查容器是否启动：

```bash
docker ps --filter name=atlas-postgres
```

预期能看到 `atlas-postgres`，状态为 `Up`。

如果以后要停止数据库：

```bash
docker stop atlas-postgres
```

重新启动已经创建过的数据库：

```bash
docker start atlas-postgres
```

如果你想彻底删除这个本地数据库容器和数据：

```bash
docker rm -f atlas-postgres
```

删除后再运行前面的 `docker run` 命令，会得到一个全新的空数据库。

### 10.2 如果你已经安装了本机 PostgreSQL

如果你不用 Docker，而是已经有本机 PostgreSQL，可以用 `psql` 创建本地开发库：

```bash
createdb atlas_knowledge_hub
createuser atlas_user
psql -d atlas_knowledge_hub -c "ALTER USER atlas_user WITH PASSWORD 'change-me-local-only';"
psql -d atlas_knowledge_hub -c "GRANT ALL PRIVILEGES ON DATABASE atlas_knowledge_hub TO atlas_user;"
```

如果 `createuser atlas_user` 提示用户已存在，可以跳过这一行。

### 10.3 配置后端环境变量

准备好 PostgreSQL 后，在同一个 VS Code 终端设置环境变量：

```bash
export ATLAS_DB_URL='jdbc:postgresql://127.0.0.1:5432/atlas_knowledge_hub'
export ATLAS_DB_USERNAME='atlas_user'
export ATLAS_DB_PASSWORD='change-me-local-only'
export ATLAS_DB_SCHEMA='atlas'
```

这些变量只对当前终端有效。关闭终端后需要重新设置。

### 10.4 启动后端

启动后端：

```bash
mvn -f backend/pom.xml spring-boot:run
```

预期结果：

- Spring Boot 启动。
- Flyway 校验并执行迁移。
- API 在默认 Spring Boot 端口可访问，除非你额外改了端口。

Flyway 会自动执行：

```text
backend/src/main/resources/db/migration/
```

所以你不需要手动运行 SQL migration。

安全提醒：

- 不要把真实密码写进文档、截图或 commit。
- 不要在共享终端里 `echo "$ATLAS_DB_PASSWORD"`。

### 10.5 验证数据库里已经建表

如果使用 Docker 容器：

```bash
docker exec -it atlas-postgres psql -U atlas_user -d atlas_knowledge_hub
```

进入 `psql` 后运行：

```sql
\dn
\dt atlas.*
```

退出：

```sql
\q
```

如果能看到 `atlas` schema 和若干表，说明 Flyway migration 已经执行。

## 11. Configured 模式

Configured 模式用于本地/公司集成环境，不是默认开发路径。

从模板开始：

```bash
cp configs/atlas.company.example.env .env
```

然后填写你本地批准使用的配置：

```text
ATLAS_MODE=configured
ATLAS_API_BASE_URL=...
ATLAS_DATABASE_URL=...
ATLAS_MODEL_PROVIDER=...
ATLAS_MODEL_ENDPOINT=...
ATLAS_MODEL_API_KEY=...
```

运行：

```bash
npm run e2e:loop:configured
```

注意：

- `.env` 不要提交。
- 不要把真实 API key 粘进 issue、文档、截图、前端代码。
- DeepSeek/Copilot/provider 测试应该是 opt-in，不应该进入默认 CI。

## 12. VS Code 推荐开几个终端

建议开 3 个 VS Code terminal：

```text
Terminal 1: frontend dev server
  cd frontend
  npm run dev

Terminal 2: backend API
  export ATLAS_DB_URL=...
  export ATLAS_DB_USERNAME=...
  export ATLAS_DB_PASSWORD=...
  mvn -f backend/pom.xml spring-boot:run

Terminal 3: tests / git
  npm run e2e:first-layer
  npm run e2e:second-layer
  npm --prefix frontend run e2e
  mvn -f backend/pom.xml verify
  git status --short
```

如果只是跑 mock E2E，不需要开 backend API terminal。

## 13. 清理本地生成文件

安全清理命令：

```bash
rm -rf frontend/playwright-report frontend/test-results
rm -rf samples/output/e2e
rm -rf backend/target
```

不要删除：

```text
backend/src/main/resources/db/migration/
docs/
frontend/src/
prototypes/
```

## 14. 常见问题

### `npm run setup` 失败

进入 frontend 目录手动安装：

```bash
cd frontend
npm ci
```

如果 `package-lock.json` 和 `package.json` 不一致，先看 git diff，不要随手升级依赖。

### Playwright 提示缺少浏览器

在 `frontend/` 里运行：

```bash
npx playwright install
```

再跑：

```bash
npm run e2e
```

### 端口被占用

常见端口：

- `5173`：Vite dev server。
- `4173`：Vite preview / Playwright webServer。
- `55432`：第二层 E2E 临时 PostgreSQL。
- `18080`：第二层 E2E 临时 Spring Boot API。

检查：

```bash
lsof -i :5173
lsof -i :4173
lsof -i :55432
lsof -i :18080
```

关闭对应进程后重试。

如果只是第二层端口冲突，也可以直接换端口：

```bash
ATLAS_E2E_POSTGRES_PORT=55433 ATLAS_E2E_BACKEND_PORT=18081 npm run e2e:second-layer
```

### 后端测试找不到 Docker

确认 Docker Desktop 已启动，然后重跑：

```bash
mvn -f backend/pom.xml verify
```

### 后端启动报 datasource 缺失

检查：

```bash
echo "$ATLAS_DB_URL"
echo "$ATLAS_DB_USERNAME"
```

不要输出密码。

### Flyway migration validation 失败

不要随便改已经应用过的 migration。先确认是不是本地数据库过旧。对于本地 scratch database，可以重建数据库后再启动。

### `git status` 出现生成文件

检查：

```bash
git status --short
```

这些应该是生成文件，不要提交：

```text
backend/target/
frontend/playwright-report/
frontend/test-results/
samples/output/
```

## 15. 判断本地已经跑通的最小标准

在 VS Code 终端里跑：

```bash
npm run e2e:first-layer
npm run e2e:second-layer
mvn -f backend/pom.xml verify
git diff --check
git status --short
```

预期：

- 第一层 E2E 通过。
- 第二层 E2E 通过。
- Backend verify 通过。
- 没有 whitespace 错误。
- 没有意外生成文件、真实密钥或真实公司数据进入 git。

如果有命令没跑，要记录原因。

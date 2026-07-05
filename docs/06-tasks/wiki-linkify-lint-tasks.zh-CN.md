# 任务：Wiki 自动互链与质量检查

## 状态

当前用户已接受，并已按 Wave 1 `wiki-linkify-lint` 的 deterministic 成熟度目标实现和验证。

## 任务清单

| ID | Task | Requirements | Verification |
|---|---|---|---|
| T-WIKI-LINKIFY-LINT-001 | 在产品代码改动前记录用户对本双语 SDD 的接受。 | REQ-WIKI-LINKIFY-LINT-001 | Traceability 在实现前包含 accepted gate。 |
| T-WIKI-LINKIFY-LINT-002 | 增加后端 schema 增量支持：`wiki_generation_run.mode = linkify-lint` 和所需 issue 查询路径。 | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-011 | Flyway migration 在 `cd backend && mvn verify` 下校验通过。 |
| T-WIKI-LINKIFY-LINT-003 | 增加启动 linkify/lint run 与列出空间 issues 的 request/response DTO 和 API endpoints。 | REQ-WIKI-LINKIFY-LINT-002, REQ-WIKI-LINKIFY-LINT-014 | API contract tests 覆盖成功路径、validation、安全 errors。 |
| T-WIKI-LINKIFY-LINT-004 | 基于同空间 slugs 和 aliases 实现 deterministic target indexing。 | REQ-WIKI-LINKIFY-LINT-003 | Unit/service tests 覆盖 aliases、ambiguity、cross-space exclusion。 |
| T-WIKI-LINKIFY-LINT-005 | 实现感知 protected region 的 Markdown linkify utility。 | REQ-WIKI-LINKIFY-LINT-004, REQ-WIKI-LINKIFY-LINT-005 | Unit tests 覆盖 frontmatter、fenced code、inline code、existing Wiki links、Markdown links、images、self links、one link per target。 |
| T-WIKI-LINKIFY-LINT-006 | 确定性且幂等地更新 Wiki page `inLinks` 和 `outLinks`。 | REQ-WIKI-LINKIFY-LINT-006 | Service tests 证明重复 run 不重复链接或非预期重排。 |
| T-WIKI-LINKIFY-LINT-007 | 实现 broken links、orphan pages、missing refs、stale refs、thin content、review-required ambiguity 的 lint issue rules。 | REQ-WIKI-LINKIFY-LINT-007, REQ-WIKI-LINKIFY-LINT-008, REQ-WIKI-LINKIFY-LINT-009, REQ-WIKI-LINKIFY-LINT-010 | Service tests 覆盖每种 issue type 和 no-model/no-parser 行为。 |
| T-WIKI-LINKIFY-LINT-008 | 持久化安全 run、log、issue evidence，并具备确定性重复处理。 | REQ-WIKI-LINKIFY-LINT-011 | Repository/service tests 断言 safe summaries、issue status、无重复膨胀。 |
| T-WIKI-LINKIFY-LINT-009 | 更新 Vue Processing Center 和 Wiki page 表面，展示 issue counts 和 page warnings。 | REQ-WIKI-LINKIFY-LINT-013 | `cd frontend && npm run typecheck && npm run test && npm run build`；若 UI path 改动则跑 E2E。 |
| T-WIKI-LINKIFY-LINT-010 | 运行 publish/list/ingest 回归测试，并确认 review statuses 被保留。 | REQ-WIKI-LINKIFY-LINT-012 | Focused backend tests 加 `cd backend && mvn verify`。 |
| T-WIKI-LINKIFY-LINT-011 | 运行完整 verification 与 safety scans。 | REQ-WIKI-LINKIFY-LINT-014 | Backend verify、frontend checks、适用时 E2E、`npm run e2e:second-layer`、`git diff --check`、secret/path scan、network/dependency scan。 |
| T-WIKI-LINKIFY-LINT-012 | 实现后更新 traceability、roadmap status 和 residual risk notes。 | REQ-WIKI-LINKIFY-LINT-001, REQ-WIKI-LINKIFY-LINT-014 | Context files 列出 changed docs/code、verification evidence、skipped checks 和 next slice。 |

## 实现顺序

接受后按 ID 顺序执行任务。如果实现会偏离 `docs/03-spec/wiki-linkify-lint-spec.md`，先停止并更新 SDD 或寻求方向，不要绕过规格编码。

## 实现后的必跑验证命令

```bash
cd backend && mvn verify
cd frontend && npm run typecheck && npm run test && npm run build
cd frontend && npm run e2e
npm run e2e:second-layer
git diff --check
```

另外对 changed files 运行 focused secret/private-path 和 network/dependency scans。

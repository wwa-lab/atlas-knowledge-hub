# 可追溯性：Provider-Backed E2E

## 状态

已实现。`provider-backed-e2e` 现在已有轻量 SDD 集、opt-in third-layer 命令、ModelAdapter 后的 DeepSeek chat execution、provider-backed Playwright 覆盖，以及验证证据。真实 provider run 依赖本地 key；当 `ATLAS_MODEL_API_KEY` 缺失时不执行。

## Slice 契约

| 字段 | 内容 |
|---|---|
| 目标 | 本地开发者可以通过环境变量显式提供 DeepSeek API key，一条命令启动本地 Atlas stack，并在浏览器页面完成真实 provider-backed Ask E2E，且只使用 mock/sample knowledge data。 |
| Slice | `provider-backed-e2e` |
| 阶段 | 第三层验收 / Phase 4+ provider 集成验证 |
| 范围 | Opt-in 第三层 E2E 命令、本地 stack 编排、浏览器里的 provider-backed Ask、ModelAdapter 后的 DeepSeek execution、安全失败和 artifact hygiene。 |
| 不包含 | 默认 first-layer/second-layer 变更、真实公司数据、raw provider artifacts、生产 auth/RBAC、成本治理，以及非 DeepSeek provider 选择。 |

## 已阅读来源

- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/sdd-profile.md`
- `docs/07-acceptance/README.md`
- `docs/07-acceptance/knowledge-loop-e2e.md`
- `docs/local-runbook.md`
- `docs/local-runbook.zh-CN.md`
- `docs/01-requirements/model-adapter-requirements.md`
- `docs/03-spec/model-adapter-spec.md`
- `docs/06-tasks/model-adapter-tasks.md`
- `docs/00-context/model-adapter-traceability.md`
- `docs/01-requirements/ask-rag-requirements.md`
- `docs/03-spec/ask-rag-spec.md`
- `docs/06-tasks/ask-rag-tasks.md`
- `docs/00-context/ask-rag-traceability.md`
- `docs/01-requirements/knowledge-graph-requirements.md`
- `docs/03-spec/knowledge-graph-spec.md`
- `docs/06-tasks/knowledge-graph-tasks.md`

## Skill 使用

| Skill | 使用方式 |
|---|---|
| `atlas-sdd-generate-all` | 作为 Atlas SDD 编排参考，用于双语文件、traceability、ID 和质量门禁。 |

完整 downstream SDD generation skills 本轮没有展开，因为用户明确要求轻量 documentation-only subset：requirements、spec、tasks 和 traceability。

## SDD Artifacts

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/provider-backed-e2e-requirements.md` | `docs/01-requirements/provider-backed-e2e-requirements.zh-CN.md` |
| Spec | `docs/03-spec/provider-backed-e2e-spec.md` | `docs/03-spec/provider-backed-e2e-spec.zh-CN.md` |
| Tasks | `docs/06-tasks/provider-backed-e2e-tasks.md` | `docs/06-tasks/provider-backed-e2e-tasks.zh-CN.md` |
| Traceability | `docs/00-context/provider-backed-e2e-traceability.md` | `docs/00-context/provider-backed-e2e-traceability.zh-CN.md` |

## 实现证据

| 区域 | 证据 |
|---|---|
| 根命令 | `package.json` 暴露 `npm run e2e:third-layer`。 |
| 前端命令 | `frontend/package.json` 暴露 `npm --prefix frontend run e2e:third-layer`，且只指向 `frontend/tests/e2e/third-layer`。 |
| 本地编排 | `scripts/e2e/run-third-layer.sh` 启动临时 PostgreSQL、Spring Boot API，构建 frontend，运行 provider-backed Playwright，扫描 artifacts，并清理服务。 |
| Provider adapter | `backend/src/main/java/com/atlas/metadata/adapter/ConfiguredModelAdapter.java` 在 `ModelAdapter` 后实现最小 DeepSeek chat execution。 |
| Ask 路由 | `backend/src/main/java/com/atlas/metadata/service/AskService.java` 将 `mode=configured` 的 Ask model run 路由到 adapter key `deepseek`，controller 与 Playwright 不暴露 provider 调用。 |
| E2E 覆盖 | `frontend/tests/e2e/third-layer/provider-backed-ask.spec.ts` 准备 approved sample evidence、发布 Wiki、建立 graph/vector evidence、触发 Ask、验证 citation/source trace，并确认浏览器 graph 连接 live API。 |
| 配置占位 | `configs/atlas.example.env`、`configs/atlas.company.example.env` 与 `configs/adapters.configured.example.yaml` 记录 provider settings，但不写真实 key。 |
| 用户可执行文档 | `README.md`、`docs/07-acceptance/README.md`、`docs/07-acceptance/knowledge-loop-e2e.md`、`docs/local-runbook.md` 与 `docs/local-runbook.zh-CN.md` 已说明 first/second/third-layer 验收、本地 DeepSeek 环境变量、VS Code 执行、报告、清理，以及 skip/failure 条件。 |

## API Guide 决策

本轻量 SDD pass 不包含 API guide。当前 slice 是 acceptance/documentation slice。如果后续实现引入超出现有 model-adapter 或 ask-rag contracts 的新 backend API 行为，必须在代码变更前增加 API guide 或已接受的 contract update。

## Requirement Trace

| Requirement | Spec Sections | Tasks |
|---|---|---|
| REQ-PBE2E-001 | Layer Isolation, Acceptance Layer Model | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-002 | Local Stack And Browser Journey | T-PBE2E-001, T-PBE2E-005 |
| REQ-PBE2E-003 | Local Stack And Browser Journey, Scope | T-PBE2E-005 |
| REQ-PBE2E-004 | Provider Configuration, Artifact Hygiene | T-PBE2E-002, T-PBE2E-003, T-PBE2E-007 |
| REQ-PBE2E-005 | ModelAdapter Boundary | T-PBE2E-003, T-PBE2E-004 |
| REQ-PBE2E-006 | Local Stack And Browser Journey, ModelAdapter Boundary | T-PBE2E-003, T-PBE2E-004, T-PBE2E-005 |
| REQ-PBE2E-007 | Safe Failure Behavior, State Model | T-PBE2E-002, T-PBE2E-006 |
| REQ-PBE2E-008 | Provider Configuration, Safe Failure Behavior | T-PBE2E-001, T-PBE2E-002, T-PBE2E-006 |
| REQ-PBE2E-009 | Artifact Hygiene | T-PBE2E-005, T-PBE2E-007 |
| REQ-PBE2E-010 | Layer Isolation, Acceptance Matrix | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-011 | Layer Isolation, Acceptance Layer Model | T-PBE2E-001, T-PBE2E-008 |
| REQ-PBE2E-012 | ModelAdapter Boundary, Artifact Hygiene | T-PBE2E-003, T-PBE2E-004, T-PBE2E-007, T-PBE2E-008 |

## 关键决策

| 决策 | 理由 |
|---|---|
| Third-layer 只能 opt-in。 | 防止意外网络/provider 调用、成本、凭据依赖和默认测试不稳定。 |
| First-layer 和 second-layer 保持 mock/sample-only。 | 保留现有本地可靠性和验收保证。 |
| DeepSeek key 只能来自环境变量。 | 避免真实凭据进入源码、文档、日志、截图、trace 和 git。 |
| Provider calls 必须经过 ModelAdapter。 | 保持 Atlas provider 可替换性和 Ask/RAG adapter 边界。 |
| Mock/sample knowledge data 仍然强制使用。 | Provider-backed generation 验证 integration path，而不是暴露真实公司文档。 |

## Review-Doc-Quality Gate

| Check | Result |
|---|---|
| 每个 touched artifact 都有英文和中文副本 | Pass |
| REQ/T IDs 在双语文档中一致 | Pass |
| Requirements 映射到 spec/tasks | Pass |
| Tasks 对后续 Codex implementation 可执行 | Pass |
| Phase 和 acceptance-layer discipline 明确 | Pass |
| Adapter、opt-in、environment-only key、mock/sample data 和 safe-failure constraints 明确 | Pass |
| API guide inclusion/omission 已记录 | Omitted and recorded |

## 验证证据

```bash
npm --prefix frontend run typecheck
mvn -f backend/pom.xml verify
npm run e2e:first-layer
npm run e2e:second-layer
if [ -n "${ATLAS_MODEL_API_KEY:-}" ]; then npm run e2e:third-layer; fi
npm run e2e:third-layer  # ATLAS_MODEL_API_KEY 缺失时已验证清晰 preflight failure
git diff --check
rg -n "<focused-doc-secret-private-path-pattern>" README.md docs/07-acceptance/README.md docs/07-acceptance/knowledge-loop-e2e.md docs/local-runbook.md docs/local-runbook.zh-CN.md docs/00-context/provider-backed-e2e-traceability.md docs/00-context/provider-backed-e2e-traceability.zh-CN.md
```

## 剩余风险

- 完整 third-layer provider execution 依赖网络与凭据；只有本地存在 `ATLAS_MODEL_API_KEY` 时才运行。
- Provider latency、quota、429 和 5xx 行为由 adapter 脱敏处理，但仍依赖 DeepSeek 可用性。
- Third-layer 不应成为默认 deterministic gate，除非另有已接受的 CI/secret-management 设计。
- 用户文档只展示 placeholder；操作者必须从已批准的本地 secret handling 取得真实 key，并确保 `.env` 或 shell exports 不进入 git。

## 推荐 Codex Handoff Command

```text
Implement the provider-backed-e2e slice strictly against docs/03-spec/provider-backed-e2e-spec.md and docs/06-tasks/provider-backed-e2e-tasks.md. Keep it opt-in, use mock/sample knowledge data only, route provider calls through ModelAdapter, keep DeepSeek credentials environment-only, and do not change first-layer or second-layer defaults.
```

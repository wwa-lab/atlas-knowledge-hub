# Atlas Knowledge Hub 下一步执行计划

日期：2026-07-05
基线：Product Goal Batch 1-8 已完成，Phase A-J 验收证据整理已完成
状态：用户验收评审就绪；下一步产品建设待启动

## 一、背景

Atlas Knowledge Hub 从 2026-07-05 完成了 Phase A-J 全路线图的 L2 Vue parity + L3 API-backed + L4 readiness preparation。现有 13 个 Playwright E2E、86 个后端 unit tests、37 个 integration tests 全部通过，second-layer 和 opt-in third-layer E2E 也已通过。

但当前的 Atlas 本质上是一个**架构完善、功能缺失**的知识库骨架：

- 产品外壳（Vue 首页、空间详情、标签页）✅
- 后端元数据 API（空间/批次/文件/chunk/审核/Wiki/Graph/Ask）✅
- 五个适配器契约（converter/parser/storage/vector/model）✅
- 真正的知识自动生成与维护 ❌

对照参考产品 WeKnora，Atlas 在以下核心能力上存在根本性缺口：Wiki Mode（自维护 Wiki ingest pipeline）、真实上传/解析链路、生产安全、部署运维。

## 二、核心差距总结

### P0 差距（阻塞产品可用性）

| # | 差距 | 影响 | WeKnora 对应 |
|---|---|---|---|
| G1 | **Wiki ingest pipeline 完全缺失** | Atlas 无法从文档自动生成和维护 Wiki 页面。现有 Wiki 标签页只是静态 mock 内容，没有 pending op、map/reduce、linkify、lint、index/log 任何机制。 | Wiki Mode 是 WeKnora 的三大核心能力之一 |
| G2 | **Wiki Page 数据模型太薄** | Atlas 当前的 Wiki page 没有 slug、page_type、aliases、source_refs、chunk_refs、in_links/out_links、version 等关键字段，无法支撑自维护 Wiki。 | WeKnora 的 wiki_pages 表有完整的数据模型 |
| G3 | **真实上传/解析链路未接通** | 上传仍是 mock，`trinity-office` 和 `document-normalize` 只是适配器契约+mock-engine，从未接入真实 runtime。 | WeKnora 有完整的 docreader → parsing → chunking → embedding 链路 |

### P1 差距（阻塞内部试用）

| # | 差距 | 影响 |
|---|---|---|
| G4 | 生产安全（auth/RBAC/rate limiting/audit/secret manager）未实现 |
| G5 | Linkify 和 Lint 工具完全缺失 |
| G6 | 知识图谱实体/关系自动抽取未实现 |
| G7 | Trusted Ask 检索治理、答案审核、cost guardrails 未实现 |

### P2 差距（阻塞生产上线）

| # | 差距 |
|---|---|
| G8 | 部署拓扑、监控、SLO、alert、rollback runbook |
| G9 | 大批量性能 benchmark |
| G10 | Multi-source ingestion、ReAct Agent、CLI/Agent Skills 等 WeKnora 有但 Atlas 未规划的能力 |

## 三、下一步执行路线

按优先级分三个 execution wave：

```
Wave 1: Wiki Foundation（让 Atlas 能从文档生成知识）
  ├─ Slice 1.1: Wiki Page 数据模型升级
  ├─ Slice 1.2: Wiki Ingest Pipeline v0（最小切片）
  ├─ Slice 1.3: Linkify + Basic Lint
  └─ Slice 1.4: 真实上传链路打通（trinity-office + document-normalize）

Wave 2: Wiki Hardening（让 Wiki 可维护、可信任）
  ├─ Slice 2.1: Index/Log 维护
  ├─ Slice 2.2: 增量修正（retract/refresh）
  ├─ Slice 2.3: Lint 增强 + 人工审核闸门
  └─ Slice 2.4: 知识图谱质量提升

Wave 3: Production Readiness（让 Atlas 可内部试用）
  ├─ Slice 3.1: 生产安全基础
  ├─ Slice 3.2: Trusted Ask 检索治理
  └─ Slice 3.3: 部署与监控
```

### 每个 Wave 的准入条件

- **Wave 1**：当前即可开始。所有 SDD 需在本 Wave 内补齐。
- **Wave 2**：Wave 1 全部完成 + 通过 first-layer 和 second-layer E2E。
- **Wave 3**：Wave 2 全部完成 + 用户批准内部试用计划。

## 四、Wave 1 详细规划

### Slice 1.1: Wiki Page 数据模型升级

**目标**：给 Atlas 的 Wiki page 增加自维护 Wiki 所需的完整数据字段。

**范围**：

- 后端 `wiki_page` 表新增字段：`slug`（唯一地址）、`page_type`（summary/entity/concept/index/synthesis/comparison）、`aliases`（JSONB 同义名数组）、`source_refs`（来源知识 ID 数组）、`chunk_refs`（chunk ID 数组）、`in_links`/`out_links`（Wiki graph 边）、`version`（用户可见内容变化时递增）
- 新增 `wiki_folder` 表（用于组织 Wiki 页面树形结构）
- 新增 `wiki_log_entry` 表（append-only ingest log，替代 giant TEXT log）
- 新增 `wiki_page_issue` 表（lint issues）
- 后端 API：CRUD for wiki_pages（含 slug 查找）、wiki_folders、wiki_log_entries、wiki_page_issues
- 前端 Vue：Wiki 标签页适配新数据模型，展示 page_type badge、aliases、source/chunk refs、in/out links

**排除**：
- 不实现 Wiki ingest 逻辑（slice 1.2）
- 不实现 linkify/lint 工具（slice 1.3）
- 不改变现有 Ask/Graph/Wiki 的审核发布流程

**SDD 需要产出**：
- `wiki-data-model` slice：requirements、user stories、spec、architecture、data-flow、data-model、design（含 API guide）、tasks、traceability

**验收**：
- Flyway migration 通过
- 后端 wiki_page CRUD 含 slug 查找通过
- 前端 Wiki 标签页正确展示新字段
- `npm run typecheck && npm run test && npm run build` 通过
- `cd backend && mvn verify` 通过
- `npm run e2e:second-layer` 通过
- existing E2E regression 通过

**预估复杂度**：中等（主要是 schema migration + API 扩展，不涉及 ingest 逻辑）

---

### Slice 1.2: Wiki Ingest Pipeline v0（最小切片）

**目标**：实现从 source document 到 Wiki page 的最小闭环，验证「pending op → map → reduce → source refs → run report」核心链路。

**设计约束**：
- 第一版不做服务化，不引入新数据库或消息队列
- pending ops 存为 JSONL 文件或后端数据库表的简化版（使能复用现有 PostgreSQL）
- 用后端数据库 advisory lock 替代文件锁（因为 Atlas 已有 PostgreSQL）
- 所有 LLM 调用走 `model-adapter`，token 消耗可追踪
- Map phase 对每个 source document 生成 summary page + candidate entity/concept pages
- Reduce phase 按 slug 聚合更新，优先更新既有页面避免重复
- 自动 linkify 暂不实现（slice 1.3）
- Lint 暂不实现（slice 1.3）
- Index/log 维护用最简单的 append 方式

**范围**：

| 能力 | v0 实现 | 延后 |
|---|---|---|
| Pending op queue | `wiki_pending_op` 数据库表，支持 `ingest`/`retract`/`refresh` | 无 |
| Space-level lock | PostgreSQL advisory lock | 无 |
| Map phase | 单文档 → summary + candidate entity/concept pages | 并行 map |
| Reduce phase | 按 slug 聚合更新，合并 source_refs | 复杂冲突解决 |
| Source refs | 写入 `source_refs` 字段 | chunk-level refs |
| Run report | batch 完成后生成 run report | 无 |
| Linkify | 不实现 | Slice 1.3 |
| Lint | 不实现 | Slice 1.3 |
| Index/log | 追加 batch log entry | 结构化 index overview |
| Incremental update | 仅 ingest，不做 retract/refresh 的完整实现 | Slice 2.2 |

**Prompt 设计**（参考 WeKnora 的 `internal/agent/prompts_wiki.go` 但不复制）：

Map phase 需要三个 prompt：
1. **source_summary**：生成 source 的摘要页（不超过 500 字）、候选 slug、关键实体/概念列表、chunk citation
2. **taxonomy_plan**：为本 batch 规划 folder path 结构
3. **entity_extraction**：从 source 提取实体/概念，生成 candidate wiki pages

Reduce phase 需要一个 prompt：
1. **page_merge**：把多个 candidate updates 合并到已有 wiki page，保留变更记录

所有 prompt 存入 `converter/prompts/wiki-ingest/`，作为适配器 prompt 资产。

**异常处理**（参考 WeKnora 的工程实践）：
- per-space active lock 防止并发 batch 写冲突
- lock 有 TTL 和 renew
- map 阶段单文档失败不影响同 batch 其他文档
- failed ops 记录重试次数，超阈值进入 dead letter
- 空文档不得编造主题，只记录「不可提取」
- 重复 source 按 checksum 去重

**SDD 需要产出**：
- `wiki-ingest` slice：requirements、user stories、spec、architecture、data-flow、data-model、design（含 prompt 契约和 API guide）、tasks、traceability

**验收**：
- 创建知识空间 → 上传一个 mock Markdown source → 调用 ingest API → 生成至少 1 个 summary page + 1 个 entity/concept page
- 同一 source 重跑不重复创建页面
- batch report 列出 created/updated/skipped pages
- touched pages 包含 source_refs
- map phase 单文档失败不影响同 batch 其他文档
- `npm run typecheck && npm run test && npm run build` 通过
- `cd backend && mvn verify` 通过
- `npm run e2e:second-layer` 通过
- existing E2E regression 通过
- 新增 wiki-ingest golden tests（用小型 fixture 验证幂等、去重、去重跑）

**预估复杂度**：高（核心链路首次落地，涉及 prompt 设计、adapter 调用、状态管理、异常路径）

---

### Slice 1.3: Linkify + Basic Lint

**目标**：实现自动跨 Wiki 链接注入和基本健康检查。

**范围**：

**Linkify**：
- 扫描 Wiki page 正文，对匹配已有 slug/aliases 的文本自动注入 `[[wiki-link]]`
- 安全跳过规则（按优先级）：
  1. fenced code block（```...```）内的文本
  2. inline code（`` `...` ``）内的文本
  3. 已有 wiki link（`[[...]]`）内的文本
  4. Markdown link（`[...](...)`）内的文本
  5. image（`![...](...)`）内的文本
  6. YAML frontmatter 内的文本
- 每个 target slug 在每个页面中最多 linkify 一次（首次出现）
- linkify 后更新 page 的 `out_links`，并更新 target page 的 `in_links`

**Basic Lint**：
- orphan page：没有 in_links 且不是 index page
- broken link：`out_links` 指向不存在的 slug
- stale source ref：`source_refs` 指向已删除的 source document
- empty/thin content：content 为空或低于阈值（如 < 100 字符且没有 source_refs）
- lint issues 写入 `wiki_page_issues` 表
- 前端处理中心展示 lint issues 统计和列表

**SDD 需要产出**：
- `wiki-linkify-lint` slice：requirements、user stories、spec、architecture、data-flow、design（含 linkify 算法）、tasks、traceability

**验收**：
- 自动 linkify 不会修改 code block、inline code、已有 links、images、frontmatter
- 每个 target 每个页面只 linkify 一次
- linkify 后 in_links/out_links 正确更新
- lint 能发现 broken link、orphan page、stale ref、empty content
- 处理中心展示 lint issues
- `npm run typecheck && npm run test && npm run build` 通过
- `cd backend && mvn verify` 通过
- `npm run e2e:second-layer` 通过
- existing E2E regression 通过

**预估复杂度**：中等（linkify 规则引擎需要仔细设计和测试）

---

### Slice 1.4: 真实上传链路打通

**目标**：把 converter-adapter 和 parser-adapter 从 mock-engine 切换到真实 runtime。

**范围**：
- 配置 `trinity-office` 的 adapter runtime（CLI 调用封装）
- 配置 `document-normalize` 的 adapter runtime
- Office 文档上传 → `trinity-office` → PDF → `document-normalize` → Markdown + images
- 真实文件类型检测、不支持格式报告
- 真实批次状态（转换中/转换失败/解析中/解析失败/需要 OCR/低置信度）
- 错误恢复和重试

**排除**：
- 外部网络调用
- 新 parser engine 的接入（MinU、Docling 等延后）

**SDD 需要产出**：
- 更新 converter-adapter 和 parser-adapter 的相关 SDD，补充 real-runtime 验收标准

**验收**：
- 上传真实 .docx/.pptx → PDF 转换 → Markdown 解析全链路通过
- 文件类型检测正确
- 转换/解析失败状态正确映射
- mock-engine 测试不被破坏
- 不引入外部网络调用
- `npm run e2e:second-layer` 通过

**预估复杂度**：中等（取决于 `trinity-office` 和 `document-normalize` 的 CLI 成熟度）

---

## 五、Wave 2 预览（详细规划延后到 Wave 1 完成后）

### Slice 2.1: Index/Log 维护
- 结构化 index overview（按 page_type、folder、更新时间组织）
- append-only log entries 替代 giant text log
- page-level change log（`## 变更记录` section）

### Slice 2.2: 增量修正
- retract: 删除 source document → 标记相关 wiki pages stale / retract source refs
- refresh: 重新处理 source document → 增量更新 wiki pages
- tombstone 机制：防止 in-flight ingest 重新写回已删除内容

### Slice 2.3: Lint 增强 + 人工审核闸门
- duplicate near-match page detection
- source note 没有传播到 wiki 的情况
- 大段自动重写的审核闸门

### Slice 2.4: 知识图谱质量提升
- 从已审核 Wiki pages 自动抽取实体和关系
- 更丰富的节点类型和边类型
- 生产级图谱布局

---

## 六、Wave 3 预览

### Slice 3.1: 生产安全基础
- RBAC（管理员/编辑/只读）
- API key 和 secret manager 集成
- Rate limiting
- Audit log

### Slice 3.2: Trusted Ask 检索治理
- 答案审核流程
- Provider cost guardrails
- Retrieval quality metrics

### Slice 3.3: 部署与监控
- 生产部署拓扑
- SLO/alert
- Rollback runbook

---

## 七、执行规则

本计划的执行遵循 Atlas 已有的 SDD 循环纪律：

1. **每次一个 Slice**。走完 SDD → 接受 → 实现 → 评审循环后再开下一个。
2. **SDD 先于代码**。每个 Slice 必须先有完整的双语 SDD 文档集（requirements → tasks + traceability），经人工接受后，再交给实现 Agent。
3. **不破坏现有基线**。每个 Slice 完成后必须通过：`npm run typecheck && npm run test && npm run build && npm run e2e:second-layer` + 全部现有 E2E regression。
4. **WeKnora 参考但不复制**。只借鉴架构思想（pending op、map/reduce、source refs、linkify/lint、index/log），不复制源码、prompt、UI 或项目结构。
5. **安全护栏**：所有 LLM 调用走 model-adapter；所有解析走 adapter 边界；不引入外部网络调用；不提交真实数据或密钥。
6. **每个 Slice 结束时更新**本文件和 `product-goal-progress.zh-CN.md`。

---

## 八、预估时间与风险

| Slice | 预估迭代 | 主要风险 | 缓解 |
|---|---|---|---|
| 1.1 Wiki 数据模型升级 | 1-2 轮 | schema migration 破坏现有 API | Flyway 版本化 + backward-compatible API |
| 1.2 Wiki Ingest Pipeline v0 | 2-4 轮 | prompt 质量不稳定；map/reduce 异常路径多 | golden tests + 小 fixture 迭代；先单文档后 batch |
| 1.3 Linkify + Basic Lint | 1-2 轮 | linkify 边界情况（code/image/链接嵌套） | TDD：先写所有边界测试，再实现 |
| 1.4 真实上传链路 | 1-3 轮 | trinity-office/document-normalize runtime 不稳定 | 保留 mock-engine 回退；先小文件测试 |

---

## 九、下一步行动

**立即可做**：

1. 审阅本计划，确认 Wave 1 范围和优先级是否合理。
2. 如果同意，从 **Slice 1.1: Wiki 数据模型升级** 开始——先调用 `atlas-sdd-generate-all` 生成完整双语 SDD，再进入实现。
3. 如果不确定优先级，可以用 `weknora-wiki-mode/04-sdd-handoff.md` 和 `03-replication-spec.md` 做更细粒度的需求评审。

**本文件会被持续更新**，每次 Slice 完成后追加进度记录。

# 溯源：Parser Adapter

## 状态

已于 2026-07-03 依据 `docs/03-spec/parser-adapter-spec.md` 与 `docs/06-tasks/parser-adapter-tasks.md` 实现。本切片使用 mock `document-normalize` parser adapter boundary 做验证，不启用真实 parser runtime。

## 切片契约

| Field | Value |
|---|---|
| Goal | Atlas 可以通过可替换 parser adapter，将已转换 PDF 解析为可追溯 Markdown、抽取 assets 与 source chunks，且 product layer 不耦合到 `document-normalize`。 |
| Slice | `parser-adapter` |
| Phase | 3 adapter |
| Scope | Parser adapter contract、capabilities、parser run behavior、Markdown/assets/source chunk output contracts、metadata write-back、mock-engine verification、API guide、tasks、后端实现与契约测试。 |
| Exclusions | 真实 parser runtime topology、OCR execution、LLM enrichment、graph、Ask/RAG、publish-to-Wiki、frontend UI、storage object operations、production auth/RBAC。 |
| Verification row | 针对 mock engines 的单元 + 集成测试。 |
| Constraints row | Parser/converter/model/vector/storage 只走产品侧 adapters；禁止直连工具；禁止硬编码单一实现；secret 脱敏；保留 trace/confidence/review。 |

## 来源文档

| Source | Use |
|---|---|
| `README.md` | 产品 workflow 与技术栈方向。 |
| `PROJECT_RULES.md` | SDD、adapter、security、data、phase discipline。 |
| `DEVELOPMENT_STANDARDS.md` | Phase 3 adapter standards 与 verification。 |
| `docs/00-context/sdd-profile.md` | 必需 SDD artifact chain 与 ID format。 |
| `docs/00-context/slice-roadmap.md` | Phase 3 verification/constraints 与 slice backlog。 |
| `docs/01-requirements/requirement.md` | 产品级 parser/converter、Markdown、trace/review requirements。 |
| `docs/markdown-standard.md` | Front matter 与 source trace requirements。 |
| `docs/batch-processing-design.md` | File lifecycle 与 reports。 |
| `docs/architecture.md` | Adapter-based architecture 与 worker plane。 |
| `docs/03-spec/converter-adapter-spec.md` | 同阶段 adapter pattern。 |
| `docs/04-architecture/converter-adapter-data-model.md` | 同阶段 run/result data model pattern。 |
| Existing backend metadata code | Grounded current entities、enums、validators、seam guard。 |

## 门禁说明

Phase 2 `metadata-api` 与 Phase 3 `converter-adapter` 已实现，因此 parser adapter 准入门已满足。`docs/00-context/slice-roadmap.md` 现已将 `parser-adapter` 标记为已实现，并将真实 runtime contract 作为后续延后项。

## SDD 产物

| Stage | English | Simplified Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/parser-adapter-requirements.md` | `docs/01-requirements/parser-adapter-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/parser-adapter-stories.md` | `docs/02-user-stories/parser-adapter-stories.zh-CN.md` |
| Spec | `docs/03-spec/parser-adapter-spec.md` | `docs/03-spec/parser-adapter-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/parser-adapter-architecture.md` | `docs/04-architecture/parser-adapter-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/parser-adapter-data-flow.md` | `docs/04-architecture/parser-adapter-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/parser-adapter-data-model.md` | `docs/04-architecture/parser-adapter-data-model.zh-CN.md` |
| Design | `docs/05-design/parser-adapter-design.md` | `docs/05-design/parser-adapter-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/parser-adapter-tasks.md` | `docs/06-tasks/parser-adapter-tasks.zh-CN.md` |
| Traceability | `docs/00-context/parser-adapter-traceability.md` | `docs/00-context/parser-adapter-traceability.zh-CN.md` |

## API Guide 决定

已包含 API guide。Parser-adapter 是 Phase 3 backend/API + adapter contract 切片，因此实现前需要 `docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md`。

## Requirement Trace

| Requirement | Stories | Spec Sections | Tasks |
|---|---|---|---|
| REQ-PA-001 | US-PA-001, US-PA-005 | Adapter Boundary | T-PA-003, T-PA-005, T-PA-009 |
| REQ-PA-002 | US-PA-001, US-PA-002 | Adapter Boundary, Capability Metadata | T-PA-003, T-PA-004 |
| REQ-PA-003 | US-PA-002 | Capability Metadata, API / Interface Surface | T-PA-002, T-PA-005, T-PA-008 |
| REQ-PA-004 | US-PA-001 | Parser Run | T-PA-001, T-PA-006, T-PA-008 |
| REQ-PA-005 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-006, T-PA-007 |
| REQ-PA-006 | US-PA-004 | Status Mapping And Failure Behavior | T-PA-006 |
| REQ-PA-007 | US-PA-004 | Status Mapping And Failure Behavior | T-PA-006 |
| REQ-PA-008 | US-PA-004, US-PA-005 | Status Mapping, Metadata Write-Back | T-PA-006, T-PA-009 |
| REQ-PA-009 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-007 |
| REQ-PA-010 | US-PA-003 | Markdown, Assets, And Chunks | T-PA-007 |
| REQ-PA-011 | US-PA-005 | Metadata Write-Back And Validation | T-PA-007, T-PA-009 |
| REQ-PA-012 | US-PA-001, US-PA-005 | Constraints, Acceptance Matrix | T-PA-003, T-PA-004, T-PA-009, T-PA-010 |
| REQ-PA-013 | US-PA-004 | Parser Run, Reporting | T-PA-001, T-PA-002, T-PA-006, T-PA-008, T-PA-010 |
| REQ-PA-014 | US-PA-003, US-PA-005 | Metadata Write-Back And Validation | T-PA-001, T-PA-006, T-PA-007, T-PA-010 |

## Grounding Evidence

| Claim | Evidence |
|---|---|
| Existing file items support PDF/Markdown/assets path metadata. | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` |
| Existing source chunks support page/section/confidence/review status. | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` |
| Existing statuses include parser-relevant values. | `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` |
| Existing path validator rejects unsafe paths. | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:9` |
| Existing converter API pattern provides capability/run/report precedent. | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:30` |
| Existing seam guard already scans parser engine names and outbound clients. | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` |

## 实现证据

| Task range | Evidence |
|---|---|
| T-PA-001 → T-PA-002 | `ParserRun`、`ParserFileResult`、Flyway V4 migration、parser DTOs、schema contract assertions。 |
| T-PA-003 → T-PA-004 | `ParserAdapter`、`ParserCapability`、`ParserRequest`、`ParserResult`、`MockDocumentNormalizeParserAdapter`、`DocumentNormalizeParserAdapter`。 |
| T-PA-005 → T-PA-008 | `ParserController`、`ParserService`、`ParserAdapterRegistry`、`ParserSummaryCalculator`、`ParserMapper`、parser API contract tests。 |
| T-PA-009 → T-PA-010 | `AdapterSeamGuardTest`、secret/path sanitization tests、完整 `mvn verify` 证据。 |

已运行验证：

```bash
cd backend && mvn verify
git diff --check
! rg -n "document-normalize|MinerU|Docling|PaddleOCR|ProcessBuilder|Runtime\\.getRuntime\\(|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n -e "api[_-]?[k]ey\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[p]assword\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[t]oken\\s*[:=]\\s*[^$\\s{][^\\s]*" -e "[A]KIA" -e "BEGIN .*PRIVATE [K]EY" -e "/[U]sers/" -e "[C]:\\\\" backend/src docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md
```

结果：2026-07-03 通过。

## 生成技能链

- `atlas-sdd-generate-all`
- `req-to-user-story`
- `user-story-to-spec`
- `spec-to-architecture`
- `architecture-to-design`
- `design-to-tasks`
- `architecture-review` gate considered for adapter boundary quality
- `review-doc-quality` gate applied as final SDD quality review

## 待确认问题

| ID | Question | Owner | Blocks Implementation? |
|---|---|---|---|
| OQ-PA-001 | 真实 `document-normalize` process vs worker topology。 | Architecture / platform | No；mock contract 可继续。 |
| OQ-PA-002 | 未来 draft `wiki_page` rows 的归属。 | Product / architecture | No；parser 默认不创建 `wiki_page`。 |
| OQ-PA-003 | 如产品后续需要不同 cutoff，再调整 low-confidence threshold `< 0.800`。 | Product | No；已严格按 spec 实现。 |

## 验证计划

Documentation-pass checks：

```bash
git diff --check
for f in docs/01-requirements/parser-adapter-requirements.md docs/01-requirements/parser-adapter-requirements.zh-CN.md docs/02-user-stories/parser-adapter-stories.md docs/02-user-stories/parser-adapter-stories.zh-CN.md docs/03-spec/parser-adapter-spec.md docs/03-spec/parser-adapter-spec.zh-CN.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-architecture.zh-CN.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-flow.zh-CN.md docs/04-architecture/parser-adapter-data-model.md docs/04-architecture/parser-adapter-data-model.zh-CN.md docs/05-design/parser-adapter-design.md docs/05-design/parser-adapter-design.zh-CN.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.zh-CN.md docs/06-tasks/parser-adapter-tasks.md docs/06-tasks/parser-adapter-tasks.zh-CN.md docs/00-context/parser-adapter-traceability.md docs/00-context/parser-adapter-traceability.zh-CN.md; do test -s "$f" || exit 1; done
! rg -n "T[O]DO|T[B]D|to be determine[d]|implementation will decid[e]|grep late[r]" docs/01-requirements/parser-adapter-requirements.md docs/02-user-stories/parser-adapter-stories.md docs/03-spec/parser-adapter-spec.md docs/04-architecture/parser-adapter-architecture.md docs/04-architecture/parser-adapter-data-flow.md docs/04-architecture/parser-adapter-data-model.md docs/05-design/parser-adapter-design.md docs/05-design/contracts/parser-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/parser-adapter-tasks.md docs/00-context/parser-adapter-traceability.md
```

Implementation-pass checks 见 `docs/06-tasks/parser-adapter-tasks.md`。

## 推荐 Codex 交接命令

```text
Implement the parser-adapter slice strictly against docs/03-spec/parser-adapter-spec.md and docs/06-tasks/parser-adapter-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

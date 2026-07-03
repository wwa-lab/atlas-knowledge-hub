# 需求：知识图谱

## 状态

草稿。Phase 4 hardening。切片 `knowledge-graph`。最后更新：2026-07-03。

## 切片契约

**目标：** 为用户提供可检查、证据支撑的知识图谱，图谱关系必须能被信任、审核，并回溯到 Wiki 页面与 source chunk。

**范围：** 从已批准/已发布 Wiki 与 source trace 元数据投影节点和边；图谱查询 API；图谱端点的 RBAC、审计和错误加固；前端 Graph tab 接入可检查证据详情；覆盖所触层的测试与 E2E。

**排除：** 直接从未审核 parser 原始输出建图、直连模型/向量/解析/存储引擎、真实外部图数据库、Ask 答案生成、Wiki 发布状态机实现、生产部署、真实公司数据、明文 secret、复制外部产品实现或资产。

**来源：** `README.md`、`PROJECT_RULES.md`、`DEVELOPMENT_STANDARDS.md`、`docs/00-context/sdd-profile.md`、`docs/00-context/slice-roadmap.md`、`docs/01-requirements/requirement.md`、`docs/knowledge-graph-design.md`、`docs/03-spec/knowledge-space-spec.md`、`docs/05-design/knowledge-space-design.md`、`docs/04-architecture/metadata-api-architecture.md`、`docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`、`frontend/public/atlas-prototype.html`、`prototypes/index.html`，以及现有后端 graph placeholder 实体。

## 产品需求

| ID | 需求 | 优先级 | 来源 |
|---|---|---:|---|
| REQ-KG-001 | 图谱只能从已批准或已发布 Wiki/source-trace 元数据派生，绝不能直接从未审核 parser 原始输出派生。 | Must | REQ-PROD-034 |
| REQ-KG-002 | 图谱节点必须支持 `KNOWLEDGE_SPACE`、`DOCUMENT`、`WIKI_PAGE`、`CONCEPT`、`ENTITY`、`SOURCE_CHUNK`。 | Must | `docs/knowledge-graph-design.md` |
| REQ-KG-003 | 图谱边必须支持 `CONTAINS`、`DERIVED_FROM`、`MENTIONS`、`DEFINES`、`RELATED_TO`、`BELONGS_TO`、`USES`、`DEPENDS_ON`、`REVIEWED_BY`。 | Must | `docs/knowledge-graph-design.md` |
| REQ-KG-004 | 每条可信边必须携带至少一个 Wiki page 或 source chunk 证据引用，并保留 confidence 与 review status。 | Must | REQ-PROD-037 |
| REQ-KG-005 | 图谱查询 API 必须暴露有界的节点/边列表、统计、过滤条件，以及选中节点的证据详情，并使用 Atlas 响应信封。 | Must | Phase 4 API guide 要求 |
| REQ-KG-006 | 图谱端点必须由后端强制授权，并返回用户安全错误，不泄露栈、SQL、secret、私有路径或原始引擎诊断。 | Must | REQ-PROD-053、REQ-PROD-077 |
| REQ-KG-007 | 图谱投影必须位于产品面 projection/adapter 边界之后，方便未来替换抽取引擎。 | Must | 适配器边界规则 |
| REQ-KG-008 | 前端 Graph tab 必须保留已接受原型交互：图谱画布、搜索/过滤、图例、hover、点击详情、证据详情面板。 | Must | FE baseline |
| REQ-KG-009 | review-required、低置信、缺失 source_trace、失败或未批准材料必须被可信图谱投影阻断，并可见地排除或报告。 | Must | Processing Center gate |
| REQ-KG-010 | 图谱操作必须记录审计证据，包括投影运行、手动关系审核动作、端点访问失败和发布到图谱的变化。 | Must | Phase 4 hardening |
| REQ-KG-011 | 图谱响应不得暴露明文 secret、内部端点、私有绝对路径、原始向量、原始模型 prompt 或机密文档正文。 | Must | 数据安全 |
| REQ-KG-012 | 验证必须包含所触层的完整单元、集成、API contract 与前端 E2E，并包含 diff hygiene、网络/依赖和 secret 扫描。 | Must | 路线图 Phase 4 |

## 假设

- 现有 Phase 2 graph 表是 placeholder；本切片可按已接受 API guide 添加必要字段、repository、service、endpoint 和 migration。
- `review-publish` 是上游门禁。如果实现开始时它尚未完成，Codex 必须使用 approved/published seed 或 fixture 数据，并记录依赖，不得绕过 review status。
- 真实图谱抽取未来可能使用模型或图引擎，但本切片必须用确定性/mock 投影验证，且不做外部网络调用。

## 待确认问题

| ID | 问题 | 本 SDD 默认 |
|---|---|---|
| OQ-KG-001 | SME 手动编辑图谱边是否属于本切片，还是属于 `review-publish`？ | 包含 graph edge 审核记录，但复杂图谱编辑不在范围内。 |
| OQ-KG-002 | 生产图谱布局引擎是什么？ | 暂不选择。使用前端渲染的图谱数据，并保持布局引擎可替换。 |
| OQ-KG-003 | 实现开始前 `review-publish` 是否已完成？ | 作为入口依赖；需要时用 approved fixture 做测试。 |

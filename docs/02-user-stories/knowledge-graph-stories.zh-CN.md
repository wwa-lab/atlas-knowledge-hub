# 用户故事：知识图谱

## 状态

草稿。来源：`docs/01-requirements/knowledge-graph-requirements.md`。

## 用户故事

### US-KG-001：探索证据支撑的图谱

作为知识使用者，我希望探索由已批准证据支撑的图谱节点和关系，以便在不信任未审核内容的前提下理解知识空间。

**验收标准**

1. Given 某知识空间存在已批准图谱数据，When 我打开 Graph，Then 我能看到节点、边、类型图例、统计和搜索/过滤控件。
2. Given 我 hover 或聚焦某个节点，When 详情可用，Then UI 展示 label、type、confidence 和 review status。
3. Given 我点击节点或边，When 存在证据，Then 我看到关联 Wiki/source chunk 引用，但看不到原始机密文档正文。
4. Given 某来源项未批准或缺少 source trace，When 图谱数据投影，Then 它被可信图谱结果排除。

**映射：** REQ-KG-001、REQ-KG-002、REQ-KG-003、REQ-KG-004、REQ-KG-008、REQ-KG-009。

### US-KG-002：通过加固 API 查询图谱数据

作为前端工程师，我希望拥有稳定的图谱 API 契约，以便 Graph tab 能渲染可信图谱数据，而不依赖持久化或抽取细节。

**验收标准**

1. Given 有效用户有空间访问权，When 前端请求 `/api/spaces/{spaceId}/graph`，Then API 返回 Atlas envelope，包含有界 nodes、edges、counts、filters 和选中详情元数据。
2. Given 提交无效过滤条件或未知 id，When API 校验请求，Then 返回字段级安全错误。
3. Given 未授权用户调用 API，When 授权失败，Then 后端返回 `401` 或 `403`，且不泄露敏感内部信息。
4. Given 返回图谱证据引用，Then 引用包含 source trace、confidence 和 review status。

**映射：** REQ-KG-004、REQ-KG-005、REQ-KG-006、REQ-KG-011。

### US-KG-003：在可替换边界后投影图谱记录

作为平台管理员，我希望图谱抽取隔离在产品面投影边界之后，以便 Atlas 未来替换图谱引擎时不改变产品工作流。

**验收标准**

1. Given 已批准 Wiki/source trace 记录存在，When 投影运行，Then graph nodes 和 edges 通过产品面 service boundary 创建或刷新。
2. Given 未来加入 graph/model engine，Then 引擎特定代码只存在于 adapter/worker 边界中，不进入 controller、前端组件或通用 metadata service。
3. Given 投影遇到 unsupported、unapproved 或 unsafe evidence，Then 该项被跳过，并留下安全原因和审计证据。
4. Given 投影部分失败，Then 有效输出保持 review-aware，失败详情经过脱敏。

**映射：** REQ-KG-001、REQ-KG-007、REQ-KG-009、REQ-KG-010、REQ-KG-011。

### US-KG-004：审计和治理图谱可信度

作为 SME reviewer 或 delivery lead，我希望图谱投影和审核动作可审计，以便可信图谱关系能够被解释和质疑。

**验收标准**

1. Given 投影创建或更新图谱记录，When run 完成，Then 审计元数据记录 actor/system、time、scope、counts 和安全 summary。
2. Given SME 审核某条 graph edge，When action 保存，Then edge review status 与 append-only review/audit record 反映该动作。
3. Given 某关系没有证据支撑，When 被检查，Then 它不能被标记为 trusted。
4. Given 后续 Ask 使用图谱数据，Then source trace、confidence 和 review status 仍然存在。

**映射：** REQ-KG-004、REQ-KG-006、REQ-KG-010、REQ-KG-012。

## 依赖

- Phase 2 metadata API 和 graph placeholder 表。
- Phase 3 parser/storage/vector/model adapter seam。
- `review-publish` 或 approved fixture 数据，作为 approved/published Wiki 与 source trace 来源。

## 范围外

- 真实外部图数据库集成。
- Ask/RAG 答案生成。
- 直接基于 parser 原始输出建图。
- 超出 inspect/review action 的复杂图谱编辑 UI。

## 待确认问题

- 见需求文档中的 OQ-KG-001 到 OQ-KG-003。

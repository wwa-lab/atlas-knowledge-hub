# 架构：文件夹上传（Folder Upload）

## 状态

草稿。Phase 1 前端，仅 mock。源自 `docs/03-spec/folder-upload-spec.md`。

## 范围与原则

既有 Vue 3 + Vite + TypeScript 应用内的纯前端 mock 功能。无后端、无持久化、无网络。所有入库逻辑在一个小型 typed 模块后被模拟，使后续阶段可在不改视图的前提下把 mock 换成真实适配器支撑的调用。

## 组件归属

```
Documents 标签页（knowledge-space 外壳，宿主）
        │  触发
        ▼
UploadReview（folder-upload）
   ├─ InventoryList        （行：路径、类型、大小、徽章、confidence、review）
   ├─ UnsupportedGroup     （单独的不支持文件）
   └─ 操作：Create Batch / Cancel
        │  Create Batch 时
        ▼
BatchView（folder-upload 扩展基线 renderDocs）
   ├─ BatchMetrics         （total / converted / markdown / review / failed / unsupported）
   ├─ BatchProgress        （4 阶段，mock %）
   └─ FileTree             （由清单路径构建层级）
        │  View Report
        ▼
BatchReport（folder-upload）
   └─ 报告分区：清单 / 不支持 / 失败 / 低置信 / 待审核
```

所有组件从单一 `folderUploadMock` 提供者消费 typed 数据；无一直接读文件或调用服务。

## 边界

### FE 组件边界

- **`folder-upload` 拥有：** `UploadReview`、`InventoryList`、`UnsupportedGroup`、`BatchReport`，以及批次创建/推进逻辑。`BatchView`/`FileTree` 渲染与 `knowledge-space` Documents 标签页共享并扩展，而非分叉。
- **`knowledge-space` 拥有：** Documents 标签页外壳、标签路由、全局文案表与主题 token（以新键扩展）。

### 后端/API 边界

不在范围内。无 API guide。未来 `POST /batches`、清单上传与状态轮询属于后续后端切片。

### 适配器边界（记录，不实现）

mock 入库模块定义与未来适配器对齐的概念缝隙，使后续替换机械化：

| 未来适配器 | 职责 | Phase-1 mock 替身 |
|---|---|---|
| storage 适配器 | 持久化上传原始包 + 文件 | 种子化内存清单 |
| converter 适配器（`trinity-office`） | Office→PDF | mock `PDF_CONVERTED` 状态分配 |
| parser 适配器（`document-normalize`） | PDF→Markdown + 图片 | mock `MARKDOWN_GENERATED` + confidence |

产品逻辑（视图）绝不得按名引用或调用这些工具；仅依赖 `folderUploadMock` 接口。以此维护 `PROJECT_RULES.md` 的 Adapter Boundaries 与 Parser Neutral 规则。

## 数据安全与溯源约束

- 不引入任何真实文件内容、私有路径、凭据或网络出站（仅 mock）。
- 每个生成文件项与报告条目保留 source-trace、confidence、review-status（`docs/markdown-standard.md`）；生成/低置信项默认 `REVIEW_REQUIRED`。
- 确定性处理（清单、分类、状态）先于任何未来 LLM 步骤。

## 模块布局（建议，供 Codex）

```
frontend/src/
  data/
    folderUploadMock.ts        # 种子清单 + 批次/报告工厂（纯函数、typed）
  features/folder-upload/
    UploadReview.vue
    InventoryList.vue
    UnsupportedGroup.vue
    BatchReport.vue
  types.ts                     # 扩展 FileItem/Batch/Inventory/Report 类型
```

组件/文件边界遵循全局编码规范（多小文件，典型 200–400 行）。确切放置在设计文档中对照当前 `frontend/src` 结构确认。

## 失败处理

- 空 / 全不支持清单禁用 `Create Batch` 并显示消息（spec 空态与错误态）。
- 清单展示封顶到文档化最大行数，避免病态渲染开销。
- 不存在解析抛错路径，因无真实解析；保护仅为对种子 mock 的输入形状校验。

## 非功能

- 无新增运行时依赖；复用现有 Vue/Vite 工具链。
- 保持静态镜像（`prototypes/index.html`）与 `frontend/public/atlas-prototype.html` 同步。
- 可访问性：上传触发与操作为带标签的真实按钮；清单为带可读状态徽章的列表/表格。

## 待解问题

- `folderUploadMock` 现在是否应暴露 async 签名（以对齐未来适配器 Promise）还是保持同步。建议：async 形态函数返回已解析的 mock 数据，使后续适配器替换无需改视图。（在设计中确认。）

# 数据模型：Knowledge Space

## 状态

草稿。此中文 companion 记录当前 Phase 1 / Product Goal Batch 1 的前端 mock 数据模型要点。

## ProductSpace

| 字段 | 含义 |
|---|---|
| `id` | 稳定知识空间标识，例如 `ibm-i-modernization`。 |
| `name` | 展示名称。 |
| `description` | 卡片和详情页描述。 |
| `documents` | mock 文档数量。 |
| `reviews` | mock 审核数量。 |
| `owner` | 所有者/创建者展示文本。 |

## ProductView

| 值 | 含义 |
|---|---|
| `home` | 首页知识空间库。 |
| `chat` | Global Chat 工作区级对话。 |
| `space` | 知识空间详情页。 |

## SpaceTab

| 值 | 含义 |
|---|---|
| `docs` | Documents 标签。 |
| `review` | Processing Center 标签。历史命名保留为内部状态，UI 显示处理中心。 |
| `wiki` | LM Wiki 标签。默认值。 |
| `graph` | Knowledge Graph 标签。 |

## SettingsPanel

| 值 | 含义 |
|---|---|
| `general` | 常规设置。 |
| `members` | 成员管理。 |
| `models` | 模型管理。 |
| `vector` | 向量数据库引擎。 |
| `parser` | 解析引擎。 |
| `storage` | 存储引擎。 |

## VueModelConfig

| 字段 | 含义 |
|---|---|
| `id` | 模型配置标识。 |
| `category` | `chat`、`embedding`、`rerank`、`vision`、`speech`。 |
| `displayName` | UI 展示名。 |
| `provider` | provider 展示名。 |
| `source` | `Ollama` 或 `API`。 |
| `name` | 模型名称。 |
| `baseUrl` | mock Base URL。 |
| `apiKeyStatus` | `configured` 或 `not_configured`；不得保存明文 key。 |
| `supportsMultimodal` | 是否支持多模态。 |
| `thinkingFormat` | 思考参数格式。 |

## 未来数据边界

- 真实 Knowledge Space、Batch、File、Chunk、Wiki、Graph、Ask、Model config 应通过后端 API/adapter contract 接入。
- parser/converter/model/vector/storage/search 不得直接暴露到产品 UI。
- source trace、confidence、review status 是 Wiki、Graph、Ask 可用性的核心字段。

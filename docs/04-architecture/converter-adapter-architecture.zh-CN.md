# 架构：Converter Adapter

## 状态

草稿。Phase 3 adapter 切片。来源 spec：`docs/03-spec/converter-adapter-spec.md`。

## 概览

Converter adapter 切片在 Atlas metadata workflow 与具体转换引擎之间引入产品面的 Office-to-PDF 转换边界。`trinity-office` 被视为首个可替换 adapter implementation 目标；controller、service、repository 和 UI 代码只与 adapter registry 和 conversion contract 交互。

## 架构驱动因素

| 驱动 | 影响 |
|---|---|
| Adapter boundary | 产品工作流通过 registry 解析 converter，不直接调用 engine。 |
| Metadata continuity | Conversion 通过 metadata contract 写回 file status、PDF path、confidence、review status 和安全 error。 |
| Engine replaceability | Adapter capability metadata 和 configuration shape 允许后续 converter implementation 替换。 |
| Mock-engine verification | CI 使用 mock/fake adapter，不依赖真实 `trinity-office` binary。 |
| Secret/path safety | Configuration 和 error 脱敏；持久化路径仅允许相对路径。 |

## 已验证现有上下文

- 现有 batch create 在 `backend/src/main/java/com/atlas/metadata/dto/CreateBatchRequest.java:18` 接收 inventory file metadata，包括 `sourcePath`、`sourceType`、`status`、`confidence`、`reviewStatus`、`pdfPath`、`markdownPath`、`assetsPath`、`errorMessage` 和 chunks。
- 现有 file item response 在 `backend/src/main/java/com/atlas/metadata/dto/FileItemResponse.java:9` 暴露 `pdfPath`、status、confidence 和 review status。
- 现有 file status 在 `backend/src/main/java/com/atlas/metadata/enums/FileStatus.java:6` 包含 `PDF_CONVERTED`、`PDF_CONVERT_FAILED`、`OCR_REQUIRED`、`FAILED` 和 `UNSUPPORTED`。
- 现有 adapter guard 在 `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:11` 保持 Phase 2 adapter package 为空并阻断 engine references；本切片将该规则改为只允许 adapter implementation 边界内出现 engine-specific references。

## 系统上下文

```text
+-------------------------+
| Atlas user / workflow   |
| batch conversion action |
+-----------+-------------+
            |
            v
+-------------------------+
| Spring Boot API         |
| conversion run endpoints|
+-----------+-------------+
            |
            v
+-------------------------+
| Conversion Service      |
| validation + state      |
+-----------+-------------+
            |
            v
+-------------------------+       +-------------------------+
| Converter Adapter       |------>| trinity-office wrapper  |
| Registry + Interface    |       | or mock/fake engine     |
+-----------+-------------+       +-------------------------+
            |
            v
+-------------------------+
| Metadata Persistence    |
| file status + reports   |
+-------------------------+
```

## 组件拆分

| 组件 | 职责 |
|---|---|
| Converter API boundary | 暴露 adapter capabilities、conversion run 创建和 run report 的内部端点。 |
| Converter application service | 校验 batch/file targets、解析 adapter、管理 run state、映射 adapter results、写 metadata。 |
| Converter adapter registry | 保存已配置 converter adapters，并返回带脱敏配置的 capability metadata。 |
| Converter adapter interface | 产品面契约，把 source file metadata 转换为 PDF conversion results。 |
| Trinity-office adapter implementation | 位于 interface 后的可选具体 adapter；产品层不得直接依赖它。 |
| Mock/fake converter engine | 单元/集成测试和 CI 使用的确定性 test adapter。 |
| Conversion persistence | 记录 run summary 和逐文件 result；更新现有 file item metadata。 |
| Seam guard tests | 证明具体 engine names 和 command execution boundaries 只存在于 adapter implementation package。 |

## 层边界

- **Controller layer：** 接收 JSON request 并返回 envelope。不得执行命令、读取本地文件字节或引用 `trinity-office`。
- **Application/service layer：** 拥有 validation、state transition 和 adapter registry 使用。依赖 converter interface，而非 concrete engine。
- **Adapter layer：** 拥有 engine-specific invocation，包括 fake/mock runner 和可选 `trinity-office` wrapper。
- **Repository layer：** 持久化 conversion run/result metadata 和 file item updates。不得执行 converter。
- **Worker/execution boundary：** 首个切片可保持 in-process，但必须能后续切换为 external worker。

## 集成架构

| 集成 | 模式 | 边界 |
|---|---|---|
| Metadata API file item model | Internal service/repository update | 保留现有 status 和相对 artifact path。 |
| `trinity-office` | 仅 adapter implementation | 配置门控、可替换、CI 不要求真实存在。 |
| Mock converter | 测试用 adapter implementation | 确定性 success/failure/OCR/unsupported 行为。 |
| Future storage adapter | 延后 | 本切片记录相对 artifact path，但不存储字节。 |
| Future parser adapter | 延后 | 后续切片消费已转换 PDF。 |

## 状态策略

Conversion run state 与 file status 分离。File status 仍是 `docs/batch-processing-design.md` 定义的产品可见生命周期；conversion run status 记录执行生命周期与摘要。

```text
REQUESTED -> RUNNING -> SUCCEEDED
                      -> PARTIAL_FAILED
                      -> FAILED
```

## 安全与数据保护

- 测试或 seed data 中无真实公司文档或私有路径。
- 不向用户返回原始命令输出。
- Config summary 仅脱敏/状态化展示。
- 持久化前路径必须是相对且无目录穿越。
- 日志可包含 run id、batch id、adapter key 和安全 status；不得包含原始源内容、完整本地路径或凭据。

## Architecture Review Notes

本 SDD pass 已应用 architecture-review 标准：

- **Decoupling：** 产品层依赖 adapter contract，而非 concrete engine call。
- **Configuration externalization：** adapter enablement 与 command details 由配置驱动并脱敏。
- **Error handling：** 跨边界错误为用户安全摘要。
- **Immutability/audit：** conversion run/result record 是 append-oriented evidence；file item status update 是明确 state transition。

## 风险与权衡

| 风险 | 缓解 |
|---|---|
| 真实 `trinity-office` command contract 尚未确认。 | 测试使用 fake command runner；真实 wrapper 在 OQ-CA-003 回答前保持配置门控。 |
| In-process adapter 未来可能需要 external worker 隔离。 | 保持 adapter interface 和 run contract 独立于 runtime topology。 |
| PDF pass-through 策略依赖 storage-adapter。 | 记录 OQ-CA-002，并在 API guide 中明确 path 语义。 |

## 待确认问题

- OQ-CA-001：local process wrapper vs external worker。
- OQ-CA-002：PDF pass-through copy vs reference policy。
- OQ-CA-003：确切 `trinity-office` command-line contract。

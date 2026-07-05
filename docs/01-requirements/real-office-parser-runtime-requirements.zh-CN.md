# 需求：真实 Office 解析 Runtime

## 状态

供用户审阅的草稿。产品代码必须等待用户明确接受。

## 切片契约

- **Slice:** `real-office-parser-runtime`
- **Wave:** Wave 2 / Runtime Integration
- **Goal:** 在现有 Atlas converter/parser adapter 边界之后，启用受控的内部 Office conversion 和 PDF parsing runtime execution。
- **Maturity target:** SDD-ready runtime integration slice。不代表 production readiness、RBAC、connector sync、worker scale-out 或不受限制的真实数据摄取。

## 来源上下文

本切片建立在已接受并实现的 adapter foundation 上：

- `converter-adapter` 已定义 `ConverterAdapter`、capability metadata、conversion runs、安全 status mapping，以及 mock `trinity-office` adapter。
- `parser-adapter` 已定义 `ParserAdapter`、capability metadata、parser runs、安全 status mapping、source chunks，以及 mock `document-normalize` adapter。
- 现有可选 real wrapper 当前只暴露脱敏 capability status，不执行真实工具。
- `wiki-ingest-v0` 和 `wiki-linkify-lint` 只在 review-safe processing 后消费已批准或生成 metadata。

## 需求

| ID | Requirement | Priority | Source |
|---|---|---|---|
| REQ-REAL-OFFICE-PARSER-RUNTIME-001 | 用户接受本双语 SDD 前，implementation 必须阻塞。 | Must | Goal-driven SDD gate |
| REQ-REAL-OFFICE-PARSER-RUNTIME-002 | Atlas 必须只通过现有 product-facing converter adapter contract 启用真实 `trinity-office` conversion。 | Must | REQ-PROD-015, REQ-PROD-016 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-003 | Atlas 必须只通过现有 product-facing parser adapter contract 启用真实 `document-normalize` parsing。 | Must | REQ-PROD-015, REQ-PROD-016 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-004 | Product controllers、services、repositories、domains、frontend code、Wiki/Ask/Graph workflows 不得直接调用 runtime commands、SDK、HTTP clients 或 tool-specific APIs。 | Must | Adapter boundary |
| REQ-REAL-OFFICE-PARSER-RUNTIME-005 | Runtime selection 必须由配置门控，并暴露安全 capability metadata：`AVAILABLE`、`DISABLED` 或 `MISCONFIGURED`，以及脱敏的 status-only config summaries。 | Must | Adapter capability pattern |
| REQ-REAL-OFFICE-PARSER-RUNTIME-006 | 自动化验证必须在未安装真实 binaries 时继续通过，使用 mocks/fakes；真实 runtime execution 必须通过配置或 test harness opt-in。 | Must | CI safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-007 | Runtime inputs/outputs 在仓库测试和 fixtures 中只能使用 mock/sample-safe files；不得提交真实公司文档、日志、私有路径或机密截图。 | Must | Data safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-008 | Runtime 输入输出必须保持在安全相对 artifact roots 内，并拒绝 traversal、absolute paths、URI-prefixed paths、hostnames 和 API response 中的 raw local paths。 | Must | Path safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-009 | Runtime stdout/stderr、exception messages 和 adapter safe messages 必须在持久化或 API response 前脱敏。 | Must | Secret/path safety |
| REQ-REAL-OFFICE-PARSER-RUNTIME-010 | Conversion runtime results 只能映射到已接受 file statuses，例如 `PDF_CONVERTED`、`PDF_CONVERT_FAILED`、`OCR_REQUIRED`、`FAILED`、`UNSUPPORTED`。 | Must | Converter contract |
| REQ-REAL-OFFICE-PARSER-RUNTIME-011 | Parser runtime results 只能映射到已接受 file statuses，例如 `MARKDOWN_GENERATED`、`LOW_CONFIDENCE`、`OCR_REQUIRED`、`FAILED`、`UNSUPPORTED`。 | Must | Parser contract |
| REQ-REAL-OFFICE-PARSER-RUNTIME-012 | Parser runtime output 必须保留 source trace、PDF path、Markdown path、assets path、confidence、parser key、source chunks 和 review status。 | Must | Trace/review rules |
| REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Runtime integration 不得自动 publish Wiki pages、自动 approve review-required content、调用 LLM、创建 graph edges 或写 Ask indexes。 | Must | Trust boundary |
| REQ-REAL-OFFICE-PARSER-RUNTIME-014 | Runtime failure、timeout、missing binary、invalid output 和 partial file failure 必须生成有界安全 run evidence，并保持无关 metadata 不变。 | Must | Reliability |
| REQ-REAL-OFFICE-PARSER-RUNTIME-015 | Adapter seam guard 和 safety scans 必须证明 tool-specific names 与 command execution APIs 只出现在允许的 adapter/runtime packages 和 tests 中。 | Must | Static guard |
| REQ-REAL-OFFICE-PARSER-RUNTIME-016 | SDD 必须记录 open runtime contract questions，包括 command invocation shape、timeout policy、artifact materialization、local process vs worker topology。 | Must | Architecture readiness |

## 明确排除

- 生产 authentication、RBAC、audit policy、secret-manager integration 和 rate limiting。
- 外部 cloud parsing 或 model calls。
- Connector sync、大规模 worker queue、dead-letter queue 和 operations runbook。
- 仓库测试或 fixtures 中的真实公司文档摄取。
- Wiki review gate、refresh/retract automation、graph extraction 和 trusted Ask indexing。

## 接受标准

| Requirement | Observable acceptance |
|---|---|
| REQ-REAL-OFFICE-PARSER-RUNTIME-001 | Traceability 在 implementation 开始前记录用户接受。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-002 to 004 | Spec/design/tasks 要求所有 runtime execution 通过 adapter interfaces 和 static guard tests。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-005 to 006 | API guide 与 tasks 定义 masked capability behavior 和 mock-safe CI verification。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-007 to 009 | Tasks 包含 focused secret/private-path 和 command-output sanitization checks。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-010 to 012 | Spec 定义 conversion/parsing 的 status mapping 与 trace/review preservation。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-013 | Out-of-scope 和 state model 禁止 publish/trust/Ask/Graph side effects。 |
| REQ-REAL-OFFICE-PARSER-RUNTIME-014 to 016 | Data flow/design/tasks 包含 failure handling、seam guard 和 open questions。 |

## 假设

- 第一实现目标仍是 Java/Spring Boot backend adapter code。
- 真实 binaries 或 workers 是可选且配置门控的；缺少 binaries 不得让正常 CI 失败。
- 除非实现发现并记录 mismatch，否则复用现有 conversion 和 parser API shape。

## 开放问题

| ID | Question | Owner |
|---|---|---|
| OQ-REAL-OFFICE-PARSER-RUNTIME-001 | 首个 runtime topology 应采用 local process execution、sidecar worker，还是 remote internal worker endpoint？ | Architecture |
| OQ-REAL-OFFICE-PARSER-RUNTIME-002 | 已批准的 `trinity-office` command contract 是什么，包括 input/output directory expectations？ | Platform |
| OQ-REAL-OFFICE-PARSER-RUNTIME-003 | 已批准的 `document-normalize` contract 是什么，包括 Markdown/assets/report output layout？ | Platform |
| OQ-REAL-OFFICE-PARSER-RUNTIME-004 | 受控内部 runs 应使用什么 timeout 和 max-output policy？ | Platform / Security |

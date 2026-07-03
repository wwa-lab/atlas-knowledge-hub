# API 实现指南：Metadata API

## 状态

草稿。**必需，且必须在后端实现前被接受**（REQ-PROD-073）。Phase 2。切片 `metadata-api`。正式化 `docs/05-design/contracts/knowledge-space-API_IMPLEMENTATION_GUIDE.md` 中勾勒的候选端点。

## 原则

- 每个端点统一响应信封（成功与错误）。
- 所有内容响应保留 source trace、confidence 与 review status。
- 仅用户安全错误——无堆栈、SQL、secret、主机名或绝对路径。
- 所有响应仅相对路径。
- 仅 metadata：无端点触发转换、解析、存储、模型或向量工作。
- 本切片无 auth（仅内部）；不得按现状公开暴露。

## 约定

- 基路径：`/api`。仅 JSON（`application/json`）。
- 分页：`?page=<0 起>&size=<1..200，默认 20>`。列表响应含 `meta`。
- 过滤：文件列表 `?status=<enum>`；space 列表 `?status=<space_status>`。
- ID 为不透明字符串。时间戳为 ISO-8601 UTC（`timestamptz`）。

## 信封

```json
{ "success": true, "data": {}, "error": null, "meta": { "page": 0, "size": 20, "total": 42 } }
```

错误：

```json
{ "success": false, "data": null,
  "error": { "code": "VALIDATION_ERROR", "message": "Invalid request.",
             "fields": { "name": "must not be blank" },
             "timestamp": 1750000000000, "path": "/api/spaces" },
  "meta": null }
```

`code` ∈ `VALIDATION_ERROR`（400）· `NOT_FOUND`（404）· `CONFLICT`（409）· `INTERNAL_ERROR`（500）。`timestamp`（epoch 毫秒）与 `path` 仅在错误响应出现（镜像 Spring `DefaultErrorAttributes`）；成功响应省略。

## 端点

### Knowledge Space

#### `GET /api/spaces`
列出 space 卡片。查询：`page`、`size`、`status?`。
```json
{ "success": true,
  "data": [ { "id": "ibm-i-modernization", "name": "IBM i Modernization",
              "description": "…", "owner": "Platform Team", "status": "HEALTHY",
              "documentCount": 42, "wikiPageCount": 12, "reviewCount": 3,
              "createdAt": "2026-06-01T09:00:00Z", "updatedAt": "2026-06-20T14:30:00Z" } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 5 } }
```

#### `GET /api/spaces/{spaceId}`
space 详情。未知则 `404 NOT_FOUND`。`data` 为单个 space 对象（同上）。

#### `POST /api/spaces`
创建 space。
请求：
```json
{ "name": "IBM i Modernization", "description": "…",
  "type": "document", "indexStrategy": "rag", "owner": "Platform Team" }
```
校验：`name` 非空 ≤200；`type` ∈ {`document`,`faq`}；`indexStrategy` ∈ {`rag`,`wiki`}。
响应 `201`：含已创建 space 的信封（服务端设 `id`、`status=HEALTHY`、`createdAt`、`updatedAt`）。
错误：`400 VALIDATION_ERROR`。

### 批次

#### `GET /api/spaces/{spaceId}/batches`
列出某 space 的批次及**派生** metrics。查询：`page`、`size`。
```json
{ "success": true,
  "data": [ { "id": "batch-2026-06-20-001", "spaceId": "ibm-i-modernization",
              "name": "Discovery Package", "sourceKind": "folder",
              "owner": "Delivery Lead", "uploadedAt": "2026-06-20T14:30:00Z",
              "metrics": { "total": 24, "pdfConverted": 20, "markdownGenerated": 18,
                           "reviewRequired": 6, "failed": 1, "unsupported": 2 } } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 3 } }
```

#### `GET /api/batches/{batchId}`
批次详情及派生 metrics。未知则 `404`。

#### `POST /api/spaces/{spaceId}/batches`
从**预先算好的 inventory metadata** 创建批次 + file item。不触碰字节/引擎。
请求：
```json
{ "name": "Discovery Package", "sourceKind": "folder", "owner": "Delivery Lead",
  "files": [
    { "sourcePath": "Discovery/BRD/BRD.docx", "sourceType": "docx",
      "status": "MARKDOWN_GENERATED", "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED",
      "chunks": [ { "sourceFile": "BRD.docx", "page": 12, "section": "Scope",
                    "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED" } ] },
    { "sourcePath": "Discovery/notes.txt", "sourceType": "unsupported",
      "status": "UNSUPPORTED", "confidence": 0, "reviewStatus": "REVIEW_REQUIRED" }
  ] }
```
校验：`sourceKind` ∈ {`folder`,`zip`}；每个文件 `sourcePath` 相对且无穿越；`status` ∈ `FileStatus`；`confidence` ∈ [0,1]。generated/低置信度文件默认 `reviewStatus=REVIEW_REQUIRED`；服务端拒绝创建时的 `APPROVED`。
响应 `201`：含派生 metrics 的已创建批次。错误：`400 VALIDATION_ERROR`、`404`（未知 space）。

### File Item

#### `GET /api/batches/{batchId}/files`
列出 file item。查询：`page`、`size`、`status?`。
```json
{ "success": true,
  "data": [ { "id": "file-003", "batchId": "batch-2026-06-20-001",
              "sourcePath": "Discovery/BRD/BRD.docx", "sourceType": "docx",
              "status": "MARKDOWN_GENERATED", "confidence": 0.82,
              "reviewStatus": "REVIEW_REQUIRED",
              "pdfPath": "generated/pdf/BRD.pdf", "markdownPath": "generated/md/BRD.md",
              "assetsPath": "generated/assets/BRD/", "errorMessage": null } ],
  "error": null, "meta": { "page": 0, "size": 20, "total": 24 } }
```

#### `GET /api/files/{fileId}`
file item 详情。未知则 `404`。

#### `GET /api/files/{fileId}/chunks`
某文件的 source chunk（溯源记录）。
```json
{ "success": true,
  "data": [ { "id": "chunk-file-003-p12-b02", "fileItemId": "file-003",
              "sourceFile": "BRD.docx", "page": 12, "section": "Scope",
              "confidence": 0.82, "reviewStatus": "REVIEW_REQUIRED" } ],
  "error": null, "meta": null }
```

### Review

#### `POST /api/files/{fileId}/reviews`
追加不可变 review 记录；更新目标 `reviewStatus`。
请求：
```json
{ "action": "NEED_FIX", "reviewer": "sme.alex",
  "comment": "Terminology mismatch in section 3.",
  "affectedChunks": ["chunk-file-003-p12-b02"] }
```
校验：`action` ∈ {`APPROVE`,`NEED_FIX`,`OCR_REQUIRED`}；`reviewer` 非空。
效果：`APPROVE→APPROVED`、`NEED_FIX→NEED_FIX`、`OCR_REQUIRED→OCR_REQUIRED`。此处绝不设 `PUBLISHED`。
响应 `201`：含服务端 `createdAt` 的已创建 review 记录。

#### `GET /api/files/{fileId}/reviews`
只追加历史，按时间顺序。
```json
{ "success": true,
  "data": [ { "id": 1001, "targetType": "file", "targetId": "file-003",
              "action": "NEED_FIX", "reviewer": "sme.alex",
              "comment": "…", "affectedChunks": ["chunk-file-003-p12-b02"],
              "createdAt": "2026-06-21T10:15:00Z" } ],
  "error": null, "meta": null }
```

## 延后端点（范围外 —— REQ-MA-014）

表存在且已种子化，但本切片**不暴露端点**：

- Wiki 页（`review-publish` 切片）—— `GET /api/spaces/{id}/wiki-pages`、发布转换。
- 图谱（`knowledge-graph` 切片）—— `GET /api/spaces/{id}/graph`。
- Ask（`ask-rag` 切片）—— `POST /api/spaces/{id}/ask`。
- 模型/引擎/成员/注册管理——各自后续切片。

## 契约测试预期

每个端点有契约测试，断言：正确状态码；信封形态（`success`/`data`/`error`/`meta`）；列表上分页 `meta`；坏输入的 `400` 字段级校验；未知 id 的 `404`；用户安全错误体（无堆栈/SQL/secret/绝对路径）；所有响应相对路径；生成内容绝不默认返回 `APPROVED`。在 `mvn verify` 下运行。

## 安全与 secret 处理

- datasource 凭证来自外部化配置（`${ATLAS_DB_*}`），绝不提交字面量。
- 无端点返回原始 secret；任何未来 key/config 字段为 status-only（`configured`/`not_configured`）。
- 本切片无 auth——仅内部；RBAC 延后至 Phase 4。不得按现状公开部署。

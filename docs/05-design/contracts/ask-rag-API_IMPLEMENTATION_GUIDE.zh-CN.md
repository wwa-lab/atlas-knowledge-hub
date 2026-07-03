# Ask RAG — API 实现 Guide

## 状态

草稿。Phase 4 `ask-rag` 新增 backend/API 行为，因此本 guide 必需。

## 基础约定

- Base path：`/api`。
- Envelope：`ApiEnvelope<T>`，包含 `success`、`data`、`error` 和可选 `meta`。
- Auth/RBAC：本切片仅内部/mock guard，除非单独 security slice 接受完整生产 enforcement。
- Mode：自动化测试使用 `mock`。
- Secrets：仅 masked/status-only；绝不返回 raw credential、endpoint、provider payload、private path、raw prompt、raw source text、raw vector 或 stack trace。

## `POST /api/spaces/{spaceId}/ask`

目的：运行一个 scoped Trusted Ask request。

请求：

```json
{
  "question": "Explain what Source Trace means in the modernization workflow.",
  "requestedBy": "delivery-lead",
  "reviewPolicy": "APPROVED_ONLY",
  "limit": 5,
  "mode": "mock",
  "filters": {
    "fileItemIds": ["file-003"],
    "sourceTypes": ["pdf", "pptx"]
  }
}
```

校验：

- `spaceId` 必须引用现有 space。
- `question` 必填、trimmed 且有长度边界。
- `requestedBy` 必填，用于 audit。
- `reviewPolicy` 为 `APPROVED_ONLY` 或 `INCLUDE_REVIEW_REQUIRED`；默认 `APPROVED_ONLY`。
- `limit` 为正数且有边界。
- filters 如存在，必须限定在选定 space 内。
- 测试中的 request text 与 filters 不得包含 raw credential、private path、作为 provider endpoint 的 URL 或真实公司内容。

带回答的成功响应：

```json
{
  "success": true,
  "data": {
    "runId": "ask-run-20260703-001",
    "spaceId": "ibm-i-modernization",
    "status": "SUCCEEDED",
    "question": "Explain what Source Trace means in the modernization workflow.",
    "answer": "Source Trace is the evidence chain that links Atlas answers back to approved Wiki sections and source chunks.",
    "answerConfidence": 0.86,
    "answerReviewStatus": "REVIEW_REQUIRED",
    "reviewPolicy": "APPROVED_ONLY",
    "modelRunId": "model-run-20260703-001",
    "safeMessage": "Answer generated from approved evidence.",
    "evidence": [
      {
        "sourceChunkId": "chunk-file-003-p12-b02",
        "fileItemId": "file-003",
        "sourceFile": "BRD_Methodology.pdf",
        "page": 12,
        "section": "Source Trace",
        "score": 0.92,
        "confidence": 0.91,
        "reviewStatus": "APPROVED",
        "safeExcerptLabel": "BRD page 12 / Source Trace"
      }
    ],
    "createdAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:02Z"
  },
  "error": null,
  "meta": null
}
```

No-evidence 响应：

```json
{
  "success": true,
  "data": {
    "runId": "ask-run-20260703-002",
    "spaceId": "ibm-i-modernization",
    "status": "NO_EVIDENCE",
    "question": "Which unapproved document changed the policy?",
    "answer": "No approved evidence was found for this question. Review or publish relevant evidence before using Trusted Ask.",
    "answerConfidence": null,
    "answerReviewStatus": "REVIEW_REQUIRED",
    "reviewPolicy": "APPROVED_ONLY",
    "modelRunId": null,
    "safeMessage": "No approved evidence found.",
    "evidence": []
  },
  "error": null,
  "meta": null
}
```

## `GET /api/ask-runs/{runId}`

目的：返回持久化 Ask run detail 与 evidence。

响应形状：与 `POST /api/spaces/{spaceId}/ask` 的 `data` body 相同。

错误：

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Invalid request payload、policy、limit 或 unsafe filter。 |
| 404 | `NOT_FOUND` | Unknown space 或 Ask run。 |
| 409 | `CONFLICT` | 如实现同 scope 活跃 Ask run 冲突检测，则冲突时返回。 |
| 500 | `INTERNAL_ERROR` | 非预期安全 server fault；响应不含 raw details。 |

## 内部编排契约

```text
AskService
  validate request and scope
  create AskRun REQUESTED
  query vector evidence through product-facing vector contract
  if no approved evidence: complete NO_EVIDENCE without model call
  create model run through product-facing model contract
  persist AskEvidence rows and final AskRun status
  return safe Ask response
```

## Contract Tests

实现必须运行：

```bash
cd backend && mvn -Dtest=AskServiceTest,AskSummaryCalculatorTest test
cd backend && mvn -Dit.test=AskApiContractIT verify
cd frontend && npm run typecheck
cd frontend && npm run test
cd frontend && npm run build
cd frontend && npm run e2e
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|pgvector|Milvus|Qdrant|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src frontend/src docs/01-requirements/ask-rag-requirements.md docs/02-user-stories/ask-rag-stories.md docs/03-spec/ask-rag-spec.md docs/04-architecture/ask-rag-architecture.md docs/04-architecture/ask-rag-data-flow.md docs/04-architecture/ask-rag-data-model.md docs/05-design/ask-rag-design.md docs/05-design/contracts/ask-rag-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/ask-rag-tasks.md
```

Seam scan 在 non-adapter product layers 中必须无匹配。Adapter implementation packages 仅在 guard tests 覆盖且 outbound network clients 不泄漏到产品层时，才可包含安全 provider labels。

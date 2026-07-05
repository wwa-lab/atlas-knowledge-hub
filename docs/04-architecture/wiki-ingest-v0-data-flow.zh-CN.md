# 数据流：Wiki Ingest v0

## 状态

供用户接受的草稿。

## End-To-End Flow

```text
┌──────────────┐
│ User/API     │
│ start run    │
└──────┬───────┘
       ▼
┌────────────────────────────┐
│ Wiki Ingest API             │
│ validate space + request    │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Input Selection             │
│ approved traced chunks only │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Candidate Builder           │
│ deterministic by default    │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Slug Merge                  │
│ create/update/conflict      │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Evidence Records            │
│ run + log + issue metadata  │
└──────┬─────────────────────┘
       ▼
┌────────────────────────────┐
│ Vue Status Display          │
│ generated review-required   │
└────────────────────────────┘
```

## Input Selection Flow

| Input | Rule | Output |
|---|---|---|
| Space id | 必须解析到 existing Knowledge Space。 | Run scoped to one space。 |
| Source chunks | 必须属于 selected space 且为 `APPROVED`。 | Eligible evidence set。 |
| Missing trace | 排除。 | Exclusion count 与 safe reason。 |
| Non-approved chunks | 排除。 | Exclusion count 与 safe reason。 |
| Failed/unsupported/OCR-required files | 排除。 | Exclusion count 与 safe reason。 |

## Candidate Build Flow

```text
Approved chunks
  -> group by deterministic topic key
  -> derive title and slug
  -> create generated Markdown artifact with safe summary
  -> collect sourceRefs and chunkRefs
  -> aggregate confidence
  -> set sourceMode=AUTO_GENERATED
  -> set reviewStatus=REVIEW_REQUIRED
  -> set refreshPolicy=ON_SOURCE_CHANGE
```

## Merge Flow

| Existing page state | Action |
|---|---|
| No same slug | 创建 review-required candidate。 |
| Same slug, `AUTO_GENERATED`, review-required | 合并 refs，并按已接受实现方案更新 version。 |
| Same slug, `PUBLISHED_FILE` or trusted published page | 不覆盖；记录 safe conflict issue。 |
| Same slug in another space | 无冲突；slug 按 space scoped。 |

## Run State Flow

```text
REQUESTED -> RUNNING -> SUCCEEDED
REQUESTED -> RUNNING -> PARTIAL_FAILED
REQUESTED -> RUNNING -> FAILED
REQUESTED -> CANCELLED
```

## Data Object Lifecycle

| Object | Created/Updated | Notes |
|---|---|---|
| `wiki_generation_run` | Run start 创建，finish 更新。 | 只存 safe summary 与 counts。 |
| `wiki_page` | 为 generated candidates 创建或更新。 | v0 中始终 review-required。 |
| `wiki_log_entry` | 为 safe lifecycle events 追加。 | 无 raw source text 或 provider payload。 |
| `wiki_page_issue` | 为 conflicts 或 excluded evidence 创建。 | 只存 safe evidence refs。 |

## Safety Filters

- 从 run/log/error summaries 中移除 raw source text。
- 只存 safe IDs、labels、counts、page/section locators 和 relative artifact refs。
- 永不存储 raw secret、raw prompt、raw provider response、private absolute path 或 stack trace。

## Adapter Flow

Default deterministic mode:

```text
Wiki Ingest Service -> local deterministic candidate builder -> repositories
```

Future model-assisted mode，本 slice 范围外：

```text
Wiki Ingest Service -> ModelAdapter -> safe summarized output -> review-required candidate
```

本 slice 不直接调用 parser、converter、model、vector、storage 和 search engines。

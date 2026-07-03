# Data Flow: Parser Adapter

## Status

Draft. Companion to `docs/04-architecture/parser-adapter-architecture.md`.

## End-To-End Flow

```text
1. Caller requests parser run for a batch.
2. Backend validates batch and target file ids.
3. Backend filters eligible files: status PDF_CONVERTED + safe pdfPath.
4. Parser registry resolves default or requested parser adapter.
5. Parser service creates parser_run and marks it RUNNING.
6. Parser adapter receives product-facing file metadata.
7. Mock/configured adapter returns per-file Markdown/assets/chunk outcomes.
8. Parser service validates paths, confidence, file ids, and chunk data.
9. Parser service writes file_item updates, source_chunk rows, parser_file_result rows.
10. Parser service completes parser_run and returns a report.
```

## Main Data Path

```text
+----------------+
| Request body   |
| adapterKey     |
| fileIds        |
| mode           |
+-------+--------+
        |
        v
+----------------+
| Batch + files  |
| metadata read  |
+-------+--------+
        |
        v
+----------------+
| Eligibility    |
| PDF_CONVERTED  |
| safe pdfPath   |
+-------+--------+
        |
        v
+----------------+
| ParserAdapter  |
| request        |
+-------+--------+
        |
        v
+----------------+
| ParserResult   |
| files/chunks   |
+-------+--------+
        |
        v
+-----------------------------+
| Validation + sanitization   |
| relative paths, confidence  |
| safe errors, file ownership |
+-------+---------------------+
        |
        v
+-----------------------------+
| Metadata writes             |
| file_item, source_chunk,    |
| parser_run, parser_result   |
+-----------------------------+
```

## Data Objects

| Object | Producer | Consumer | Notes |
|---|---|---|---|
| Parser run request | API caller | Parser service | Contains batch id from path, optional adapter key/file ids/mode/requestedBy. |
| Parser adapter request | Parser service | Parser adapter | Contains run id, batch id, file descriptors, artifact root hint, low-confidence threshold. |
| Parser result | Parser adapter | Parser service | Contains adapter key, per-file result, chunks, safe message. |
| Parser file result | Parser service | Database/report | Stores per-file status, markdown/assets paths, confidence, safe error. |
| Source chunk | Parser service | Database/file chunk API | Stores source file, page, section, confidence, review status. |
| Generated Markdown | Parser adapter / worker | Storage path referenced by metadata | Must include front matter and source trace blocks. |

## Field Mapping

| Parser Result Field | Metadata Target | Rule |
|---|---|---|
| `fileId` | `parser_file_result.fileItemId`, `file_item.id` lookup | Must target a requested file in the batch. |
| `status` | `file_item.status`, `parser_file_result.status` | Uses existing `FileStatus` values only. |
| `markdownPath` | `file_item.markdown_path`, `parser_file_result.markdownPath` | Safe relative path required for generated Markdown. |
| `assetsPath` | `file_item.assets_path`, `parser_file_result.assetsPath` | Optional safe relative path. |
| `confidence` | `file_item.confidence`, `source_chunk.confidence`, `parser_file_result.confidence` | Decimal in `[0,1]`, rounded consistently by implementation. |
| `chunks[].page` | `source_chunk.page` | Positive integer when supplied. |
| `chunks[].section` | `source_chunk.section` | User-safe section label, not raw parser log. |
| `safeError` | `file_item.error_message`, `parser_file_result.safeError` | Sanitized and bounded. |

## Failure Flow

```text
+-----------------------+
| Adapter unavailable   |
+-----------+-----------+
            |
            v
  parser_run FAILED
  file_item unchanged

+-----------------------+
| Unsafe parser result  |
+-----------+-----------+
            |
            v
  validation error
  no unsafe metadata persisted

+-----------------------+
| Per-file parse fail   |
+-----------+-----------+
            |
            v
  file_item FAILED
  safeError only
  report includes failed count
```

## Review And Trace Preservation

- File-level `review_status` remains `REVIEW_REQUIRED` for generated/low-confidence/OCR-required output.
- Source chunks default to `REVIEW_REQUIRED`.
- Markdown source trace blocks use the same source file/PDF/page/section/chunk evidence as persisted source chunks.
- Parser-adapter does not mark content `APPROVED` or `PUBLISHED`.

## Verification Hooks

- Mock parser tests validate all outcome branches.
- Seam guard verifies no direct parser engine reference outside adapter package.
- Secret/path scans cover parser SDD docs and backend implementation paths.

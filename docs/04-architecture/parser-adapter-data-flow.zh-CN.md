# 数据流：Parser Adapter

## 状态

草稿。`docs/04-architecture/parser-adapter-architecture.md` 的配套文档。

## 端到端流程

```text
1. Caller 为一个 batch 请求 parser run。
2. Backend 校验 batch 与目标 file ids。
3. Backend 过滤符合条件文件：status PDF_CONVERTED + safe pdfPath。
4. Parser registry 解析默认或指定 parser adapter。
5. Parser service 创建 parser_run 并标记 RUNNING。
6. Parser adapter 接收产品侧 file metadata。
7. Mock/configured adapter 返回逐文件 Markdown/assets/chunk outcomes。
8. Parser service 校验 paths、confidence、file ids、chunk data。
9. Parser service 写入 file_item updates、source_chunk rows、parser_file_result rows。
10. Parser service 完成 parser_run 并返回 report。
```

## 主数据路径

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

## 数据对象

| Object | Producer | Consumer | 说明 |
|---|---|---|---|
| Parser run request | API caller | Parser service | 从 path 获取 batch id，body 含可选 adapter key/file ids/mode/requestedBy。 |
| Parser adapter request | Parser service | Parser adapter | 含 run id、batch id、file descriptors、artifact root hint、low-confidence threshold。 |
| Parser result | Parser adapter | Parser service | 含 adapter key、逐文件 result、chunks、safe message。 |
| Parser file result | Parser service | Database/report | 存储逐文件 status、markdown/assets paths、confidence、safe error。 |
| Source chunk | Parser service | Database/file chunk API | 存储 source file、page、section、confidence、review status。 |
| Generated Markdown | Parser adapter / worker | metadata 引用的 storage path | 必须包含 front matter 与 source trace blocks。 |

## 字段映射

| Parser Result Field | Metadata Target | 规则 |
|---|---|---|
| `fileId` | `parser_file_result.fileItemId`、`file_item.id` lookup | 必须指向请求 batch 中的目标文件。 |
| `status` | `file_item.status`、`parser_file_result.status` | 只使用现有 `FileStatus` 值。 |
| `markdownPath` | `file_item.markdown_path`、`parser_file_result.markdownPath` | 生成 Markdown 需要安全相对路径。 |
| `assetsPath` | `file_item.assets_path`、`parser_file_result.assetsPath` | 可选安全相对路径。 |
| `confidence` | `file_item.confidence`、`source_chunk.confidence`、`parser_file_result.confidence` | `[0,1]` decimal，由实现一致舍入。 |
| `chunks[].page` | `source_chunk.page` | 提供时必须为正整数。 |
| `chunks[].section` | `source_chunk.section` | User-safe section label，不是 raw parser log。 |
| `safeError` | `file_item.error_message`、`parser_file_result.safeError` | 脱敏且有界。 |

## 失败流

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

## Review 与 Trace 保留

- Generated/low-confidence/OCR-required output 的 file-level `review_status` 保持 `REVIEW_REQUIRED`。
- Source chunks 默认 `REVIEW_REQUIRED`。
- Markdown source trace blocks 使用与持久化 source chunks 相同的 source file/PDF/page/section/chunk evidence。
- Parser-adapter 不将内容标记为 `APPROVED` 或 `PUBLISHED`。

## 验证钩子

- Mock parser tests 验证全部 outcome branches。
- Seam guard 验证 adapter package 外无 direct parser engine reference。
- Secret/path scans 覆盖 parser SDD docs 与 backend implementation paths。

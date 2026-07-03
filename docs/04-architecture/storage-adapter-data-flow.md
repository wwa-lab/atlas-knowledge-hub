# Data Flow: Storage Adapter

## Status

Draft. Companion to `docs/04-architecture/storage-adapter-architecture.md`.

## End-To-End Flow

```text
1. Caller requests a storage operation set for a batch.
2. Backend validates workspace/batch and any target file ids.
3. Backend validates each operation: layer, safe relative key, namespace scope.
4. Storage registry resolves default or requested storage adapter.
5. Storage service creates storage_operation and marks it RUNNING.
6. Storage adapter receives product-facing storage requests (put/delete).
7. Mock/configured adapter returns per-object descriptors or safe failures.
8. Storage service validates keys, sizes, checksums, and namespace scope.
9. Storage service persists storage_object descriptors and updates file_item pointers.
10. Storage service completes storage_operation and returns a record.
```

## Main Data Path

```text
+----------------+
| Request body   |
| adapterKey     |
| operations[]   |
| layer/key/ref  |
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
| Validation     |
| layer + key    |
| namespace scope|
+-------+--------+
        |
        v
+----------------+
| StorageAdapter |
| request        |
+-------+--------+
        |
        v
+----------------+
| Descriptor     |
| key/size/hash  |
+-------+--------+
        |
        v
+-----------------------------+
| Validation + sanitization   |
| relative keys, safe errors  |
| namespace ownership         |
+-------+---------------------+
        |
        v
+-----------------------------+
| Metadata writes             |
| storage_object,             |
| storage_operation,          |
| file_item pointer update    |
+-----------------------------+
```

## Data Objects

| Object | Producer | Consumer | Notes |
|---|---|---|---|
| Storage operation request | API caller | Storage service | Contains batch id from path, optional adapter key, and an operations list. |
| Storage adapter request | Storage service | Storage adapter | Contains operation id, batch scope, layer, safe relative key, content type, content reference. |
| Stored-object descriptor | Storage adapter | Storage service | Contains layer, key, size, checksum, content type, adapter key, status, safe error. |
| Storage operation record | Storage service | Database/report | Stores operation type, layer, counts, total bytes, status, safe message. |
| File item pointer update | Storage service | Database/file API | Records the returned key into `pdf_path` / `markdown_path` / `assets_path` or a report/wiki pointer. |
| Stored object bytes | Storage adapter / engine | Storage engine (behind adapter) | Never persisted in metadata; exchanged via content reference/stream. |

## Field Mapping

| Storage Result Field | Metadata Target | Rule |
|---|---|---|
| `layer` | `storage_object.layer` | One of `raw`, `pdf`, `markdown`, `assets`, `reports`, `wiki`. |
| `objectKey` | `storage_object.objectKey`, `file_item` pointer field | Safe relative key within the workspace/batch namespace. |
| `sizeBytes` | `storage_object.sizeBytes`, `storage_operation.totalBytes` | Non-negative integer. |
| `checksum` | `storage_object.checksum` | Non-empty for `STORED` descriptors. |
| `contentType` | `storage_object.contentType` | User-safe MIME/type label. |
| `status` | `storage_object.status`, `storage_operation` summary | `STORED` / `DELETED` / `MISSING` / `FAILED`. |
| `fileId` (when present) | `file_item` pointer field target | Must reference a file in the requested batch. |
| `safeError` | `storage_object.safeError`, `storage_operation.safeMessage` | Sanitized and bounded. |

## Failure Flow

```text
+-----------------------+
| Adapter unavailable   |
+-----------+-----------+
            |
            v
  storage_operation FAILED
  file_item pointers unchanged

+-----------------------+
| Unsafe key / layer    |
+-----------+-----------+
            |
            v
  validation error
  no object written or deleted

+-----------------------+
| Per-object op fail    |
+-----------+-----------+
            |
            v
  storage_object FAILED
  safeError only
  record includes failed count
```

## Review And Trace Preservation

- `file_item.review_status`, `status`, and `confidence` remain unchanged by storage operations unless a workflow explicitly maps a change.
- Stored-object descriptors record layer/key/checksum evidence but never store raw document bytes.
- Pointer write-back links a stored object to its file item without altering source trace.
- Storage-adapter does not mark content `APPROVED` or `PUBLISHED`.

## Verification Hooks

- Mock storage tests validate all operation branches (store, get/exists, list, delete) and failure paths.
- Seam guard verifies no direct storage-engine reference outside the adapter package, and that the adapter-scope `S3` exception is applied only to the storage adapter package.
- Secret/path scans cover storage SDD docs and backend implementation paths.

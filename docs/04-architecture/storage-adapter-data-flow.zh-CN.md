# 数据流：Storage Adapter

## 状态

草稿。`docs/04-architecture/storage-adapter-architecture.md` 的配套文档。

## 端到端流程

```text
1. 调用方为某批次请求 storage 操作集。
2. 后端校验 workspace/batch 及任何目标 file id。
3. 后端校验每个操作：layer、安全相对 key、命名空间 scope。
4. Storage registry 解析默认或请求的 storage adapter。
5. Storage service 创建 storage_operation 并标记为 RUNNING。
6. Storage adapter 接收产品侧 storage 请求（put/delete）。
7. mock/configured adapter 返回逐对象 descriptor 或安全失败。
8. Storage service 校验 key、size、checksum 与命名空间 scope。
9. Storage service 持久化 storage_object descriptor 并更新 file_item 指针。
10. Storage service 完成 storage_operation 并返回记录。
```

## 主数据路径

```text
+----------------+
| 请求体         |
| adapterKey     |
| operations[]   |
| layer/key/ref  |
+-------+--------+
        |
        v
+----------------+
| 读取 batch +   |
| files metadata |
+-------+--------+
        |
        v
+----------------+
| 校验           |
| layer + key    |
| 命名空间 scope |
+-------+--------+
        |
        v
+----------------+
| StorageAdapter |
| 请求           |
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
| 校验 + 脱敏                  |
| 相对 key、安全错误          |
| 命名空间归属                |
+-------+---------------------+
        |
        v
+-----------------------------+
| Metadata 写入               |
| storage_object,             |
| storage_operation,          |
| file_item 指针更新          |
+-----------------------------+
```

## 数据对象

| 对象 | 生产者 | 消费者 | 说明 |
|---|---|---|---|
| Storage operation 请求 | API 调用方 | Storage service | 含路径中的 batch id、可选 adapter key 与 operations 列表。 |
| Storage adapter 请求 | Storage service | Storage adapter | 含 operation id、batch scope、layer、安全相对 key、content type、content reference。 |
| Stored-object descriptor | Storage adapter | Storage service | 含 layer、key、size、checksum、content type、adapter key、status、安全错误。 |
| Storage operation record | Storage service | 数据库/报告 | 存储 operation type、layer、计数、总字节数、status、安全消息。 |
| File item 指针更新 | Storage service | 数据库/file API | 将返回的 key 记录到 `pdf_path` / `markdown_path` / `assets_path` 或 report/wiki 指针。 |
| 已存储对象字节 | Storage adapter / 引擎 | 存储引擎（adapter 之后） | 绝不持久化到 metadata；通过 content reference/流交换。 |

## 字段映射

| Storage 结果字段 | Metadata 目标 | 规则 |
|---|---|---|
| `layer` | `storage_object.layer` | `raw`、`pdf`、`markdown`、`assets`、`reports`、`wiki` 之一。 |
| `objectKey` | `storage_object.objectKey`、`file_item` 指针字段 | workspace/batch 命名空间内的安全相对 key。 |
| `sizeBytes` | `storage_object.sizeBytes`、`storage_operation.totalBytes` | 非负整数。 |
| `checksum` | `storage_object.checksum` | `STORED` descriptor 必须非空。 |
| `contentType` | `storage_object.contentType` | user-safe MIME/类型标签。 |
| `status` | `storage_object.status`、`storage_operation` 汇总 | `STORED` / `DELETED` / `MISSING` / `FAILED`。 |
| `fileId`（若有） | `file_item` 指针字段目标 | 必须引用请求 batch 中的文件。 |
| `safeError` | `storage_object.safeError`、`storage_operation.safeMessage` | 脱敏且有界。 |

## 失败流程

```text
+-----------------------+
| Adapter 不可用        |
+-----------+-----------+
            |
            v
  storage_operation FAILED
  file_item 指针不变

+-----------------------+
| 不安全 key / layer    |
+-----------+-----------+
            |
            v
  校验错误
  不写入或删除任何对象

+-----------------------+
| 逐对象操作失败        |
+-----------+-----------+
            |
            v
  storage_object FAILED
  仅 safeError
  记录含 failed 计数
```

## Review 与 Trace 保留

- 除非某 workflow 明确映射变更，storage 操作不改变 `file_item.review_status`、`status` 与 `confidence`。
- Stored-object descriptor 记录 layer/key/checksum 证据，但绝不存储原始文档字节。
- 指针写回把已存储对象链接到其 file item，且不改动 source trace。
- Storage-adapter 不标记内容为 `APPROVED` 或 `PUBLISHED`。

## 验证钩子

- Mock storage 测试校验所有操作分支（store、get/exists、list、delete）与失败路径。
- Seam guard 验证 adapter 包外无直连存储引擎引用，且 adapter 范围 `S3` 例外仅应用于 storage adapter 包。
- Secret/路径扫描覆盖 storage SDD 文档与后端实现路径。

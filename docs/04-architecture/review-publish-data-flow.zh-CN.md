# 数据流：Review Publish

## 状态

草稿。

## Flow 1：读取 Review Queue

```text
用户打开 Processing Center
  -> 前端请求 /api/spaces/{spaceId}/review-queues
  -> API 从 file/chunk/wiki metadata 派生队列计数
  -> API 返回 blocked categories 和 publish-ready candidates
  -> 前端渲染 counts、actions 和 downstream blocking copy
```

## Flow 2：SME 审核决策

```text
Reviewer 提交 action
  -> POST /api/files/{fileId}/reviews
  -> 校验 action/comment/affectedChunks
  -> 追加 review_record
  -> 更新 file_item.review_status 为 APPROVED / NEED_FIX / OCR_REQUIRED
  -> 返回 created review record
```

## Flow 3：发布已批准 Markdown

```text
Admin 选择 publish
  -> POST /api/files/{fileId}/publish
  -> 加载 file item、source chunks 和 existing wiki page
  -> 检查 APPROVED、relative markdown_path、confidence、source documents、source trace
  -> 不合格：返回用户安全 400/409，且不修改数据
  -> 合格：创建/更新 review_status=PUBLISHED 的 wiki_page
  -> 返回 published wiki page metadata
```

## Flow 4：下游消费

```text
Wiki 读取已发布页面
  -> GET /api/spaces/{spaceId}/wiki-pages
  -> Graph 和 Ask 未来切片只消费 published/approved/source-traced records
  -> Blocked Processing Center items 继续排除
```

## 错误级联

| Condition | Result |
|---|---|
| Missing source trace | Publish blocked；无 wiki mutation。 |
| `reviewStatus != APPROVED` | Publish blocked；需要 reviewer action。 |
| Missing 或 absolute Markdown path | Publish blocked；validation error。 |
| Missing confidence | Publish blocked；quality metadata incomplete。 |
| Unknown file/space/page id | `404 NOT_FOUND`。 |
| Concurrent publish conflict | `409 CONFLICT`；无 partial update。 |

## 验证钩子

- 状态转换和 eligibility 的 unit tests。
- Review queue、publish success、publish failure 的 integration tests。
- Processing Center 到 published Wiki state 的 E2E tests。
- 新网络调用、direct adapter calls、secrets、private paths 的静态扫描。

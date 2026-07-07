# 用户故事：retrieval-quality-metrics

状态：已由本次 goal 预授权接受
最后更新：2026-07-07

## 故事集

### US-RETRIEVAL-QUALITY-METRICS-001：检查 Ask run 质量

作为知识运营者，我希望看到 Trusted Ask run 的安全 retrieval quality metrics，以便判断回答是否有足够 evidence grounding 可进入 review。

验收：
- Given 一个带 evidence 的已存储 Ask run，when 请求 metrics，then 返回 evidence coverage、citation health、confidence 和 review eligibility。
- Given 同一组已存储记录，when 反复请求 metrics，then 输出保持确定性。
- Given 一个 no evidence 的 Ask run，when 请求 metrics，then 该 run 被统计为 no-evidence refusal，且不是 review-eligible。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-004。

### US-RETRIEVAL-QUALITY-METRICS-002：检查 space 级检索可靠性

作为产品负责人，我希望看到 Knowledge Space 的安全 aggregate retrieval quality statistics，以便在不阅读机密内容的情况下识别弱 coverage 和 no-evidence patterns。

验收：
- Given 一个 space 中有多个 Ask runs，when 请求 aggregate metrics，then 按安全质量类别返回 counts 和 ratios。
- Given failed 或 no-evidence runs，when 请求 aggregate metrics，then 它们以 refusal/failure statistics 表示，而不是 healthy retrieval。
- Given storage 中存在 raw questions 或 answers，when 返回 aggregate metrics，then 响应中不包含这些内容。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-001、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-004。

### US-RETRIEVAL-QUALITY-METRICS-003：展示安全质量信号

作为 reviewer，我希望 Trusted Ask 与 knowledge surfaces 展示安全质量信号，以便决定是否检查 evidence 或补充 source material。

验收：
- Given 一个成功 Ask response，when UI 渲染它，then quality chips 区分 evidence coverage、citation health 和 review eligibility。
- Given quality metrics 不可用，when UI 渲染状态，then 显示安全 unavailable state，并保持既有 answer/citation 行为。
- Given diagnostics 被展示，when 用户查看，then 不显示 raw prompt、raw provider payload、private path、stack trace 或 raw source content。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-002、REQ-RETRIEVAL-QUALITY-METRICS-003、REQ-RETRIEVAL-QUALITY-METRICS-006。

### US-RETRIEVAL-QUALITY-METRICS-004：保持安全与 API contract

作为 API consumer，我希望 retrieval metrics APIs 使用 Atlas safe envelopes，以便调用方获得可预测响应和安全错误处理。

验收：
- Given 一个有效 metrics request，when 后端响应，then 使用 `ApiEnvelope`。
- Given 未知 run 或 space，when 后端响应，then 使用安全 `NOT_FOUND` 行为。
- Given metrics 已计算，when DTOs 被序列化，then private diagnostics 和 raw content 缺席。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-005、REQ-RETRIEVAL-QUALITY-METRICS-007。

### US-RETRIEVAL-QUALITY-METRICS-005：带证据关闭切片

作为维护者，我希望 SDD、tests、traceability、roadmap 和 closeout evidence 都被更新，以便未来 agent 能从持久项目状态恢复。

验收：
- Given 实现完成，when closeout 运行，then 记录 SDD、task completion、verification 和 residual risks。
- Given changes 被 staged，when commit 和 push 执行，then 只包含 retrieval-quality-metrics 相关变更。

映射到：REQ-RETRIEVAL-QUALITY-METRICS-008。

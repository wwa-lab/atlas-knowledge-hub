# Atlas Knowledge Hub — Repo 整体评价与行动建议

> 评审时间：2026-07-08
> 评审范围：backend / frontend / docs / 工程治理全维度
> 综合评分：**7.5 / 10**

---

## 一句话结论

**工程治理水平远超产品成熟度的"过度认真"型个人项目**——"怎么做"做得很出色，"做成了什么"还停在中段。

如果把"作为一个 repo 的工程质量"来评，8.5 分；如果把"作为一个产品的成熟度"来评，5.5 分。

---

## 七维评分

| 维度 | 得分 | 说明 |
|---|---|---|
| 工程治理 / 文档体系 | 9.0 | 30 个 SDD slice 全程可追溯，双语对等，真实在用非形式主义 |
| 后端架构与代码 | 8.5 | 分层严谨，adapter Mock/Real 双轨，Testcontainers 真集成，依赖极简，零 TODO |
| 前端实现 | 5.0 | App.vue 5332 行单组件，无 components 目录，单元测试仅 3 文件——最大技术债 |
| 产品成熟度 | 5.5 | L2-L3 居多，Phase J 内部 beta 仍在 readiness，部署/运维未启动 |
| 诚实度 / 自我认知 | 9.5 | 区分 task completion vs product acceptance，L0-L5 模型，主动列 limitations |
| 测试与验证 | 7.5 | 后端扎实，前端 E2E 全但单元测试不足 |
| 安全与数据规范 | 8.5 | mock-only 纪律、adapter 边界、密钥规则被遵守 |

---

## 强在哪里

### 1. 文档/SDD 治理（9.0）—— 个人项目里几乎顶尖

30 个 SDD slice，跨 7 个目录（requirements → user-stories → spec → architecture → design → tasks → acceptance），ID 全程可追溯。双语（EN + zh-CN）核心链路完全对等。这不是模板堆砌——抽查 `ask-rag-spec.md` 有完整的 FR 编号、反向引用 US/REQ ID、scope/out-of-scope/constraints 齐全。`lessons-learned.md` 有 271 行的真实复盘闭环。`execution-manifests/` 有 13 个带日期的 manifest，`evidence/` 有 10 张近期截图。**这是真实在驱动的 SDD 工作流，不是形式主义。**

### 2. 后端架构与代码（8.5）—— 分层严谨，不是空壳

24 个 controller、48 个 service、41 个 repository、19 个 Flyway 迁移连续演进。关键是六大 adapter 边界（converter/parser/storage/vector/model/connector）**真实存在且 Mock/Real 双轨并存**——`TrinityOfficeConverterAdapter`(267行)、`DocumentNormalizeParserAdapter`(308行) 是真实现，Mock 版本是有意的确定性契约验证，不是临时占位。测试 73 个类含真实 Testcontainers（postgres:16-alpine）。依赖只有 11 个，零 TODO/FIXME 堆积。**这个后端质量高于一般"声称 Spring Boot"的项目。**

### 3. 诚实度（9.5）—— 罕见的高水平自我认知

ROADMAP 明确区分"task completion" vs "product acceptance"，承认"之前 roadmap 看起来 done 但产品其实没到位"。L0-L5 成熟度模型、每个 phase 都标注"This is not final product acceptance"、README 主动列"Honest limitations"。**这种诚实在项目里非常稀缺，是成熟工程心态的体现。**

### 4. 安全与数据规范（8.5）

mock-only 数据纪律、adapter 边界规则、密钥 write-only/masked 规则都写得明确且被遵守。`atlasMock.ts` 只被测试引用，没污染 App.vue。

---

## 弱在哪里

### 1. 前端实现（5.0）—— 最大技术债，且 README 说的属实

`App.vue` **5332 行**单一巨型组件，2740 行 `<script setup>` + 2592 行 template，69 个函数、179 个响应式状态。**完全没有 `components/` 目录**，全仓就这一个 `.vue` 文件。无 pinia、无 vue-router。这是真实的可维护性风险——虽然诚实承认了，但债是实打实的。单元测试只有 3 个文件，对一个 5332 行的组件来说覆盖明显不足（E2E 16 个 spec 反而更全）。

### 2. 产品成熟度（5.5）—— 离生产远，方向清晰但路还长

ROADMAP 自己说：Phase J（内部 beta）还在 readiness preparation，auth/RBAC/audit "mostly deferred"，部署/运维 "not started"。产品成熟度集中在 L2-L3（Vue parity 到 API-backed），离 L5 production 很远。`PgVectorAdapter` 只有 47 行 placeholder，`ConfiguredModelAdapter` 带有 placeholder 注释。

### 3. 过度工程化的隐忧

669 个 docs 文件 vs 实际产品还在 L2-L3，文档/代码比偏高。30 个 slice 的治理成本可能已经超过当前产品实际交付价值。这是一个**"过程重于结果"的潜在信号**——如果目标是尽快出可用产品，这套 SDD 仪式可能是负担；如果目标是练 agent 工作流/方法论，那它是极佳实验场。

---

## 行动建议（三层优先级）

### 第一层 · 止血（1–2 周，不能再拖）

**1. 拆分 App.vue**

5332 行单组件是"任何改动都触发全文件重新认知"的状态，心智成本 O(全组件)。按产品域拆，不要按技术层拆：

```
src/
├── views/          HomeView / SpaceDetailView / ProcessingCenterView
│                   WikiView / GraphView / AskView
├── components/     Sidebar / TopBar / SettingsOverlay 等共享组件
├── composables/    useSpace / useBatch / useReview / useWiki
├── router/         引入 vue-router，替换 home/chat/else 三分手写切换
└── stores/         （可选）pinia 做跨组件状态，替代散落的 ref
```

注意 PROJECT_RULES 约束："Split App.vue into components only after the relevant SDD slice is accepted"。建议先开一个 `frontend-componentization` slice 走轻量 SDD，再动刀——正好检验方法论在"大重构"场景下好不好使。

**2. 补前端单元测试**

和拆分同步进行——拆一个组件，补一个组件的测试。优先测有状态映射、格式化、交互逻辑的 composables，而不是纯展示组件。这样拆分过程本身就带着测试保护，防止"拆完行为变了"。

### 第二层 · 推进（1–2 月，把产品从 L2/L3 推到 L4）

**3. 激活真实 adapter 链路**

现在所有 runtime adapter 默认 mock（`trinity-office.enabled:false` 等）。Mock 写得很好，但从未证明真实链路能跑通。至少拿 samples 里的样本文档跑通一次：Office→PDF→Markdown→归一化，看到真实 source trace 字段流转。这一步过了，整个架构故事才真正"落地"。

**4. 选一条完整旅程推到 L4**

现在 30 个 slice 大多 L2-L3，没有一个完整用户旅程做到 L4。与其开第 31 个 slice，不如把现有能力串成一条能演示的闭环：upload → batch → processing center → wiki → ask。重点不是加新功能，而是串接、暴露断点。

**5. 部署从 0 到 1**

L4 内部 beta 的定义就是"受控环境跑起来"——没有部署方案，L4 永远到不了。一个 `docker-compose.yml`（postgres + backend + frontend + 健康检查 + 日志）就是入场券，不需要 K8s 全家桶。

### 第三层 · 取舍（持续，方法论层面的权衡）

**6. SDD 适当减负**

30 slice × 7 目录 × 双语 = 646 个 docs 文件，治理成本对个人项目偏高。给维护型小改动开"快车道"——只更新 spec + tasks + traceability，不强制 7 件套。把 `data-flow` / `data-model` 合并进 `architecture`。新功能 slice 仍走完整链路，但区分"新行为"和"维护改动"的 SDD 深度。

**7. 重新定位 repo 目标（最根本的一条）**

诚实回答自己——这个 repo 对你到底是什么？

- **如果是"方法论实验场"**：状态很好，继续打磨 SDD + agent 工作流，沉淀成公众号内容。前端债、产品成熟度都不重要，产出是"被验证的方法论"。作为方法论样本它是 9 分。
- **如果是"要交付的内部产品"**：现状危险——文档/代码比失衡，前端债拖慢每个迭代，治理成本在吃掉本该投入产品的时间。需要主动砍 SDD 比例、加速前端、优先 L4 闭环。作为产品目前 5.5 分。

两者都没错，但不能假装是 A 实际想要 B。

---

## 核心判断

这个 repo 的画像：**一个把"工程方法论"当主业、把"产品交付"当副业的项目。**

- 如果目标是**验证 SDD + agent 工作流能否跑通**——教科书级示范，9 分。
- 如果目标是**尽快有能用的内部知识产品**——前端债 + 治理成本会拖慢你，6 分。

核心原则：**纵向做深一条完整旅程，优于横向扩张 slice 数量。** 现在 30 个 slice 已够多，治理成本逼近产品实际交付价值。

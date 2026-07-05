# 用户故事：auth-space-rbac

状态：Draft SDD
最后更新：2026-07-05

## 故事集

| Story | 标题 | Requirements |
|---|---|---|
| US-AUTH-SPACE-RBAC-001 | 解析当前用户上下文 | REQ-AUTH-SPACE-RBAC-001, REQ-AUTH-SPACE-RBAC-002, REQ-AUTH-SPACE-RBAC-003, REQ-AUTH-SPACE-RBAC-004 |
| US-AUTH-SPACE-RBAC-002 | 强制执行空间角色矩阵 | REQ-AUTH-SPACE-RBAC-005, REQ-AUTH-SPACE-RBAC-006, REQ-AUTH-SPACE-RBAC-007 |
| US-AUTH-SPACE-RBAC-003 | 保护成员管理 | REQ-AUTH-SPACE-RBAC-012, REQ-AUTH-SPACE-RBAC-013, REQ-AUTH-SPACE-RBAC-014 |
| US-AUTH-SPACE-RBAC-004 | 防止未授权资源披露 | REQ-AUTH-SPACE-RBAC-009, REQ-AUTH-SPACE-RBAC-010, REQ-AUTH-SPACE-RBAC-011 |
| US-AUTH-SPACE-RBAC-005 | 展示权限感知 UI | REQ-AUTH-SPACE-RBAC-004, REQ-AUTH-SPACE-RBAC-008 |
| US-AUTH-SPACE-RBAC-006 | 验证角色差异 | REQ-AUTH-SPACE-RBAC-006, REQ-AUTH-SPACE-RBAC-007, REQ-AUTH-SPACE-RBAC-014 |

---

## User Story US-AUTH-SPACE-RBAC-001

**标题：** 解析当前用户上下文

**故事：**
作为已认证的内部用户，  
我希望 Atlas 知道我的身份、memberships、active space 和 effective roles，  
以便每个产品动作都能被一致授权。

## 验收标准

1. **Given** local/test mode 中的 mock-auth request  
   **When** 后端解析 current user context  
   **Then** context 包含 user id、display name、email、global roles、memberships、active space 和 capabilities。

2. **Given** protected API 没有 authenticated context  
   **When** 请求到达 backend authorization  
   **Then** API 通过 Atlas envelope 返回 `401`。

3. **Given** 未来公司 SSO/OIDC provider 未配置  
   **When** local/test profile 运行  
   **Then** mock auth 不产生外部网络调用也能工作。

## 备注 / 假设
- 本切片默认使用 local mock auth。
- 公司 SSO/OIDC 只表示为 provider boundary，不做 live provider call。

## 依赖
- 现有 Atlas API envelope 与 safe error body。
- 现有 Spring Boot metadata API。

## 范围外
- Password login、MFA、SCIM、production IdP configuration。

## 开放问题
- 无阻塞 SDD 接受的问题。

---

## User Story US-AUTH-SPACE-RBAC-002

**标题：** 强制执行空间角色矩阵

**故事：**
作为空间成员，  
我希望 Atlas 根据我在当前 Knowledge Space 中的角色执行权限，  
以便用户只能读取、修改、审核、发布、提问或配置自己有权访问的内容。

## 验收标准

1. **Given** 一个 `VIEWER`  
   **When** 调用写 API  
   **Then** 后端返回 `403`。

2. **Given** 一个 `KNOWLEDGE_MANAGER`  
   **When** 执行 ingest、review、publish、graph refresh 或 Ask 等知识操作  
   **Then** 后端只允许该角色映射到的操作。

3. **Given** 一个 `SPACE_OWNER`  
   **When** 管理 membership 与 space-scoped settings  
   **Then** 只要 membership invariants 仍有效，后端允许该动作。

## 备注 / 假设
- Role names 在 API 和持久化 metadata 中使用 uppercase enum values。

## 依赖
- Additive user 与 membership schema。

## 范围外
- 组织级 directory sync。

## 开放问题
- 无阻塞 SDD 接受的问题。

---

## User Story US-AUTH-SPACE-RBAC-003

**标题：** 保护成员管理

**故事：**
作为 Space Owner，  
我希望在有 guardrails 的前提下邀请、更新、暂停和移除成员，  
以便 Knowledge Space 能被管理，同时不会意外失去 owner 或暴露敏感 membership 数据。

## 验收标准

1. **Given** 调用者不是 `SPACE_OWNER` 或 `PLATFORM_ADMIN`  
   **When** 创建、变更或移除 membership  
   **Then** 后端返回 `403`。

2. **Given** membership 变更会移除最后一个 active `SPACE_OWNER`  
   **When** 请求提交  
   **Then** 后端以 validation/conflict response 拒绝。

3. **Given** membership operation 成功或失败  
   **When** auth decision 被记录  
   **Then** 最小事件字段可供后续 audit slice 使用。

## 备注 / 假设
- 本切片不发送真实 invitation email。

## 依赖
- Auth context 与 role matrix。

## 范围外
- Email invitation workflow 与 audit UI。

## 开放问题
- 无阻塞 SDD 接受的问题。

---

## User Story US-AUTH-SPACE-RBAC-004

**标题：** 防止未授权资源披露

**故事：**
作为安全评审者，  
我希望未授权访问不会暴露受保护资源是否存在或敏感细节，  
以便内部 beta 用户不能枚举自己不应知道的 spaces、files、pages、graph objects 或 Ask runs。

## 验收标准

1. **Given** 用户在目标 space 中没有 membership  
   **When** 请求 space-scoped resource  
   **Then** 响应不包含受保护资源 metadata。

2. **Given** 用户已认证但缺少所需权限  
   **When** authorization 失败  
   **Then** 除非端点契约明确使用 safe not-found semantics，否则响应为通用 `403`。

3. **Given** auth 或 authorization 出错  
   **When** 返回响应  
   **Then** 不暴露 stack trace、secret、private path、raw token 或 provider payload。

## 备注 / 假设
- 受保护资源优先使用通用 denied message。

## 依赖
- 全局 safe error envelope。

## 范围外
- 完整 rate limiting 与 anomaly detection。

## 开放问题
- 无阻塞 SDD 接受的问题。

---

## User Story US-AUTH-SPACE-RBAC-005

**标题：** 展示权限感知 UI

**故事：**
作为 Atlas 用户，  
我希望 UI 能反映我在当前空间中可以做什么，  
以便不会看到会被后端拒绝的误导性写操作。

## 验收标准

1. **Given** 前端启动  
   **When** 加载 `/api/auth/me`  
   **Then** 收到 current user、memberships、active space、roles 和 capabilities。

2. **Given** 用户是 `VIEWER`  
   **When** 渲染写控件  
   **Then** 控件被隐藏或禁用，并显示不含敏感信息的文案。

3. **Given** 用户篡改 client  
   **When** 直接调用 forbidden backend API  
   **Then** backend authorization 仍返回 `403`。

## 备注 / 假设
- Frontend permission awareness 仅改善体验。

## 依赖
- `/api/auth/me`。

## 范围外
- Settings 或 member management 的全新视觉重设计。

## 开放问题
- 无阻塞 SDD 接受的问题。

---

## User Story US-AUTH-SPACE-RBAC-006

**标题：** 验证角色差异

**故事：**
作为交付负责人，  
我希望自动化测试证明角色差异行为，  
以便内部 beta readiness 基于后端强制权限，而不是 UI 假设。

## 验收标准

1. **Given** integration tests 运行  
   **When** `VIEWER`、`KNOWLEDGE_MANAGER` 和 `SPACE_OWNER` 调用代表性 APIs  
   **Then** allowed/denied outcomes 与 role matrix 一致。

2. **Given** E2E tests 运行  
   **When** 不同角色用户打开核心产品界面  
   **Then** 可见操作与 backend-provided capabilities 一致。

3. **Given** verification scans 运行  
   **When** 切片收尾  
   **Then** 没有引入真实公司数据、private paths、raw secrets 或外部云调用。

## 备注 / 假设
- Mock users 只作为 seeded test identities。

## 依赖
- Backend integration test harness 与 frontend E2E harness。

## 范围外
- 生产渗透测试。

## 开放问题
- 无阻塞 SDD 接受的问题。

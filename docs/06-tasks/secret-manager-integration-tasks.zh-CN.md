# 任务：secret-manager-integration

状态：已完成实现并通过本地 closeout verification
最后更新：2026-07-07

## Workstreams

- Backend DTO/API contracts。
- Backend service 与 adapter capability mapping。
- Frontend type 与 settings display。
- Tests、scans、traceability、roadmap、closeout。

## Task Details

### T-SECRET-MANAGER-INTEGRATION-001：增加共享 Secret Status DTOs

- Objective：增加 `SecretReferenceResponse` 与 `SecretStatusResponse`。
- Scope：仅 backend DTO；无 persistence。
- Requirements：REQ-001, REQ-007。
- Verification：通过 targeted tests 编译 backend。
- Status：完成。

### T-SECRET-MANAGER-INTEGRATION-002：扩展 Model Configuration Contract

- Objective：model configuration read/save/clear 返回 typed secret statuses。
- Scope：`ModelConfigurationResponse`、`ModelRuntimeConfigurationService`、相关测试。
- Requirements：REQ-001, REQ-002, REQ-003, REQ-008。
- Verification：`cd backend && mvn -Dtest=ModelRuntimeConfigurationServiceTest,ModelApiContractIT test`。
- Status：完成；Docker/Testcontainers 可用后 full backend verification 已通过。

### T-SECRET-MANAGER-INTEGRATION-003：扩展 Adapter Capability Contracts

- Objective：为 model、parser、converter、storage、vector capability responses 增加 typed secret statuses。
- Scope：Capability response records、adapter capability records 或 mappers、现有 adapters。
- Requirements：REQ-004, REQ-005, REQ-007, REQ-008。
- Verification：backend capability/API contract tests。
- Status：完成。

### T-SECRET-MANAGER-INTEGRATION-004：更新 Frontend Settings Display

- Objective：优先消费 `secretStatuses`，渲染 status-only labels。
- Scope：frontend API types、model/adapter settings display、本地 transient secret input handling。
- Requirements：REQ-006, REQ-008。
- Verification：`cd frontend && npm run typecheck && npm run test`。
- Status：完成。

### T-SECRET-MANAGER-INTEGRATION-005：增加 Redaction Tests

- Objective：证明 write-only secrets、endpoints、commands 与 private paths 不出现在 read responses/UI tests。
- Scope：backend unit/API tests 与 frontend component tests。
- Requirements：REQ-008。
- Verification：targeted backend/frontend tests。
- Status：完成。

### T-SECRET-MANAGER-INTEGRATION-006：更新 Traceability 与 Roadmaps

- Objective：记录 SDD skill chain、implementation evidence、residual risks 与状态。
- Scope：secret traceability plus slice/repo roadmap status。
- Requirements：REQ-008。
- Verification：`npm run agent:check-sdd -- --slice secret-manager-integration --require-api-guide` 与 closeout。
- Status：完成；traceability 与 roadmap/status documents 已更新。

### T-SECRET-MANAGER-INTEGRATION-007：运行完整 Verification 与 Closeout

- Objective：运行 required checks，并修复 scoped failures。
- Scope：backend/frontend verification、scans、diff check、closeout。
- Requirements：all。
- Verification：
  - `cd backend && mvn verify`
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test`
  - `cd frontend && npm run build`
  - `npm run agent:closeout`
  - `git diff --check`
  - focused secret/private-path/real-data scan
- Status：完成。

## Dependency Plan

Critical path: T-001 -> T-002 -> T-003 -> T-004 -> T-005 -> T-006 -> T-007。

## Risks

- Runtime state 仍为 in-memory，不是 production secret storage。
- 现有 request payload field names 为兼容性保留，但作为 write-only 处理。

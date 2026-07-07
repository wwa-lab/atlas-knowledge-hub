# 架构：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07

## Architectural Drivers

- REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012 要求一个 safe backend error boundary、deterministic local throttling、frontend classification、tests 与 traceability。
- 本切片扩展现有 Spring Boot 与 Vue 路径；不添加外部服务，也不改变 auth/audit semantics。

## System Context

```
Users
  |
Vue Product Shell + API Client
  |
Spring Boot /api/**
  |
Local Rate Limiter -> Auth/RBAC Interceptor -> Controllers/Services
  |
Safe Error Mapper + Global Exception Handler
  |
ApiEnvelope(success=false, error=safe body)
```

## Components

- Safe error model：扩展 existing error object，包含 safe code、message、fields、timestamp、path、correlation id 与 retry metadata。
- Safe error sanitizer：在 response serialization 前 redact forbidden strings。
- Global exception handler：将 validation、not found、conflict 与 unexpected errors 映射为 safe codes。
- Auth/RBAC interceptor：保持 current auth decisions，但输出 safe `AUTHENTICATION_REQUIRED` 与 `PERMISSION_DENIED` envelopes。
- Local rate-limit interceptor：为 `/api/**` 提供 deterministic in-memory limiter，排序在 auth 前或同层，且无 external stores。
- Frontend API client：将 safe codes 转成 typed UI categories。
- Product shell：展示代表性 safe states。

## Boundaries

- Rate limit storage 仅为 local in-memory，不是 production distributed quota store。
- Parser、converter、model、vector、storage 与 search adapter boundaries 不变。
- Audit behavior 不被重新定义。Server logs 保留 safe troubleshooting context。
- 不使用 external network calls、cloud providers、Redis、gateway、service mesh 或 real secrets。

## Security

AC-RATE-LIMIT-SAFE-ERRORS-002 通过 sanitizer 与 tests 执行。API 不得暴露 raw stack traces、raw exception class names、secrets、credentials、private paths、internal endpoints 或 raw source content。

## Architecture Review Result

Architecture-review applied：SDD design 未发现 P0/P1 violations。P2 residual risk：local in-memory rate limiting 有意不是 production-grade，必须保持 prototype/local-safe 标签。

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-001 through T-RATE-LIMIT-SAFE-ERRORS-008 implement this architecture and preserve REQ-RATE-LIMIT-SAFE-ERRORS-001 through REQ-RATE-LIMIT-SAFE-ERRORS-012.

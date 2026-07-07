# 数据模型：rate-limit-safe-errors

状态：按显式 goal 预授权接受的草案
最后更新：2026-07-07

## Overview

本切片不新增 persistent database tables。它增加 API DTO fields 和 deterministic throttling 的 local in-memory runtime state。

## DTO: Safe Error Body

| Field | Type | Required | Requirement |
|---|---|---:|---|
| code | String | yes | REQ-RATE-LIMIT-SAFE-ERRORS-001 |
| message | String | yes | REQ-RATE-LIMIT-SAFE-ERRORS-001 |
| fields | Map<String, String> | no | REQ-RATE-LIMIT-SAFE-ERRORS-002 |
| timestamp | long | yes | REQ-RATE-LIMIT-SAFE-ERRORS-001 |
| path | String | yes | REQ-RATE-LIMIT-SAFE-ERRORS-001 |
| correlationId | String | no | REQ-RATE-LIMIT-SAFE-ERRORS-006 |
| retryAfterSeconds | Integer | no | REQ-RATE-LIMIT-SAFE-ERRORS-005 |

## In-Memory Model: Local Rate Bucket

| Field | Type | Description |
|---|---|---|
| key | String | Safe caller key derived from request metadata。 |
| windowStartMillis | long | Current deterministic local window start。 |
| used | int | Requests consumed in current window。 |
| limit | int | Configured request count budget。 |
| retryAfterSeconds | int | Safe integer retry hint。 |

## Frontend Model

| Field | Type | Requirement |
|---|---|---|
| safeCategory | union | REQ-RATE-LIMIT-SAFE-ERRORS-008 |
| title | String | REQ-RATE-LIMIT-SAFE-ERRORS-009 |
| description | String | REQ-RATE-LIMIT-SAFE-ERRORS-009 |
| retryAfterSeconds | number? | REQ-RATE-LIMIT-SAFE-ERRORS-005 |

## Validation And Redaction Rules

- AC-RATE-LIMIT-SAFE-ERRORS-002: all model fields must be bounded and sanitized before response。
- AC-RATE-LIMIT-SAFE-ERRORS-004: tests can reset the local rate bucket state。
- AC-RATE-LIMIT-SAFE-ERRORS-007: this slice 不需要 persistence migration。

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-002 owns DTO/sanitizer updates. T-RATE-LIMIT-SAFE-ERRORS-004 owns local bucket behavior. T-RATE-LIMIT-SAFE-ERRORS-005 owns frontend model mapping.

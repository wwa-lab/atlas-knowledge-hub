# Data Model: rate-limit-safe-errors

Status: Draft accepted by explicit goal preauthorization
Last updated: 2026-07-07

## Overview

This slice does not add persistent database tables. It adds API DTO fields and local in-memory runtime state for deterministic throttling.

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
| key | String | Safe caller key derived from request metadata. |
| windowStartMillis | long | Start of current deterministic local window. |
| used | int | Requests consumed in the current window. |
| limit | int | Configured request count budget. |
| retryAfterSeconds | int | Safe integer retry hint. |

## Frontend Model

| Field | Type | Requirement |
|---|---|---|
| safeCategory | union | REQ-RATE-LIMIT-SAFE-ERRORS-008 |
| title | String | REQ-RATE-LIMIT-SAFE-ERRORS-009 |
| description | String | REQ-RATE-LIMIT-SAFE-ERRORS-009 |
| retryAfterSeconds | number? | REQ-RATE-LIMIT-SAFE-ERRORS-005 |

## Validation And Redaction Rules

- AC-RATE-LIMIT-SAFE-ERRORS-002: all model fields must be bounded and sanitized before response.
- AC-RATE-LIMIT-SAFE-ERRORS-004: tests can reset the local rate bucket state.
- AC-RATE-LIMIT-SAFE-ERRORS-007: no persistence migration is required for this slice.

## Task Mapping

T-RATE-LIMIT-SAFE-ERRORS-002 owns DTO/sanitizer updates. T-RATE-LIMIT-SAFE-ERRORS-004 owns local bucket behavior. T-RATE-LIMIT-SAFE-ERRORS-005 owns frontend model mapping.

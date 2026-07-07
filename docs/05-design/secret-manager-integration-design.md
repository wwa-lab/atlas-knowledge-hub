# Design: secret-manager-integration

Status: Preauthorized accepted
Last updated: 2026-07-07

## Design Scope

Implement a typed status layer over existing configuration responses. The design is intentionally additive and compatibility-preserving.

## Backend Design

- Add `SecretReferenceResponse` and `SecretStatusResponse` records under backend DTOs.
- Add a small helper/factory for converting summary map entries into status DTOs if useful.
- Extend model and adapter capability response records with `List<SecretStatusResponse> secretStatuses`.
- Keep existing `maskedConfigSummary` maps.
- Update `ModelRuntimeConfigurationService` to return secret status entries for credential and endpoint.
- Update capability mappers/adapters so parser/converter/storage/vector/model expose typed status entries.

## Frontend Design

- Extend TypeScript API types with secret reference/status types.
- Render model credential and adapter settings from `secretStatuses` where present.
- Preserve existing copy and layout style in settings.
- Do not store raw keys in persisted frontend state; transient editor input remains local and is cleared after save/close.

## API / Interface Design

No new endpoints. Existing endpoints return additive fields. Write requests keep the current endpoint, with the key material treated as replacement input only.

## Error Handling

- Validation errors name invalid fields without echoing values.
- Adapter failures continue returning sanitized messages.
- UI failure copy remains generic and safe.

## Testing

- Backend unit tests: read/save/clear status and no raw values in response.
- Backend API contract tests: capability and configuration endpoints include typed secret statuses and exclude raw values.
- Frontend tests: model settings renders status-only secret labels and sends replacement without echoing it in UI.

## Risks

- In-memory runtime configuration is not production secret storage. The traceability file must state this residual risk.
- The existing request field name remains for compatibility; this slice makes it write-only but does not rename the external payload in a breaking way.

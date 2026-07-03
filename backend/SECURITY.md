# Metadata API Security Note

This Phase 2 metadata API is internal-only. It intentionally does not implement
authentication, authorization, member management, API key handling, or RBAC.

Do not expose this service publicly as-is. Production authentication, RBAC,
auditing, and write-policy hardening are deferred to a later Phase 4 security
slice. Datasource credentials must be supplied through environment or external
configuration and must never be committed as raw values.

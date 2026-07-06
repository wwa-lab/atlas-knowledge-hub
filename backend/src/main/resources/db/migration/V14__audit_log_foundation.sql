CREATE TABLE atlas.audit_event (
  id text CONSTRAINT pk_audit_event PRIMARY KEY,
  created_at timestamptz NOT NULL,
  actor_user_id text,
  actor_display text NOT NULL,
  action text NOT NULL,
  category text NOT NULL CONSTRAINT ck_audit_event_category CHECK (
    category IN ('AUTH', 'MEMBERSHIP', 'REVIEW', 'PUBLISH', 'WIKI', 'GRAPH', 'ASK', 'MODEL', 'ADAPTER', 'SETTINGS')
  ),
  result text NOT NULL CONSTRAINT ck_audit_event_result CHECK (
    result IN ('SUCCEEDED', 'DENIED', 'FAILED', 'CONFLICT', 'SKIPPED', 'SAFE_NOT_FOUND')
  ),
  severity text NOT NULL CONSTRAINT ck_audit_event_severity CHECK (
    severity IN ('INFO', 'NOTICE', 'WARNING', 'SECURITY')
  ),
  space_id text,
  target_type text NOT NULL,
  target_id text NOT NULL,
  request_id text,
  safe_summary text NOT NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  CONSTRAINT fk_audit_event_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE INDEX idx_audit_event_space_created ON atlas.audit_event (space_id, created_at DESC);
CREATE INDEX idx_audit_event_space_category_created ON atlas.audit_event (space_id, category, created_at DESC);
CREATE INDEX idx_audit_event_space_result_created ON atlas.audit_event (space_id, result, created_at DESC);
CREATE INDEX idx_audit_event_target ON atlas.audit_event (space_id, target_type, target_id, created_at DESC);
CREATE INDEX idx_audit_event_actor_created ON atlas.audit_event (actor_user_id, created_at DESC);

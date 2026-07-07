CREATE TABLE atlas.connector_definition (
  id TEXT PRIMARY KEY,
  connector_key TEXT NOT NULL UNIQUE,
  name TEXT NOT NULL,
  connector_type TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
  version TEXT NOT NULL,
  capability_summary TEXT NOT NULL,
  configuration_state TEXT NOT NULL CHECK (configuration_state IN ('MOCK_CONFIGURED', 'NOT_CONFIGURED')),
  review_policy TEXT NOT NULL CHECK (review_policy IN ('REVIEW_REQUIRED')),
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE atlas.connector_sync_job (
  id TEXT PRIMARY KEY,
  space_id TEXT NOT NULL REFERENCES atlas.space(id) ON DELETE CASCADE,
  connector_definition_id TEXT NOT NULL REFERENCES atlas.connector_definition(id),
  requested_by TEXT NOT NULL,
  requested_at TIMESTAMPTZ NOT NULL,
  source_scope TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'REVIEW_REQUIRED')),
  safe_message TEXT
);

CREATE INDEX idx_connector_sync_job_space_requested
  ON atlas.connector_sync_job (space_id, requested_at DESC);

CREATE TABLE atlas.connector_sync_run (
  id TEXT PRIMARY KEY,
  job_id TEXT NOT NULL REFERENCES atlas.connector_sync_job(id) ON DELETE CASCADE,
  connector_definition_id TEXT NOT NULL REFERENCES atlas.connector_definition(id),
  status TEXT NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'REVIEW_REQUIRED')),
  started_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ,
  item_count INTEGER NOT NULL DEFAULT 0 CHECK (item_count >= 0),
  review_required_count INTEGER NOT NULL DEFAULT 0 CHECK (review_required_count >= 0),
  failed_count INTEGER NOT NULL DEFAULT 0 CHECK (failed_count >= 0),
  safe_message TEXT
);

CREATE INDEX idx_connector_sync_run_job_started
  ON atlas.connector_sync_run (job_id, started_at DESC);

CREATE TABLE atlas.connector_sync_item (
  id TEXT PRIMARY KEY,
  run_id TEXT NOT NULL REFERENCES atlas.connector_sync_run(id) ON DELETE CASCADE,
  external_id TEXT NOT NULL,
  title TEXT NOT NULL,
  item_status TEXT NOT NULL CHECK (
    item_status IN ('DISCOVERED', 'FETCHED', 'OUTPUT_CREATED', 'REVIEW_REQUIRED', 'FAILED')
  ),
  source_reference TEXT NOT NULL,
  source_trace JSONB NOT NULL DEFAULT '{}'::jsonb,
  provenance JSONB NOT NULL DEFAULT '{}'::jsonb,
  confidence NUMERIC(4,3),
  review_eligible BOOLEAN NOT NULL DEFAULT FALSE,
  safe_error_category TEXT NOT NULL CHECK (
    safe_error_category IN (
      'NONE', 'VALIDATION', 'CONNECTOR_UNAVAILABLE',
      'UNSUPPORTED_SOURCE', 'SOURCE_UNREADABLE', 'SAFE_SYSTEM'
    )
  ),
  safe_error_message TEXT,
  discovered_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_connector_sync_item_run_status
  ON atlas.connector_sync_item (run_id, item_status, id);

CREATE TABLE atlas.connector_output_artifact (
  id TEXT PRIMARY KEY,
  item_id TEXT NOT NULL REFERENCES atlas.connector_sync_item(id) ON DELETE CASCADE,
  artifact_type TEXT NOT NULL CHECK (artifact_type IN ('MARKDOWN_CANDIDATE')),
  review_status TEXT NOT NULL CHECK (review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')),
  title TEXT NOT NULL,
  target_path TEXT NOT NULL,
  source_trace JSONB NOT NULL DEFAULT '{}'::jsonb,
  provenance JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_connector_output_artifact_item
  ON atlas.connector_output_artifact (item_id);

INSERT INTO atlas.connector_definition (
  id,
  connector_key,
  name,
  connector_type,
  status,
  version,
  capability_summary,
  configuration_state,
  review_policy,
  created_at,
  updated_at
) VALUES (
  'connector-definition-mock-local',
  'mock-local-fixture',
  'Mock Local Fixture',
  'LOCAL_FIXTURE',
  'AVAILABLE',
  'v0',
  'Sample-safe connector fixture with source trace and review-required handoff.',
  'MOCK_CONFIGURED',
  'REVIEW_REQUIRED',
  '2026-07-07T00:00:00Z',
  '2026-07-07T00:00:00Z'
) ON CONFLICT DO NOTHING;

CREATE TABLE atlas.worker_job (
  id TEXT PRIMARY KEY,
  job_type TEXT NOT NULL CHECK (job_type IN ('BATCH_INGEST', 'CONNECTOR_SYNC', 'ASYNC_PROCESSING')),
  subject_type TEXT NOT NULL,
  subject_id TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'WAITING_RETRY', 'SUCCEEDED', 'DEAD_LETTERED', 'ACKNOWLEDGED')),
  attempt_count INTEGER NOT NULL DEFAULT 0 CHECK (attempt_count >= 0),
  max_attempts INTEGER NOT NULL DEFAULT 3 CHECK (max_attempts > 0),
  retry_delay_seconds INTEGER,
  next_retry_at TIMESTAMPTZ,
  source_trace JSONB NOT NULL DEFAULT '{}'::jsonb,
  review_eligible BOOLEAN NOT NULL DEFAULT FALSE,
  safe_error_code TEXT,
  safe_error_category TEXT CHECK (
    safe_error_category IN (
      'NONE', 'VALIDATION', 'CONNECTOR_UNAVAILABLE', 'UNSUPPORTED_SOURCE',
      'SOURCE_UNREADABLE', 'RATE_LIMITED', 'SAFE_SYSTEM'
    )
  ),
  safe_error_message TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_worker_job_status_type
  ON atlas.worker_job (status, job_type);

CREATE INDEX idx_worker_job_subject
  ON atlas.worker_job (subject_type, subject_id);

CREATE TABLE atlas.worker_job_attempt (
  id TEXT PRIMARY KEY,
  worker_job_id TEXT NOT NULL REFERENCES atlas.worker_job(id) ON DELETE CASCADE,
  attempt_number INTEGER NOT NULL CHECK (attempt_number > 0),
  status TEXT NOT NULL CHECK (status IN ('RUNNING', 'FAILED_RETRYABLE', 'FAILED_TERMINAL', 'SUCCEEDED')),
  retryable BOOLEAN NOT NULL DEFAULT FALSE,
  safe_error_code TEXT,
  safe_error_category TEXT CHECK (
    safe_error_category IN (
      'NONE', 'VALIDATION', 'CONNECTOR_UNAVAILABLE', 'UNSUPPORTED_SOURCE',
      'SOURCE_UNREADABLE', 'RATE_LIMITED', 'SAFE_SYSTEM'
    )
  ),
  safe_error_message TEXT,
  source_trace JSONB NOT NULL DEFAULT '{}'::jsonb,
  started_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ,
  UNIQUE (worker_job_id, attempt_number)
);

CREATE INDEX idx_worker_job_attempt_job_number
  ON atlas.worker_job_attempt (worker_job_id, attempt_number);

CREATE TABLE atlas.dead_letter_entry (
  id TEXT PRIMARY KEY,
  worker_job_id TEXT NOT NULL UNIQUE REFERENCES atlas.worker_job(id) ON DELETE CASCADE,
  status TEXT NOT NULL CHECK (status IN ('OPEN', 'RETRIED', 'ACKNOWLEDGED')),
  job_type TEXT NOT NULL CHECK (job_type IN ('BATCH_INGEST', 'CONNECTOR_SYNC', 'ASYNC_PROCESSING')),
  subject_type TEXT NOT NULL,
  subject_id TEXT NOT NULL,
  attempt_summary TEXT NOT NULL,
  safe_error_code TEXT NOT NULL,
  safe_error_category TEXT NOT NULL CHECK (
    safe_error_category IN (
      'NONE', 'VALIDATION', 'CONNECTOR_UNAVAILABLE', 'UNSUPPORTED_SOURCE',
      'SOURCE_UNREADABLE', 'RATE_LIMITED', 'SAFE_SYSTEM'
    )
  ),
  safe_error_message TEXT NOT NULL,
  source_trace JSONB NOT NULL DEFAULT '{}'::jsonb,
  review_eligible BOOLEAN NOT NULL DEFAULT FALSE,
  operator_action_by TEXT,
  operator_action_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_dead_letter_entry_status_created
  ON atlas.dead_letter_entry (status, created_at DESC);

INSERT INTO atlas.worker_job (
  id,
  job_type,
  subject_type,
  subject_id,
  status,
  attempt_count,
  max_attempts,
  source_trace,
  review_eligible,
  safe_error_code,
  safe_error_category,
  safe_error_message,
  created_at,
  updated_at
) VALUES
(
  'worker-job-local-fixture-001',
  'CONNECTOR_SYNC',
  'connector-sync-run',
  'connector-run-local-fixture-001',
  'DEAD_LETTERED',
  3,
  3,
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-modernization-overview","section":"overview"}'::jsonb,
  false,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '2026-07-07T00:00:00Z',
  '2026-07-07T00:00:03Z'
),
(
  'worker-job-action-fixture-001',
  'CONNECTOR_SYNC',
  'connector-sync-run',
  'connector-run-action-fixture-001',
  'DEAD_LETTERED',
  3,
  3,
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-action-overview","section":"operations"}'::jsonb,
  false,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '2026-07-07T00:01:00Z',
  '2026-07-07T00:01:03Z'
) ON CONFLICT DO NOTHING;

INSERT INTO atlas.worker_job_attempt (
  id,
  worker_job_id,
  attempt_number,
  status,
  retryable,
  safe_error_code,
  safe_error_category,
  safe_error_message,
  source_trace,
  started_at,
  completed_at
) VALUES
(
  'worker-attempt-local-fixture-001',
  'worker-job-local-fixture-001',
  1,
  'FAILED_RETRYABLE',
  true,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-modernization-overview","section":"overview"}'::jsonb,
  '2026-07-07T00:00:00Z',
  '2026-07-07T00:00:01Z'
),
(
  'worker-attempt-local-fixture-002',
  'worker-job-local-fixture-001',
  2,
  'FAILED_RETRYABLE',
  true,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-modernization-overview","section":"overview"}'::jsonb,
  '2026-07-07T00:00:01Z',
  '2026-07-07T00:00:02Z'
),
(
  'worker-attempt-local-fixture-003',
  'worker-job-local-fixture-001',
  3,
  'FAILED_TERMINAL',
  true,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-modernization-overview","section":"overview"}'::jsonb,
  '2026-07-07T00:00:02Z',
  '2026-07-07T00:00:03Z'
),
(
  'worker-attempt-action-fixture-001',
  'worker-job-action-fixture-001',
  1,
  'FAILED_TERMINAL',
  false,
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-action-overview","section":"operations"}'::jsonb,
  '2026-07-07T00:01:00Z',
  '2026-07-07T00:01:01Z'
) ON CONFLICT DO NOTHING;

INSERT INTO atlas.dead_letter_entry (
  id,
  worker_job_id,
  status,
  job_type,
  subject_type,
  subject_id,
  attempt_summary,
  safe_error_code,
  safe_error_category,
  safe_error_message,
  source_trace,
  review_eligible,
  created_at
) VALUES
(
  'dead-letter-local-fixture-001',
  'worker-job-local-fixture-001',
  'OPEN',
  'CONNECTOR_SYNC',
  'connector-sync-run',
  'connector-run-local-fixture-001',
  '3/3 attempts failed',
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-modernization-overview","section":"overview"}'::jsonb,
  false,
  '2026-07-07T00:00:03Z'
),
(
  'dead-letter-action-fixture-001',
  'worker-job-action-fixture-001',
  'OPEN',
  'CONNECTOR_SYNC',
  'connector-sync-run',
  'connector-run-action-fixture-001',
  '1/3 attempts failed',
  'SAFE_SYSTEM_ERROR',
  'SOURCE_UNREADABLE',
  'The source could not be processed safely.',
  '{"connectorKey":"mock-local-fixture","sourceName":"Mock Local Fixture","sourceLocator":"fixture-action-overview","section":"operations"}'::jsonb,
  false,
  '2026-07-07T00:01:03Z'
) ON CONFLICT DO NOTHING;

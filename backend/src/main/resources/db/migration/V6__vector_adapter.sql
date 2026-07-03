CREATE TABLE atlas.vector_run (
  id text CONSTRAINT pk_vector_run PRIMARY KEY,
  space_id text NOT NULL,
  batch_id text,
  adapter_key text NOT NULL,
  adapter_version text,
  operation text NOT NULL CONSTRAINT ck_vector_run_operation CHECK (operation IN ('INDEX', 'DEINDEX')),
  status text NOT NULL CONSTRAINT ck_vector_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  mode text NOT NULL CONSTRAINT ck_vector_run_mode CHECK (mode IN ('mock', 'configured')),
  review_policy text NOT NULL CONSTRAINT ck_vector_run_review_policy CHECK (
    review_policy IN ('APPROVED_ONLY', 'INCLUDE_REVIEW_REQUIRED')
  ),
  dimension integer CONSTRAINT ck_vector_run_dimension CHECK (
    dimension IS NULL OR dimension > 0
  ),
  requested_by text,
  total_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_vector_run_total_count CHECK (total_count >= 0),
  indexed_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_vector_run_indexed_count CHECK (indexed_count >= 0),
  deleted_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_vector_run_deleted_count CHECK (deleted_count >= 0),
  skipped_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_vector_run_skipped_count CHECK (skipped_count >= 0),
  failed_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_vector_run_failed_count CHECK (failed_count >= 0),
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text,
  CONSTRAINT fk_vector_run_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_vector_run_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id)
);

CREATE INDEX idx_vector_run_space_status ON atlas.vector_run (space_id, status);
CREATE INDEX idx_vector_run_batch_status ON atlas.vector_run (batch_id, status);

CREATE TABLE atlas.vector_item_result (
  id text CONSTRAINT pk_vector_item_result PRIMARY KEY,
  run_id text NOT NULL,
  source_chunk_id text NOT NULL,
  file_item_id text NOT NULL,
  source_file text NOT NULL,
  page integer,
  section text,
  review_status text NOT NULL CONSTRAINT ck_vector_item_result_review_status CHECK (
    review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
  ),
  confidence numeric(4,3) CONSTRAINT ck_vector_item_result_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  vector_item_key text,
  status text NOT NULL CONSTRAINT ck_vector_item_result_status CHECK (
    status IN ('INDEXED', 'DELETED', 'SKIPPED', 'FAILED')
  ),
  score numeric(4,3) CONSTRAINT ck_vector_item_result_score CHECK (
    score IS NULL OR (score >= 0 AND score <= 1)
  ),
  safe_error text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_vector_item_result_run FOREIGN KEY (run_id) REFERENCES atlas.vector_run(id),
  CONSTRAINT fk_vector_item_result_source_chunk FOREIGN KEY (source_chunk_id) REFERENCES atlas.source_chunk(id),
  CONSTRAINT fk_vector_item_result_file_item FOREIGN KEY (file_item_id) REFERENCES atlas.file_item(id)
);

CREATE INDEX idx_vector_item_result_run_created_at
  ON atlas.vector_item_result (run_id, created_at);

CREATE INDEX idx_vector_item_result_source_chunk
  ON atlas.vector_item_result (source_chunk_id);

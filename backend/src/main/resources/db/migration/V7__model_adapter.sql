CREATE TABLE atlas.model_run (
  id text CONSTRAINT pk_model_run PRIMARY KEY,
  adapter_key text NOT NULL,
  model_key text NOT NULL,
  model_type text NOT NULL CONSTRAINT ck_model_run_model_type CHECK (
    model_type IN ('CHAT', 'EMBEDDING', 'RERANK', 'VISION', 'SPEECH')
  ),
  operation text NOT NULL CONSTRAINT ck_model_run_operation CHECK (
    operation IN ('CHAT', 'EMBEDDING', 'RERANK', 'VISION', 'SPEECH')
  ),
  status text NOT NULL CONSTRAINT ck_model_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  mode text NOT NULL CONSTRAINT ck_model_run_mode CHECK (mode IN ('mock', 'configured')),
  purpose text NOT NULL,
  requested_by text,
  input_reference text,
  safe_input_summary text,
  prompt_units integer NOT NULL DEFAULT 0 CONSTRAINT ck_model_run_prompt_units CHECK (
    prompt_units >= 0
  ),
  completion_units integer NOT NULL DEFAULT 0 CONSTRAINT ck_model_run_completion_units CHECK (
    completion_units >= 0
  ),
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text
);

CREATE INDEX idx_model_run_status_started_at
  ON atlas.model_run (status, started_at);

CREATE TABLE atlas.model_run_output (
  id text CONSTRAINT pk_model_run_output PRIMARY KEY,
  run_id text NOT NULL,
  kind text NOT NULL CONSTRAINT ck_model_run_output_kind CHECK (
    kind IN (
      'TEXT_SUMMARY', 'EMBEDDING_METADATA', 'RERANK_SCORES',
      'VISION_SUMMARY', 'SPEECH_SUMMARY', 'ERROR'
    )
  ),
  output_reference text,
  safe_summary text,
  ranked_item_ids text[],
  embedding_dimension integer CONSTRAINT ck_model_run_output_embedding_dimension CHECK (
    embedding_dimension IS NULL OR embedding_dimension > 0
  ),
  embedding_item_count integer CONSTRAINT ck_model_run_output_embedding_count CHECK (
    embedding_item_count IS NULL OR embedding_item_count >= 0
  ),
  confidence numeric(4,3) CONSTRAINT ck_model_run_output_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_model_run_output_review_status CHECK (review_status IN ('REVIEW_REQUIRED')),
  safe_error text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_model_run_output_run FOREIGN KEY (run_id) REFERENCES atlas.model_run(id),
  CONSTRAINT ck_model_run_output_embedding_fields CHECK (
    kind <> 'EMBEDDING_METADATA'
    OR (embedding_dimension IS NOT NULL AND embedding_item_count IS NOT NULL)
  )
);

CREATE INDEX idx_model_run_output_run_id
  ON atlas.model_run_output (run_id);

CREATE TABLE atlas.model_run_source_reference (
  id text CONSTRAINT pk_model_run_source_reference PRIMARY KEY,
  run_id text NOT NULL,
  ref_type text NOT NULL CONSTRAINT ck_model_run_source_ref_type CHECK (
    ref_type IN ('FILE_ITEM', 'SOURCE_CHUNK', 'WIKI_PAGE', 'GRAPH_NODE', 'ASK_CONTEXT')
  ),
  ref_id text NOT NULL,
  label text,
  confidence numeric(4,3) CONSTRAINT ck_model_run_source_ref_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_model_run_source_ref_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  CONSTRAINT fk_model_run_source_ref_run FOREIGN KEY (run_id) REFERENCES atlas.model_run(id)
);

CREATE INDEX idx_model_run_source_ref_run_id
  ON atlas.model_run_source_reference (run_id);

CREATE TABLE atlas.parser_run (
  id text CONSTRAINT pk_parser_run PRIMARY KEY,
  batch_id text NOT NULL,
  adapter_key text NOT NULL,
  adapter_version text,
  status text NOT NULL CONSTRAINT ck_parser_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  requested_by text,
  mode text NOT NULL CONSTRAINT ck_parser_run_mode CHECK (mode IN ('mock', 'configured')),
  low_confidence_threshold numeric(4,3) NOT NULL CONSTRAINT ck_parser_run_threshold CHECK (
    low_confidence_threshold >= 0 AND low_confidence_threshold <= 1
  ),
  skipped_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_parser_run_skipped_count CHECK (
    skipped_count >= 0
  ),
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text,
  CONSTRAINT fk_parser_run_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id)
);

CREATE INDEX idx_parser_run_batch_status ON atlas.parser_run (batch_id, status);

CREATE TABLE atlas.parser_file_result (
  id text CONSTRAINT pk_parser_file_result PRIMARY KEY,
  run_id text NOT NULL,
  file_item_id text NOT NULL,
  source_path text NOT NULL,
  pdf_path text,
  source_type text NOT NULL CONSTRAINT ck_parser_file_result_source_type
    CHECK (source_type IN ('pptx', 'docx', 'pdf', 'xlsx', 'image', 'unsupported')),
  status text NOT NULL CONSTRAINT ck_parser_file_result_status CHECK (
    status IN (
      'NEW', 'UPLOADED', 'PDF_CONVERTED', 'PDF_CONVERT_FAILED',
      'MARKDOWN_GENERATED', 'OCR_REQUIRED', 'LOW_CONFIDENCE',
      'REVIEW_REQUIRED', 'APPROVED', 'PUBLISHED', 'FAILED', 'UNSUPPORTED'
    )
  ),
  markdown_path text,
  assets_path text,
  confidence numeric(4,3) CONSTRAINT ck_parser_file_result_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  adapter_key text NOT NULL,
  chunk_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_parser_file_result_chunk_count CHECK (
    chunk_count >= 0
  ),
  skipped boolean NOT NULL DEFAULT false,
  safe_error text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_parser_file_result_run FOREIGN KEY (run_id) REFERENCES atlas.parser_run(id),
  CONSTRAINT fk_parser_file_result_file_item FOREIGN KEY (file_item_id) REFERENCES atlas.file_item(id)
);

CREATE INDEX idx_parser_file_result_run_created_at
  ON atlas.parser_file_result (run_id, created_at);

CREATE INDEX idx_parser_file_result_file_item
  ON atlas.parser_file_result (file_item_id);

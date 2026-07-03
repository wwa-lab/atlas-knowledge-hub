CREATE TABLE atlas.conversion_run (
  id text CONSTRAINT pk_conversion_run PRIMARY KEY,
  batch_id text NOT NULL,
  adapter_key text NOT NULL,
  adapter_version text,
  status text NOT NULL CONSTRAINT ck_conversion_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  requested_by text,
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text,
  CONSTRAINT fk_conversion_run_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id)
);

CREATE INDEX idx_conversion_run_batch_status ON atlas.conversion_run (batch_id, status);

CREATE TABLE atlas.conversion_file_result (
  id text CONSTRAINT pk_conversion_file_result PRIMARY KEY,
  run_id text NOT NULL,
  file_item_id text NOT NULL,
  source_path text NOT NULL,
  source_type text NOT NULL CONSTRAINT ck_conversion_file_result_source_type
    CHECK (source_type IN ('pptx', 'docx', 'pdf', 'xlsx', 'image', 'unsupported')),
  status text NOT NULL CONSTRAINT ck_conversion_file_result_status CHECK (
    status IN ('PDF_CONVERTED', 'PDF_CONVERT_FAILED', 'OCR_REQUIRED', 'FAILED', 'UNSUPPORTED')
  ),
  pdf_path text,
  confidence numeric(4,3) CONSTRAINT ck_conversion_file_result_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  adapter_key text NOT NULL,
  safe_error text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_conversion_file_result_run FOREIGN KEY (run_id) REFERENCES atlas.conversion_run(id),
  CONSTRAINT fk_conversion_file_result_file_item FOREIGN KEY (file_item_id) REFERENCES atlas.file_item(id)
);

CREATE INDEX idx_conversion_file_result_run_created_at
  ON atlas.conversion_file_result (run_id, created_at);

CREATE INDEX idx_conversion_file_result_file_item
  ON atlas.conversion_file_result (file_item_id);

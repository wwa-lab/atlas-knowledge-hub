CREATE TABLE atlas.ask_run (
  id TEXT PRIMARY KEY,
  space_id TEXT NOT NULL REFERENCES atlas.space(id) ON DELETE CASCADE,
  question TEXT NOT NULL,
  status TEXT NOT NULL CHECK (status IN ('REQUESTED','RETRIEVING','GENERATING','SUCCEEDED','NO_EVIDENCE','PARTIAL_FAILED','FAILED')),
  review_policy TEXT NOT NULL CHECK (review_policy IN ('APPROVED_ONLY','INCLUDE_REVIEW_REQUIRED')),
  mode TEXT NOT NULL CHECK (mode IN ('mock','configured')),
  requested_by TEXT NOT NULL,
  answer TEXT,
  answer_confidence NUMERIC(4,3),
  answer_review_status TEXT NOT NULL CHECK (answer_review_status IN ('REVIEW_REQUIRED','APPROVED','REJECTED','PUBLISHED')),
  model_run_id TEXT REFERENCES atlas.model_run(id),
  safe_message TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  completed_at TIMESTAMPTZ
);

CREATE INDEX idx_ask_run_space_status_created
  ON atlas.ask_run (space_id, status, created_at DESC);

CREATE TABLE atlas.ask_evidence (
  id TEXT PRIMARY KEY,
  ask_run_id TEXT NOT NULL REFERENCES atlas.ask_run(id) ON DELETE CASCADE,
  source_chunk_id TEXT NOT NULL REFERENCES atlas.source_chunk(id),
  file_item_id TEXT NOT NULL REFERENCES atlas.file_item(id),
  source_file TEXT NOT NULL,
  page INTEGER,
  section TEXT,
  review_status TEXT NOT NULL CHECK (review_status IN ('REVIEW_REQUIRED','APPROVED','REJECTED','PUBLISHED')),
  confidence NUMERIC(4,3),
  vector_item_key TEXT,
  score NUMERIC(4,3),
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ask_evidence_run_created
  ON atlas.ask_evidence (ask_run_id, created_at ASC);

CREATE INDEX idx_ask_evidence_source_chunk
  ON atlas.ask_evidence (source_chunk_id);

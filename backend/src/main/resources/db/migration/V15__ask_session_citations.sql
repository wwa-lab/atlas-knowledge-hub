CREATE TABLE atlas.ask_session (
  id TEXT PRIMARY KEY,
  space_id TEXT NOT NULL REFERENCES atlas.space(id) ON DELETE CASCADE,
  title TEXT NOT NULL,
  created_by TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_ask_session_space_updated
  ON atlas.ask_session (space_id, updated_at DESC);

INSERT INTO atlas.ask_session (id, space_id, title, created_by, created_at, updated_at)
SELECT
  'ask-session-' || id,
  space_id,
  LEFT(question, 120),
  requested_by,
  created_at,
  COALESCE(completed_at, created_at)
FROM atlas.ask_run;

ALTER TABLE atlas.ask_run
  ADD COLUMN session_id TEXT;

UPDATE atlas.ask_run
SET session_id = 'ask-session-' || id;

ALTER TABLE atlas.ask_run
  ALTER COLUMN session_id SET NOT NULL,
  ADD CONSTRAINT fk_ask_run_session
    FOREIGN KEY (session_id) REFERENCES atlas.ask_session(id) ON DELETE CASCADE;

CREATE INDEX idx_ask_run_session_created
  ON atlas.ask_run (session_id, created_at ASC);

ALTER TABLE atlas.ask_evidence
  ADD COLUMN citation_id TEXT,
  ADD COLUMN evidence_label TEXT,
  ADD COLUMN source_locator TEXT,
  ADD COLUMN citation_status TEXT,
  ADD COLUMN review_eligible BOOLEAN,
  ADD COLUMN excluded_reason TEXT;

UPDATE atlas.ask_evidence
SET
  citation_id = id,
  evidence_label = LEFT(source_file || COALESCE(' page ' || page::TEXT, ''), 160),
  source_locator = LEFT(
    COALESCE('page ' || page::TEXT, 'page n/a') || ' / ' ||
      COALESCE(section, 'section n/a') || ' / chunk ' || source_chunk_id,
    200
  ),
  citation_status = CASE
    WHEN review_status = 'REVIEW_REQUIRED' THEN 'REVIEW_REQUIRED'
    WHEN confidence IS NOT NULL AND confidence < 0.800 THEN 'LOW_CONFIDENCE'
    ELSE 'ELIGIBLE'
  END,
  review_eligible = CASE
    WHEN review_status IN ('APPROVED','PUBLISHED')
      AND (confidence IS NULL OR confidence >= 0.800)
    THEN TRUE
    ELSE FALSE
  END,
  excluded_reason = CASE
    WHEN review_status = 'REVIEW_REQUIRED' THEN 'Evidence requires review.'
    WHEN confidence IS NOT NULL AND confidence < 0.800 THEN 'Evidence confidence is low.'
    ELSE NULL
  END;

ALTER TABLE atlas.ask_evidence
  ALTER COLUMN citation_id SET NOT NULL,
  ALTER COLUMN evidence_label SET NOT NULL,
  ALTER COLUMN source_locator SET NOT NULL,
  ALTER COLUMN citation_status SET NOT NULL,
  ALTER COLUMN review_eligible SET NOT NULL,
  ADD CONSTRAINT ck_ask_evidence_citation_status CHECK (
    citation_status IN ('ELIGIBLE','REVIEW_REQUIRED','LOW_CONFIDENCE','MISSING_SOURCE_TRACE')
  );

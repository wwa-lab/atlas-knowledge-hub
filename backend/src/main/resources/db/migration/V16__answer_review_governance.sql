ALTER TABLE atlas.ask_run
  DROP CONSTRAINT IF EXISTS ask_run_answer_review_status_check;

UPDATE atlas.ask_run
SET answer_review_status = 'APPROVED'
WHERE answer_review_status = 'PUBLISHED';

ALTER TABLE atlas.ask_run
  ADD COLUMN answer_review_reason TEXT,
  ADD COLUMN answer_reviewed_by TEXT,
  ADD COLUMN answer_reviewed_at TIMESTAMPTZ,
  ADD CONSTRAINT ck_ask_run_answer_review_status
    CHECK (answer_review_status IN ('REVIEW_REQUIRED','APPROVED','REJECTED','NEEDS_REVISION'));

CREATE INDEX idx_ask_run_space_answer_review
  ON atlas.ask_run (space_id, answer_review_status, completed_at DESC);

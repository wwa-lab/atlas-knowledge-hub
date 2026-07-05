ALTER TABLE atlas.wiki_generation_run
  DROP CONSTRAINT IF EXISTS ck_wiki_generation_run_mode;

ALTER TABLE atlas.wiki_generation_run
  ADD CONSTRAINT ck_wiki_generation_run_mode CHECK (
    mode IN ('deterministic', 'model-assisted', 'linkify-lint')
  );

CREATE INDEX IF NOT EXISTS idx_wiki_page_issue_space_status_type_created
  ON atlas.wiki_page_issue (space_id, status, issue_type, created_at DESC, id);

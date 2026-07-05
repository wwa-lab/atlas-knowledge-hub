ALTER TABLE atlas.wiki_generation_run
  ADD COLUMN mode text NOT NULL DEFAULT 'deterministic',
  ADD COLUMN eligible_chunk_count integer NOT NULL DEFAULT 0,
  ADD COLUMN excluded_chunk_count integer NOT NULL DEFAULT 0;

ALTER TABLE atlas.wiki_generation_run
  ADD CONSTRAINT ck_wiki_generation_run_mode CHECK (
    mode IN ('deterministic', 'model-assisted')
  );

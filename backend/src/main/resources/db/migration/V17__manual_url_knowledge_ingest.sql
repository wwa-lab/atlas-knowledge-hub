ALTER TABLE atlas.batch
  DROP CONSTRAINT ck_batch_source_kind;

ALTER TABLE atlas.batch
  ADD CONSTRAINT ck_batch_source_kind CHECK (source_kind IN ('folder', 'zip', 'url'));

ALTER TABLE atlas.file_item
  DROP CONSTRAINT ck_file_item_source_type;

ALTER TABLE atlas.file_item
  ADD CONSTRAINT ck_file_item_source_type
    CHECK (source_type IN ('pptx', 'docx', 'pdf', 'xlsx', 'image', 'url', 'unsupported'));

CREATE TABLE atlas.manual_url_source (
  id text PRIMARY KEY,
  space_id text NOT NULL REFERENCES atlas.space(id) ON DELETE CASCADE,
  url_hash text NOT NULL,
  display_url text NOT NULL,
  host text NOT NULL,
  title text,
  description text,
  fetch_intent text NOT NULL,
  fetch_policy text NOT NULL,
  ingest_status text NOT NULL,
  review_status text NOT NULL,
  eligibility_status text NOT NULL,
  confidence numeric(4,3) NOT NULL,
  source_trace text NOT NULL,
  batch_id text NOT NULL REFERENCES atlas.batch(id) ON DELETE CASCADE,
  file_item_id text NOT NULL REFERENCES atlas.file_item(id) ON DELETE CASCADE,
  created_by text NOT NULL,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  CONSTRAINT manual_url_source_review_required
    CHECK (review_status = 'REVIEW_REQUIRED'),
  CONSTRAINT manual_url_source_metadata_only
    CHECK (fetch_policy = 'NO_FETCH_METADATA_ONLY'),
  CONSTRAINT manual_url_source_eligibility_review_required
    CHECK (eligibility_status = 'REVIEW_REQUIRED_ONLY'),
  CONSTRAINT manual_url_source_confidence_range
    CHECK (confidence >= 0 AND confidence <= 1)
);

CREATE UNIQUE INDEX manual_url_source_space_hash_idx
  ON atlas.manual_url_source(space_id, url_hash);

CREATE INDEX manual_url_source_space_created_idx
  ON atlas.manual_url_source(space_id, created_at DESC);

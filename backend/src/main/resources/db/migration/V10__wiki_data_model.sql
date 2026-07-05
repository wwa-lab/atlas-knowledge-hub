CREATE TABLE atlas.wiki_folder (
  id text CONSTRAINT pk_wiki_folder PRIMARY KEY,
  space_id text NOT NULL,
  parent_folder_id text,
  slug text NOT NULL,
  name text NOT NULL,
  description text,
  sort_order integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  CONSTRAINT uq_wiki_folder_id_space UNIQUE (id, space_id),
  CONSTRAINT uq_wiki_folder_space_slug UNIQUE (space_id, slug),
  CONSTRAINT fk_wiki_folder_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_wiki_folder_parent FOREIGN KEY (parent_folder_id, space_id)
    REFERENCES atlas.wiki_folder(id, space_id),
  CONSTRAINT ck_wiki_folder_sort_order CHECK (sort_order >= 0)
);

CREATE INDEX idx_wiki_folder_space_sort ON atlas.wiki_folder (space_id, sort_order, name);

ALTER TABLE atlas.wiki_page ADD COLUMN folder_id text;
ALTER TABLE atlas.wiki_page ADD COLUMN slug text;
ALTER TABLE atlas.wiki_page ADD COLUMN page_type text NOT NULL DEFAULT 'SOURCE_SUMMARY';
ALTER TABLE atlas.wiki_page ADD COLUMN aliases text[] NOT NULL DEFAULT ARRAY[]::text[];
ALTER TABLE atlas.wiki_page ADD COLUMN source_refs jsonb NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE atlas.wiki_page ADD COLUMN chunk_refs jsonb NOT NULL DEFAULT '[]'::jsonb;
ALTER TABLE atlas.wiki_page ADD COLUMN in_links text[] NOT NULL DEFAULT ARRAY[]::text[];
ALTER TABLE atlas.wiki_page ADD COLUMN out_links text[] NOT NULL DEFAULT ARRAY[]::text[];
ALTER TABLE atlas.wiki_page ADD COLUMN version integer NOT NULL DEFAULT 1;
ALTER TABLE atlas.wiki_page ADD COLUMN source_mode text NOT NULL DEFAULT 'PUBLISHED_FILE';
ALTER TABLE atlas.wiki_page ADD COLUMN refresh_policy text NOT NULL DEFAULT 'MANUAL';

WITH slug_candidates AS (
  SELECT
    id,
    space_id,
    COALESCE(
      NULLIF(
        trim(
          both '-' FROM regexp_replace(
            lower(
              regexp_replace(
                regexp_replace(COALESCE(markdown_path, title, id), '^.*/', ''),
                '\.[^.]*$',
                ''
              )
            ),
            '[^a-z0-9]+',
            '-',
            'g'
          )
        ),
        ''
      ),
      id
    ) AS base_slug
  FROM atlas.wiki_page
),
deduped_slugs AS (
  SELECT
    id,
    base_slug,
    row_number() OVER (PARTITION BY space_id, base_slug ORDER BY id) AS duplicate_number
  FROM slug_candidates
)
UPDATE atlas.wiki_page page
SET slug =
  CASE
    WHEN deduped.duplicate_number = 1 THEN deduped.base_slug
    ELSE deduped.base_slug || '-' || deduped.duplicate_number
  END
FROM deduped_slugs deduped
WHERE page.id = deduped.id;

UPDATE atlas.wiki_page page
SET source_refs = COALESCE(
  (
    SELECT jsonb_agg(
      jsonb_build_object(
        'type', 'FILE',
        'id', source_id,
        'label', source_id,
        'locator', page.markdown_path
      )
    )
    FROM unnest(page.source_document_ids) AS source_document(source_id)
    WHERE source_id IS NOT NULL AND btrim(source_id) <> ''
  ),
  '[]'::jsonb
);

ALTER TABLE atlas.wiki_page ALTER COLUMN slug SET NOT NULL;
ALTER TABLE atlas.wiki_page ADD CONSTRAINT uq_wiki_page_space_slug UNIQUE (space_id, slug);
ALTER TABLE atlas.wiki_page ADD CONSTRAINT fk_wiki_page_folder FOREIGN KEY (folder_id, space_id)
  REFERENCES atlas.wiki_folder(id, space_id);
ALTER TABLE atlas.wiki_page ADD CONSTRAINT ck_wiki_page_page_type CHECK (
  page_type IN ('INDEX', 'TOPIC', 'SOURCE_SUMMARY', 'ENTITY', 'CONCEPT', 'MANUAL')
);
ALTER TABLE atlas.wiki_page ADD CONSTRAINT ck_wiki_page_version CHECK (version >= 1);
ALTER TABLE atlas.wiki_page ADD CONSTRAINT ck_wiki_page_source_mode CHECK (
  source_mode IN ('PUBLISHED_FILE', 'AUTO_GENERATED', 'MANUAL', 'HYBRID')
);
ALTER TABLE atlas.wiki_page ADD CONSTRAINT ck_wiki_page_refresh_policy CHECK (
  refresh_policy IN ('MANUAL', 'ON_SOURCE_CHANGE', 'SCHEDULED', 'LOCKED')
);

CREATE INDEX idx_wiki_page_space_review_title
  ON atlas.wiki_page (space_id, review_status, title);
CREATE INDEX idx_wiki_page_space_page_type ON atlas.wiki_page (space_id, page_type);

CREATE TABLE atlas.wiki_generation_run (
  id text CONSTRAINT pk_wiki_generation_run PRIMARY KEY,
  space_id text NOT NULL,
  page_id text,
  status text NOT NULL CONSTRAINT ck_wiki_generation_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED', 'CANCELLED')
  ),
  source_mode text NOT NULL CONSTRAINT ck_wiki_generation_run_source_mode CHECK (
    source_mode IN ('PUBLISHED_FILE', 'AUTO_GENERATED', 'MANUAL', 'HYBRID')
  ),
  refresh_policy text NOT NULL CONSTRAINT ck_wiki_generation_run_refresh_policy CHECK (
    refresh_policy IN ('MANUAL', 'ON_SOURCE_CHANGE', 'SCHEDULED', 'LOCKED')
  ),
  requested_by text,
  input_source_refs jsonb NOT NULL DEFAULT '[]'::jsonb,
  created_page_ids text[] NOT NULL DEFAULT ARRAY[]::text[],
  updated_page_ids text[] NOT NULL DEFAULT ARRAY[]::text[],
  issue_ids text[] NOT NULL DEFAULT ARRAY[]::text[],
  safe_summary text,
  safe_error text,
  started_at timestamptz,
  finished_at timestamptz,
  CONSTRAINT fk_wiki_generation_run_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_wiki_generation_run_page FOREIGN KEY (page_id) REFERENCES atlas.wiki_page(id)
);

CREATE INDEX idx_wiki_generation_run_space_started
  ON atlas.wiki_generation_run (space_id, started_at DESC NULLS LAST, id);

CREATE TABLE atlas.wiki_log_entry (
  id text CONSTRAINT pk_wiki_log_entry PRIMARY KEY,
  space_id text NOT NULL,
  page_id text,
  run_id text,
  event_type text NOT NULL CONSTRAINT ck_wiki_log_entry_event_type CHECK (
    event_type IN (
      'PUBLISHED', 'REPUBLISHED', 'METADATA_UPDATED',
      'RUN_STARTED', 'RUN_FINISHED', 'ISSUE_RECORDED'
    )
  ),
  actor text,
  message text,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_wiki_log_entry_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_wiki_log_entry_page FOREIGN KEY (page_id) REFERENCES atlas.wiki_page(id),
  CONSTRAINT fk_wiki_log_entry_run FOREIGN KEY (run_id) REFERENCES atlas.wiki_generation_run(id)
);

CREATE INDEX idx_wiki_log_entry_page_created
  ON atlas.wiki_log_entry (page_id, created_at DESC, id);

CREATE TABLE atlas.wiki_page_issue (
  id text CONSTRAINT pk_wiki_page_issue PRIMARY KEY,
  space_id text NOT NULL,
  page_id text NOT NULL,
  issue_type text NOT NULL CONSTRAINT ck_wiki_page_issue_type CHECK (
    issue_type IN (
      'STALE_SOURCE', 'BROKEN_LINK', 'ORPHAN_PAGE',
      'THIN_CONTENT', 'REVIEW_REQUIRED', 'MISSING_SOURCE_REF'
    )
  ),
  severity text NOT NULL CONSTRAINT ck_wiki_page_issue_severity CHECK (
    severity IN ('INFO', 'LOW', 'MEDIUM', 'HIGH')
  ),
  status text NOT NULL CONSTRAINT ck_wiki_page_issue_status CHECK (
    status IN ('OPEN', 'ACKNOWLEDGED', 'RESOLVED', 'IGNORED')
  ),
  evidence_refs jsonb NOT NULL DEFAULT '[]'::jsonb,
  message text,
  created_at timestamptz NOT NULL,
  resolved_at timestamptz,
  CONSTRAINT fk_wiki_page_issue_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_wiki_page_issue_page FOREIGN KEY (page_id) REFERENCES atlas.wiki_page(id)
);

CREATE INDEX idx_wiki_page_issue_page_status_created
  ON atlas.wiki_page_issue (page_id, status, created_at DESC, id);

INSERT INTO atlas.wiki_folder (
  id, space_id, parent_folder_id, slug, name, description, sort_order, created_at, updated_at
) VALUES (
  'wiki-folder-foundation',
  'ibm-i-modernization',
  NULL,
  'foundation',
  'Foundation',
  'Sample-safe Wiki foundation pages.',
  10,
  '2026-07-05T00:00:00Z',
  '2026-07-05T00:00:00Z'
) ON CONFLICT DO NOTHING;

UPDATE atlas.wiki_page
SET folder_id = 'wiki-folder-foundation'
WHERE id = 'wiki-modernization-overview';

INSERT INTO atlas.wiki_generation_run (
  id, space_id, page_id, status, source_mode, refresh_policy, requested_by,
  input_source_refs, created_page_ids, updated_page_ids, issue_ids,
  safe_summary, safe_error, started_at, finished_at
) VALUES (
  'wiki-run-sample-001',
  'ibm-i-modernization',
  'wiki-modernization-overview',
  'SUCCEEDED',
  'PUBLISHED_FILE',
  'MANUAL',
  'system-sample',
  '[{"type":"FILE","id":"file-001","label":"file-001","locator":"generated/wiki/modernization-overview.md"}]'::jsonb,
  ARRAY[]::text[],
  ARRAY['wiki-modernization-overview'],
  ARRAY['wiki-issue-001'],
  'Sample-safe metadata refresh recorded.',
  NULL,
  '2026-07-05T00:00:00Z',
  '2026-07-05T00:00:03Z'
) ON CONFLICT DO NOTHING;

INSERT INTO atlas.wiki_log_entry (
  id, space_id, page_id, run_id, event_type, actor, message, metadata, created_at
) VALUES (
  'wiki-log-001',
  'ibm-i-modernization',
  'wiki-modernization-overview',
  'wiki-run-sample-001',
  'METADATA_UPDATED',
  'system-sample',
  'Recorded sample-safe Wiki foundation metadata.',
  '{"sourceMode":"PUBLISHED_FILE","refreshPolicy":"MANUAL"}'::jsonb,
  '2026-07-05T00:00:03Z'
) ON CONFLICT DO NOTHING;

INSERT INTO atlas.wiki_page_issue (
  id, space_id, page_id, issue_type, severity, status, evidence_refs, message, created_at, resolved_at
) VALUES (
  'wiki-issue-001',
  'ibm-i-modernization',
  'wiki-modernization-overview',
  'MISSING_SOURCE_REF',
  'LOW',
  'OPEN',
  '[{"type":"WIKI_PAGE","id":"wiki-modernization-overview","label":"page","locator":null}]'::jsonb,
  'Sample-safe issue placeholder for future lint workflows.',
  '2026-07-05T00:00:00Z',
  NULL
) ON CONFLICT DO NOTHING;

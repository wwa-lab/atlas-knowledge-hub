ALTER TABLE atlas.graph_node
  ADD COLUMN evidence_wiki_page_ids text[],
  ADD COLUMN confidence numeric(4,3)
    CONSTRAINT ck_graph_node_confidence CHECK (
      confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
    ),
  ADD COLUMN updated_at timestamptz;

ALTER TABLE atlas.graph_edge
  ADD COLUMN evidence_wiki_page_ids text[],
  ADD COLUMN confidence numeric(4,3)
    CONSTRAINT ck_graph_edge_confidence CHECK (
      confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
    ),
  ADD COLUMN updated_at timestamptz;

CREATE INDEX idx_graph_node_space_review
  ON atlas.graph_node (space_id, review_status);

CREATE INDEX idx_graph_edge_space_review
  ON atlas.graph_edge (space_id, review_status);

ALTER TABLE atlas.review_record
  DROP CONSTRAINT ck_review_record_target_type,
  ADD CONSTRAINT ck_review_record_target_type CHECK (
    target_type IN ('file', 'chunk', 'graph_edge')
  );

CREATE TABLE atlas.graph_projection_run (
  id text CONSTRAINT pk_graph_projection_run PRIMARY KEY,
  space_id text NOT NULL,
  status text NOT NULL CONSTRAINT ck_graph_projection_run_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  adapter_id text NOT NULL,
  scope text NOT NULL CONSTRAINT ck_graph_projection_run_scope CHECK (
    scope IN ('APPROVED_ONLY')
  ),
  requested_by text,
  created_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_graph_projection_run_created CHECK (
    created_count >= 0
  ),
  updated_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_graph_projection_run_updated CHECK (
    updated_count >= 0
  ),
  skipped_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_graph_projection_run_skipped CHECK (
    skipped_count >= 0
  ),
  failed_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_graph_projection_run_failed CHECK (
    failed_count >= 0
  ),
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text,
  CONSTRAINT fk_graph_projection_run_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE INDEX idx_graph_projection_run_space_started
  ON atlas.graph_projection_run (space_id, started_at);

CREATE TABLE atlas.graph_projection_item (
  id text CONSTRAINT pk_graph_projection_item PRIMARY KEY,
  run_id text NOT NULL,
  source_type text NOT NULL,
  source_id text NOT NULL,
  target_type text NOT NULL,
  target_id text,
  status text NOT NULL CONSTRAINT ck_graph_projection_item_status CHECK (
    status IN ('CREATED', 'UPDATED', 'SKIPPED', 'FAILED')
  ),
  reason_code text,
  CONSTRAINT fk_graph_projection_item_run FOREIGN KEY (run_id) REFERENCES atlas.graph_projection_run(id)
);

CREATE INDEX idx_graph_projection_item_run
  ON atlas.graph_projection_item (run_id);

CREATE TABLE atlas.graph_audit_record (
  id text CONSTRAINT pk_graph_audit_record PRIMARY KEY,
  space_id text NOT NULL,
  actor text NOT NULL,
  action text NOT NULL,
  target_type text NOT NULL,
  target_id text NOT NULL,
  safe_summary text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_graph_audit_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE INDEX idx_graph_audit_target_created
  ON atlas.graph_audit_record (target_type, target_id, created_at);

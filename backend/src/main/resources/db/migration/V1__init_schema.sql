CREATE SCHEMA IF NOT EXISTS atlas;

CREATE TABLE atlas.space (
  id text CONSTRAINT pk_space PRIMARY KEY,
  name text NOT NULL,
  description text,
  type text NOT NULL
    CONSTRAINT ck_space_type CHECK (type IN ('document', 'faq')),
  index_strategy text NOT NULL
    CONSTRAINT ck_space_index_strategy CHECK (index_strategy IN ('rag', 'wiki')),
  owner text,
  status text NOT NULL DEFAULT 'HEALTHY'
    CONSTRAINT ck_space_status CHECK (status IN ('HEALTHY', 'REVIEW_REQUIRED', 'PARSING')),
  document_count integer NOT NULL DEFAULT 0,
  wiki_page_count integer NOT NULL DEFAULT 0,
  review_count integer NOT NULL DEFAULT 0,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL
);

CREATE TABLE atlas.batch (
  id text CONSTRAINT pk_batch PRIMARY KEY,
  space_id text NOT NULL,
  name text NOT NULL,
  source_kind text NOT NULL
    CONSTRAINT ck_batch_source_kind CHECK (source_kind IN ('folder', 'zip')),
  owner text,
  uploaded_at timestamptz NOT NULL,
  CONSTRAINT fk_batch_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE INDEX idx_batch_space_uploaded_at ON atlas.batch (space_id, uploaded_at);

CREATE TABLE atlas.file_item (
  id text CONSTRAINT pk_file_item PRIMARY KEY,
  batch_id text NOT NULL,
  source_path text NOT NULL,
  source_type text NOT NULL
    CONSTRAINT ck_file_item_source_type
      CHECK (source_type IN ('pptx', 'docx', 'pdf', 'xlsx', 'image', 'unsupported')),
  status text NOT NULL
    CONSTRAINT ck_file_item_status CHECK (status IN (
      'NEW', 'UPLOADED', 'PDF_CONVERTED', 'PDF_CONVERT_FAILED',
      'MARKDOWN_GENERATED', 'OCR_REQUIRED', 'LOW_CONFIDENCE',
      'REVIEW_REQUIRED', 'APPROVED', 'PUBLISHED', 'FAILED', 'UNSUPPORTED'
    )),
  confidence numeric(4,3) CONSTRAINT ck_file_item_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_file_item_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  pdf_path text,
  markdown_path text,
  assets_path text,
  error_message text,
  created_at timestamptz NOT NULL,
  CONSTRAINT fk_file_item_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id)
);

CREATE INDEX idx_file_item_batch_status ON atlas.file_item (batch_id, status);

CREATE TABLE atlas.source_chunk (
  id text CONSTRAINT pk_source_chunk PRIMARY KEY,
  file_item_id text NOT NULL,
  source_file text NOT NULL,
  page integer,
  section text,
  confidence numeric(4,3) CONSTRAINT ck_source_chunk_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_source_chunk_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  CONSTRAINT fk_source_chunk_file_item FOREIGN KEY (file_item_id) REFERENCES atlas.file_item(id)
);

CREATE INDEX idx_source_chunk_file_item ON atlas.source_chunk (file_item_id);

CREATE TABLE atlas.review_record (
  id bigserial CONSTRAINT pk_review_record PRIMARY KEY,
  target_type text NOT NULL CONSTRAINT ck_review_record_target_type CHECK (
    target_type IN ('file', 'chunk')
  ),
  target_id text NOT NULL,
  action text NOT NULL
    CONSTRAINT ck_review_record_action CHECK (action IN ('APPROVE', 'NEED_FIX', 'OCR_REQUIRED')),
  reviewer text NOT NULL,
  comment text,
  affected_chunks text[],
  created_at timestamptz NOT NULL
);

CREATE INDEX idx_review_record_target_created_at
  ON atlas.review_record (target_type, target_id, created_at);

CREATE TABLE atlas.wiki_page (
  id text CONSTRAINT pk_wiki_page PRIMARY KEY,
  space_id text NOT NULL,
  title text NOT NULL,
  markdown_path text,
  source_document_ids text[],
  confidence numeric(4,3) CONSTRAINT ck_wiki_page_confidence CHECK (
    confidence IS NULL OR (confidence >= 0 AND confidence <= 1)
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_wiki_page_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  owner text,
  last_updated timestamptz,
  CONSTRAINT fk_wiki_page_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE TABLE atlas.graph_node (
  id text CONSTRAINT pk_graph_node PRIMARY KEY,
  space_id text NOT NULL,
  label text NOT NULL,
  type text NOT NULL CONSTRAINT ck_graph_node_type CHECK (
    type IN ('KNOWLEDGE_SPACE', 'DOCUMENT', 'WIKI_PAGE', 'CONCEPT', 'ENTITY', 'SOURCE_CHUNK')
  ),
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_graph_node_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  evidence_chunk_ids text[],
  CONSTRAINT fk_graph_node_space FOREIGN KEY (space_id) REFERENCES atlas.space(id)
);

CREATE TABLE atlas.graph_edge (
  id text CONSTRAINT pk_graph_edge PRIMARY KEY,
  space_id text NOT NULL,
  source_node_id text NOT NULL,
  target_node_id text NOT NULL,
  type text NOT NULL CONSTRAINT ck_graph_edge_type CHECK (
    type IN (
      'CONTAINS', 'DERIVED_FROM', 'MENTIONS', 'DEFINES', 'RELATED_TO',
      'BELONGS_TO', 'USES', 'DEPENDS_ON', 'REVIEWED_BY'
    )
  ),
  evidence_chunk_ids text[],
  review_status text NOT NULL DEFAULT 'REVIEW_REQUIRED'
    CONSTRAINT ck_graph_edge_review_status CHECK (
      review_status IN ('REVIEW_REQUIRED', 'APPROVED', 'NEED_FIX', 'OCR_REQUIRED', 'PUBLISHED')
    ),
  CONSTRAINT fk_graph_edge_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_graph_edge_source_node FOREIGN KEY (source_node_id) REFERENCES atlas.graph_node(id),
  CONSTRAINT fk_graph_edge_target_node FOREIGN KEY (target_node_id) REFERENCES atlas.graph_node(id)
);

CREATE TABLE atlas.storage_operation (
  id text CONSTRAINT pk_storage_operation PRIMARY KEY,
  batch_id text NOT NULL,
  workspace_id text,
  adapter_key text NOT NULL,
  adapter_version text,
  operation_type text NOT NULL CONSTRAINT ck_storage_operation_type CHECK (
    operation_type IN ('STORE', 'DELETE', 'LIST')
  ),
  status text NOT NULL CONSTRAINT ck_storage_operation_status CHECK (
    status IN ('REQUESTED', 'RUNNING', 'SUCCEEDED', 'PARTIAL_FAILED', 'FAILED')
  ),
  requested_by text,
  mode text NOT NULL CONSTRAINT ck_storage_operation_mode CHECK (mode IN ('mock', 'configured')),
  total_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_total_count CHECK (
    total_count >= 0
  ),
  stored_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_stored_count CHECK (
    stored_count >= 0
  ),
  deleted_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_deleted_count CHECK (
    deleted_count >= 0
  ),
  missing_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_missing_count CHECK (
    missing_count >= 0
  ),
  failed_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_failed_count CHECK (
    failed_count >= 0
  ),
  skipped_count integer NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_skipped_count CHECK (
    skipped_count >= 0
  ),
  total_bytes bigint NOT NULL DEFAULT 0 CONSTRAINT ck_storage_operation_total_bytes CHECK (
    total_bytes >= 0
  ),
  started_at timestamptz NOT NULL,
  completed_at timestamptz,
  safe_message text,
  CONSTRAINT fk_storage_operation_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id)
);

CREATE INDEX idx_storage_operation_batch_status
  ON atlas.storage_operation (batch_id, status);

CREATE TABLE atlas.storage_object (
  id text CONSTRAINT pk_storage_object PRIMARY KEY,
  operation_id text NOT NULL,
  batch_id text NOT NULL,
  file_item_id text,
  layer text NOT NULL CONSTRAINT ck_storage_object_layer CHECK (
    layer IN ('raw', 'pdf', 'markdown', 'assets', 'reports', 'wiki')
  ),
  object_key text NOT NULL,
  content_type text,
  size_bytes bigint CONSTRAINT ck_storage_object_size_bytes CHECK (
    size_bytes IS NULL OR size_bytes >= 0
  ),
  checksum text,
  adapter_key text NOT NULL,
  status text NOT NULL CONSTRAINT ck_storage_object_status CHECK (
    status IN ('STORED', 'DELETED', 'MISSING', 'FAILED')
  ),
  safe_error text,
  created_at timestamptz NOT NULL,
  updated_at timestamptz,
  CONSTRAINT fk_storage_object_operation FOREIGN KEY (operation_id) REFERENCES atlas.storage_operation(id),
  CONSTRAINT fk_storage_object_batch FOREIGN KEY (batch_id) REFERENCES atlas.batch(id),
  CONSTRAINT fk_storage_object_file_item FOREIGN KEY (file_item_id) REFERENCES atlas.file_item(id),
  CONSTRAINT ck_storage_object_stored_checksum CHECK (
    status <> 'STORED' OR (checksum IS NOT NULL AND btrim(checksum) <> '')
  )
);

CREATE INDEX idx_storage_object_operation_created_at
  ON atlas.storage_object (operation_id, created_at);

CREATE INDEX idx_storage_object_batch_layer_created_at
  ON atlas.storage_object (batch_id, layer, created_at);

CREATE INDEX idx_storage_object_file_item
  ON atlas.storage_object (file_item_id);

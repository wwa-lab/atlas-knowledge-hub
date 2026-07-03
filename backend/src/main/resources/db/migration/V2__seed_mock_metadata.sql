INSERT INTO atlas.space (
  id, name, description, type, index_strategy, owner, status,
  document_count, wiki_page_count, review_count, created_at, updated_at
) VALUES
  (
    'ibm-i-modernization',
    'IBM i Modernization',
    'Mock discovery package for modernization planning.',
    'document',
    'rag',
    'Platform Team',
    'REVIEW_REQUIRED',
    6,
    2,
    3,
    '2026-06-01T09:00:00Z',
    '2026-06-20T14:30:00Z'
  ),
  (
    'claims-knowledge-base',
    'Claims Knowledge Base',
    'Mock FAQ-oriented operational knowledge space.',
    'faq',
    'wiki',
    'Operations Team',
    'HEALTHY',
    3,
    4,
    0,
    '2026-06-02T09:00:00Z',
    '2026-06-18T11:00:00Z'
  );

INSERT INTO atlas.batch (id, space_id, name, source_kind, owner, uploaded_at) VALUES
  (
    'batch-2026-06-20-001',
    'ibm-i-modernization',
    'Discovery Package',
    'folder',
    'Delivery Lead',
    '2026-06-20T14:30:00Z'
  );

INSERT INTO atlas.file_item (
  id, batch_id, source_path, source_type, status, confidence, review_status,
  pdf_path, markdown_path, assets_path, error_message, created_at
) VALUES
  (
    'file-001',
    'batch-2026-06-20-001',
    'Discovery/BRD/BRD.docx',
    'docx',
    'MARKDOWN_GENERATED',
    0.820,
    'REVIEW_REQUIRED',
    'generated/pdf/BRD.pdf',
    'generated/md/BRD.md',
    'generated/assets/BRD/',
    NULL,
    '2026-06-20T14:31:00Z'
  ),
  (
    'file-002',
    'batch-2026-06-20-001',
    'Discovery/Architecture/Current-State.pdf',
    'pdf',
    'PDF_CONVERTED',
    0.910,
    'REVIEW_REQUIRED',
    'generated/pdf/Current-State.pdf',
    NULL,
    NULL,
    NULL,
    '2026-06-20T14:32:00Z'
  ),
  (
    'file-003',
    'batch-2026-06-20-001',
    'Discovery/Scans/legacy-screen.png',
    'image',
    'OCR_REQUIRED',
    0.410,
    'REVIEW_REQUIRED',
    NULL,
    NULL,
    'generated/assets/legacy-screen/',
    'OCR is required before Markdown generation.',
    '2026-06-20T14:33:00Z'
  ),
  (
    'file-004',
    'batch-2026-06-20-001',
    'Discovery/notes.txt',
    'unsupported',
    'UNSUPPORTED',
    0.000,
    'REVIEW_REQUIRED',
    NULL,
    NULL,
    NULL,
    'Unsupported source type.',
    '2026-06-20T14:34:00Z'
  ),
  (
    'file-005',
    'batch-2026-06-20-001',
    'Discovery/Data/inventory.xlsx',
    'xlsx',
    'LOW_CONFIDENCE',
    0.540,
    'REVIEW_REQUIRED',
    'generated/pdf/inventory.pdf',
    'generated/md/inventory.md',
    'generated/assets/inventory/',
    'Low confidence table extraction.',
    '2026-06-20T14:35:00Z'
  ),
  (
    'file-006',
    'batch-2026-06-20-001',
    'Discovery/Conversion/broken.pptx',
    'pptx',
    'PDF_CONVERT_FAILED',
    0.000,
    'REVIEW_REQUIRED',
    NULL,
    NULL,
    NULL,
    'Mock conversion failure.',
    '2026-06-20T14:36:00Z'
  );

INSERT INTO atlas.source_chunk (
  id, file_item_id, source_file, page, section, confidence, review_status
) VALUES
  (
    'chunk-file-001-p12-b02',
    'file-001',
    'BRD.docx',
    12,
    'Scope',
    0.820,
    'REVIEW_REQUIRED'
  ),
  (
    'chunk-file-005-p03-b01',
    'file-005',
    'inventory.xlsx',
    3,
    'Application Inventory',
    0.540,
    'REVIEW_REQUIRED'
  );

INSERT INTO atlas.review_record (
  target_type, target_id, action, reviewer, comment, affected_chunks, created_at
) VALUES
  (
    'file',
    'file-001',
    'NEED_FIX',
    'sme.alex',
    'Terminology mismatch in section 3.',
    ARRAY['chunk-file-001-p12-b02'],
    '2026-06-21T10:15:00Z'
  );

INSERT INTO atlas.wiki_page (
  id, space_id, title, markdown_path, source_document_ids, confidence,
  review_status, owner, last_updated
) VALUES
  (
    'wiki-modernization-overview',
    'ibm-i-modernization',
    'Modernization Overview',
    'generated/wiki/modernization-overview.md',
    ARRAY['file-001', 'file-005'],
    0.780,
    'REVIEW_REQUIRED',
    'SME Team',
    '2026-06-21T11:00:00Z'
  );

INSERT INTO atlas.graph_node (
  id, space_id, label, type, review_status, evidence_chunk_ids
) VALUES
  (
    'node-ks-ibm-i',
    'ibm-i-modernization',
    'IBM i Modernization',
    'KNOWLEDGE_SPACE',
    'REVIEW_REQUIRED',
    ARRAY['chunk-file-001-p12-b02']
  ),
  (
    'node-concept-application-inventory',
    'ibm-i-modernization',
    'Application Inventory',
    'CONCEPT',
    'REVIEW_REQUIRED',
    ARRAY['chunk-file-005-p03-b01']
  );

INSERT INTO atlas.graph_edge (
  id, space_id, source_node_id, target_node_id, type, evidence_chunk_ids, review_status
) VALUES
  (
    'edge-ks-contains-inventory',
    'ibm-i-modernization',
    'node-ks-ibm-i',
    'node-concept-application-inventory',
    'CONTAINS',
    ARRAY['chunk-file-005-p03-b01'],
    'REVIEW_REQUIRED'
  );

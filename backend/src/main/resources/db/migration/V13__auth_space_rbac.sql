CREATE TABLE atlas.atlas_user (
  id text CONSTRAINT pk_atlas_user PRIMARY KEY,
  email text NOT NULL CONSTRAINT uq_atlas_user_email UNIQUE,
  display_name text NOT NULL,
  status text NOT NULL
    CONSTRAINT ck_atlas_user_status CHECK (status IN ('ACTIVE', 'DISABLED')),
  global_roles text[] NOT NULL DEFAULT ARRAY[]::text[],
  auth_subject text,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL
);

CREATE TABLE atlas.space_membership (
  id text CONSTRAINT pk_space_membership PRIMARY KEY,
  space_id text NOT NULL,
  user_id text NOT NULL,
  role text NOT NULL CONSTRAINT ck_space_membership_role CHECK (
    role IN ('VIEWER', 'EDITOR', 'KNOWLEDGE_MANAGER', 'SPACE_OWNER', 'AUDITOR', 'PLATFORM_ADMIN')
  ),
  status text NOT NULL CONSTRAINT ck_space_membership_status CHECK (
    status IN ('INVITED', 'ACTIVE', 'SUSPENDED', 'REMOVED')
  ),
  invited_by_user_id text,
  created_at timestamptz NOT NULL,
  updated_at timestamptz NOT NULL,
  CONSTRAINT fk_space_membership_space FOREIGN KEY (space_id) REFERENCES atlas.space(id),
  CONSTRAINT fk_space_membership_user FOREIGN KEY (user_id) REFERENCES atlas.atlas_user(id),
  CONSTRAINT fk_space_membership_invited_by FOREIGN KEY (invited_by_user_id) REFERENCES atlas.atlas_user(id)
);

CREATE UNIQUE INDEX uq_space_membership_space_user_active
  ON atlas.space_membership (space_id, user_id)
  WHERE status IN ('INVITED', 'ACTIVE', 'SUSPENDED');

CREATE INDEX idx_space_membership_user_status ON atlas.space_membership (user_id, status);
CREATE INDEX idx_space_membership_space_role_status
  ON atlas.space_membership (space_id, role, status);

INSERT INTO atlas.atlas_user (
  id, email, display_name, status, global_roles, auth_subject, created_at, updated_at
) VALUES
  (
    'mock-owner',
    'owner@example.test',
    'Atlas Owner',
    'ACTIVE',
    ARRAY['PLATFORM_ADMIN'],
    'mock:owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'mock-viewer',
    'viewer@example.test',
    'Atlas Viewer',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:viewer',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'mock-editor',
    'editor@example.test',
    'Atlas Editor',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:editor',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'mock-manager',
    'manager@example.test',
    'Atlas Knowledge Manager',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:manager',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'mock-auditor',
    'auditor@example.test',
    'Atlas Auditor',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:auditor',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'mock-disabled',
    'disabled@example.test',
    'Disabled Atlas User',
    'DISABLED',
    ARRAY[]::text[],
    'mock:disabled',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'frontend-demo',
    'frontend-demo@example.test',
    'Frontend Demo',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:frontend-demo',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'delivery-lead',
    'delivery-lead@example.test',
    'Delivery Lead',
    'ACTIVE',
    ARRAY['PLATFORM_ADMIN'],
    'mock:delivery-lead',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'sme.alex',
    'sme.alex@example.test',
    'SME Alex',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:sme.alex',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'viewer',
    'viewer.legacy@example.test',
    'Legacy Viewer',
    'ACTIVE',
    ARRAY[]::text[],
    'mock:viewer-legacy',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  );

INSERT INTO atlas.space_membership (
  id, space_id, user_id, role, status, invited_by_user_id, created_at, updated_at
) VALUES
  (
    'membership-owner-ibmi',
    'ibm-i-modernization',
    'mock-owner',
    'SPACE_OWNER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-viewer-ibmi',
    'ibm-i-modernization',
    'mock-viewer',
    'VIEWER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-editor-ibmi',
    'ibm-i-modernization',
    'mock-editor',
    'EDITOR',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-manager-ibmi',
    'ibm-i-modernization',
    'mock-manager',
    'KNOWLEDGE_MANAGER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-auditor-ibmi',
    'ibm-i-modernization',
    'mock-auditor',
    'AUDITOR',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-frontend-ibmi',
    'ibm-i-modernization',
    'frontend-demo',
    'SPACE_OWNER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-sme-ibmi',
    'ibm-i-modernization',
    'sme.alex',
    'KNOWLEDGE_MANAGER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-legacy-viewer-ibmi',
    'ibm-i-modernization',
    'viewer',
    'VIEWER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-owner-claims',
    'claims-knowledge-base',
    'mock-owner',
    'SPACE_OWNER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  ),
  (
    'membership-viewer-claims',
    'claims-knowledge-base',
    'mock-viewer',
    'VIEWER',
    'ACTIVE',
    'mock-owner',
    '2026-07-05T00:00:00Z',
    '2026-07-05T00:00:00Z'
  );

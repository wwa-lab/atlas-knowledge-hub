# Data Model: Knowledge Space

## Status

Draft.

## Entities

### KnowledgeSpace

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable slug, e.g. `ibm-i-modernization`. |
| `name` | string | Display name. |
| `description` | string | Short product-facing summary. |
| `owner` | string | Responsible team or person. |
| `status` | enum | `Healthy`, `Review Required`, `Parsing`. |
| `document_count` | number | Total file or document count. |
| `wiki_page_count` | number | Published or draft Wiki page count. |
| `review_count` | number | Open review task count. |
| `last_updated` | date | Display and sorting value. |

### Batch

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable batch identifier. |
| `space_id` | string | Owning KnowledgeSpace. |
| `name` | string | Human-readable batch name. |
| `source_package_type` | enum | `folder`, `zip`. |
| `total_files` | number | Inventory count. |
| `pdf_converted` | number | Count of PDF converted files. |
| `markdown_generated` | number | Count of Markdown outputs. |
| `review_required` | number | Count of review-required items. |
| `failed` | number | Count of failed items. |
| `created_by` | string | Owner or uploader. |
| `created_at` | datetime | Creation time. |

### FileItem

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable file item ID. |
| `batch_id` | string | Owning batch. |
| `source_path` | string | Relative path only. |
| `source_type` | string | Original extension/type. |
| `pdf_path` | string | Generated PDF path if available. |
| `markdown_path` | string | Generated Markdown path if available. |
| `assets_path` | string | Extracted assets path if available. |
| `status` | enum | See batch status flow. |
| `confidence` | number | Parser or normalizer confidence. |
| `review_status` | enum | See review states. |
| `error_message` | string | User-safe summary when failed. |

### WikiPage

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable page ID. |
| `space_id` | string | Owning space. |
| `title` | string | Page title. |
| `markdown_path` | string | Normalized Markdown location. |
| `source_document_ids` | string[] | Documents used by the page. |
| `confidence` | number | Page-level confidence. |
| `review_status` | enum | `REVIEW_REQUIRED`, `APPROVED`, `PUBLISHED`, etc. |
| `owner` | string | Responsible reviewer/team. |
| `last_updated` | datetime | Last content or metadata update. |

### SourceChunk

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable chunk ID. |
| `wiki_page_id` | string | Owning page. |
| `source_file` | string | Source file name. |
| `pdf_file` | string | Generated PDF name/path. |
| `page` | number | Page number when available. |
| `section` | string | Source or Wiki section label. |
| `confidence` | number | Chunk-level confidence. |
| `review_status` | enum | Review state for the chunk. |

### ReviewTask

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable review task ID. |
| `target_type` | enum | `file`, `page`, `chunk`, `graph_edge`. |
| `target_id` | string | Target entity ID. |
| `source_file` | string | Display source. |
| `page` | number | Source page when available. |
| `confidence` | number | Confidence prompting review. |
| `status` | enum | `REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`. |
| `comments` | string | SME comments. |
| `reviewer` | string | Reviewer identity/team. |

### GraphNode

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable node ID. |
| `space_id` | string | Owning space. |
| `label` | string | Display label. |
| `type` | enum | `KnowledgeSpace`, `Document`, `WikiPage`, `Concept`, `Entity`, `SourceChunk`. |
| `review_status` | enum | Relationship to review gate. |
| `evidence_chunk_ids` | string[] | Evidence backing the node. |

### GraphEdge

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable edge ID. |
| `space_id` | string | Owning space. |
| `source_node_id` | string | Source graph node. |
| `target_node_id` | string | Target graph node. |
| `type` | enum | `CONTAINS`, `DERIVED_FROM`, `MENTIONS`, `DEFINES`, `RELATED_TO`, `BELONGS_TO`, `USES`, `DEPENDS_ON`, `REVIEWED_BY`. |
| `evidence_chunk_ids` | string[] | Required for trustable edges. |
| `review_status` | enum | Review state for the relationship. |

### AskEvidenceBundle

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable bundle ID. |
| `space_id` | string | Selected context. |
| `question` | string | User question. |
| `answer` | string | Generated or mock answer. |
| `source_refs` | object[] | File, page, chunk references. |
| `confidence` | enum | `low`, `medium`, `high`. |
| `review_status` | enum | Review state for generated answer. |

### UiPreference

Prototype mode may keep these values in local UI state or `localStorage`. Future implementation may persist them per user.

| Field | Type | Notes |
|---|---|---|
| `language` | enum | `zh-CN`, `en-US`. |
| `theme` | enum | `day`, `night`. |
| `updated_at` | datetime | Last preference change when persisted. |

### ModelConfig

Prototype mode uses mock model configuration only. Future implementation must store secrets outside frontend code.

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable model config ID. |
| `provider` | string | Provider name, e.g. `DeepSeek`, `GitHub Models`, `Ollama`. |
| `source_type` | enum | `api`, `ollama`, `copilot_policy`, `custom`. |
| `model_type` | enum | `chat`, `embedding`, `rerank`, `vision`, `speech`. |
| `model_name` | string | Provider model identifier. |
| `display_name` | string | UI display name. |
| `base_url` | string | API endpoint when applicable. |
| `api_key_status` | enum | `not_configured`, `configured`, `rotating`. Never store raw key in UI model. |
| `custom_headers` | object | Optional header names and masked values. |
| `supports_multimodal` | boolean | Whether image or mixed input is supported. |
| `thinking_parameter_format` | enum | `none`, `provider_default`, `custom`. |
| `enabled` | boolean | Whether the config is available for use. |
| `notes` | string | Provider-specific caveats, e.g. GitHub plan or org policy availability. |

### UserAccount

Prototype mode uses mock users only. Future implementation must store credentials and identities securely.

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable user ID. |
| `name` | string | Display name. |
| `email` | string | User email. |
| `status` | enum | `pending`, `active`, `disabled`. |
| `created_at` | datetime | Registration or creation time. |
| `last_login_at` | datetime | Optional activity metadata. |

### SpaceMembership

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable membership ID. |
| `space_id` | string | Owning KnowledgeSpace. |
| `user_id` | string | Member user. |
| `role` | enum | `owner`, `admin`, `reviewer`, `viewer`. |
| `joined_at` | datetime | Join time. |
| `invited_by` | string | User ID of inviter when available. |

### SpaceInvitation

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable invitation ID. |
| `space_id` | string | Target KnowledgeSpace. |
| `email` | string | Invitee email. |
| `role` | enum | `admin`, `reviewer`, `viewer`. |
| `status` | enum | `pending`, `accepted`, `expired`, `revoked`. |
| `expires_at` | datetime | Expiration time. |
| `invited_by` | string | User ID of inviter. |

### RegistrationPolicy

| Field | Type | Notes |
|---|---|---|
| `enabled` | boolean | Whether self-registration is allowed. |
| `mode` | enum | `open`, `domain_restricted`, `invite_only`. |
| `allowed_domains` | string[] | Approved email domains. |
| `default_role` | enum | Default role after approval, usually `viewer`. |
| `requires_approval` | boolean | Whether owner/admin approval is needed. |

### AccountSettings

| Field | Type | Notes |
|---|---|---|
| `user_id` | string | Owning user. |
| `language` | enum | `zh-CN`, `en-US`. |
| `theme` | enum | `day`, `night`. |
| `default_space_id` | string | Default landing Knowledge Space. |
| `notifications_enabled` | boolean | Whether product notifications are enabled. |

### ApiAccessInfo

| Field | Type | Notes |
|---|---|---|
| `user_id` | string | Owning user. |
| `api_key_status` | enum | `not_configured`, `configured`, `rotating`, `revoked`. |
| `scopes` | string[] | Allowed API scopes. |
| `last_used_at` | datetime | Last API key usage time. |
| `created_at` | datetime | Key creation time. |

### EngineConfig

| Field | Type | Notes |
|---|---|---|
| `id` | string | Stable engine config ID. |
| `engine_type` | enum | `vector_database`, `parser`, `storage`. |
| `name` | string | Display name. |
| `adapter` | string | Adapter implementation name. |
| `status` | enum | `default`, `available`, `disabled`, `experimental`. |
| `config_summary` | string | User-safe summary, no secrets. |
| `is_default` | boolean | Whether the engine is default for its type. |

## Data Safety Rules

- Paths must be relative inside repo/sample storage, not private absolute paths.
- Mock data must not contain real company content.
- Error messages must be user-safe and avoid leaking internal command details.
- Raw API keys must never be committed or stored in frontend source.
- Passwords and credential material must never appear in prototype mock data.
- Future RBAC must be enforced server-side, not only in the frontend.
- Engine configuration must not expose secret values or bind product workflows directly to a single engine implementation.

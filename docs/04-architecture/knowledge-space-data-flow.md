# Data Flow: Knowledge Space

## Status

Draft.

## Prototype Flow

```text
Open prototype
  -> Home dialogue mode and Knowledge Space cards render from mock data
  -> User clicks IBM i Modernization card or knowledge base item
  -> Space detail opens
  -> User switches among Wiki, Documents, Graph, Review, and Ask tabs
  -> UI updates in memory only
```

## Target Batch Flow

```text
Create or open Knowledge Space
  -> Upload folder or ZIP
  -> Create Batch
  -> Inventory files
  -> Convert Office documents to PDF through converter adapter
  -> Parse PDFs to Markdown/assets through parser adapter
  -> Normalize Markdown to Atlas standard
  -> Validate source trace, confidence, and review status
  -> Create review tasks for low-confidence or generated content
  -> SME approves, requests fix, or marks OCR required
  -> Approved content publishes to LM Wiki
  -> Reviewed Wiki and source chunks derive graph and Ask evidence
```

## Status Flow

File statuses follow `docs/batch-processing-design.md`:

```text
NEW -> UPLOADED -> PDF_CONVERTED -> MARKDOWN_GENERATED -> REVIEW_REQUIRED -> APPROVED -> PUBLISHED
                    |                 |                    |
                    v                 v                    v
            PDF_CONVERT_FAILED   LOW_CONFIDENCE      OCR_REQUIRED
                    |
                    v
                  FAILED
```

## Review Flow

```text
Markdown generated
  -> confidence/source trace validation
  -> REVIEW_REQUIRED when low confidence or LLM-generated
  -> SME side-by-side review
  -> APPROVED | NEED_FIX | OCR_REQUIRED
  -> PUBLISHED only after approval
```

## Ask Flow

```text
User selects knowledge context
  -> User asks question
  -> Retrieve approved Wiki pages and evidence chunks
  -> Compose evidence bundle
  -> Generate answer with sources and confidence
  -> Mark answer as review-required or medium confidence when evidence is incomplete
```

## Empty And Error States

- No spaces: show empty state and create-space action.
- No selected knowledge context: disable or label Ask as requiring context.
- No batch: show upload folder and upload ZIP actions.
- Conversion failure: show failed count and report entry.
- OCR required: show OCR status and review queue entry.
- No approved evidence: Ask should say no trusted answer is available.

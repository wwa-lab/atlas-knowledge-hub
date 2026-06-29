# Review Workflow

The review workflow turns parser output into trusted Wiki content.

## Steps

1. Parser generates Markdown and extracted assets.
2. Markdown normalizer adds front matter, source trace, confidence, and review status.
3. Low-confidence detection flags sections, pages, or files.
4. SME reviews Markdown side by side with source PDF or extracted image.
5. SME chooses one action:
   - Approve.
   - Need Fix.
   - OCR Required.
6. Approved content is published to LM Wiki.
7. Review history records reviewer, action, timestamp, comments, and affected chunks.

## Review States

- `REVIEW_REQUIRED`: Needs human review before publication.
- `APPROVED`: SME approved the content.
- `NEED_FIX`: Content requires correction or reprocessing.
- `OCR_REQUIRED`: Source quality requires OCR or manual intervention.
- `PUBLISHED`: Approved content is visible in the Wiki.

## Review Principles

- Do not publish low-confidence content without review.
- Do not treat LLM-enriched text as approved by default.
- Keep source trace visible during review.
- Preserve original parser output when corrections are made.

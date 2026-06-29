# Markdown Normalizer Prompt Notes

Normalize parser output into Atlas Markdown.

Required output:

- YAML front matter with all required fields from `docs/markdown-standard.md`.
- Clear headings.
- Page-level source trace comments.
- Confidence values when available.
- `review_status: "REVIEW_REQUIRED"` for LLM-generated or low-confidence content.

Do not invent source paths, reviewers, or approval states.

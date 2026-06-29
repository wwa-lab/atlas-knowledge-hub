# Knowledge Graph Design

The initial graph is lightweight and explainable. It should be derived from approved Wiki pages and source traces, not from unreviewed raw parser output.

## Node Types

- `KnowledgeSpace`: Project or domain container.
- `Document`: Original uploaded or converted document.
- `WikiPage`: Published normalized Markdown page.
- `Concept`: Business or technical concept.
- `Entity`: System, table, application, team, vendor, person, or platform.
- `SourceChunk`: Traceable source excerpt from a page, slide, section, or block.

## Edge Types

- `CONTAINS`: KnowledgeSpace contains documents, pages, concepts, or entities.
- `DERIVED_FROM`: WikiPage or SourceChunk derives from a source document.
- `MENTIONS`: Page or chunk mentions a concept or entity.
- `DEFINES`: Page or chunk defines a concept.
- `RELATED_TO`: General reviewed relationship between concepts or entities.
- `BELONGS_TO`: Entity belongs to a space, domain, or system group.
- `USES`: Entity or system uses another entity, platform, or interface.
- `DEPENDS_ON`: Concept, page, or system depends on another node.
- `REVIEWED_BY`: Page, chunk, or relationship was reviewed by an SME.

## Graph Rule

Graph edges should keep evidence. A graph relationship is more useful when it points back to the Wiki page and source chunk that justify it.

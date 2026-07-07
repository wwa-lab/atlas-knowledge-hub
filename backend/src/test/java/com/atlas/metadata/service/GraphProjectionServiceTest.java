package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.DeterministicGraphProjectionAdapter;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.GraphAuditRecord;
import com.atlas.metadata.domain.GraphEdge;
import com.atlas.metadata.domain.GraphNode;
import com.atlas.metadata.domain.GraphProjectionItem;
import com.atlas.metadata.domain.GraphProjectionRun;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.GraphProjectionItemStatus;
import com.atlas.metadata.enums.GraphProjectionStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.GraphAuditRecordRepository;
import com.atlas.metadata.repository.GraphEdgeRepository;
import com.atlas.metadata.repository.GraphNodeRepository;
import com.atlas.metadata.repository.GraphProjectionItemRepository;
import com.atlas.metadata.repository.GraphProjectionRunRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for graph projection trust gates and audit evidence. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GraphProjectionServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;
  @Mock private GraphNodeRepository graphNodeRepository;
  @Mock private GraphEdgeRepository graphEdgeRepository;
  @Mock private GraphProjectionRunRepository graphProjectionRunRepository;
  @Mock private GraphProjectionItemRepository graphProjectionItemRepository;
  @Mock private GraphAuditRecordRepository graphAuditRecordRepository;
  @Mock private WikiPageRepository wikiPageRepository;

  private final List<GraphProjectionItem> savedItems = new ArrayList<>();
  private final List<GraphAuditRecord> savedAudits = new ArrayList<>();
  private final List<GraphNode> savedNodes = new ArrayList<>();
  private final List<GraphEdge> savedEdges = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(spaceRepository.existsById("space")).thenReturn(true);
    when(batchRepository.findBySpaceId("space"))
        .thenReturn(List.of(Batch.create("batch", "space", "Graph Batch", SourceKind.folder, "lead", now())));
    when(fileItemRepository.findByBatchIdIn(any())).thenReturn(List.of(file("file-001")));
    when(sourceChunkRepository.findByFileItemIdIn(any()))
        .thenReturn(
            List.of(
                chunk("chunk-approved", ReviewStatus.APPROVED, "Overview"),
                chunk("chunk-review", ReviewStatus.REVIEW_REQUIRED, "Draft appendix"),
                chunkWithoutSourceTrace("chunk-missing-trace")));
    when(wikiPageRepository.findBySpaceIdOrderByTitleAsc("space")).thenReturn(List.of());
    when(graphProjectionRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(graphProjectionItemRepository.save(any(GraphProjectionItem.class)))
        .thenAnswer(invocation -> {
          GraphProjectionItem item = invocation.getArgument(0);
          savedItems.add(item);
          return item;
        });
    when(graphProjectionItemRepository.findByRunIdOrderByIdAsc(any())).thenReturn(savedItems);
    when(graphAuditRecordRepository.save(any(GraphAuditRecord.class)))
        .thenAnswer(invocation -> {
          GraphAuditRecord audit = invocation.getArgument(0);
          savedAudits.add(audit);
          return audit;
        });
    when(graphNodeRepository.save(any(GraphNode.class)))
        .thenAnswer(invocation -> {
          GraphNode node = invocation.getArgument(0);
          savedNodes.add(node);
          return node;
        });
    when(graphNodeRepository.existsById(any()))
        .thenAnswer(
            invocation -> {
              String id = invocation.getArgument(0);
              return savedNodes.stream().anyMatch(node -> node.getId().equals(id));
            });
    when(graphEdgeRepository.save(any(GraphEdge.class)))
        .thenAnswer(invocation -> {
          GraphEdge edge = invocation.getArgument(0);
          savedEdges.add(edge);
          return edge;
        });
    when(graphEdgeRepository.existsById(any()))
        .thenAnswer(
            invocation -> {
              String id = invocation.getArgument(0);
              return savedEdges.stream().anyMatch(edge -> edge.getId().equals(id));
            });
  }

  @Test
  void projectionUsesApprovedEvidenceOnlyAndAuditsSkippedItems() {
    GraphService service = service();

    var response =
        service.createProjectionRun(
            "space", new CreateGraphProjectionRunRequest("APPROVED_ONLY", "deterministic", false));

    assertThat(response.status()).isEqualTo(GraphProjectionStatus.PARTIAL_FAILED);
    assertThat(response.summary().createdCount()).isEqualTo(3);
    assertThat(response.summary().skippedCount()).isEqualTo(2);
    assertThat(response.items())
        .extracting(item -> item.reasonCode())
        .contains("UNAPPROVED_SOURCE", "MISSING_SOURCE_TRACE");
    assertThat(savedItems)
        .filteredOn(item -> item.getStatus() == GraphProjectionItemStatus.CREATED)
        .allSatisfy(item -> assertThat(item.getReasonCode()).isNull());
    assertThat(savedNodes).allSatisfy(node -> assertThat(node.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED));
    assertThat(savedEdges).allSatisfy(edge -> {
      assertThat(edge.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
      assertThat(edge.getEvidenceChunkIds()).contains("chunk-approved");
    });
    assertThat(savedAudits).extracting(GraphAuditRecord::getAction).contains("PROJECTION_RUN");
  }

  @Test
  void projectionUsesEligibleWikiPagesAndIsIdempotent() {
    SourceChunk approved = chunk("chunk-approved", ReviewStatus.APPROVED, "Overview");
    WikiPage published =
        WikiPage.publish(
            "wiki-published",
            "space",
            "Published Modernization Wiki",
            "generated/markdown/published-modernization.md",
            new String[] {"file-001"},
            List.of(approved),
            new BigDecimal("0.930"),
            "sme",
            now());
    WikiPage lowConfidence =
        WikiPage.publish(
            "wiki-low",
            "space",
            "Low Confidence Wiki",
            "generated/markdown/low-confidence.md",
            new String[] {"file-001"},
            List.of(approved),
            new BigDecimal("0.700"),
            "sme",
            now());
    WikiPage reviewRequired =
        WikiPage.generatedCandidate(
            "wiki-review",
            "space",
            "Draft Wiki",
            "draft-wiki",
            "generated/markdown/draft.md",
            new String[] {"file-001"},
            List.of(approved),
            new BigDecimal("0.930"),
            "system",
            now());
    when(wikiPageRepository.findBySpaceIdOrderByTitleAsc("space"))
        .thenReturn(List.of(published, lowConfidence, reviewRequired));

    GraphService service = service();

    var first =
        service.createProjectionRun(
            "space", new CreateGraphProjectionRunRequest("APPROVED_ONLY", "deterministic", false));
    var second =
        service.createProjectionRun(
            "space", new CreateGraphProjectionRunRequest("APPROVED_ONLY", "deterministic", false));

    assertThat(first.summary().createdCount()).isGreaterThan(0);
    assertThat(first.summary().skippedCount()).isEqualTo(2);
    assertThat(second.summary().updatedCount()).isGreaterThan(0);
    assertThat(savedItems)
        .extracting(GraphProjectionItem::getReasonCode)
        .contains("LOW_CONFIDENCE_WIKI_PAGE", "UNAPPROVED_WIKI_PAGE");
    assertThat(savedNodes)
        .anySatisfy(
            node -> {
              assertThat(node.getType().name()).isEqualTo("WIKI_PAGE");
              assertThat(node.getEvidenceWikiPageIds()).contains("wiki-published");
            });
    assertThat(savedEdges)
        .anySatisfy(
            edge -> {
              assertThat(edge.getEvidenceWikiPageIds()).contains("wiki-published");
              assertThat(edge.getEvidenceChunkIds()).contains("chunk-approved");
            });
  }

  private GraphService service() {
    return new GraphService(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        graphNodeRepository,
        graphEdgeRepository,
        graphProjectionRunRepository,
        graphProjectionItemRepository,
        graphAuditRecordRepository,
        wikiPageRepository,
        new DeterministicGraphProjectionAdapter(),
        CLOCK);
  }

  private OffsetDateTime now() {
    return OffsetDateTime.now(CLOCK);
  }

  private FileItem file(String id) {
    return FileItem.create(
        id,
        "batch",
        "Graph/BRD.pdf",
        SourceType.pdf,
        FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.930"),
        ReviewStatus.REVIEW_REQUIRED,
        now());
  }

  private SourceChunk chunk(String id, ReviewStatus reviewStatus, String section) {
    return SourceChunk.create(
        id,
        "file-001",
        "Graph/BRD.pdf",
        1,
        section,
        new BigDecimal("0.930"),
        reviewStatus);
  }

  private SourceChunk chunkWithoutSourceTrace(String id) {
    return SourceChunk.create(id, "file-001", null, 1, "Missing trace", new BigDecimal("0.930"), ReviewStatus.APPROVED);
  }
}

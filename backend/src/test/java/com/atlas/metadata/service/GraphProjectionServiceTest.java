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
    when(graphEdgeRepository.save(any(GraphEdge.class)))
        .thenAnswer(invocation -> {
          GraphEdge edge = invocation.getArgument(0);
          savedEdges.add(edge);
          return edge;
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

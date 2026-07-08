<script setup lang="ts">
import {
  answerReuseHint,
  answerReviewReasonLine,
  formatGraphEvidenceReference,
  formatWikiReference
} from '@/domain/viewModels'
import type {
  ApiAskRun,
  ApiAskSessionDetail,
  ApiAskSessionSummary,
  ApiBatch,
  ApiFileItem,
  ApiGraphEdge,
  ApiGraphEvidenceReference,
  ApiGraphNode,
  ApiGraphNodeType,
  ApiGraphView,
  ApiReviewQueues,
  ApiReviewStatus,
  ApiSourceChunk,
  ApiSpace,
  ApiWikiPage
} from '@/types'

type GraphState = 'loading' | 'ready' | 'empty' | 'unauthorized' | 'error'
type CanvasNode = ApiGraphNode & { x: number; y: number }

defineProps<{
  isLoadingSpaces: boolean
  spacesError: string
  spaces: ApiSpace[]
  selectedSpaceId: string
  selectedSpace: ApiSpace | null
  wikiPages: ApiWikiPage[]
  blockedQueueCount: number
  isLoadingSpace: boolean
  workflowError: string
  workflowMessage: string
  isCreatingBatch: boolean
  canWriteContent: boolean
  batches: ApiBatch[]
  selectedBatchId: string
  files: ApiFileItem[]
  selectedFileId: string
  chunks: ApiSourceChunk[]
  readyQueueCount: number
  reviewQueues: ApiReviewQueues | null
  canApprove: boolean
  isReviewing: boolean
  selectedFile: ApiFileItem | null
  canPublish: boolean
  isPublishing: boolean
  isRefreshingEvidence: boolean
  canOperateKnowledge: boolean
  downstreamReady: boolean
  askRun: ApiAskRun | null
  canAsk: boolean
  isAsking: boolean
  askError: string
  askQualityChips: string[]
  askQualityError: string
  askSessionError: string
  selectedAskSession: ApiAskSessionDetail | null
  askSessions: ApiAskSessionSummary[]
  graph: ApiGraphView | null
  graphState: GraphState
  isLoadingGraph: boolean
  graphError: string
  nodeTypeOptions: ApiGraphNodeType[]
  edgeTypeOptions: string[]
  reviewStatusOptions: ApiReviewStatus[]
  visibleEdges: ApiGraphEdge[]
  canvasNodes: CanvasNode[]
  canvasNodeById: Map<string, CanvasNode>
  selectedEdge: ApiGraphEdge | null
  activeNode: ApiGraphNode | null
  visibleNodes: ApiGraphNode[]
  activeSelectionLabel: string
  evidenceReferences: ApiGraphEvidenceReference[]
  edgeNodeLabel: (nodeId: string) => string
}>()

const askQuestion = defineModel<string>('askQuestion', { required: true })
const searchText = defineModel<string>('searchText', { required: true })
const nodeTypeFilter = defineModel<'ALL' | ApiGraphNodeType>('nodeTypeFilter', { required: true })
const edgeTypeFilter = defineModel<string>('edgeTypeFilter', { required: true })
const reviewStatusFilter = defineModel<'ALL' | ApiReviewStatus>('reviewStatusFilter', {
  required: true
})
const evidenceOnly = defineModel<boolean>('evidenceOnly', { required: true })
const hoveredNodeId = defineModel<string>('hoveredNodeId', { required: true })

const emit = defineEmits<{
  showPrototype: []
  selectSpace: [spaceId: string]
  createBatchFromBrowser: []
  selectBatch: [batchId: string]
  selectFile: [fileId: string]
  approveSelectedFile: []
  publishSelectedFile: []
  refreshDownstreamEvidence: []
  submitAsk: []
  selectNode: [node: ApiGraphNode]
  selectEdge: [edge: ApiGraphEdge]
}>()
</script>

<template>
  <main class="app-shell" aria-label="Atlas Knowledge Hub P0 full-stack workspace">
    <header class="app-header">
      <div>
        <p class="eyebrow">Atlas Knowledge Hub</p>
        <h1>Knowledge Workspace</h1>
        <p>API-driven review, publish, graph, and Ask flow using mock/sample metadata only.</p>
      </div>
      <div class="status-strip" aria-label="P0 status">
        <button class="mode-link" type="button" @click="emit('showPrototype')">Product home</button>
        <span>Mock/sample only</span>
        <span>Adapter-backed</span>
        <span>No production auth</span>
      </div>
    </header>

    <section class="workspace-grid">
      <aside class="space-panel" data-testid="space-list" aria-label="Knowledge Space list">
        <div class="panel-heading">
          <h2>Knowledge Spaces</h2>
          <span v-if="isLoadingSpaces">Loading</span>
        </div>
        <p v-if="spacesError" class="state-message error">{{ spacesError }}</p>
        <p v-if="!isLoadingSpaces && spaces.length === 0 && !spacesError" class="state-message">
          No Knowledge Spaces are available.
        </p>
        <button
          v-for="space in spaces"
          :key="space.id"
          class="space-card"
          data-testid="space-card"
          type="button"
          :data-selected="selectedSpaceId === space.id"
          @click="emit('selectSpace', space.id)"
        >
          <strong>{{ space.name }}</strong>
          <span>{{ space.description }}</span>
          <span>{{ space.owner }} · {{ space.status }}</span>
          <span>
            {{ space.documentCount }} docs · {{ space.wikiPageCount }} wiki ·
            {{ space.reviewCount }} reviews
          </span>
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Production file upload coming soon
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Production auth and member admin coming soon
        </button>
        <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
          Real provider setup coming soon
        </button>
      </aside>

      <section class="flow-panel" data-testid="space-detail" aria-label="Knowledge Space detail">
        <div class="space-hero">
          <div>
            <p class="eyebrow">Selected Space</p>
            <h2>{{ selectedSpace?.name ?? selectedSpaceId }}</h2>
            <p>
              {{ selectedSpace?.description ?? 'Loading live space metadata from the Atlas API.' }}
            </p>
            <p class="space-meta">
              {{ selectedSpace?.owner ?? 'Owner pending' }} ·
              {{ selectedSpace?.status ?? 'Status pending' }}
            </p>
          </div>
          <div class="metric-row">
            <span>Documents {{ selectedSpace?.documentCount ?? 0 }}</span>
            <span>Wiki {{ selectedSpace?.wikiPageCount ?? wikiPages.length }}</span>
            <span>Review {{ selectedSpace?.reviewCount ?? blockedQueueCount }}</span>
          </div>
        </div>
        <p v-if="isLoadingSpace" class="state-message">Loading selected space workflow.</p>
        <p v-if="workflowError" class="state-message error">{{ workflowError }}</p>
        <p v-if="workflowMessage" class="state-message success">{{ workflowMessage }}</p>

        <div class="workflow-grid">
          <section class="panel" data-tab="documents" aria-label="Documents batch workflow">
            <div class="panel-heading">
              <h2>Documents</h2>
              <button
                data-testid="create-sample-batch"
                type="button"
                :disabled="isCreatingBatch || !selectedSpaceId || !canWriteContent"
                @click="emit('createBatchFromBrowser')"
              >
                {{ isCreatingBatch ? 'Creating...' : 'Create sample batch' }}
              </button>
            </div>
            <div class="split-list">
              <div data-testid="batch-list">
                <h3>Batches</h3>
                <button
                  v-for="batch in batches"
                  :key="batch.id"
                  class="list-button"
                  type="button"
                  :data-selected="batch.id === selectedBatchId"
                  @click="emit('selectBatch', batch.id)"
                >
                  <strong>{{ batch.name }}</strong>
                  <span>{{ batch.id }}</span>
                  <span>
                    {{ batch.metrics.totalFiles }} files · {{ batch.metrics.reviewRequired }} review
                  </span>
                </button>
                <p v-if="batches.length === 0" class="state-message">No batches yet.</p>
              </div>
              <div data-testid="file-list">
                <h3>Files</h3>
                <button
                  v-for="file in files"
                  :key="file.id"
                  class="list-button"
                  type="button"
                  :data-selected="file.id === selectedFileId"
                  @click="emit('selectFile', file.id)"
                >
                  <strong>{{ file.sourcePath }}</strong>
                  <span>{{ file.status }} · {{ file.reviewStatus }}</span>
                  <span>confidence {{ file.confidence ?? 'n/a' }}</span>
                </button>
                <p v-if="files.length === 0" class="state-message">No files loaded.</p>
              </div>
            </div>
            <div class="source-trace" data-testid="chunk-list">
              <h3>Source Trace</h3>
              <p v-if="chunks.length === 0" class="state-message">
                Select or create a file with chunks.
              </p>
              <ul>
                <li v-for="chunk in chunks" :key="chunk.id">
                  <strong>{{ chunk.id }}</strong>
                  <span>
                    {{ chunk.sourceFile }} · {{ chunk.section ?? 'section n/a' }} · page
                    {{ chunk.page ?? 'n/a' }}
                  </span>
                  <span>confidence {{ chunk.confidence ?? 'n/a' }} · {{ chunk.reviewStatus }}</span>
                </li>
              </ul>
            </div>
          </section>

          <section class="panel" data-tab="review" aria-label="Review workflow">
            <div class="panel-heading">
              <h2>Review</h2>
              <span>Ready {{ readyQueueCount }} · Blocked {{ blockedQueueCount }}</span>
            </div>
            <div class="queue-grid" data-testid="review-queues">
              <span v-for="queue in reviewQueues?.queues ?? []" :key="queue.type">
                {{ queue.type }} {{ queue.count }}
              </span>
            </div>
            <button
              data-testid="approve-file"
              type="button"
              :disabled="!canApprove || isReviewing"
              @click="emit('approveSelectedFile')"
            >
              {{ isReviewing ? 'Approving...' : 'Approve selected file' }}
            </button>
            <p class="state-message">
              Selected file: {{ selectedFile?.sourcePath ?? 'none' }} ·
              {{ selectedFile?.reviewStatus ?? 'n/a' }}
            </p>
          </section>

          <section class="panel" data-tab="wiki" aria-label="Wiki publish workflow">
            <div class="panel-heading">
              <h2>Wiki</h2>
              <button
                data-testid="publish-file"
                type="button"
                :disabled="!canPublish || isPublishing"
                @click="emit('publishSelectedFile')"
              >
                {{ isPublishing ? 'Publishing...' : 'Publish Wiki' }}
              </button>
            </div>
            <div data-testid="wiki-pages" class="wiki-list">
              <p v-if="wikiPages.length === 0" class="state-message">
                No published Wiki pages yet.
              </p>
              <article v-for="page in wikiPages" :key="page.id">
                <strong>{{ page.title }}</strong>
                <span>{{ page.reviewStatus }} · confidence {{ page.confidence ?? 'n/a' }}</span>
                <span>
                  {{ page.slug ?? page.markdownPath }} · {{ page.pageType ?? 'SOURCE_SUMMARY' }} ·
                  v{{ page.version ?? 1 }}
                </span>
                <span>
                  {{ page.sourceMode ?? 'PUBLISHED_FILE' }} ·
                  {{ page.refreshPolicy ?? 'MANUAL' }}
                </span>
                <span>{{ page.markdownPath }}</span>
                <span>sources {{ page.sourceDocumentIds.join(', ') }}</span>
                <span>
                  refs
                  {{
                    page.sourceRefs?.length > 0
                      ? page.sourceRefs.map(formatWikiReference).join('; ')
                      : 'none'
                  }}
                </span>
                <span>
                  links in {{ page.inLinks?.length ?? 0 }} / out
                  {{ page.outLinks?.length ?? 0 }}
                </span>
              </article>
            </div>
            <button
              data-testid="refresh-graph-evidence"
              type="button"
              :disabled="
                wikiPages.length === 0 ||
                chunks.length === 0 ||
                isRefreshingEvidence ||
                !canOperateKnowledge
              "
              @click="emit('refreshDownstreamEvidence')"
            >
              {{
                isRefreshingEvidence ? 'Refreshing evidence...' : 'Refresh Graph and Ask evidence'
              }}
            </button>
            <span v-if="downstreamReady" class="state-message success">
              Downstream evidence is ready.
            </span>
          </section>

          <section class="panel ask-panel" data-tab="ask" aria-label="Trusted Ask workflow">
            <div class="panel-heading">
              <h2>Ask</h2>
              <span>Answer status {{ askRun?.answerReviewStatus ?? 'pending' }}</span>
            </div>
            <label>
              Question
              <textarea v-model="askQuestion" data-testid="ask-question" rows="3"></textarea>
            </label>
            <button
              data-testid="ask-submit"
              type="button"
              :disabled="!canAsk || isAsking"
              @click="emit('submitAsk')"
            >
              {{ isAsking ? 'Asking...' : 'Ask selected space' }}
            </button>
            <p v-if="askError" class="state-message error">{{ askError }}</p>
            <article v-if="askRun" class="ask-answer" data-testid="ask-answer">
              <strong>
                {{ askRun.status }} · {{ askRun.answerReviewStatus }} ·
                {{ askRun.answerReviewLabel }} ·
                {{ askRun.sessionTitle ?? askRun.sessionId }}
              </strong>
              <p>{{ askRun.answer ?? askRun.safeMessage }}</p>
              <p class="source-line">{{ answerReuseHint(askRun) }}</p>
              <p class="source-line">{{ answerReviewReasonLine(askRun) }}</p>
              <span>confidence {{ askRun.answerConfidence ?? 'n/a' }}</span>
              <div v-if="askQualityChips.length > 0" class="ask-quality-signals">
                <span v-for="chip in askQualityChips" :key="chip">{{ chip }}</span>
              </div>
              <p v-if="askQualityError" class="state-message warning">{{ askQualityError }}</p>
              <ul>
                <li v-for="evidence in askRun.evidence" :key="evidence.evidenceId">
                  {{ evidence.citationId }} · {{ evidence.evidenceLabel }} ·
                  {{ evidence.sourceLocator }} · {{ evidence.citationStatus }} · score
                  {{ evidence.score ?? 'n/a' }}
                  <span v-if="!evidence.reviewEligible"> · {{ evidence.excludedReason }}</span>
                </li>
              </ul>
            </article>
            <p v-if="askSessionError" class="state-message error">{{ askSessionError }}</p>
            <article v-if="selectedAskSession" class="ask-answer" data-testid="ask-session-history">
              <strong>{{ selectedAskSession.title }}</strong>
              <span>{{ askSessions.length }} recent sessions</span>
              <ul>
                <li v-for="run in selectedAskSession.runs" :key="run.runId">
                  {{ run.question }} · {{ run.status }} · {{ run.evidence.length }} citations
                </li>
              </ul>
            </article>
          </section>
        </div>
      </section>
    </section>

    <section
      class="graph-hardening-panel"
      aria-label="Knowledge Space Graph tab"
      data-tab="graph"
      :data-state="graphState"
    >
      <div class="graph-header">
        <p class="graph-kicker">Trusted Graph</p>
        <h1>{{ selectedSpace?.name ?? 'IBM i Modernization' }} Knowledge Graph</h1>
        <p>Evidence-backed nodes and edges preserve source trace, confidence, and review status.</p>
      </div>
      <div class="graph-status" role="status" data-testid="graph-state">
        <span v-if="isLoadingGraph">Loading graph evidence</span>
        <span v-else-if="graphState === 'unauthorized'">
          Unauthorized graph access: {{ graphError }}
        </span>
        <span v-else-if="graphState === 'empty'">
          No approved or published evidence is available for graph projection.
        </span>
        <span v-else-if="graphState === 'error'">{{ graphError }}</span>
        <span v-else>Graph API connected</span>
      </div>

      <div
        v-if="graph && graphState !== 'unauthorized'"
        class="graph-filter-bar"
        aria-label="Graph filters"
      >
        <label>
          Search
          <input
            v-model="searchText"
            data-testid="graph-search"
            type="search"
            placeholder="Find concept, document, or edge"
          />
        </label>
        <label>
          Node
          <select v-model="nodeTypeFilter" data-testid="graph-node-filter">
            <option value="ALL">All nodes</option>
            <option v-for="type in nodeTypeOptions" :key="type" :value="type">{{ type }}</option>
          </select>
        </label>
        <label>
          Edge
          <select v-model="edgeTypeFilter" data-testid="graph-edge-filter">
            <option value="ALL">All edges</option>
            <option v-for="type in edgeTypeOptions" :key="type" :value="type">{{ type }}</option>
          </select>
        </label>
        <label>
          Review
          <select v-model="reviewStatusFilter" data-testid="graph-review-filter">
            <option value="ALL">All review states</option>
            <option v-for="status in reviewStatusOptions" :key="status" :value="status">
              {{ status }}
            </option>
          </select>
        </label>
        <label class="graph-toggle">
          <input v-model="evidenceOnly" data-testid="graph-evidence-toggle" type="checkbox" />
          Evidence only
        </label>
      </div>

      <div v-if="graph && graphState !== 'unauthorized'" class="graph-shell">
        <div class="graph-canvas-wrap" aria-label="Graph canvas">
          <svg
            class="graph-canvas"
            viewBox="0 0 360 300"
            role="img"
            aria-label="API-backed graph canvas"
          >
            <line
              v-for="edge in visibleEdges"
              :key="edge.id"
              :x1="canvasNodeById.get(edge.sourceNodeId)?.x"
              :y1="canvasNodeById.get(edge.sourceNodeId)?.y"
              :x2="canvasNodeById.get(edge.targetNodeId)?.x"
              :y2="canvasNodeById.get(edge.targetNodeId)?.y"
              class="graph-edge-line"
              :class="{ active: selectedEdge?.id === edge.id }"
            />
            <g
              v-for="node in canvasNodes"
              :key="node.id"
              class="graph-svg-node"
              :class="{ active: activeNode?.id === node.id, hovered: hoveredNodeId === node.id }"
              :transform="`translate(${node.x}, ${node.y})`"
              tabindex="0"
              role="button"
              :aria-label="`${node.label} ${node.type} ${node.reviewStatus}`"
              @click="emit('selectNode', node)"
              @keyup.enter="emit('selectNode', node)"
              @focus="hoveredNodeId = node.id"
              @blur="hoveredNodeId = ''"
              @mouseenter="hoveredNodeId = node.id"
              @mouseleave="hoveredNodeId = ''"
            >
              <circle r="24" />
              <text y="4">{{ node.label.slice(0, 2).toUpperCase() }}</text>
            </g>
          </svg>
          <div class="graph-legend" aria-label="Graph legend">
            <span><i class="legend-node"></i>Node</span>
            <span><i class="legend-edge"></i>Evidence edge</span>
            <span><i class="legend-active"></i>Selected</span>
          </div>
        </div>

        <div class="graph-lists">
          <div>
            <h2>Nodes</h2>
            <button
              v-for="node in visibleNodes"
              :key="node.id"
              class="graph-node-button"
              type="button"
              :data-selected="activeNode?.id === node.id"
              @click="emit('selectNode', node)"
            >
              <strong>{{ node.label }}</strong>
              <span>{{ node.type }} · {{ node.reviewStatus }}</span>
              <span>
                confidence {{ node.confidence ?? 'n/a' }} · evidence {{ node.evidenceCount }}
              </span>
            </button>
            <p v-if="visibleNodes.length === 0" class="graph-empty" data-testid="graph-empty">
              No graph nodes match the current filters.
            </p>
          </div>

          <div>
            <h2>Edges</h2>
            <button
              v-for="edge in visibleEdges"
              :key="edge.id"
              class="graph-edge-button"
              type="button"
              :data-selected="selectedEdge?.id === edge.id"
              @click="emit('selectEdge', edge)"
            >
              <strong>{{ edge.type }}</strong>
              <span>
                {{ edgeNodeLabel(edge.sourceNodeId) }} ->
                {{ edgeNodeLabel(edge.targetNodeId) }}
              </span>
              <span>
                confidence {{ edge.confidence ?? 'n/a' }} · evidence {{ edge.evidenceCount }}
              </span>
            </button>
            <p v-if="visibleEdges.length === 0" class="graph-empty" data-testid="graph-no-evidence">
              No evidence-backed relationships match the current filters.
            </p>
          </div>
        </div>
      </div>

      <aside
        v-if="graph && graphState !== 'unauthorized'"
        class="graph-evidence-detail"
        data-testid="graph-evidence-detail"
      >
        <strong>{{ activeSelectionLabel }}</strong>
        <span v-if="selectedEdge">
          {{ selectedEdge.type }} · {{ selectedEdge.reviewStatus }} · confidence
          {{ selectedEdge.confidence ?? 'n/a' }}
        </span>
        <span v-else-if="activeNode">
          {{ activeNode.type }} · {{ activeNode.reviewStatus }} · confidence
          {{ activeNode.confidence ?? 'n/a' }}
        </span>
        <h2>Source Trace</h2>
        <ul v-if="evidenceReferences.length > 0">
          <li
            v-for="evidence in evidenceReferences"
            :key="`${evidence.referenceType ?? 'SOURCE_CHUNK'}-${evidence.sourceChunkId ?? evidence.wikiPageId}`"
          >
            {{ formatGraphEvidenceReference(evidence) }}
          </li>
        </ul>
        <p v-else class="graph-empty">No source-trace evidence is attached to this graph object.</p>
      </aside>

      <div v-if="graph" class="graph-counts" aria-label="Graph counts">
        <span>Nodes {{ graph.counts.nodes ?? graph.nodes.length }}</span>
        <span>Edges {{ graph.counts.edges ?? graph.edges.length }}</span>
        <span>Excluded {{ graph.counts.excluded ?? 0 }}</span>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  approveFile as approveFileApi,
  createAskRun,
  createGraphProjection,
  createSampleBatch,
  createVectorRun,
  getAskRun,
  getGraph,
  getGraphNode,
  getSpace,
  listBatches,
  listChunks,
  listFiles,
  listSpaces,
  listWikiPages,
  publishFile as publishFileApi,
  getReviewQueues
} from '@/api'
import type {
  ApiAskRun,
  ApiBatch,
  ApiFileItem,
  ApiGraphEdge,
  ApiGraphEdgeType,
  ApiGraphNode,
  ApiGraphNodeDetail,
  ApiGraphNodeType,
  ApiGraphView,
  ApiReviewQueues,
  ApiReviewStatus,
  ApiSourceChunk,
  ApiSpace,
  ApiWikiPage
} from '@/types'

const defaultSpaceId = 'ibm-i-modernization'
const prototypeSrc = '/atlas-prototype.html'

const activeExperience = ref<'product' | 'p0'>('product')
const spaces = ref<ApiSpace[]>([])
const selectedSpace = ref<ApiSpace | null>(null)
const selectedSpaceId = ref(defaultSpaceId)
const batches = ref<ApiBatch[]>([])
const selectedBatchId = ref('')
const files = ref<ApiFileItem[]>([])
const selectedFileId = ref('')
const chunks = ref<ApiSourceChunk[]>([])
const reviewQueues = ref<ApiReviewQueues | null>(null)
const wikiPages = ref<ApiWikiPage[]>([])
const askRun = ref<ApiAskRun | null>(null)
const askQuestion = ref('What evidence was published for the P0 browser flow?')
const workflowMessage = ref('')
const downstreamReady = ref(false)

const isLoadingSpaces = ref(true)
const isLoadingSpace = ref(false)
const isCreatingBatch = ref(false)
const isReviewing = ref(false)
const isPublishing = ref(false)
const isRefreshingEvidence = ref(false)
const isAsking = ref(false)
const spacesError = ref('')
const workflowError = ref('')
const askError = ref('')

const graph = ref<ApiGraphView | null>(null)
const selectedDetail = ref<ApiGraphNodeDetail | null>(null)
const selectedEdge = ref<ApiGraphEdge | null>(null)
const isLoadingGraph = ref(true)
const graphError = ref('')
const graphState = ref<'loading' | 'ready' | 'empty' | 'unauthorized' | 'error'>('loading')
const searchText = ref('')
const nodeTypeFilter = ref<'ALL' | ApiGraphNodeType>('ALL')
const edgeTypeFilter = ref<'ALL' | ApiGraphEdgeType>('ALL')
const reviewStatusFilter = ref<'ALL' | ApiReviewStatus>('ALL')
const evidenceOnly = ref(true)
const hoveredNodeId = ref('')

const selectedBatch = computed(
  () => batches.value.find(batch => batch.id === selectedBatchId.value) ?? null
)
const selectedFile = computed(
  () => files.value.find(file => file.id === selectedFileId.value) ?? null
)
const selectedChunkIds = computed(() => chunks.value.map(chunk => chunk.id))
const canApprove = computed(
  () =>
    Boolean(selectedFile.value) &&
    selectedFile.value?.reviewStatus !== 'APPROVED' &&
    chunks.value.length > 0
)
const canPublish = computed(() => selectedFile.value?.reviewStatus === 'APPROVED')
const canAsk = computed(() => Boolean(selectedSpaceId.value && askQuestion.value.trim()))
const readyQueueCount = computed(
  () => reviewQueues.value?.queues.find(queue => queue.type === 'READY_TO_PUBLISH')?.count ?? 0
)
const blockedQueueCount = computed(
  () =>
    reviewQueues.value?.queues
      .filter(queue => queue.publishBlocked)
      .reduce((total, queue) => total + queue.count, 0) ?? 0
)

const activeNode = computed(() => selectedDetail.value?.node ?? graph.value?.nodes[0] ?? null)
const activeSelectionLabel = computed(() => {
  if (!selectedEdge.value) {
    return activeNode.value?.label ?? 'No graph object selected'
  }
  return `${edgeNodeLabel(selectedEdge.value.sourceNodeId)} -> ${edgeNodeLabel(selectedEdge.value.targetNodeId)}`
})
const evidenceReferences = computed(() => selectedDetail.value?.evidenceReferences ?? [])
const graphNodes = computed(() => graph.value?.nodes ?? [])
const graphEdges = computed(() => graph.value?.edges ?? [])
const visibleNodes = computed(() => graphNodes.value.filter(matchesNodeFilters))
const visibleNodeIds = computed(() => new Set(visibleNodes.value.map(node => node.id)))
const visibleEdges = computed(() =>
  graphEdges.value.filter(
    edge =>
      visibleNodeIds.value.has(edge.sourceNodeId) &&
      visibleNodeIds.value.has(edge.targetNodeId) &&
      matchesEdgeFilters(edge)
  )
)
const nodeTypeOptions = computed(() => uniqueValues(graphNodes.value.map(node => node.type)))
const edgeTypeOptions = computed(() => uniqueValues(graphEdges.value.map(edge => edge.type)))
const reviewStatusOptions = computed(() =>
  uniqueValues([
    ...graphNodes.value.map(node => node.reviewStatus),
    ...graphEdges.value.map(edge => edge.reviewStatus)
  ])
)
const canvasNodes = computed(() => {
  const count = Math.max(visibleNodes.value.length, 1)
  const radius = 118
  return visibleNodes.value.map((node, index) => {
    const angle = (Math.PI * 2 * index) / count - Math.PI / 2
    return {
      ...node,
      x: Math.round(180 + Math.cos(angle) * radius),
      y: Math.round(150 + Math.sin(angle) * radius)
    }
  })
})
const canvasNodeById = computed(
  () => new Map(canvasNodes.value.map(node => [node.id, node] as const))
)

onMounted(() => {
  void initialize()
})

async function initialize() {
  await Promise.all([loadSpaces(), loadGraph()])
}

async function loadSpaces() {
  isLoadingSpaces.value = true
  spacesError.value = ''
  try {
    spaces.value = await listSpaces()
    const preferred = spaces.value.find(space => space.id === defaultSpaceId) ?? spaces.value[0]
    if (preferred) {
      await selectSpace(preferred.id)
    }
  } catch (error) {
    spacesError.value = safeError(error, 'Unable to load Knowledge Spaces.')
    await loadSpaceContext(defaultSpaceId)
  } finally {
    isLoadingSpaces.value = false
  }
}

async function selectSpace(spaceId: string) {
  selectedSpaceId.value = spaceId
  await Promise.all([loadSpaceContext(spaceId), loadGraph()])
}

async function loadSpaceContext(spaceId: string) {
  isLoadingSpace.value = true
  workflowError.value = ''
  try {
    const [space, batchList, queues, pages] = await Promise.all([
      getSpace(spaceId),
      listBatches(spaceId),
      getReviewQueues(spaceId),
      listWikiPages(spaceId)
    ])
    selectedSpace.value = space
    batches.value = batchList
    reviewQueues.value = queues
    wikiPages.value = pages
    const firstBatch = batchList[0]
    if (firstBatch) {
      await selectBatch(firstBatch.id)
    } else {
      selectedBatchId.value = ''
      files.value = []
      chunks.value = []
      selectedFileId.value = ''
    }
  } catch (error) {
    workflowError.value = safeError(error, 'Unable to load selected space workflow.')
  } finally {
    isLoadingSpace.value = false
  }
}

async function selectBatch(batchId: string) {
  selectedBatchId.value = batchId
  const fileList = await listFiles(batchId)
  files.value = fileList
  const preferred = fileList.find(file => file.reviewStatus !== 'PUBLISHED') ?? fileList[0]
  if (preferred) {
    await selectFile(preferred.id)
  }
}

async function selectFile(fileId: string) {
  selectedFileId.value = fileId
  chunks.value = await listChunks(fileId)
}

async function createBatchFromBrowser() {
  if (!selectedSpaceId.value) {
    return
  }
  isCreatingBatch.value = true
  workflowError.value = ''
  workflowMessage.value = ''
  const section = `P0 Browser Evidence ${Date.now()}`
  try {
    const batch = await createSampleBatch(selectedSpaceId.value, section)
    workflowMessage.value = `Sample batch created: ${batch.name}`
    await refreshWorkflow(batch.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Sample batch creation failed safely.')
  } finally {
    isCreatingBatch.value = false
  }
}

async function approveSelectedFile() {
  const file = selectedFile.value
  if (!file || chunks.value.length === 0) {
    return
  }
  isReviewing.value = true
  workflowError.value = ''
  try {
    await approveFileApi(file.id, selectedChunkIds.value)
    workflowMessage.value = `Approved ${file.sourcePath}`
    await refreshWorkflow(selectedBatchId.value, file.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Review failed safely.')
  } finally {
    isReviewing.value = false
  }
}

async function publishSelectedFile() {
  const file = selectedFile.value
  if (!file || !canPublish.value) {
    return
  }
  isPublishing.value = true
  workflowError.value = ''
  try {
    const title = `P0 Wiki ${new Date().toISOString().slice(0, 10)}`
    const page = await publishFileApi(file.id, title)
    workflowMessage.value = `Published Wiki page: ${page.title}`
    await refreshWorkflow(selectedBatchId.value, file.id)
  } catch (error) {
    workflowError.value = safeError(error, 'Publish failed safely.')
  } finally {
    isPublishing.value = false
  }
}

async function refreshDownstreamEvidence() {
  const batch = selectedBatch.value
  if (!batch || chunks.value.length === 0) {
    return
  }
  isRefreshingEvidence.value = true
  workflowError.value = ''
  try {
    await createGraphProjection(selectedSpaceId.value)
    await createVectorRun(selectedSpaceId.value, batch.id, selectedChunkIds.value)
    downstreamReady.value = true
    workflowMessage.value = 'Graph and Ask evidence refreshed.'
    await loadGraph(searchText.value)
  } catch (error) {
    workflowError.value = safeError(error, 'Downstream evidence refresh failed safely.')
  } finally {
    isRefreshingEvidence.value = false
  }
}

async function submitAsk() {
  if (!canAsk.value) {
    return
  }
  isAsking.value = true
  askError.value = ''
  askRun.value = null
  try {
    const created = await createAskRun(
      selectedSpaceId.value,
      askQuestion.value.trim(),
      selectedFileId.value
    )
    askRun.value = await getAskRun(created.runId)
  } catch (error) {
    askError.value = safeError(error, 'Trusted Ask failed safely.')
  } finally {
    isAsking.value = false
  }
}

async function refreshWorkflow(batchId = selectedBatchId.value, fileId = selectedFileId.value) {
  const [batchList, queues, pages] = await Promise.all([
    listBatches(selectedSpaceId.value),
    getReviewQueues(selectedSpaceId.value),
    listWikiPages(selectedSpaceId.value)
  ])
  batches.value = batchList
  reviewQueues.value = queues
  wikiPages.value = pages
  const nextBatchId = batchId || batchList[0]?.id || ''
  if (nextBatchId) {
    selectedBatchId.value = nextBatchId
    const fileList = await listFiles(nextBatchId)
    files.value = fileList
    const nextFile = fileList.find(file => file.id === fileId) ?? fileList[0]
    if (nextFile) {
      await selectFile(nextFile.id)
    }
  }
}

async function loadGraph(query = searchText.value) {
  isLoadingGraph.value = true
  graphError.value = ''
  graphState.value = 'loading'
  try {
    const response = await getGraph(selectedSpaceId.value, query)
    graph.value = response
    graphState.value = response.nodes.length === 0 ? 'empty' : 'ready'
    const first = response.nodes[0]
    if (first) {
      await selectNode(first)
    } else {
      selectedDetail.value = null
      selectedEdge.value = null
    }
  } catch (error) {
    graph.value = null
    selectedDetail.value = null
    selectedEdge.value = null
    const message = safeError(error, 'Graph API unavailable.')
    graphError.value = message
    graphState.value = message.toLowerCase().includes('forbidden') ? 'unauthorized' : 'error'
  } finally {
    isLoadingGraph.value = false
  }
}

async function selectNode(node: ApiGraphNode) {
  selectedEdge.value = null
  try {
    selectedDetail.value = await getGraphNode(selectedSpaceId.value, node.id)
  } catch (error) {
    selectedDetail.value = {
      node,
      adjacentNodes: [],
      adjacentEdges: [],
      evidenceReferences: []
    }
    graphError.value = safeError(error, 'Graph detail unavailable.')
  }
}

async function selectEdge(edge: ApiGraphEdge) {
  selectedEdge.value = edge
  const targetNode = graphNodes.value.find(node => node.id === edge.targetNodeId)
  if (targetNode) {
    await selectNode(targetNode)
    selectedEdge.value = edge
  }
}

function matchesNodeFilters(node: ApiGraphNode) {
  const query = searchText.value.trim().toLocaleLowerCase()
  const matchesSearch =
    query.length === 0 ||
    node.label.toLocaleLowerCase().includes(query) ||
    node.type.toLocaleLowerCase().includes(query)
  const matchesType = nodeTypeFilter.value === 'ALL' || node.type === nodeTypeFilter.value
  const matchesReview =
    reviewStatusFilter.value === 'ALL' || node.reviewStatus === reviewStatusFilter.value
  const matchesEvidence =
    !evidenceOnly.value || node.evidenceCount > 0 || node.type === 'KNOWLEDGE_SPACE'
  return matchesSearch && matchesType && matchesReview && matchesEvidence
}

function matchesEdgeFilters(edge: ApiGraphEdge) {
  const query = searchText.value.trim().toLocaleLowerCase()
  const matchesSearch =
    query.length === 0 ||
    edge.type.toLocaleLowerCase().includes(query) ||
    edgeNodeLabel(edge.sourceNodeId).toLocaleLowerCase().includes(query) ||
    edgeNodeLabel(edge.targetNodeId).toLocaleLowerCase().includes(query)
  const matchesType = edgeTypeFilter.value === 'ALL' || edge.type === edgeTypeFilter.value
  const matchesReview =
    reviewStatusFilter.value === 'ALL' || edge.reviewStatus === reviewStatusFilter.value
  const matchesEvidence = !evidenceOnly.value || edge.evidenceCount > 0
  return matchesSearch && matchesType && matchesReview && matchesEvidence
}

function edgeNodeLabel(nodeId: string) {
  return graphNodes.value.find(node => node.id === nodeId)?.label ?? nodeId
}

function uniqueValues<T extends string>(values: T[]) {
  return Array.from(new Set(values)).sort()
}

function safeError(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback
}
</script>

<template>
  <main
    v-if="activeExperience === 'product'"
    class="product-experience"
    aria-label="Atlas logged-in product workspace"
  >
    <iframe
      class="product-frame"
      title="Atlas Knowledge Hub logged-in product experience"
      :src="prototypeSrc"
    ></iframe>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </main>

  <main v-else class="app-shell" aria-label="Atlas Knowledge Hub P0 full-stack workspace">
    <header class="app-header">
      <div>
        <p class="eyebrow">Atlas Knowledge Hub</p>
        <h1>Knowledge Workspace</h1>
        <p>API-driven review, publish, graph, and Ask flow using mock/sample metadata only.</p>
      </div>
      <div class="status-strip" aria-label="P0 status">
        <button class="mode-link" type="button" @click="activeExperience = 'product'">
          Product home
        </button>
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
          @click="selectSpace(space.id)"
        >
          <strong>{{ space.name }}</strong>
          <span>{{ space.description }}</span>
          <span>{{ space.owner }} · {{ space.status }}</span>
          <span
            >{{ space.documentCount }} docs · {{ space.wikiPageCount }} wiki ·
            {{ space.reviewCount }} reviews</span
          >
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
                :disabled="isCreatingBatch || !selectedSpaceId"
                @click="createBatchFromBrowser"
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
                  @click="selectBatch(batch.id)"
                >
                  <strong>{{ batch.name }}</strong>
                  <span>{{ batch.id }}</span>
                  <span
                    >{{ batch.metrics.totalFiles }} files ·
                    {{ batch.metrics.reviewRequired }} review</span
                  >
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
                  @click="selectFile(file.id)"
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
                  <span
                    >{{ chunk.sourceFile }} · {{ chunk.section ?? 'section n/a' }} · page
                    {{ chunk.page ?? 'n/a' }}</span
                  >
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
              @click="approveSelectedFile"
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
                @click="publishSelectedFile"
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
                <span>{{ page.markdownPath }}</span>
                <span>sources {{ page.sourceDocumentIds.join(', ') }}</span>
              </article>
            </div>
            <button
              data-testid="refresh-graph-evidence"
              type="button"
              :disabled="wikiPages.length === 0 || chunks.length === 0 || isRefreshingEvidence"
              @click="refreshDownstreamEvidence"
            >
              {{
                isRefreshingEvidence ? 'Refreshing evidence...' : 'Refresh Graph and Ask evidence'
              }}
            </button>
            <span v-if="downstreamReady" class="state-message success"
              >Downstream evidence is ready.</span
            >
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
              @click="submitAsk"
            >
              {{ isAsking ? 'Asking...' : 'Ask selected space' }}
            </button>
            <p v-if="askError" class="state-message error">{{ askError }}</p>
            <article v-if="askRun" class="ask-answer" data-testid="ask-answer">
              <strong>{{ askRun.status }} · {{ askRun.answerReviewStatus }}</strong>
              <p>{{ askRun.answer ?? askRun.safeMessage }}</p>
              <span>confidence {{ askRun.answerConfidence ?? 'n/a' }}</span>
              <ul>
                <li v-for="evidence in askRun.evidence" :key="evidence.evidenceId">
                  {{ evidence.sourceChunkId }} · {{ evidence.sourceFile }} ·
                  {{ evidence.section ?? 'section n/a' }} · score {{ evidence.score ?? 'n/a' }} ·
                  {{ evidence.reviewStatus }}
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
        <span v-else-if="graphState === 'unauthorized'"
          >Unauthorized graph access: {{ graphError }}</span
        >
        <span v-else-if="graphState === 'empty'"
          >No approved or published evidence is available for graph projection.</span
        >
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
              @click="selectNode(node)"
              @keyup.enter="selectNode(node)"
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
              @click="selectNode(node)"
            >
              <strong>{{ node.label }}</strong>
              <span>{{ node.type }} · {{ node.reviewStatus }}</span>
              <span
                >confidence {{ node.confidence ?? 'n/a' }} · evidence {{ node.evidenceCount }}</span
              >
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
              @click="selectEdge(edge)"
            >
              <strong>{{ edge.type }}</strong>
              <span
                >{{ edgeNodeLabel(edge.sourceNodeId) }} ->
                {{ edgeNodeLabel(edge.targetNodeId) }}</span
              >
              <span
                >confidence {{ edge.confidence ?? 'n/a' }} · evidence {{ edge.evidenceCount }}</span
              >
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
        <span v-if="selectedEdge"
          >{{ selectedEdge.type }} · {{ selectedEdge.reviewStatus }} · confidence
          {{ selectedEdge.confidence ?? 'n/a' }}</span
        >
        <span v-else-if="activeNode"
          >{{ activeNode.type }} · {{ activeNode.reviewStatus }} · confidence
          {{ activeNode.confidence ?? 'n/a' }}</span
        >
        <h2>Source Trace</h2>
        <ul v-if="evidenceReferences.length > 0">
          <li v-for="evidence in evidenceReferences" :key="evidence.sourceChunkId">
            {{ evidence.sourceChunkId }} · {{ evidence.sourceFile }} ·
            {{ evidence.section ?? 'section n/a' }} · confidence
            {{ evidence.confidence ?? 'n/a' }} · {{ evidence.reviewStatus }}
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

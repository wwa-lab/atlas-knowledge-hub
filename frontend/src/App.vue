<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type {
  ApiEnvelope,
  ApiGraphEdge,
  ApiGraphEdgeType,
  ApiGraphNode,
  ApiGraphNodeDetail,
  ApiGraphNodeType,
  ApiGraphView,
  ApiReviewStatus
} from '@/types'

const prototypeSrc = '/atlas-prototype.html'
const spaceId = 'ibm-i-modernization'
const graphEndpoint = `/api/spaces/${spaceId}/graph`
const graphHeaders = {
  'X-Atlas-User': 'frontend-demo',
  'X-Atlas-Role': 'VIEWER'
}

const graph = ref<ApiGraphView | null>(null)
const selectedDetail = ref<ApiGraphNodeDetail | null>(null)
const selectedEdge = ref<ApiGraphEdge | null>(null)
const isLoadingGraph = ref(true)
const graphError = ref('')
const graphState = ref<'loading' | 'ready' | 'fallback' | 'empty' | 'unauthorized' | 'error'>(
  'loading'
)
const searchText = ref('')
const nodeTypeFilter = ref<'ALL' | ApiGraphNodeType>('ALL')
const edgeTypeFilter = ref<'ALL' | ApiGraphEdgeType>('ALL')
const reviewStatusFilter = ref<'ALL' | ApiReviewStatus>('ALL')
const evidenceOnly = ref(true)
const hoveredNodeId = ref('')

const fallbackGraph: ApiGraphView = {
  spaceId,
  nodes: [
    {
      id: 'node-space-ibm-i-modernization',
      label: 'IBM i Modernization',
      type: 'KNOWLEDGE_SPACE',
      reviewStatus: 'APPROVED',
      confidence: null,
      evidenceCount: 0
    },
    {
      id: 'node-document-target-architecture',
      label: 'Target Architecture',
      type: 'DOCUMENT',
      reviewStatus: 'APPROVED',
      confidence: 0.91,
      evidenceCount: 1
    },
    {
      id: 'node-concept-rpgle',
      label: 'RPGLE modernization',
      type: 'CONCEPT',
      reviewStatus: 'APPROVED',
      confidence: 0.93,
      evidenceCount: 1
    }
  ],
  edges: [
    {
      id: 'edge-source-mentions-rpgle',
      sourceNodeId: 'node-document-target-architecture',
      targetNodeId: 'node-concept-rpgle',
      type: 'MENTIONS',
      reviewStatus: 'APPROVED',
      confidence: 0.93,
      evidenceCount: 1
    }
  ],
  counts: { nodes: 3, edges: 1, excluded: 0 }
}

const activeNode = computed(() => selectedDetail.value?.node ?? graph.value?.nodes[0] ?? null)
const activeSelectionLabel = computed(() => {
  if (!selectedEdge.value) {
    return activeNode.value?.label ?? 'No graph object selected'
  }
  return `${edgeNodeLabel(selectedEdge.value.sourceNodeId)} -> ${edgeNodeLabel(
    selectedEdge.value.targetNodeId
  )}`
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
  void loadGraph()
})

async function loadGraph() {
  isLoadingGraph.value = true
  graphError.value = ''
  graphState.value = 'loading'
  try {
    const response = await atlasFetch(
      `${graphEndpoint}?limit=50&evidenceOnly=${evidenceOnly.value}`,
      {
        headers: graphHeaders
      }
    )
    if (response.status === 401 || response.status === 403) {
      graph.value = null
      selectedDetail.value = null
      selectedEdge.value = null
      graphState.value = 'unauthorized'
      graphError.value = 'You do not have permission to inspect this graph.'
      return
    }
    if (!response.ok) {
      throw new Error(`Graph API returned ${response.status}`)
    }
    const envelope = (await response.json()) as ApiEnvelope<ApiGraphView>
    if (!envelope.success || !envelope.data) {
      throw new Error(envelope.error?.message ?? 'Graph API returned an empty response')
    }
    graph.value = envelope.data
    graphState.value = envelope.data.nodes.length === 0 ? 'empty' : 'ready'
    const first = envelope.data.nodes[0]
    if (first) {
      await selectNode(first)
    }
  } catch (error) {
    graph.value = fallbackGraph
    selectedDetail.value = null
    selectedEdge.value = null
    graphState.value = 'fallback'
    graphError.value = error instanceof Error ? error.message : 'Graph API unavailable'
    const first = fallbackGraph.nodes[0]
    if (first) {
      await selectNode(first)
    }
  } finally {
    isLoadingGraph.value = false
  }
}

async function selectNode(node: ApiGraphNode) {
  selectedEdge.value = null
  try {
    const response = await atlasFetch(`${graphEndpoint}/nodes/${node.id}`, {
      headers: graphHeaders
    })
    if (!response.ok) {
      throw new Error(`Graph detail returned ${response.status}`)
    }
    const envelope = (await response.json()) as ApiEnvelope<ApiGraphNodeDetail>
    selectedDetail.value = envelope.data
  } catch {
    selectedDetail.value = {
      node,
      adjacentNodes: [],
      adjacentEdges: [],
      evidenceReferences: fallbackEvidenceFor(node)
    }
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

function atlasFetch(
  input: string,
  init?: { headers: Record<string, string> }
): Promise<{ ok: boolean; status: number; json: () => Promise<unknown> }> {
  return globalThis.fetch(input, init)
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

function fallbackEvidenceFor(node: ApiGraphNode) {
  if (node.evidenceCount === 0) {
    return []
  }
  return [
    {
      sourceChunkId: 'chunk-file-001-p12-b02',
      sourceFile: 'Graph/Modernization.md',
      page: 1,
      section: node.label,
      confidence: node.confidence,
      reviewStatus: node.reviewStatus
    }
  ]
}
</script>

<template>
  <main class="prototype-host" aria-label="Atlas Phase 1 prototype fidelity host">
    <section
      class="graph-hardening-panel"
      aria-label="Knowledge Space Graph tab"
      data-tab="graph"
      :data-state="graphState"
    >
      <div class="graph-header">
        <p class="graph-kicker">Trusted Graph</p>
        <h1>IBM i Modernization Knowledge Graph</h1>
        <p>Evidence-backed nodes and edges preserve source trace, confidence, and review status.</p>
      </div>
      <div class="graph-status" role="status" data-testid="graph-state">
        <span v-if="isLoadingGraph">Loading graph evidence</span>
        <span v-else-if="graphState === 'unauthorized'"
          >Unauthorized graph access: {{ graphError }}</span
        >
        <span v-else-if="graphState === 'fallback'">Using safe mock graph: {{ graphError }}</span>
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
          <li v-for="evidence in evidenceReferences" :key="evidence.sourceChunkId">
            {{ evidence.sourceChunkId }} · {{ evidence.sourceFile }} ·
            {{ evidence.section ?? 'section n/a' }} · confidence
            {{ evidence.confidence ?? 'n/a' }} ·
            {{ evidence.reviewStatus }}
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
    <iframe
      class="prototype-frame"
      title="Atlas Knowledge Hub Phase 1 Prototype"
      :src="prototypeSrc"
    ></iframe>
  </main>
</template>

import { computed, ref } from 'vue'
import { getGraph, getGraphNode } from '@/api'
import { isPermissionDeniedError, safeError } from '@/composables/apiErrors'
import {
  formatGraphEvidenceReference,
  productGraphNodeType,
  productReviewStatus,
  uniqueValues
} from '@/domain/viewModels'
import type { ProductGraphEdge, ProductGraphNode } from '@/domain/viewModels'
import type {
  ApiGraphEdge,
  ApiGraphEdgeType,
  ApiGraphNode,
  ApiGraphNodeDetail,
  ApiGraphNodeType,
  ApiGraphView,
  ApiReviewStatus
} from '@/types'

export interface UseGraphOptions {
  selectedSpaceId: () => string
}

const productGraphNodes: ProductGraphNode[] = [
  {
    id: 'node-wiki',
    label: 'Modernization Index',
    type: 'Wiki Page',
    reviewStatus: 'PUBLISHED',
    confidence: 0.96,
    sourceTrace: 'modernization-index.md / section index / chunk wiki-001',
    detail: 'Published Wiki page created from approved discovery and batch workflow metadata.',
    x: 52,
    y: 30
  },
  {
    id: 'node-ibmi',
    label: 'IBM i',
    type: 'Entity',
    reviewStatus: 'APPROVED',
    confidence: 0.94,
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012',
    detail: 'Core platform entity extracted from approved BRD evidence.',
    x: 26,
    y: 58
  },
  {
    id: 'node-rpg',
    label: 'RPG Program',
    type: 'Concept',
    reviewStatus: 'REVIEW_REQUIRED',
    confidence: 0.67,
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
    detail: 'Low-confidence concept kept visible as review-required and excluded from trusted Ask.',
    x: 72,
    y: 62
  },
  {
    id: 'node-doc',
    label: 'BRD Methodology',
    type: 'Document',
    reviewStatus: 'APPROVED',
    confidence: 0.92,
    sourceTrace: 'BRD_Methodology.pdf / page 12',
    detail: 'Approved source document that provides evidence for the Wiki page and entity nodes.',
    x: 50,
    y: 84
  },
  {
    id: 'node-review',
    label: 'Review Hold',
    type: 'Review Required',
    reviewStatus: 'REVIEW_REQUIRED',
    confidence: 0.61,
    sourceTrace: 'green-screen-flow.png / image region 2',
    detail: 'OCR-required screenshot evidence is intentionally gated before graph trust expansion.',
    x: 84,
    y: 34
  }
]

const productGraphEdges: ProductGraphEdge[] = [
  {
    id: 'edge-wiki-ibmi',
    source: 'node-wiki',
    target: 'node-ibmi',
    label: 'defines',
    confidence: 0.94,
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012',
    reviewStatus: 'APPROVED'
  },
  {
    id: 'edge-wiki-doc',
    source: 'node-wiki',
    target: 'node-doc',
    label: 'derived from',
    confidence: 0.92,
    sourceTrace: 'BRD_Methodology.pdf / page 12',
    reviewStatus: 'APPROVED'
  },
  {
    id: 'edge-rpg-review',
    source: 'node-rpg',
    target: 'node-review',
    label: 'blocked by',
    confidence: 0.61,
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42',
    reviewStatus: 'REVIEW_REQUIRED'
  }
]

export function useGraph(options: UseGraphOptions) {
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
  const selectedProductGraphNodeId = ref('node-wiki')
  const productGraphSearch = ref('')

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

  const apiProductGraphNodes = computed<ProductGraphNode[]>(() =>
    graphNodes.value.map((node, index) => ({
      id: node.id,
      label: node.label,
      type: productGraphNodeType(node.type),
      reviewStatus: productReviewStatus(node.reviewStatus),
      confidence: node.confidence ?? 0,
      sourceTrace: `API graph evidence ${node.evidenceCount}`,
      detail: `${node.type} loaded from Atlas graph API with ${node.evidenceCount} evidence reference(s).`,
      x: 18 + (index % 3) * 32,
      y: 20 + Math.floor(index / 3) * 28
    }))
  )
  const apiProductGraphEdges = computed<ProductGraphEdge[]>(() =>
    graphEdges.value.map(edge => ({
      id: edge.id,
      source: edge.sourceNodeId,
      target: edge.targetNodeId,
      label: edge.type,
      confidence: edge.confidence ?? 0,
      sourceTrace: `API edge evidence ${edge.evidenceCount}`,
      reviewStatus: productReviewStatus(edge.reviewStatus)
    }))
  )
  const productGraphNodeList = computed(() =>
    apiProductGraphNodes.value.length > 0 ? apiProductGraphNodes.value : productGraphNodes
  )
  const productGraphEdgeList = computed(() =>
    apiProductGraphNodes.value.length > 0 ? apiProductGraphEdges.value : productGraphEdges
  )
  const selectedProductGraphNode = computed(
    () =>
      productGraphNodeList.value.find(node => node.id === selectedProductGraphNodeId.value) ??
      productGraphNodeList.value[0]
  )
  const visibleProductGraphNodes = computed(() => {
    const query = productGraphSearch.value.trim().toLocaleLowerCase()
    if (!query) {
      return productGraphNodeList.value
    }
    return productGraphNodeList.value.filter(
      node =>
        node.label.toLocaleLowerCase().includes(query) ||
        node.type.toLocaleLowerCase().includes(query) ||
        node.reviewStatus.toLocaleLowerCase().includes(query)
    )
  })
  const visibleProductGraphNodeIds = computed(
    () => new Set(visibleProductGraphNodes.value.map(node => node.id))
  )
  const visibleProductGraphEdges = computed(() =>
    productGraphEdgeList.value.filter(
      edge =>
        visibleProductGraphNodeIds.value.has(edge.source) &&
        visibleProductGraphNodeIds.value.has(edge.target)
    )
  )
  const selectedProductGraphEvidence = computed(() => {
    const node = selectedProductGraphNode.value
    const apiEvidence = selectedDetail.value?.evidenceReferences ?? []
    if (apiEvidence.length > 0 && apiProductGraphNodes.value.some(item => item.id === node.id)) {
      return apiEvidence.map(formatGraphEvidenceReference)
    }
    return [
      node.sourceTrace,
      ...productGraphEdgeList.value
        .filter(edge => edge.source === node.id || edge.target === node.id)
        .map(edge => edge.sourceTrace)
    ]
  })

  async function loadGraph(query = searchText.value) {
    isLoadingGraph.value = true
    graphError.value = ''
    graphState.value = 'loading'
    try {
      const response = await getGraph(options.selectedSpaceId(), query)
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
      graphState.value = isPermissionDeniedError(error, message) ? 'unauthorized' : 'error'
    } finally {
      isLoadingGraph.value = false
    }
  }

  async function selectNode(node: ApiGraphNode) {
    selectedEdge.value = null
    try {
      selectedDetail.value = await getGraphNode(options.selectedSpaceId(), node.id)
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

  function selectProductGraphNode(nodeId: string) {
    selectedProductGraphNodeId.value = nodeId
    const apiNode = graphNodes.value.find(node => node.id === nodeId)
    if (apiNode) {
      void selectNode(apiNode)
    }
  }

  return {
    graph,
    selectedDetail,
    selectedEdge,
    isLoadingGraph,
    graphError,
    graphState,
    searchText,
    nodeTypeFilter,
    edgeTypeFilter,
    reviewStatusFilter,
    evidenceOnly,
    hoveredNodeId,
    selectedProductGraphNodeId,
    productGraphSearch,
    activeNode,
    activeSelectionLabel,
    evidenceReferences,
    graphNodes,
    graphEdges,
    visibleNodes,
    visibleEdges,
    nodeTypeOptions,
    edgeTypeOptions,
    reviewStatusOptions,
    canvasNodes,
    canvasNodeById,
    apiProductGraphNodes,
    apiProductGraphEdges,
    productGraphNodeList,
    productGraphEdgeList,
    selectedProductGraphNode,
    visibleProductGraphNodes,
    visibleProductGraphEdges,
    selectedProductGraphEvidence,
    loadGraph,
    selectNode,
    selectEdge,
    matchesNodeFilters,
    matchesEdgeFilters,
    edgeNodeLabel,
    selectProductGraphNode
  }
}

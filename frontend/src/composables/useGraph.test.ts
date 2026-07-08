import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getGraph, getGraphNode } from '@/api'
import { useGraph } from '@/composables/useGraph'
import type { ApiGraphNode, ApiGraphNodeDetail, ApiGraphView } from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    getGraph: vi.fn(),
    getGraphNode: vi.fn()
  }
})

const mockGetGraph = vi.mocked(getGraph)
const mockGetGraphNode = vi.mocked(getGraphNode)

describe('useGraph', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads graph data, selects first node detail, and maps API nodes to product graph nodes', async () => {
    mockGetGraph.mockResolvedValue(graphView())
    mockGetGraphNode.mockResolvedValue(nodeDetail(node()))
    const state = useGraph({ selectedSpaceId: () => 'space-1' })

    await state.loadGraph('Evidence')

    expect(mockGetGraph).toHaveBeenCalledWith('space-1', 'Evidence')
    expect(state.graphState.value).toBe('ready')
    expect(state.activeNode.value?.id).toBe('node-1')
    expect(state.selectedProductGraphNode.value).toMatchObject({
      id: 'node-1',
      type: 'Concept',
      sourceTrace: 'API graph evidence 1'
    })
    expect(state.selectedProductGraphEvidence.value[0]).toContain('Source chunk chunk-1')
  })

  it('filters graph canvas nodes and edges by search, type, review, and evidence gates', () => {
    const state = useGraph({ selectedSpaceId: () => 'space-1' })
    state.graph.value = graphView()

    state.searchText.value = 'evidence'
    state.nodeTypeFilter.value = 'CONCEPT'
    state.reviewStatusFilter.value = 'APPROVED'

    expect(state.visibleNodes.value.map(item => item.id)).toEqual(['node-1'])
    expect(state.visibleEdges.value).toHaveLength(0)
  })

  it('falls back to local detail safely when graph node detail is unavailable', async () => {
    mockGetGraphNode.mockRejectedValue(new Error('detail unavailable'))
    const state = useGraph({ selectedSpaceId: () => 'space-1' })

    await state.selectNode(node())

    expect(state.selectedDetail.value?.node.id).toBe('node-1')
    expect(state.graphError.value).toContain('detail unavailable')
  })
})

function node(overrides: Partial<ApiGraphNode> = {}): ApiGraphNode {
  return {
    id: 'node-1',
    label: 'Evidence Concept',
    type: 'CONCEPT',
    reviewStatus: 'APPROVED',
    confidence: 0.93,
    evidenceCount: 1,
    ...overrides
  }
}

function graphView(): ApiGraphView {
  return {
    spaceId: 'space-1',
    nodes: [
      node(),
      node({ id: 'node-2', label: 'Unreviewed', reviewStatus: 'REVIEW_REQUIRED', evidenceCount: 0 })
    ],
    edges: [
      {
        id: 'edge-1',
        sourceNodeId: 'node-1',
        targetNodeId: 'node-2',
        type: 'RELATED_TO',
        reviewStatus: 'APPROVED',
        confidence: 0.9,
        evidenceCount: 1
      }
    ],
    counts: { nodes: 2, edges: 1 }
  }
}

function nodeDetail(detailNode: ApiGraphNode): ApiGraphNodeDetail {
  return {
    node: detailNode,
    adjacentNodes: [],
    adjacentEdges: [],
    evidenceReferences: [
      {
        referenceType: 'SOURCE_CHUNK',
        sourceChunkId: 'chunk-1',
        sourceFile: 'mock.md',
        section: 'Evidence',
        confidence: 0.93,
        reviewStatus: 'APPROVED'
      }
    ]
  }
}

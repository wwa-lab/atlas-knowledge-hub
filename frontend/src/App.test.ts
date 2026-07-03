import { mount } from '@vue/test-utils'
import App from './App.vue'
import { trustedAskRun } from './data/atlasMock'

describe('Atlas Phase 1 prototype fidelity host', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('embeds the accepted prototype fixture', () => {
    mockGraphApi()
    const wrapper = mount(App)
    const frame = wrapper.get('iframe')

    expect(frame.attributes('src')).toBe('/atlas-prototype.html')
    expect(frame.attributes('title')).toBe('Atlas Knowledge Hub Phase 1 Prototype')
  })

  it('renders an API-backed Graph tab with filters, canvas, and evidence metadata', async () => {
    mockGraphApi()
    const wrapper = mount(App)
    await flushAsync()

    const graphTab = wrapper.get('[data-tab="graph"]')
    expect(graphTab.attributes('data-state')).toBe('ready')
    expect(graphTab.text()).toContain('Graph API connected')
    expect(graphTab.text()).toContain('RPGLE modernization')
    expect(graphTab.text()).toContain('MENTIONS')
    expect(graphTab.get('[data-testid="graph-evidence-detail"]').text()).toContain('Source Trace')
    expect(graphTab.text()).toContain('chunk-file-001-p12-b02')

    await graphTab.get('[data-testid="graph-search"]').setValue('No matching graph object')
    expect(graphTab.get('[data-testid="graph-empty"]').text()).toContain('No graph nodes match')
    expect(graphTab.get('[data-testid="graph-no-evidence"]').text()).toContain(
      'No evidence-backed relationships'
    )
  })

  it('shows unauthorized graph state without falling back to mock data', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 403,
      json: async () => ({
        success: false,
        data: null,
        error: { code: 'FORBIDDEN', message: 'Forbidden' },
        meta: null
      })
    } as Response)

    const wrapper = mount(App)
    await flushAsync()

    const graphTab = wrapper.get('[data-tab="graph"]')
    expect(graphTab.attributes('data-state')).toBe('unauthorized')
    expect(graphTab.text()).toContain('Unauthorized graph access')
    expect(graphTab.text()).not.toContain('Using safe mock graph')
  })

  it('shows an empty graph state for spaces without approved evidence', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        success: true,
        data: {
          spaceId: 'ibm-i-modernization',
          nodes: [],
          edges: [],
          counts: { nodes: 0, edges: 0, excluded: 3 }
        },
        error: null,
        meta: null
      })
    } as Response)

    const wrapper = mount(App)
    await flushAsync()

    const graphTab = wrapper.get('[data-tab="graph"]')
    expect(graphTab.attributes('data-state')).toBe('empty')
    expect(graphTab.text()).toContain('No approved or published evidence')
    expect(graphTab.text()).toContain('Excluded 3')
  })

  it('maps trusted Ask mock state to review-required answer evidence', () => {
    expect(trustedAskRun.status).toBe('SUCCEEDED')
    expect(trustedAskRun.answerReviewStatus).toBe('REVIEW_REQUIRED')
    expect(trustedAskRun.reviewPolicy).toBe('INCLUDE_REVIEW_REQUIRED')
    expect(trustedAskRun.evidence[0].reviewStatus).toBe('APPROVED')
    expect(trustedAskRun.safeMessage).not.toContain('https://')
  })
})

function mockGraphApi() {
  vi.spyOn(globalThis, 'fetch').mockImplementation(async input => {
    const url = String(input)
    if (url.includes('/nodes/node-concept-rpgle')) {
      return {
        ok: true,
        status: 200,
        json: async () => ({
          success: true,
          data: {
            node: {
              id: 'node-concept-rpgle',
              label: 'RPGLE modernization',
              type: 'CONCEPT',
              reviewStatus: 'APPROVED',
              confidence: 0.93,
              evidenceCount: 1
            },
            adjacentNodes: [],
            adjacentEdges: [],
            evidenceReferences: [
              {
                sourceChunkId: 'chunk-file-001-p12-b02',
                sourceFile: 'Graph/Modernization.md',
                page: 1,
                section: 'RPGLE modernization',
                confidence: 0.93,
                reviewStatus: 'APPROVED'
              }
            ]
          },
          error: null,
          meta: null
        })
      } as Response
    }
    return {
      ok: true,
      status: 200,
      json: async () => ({
        success: true,
        data: {
          spaceId: 'ibm-i-modernization',
          nodes: [
            {
              id: 'node-concept-rpgle',
              label: 'RPGLE modernization',
              type: 'CONCEPT',
              reviewStatus: 'APPROVED',
              confidence: 0.93,
              evidenceCount: 1
            },
            {
              id: 'node-document-target-architecture',
              label: 'Target Architecture',
              type: 'DOCUMENT',
              reviewStatus: 'APPROVED',
              confidence: 0.91,
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
          counts: { nodes: 2, edges: 1, excluded: 0 }
        },
        error: null,
        meta: null
      })
    } as Response
  })
}

async function flushAsync() {
  await new Promise(resolve => setTimeout(resolve, 0))
  await new Promise(resolve => setTimeout(resolve, 0))
}

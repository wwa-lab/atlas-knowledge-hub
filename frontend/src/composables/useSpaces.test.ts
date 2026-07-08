import { describe, expect, it, vi, beforeEach } from 'vitest'
import { createSpace, getCurrentUser, getSpace, listSpaces } from '@/api'
import { defaultMockMe, defaultSpaceId, useSpaces } from '@/composables/useSpaces'
import type { ApiSpace } from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    createSpace: vi.fn(),
    getCurrentUser: vi.fn(),
    getSpace: vi.fn(),
    listSpaces: vi.fn()
  }
})

const mockCreateSpace = vi.mocked(createSpace)
const mockGetCurrentUser = vi.mocked(getCurrentUser)
const mockGetSpace = vi.mocked(getSpace)
const mockListSpaces = vi.mocked(listSpaces)

describe('useSpaces', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads spaces, selects the preferred space, and merges API cards with sample fallbacks', async () => {
    const apiSpace = space({ id: defaultSpaceId, name: 'IBM i Modernization API' })
    mockListSpaces.mockResolvedValue([apiSpace])
    mockGetCurrentUser.mockResolvedValue(defaultMockMe)

    const state = useSpaces()
    await state.loadCurrentUser()
    const selectedId = await state.loadSpaces()

    expect(selectedId).toBe(defaultSpaceId)
    expect(state.selectedSpaceId.value).toBe(defaultSpaceId)
    expect(state.productSpaceCards.value[0]).toMatchObject({
      id: defaultSpaceId,
      name: 'IBM i Modernization API',
      source: 'api'
    })
    expect(state.productSpaceCards.value.some(card => card.source === 'sample')).toBe(true)
    expect(state.canManageSpaces.value).toBe(true)
  })

  it('creates a trimmed Knowledge Space and updates local selection immutably', async () => {
    const created = space({ id: 'claims-ops', name: 'Claims Ops Hub' })
    mockCreateSpace.mockResolvedValue(created)

    const state = useSpaces()
    state.createSpaceDraft.value = {
      name: ' Claims Ops Hub ',
      description: ' Claims operations ',
      type: 'document',
      indexStrategy: 'rag',
      owner: ''
    }

    const result = await state.createProductSpace()

    expect(result).toEqual(created)
    expect(mockCreateSpace).toHaveBeenCalledWith({
      name: 'Claims Ops Hub',
      description: 'Claims operations',
      type: 'document',
      indexStrategy: 'rag',
      owner: '我创建'
    })
    expect(state.spaces.value[0]).toEqual(created)
    expect(state.selectedSpaceId.value).toBe('claims-ops')
    expect(state.createSpaceStatus.value).toBe('知识库已创建')
  })

  it('loads selected space metadata and applies space info overrides without mutating originals', async () => {
    const original = space({ id: 'space-1', name: 'Original' })
    mockGetSpace.mockResolvedValue(original)

    const state = useSpaces()
    await state.loadSelectedSpace('space-1')
    state.spaces.value = [original]
    state.selectedProductSpaceId.value = 'space-1'

    state.updateSelectedSpaceInfo('space-1', {
      name: 'Renamed',
      description: 'Updated description'
    })

    expect(original.name).toBe('Original')
    expect(state.spaces.value[0]).toMatchObject({ name: 'Renamed' })
    expect(state.selectedSpace.value).toMatchObject({ description: 'Updated description' })
    expect(state.selectedProductSpace.value.name).toBe('Renamed')
  })
})

function space(overrides: Partial<ApiSpace> = {}): ApiSpace {
  return {
    id: 'space-1',
    name: 'Space One',
    description: 'A sample space',
    type: 'document',
    indexStrategy: 'rag',
    owner: 'owner',
    status: 'REVIEW_REQUIRED',
    documentCount: 3,
    wikiPageCount: 2,
    reviewCount: 1,
    createdAt: '2026-07-08T00:00:00Z',
    updatedAt: '2026-07-08T00:00:00Z',
    ...overrides
  }
}

import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getDeepSeekConfiguration,
  listAuditEvents,
  listModelAdapters,
  saveDeepSeekConfiguration
} from '@/api'
import { useSettings } from '@/composables/useSettings'
import type { ProductSpaceCard } from '@/domain/viewModels'
import type { ApiAuditEvent, ApiModelCapability, ApiModelConfiguration, ApiSpace } from '@/types'

vi.mock('@/api', async importActual => {
  const actual = await importActual<typeof import('@/api')>()
  return {
    ...actual,
    clearDeepSeekConfiguration: vi.fn(),
    getDeepSeekConfiguration: vi.fn(),
    listAuditEvents: vi.fn(),
    listModelAdapters: vi.fn(),
    saveDeepSeekConfiguration: vi.fn()
  }
})

const mockGetDeepSeekConfiguration = vi.mocked(getDeepSeekConfiguration)
const mockListAuditEvents = vi.mocked(listAuditEvents)
const mockListModelAdapters = vi.mocked(listModelAdapters)
const mockSaveDeepSeekConfiguration = vi.mocked(saveDeepSeekConfiguration)

describe('useSettings', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('loads model capabilities and saves DeepSeek runtime configuration without exposing raw keys', async () => {
    mockListModelAdapters.mockResolvedValue([modelCapability()])
    mockGetDeepSeekConfiguration.mockResolvedValue(modelConfiguration())
    mockSaveDeepSeekConfiguration.mockResolvedValue(modelConfiguration())
    const state = useSettings(testOptions())

    await state.loadModelCapabilities()
    state.openModelEditor(state.models.value[0])
    state.modelDraft.value = {
      ...state.modelDraft.value!,
      name: 'deepseek-reasoner',
      baseUrl: 'https://api.deepseek.com/v1/'
    }
    await state.saveModelEditor()

    expect(mockSaveDeepSeekConfiguration).toHaveBeenCalledWith({
      endpoint: 'https://api.deepseek.com/v1/',
      modelName: 'deepseek-reasoner'
    })
    expect(state.modelSaveStatus.value).toBe('配置已更新')
    expect(state.models.value[0].baseUrl).toBe('')
    expect(JSON.stringify(mockSaveDeepSeekConfiguration.mock.calls)).not.toContain('raw-secret')
  })

  it('keeps settings navigation explicit and blocks audit panel without governance read', () => {
    const state = useSettings(testOptions({ canReadGovernance: () => false }))

    state.openSettings('audit')

    expect(state.settingsOpen.value).toBe(true)
    expect(state.settingsPanel.value).toBe('general')
  })

  it('updates editable space info through the supplied space callback', () => {
    const updateSelectedSpaceInfo = vi.fn()
    const state = useSettings(testOptions({ updateSelectedSpaceInfo }))

    state.beginSpaceInfoEdit('name')
    state.spaceInfoDraft.value = { name: ' Renamed Space ', description: ' Updated ' }
    state.saveSpaceInfoEdit()

    expect(updateSelectedSpaceInfo).toHaveBeenCalledWith('space-1', {
      name: 'Renamed Space',
      description: 'Updated'
    })
    expect(state.spaceInfoStatus.value).toBe('空间信息已更新')
  })

  it('loads audit events only when governance read is available', async () => {
    mockListAuditEvents.mockResolvedValue([auditEvent()])
    const state = useSettings(testOptions())

    await state.loadAuditEvents()

    expect(mockListAuditEvents).toHaveBeenCalledWith('space-1')
    expect(state.auditSummary.value).toMatchObject({ total: 1, security: 1, denied: 1 })
  })
})

function testOptions(overrides: Partial<Parameters<typeof useSettings>[0]> = {}) {
  return {
    selectedProductSpace: () => productSpace(),
    selectedProductApiSpace: () => apiSpace(),
    selectedSpaceId: () => 'space-1',
    canReadGovernance: () => true,
    updateSelectedSpaceInfo: vi.fn(),
    ...overrides
  }
}

function productSpace(): ProductSpaceCard {
  return {
    id: 'space-1',
    name: 'IBM i Modernization',
    description: 'Sample product space',
    documents: 1,
    reviews: 1,
    owner: 'owner',
    status: 'REVIEW_REQUIRED',
    wikiPages: 1,
    source: 'api'
  }
}

function apiSpace(): ApiSpace {
  return {
    id: 'space-1',
    name: 'IBM i Modernization',
    description: 'Sample product space',
    type: 'document',
    indexStrategy: 'rag',
    owner: 'owner',
    status: 'REVIEW_REQUIRED',
    documentCount: 1,
    wikiPageCount: 1,
    reviewCount: 1,
    createdAt: '2026-07-08T00:00:00Z',
    updatedAt: '2026-07-08T00:00:00Z'
  }
}

function credentialStatus(status: ApiModelConfiguration['credentialStatus'] = 'CONFIGURED') {
  return {
    reference: {
      provider: 'deepseek',
      scope: 'model',
      key: 'credential',
      displayName: 'DeepSeek credential'
    },
    status,
    source: 'runtime',
    maskedLabel: 'runtime-secret',
    replaceable: true,
    removable: true
  }
}

function modelCapability(): ApiModelCapability {
  return {
    adapterKey: 'deepseek',
    modelKey: 'deepseek-chat',
    displayName: 'DeepSeek Chat',
    providerFamily: 'DeepSeek',
    modelType: 'CHAT',
    supportedOperations: ['CHAT'],
    defaultModel: true,
    status: 'AVAILABLE',
    contextLimit: 64000,
    secretStatuses: [credentialStatus()],
    maskedConfigSummary: {}
  }
}

function modelConfiguration(): ApiModelConfiguration {
  return {
    adapterKey: 'deepseek',
    provider: 'deepseek',
    modelKey: 'deepseek-chat',
    credentialStatus: 'CONFIGURED',
    endpointStatus: 'CONFIGURED',
    mode: 'runtime',
    secretStatuses: [credentialStatus()],
    maskedConfigSummary: {}
  }
}

function auditEvent(): ApiAuditEvent {
  return {
    id: 'audit-1',
    createdAt: '2026-07-08T00:00:00Z',
    actorUserId: 'tester',
    actorDisplay: 'tester',
    category: 'AUTH',
    action: 'GOVERNANCE_READ',
    result: 'DENIED',
    severity: 'SECURITY',
    spaceId: 'space-1',
    targetType: 'SPACE',
    targetId: 'space-1',
    requestId: 'request-1',
    safeSummary: 'Denied safely.',
    metadata: {}
  }
}

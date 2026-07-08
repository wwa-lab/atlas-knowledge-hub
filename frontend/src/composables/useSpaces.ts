import { computed, ref } from 'vue'
import { createSpace as createSpaceApi, getCurrentUser, getSpace, listSpaces } from '@/api'
import { safeError } from '@/composables/apiErrors'
import type { ApiMe, ApiSpace } from '@/types'
import type { CreateSpaceDraft, ProductSpaceCard, SpaceInfoDraft } from '@/domain/viewModels'

export const defaultSpaceId = 'ibm-i-modernization'

export const defaultMockMe: ApiMe = {
  user: {
    id: 'frontend-demo',
    email: 'frontend-demo@example.test',
    displayName: 'Frontend Demo',
    status: 'ACTIVE',
    globalRoles: []
  },
  activeSpaceId: defaultSpaceId,
  memberships: [
    {
      id: 'membership-frontend-ibmi',
      spaceId: defaultSpaceId,
      spaceName: 'IBM i Modernization',
      role: 'SPACE_OWNER',
      status: 'ACTIVE'
    }
  ],
  capabilities: [
    'CONTENT_READ',
    'CONTENT_WRITE',
    'GOVERNANCE_READ',
    'KNOWLEDGE_OPERATE',
    'MEMBER_MANAGE',
    'SETTINGS_MANAGE',
    'SPACE_MANAGE',
    'SPACE_READ'
  ]
}

const productSpaceFallbacks: ProductSpaceCard[] = [
  {
    id: 'ibm-i-modernization',
    name: 'IBM i Modernization',
    description: '面向 IBM i 现代化项目的文档、Wiki、图谱和可信问答知识空间。',
    documents: 128,
    reviews: 35,
    owner: '我创建',
    status: 'REVIEW_REQUIRED',
    wikiPages: 42,
    source: 'sample'
  },
  {
    id: 'legacy-spec-factory',
    name: 'Legacy Spec Factory',
    description: '从遗留系统证据生成业务规格、接口说明和迁移边界。',
    documents: 64,
    reviews: 12,
    owner: '我创建',
    status: 'REVIEW_REQUIRED',
    wikiPages: 18,
    source: 'sample'
  },
  {
    id: 'ai-engineering-playbook',
    name: 'AI Engineering Playbook',
    description: '评测、RAG、提示词、模型网关、监控和安全实践知识库。',
    documents: 42,
    reviews: 8,
    owner: '我创建',
    status: 'READY',
    wikiPages: 12,
    source: 'sample'
  }
]

export function useSpaces() {
  const spaces = ref<ApiSpace[]>([])
  const currentUser = ref<ApiMe | null>(defaultMockMe)
  const selectedSpace = ref<ApiSpace | null>(null)
  const selectedSpaceId = ref(defaultSpaceId)
  const selectedProductSpaceId = ref(defaultSpaceId)
  const spaceInfoOverrides = ref<Record<string, SpaceInfoDraft>>({})

  const isLoadingSpaces = ref(true)
  const isLoadingSpace = ref(false)
  const isCreateSpaceOpen = ref(false)
  const isCreatingSpace = ref(false)

  const spacesError = ref('')
  const authError = ref('')
  const createSpaceError = ref('')
  const createSpaceStatus = ref('')

  const createSpaceDraft = ref<CreateSpaceDraft>({
    name: '',
    description: '',
    type: 'document',
    indexStrategy: 'rag',
    owner: '我创建'
  })

  const currentCapabilities = computed(() => new Set(currentUser.value?.capabilities ?? []))
  const canManageSpaces = computed(() => hasCapability('SPACE_MANAGE'))
  const canWriteContent = computed(() => hasCapability('CONTENT_WRITE'))
  const canOperateKnowledge = computed(() => hasCapability('KNOWLEDGE_OPERATE'))
  const canManageMembers = computed(() => hasCapability('MEMBER_MANAGE'))
  const canReadGovernance = computed(() => hasCapability('GOVERNANCE_READ'))

  const selectedProductApiSpace = computed(
    () =>
      spaces.value.find(space => space.id === selectedProductSpaceId.value) ?? selectedSpace.value
  )
  const apiBackedProductSpace = computed(() => selectedProductApiSpace.value ?? selectedSpace.value)

  const productSpaceCards = computed<ProductSpaceCard[]>(() => {
    const apiCards = spaces.value.map(space => ({
      id: space.id,
      name: spaceInfoOverrides.value[space.id]?.name ?? space.name,
      description: spaceInfoOverrides.value[space.id]?.description ?? space.description,
      documents: space.documentCount,
      reviews: space.reviewCount,
      owner: space.owner,
      status: space.status,
      wikiPages: space.wikiPageCount,
      source: 'api' as const
    }))
    const apiIds = new Set(apiCards.map(space => space.id))
    return [
      ...apiCards,
      ...productSpaceFallbacks
        .filter(space => !apiIds.has(space.id))
        .map(space => ({
          ...space,
          name: spaceInfoOverrides.value[space.id]?.name ?? space.name,
          description: spaceInfoOverrides.value[space.id]?.description ?? space.description
        }))
    ]
  })

  const selectedProductSpace = computed(
    () =>
      productSpaceCards.value.find(space => space.id === selectedProductSpaceId.value) ??
      productSpaceCards.value[0]
  )

  async function loadCurrentUser() {
    try {
      currentUser.value = await getCurrentUser()
      authError.value = ''
    } catch (error) {
      authError.value = safeError(
        error,
        'Current user context unavailable; using local mock permissions.'
      )
    }
  }

  async function loadSpaces() {
    isLoadingSpaces.value = true
    spacesError.value = ''
    try {
      spaces.value = await listSpaces()
      const preferred = spaces.value.find(space => space.id === defaultSpaceId) ?? spaces.value[0]
      if (preferred) {
        selectSpaceIds(preferred.id)
      }
      return preferred?.id ?? defaultSpaceId
    } catch (error) {
      spacesError.value = safeError(error, 'Unable to load Knowledge Spaces.')
      selectSpaceIds(defaultSpaceId)
      return defaultSpaceId
    } finally {
      isLoadingSpaces.value = false
    }
  }

  async function loadSelectedSpace(spaceId: string) {
    isLoadingSpace.value = true
    try {
      selectedSpace.value = await getSpace(spaceId)
    } catch (error) {
      selectedSpace.value = null
      throw error
    } finally {
      isLoadingSpace.value = false
    }
  }

  function selectSpaceIds(spaceId: string) {
    selectedSpaceId.value = spaceId
    selectedProductSpaceId.value = spaceId
  }

  function openCreateSpacePanel() {
    if (!canManageSpaces.value) {
      createSpaceStatus.value = '当前账号没有创建知识库权限。'
      return
    }
    createSpaceDraft.value = {
      name: '',
      description: '',
      type: 'document',
      indexStrategy: 'rag',
      owner: '我创建'
    }
    createSpaceError.value = ''
    createSpaceStatus.value = ''
    isCreateSpaceOpen.value = true
  }

  function closeCreateSpacePanel() {
    isCreateSpaceOpen.value = false
    createSpaceError.value = ''
  }

  async function createProductSpace() {
    if (!canManageSpaces.value) {
      createSpaceError.value = '当前账号没有创建知识库权限。'
      return null
    }
    const draft = {
      ...createSpaceDraft.value,
      name: createSpaceDraft.value.name.trim(),
      description: createSpaceDraft.value.description.trim(),
      owner: createSpaceDraft.value.owner.trim() || '我创建'
    }
    if (draft.name.length === 0) {
      createSpaceError.value = '请输入知识库名称。'
      return null
    }
    isCreatingSpace.value = true
    createSpaceError.value = ''
    createSpaceStatus.value = ''
    try {
      const created = await createSpaceApi(draft)
      spaces.value = [created, ...spaces.value.filter(space => space.id !== created.id)]
      selectSpaceIds(created.id)
      createSpaceStatus.value = '知识库已创建'
      isCreateSpaceOpen.value = false
      return created
    } catch (error) {
      createSpaceError.value = safeError(error, 'Knowledge Space creation failed safely.')
      return null
    } finally {
      isCreatingSpace.value = false
    }
  }

  function updateSelectedSpaceInfo(spaceId: string, draft: SpaceInfoDraft) {
    spaceInfoOverrides.value = {
      ...spaceInfoOverrides.value,
      [spaceId]: draft
    }
    spaces.value = spaces.value.map(space =>
      space.id === spaceId ? { ...space, ...draft } : space
    )
    if (selectedSpace.value?.id === spaceId) {
      selectedSpace.value = { ...selectedSpace.value, ...draft }
    }
  }

  function hasCapability(capability: string) {
    return currentCapabilities.value.has(capability)
  }

  return {
    spaces,
    currentUser,
    selectedSpace,
    selectedSpaceId,
    selectedProductSpaceId,
    spaceInfoOverrides,
    isLoadingSpaces,
    isLoadingSpace,
    isCreateSpaceOpen,
    isCreatingSpace,
    spacesError,
    authError,
    createSpaceError,
    createSpaceStatus,
    createSpaceDraft,
    currentCapabilities,
    canManageSpaces,
    canWriteContent,
    canOperateKnowledge,
    canManageMembers,
    canReadGovernance,
    selectedProductApiSpace,
    apiBackedProductSpace,
    productSpaceCards,
    selectedProductSpace,
    loadCurrentUser,
    loadSpaces,
    loadSelectedSpace,
    selectSpaceIds,
    openCreateSpacePanel,
    closeCreateSpacePanel,
    createProductSpace,
    updateSelectedSpaceInfo,
    hasCapability
  }
}

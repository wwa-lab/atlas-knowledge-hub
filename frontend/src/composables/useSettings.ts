import { computed, ref } from 'vue'
import {
  clearDeepSeekConfiguration,
  getDeepSeekConfiguration,
  listAuditEvents,
  listModelAdapters,
  saveDeepSeekConfiguration
} from '@/api'
import { safeError } from '@/composables/apiErrors'
import {
  isPlaceholderSettingsPanel,
  isSecretConfigured,
  modelTypeToCategory,
  secretStatusByKey
} from '@/domain/viewModels'
import {
  accountProfileRows,
  addableModelTypes,
  codeFontOptions,
  fontSizeOptions,
  interfaceFontOptions,
  languageOptions,
  maskedApiKey,
  memberRoleOptions,
  messageEmbeddingOptions,
  modelCategories,
  modelProviderOptions,
  safeErrorPreviews,
  settingsSurfaces,
  spaceOperationalMetadata,
  themeOptions,
  thinkingOptions
} from '@/composables/settingsOptions'
import type {
  ApiInfoState,
  AuditSummary,
  GeneralSettingsState,
  MemberRole,
  MessageIndexStat,
  ModelCategory,
  ModelSource,
  PendingInvitation,
  SettingsPanel,
  SpaceInfoDraft,
  SpaceInfoEditableField,
  SpaceInfoRow,
  SpaceMember,
  SpaceOperationalMetadata,
  VueModelConfig,
  VueModelDraft,
  ProductSpaceCard
} from '@/domain/viewModels'
import type { ApiAuditEvent, ApiModelCapability, ApiModelConfiguration, ApiSpace } from '@/types'

export interface UseSettingsOptions {
  selectedProductSpace: () => ProductSpaceCard
  selectedProductApiSpace: () => ApiSpace | null
  selectedSpaceId: () => string
  canReadGovernance: () => boolean
  updateSelectedSpaceInfo: (spaceId: string, draft: SpaceInfoDraft) => void
}

export function useSettings(options: UseSettingsOptions) {
  const settingsOpen = ref(false)
  const settingsPanel = ref<SettingsPanel>('models')
  const generalSettings = ref<GeneralSettingsState>({
    language: 'zh-CN',
    themeMode: 'light',
    interfaceFont: 'system',
    codeFont: 'system-mono',
    fontSize: 'normal',
    memoryEnabled: true
  })
  const apiInfo = ref<ApiInfoState>({
    keyVersion: 1,
    baseUrl: '/api',
    docsPath: '/docs/api',
    status: ''
  })
  const messageIndexEnabled = ref(false)
  const messageEmbeddingModel = ref('text-embedding-v4')
  const auditEvents = ref<ApiAuditEvent[]>([])
  const auditError = ref('')
  const isLoadingAuditEvents = ref(false)
  const models = ref<VueModelConfig[]>([
    {
      id: 'deepseek-flash',
      category: 'chat',
      displayName: 'DeepSeek Flash',
      provider: 'DeepSeek',
      source: 'API',
      name: 'deepseek-v4-flash',
      baseUrl: 'mock://deepseek-compatible',
      apiKeyStatus: 'configured',
      secretStatuses: [],
      supportsMultimodal: true,
      thinkingFormat: 'none'
    },
    {
      id: 'dashscope-embedding',
      category: 'embedding',
      displayName: 'text-embedding-v4',
      provider: 'Aliyun DashScope',
      source: 'API',
      name: 'text-embedding-v4',
      baseUrl: 'mock://dashscope-compatible',
      apiKeyStatus: 'configured',
      secretStatuses: [],
      supportsMultimodal: false,
      thinkingFormat: 'none'
    }
  ])
  const modelCapabilities = ref<ApiModelCapability[]>([])
  const deepSeekConfiguration = ref<ApiModelConfiguration | null>(null)
  const modelApiError = ref('')
  const modelSaveStatus = ref('')
  const activeModelCategory = ref<ModelCategory>('all')
  const isModelAddMenuOpen = ref(false)
  const selectedModelId = ref('deepseek-flash')
  const modelDraft = ref<VueModelDraft | null>(null)
  const apiKeyInput = ref('')
  const modelTestStatus = ref('')
  const memberSearch = ref('')
  const memberActionStatus = ref('')
  const spaceInfoStatus = ref('')
  const activeSpaceInfoEditField = ref<SpaceInfoEditableField>(null)
  const spaceInfoDraft = ref<SpaceInfoDraft>({ name: '', description: '' })
  const pendingInvitations = ref<PendingInvitation[]>([])
  const spaceMembers = ref<SpaceMember[]>([
    {
      id: 'member-leo',
      name: 'leo',
      email: 'leo@example.com',
      role: 'owner',
      joinedAt: '2026/06/23 13:19',
      removable: false
    },
    {
      id: 'member-pkos-admin',
      name: 'Atlas Admin',
      email: 'admin@example.com',
      role: 'owner',
      joinedAt: '2026/06/26 15:44',
      removable: true
    }
  ])

  const apiKeyDisplayValue = computed(() => {
    void apiInfo.value.keyVersion
    return maskedApiKey
  })
  const visibleModels = computed(() =>
    activeModelCategory.value === 'all'
      ? models.value
      : models.value.filter(model => model.category === activeModelCategory.value)
  )
  const modelApiStatus = computed(() =>
    modelCapabilities.value.length > 0 ? 'API-backed masked capabilities' : 'Sample model fallback'
  )
  const messageIndexConfigured = computed(
    () => messageIndexEnabled.value && messageEmbeddingModel.value.length > 0
  )
  const selectedMessageEmbeddingModel = computed(
    () =>
      messageEmbeddingOptions.find(option => option.value === messageEmbeddingModel.value)?.label ??
      '未选择'
  )
  const messageIndexStats = computed<MessageIndexStat[]>(() => [
    {
      label: 'Embedding 模型',
      value: selectedMessageEmbeddingModel.value,
      note: '用于消息语义搜索的 mock-safe 模型配置。'
    },
    { label: '已索引消息', value: '248', note: '仅为演示统计，不读取真实聊天历史。' },
    { label: '已索引对话', value: '18', note: '只表示本地 Vue 状态，不写入向量库。' },
    { label: '最后索引', value: '2026/07/05 10:30', note: 'Mock timestamp for UI acceptance.' }
  ])
  const currentSettingsSurface = computed(() =>
    isPlaceholderSettingsPanel(settingsPanel.value) ? settingsSurfaces[settingsPanel.value] : null
  )
  const selectedSpaceInfoMetadata = computed<SpaceOperationalMetadata>(
    () =>
      spaceOperationalMetadata[options.selectedProductSpace().id] ?? {
        createdAt: '2026-06-23T13:19:00Z',
        storageQuota: '5 GB',
        storageUsed: '0 MB',
        storageUsageRate: '0%'
      }
  )
  const selectedSpaceInfoRows = computed<SpaceInfoRow[]>(() => [
    {
      key: 'id',
      label: '空间 ID',
      note: '当前知识空间的唯一标识',
      value: options.selectedProductSpace().id
    },
    {
      key: 'name',
      label: '空间名称',
      note: '当前知识空间在 Atlas 中的显示名称',
      value: options.selectedProductSpace().name,
      field: 'name'
    },
    {
      key: 'description',
      label: '空间描述',
      note: '帮助成员理解此空间的内容边界',
      value: options.selectedProductSpace().description,
      field: 'description'
    },
    {
      key: 'status',
      label: '空间状态',
      note: '空间当前的运行与质量门禁状态',
      value: spaceInfoStatus.value || options.selectedProductSpace().status
    },
    {
      key: 'createdAt',
      label: '空间创建时间',
      note: '空间创建的 mock 时间戳',
      value: formatSpaceTimestamp(
        options.selectedProductApiSpace()?.createdAt ?? selectedSpaceInfoMetadata.value.createdAt
      )
    },
    {
      key: 'storageQuota',
      label: '存储配额',
      note: '空间的总存储空间配额',
      value: selectedSpaceInfoMetadata.value.storageQuota
    },
    {
      key: 'storageUsed',
      label: '已使用存储',
      note: '已使用的 mock 存储空间',
      value: selectedSpaceInfoMetadata.value.storageUsed
    },
    {
      key: 'storageUsageRate',
      label: '存储使用率',
      note: '当前 mock 存储占用比例',
      value: selectedSpaceInfoMetadata.value.storageUsageRate
    }
  ])
  const pendingInvitationCount = computed(() => pendingInvitations.value.length)
  const filteredSpaceMembers = computed(() => {
    const query = memberSearch.value.trim().toLocaleLowerCase()
    if (!query) {
      return spaceMembers.value
    }
    return spaceMembers.value.filter(
      member =>
        member.name.toLocaleLowerCase().includes(query) ||
        member.email.toLocaleLowerCase().includes(query) ||
        memberRoleLabel(member.role).toLocaleLowerCase().includes(query)
    )
  })
  const auditSummary = computed<AuditSummary>(() => ({
    total: auditEvents.value.length,
    security: auditEvents.value.filter(event => event.severity === 'SECURITY').length,
    denied: auditEvents.value.filter(event => event.result === 'DENIED').length
  }))

  function openSettings(panel: SettingsPanel = 'general') {
    if (panel === 'audit' && !options.canReadGovernance()) {
      settingsPanel.value = 'general'
      settingsOpen.value = true
      return
    }
    settingsPanel.value = panel
    settingsOpen.value = true
    if (panel === 'audit') {
      void loadAuditEvents()
    }
  }

  function setSettingsPanel(panel: SettingsPanel) {
    settingsPanel.value = panel
  }

  function closeSettings() {
    settingsOpen.value = false
    modelDraft.value = null
    activeSpaceInfoEditField.value = null
  }

  function updateGeneralSettings(settings: GeneralSettingsState) {
    generalSettings.value = settings
  }

  function updateSpaceInfoDraft(draft: SpaceInfoDraft) {
    spaceInfoDraft.value = draft
  }

  function updateMemberSearch(query: string) {
    memberSearch.value = query
  }

  function noteAuditEntry() {
    memberActionStatus.value = '审计日志入口为 mock。'
  }

  function updateMessageEmbeddingModel(model: string) {
    messageEmbeddingModel.value = model
  }

  async function loadAuditEvents(spaceId = options.selectedSpaceId()) {
    if (!spaceId || !options.canReadGovernance()) {
      auditEvents.value = []
      auditError.value = '当前账号没有审计日志读取权限。'
      return
    }
    isLoadingAuditEvents.value = true
    auditError.value = ''
    try {
      auditEvents.value = await listAuditEvents(spaceId)
    } catch (error) {
      auditError.value = safeError(error, 'Audit log API unavailable.')
    } finally {
      isLoadingAuditEvents.value = false
    }
  }

  function beginSpaceInfoEdit(field: keyof SpaceInfoDraft) {
    activeSpaceInfoEditField.value = field
    spaceInfoStatus.value = ''
    spaceInfoDraft.value = {
      name: options.selectedProductSpace().name,
      description: options.selectedProductSpace().description
    }
  }

  function cancelSpaceInfoEdit() {
    activeSpaceInfoEditField.value = null
    spaceInfoDraft.value = { name: '', description: '' }
  }

  function saveSpaceInfoEdit() {
    const name = spaceInfoDraft.value.name.trim()
    const description = spaceInfoDraft.value.description.trim()
    if (name.length === 0) {
      spaceInfoStatus.value = '空间名称不能为空。'
      return
    }
    const spaceId = options.selectedProductSpace().id
    options.updateSelectedSpaceInfo(spaceId, { name, description })
    activeSpaceInfoEditField.value = null
    spaceInfoStatus.value = '空间信息已更新'
  }

  function formatSpaceTimestamp(value: string) {
    const [datePart, timePart = ''] = value.replace('Z', '').split('T')
    const date = datePart.replaceAll('-', '/')
    const time = timePart.slice(0, 5)
    return time ? `${date} ${time}` : date
  }

  function memberRoleLabel(role: MemberRole) {
    return memberRoleOptions.find(option => option.value === role)?.label ?? role
  }

  function handleMemberRoleChange(memberId: string, role: MemberRole) {
    const target = spaceMembers.value.find(member => member.id === memberId)
    spaceMembers.value = spaceMembers.value.map(member =>
      member.id === memberId ? { ...member, role } : member
    )
    memberActionStatus.value = `${target?.name ?? '成员'} 的角色已更新为 ${memberRoleLabel(role)}（mock only）。`
  }

  function inviteMockMember() {
    memberActionStatus.value = '邀请成员动作已记录为 mock，不发送邮件，也不创建真实账号。'
  }

  function copyInviteMockLink() {
    memberActionStatus.value = '邀请链接已复制为 mock 状态，不包含真实 workspace token。'
  }

  function removeSpaceMember(memberId: string) {
    const target = spaceMembers.value.find(member => member.id === memberId)
    if (!target || !target.removable) {
      memberActionStatus.value = '所有者账号不能在当前 mock 面板中移除。'
      return
    }
    spaceMembers.value = spaceMembers.value.filter(member => member.id !== memberId)
    memberActionStatus.value = `${target.name} 已从当前 mock 空间成员列表移除。`
  }

  function toggleApiKeyVisibility() {
    apiInfo.value = { ...apiInfo.value, status: 'API Key 保持隐藏（mock）' }
  }

  function copyApiInfoValue(successMessage: string) {
    apiInfo.value = { ...apiInfo.value, status: successMessage }
  }

  function refreshApiKey() {
    apiInfo.value = {
      ...apiInfo.value,
      keyVersion: apiInfo.value.keyVersion + 1,
      status: 'API Key 已刷新（mock）'
    }
  }

  function openApiDocumentation() {
    apiInfo.value = { ...apiInfo.value, status: 'API 文档入口已准备（mock）' }
  }

  function toggleMessageIndexing() {
    messageIndexEnabled.value = !messageIndexEnabled.value
  }

  async function loadModelCapabilities() {
    modelApiError.value = ''
    try {
      const [capabilities, configuration] = await Promise.all([
        listModelAdapters(),
        getDeepSeekConfiguration()
      ])
      modelCapabilities.value = capabilities
      deepSeekConfiguration.value = configuration
      if (capabilities.length > 0) {
        models.value = capabilities.map(modelCapabilityToVueModel)
        selectedModelId.value = capabilities[0].modelKey
      }
    } catch (error) {
      modelApiError.value = safeError(error, 'Model capability API unavailable.')
    }
  }

  function toggleModelAddMenu() {
    isModelAddMenuOpen.value = !isModelAddMenuOpen.value
  }

  function updateActiveModelCategory(category: ModelCategory) {
    activeModelCategory.value = category
  }

  function modelCapabilityToVueModel(capability: ApiModelCapability): VueModelConfig {
    const configurationCredential =
      capability.adapterKey === 'deepseek'
        ? secretStatusByKey(deepSeekConfiguration.value?.secretStatuses, 'credential')
        : undefined
    const capabilityCredential = secretStatusByKey(capability.secretStatuses, 'credential')
    const credential = configurationCredential ?? capabilityCredential
    return {
      id: capability.modelKey,
      category: modelTypeToCategory(capability.modelType),
      displayName: capability.displayName,
      provider: capability.providerFamily,
      source: 'API',
      name: capability.modelKey,
      baseUrl: '',
      apiKeyStatus: isSecretConfigured(credential) ? 'configured' : 'not_configured',
      secretStatuses:
        capability.adapterKey === 'deepseek' && deepSeekConfiguration.value
          ? deepSeekConfiguration.value.secretStatuses
          : capability.secretStatuses,
      supportsMultimodal: capability.modelType === 'VISION',
      thinkingFormat: 'none',
      sourceLabel: `${capability.adapterKey} · ${capability.status}`
    }
  }

  function openModelEditor(model: VueModelConfig) {
    selectedModelId.value = model.id
    isModelAddMenuOpen.value = false
    modelTestStatus.value = ''
    modelSaveStatus.value = ''
    modelDraft.value = { ...model, apiKeyEditing: false }
  }

  function updateModelDraft(draft: VueModelDraft) {
    modelDraft.value = draft
  }

  function updateApiKeyInput(value: string) {
    apiKeyInput.value = value
  }

  function closeModelEditor() {
    modelDraft.value = null
    modelTestStatus.value = ''
  }

  function setModelSource(source: ModelSource) {
    if (!modelDraft.value) return
    modelDraft.value = {
      ...modelDraft.value,
      source,
      provider:
        source === 'Ollama'
          ? 'Ollama'
          : modelDraft.value.provider === 'Ollama'
            ? 'DeepSeek'
            : modelDraft.value.provider,
      baseUrl: source === 'Ollama' ? 'http://localhost:11434' : modelDraft.value.baseUrl
    }
  }

  async function removeApiKey() {
    if (!modelDraft.value) return
    try {
      if (isDeepSeekDraft(modelDraft.value)) {
        deepSeekConfiguration.value = await clearDeepSeekConfiguration()
      }
      modelDraft.value = {
        ...modelDraft.value,
        apiKeyStatus: 'not_configured',
        apiKeyEditing: false,
        secretStatuses:
          deepSeekConfiguration.value?.secretStatuses ?? modelDraft.value.secretStatuses
      }
      apiKeyInput.value = ''
      modelTestStatus.value = 'API key state cleared.'
    } catch (error) {
      modelApiError.value = safeError(error, 'Model key removal failed safely.')
    }
  }

  function startApiKeyReplace() {
    if (!modelDraft.value) return
    apiKeyInput.value = ''
    modelDraft.value = { ...modelDraft.value, apiKeyEditing: true }
  }

  function cancelApiKeyReplace() {
    if (!modelDraft.value) return
    apiKeyInput.value = ''
    modelDraft.value = { ...modelDraft.value, apiKeyEditing: false }
  }

  function confirmApiKeyReplace() {
    if (!modelDraft.value || apiKeyInput.value.trim().length === 0) return
    modelDraft.value = { ...modelDraft.value, apiKeyStatus: 'configured', apiKeyEditing: false }
  }

  function testModelConnection() {
    if (!modelDraft.value) return
    if (isDeepSeekDraft(modelDraft.value)) {
      modelTestStatus.value =
        modelDraft.value.apiKeyStatus === 'configured'
          ? 'DeepSeek configuration is ready; raw key remains masked.'
          : 'DeepSeek needs an API key before configured Ask can run.'
      return
    }
    if (modelDraft.value.sourceLabel?.startsWith('mock-model')) {
      modelTestStatus.value =
        modelDraft.value.apiKeyStatus === 'configured'
          ? 'Mock connection passed with masked credentials.'
          : 'Mock connection needs configured API key state.'
      return
    }
    modelTestStatus.value = 'Connection test is coming soon for this model type.'
  }

  function toggleModelMultimodal() {
    if (!modelDraft.value) return
    modelDraft.value = {
      ...modelDraft.value,
      supportsMultimodal: !modelDraft.value.supportsMultimodal
    }
  }

  async function saveModelEditor() {
    const draft = modelDraft.value
    if (!draft || draft.name.trim().length === 0) return
    modelApiError.value = ''
    modelSaveStatus.value = ''
    const { apiKeyEditing, ...persisted } = {
      ...draft,
      name: draft.name.trim(),
      displayName: draft.displayName.trim() || draft.name.trim(),
      baseUrl: draft.baseUrl.trim()
    }
    void apiKeyEditing
    if (isDeepSeekDraft(draft)) {
      const apiKey = apiKeyInput.value.trim()
      if (apiKey.length === 0 && persisted.apiKeyStatus !== 'configured') {
        modelTestStatus.value = 'DeepSeek API key is required before saving this runtime model.'
        return
      }
      try {
        deepSeekConfiguration.value = await saveDeepSeekConfiguration({
          endpoint: persisted.baseUrl,
          modelName: persisted.name,
          ...(apiKey.length > 0 ? { apiKey } : {})
        })
      } catch (error) {
        modelApiError.value = safeError(error, 'Model configuration save failed safely.')
        return
      }
      persisted.apiKeyStatus = isSecretConfigured(
        secretStatusByKey(deepSeekConfiguration.value.secretStatuses, 'credential')
      )
        ? 'configured'
        : 'not_configured'
      persisted.secretStatuses = deepSeekConfiguration.value.secretStatuses
      persisted.baseUrl = ''
    } else if (persisted.category !== 'chat') {
      persisted.apiKeyStatus = 'not_configured'
    }
    models.value = models.value.map(model => (model.id === persisted.id ? persisted : model))
    selectedModelId.value = persisted.id
    modelTestStatus.value = ''
    modelSaveStatus.value = '配置已更新'
    apiKeyInput.value = ''
    modelDraft.value = null
  }

  function isDeepSeekDraft(model: VueModelConfig) {
    return (
      model.provider.toLowerCase().includes('deepseek') || model.sourceLabel?.startsWith('deepseek')
    )
  }

  return {
    settingsOpen,
    settingsPanel,
    generalSettings,
    apiInfo,
    safeErrorPreviews,
    messageIndexEnabled,
    messageEmbeddingModel,
    auditEvents,
    auditError,
    isLoadingAuditEvents,
    models,
    modelCapabilities,
    deepSeekConfiguration,
    modelApiError,
    modelSaveStatus,
    activeModelCategory,
    isModelAddMenuOpen,
    selectedModelId,
    modelDraft,
    apiKeyInput,
    modelTestStatus,
    memberSearch,
    memberActionStatus,
    spaceInfoStatus,
    activeSpaceInfoEditField,
    spaceInfoDraft,
    pendingInvitations,
    spaceMembers,
    modelCategories,
    addableModelTypes,
    modelProviderOptions,
    thinkingOptions,
    languageOptions,
    themeOptions,
    interfaceFontOptions,
    codeFontOptions,
    fontSizeOptions,
    memberRoleOptions,
    messageEmbeddingOptions,
    accountProfileRows,
    apiKeyDisplayValue,
    visibleModels,
    modelApiStatus,
    messageIndexConfigured,
    selectedMessageEmbeddingModel,
    messageIndexStats,
    currentSettingsSurface,
    selectedSpaceInfoMetadata,
    selectedSpaceInfoRows,
    pendingInvitationCount,
    filteredSpaceMembers,
    auditSummary,
    openSettings,
    setSettingsPanel,
    closeSettings,
    updateGeneralSettings,
    updateSpaceInfoDraft,
    updateMemberSearch,
    noteAuditEntry,
    updateMessageEmbeddingModel,
    loadAuditEvents,
    beginSpaceInfoEdit,
    cancelSpaceInfoEdit,
    saveSpaceInfoEdit,
    formatSpaceTimestamp,
    memberRoleLabel,
    handleMemberRoleChange,
    inviteMockMember,
    copyInviteMockLink,
    removeSpaceMember,
    toggleApiKeyVisibility,
    copyApiInfoValue,
    refreshApiKey,
    openApiDocumentation,
    toggleMessageIndexing,
    loadModelCapabilities,
    toggleModelAddMenu,
    updateActiveModelCategory,
    modelCapabilityToVueModel,
    openModelEditor,
    updateModelDraft,
    updateApiKeyInput,
    closeModelEditor,
    setModelSource,
    removeApiKey,
    startApiKeyReplace,
    cancelApiKeyReplace,
    confirmApiKeyReplace,
    testModelConnection,
    toggleModelMultimodal,
    saveModelEditor,
    isDeepSeekDraft
  }
}

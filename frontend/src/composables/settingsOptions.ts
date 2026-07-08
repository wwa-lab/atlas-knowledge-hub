import type {
  AccountProfileRow,
  GeneralCodeFont,
  GeneralFontSize,
  GeneralInterfaceFont,
  GeneralLanguage,
  GeneralOption,
  GeneralThemeMode,
  MemberRole,
  ModelCategory,
  ModelCategoryOption,
  PlaceholderSettingsPanel,
  SafeErrorPreview,
  SettingsSurface,
  SpaceOperationalMetadata,
  ThinkingOption
} from '@/domain/viewModels'

export const modelCategories: ModelCategoryOption[] = [
  { key: 'all', label: '全部' },
  { key: 'chat', label: '对话' },
  { key: 'embedding', label: 'Embedding' },
  { key: 'rerank', label: 'ReRank' },
  { key: 'vision', label: '视觉' },
  { key: 'speech', label: '语音' }
]

export const addableModelTypes = modelCategories.filter(
  (category): category is { key: Exclude<ModelCategory, 'all'>; label: string } =>
    category.key !== 'all'
)

export const modelProviderOptions = [
  'DeepSeek',
  'Aliyun DashScope',
  'OpenAI-compatible',
  'Custom API',
  'GitHub Models',
  'Ollama'
]

export const thinkingOptions: ThinkingOption[] = [
  { value: 'none', label: '不写入思考参数' },
  { value: 'provider_default', label: '使用 provider 默认格式' },
  { value: 'custom', label: '自定义参数格式' }
]

export const languageOptions: Array<GeneralOption<GeneralLanguage>> = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]

export const themeOptions: Array<GeneralOption<GeneralThemeMode>> = [
  { value: 'light', label: '浅色' },
  { value: 'dark', label: '深色' },
  { value: 'system', label: '跟随系统' }
]

export const interfaceFontOptions: Array<GeneralOption<GeneralInterfaceFont>> = [
  { value: 'system', label: '系统默认' },
  { value: 'pingfang', label: '苹方 / PingFang SC' },
  { value: 'microsoft', label: '微软雅黑 / Microsoft YaHei' }
]

export const codeFontOptions: Array<GeneralOption<GeneralCodeFont>> = [
  { value: 'system-mono', label: '系统默认' },
  { value: 'sf-mono', label: 'SF Mono' },
  { value: 'jetbrains', label: 'JetBrains Mono' }
]

export const fontSizeOptions: Array<GeneralOption<GeneralFontSize>> = [
  { value: 'small', label: '小' },
  { value: 'normal', label: '正常' },
  { value: 'large', label: '大' }
]

export const memberRoleOptions: Array<GeneralOption<MemberRole>> = [
  { value: 'owner', label: '所有者' },
  { value: 'admin', label: '管理员' },
  { value: 'reviewer', label: '审核者' },
  { value: 'viewer', label: '浏览者' }
]

export const messageEmbeddingOptions: Array<GeneralOption<string>> = [
  { value: 'text-embedding-v4', label: 'text-embedding-v4' },
  { value: 'mock-embedding-1024', label: 'mock-embedding-1024' }
]

export const accountProfileRows: AccountProfileRow[] = [
  {
    key: 'id',
    testId: 'vue-user-info-id',
    label: '用户 ID',
    value: 'atlas-demo-user-001',
    note: '稳定 mock ID，用于前端设置页验收。'
  },
  {
    key: 'name',
    testId: 'vue-user-info-name',
    label: '用户名',
    value: 'leo',
    note: '用于设置页、审核记录和 mock 活动流展示。'
  },
  {
    key: 'email',
    testId: 'vue-user-info-email',
    label: '邮箱',
    value: 'leo@example.com',
    note: '示例账号信息，不代表真实身份系统。'
  },
  {
    key: 'registered',
    testId: 'vue-user-info-registered',
    label: '注册时间',
    value: '2026/06/23 13:19',
    note: 'Mock registration timestamp for UI parity.'
  },
  {
    key: 'role',
    testId: 'vue-user-info-role',
    label: '当前角色',
    value: 'Workspace Owner',
    note: '生产权限以后端 RBAC 为准。'
  }
]

export const spaceOperationalMetadata: Record<string, SpaceOperationalMetadata> = {
  'ibm-i-modernization': {
    createdAt: '2026-06-23T13:19:00Z',
    storageQuota: '10 GB',
    storageUsed: '81.83 MB',
    storageUsageRate: '0.8%'
  },
  'legacy-spec-factory': {
    createdAt: '2026-06-18T10:30:00Z',
    storageQuota: '8 GB',
    storageUsed: '296 MB',
    storageUsageRate: '3.7%'
  },
  'ai-engineering-playbook': {
    createdAt: '2026-06-12T16:10:00Z',
    storageQuota: '6 GB',
    storageUsed: '144 MB',
    storageUsageRate: '2.4%'
  }
}

export const maskedApiKey = '••••••••••••••••••••••••••••••••'

export const settingsSurfaces: Record<PlaceholderSettingsPanel, SettingsSurface> = {
  registration: {
    title: '注册策略',
    status: 'Invite only',
    summary: '定义内部 Beta 前的加入策略、域名策略和审批边界，全部为 mock-safe 展示。',
    rows: [
      { label: '加入方式', value: '管理员邀请', note: '不开放自助注册。' },
      { label: '域名策略', value: 'example.internal masked', note: '不提交真实公司域名。' },
      { label: '审批', value: 'Workspace owner approval', note: '真实审批流未实现。' }
    ]
  },
  vector: {
    title: '向量数据库引擎',
    status: 'Adapter boundary',
    summary: '配置向量能力的产品面状态，不直连 pgvector、Milvus、Qdrant 或外部服务。',
    rows: [
      { label: 'Active adapter', value: 'mock-vector', note: '自动化验证不需要真实向量库。' },
      {
        label: 'Secret reference',
        value: 'status only',
        note: 'endpoint 与 credential 只显示配置状态。'
      },
      {
        label: 'Review policy',
        value: 'APPROVED_ONLY',
        note: 'review-required evidence 默认排除。'
      },
      { label: 'Health', value: 'configured metadata only', note: '不执行连接测试。' }
    ]
  },
  parser: {
    title: '解析引擎',
    status: 'Parser adapter',
    summary: '展示 document-normalize 这类内部能力的适配器状态，产品界面不依赖单一实现。',
    rows: [
      { label: 'PDF parser', value: 'mock-parser', note: '真实 parser runtime 留在 adapter 后。' },
      { label: 'Runtime command', value: 'masked status', note: '不展示本地命令或私有路径。' },
      {
        label: 'OCR route',
        value: 'queued when required',
        note: '仅展示状态，不执行 OCR worker。'
      },
      {
        label: 'Trace validator',
        value: 'source_trace required',
        note: '缺失则阻塞 Wiki / Graph / Ask。'
      }
    ]
  },
  storage: {
    title: '存储引擎',
    status: 'Storage adapter',
    summary: '展示对象存储和生成资产路径的产品面配置，不显示 bucket secret 或私有路径。',
    rows: [
      {
        label: 'Object storage',
        value: 'mock-s3-compatible',
        note: '无真实 bucket 或 credential。'
      },
      {
        label: 'Secret reference',
        value: 'missing / configured',
        note: '只显示状态，不显示 endpoint。'
      },
      { label: 'Generated assets', value: 'relative product paths', note: '禁止私有绝对路径。' },
      { label: 'Retention', value: 'review evidence kept', note: '原始机密文档不进入样例数据。' }
    ]
  }
}

export const safeErrorPreviews: SafeErrorPreview[] = [
  {
    code: 'AUTHENTICATION_REQUIRED',
    title: 'Authentication required',
    description: 'The request needs a valid Atlas user header.'
  },
  {
    code: 'PERMISSION_DENIED',
    title: 'Permission denied',
    description: 'The current user is not allowed to perform this action.'
  },
  {
    code: 'RATE_LIMITED',
    title: 'Rate limited',
    description: 'Too many requests. Please try again later.'
  },
  {
    code: 'SAFE_SYSTEM_ERROR',
    title: 'Safe system error',
    description: 'Unexpected server error with a correlation reference.'
  }
]

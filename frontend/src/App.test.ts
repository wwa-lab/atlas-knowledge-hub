import { mount } from '@vue/test-utils'
import App from './App.vue'
import { trustedAskRun } from './data/atlasMock'

describe('Atlas P0 full-stack productization shell', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('uses the real Vue 3 product page and opens model settings without the prototype iframe', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-product-page"]').text()).toContain('知识库')
    expect(wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').text()).toContain(
      'IBM i Modernization'
    )
    expect(wrapper.find('iframe.product-frame').exists()).toBe(false)

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-space-detail"]').text()).toContain(
      'IBM i Modernization Index'
    )

    const modelSettingsButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('模型管理'))
    expect(modelSettingsButton).toBeTruthy()
    await modelSettingsButton!.trigger('click')
    expect(wrapper.get('[data-testid="vue-model-manager"]').text()).toContain('模型配置')

    await wrapper.get('[data-testid="vue-model-card-deepseek-flash"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-model-editor"]').text()).toContain('编辑模型')
    await wrapper.get('[data-testid="vue-test-model"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-model-test-status"]').text()).toContain(
      'Mock connection passed'
    )

    await wrapper.get('[data-testid="vue-model-display-name"]').setValue('DeepSeek Vue Edited')
    await wrapper.get('[data-testid="vue-key-replace"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-key-input"]').isVisible()).toBe(true)

    await wrapper.get('[data-testid="vue-key-confirm"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-key-input"]').isVisible()).toBe(true)

    await wrapper.get('[data-testid="vue-key-input"]').setValue('mock-key-not-persisted')
    await wrapper.get('[data-testid="vue-key-confirm"]').trigger('click')
    expect(wrapper.find('[data-testid="vue-key-input"]').exists()).toBe(false)

    await wrapper.get('[data-testid="vue-save-model"]').trigger('click')
    expect(wrapper.find('[data-testid="vue-model-editor"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="vue-model-manager"]').text()).toContain('DeepSeek Vue Edited')
    expect(wrapper.html()).not.toContain('mock-key-not-persisted')
  })

  it('saves DeepSeek model settings while keeping an already configured key masked', async () => {
    const api = mockP0Api({ deepSeekConfigured: true })
    const wrapper = mount(App)
    await flushAsync()

    const modelSettingsButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('模型管理'))
    expect(modelSettingsButton).toBeTruthy()
    await modelSettingsButton!.trigger('click')
    await flushAsync()

    await wrapper.get('[data-testid="vue-model-card-deepseek-chat"]').trigger('click')
    await wrapper.get('[data-testid="vue-model-base-url"]').setValue('https://api.deepseek.com/v1/')
    await wrapper.get('[data-testid="vue-model-name"]').setValue('deepseek-reasoner')
    await wrapper.get('[data-testid="vue-save-model"]').trigger('click')
    await flushAsync()

    expect(api.lastModelConfigurationSave).toEqual({
      provider: 'deepseek',
      endpoint: 'https://api.deepseek.com/v1/',
      modelName: 'deepseek-reasoner'
    })
    expect(wrapper.find('[data-testid="vue-model-editor"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="vue-model-save-status"]').text()).toContain('配置已更新')
    expect(wrapper.html()).not.toContain('runtime-secret')
  })

  it('starts connector sync v0 and renders review-required source trace safely', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await wrapper.get('[data-testid="vue-connector-sync-tab"]').trigger('click')
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-connector-sync"]').text()).toContain('Mock Local Fixture')
    expect(wrapper.get('[data-testid="vue-connector-sync"]').text()).toContain(
      'Local fixture connector only'
    )

    await wrapper.get('[data-testid="vue-start-connector-sync"]').trigger('click')
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-connector-run-status"]').text()).toContain('Review 2')
    expect(wrapper.get('[data-testid="vue-connector-sync"]').text()).toContain('REVIEW_REQUIRED')
    expect(wrapper.findAll('[data-testid="vue-connector-item"]')).toHaveLength(3)
    expect(wrapper.get('[data-testid="vue-connector-trace"]').text()).toContain('source_trace')
    expect(wrapper.get('[data-testid="vue-connector-trace"]').text()).toContain('provenance')
    expect(wrapper.get('[data-testid="vue-connector-trace"]').text()).toContain(
      'MARKDOWN_CANDIDATE'
    )
    expect(wrapper.html()).not.toContain(`${'internal'}.invalid`)
    expect(wrapper.html()).not.toContain(`/${'Users'}/`)
    expect(wrapper.html()).not.toContain(`${'to'}ken=mock`)
  })

  it('renders worker dead-letter recovery and applies safe operator transitions', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await wrapper
      .findAll('button')
      .find(button => button.text() === '处理中心')!
      .trigger('click')
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-dead-letter-ops"]').text()).toContain('Worker recovery')
    expect(wrapper.get('[data-testid="vue-dead-letter-entry"]').text()).toContain('OPEN')
    expect(wrapper.get('[data-testid="vue-dead-letter-detail"]').text()).toContain(
      'SOURCE_UNREADABLE'
    )
    expect(wrapper.get('[data-testid="vue-dead-letter-detail"]').text()).toContain(
      'DEAD_LETTERED'
    )
    expect(wrapper.get('[data-testid="vue-dead-letter-trace"]').text()).toContain('source_trace')
    expect(wrapper.get('[data-testid="vue-dead-letter-attempt"]').text()).toContain(
      'FAILED_TERMINAL'
    )
    expect(wrapper.html()).not.toContain('/Users/')
    expect(wrapper.html()).not.toContain(`${'to'}ken=mock`)
    expect(wrapper.html()).not.toContain('StackTrace')

    await wrapper.get('[data-testid="vue-retry-dead-letter"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-dead-letter-detail"]').text()).toContain('RETRIED')
    expect(wrapper.get('[data-testid="vue-dead-letter-detail"]').text()).toContain('WAITING_RETRY')

    await wrapper.get('[data-testid="vue-refresh-dead-letters"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-dead-letter-detail"]').text()).toContain('RETRIED')
  })

  it('registers a manual URL source as review-required metadata with source trace visible', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await flushAsync()
    await wrapper.findAll('button').find(button => button.text().includes('文档'))!.trigger('click')
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-manual-url-status"]').text()).toContain(
      'REVIEW_REQUIRED'
    )
    await wrapper.get('[data-testid="vue-manual-url-input"]').setValue('https://example.com/docs/url')
    await wrapper.get('[data-testid="vue-manual-url-title"]').setValue('Manual URL Fixture')
    await wrapper.get('[data-testid="vue-manual-url-ingest"]').trigger('submit')
    await flushAsync()

    const status = wrapper.get('[data-testid="vue-manual-url-status"]').text()
    expect(status).toContain('https://example.com/docs/url')
    expect(status).toContain('NO_FETCH_METADATA_ONLY')
    expect(status).toContain('REVIEW_REQUIRED_ONLY')
    expect(status).toContain('Manual URL metadata: https://example.com/docs/url')
  })

  it('renders real Vue administration panels without exposing production secrets', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    const settingsButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('全部设置'))
    expect(settingsButton).toBeTruthy()
    await settingsButton!.trigger('click')
    expect(wrapper.get('[data-testid="vue-admin-panel"]').text()).toContain('常规设置')
    expect(wrapper.get('[data-testid="vue-admin-panel"]').text()).toContain(
      '配置语言、外观等基础选项'
    )
    expect(wrapper.get('[data-testid="vue-general-language"]').element).toHaveProperty(
      'value',
      'zh-CN'
    )
    expect(wrapper.get('[data-testid="vue-general-theme"]').element).toHaveProperty(
      'value',
      'light'
    )
    await wrapper.get('[data-testid="vue-general-font-size-large"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-general-font-size-large"]').classes()).toContain('active')
    await wrapper.get('[data-testid="vue-general-memory"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-general-memory"]').attributes('aria-checked')).toBe(
      'false'
    )

    await wrapper
      .findAll('button')
      .find(button => button.text() === '用户信息')!
      .trigger('click')
    const userInfoText = wrapper.get('[data-testid="vue-user-info-panel"]').text()
    expect(userInfoText).toContain('用户信息')
    expect(userInfoText).toContain('用户 ID')
    expect(userInfoText).toContain('用户名')
    expect(userInfoText).toContain('邮箱')
    expect(userInfoText).toContain('注册时间')
    expect(userInfoText).toContain('atlas-demo-user-001')
    expect(userInfoText).toContain('leo@example.com')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '空间信息')!
      .trigger('click')
    const spaceInfoPanel = wrapper.get('[data-testid="vue-space-info-panel"]')
    expect(spaceInfoPanel.text()).toContain('空间信息')
    expect(wrapper.get('[data-testid="vue-space-info-id"]').text()).toContain('ibm-i-modernization')
    expect(wrapper.get('[data-testid="vue-space-info-name"]').text()).toContain(
      'IBM i Modernization'
    )
    expect(wrapper.get('[data-testid="vue-space-info-status"]').text()).toContain('REVIEW_REQUIRED')
    expect(wrapper.get('[data-testid="vue-space-info-storageQuota"]').text()).toContain('10 GB')
    expect(wrapper.get('[data-testid="vue-space-info-storageUsed"]').text()).toContain('81.83 MB')
    await wrapper.get('[data-testid="vue-space-info-edit-name"]').trigger('click')
    await wrapper
      .get('[data-testid="vue-space-info-name-input"]')
      .setValue('IBM i Modernization Workspace')
    await wrapper.get('[data-testid="vue-space-info-save"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-space-info-save-status"]').text()).toContain(
      '空间信息已更新'
    )
    expect(wrapper.get('[data-testid="vue-space-info-name"]').text()).toContain(
      'IBM i Modernization Workspace'
    )

    await wrapper
      .findAll('button')
      .find(button => button.text() === '成员管理')!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-member-manager"]').text()).toContain('待接受的邀请 0')
    expect(wrapper.get('[data-testid="vue-member-manager"]').text()).toContain('Atlas Admin')
    await wrapper.get('[data-testid="vue-member-search"]').setValue('admin@example.com')
    expect(wrapper.get('[data-testid="vue-member-manager"]').text()).toContain('Atlas Admin')
    await wrapper.get('[aria-label="Atlas Admin 角色"]').setValue('reviewer')
    expect(wrapper.get('[data-testid="vue-member-status"]').text()).toContain('审核者')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '注册配置')!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-admin-panel"]').text()).toContain('注册策略')
    expect(wrapper.get('.atlas-settings-placeholder').text()).toContain('不提交真实公司域名')

    await wrapper
      .findAll('button')
      .find(button => button.text() === 'API 信息')!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-admin-panel"]').text()).toContain('API 信息')
    expect(wrapper.get('[data-testid="vue-api-info-panel"]').text()).toContain(
      '查看和管理 Atlas API 调用信息'
    )
    expect(
      (wrapper.get('[data-testid="vue-api-key-value"]').element as HTMLInputElement).value
    ).toMatch(/^•{32}$/)
    expect(wrapper.html()).not.toContain('atlas-mock-api-key-v001')
    await wrapper.get('[data-testid="vue-api-key-reveal"]').trigger('click')
    await flushAsync()
    expect(
      (wrapper.get('[data-testid="vue-api-key-value"]').element as HTMLInputElement).value
    ).toMatch(/^•{32}$/)
    expect(wrapper.get('[data-testid="vue-api-info-status"]').text()).toContain('API Key 保持隐藏')
    await wrapper.get('[data-testid="vue-api-key-copy"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-api-info-status"]').text()).toContain('API Key 已复制')
    await wrapper.get('[data-testid="vue-api-key-refresh"]').trigger('click')
    await flushAsync()
    expect(
      (wrapper.get('[data-testid="vue-api-key-value"]').element as HTMLInputElement).value
    ).toMatch(/^•{32}$/)
    expect(wrapper.get('[data-testid="vue-api-info-status"]').text()).toContain('API Key 已刷新')
    await wrapper.get('[data-testid="vue-api-base-copy"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-api-info-status"]').text()).toContain('API 地址已复制')
    await wrapper.get('[data-testid="vue-api-doc-link"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-api-info-status"]').text()).toContain(
      'API 文档入口已准备'
    )

    await wrapper
      .findAll('button')
      .find(button => button.text() === '消息管理')!
      .trigger('click')
    const messagePanel = wrapper.get('[data-testid="vue-message-management"]')
    expect(messagePanel.text()).toContain('消息管理')
    expect(messagePanel.text()).toContain('启用消息索引')
    expect(messagePanel.text()).toContain('索引统计')
    expect(messagePanel.text()).toContain('消息索引未配置')
    expect(wrapper.get('[data-testid="vue-message-index-toggle"]').attributes('aria-checked')).toBe(
      'false'
    )

    await wrapper.get('[data-testid="vue-message-index-toggle"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-message-index-toggle"]').attributes('aria-checked')).toBe(
      'true'
    )
    expect(wrapper.get('[data-testid="vue-message-management"]').text()).toContain(
      'text-embedding-v4'
    )
    expect(wrapper.get('[data-testid="vue-message-index-stats"]').text()).toContain('已索引消息')
    expect(wrapper.get('[data-testid="vue-message-management"]').text()).toContain(
      '不执行真实 embedding'
    )

    await wrapper
      .findAll('button')
      .find(button => button.text() === '审计日志')!
      .trigger('click')
    await flushAsync()
    const auditPanel = wrapper.get('[data-testid="vue-audit-log-panel"]').text()
    expect(auditPanel).toContain('审计日志')
    expect(auditPanel).toContain('AUTH_GOVERNANCE_READ_DENIED')
    expect(auditPanel).toContain('MEMBERSHIP_ADDED')
    expect(auditPanel).not.toContain('password')
    expect(auditPanel).not.toContain('https://')
    expect(auditPanel).not.toContain(`/${'Users'}/`)
  })

  it('hides the audit log settings entry without governance-read capability', async () => {
    mockP0Api({ currentUser: authMe('VIEWER', ['CONTENT_READ', 'SPACE_READ']) })
    const wrapper = mount(App)
    await flushAsync()

    const settingsButton = wrapper
      .findAll('button')
      .find(button => button.text().includes('全部设置'))
    expect(settingsButton).toBeTruthy()
    await settingsButton!.trigger('click')

    expect(wrapper.findAll('button').some(button => button.text() === '审计日志')).toBe(false)
    expect(wrapper.find('[data-testid="vue-audit-log-panel"]').exists()).toBe(false)
  })

  it('opens the create Knowledge Space flow and adds the created space to the library', async () => {
    const api = mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-create-space-open"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-create-space-panel"]').text()).toContain('新建知识库')

    await wrapper.get('[data-testid="vue-create-space-name"]').setValue('Claims Ops Hub')
    await wrapper
      .get('[data-testid="vue-create-space-description"]')
      .setValue('Claims operations review space.')
    await wrapper.get('[data-testid="vue-create-space-submit"]').trigger('click')
    await flushAsync()

    expect(api.lastCreatedSpace).toMatchObject({
      name: 'Claims Ops Hub',
      description: 'Claims operations review space.',
      type: 'document',
      indexStrategy: 'rag',
      owner: '我创建'
    })
    expect(wrapper.find('[data-testid="vue-create-space-panel"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="vue-space-create-status"]').text()).toContain('知识库已创建')
    expect(wrapper.get('[data-testid="vue-space-card-claims-ops-hub"]').text()).toContain(
      'Claims Ops Hub'
    )
  })

  it('drives real Vue upload inventory into batch report and processing queues', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await wrapper
      .findAll('button')
      .find(button => button.text() === '文档')!
      .trigger('click')
    await wrapper.get('[data-testid="vue-upload-folder"]').trigger('click')

    expect(wrapper.get('[data-testid="vue-upload-review"]').text()).toContain('Upload Review')
    expect(wrapper.findAll('[data-testid="vue-inventory-row"]')).toHaveLength(7)
    expect(wrapper.get('[data-testid="vue-upload-review"]').text()).toContain('UNSUPPORTED')

    await wrapper.get('[data-testid="vue-create-batch"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-batch-summary"]').text()).toContain('Batch created')
    expect(wrapper.get('[data-testid="vue-batch-summary"]').text()).toContain('PDF_CONVERT_FAILED')
    expect(wrapper.get('[data-testid="vue-batch-summary"]').text()).toContain('OCR_REQUIRED')

    await wrapper.get('[data-testid="vue-view-report"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-batch-report"]').text()).toContain('source_trace:')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '处理中心')!
      .trigger('click')
    const processingText = wrapper.get('[data-testid="vue-processing-center"]').text()
    expect(processingText).toContain('parse failures')
    expect(processingText).toContain('OCR required')
    expect(wrapper.get('[data-testid="vue-processing-issue"]').text()).toContain(
      'blocks Wiki / Graph / Ask'
    )
  })

  it('cuts real Vue space, batch metadata, and review queues to API-backed state', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-api-space-status"]').text()).toContain(
      'API-backed metadata'
    )
    expect(wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').text()).toContain(
      'API'
    )

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-space-head"]').text()).toContain('API-backed Space')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '文档')!
      .trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-api-metadata"]').text()).toContain('Batches 0')
    await wrapper.get('[data-testid="vue-api-create-batch"]').trigger('click')
    await flushAsync()
    const metadataText = wrapper.get('[data-testid="vue-api-metadata"]').text()
    expect(metadataText).toContain('P0 Browser Batch')
    expect(metadataText).toContain('samples/p0/productization.md')
    expect(metadataText).toContain('chunk-p0')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '处理中心')!
      .trigger('click')
    const queueText = wrapper.get('[data-testid="vue-api-review-queues"]').text()
    expect(queueText).toContain('API review queues')
    expect(queueText).toContain('READY TO PUBLISH')
    expect(queueText).toContain('LOW CONFIDENCE')
  })

  it('cuts real Vue Wiki, graph, Ask, and model metadata to API-backed state', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await flushAsync()
    await wrapper
      .findAll('button')
      .find(button => button.text() === '文档')!
      .trigger('click')
    await wrapper.get('[data-testid="vue-api-create-batch"]').trigger('click')
    await flushAsync()
    await wrapper
      .findAll('button')
      .find(button => button.text() === '处理中心')!
      .trigger('click')
    await wrapper.get('[data-testid="vue-api-approve-file"]').trigger('click')
    await flushAsync()
    await wrapper
      .findAll('button')
      .find(button => button.text() === 'Wiki')!
      .trigger('click')
    await wrapper.get('[data-testid="vue-api-publish-file"]').trigger('click')
    await flushAsync()

    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('P0 Wiki')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain(
      'API-backed published metadata'
    )
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('p0-wiki')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('SOURCE_SUMMARY')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('PUBLISHED_FILE')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('MANUAL')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('P0 Evidence')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('chunk-p0')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('wiki warnings 1')
    expect(wrapper.get('[data-testid="vue-wiki-issues"]').text()).toContain('BROKEN_LINK')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '图谱')!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-product-graph"]').text()).toContain('P0 Browser Evidence')
    expect(wrapper.get('[data-testid="vue-graph-detail"]').text()).toContain('chunk-p0')
    expect(wrapper.get('[data-testid="vue-graph-detail"]').text()).toContain('Wiki page')

    await wrapper
      .findAll('button')
      .find(button => button.text().includes('对话'))!
      .trigger('click')
    await wrapper.get('[data-testid="vue-api-ask-submit"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'API-backed trusted answer'
    )
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain('chunk-p0')

    await wrapper
      .findAll('button')
      .find(button => button.text().includes('模型管理'))!
      .trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="vue-api-model-status"]').text()).toContain(
      'API-backed masked capabilities'
    )
    expect(wrapper.get('[data-testid="vue-model-manager"]').text()).toContain('mock-model')
  })

  it('renders product Wiki, graph evidence, and Trusted Ask states on the real Vue path', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain(
      'IBM i Modernization Index'
    )
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('source_trace:')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('PUBLISHED')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('INDEX')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('Modernization Hub')

    await wrapper
      .findAll('button')
      .find(button => button.text().includes('Source Trace Standard'))!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-wiki-page"]').text()).toContain('Trace Coverage')

    await wrapper
      .findAll('button')
      .find(button => button.text() === '图谱')!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-product-graph"]').text()).toContain('Knowledge Graph')
    await wrapper.findAll('[data-testid="vue-graph-node"]')[0].trigger('click')
    const graphDetail = wrapper.get('[data-testid="vue-graph-detail"]').text()
    expect(graphDetail).toContain('APPROVED')
    expect(graphDetail).toContain('productization.md')
    expect(graphDetail).toContain('wiki-p0')

    await wrapper
      .findAll('button')
      .find(button => button.text().includes('对话'))!
      .trigger('click')
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'Evidence citations'
    )
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'Review required'
    )
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'Not approved reusable knowledge'
    )
    await wrapper.get('[data-testid="vue-ask-mode"]').setValue('refusal')
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'NO_APPROVED_EVIDENCE'
    )
    await wrapper.get('[data-testid="vue-ask-mode"]').setValue('review-warning')
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'REVIEW_REQUIRED'
    )
    expect(wrapper.get('[data-testid="vue-trusted-ask-answer"]').text()).toContain(
      'Needs revision'
    )
  })

  it('renders API-backed space detail and disables unconnected prototype actions', async () => {
    mockP0Api()
    const wrapper = await mountWorkbench()

    expect(wrapper.get('[data-testid="space-list"]').text()).toContain('IBM i Modernization')
    expect(wrapper.get('[data-testid="space-detail"]').text()).toContain('Mock discovery package')
    expect(wrapper.findAll('[data-testid="coming-soon"]').length).toBeGreaterThanOrEqual(2)
    expect(wrapper.get('[data-tab="graph"]').attributes('data-state')).toBe('ready')
  })

  it('drives sample batch, review, publish, downstream refresh, and Ask through API calls', async () => {
    const api = mockP0Api()
    const wrapper = await mountWorkbench()

    await wrapper.get('[data-testid="create-sample-batch"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="batch-list"]').text()).toContain('P0 Browser Batch')
    expect(wrapper.get('[data-testid="file-list"]').text()).toContain('REVIEW_REQUIRED')
    expect(wrapper.get('[data-testid="chunk-list"]').text()).toContain('chunk-p0')

    await wrapper.get('[data-testid="approve-file"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="file-list"]').text()).toContain('APPROVED')

    await wrapper.get('[data-testid="publish-file"]').trigger('click')
    await flushAsync()
    expect(wrapper.get('[data-testid="wiki-pages"]').text()).toContain('PUBLISHED')

    await wrapper.get('[data-testid="refresh-graph-evidence"]').trigger('click')
    await flushAsync()
    expect(api.graphProjectionCreated).toBe(true)
    expect(api.vectorRunCreated).toBe(true)

    await wrapper.get('[data-testid="ask-submit"]').trigger('click')
    await flushAsync()
    const answer = wrapper.get('[data-testid="ask-answer"]').text()
    expect(answer).toContain('SUCCEEDED')
    expect(answer).toContain('REVIEW_REQUIRED')
    expect(answer).toContain('evidence 100%')
    expect(answer).toContain('citations 100%')
    expect(answer).toContain('chunk-p0')
  })

  it('disables API-backed write controls for viewer capabilities', async () => {
    mockP0Api({ currentUser: authMe('VIEWER', ['CONTENT_READ', 'SPACE_READ']) })
    const wrapper = mount(App)
    await flushAsync()

    expect(
      wrapper.get('[data-testid="vue-create-space-open"]').attributes('disabled')
    ).toBeDefined()

    await wrapper.get('[data-testid="vue-space-card-ibm-i-modernization"]').trigger('click')
    await flushAsync()
    await wrapper
      .findAll('button')
      .find(button => button.text() === '文档')!
      .trigger('click')

    expect(wrapper.get('[data-testid="vue-api-create-batch"]').attributes('disabled')).toBeDefined()
    expect(
      wrapper.get('[data-testid="vue-api-upload-documents"]').attributes('disabled')
    ).toBeDefined()
  })

  it('shows safe space loading errors without hiding coming-soon guardrails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({
        success: false,
        data: null,
        error: {
          code: 'SAFE_SYSTEM_ERROR',
          message: 'API failed safely',
          correlationId: 'req-safe-1'
        },
        meta: null
      })
    } as Response)

    const wrapper = await mountWorkbench()

    expect(wrapper.text()).toContain('API failed safely')
    expect(wrapper.text()).toContain('Reference req-safe-1.')
    expect(wrapper.findAll('[data-testid="coming-soon"]').length).toBeGreaterThan(0)
  })

  it('shows the safe API error contract without sensitive implementation details', async () => {
    mockP0Api()
    const wrapper = mount(App)
    await flushAsync()

    await wrapper
      .findAll('button')
      .find(button => button.text().includes('全部设置'))!
      .trigger('click')
    await wrapper
      .findAll('button')
      .find(button => button.text() === 'API 信息')!
      .trigger('click')
    const safeStates = wrapper.get('[data-testid="vue-safe-error-states"]')

    expect(safeStates.text()).toContain('AUTHENTICATION_REQUIRED')
    expect(safeStates.text()).toContain('PERMISSION_DENIED')
    expect(safeStates.text()).toContain('RATE_LIMITED')
    expect(safeStates.text()).toContain('SAFE_SYSTEM_ERROR')
    expect(safeStates.text()).not.toContain('https://')
    expect(safeStates.text()).not.toContain('/Users/')
    expect(safeStates.text()).not.toContain('password')
  })

  it('maps trusted Ask mock state to review-required answer evidence', () => {
    expect(trustedAskRun.status).toBe('SUCCEEDED')
    expect(trustedAskRun.answerReviewStatus).toBe('REVIEW_REQUIRED')
    expect(trustedAskRun.answerReviewLabel).toBe('Review required')
    expect(trustedAskRun.answerReusable).toBe(false)
    expect(trustedAskRun.reviewPolicy).toBe('INCLUDE_REVIEW_REQUIRED')
    expect(trustedAskRun.evidence[0].reviewStatus).toBe('APPROVED')
    expect(trustedAskRun.safeMessage).not.toContain('https://')
  })
})

async function mountWorkbench() {
  const wrapper = mount(App)
  await flushAsync()
  await wrapper.get('.workbench-entry').trigger('click')
  await flushAsync()
  return wrapper
}

function mockP0Api(
  options: { deepSeekConfigured?: boolean; currentUser?: ReturnType<typeof authMe> } = {}
) {
  const state = {
    batchCreated: false,
    fileReviewStatus: 'REVIEW_REQUIRED',
    wikiPublished: false,
    graphProjectionCreated: false,
    vectorRunCreated: false,
    connectorRunCreated: false,
    deadLetterStatus: 'OPEN',
    deadLetterJobStatus: 'DEAD_LETTERED',
    lastModelConfigurationSave: null as unknown,
    lastCreatedSpace: null as unknown,
    createdSpaces: [] as ReturnType<typeof space>[],
    manualUrlSources: [manualUrlSource()]
  }

  vi.spyOn(globalThis, 'fetch').mockImplementation(async (input, init) => {
    const url = String(input)
    const method = init?.method ?? 'GET'

    if (url.endsWith('/api/auth/me')) {
      return jsonOk(options.currentUser ?? authMe('SPACE_OWNER'))
    }

    if (url.endsWith('/api/spaces') && method === 'POST') {
      const body = JSON.parse(String(init?.body ?? '{}'))
      state.lastCreatedSpace = body
      const created = space({
        id: body.name
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/^-|-$/g, ''),
        name: body.name,
        description: body.description,
        owner: body.owner,
        status: 'REVIEW_REQUIRED'
      })
      state.createdSpaces = [created, ...state.createdSpaces]
      return jsonOk(created, 201)
    }

    if (url.endsWith('/api/spaces')) {
      return jsonOk([space(), ...state.createdSpaces])
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization')) {
      return jsonOk(space())
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/batches') && method === 'POST') {
      state.batchCreated = true
      return jsonOk(batch(), 201)
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/batches')) {
      return jsonOk(state.batchCreated ? [batch()] : [])
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/manual-url-sources') && method === 'POST') {
      const body = JSON.parse(String(init?.body ?? '{}'))
      const source = manualUrlSource({
        displayUrl: body.url,
        title: body.title,
        fetchIntent: body.fetchIntent ?? 'METADATA_ONLY'
      })
      state.manualUrlSources = [source, ...state.manualUrlSources]
      state.batchCreated = true
      return jsonOk(source, 201)
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/manual-url-sources')) {
      return jsonOk(state.manualUrlSources)
    }

    if (url.includes('/api/manual-url-sources/url-src-p0')) {
      return jsonOk(state.manualUrlSources[0])
    }

    if (url.endsWith('/api/batches/batch-p0/files')) {
      return jsonOk(state.batchCreated ? [file(state.fileReviewStatus)] : [])
    }

    if (url.endsWith('/api/files/file-p0/chunks')) {
      return jsonOk([chunk()])
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/review-queues')) {
      return jsonOk({
        spaceId: 'ibm-i-modernization',
        queues: [
          {
            type: 'READY_TO_PUBLISH',
            count: state.fileReviewStatus === 'APPROVED' ? 1 : 0,
            publishBlocked: false,
            representativeItems: []
          },
          {
            type: 'LOW_CONFIDENCE',
            count: 0,
            publishBlocked: true,
            representativeItems: []
          }
        ]
      })
    }

    if (url.endsWith('/api/files/file-p0/reviews') && method === 'POST') {
      state.fileReviewStatus = 'APPROVED'
      return jsonOk(
        {
          id: 1,
          targetType: 'file',
          targetId: 'file-p0',
          action: 'APPROVE',
          reviewer: 'p0-browser',
          comment: 'Approved',
          affectedChunks: ['chunk-p0'],
          createdAt: '2026-07-03T00:00:00Z'
        },
        201
      )
    }

    if (url.endsWith('/api/files/file-p0/publish') && method === 'POST') {
      state.wikiPublished = true
      return jsonOk(wikiPage(), 201)
    }

    if (url.includes('/api/spaces/ibm-i-modernization/wiki-pages')) {
      return jsonOk(state.wikiPublished ? [wikiPage()] : [])
    }

    if (url.includes('/api/spaces/ibm-i-modernization/wiki-page-issues')) {
      return jsonOk(state.wikiPublished ? [wikiIssue()] : [])
    }

    if (url.includes('/api/spaces/ibm-i-modernization/audit-events')) {
      return jsonOk(auditEvents())
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph/projection-runs')) {
      state.graphProjectionCreated = true
      return jsonOk(
        { runId: 'graph-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED', summary: {} },
        201
      )
    }

    if (url.includes('/api/spaces/ibm-i-modernization/vector-runs')) {
      state.vectorRunCreated = true
      return jsonOk(
        { runId: 'vector-run-p0', spaceId: 'ibm-i-modernization', status: 'SUCCEEDED' },
        201
      )
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/downstream-refresh') && method === 'POST') {
      state.graphProjectionCreated = true
      state.vectorRunCreated = true
      return jsonOk({
        graphRun: {
          runId: 'graph-run-p0',
          spaceId: 'ibm-i-modernization',
          adapterId: 'deterministic',
          scope: 'APPROVED_ONLY',
          status: 'SUCCEEDED',
          safeMessage: 'Graph done.',
          summary: {}
        },
        vectorRun: {
          runId: 'vector-run-p0',
          spaceId: 'ibm-i-modernization',
          batchId: 'batch-p0',
          adapterKey: 'mock-vector',
          operation: 'INDEX',
          status: 'SUCCEEDED',
          safeMessage: 'Vector done.'
        },
        message: 'Downstream evidence refreshed.'
      })
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph/nodes/node-p0')) {
      return jsonOk(graphDetail())
    }

    if (url.includes('/api/spaces/ibm-i-modernization/graph')) {
      return jsonOk(graphView())
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/ask') && method === 'POST') {
      return jsonOk(askRun(), 201)
    }

    if (url.endsWith('/api/ask-runs/ask-p0')) {
      return jsonOk(askRun())
    }

    if (url.endsWith('/api/ask-runs/ask-p0/quality-metrics')) {
      return jsonOk(askQualityMetrics())
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/ask-sessions')) {
      return jsonOk(askSessions())
    }

    if (url.endsWith('/api/ask-sessions/ask-session-p0')) {
      return jsonOk(askSessionDetail())
    }

    if (url.endsWith('/api/connector-definitions')) {
      return jsonOk(connectorDefinitions())
    }

    if (url.endsWith('/api/spaces/ibm-i-modernization/connector-sync-jobs') && method === 'POST') {
      state.connectorRunCreated = true
      return jsonOk(connectorRun(), 201)
    }

    if (url.endsWith('/api/connector-sync-runs/connector-run-p0')) {
      return jsonOk(connectorRun())
    }

    if (url.endsWith('/api/connector-sync-runs/connector-run-p0/items')) {
      return jsonOk(state.connectorRunCreated ? connectorItems() : [])
    }

    if (url.endsWith('/api/dead-letter-entries') && method === 'GET') {
      return jsonOk([deadLetterEntry(state.deadLetterStatus, state.deadLetterJobStatus)])
    }

    if (url.endsWith('/api/dead-letter-entries/dead-letter-p0') && method === 'GET') {
      return jsonOk(deadLetterEntry(state.deadLetterStatus, state.deadLetterJobStatus))
    }

    if (url.endsWith('/api/dead-letter-entries/dead-letter-p0/retry') && method === 'POST') {
      state.deadLetterStatus = 'RETRIED'
      state.deadLetterJobStatus = 'WAITING_RETRY'
      return jsonOk(deadLetterEntry(state.deadLetterStatus, state.deadLetterJobStatus))
    }

    if (
      url.endsWith('/api/dead-letter-entries/dead-letter-p0/acknowledge') &&
      method === 'POST'
    ) {
      state.deadLetterStatus = 'ACKNOWLEDGED'
      state.deadLetterJobStatus = 'ACKNOWLEDGED'
      return jsonOk(deadLetterEntry(state.deadLetterStatus, state.deadLetterJobStatus))
    }

    if (url.endsWith('/api/model-adapters')) {
      return jsonOk(modelAdapters(options.deepSeekConfigured))
    }

    if (url.endsWith('/api/model-configurations/deepseek')) {
      if (method === 'PUT') {
        state.lastModelConfigurationSave = JSON.parse(String(init?.body ?? '{}'))
        return jsonOk(modelConfiguration('CONFIGURED'))
      }
      if (method === 'DELETE') {
        return jsonOk(modelConfiguration('MISSING'))
      }
      return jsonOk(modelConfiguration(options.deepSeekConfigured ? 'CONFIGURED' : 'MISSING'))
    }

    return jsonOk(null)
  })

  return state
}

function authMe(
  role: 'VIEWER' | 'EDITOR' | 'KNOWLEDGE_MANAGER' | 'SPACE_OWNER' | 'AUDITOR' = 'SPACE_OWNER',
  capabilities = [
    'CONTENT_READ',
    'CONTENT_WRITE',
    'GOVERNANCE_READ',
    'KNOWLEDGE_OPERATE',
    'MEMBER_MANAGE',
    'SETTINGS_MANAGE',
    'SPACE_MANAGE',
    'SPACE_READ'
  ]
) {
  return {
    user: {
      id: 'frontend-demo',
      email: 'frontend-demo@example.test',
      displayName: 'Frontend Demo',
      status: 'ACTIVE',
      globalRoles: []
    },
    activeSpaceId: 'ibm-i-modernization',
    memberships: [
      {
        id: 'membership-frontend-ibmi',
        spaceId: 'ibm-i-modernization',
        spaceName: 'IBM i Modernization',
        role,
        status: 'ACTIVE'
      }
    ],
    capabilities
  }
}

function jsonOk(data: unknown, status = 200) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: async () => ({ success: true, data, error: null, meta: null })
  } as Response)
}

function connectorDefinitions() {
  return [
    {
      id: 'connector-definition-mock-local',
      connectorKey: 'mock-local-fixture',
      name: 'Mock Local Fixture',
      connectorType: 'LOCAL_FIXTURE',
      status: 'AVAILABLE',
      version: 'v0',
      capabilitySummary: 'Local fixture connector only; no external API or credential access.',
      configurationState: 'MOCK_CONFIGURED',
      reviewPolicy: 'REVIEW_REQUIRED'
    }
  ]
}

function connectorRun() {
  return {
    runId: 'connector-run-p0',
    jobId: 'connector-job-p0',
    spaceId: 'ibm-i-modernization',
    connectorKey: 'mock-local-fixture',
    status: 'REVIEW_REQUIRED',
    itemCount: 3,
    reviewRequiredCount: 2,
    failedCount: 1,
    safeMessage: 'Connector sync completed with review-required output.',
    startedAt: '2026-07-07T00:00:00Z',
    completedAt: '2026-07-07T00:00:01Z'
  }
}

function connectorItems() {
  return [
    {
      id: 'connector-item-001',
      runId: 'connector-run-p0',
      externalId: 'fixture:connector-sync:architecture',
      title: 'Connector Sync Architecture',
      itemStatus: 'REVIEW_REQUIRED',
      sourceReference: 'local-fixture://connector-sync/architecture',
      sourceTrace: {
        connectorKey: 'mock-local-fixture',
        sourceReferenceId: 'fixture:connector-sync:architecture',
        section: 'Architecture',
        locator: 'fixtures/connector-sync-v0/architecture.md#adapter-boundary'
      },
      provenance: {
        syncRunId: 'connector-run-p0',
        adapterKey: 'mock-local-fixture',
        adapterVersion: 'v0'
      },
      confidence: 0.93,
      reviewEligible: true,
      safeErrorCategory: 'NONE',
      safeErrorMessage: null,
      outputArtifacts: [
        {
          id: 'connector-artifact-001',
          artifactType: 'MARKDOWN_CANDIDATE',
          reviewStatus: 'REVIEW_REQUIRED',
          title: 'Connector Sync Architecture',
          targetPath: 'generated/connector-sync-v0/architecture.md'
        }
      ],
      discoveredAt: '2026-07-07T00:00:00Z'
    },
    {
      id: 'connector-item-002',
      runId: 'connector-run-p0',
      externalId: 'fixture:connector-sync:operations',
      title: 'Connector Sync Operations',
      itemStatus: 'REVIEW_REQUIRED',
      sourceReference: 'local-fixture://connector-sync/operations',
      sourceTrace: {
        connectorKey: 'mock-local-fixture',
        sourceReferenceId: 'fixture:connector-sync:operations',
        section: 'Operations',
        locator: 'fixtures/connector-sync-v0/operations.md#safe-errors'
      },
      provenance: {
        syncRunId: 'connector-run-p0',
        adapterKey: 'mock-local-fixture',
        adapterVersion: 'v0'
      },
      confidence: 0.91,
      reviewEligible: true,
      safeErrorCategory: 'NONE',
      safeErrorMessage: null,
      outputArtifacts: [
        {
          id: 'connector-artifact-002',
          artifactType: 'MARKDOWN_CANDIDATE',
          reviewStatus: 'REVIEW_REQUIRED',
          title: 'Connector Sync Operations',
          targetPath: 'generated/connector-sync-v0/operations.md'
        }
      ],
      discoveredAt: '2026-07-07T00:00:00Z'
    },
    {
      id: 'connector-item-003',
      runId: 'connector-run-p0',
      externalId: 'fixture:connector-sync:unreadable',
      title: 'Unreadable Fixture',
      itemStatus: 'FAILED',
      sourceReference: 'local-fixture://connector-sync/unreadable',
      sourceTrace: {
        connectorKey: 'mock-local-fixture',
        sourceReferenceId: 'fixture:connector-sync:unreadable',
        section: 'Unreadable',
        locator: 'fixtures/connector-sync-v0/unreadable.md'
      },
      provenance: {
        syncRunId: 'connector-run-p0',
        adapterKey: 'mock-local-fixture',
        adapterVersion: 'v0'
      },
      confidence: 0.21,
      reviewEligible: false,
      safeErrorCategory: 'SOURCE_UNREADABLE',
      safeErrorMessage: 'Unable to read fixture source with redacted endpoint and path.',
      outputArtifacts: [],
      discoveredAt: '2026-07-07T00:00:00Z'
    }
  ]
}

function deadLetterEntry(status = 'OPEN', jobStatus = 'DEAD_LETTERED') {
  const attempt = {
    id: 'attempt-p0',
    workerJobId: 'worker-job-p0',
    attemptNumber: 3,
    status: jobStatus === 'DEAD_LETTERED' ? 'FAILED_TERMINAL' : 'FAILED_RETRYABLE',
    retryable: false,
    safeErrorCode: 'SOURCE_UNREADABLE',
    safeErrorCategory: 'SOURCE_UNREADABLE',
    safeErrorMessage: 'Fixture source could not be read; raw exception was redacted.',
    sourceTrace: {
      spaceId: 'ibm-i-modernization',
      batchId: 'batch-p0',
      sourceReferenceId: 'fixture:worker-retry:unreadable',
      locator: 'fixtures/worker-retry-dead-letter/unreadable.md'
    },
    startedAt: '2026-07-07T00:00:02Z',
    completedAt: '2026-07-07T00:00:03Z'
  }
  const job = {
    id: 'worker-job-p0',
    jobType: 'CONNECTOR_SYNC',
    subjectType: 'connector_sync_item',
    subjectId: 'connector-item-003',
    status: jobStatus,
    attemptCount: 3,
    maxAttempts: 3,
    retryDelaySeconds: jobStatus === 'WAITING_RETRY' ? 30 : null,
    nextRetryAt: jobStatus === 'WAITING_RETRY' ? '2026-07-07T00:00:33Z' : null,
    sourceTrace: attempt.sourceTrace,
    reviewEligible: false,
    safeErrorCode: 'SOURCE_UNREADABLE',
    safeErrorCategory: 'SOURCE_UNREADABLE',
    safeErrorMessage: 'Fixture source could not be read; raw exception was redacted.',
    createdAt: '2026-07-07T00:00:00Z',
    updatedAt: '2026-07-07T00:00:03Z',
    attempts: [attempt]
  }
  return {
    id: 'dead-letter-p0',
    workerJobId: 'worker-job-p0',
    status,
    jobType: 'CONNECTOR_SYNC',
    subjectType: 'connector_sync_item',
    subjectId: 'connector-item-003',
    attemptSummary: '3/3 attempts terminal',
    safeErrorCode: 'SOURCE_UNREADABLE',
    safeErrorCategory: 'SOURCE_UNREADABLE',
    safeErrorMessage: 'Fixture source could not be read; raw exception was redacted.',
    sourceTrace: attempt.sourceTrace,
    reviewEligible: false,
    operatorActionBy: status === 'OPEN' ? null : 'p0-browser',
    operatorActionAt: status === 'OPEN' ? null : '2026-07-07T00:00:04Z',
    createdAt: '2026-07-07T00:00:03Z',
    job,
    attempts: [attempt]
  }
}

function auditEvents() {
  return [
    {
      id: 'audit-auth-denied',
      createdAt: '2026-07-06T00:00:00Z',
      actorUserId: 'mock-viewer',
      actorDisplay: 'Atlas Viewer',
      action: 'AUTH_GOVERNANCE_READ_DENIED',
      category: 'AUTH',
      result: 'DENIED',
      severity: 'SECURITY',
      spaceId: 'ibm-i-modernization',
      targetType: 'api_route',
      targetId: '/api/spaces/ibm-i-modernization/audit-events',
      requestId: 'req-audit-ui',
      safeSummary: 'Access denied for required capability GOVERNANCE_READ.',
      metadata: {
        capability: 'GOVERNANCE_READ',
        httpMethod: 'GET'
      }
    },
    {
      id: 'audit-member-added',
      createdAt: '2026-07-06T00:01:00Z',
      actorUserId: 'mock-owner',
      actorDisplay: 'Atlas Owner',
      action: 'MEMBERSHIP_ADDED',
      category: 'MEMBERSHIP',
      result: 'SUCCEEDED',
      severity: 'NOTICE',
      spaceId: 'ibm-i-modernization',
      targetType: 'space_membership',
      targetId: 'mock-viewer',
      requestId: null,
      safeSummary: 'Space membership added or reactivated.',
      metadata: {
        role: 'VIEWER',
        status: 'ACTIVE'
      }
    }
  ]
}

function space(
  overrides: Partial<{
    id: string
    name: string
    description: string
    owner: string
    status: string
    documentCount: number
    wikiPageCount: number
    reviewCount: number
  }> = {}
) {
  return {
    id: overrides.id ?? 'ibm-i-modernization',
    name: overrides.name ?? 'IBM i Modernization',
    description: overrides.description ?? 'Mock discovery package for modernization planning.',
    type: 'document',
    indexStrategy: 'rag',
    owner: overrides.owner ?? 'Platform Team',
    status: overrides.status ?? 'REVIEW_REQUIRED',
    documentCount: overrides.documentCount ?? 6,
    wikiPageCount: overrides.wikiPageCount ?? 0,
    reviewCount: overrides.reviewCount ?? 1,
    createdAt: '2026-06-01T09:00:00Z',
    updatedAt: '2026-06-20T14:30:00Z'
  }
}

function batch() {
  return {
    id: 'batch-p0',
    spaceId: 'ibm-i-modernization',
    name: 'P0 Browser Batch',
    sourceKind: 'folder',
    owner: 'P0 Browser',
    uploadedAt: '2026-07-03T00:00:00Z',
    metrics: {
      totalFiles: 1,
      pdfConverted: 0,
      markdownGenerated: 1,
      reviewRequired: 1,
      failed: 0,
      unsupported: 0
    }
  }
}

function manualUrlSource(
  overrides: Partial<{
    id: string
    displayUrl: string
    title: string
    fetchIntent: 'METADATA_ONLY' | 'FETCH_LATER'
  }> = {}
) {
  const displayUrl = overrides.displayUrl ?? 'https://example.com/reference/page'
  return {
    id: overrides.id ?? 'url-src-p0',
    spaceId: 'ibm-i-modernization',
    displayUrl,
    host: 'example.com',
    title: overrides.title ?? 'Vendor reference page',
    description: 'Sample-safe manual URL metadata.',
    fetchIntent: overrides.fetchIntent ?? 'FETCH_LATER',
    fetchPolicy: 'NO_FETCH_METADATA_ONLY',
    ingestStatus: 'REVIEW_REQUIRED',
    reviewStatus: 'REVIEW_REQUIRED',
    eligibilityStatus: 'REVIEW_REQUIRED_ONLY',
    confidence: 0.3,
    sourceTrace: `Manual URL metadata: ${displayUrl}`,
    batchId: 'batch-p0',
    fileItemId: 'file-p0',
    createdBy: 'frontend-user',
    createdAt: '2026-07-07T00:00:00Z',
    updatedAt: '2026-07-07T00:00:00Z'
  }
}

function file(reviewStatus: string) {
  return {
    id: 'file-p0',
    batchId: 'batch-p0',
    sourcePath: 'samples/p0/productization.md',
    sourceType: 'pdf',
    status: 'MARKDOWN_GENERATED',
    confidence: 0.93,
    reviewStatus,
    pdfPath: null,
    markdownPath: 'generated/md/productization.md',
    assetsPath: null,
    errorMessage: null
  }
}

function chunk() {
  return {
    id: 'chunk-p0',
    fileItemId: 'file-p0',
    sourceFile: 'productization.md',
    page: 1,
    section: 'P0 Browser Evidence',
    confidence: 0.93,
    reviewStatus: 'APPROVED'
  }
}

function wikiPage() {
  return {
    id: 'wiki-file-p0',
    spaceId: 'ibm-i-modernization',
    folderId: null,
    title: 'P0 Wiki',
    slug: 'p0-wiki',
    pageType: 'SOURCE_SUMMARY',
    markdownPath: 'generated/md/productization.md',
    sourceDocumentIds: ['file-p0'],
    aliases: ['P0 Evidence'],
    sourceRefs: [
      {
        type: 'FILE',
        id: 'file-p0',
        label: 'file-p0',
        locator: 'generated/md/productization.md'
      }
    ],
    chunkRefs: [
      {
        type: 'SOURCE_CHUNK',
        id: 'chunk-p0',
        label: 'source chunk',
        locator: 'page 1'
      }
    ],
    inLinks: [],
    outLinks: ['p0-browser-evidence'],
    version: 1,
    sourceMode: 'PUBLISHED_FILE',
    refreshPolicy: 'MANUAL',
    confidence: 0.93,
    reviewStatus: 'PUBLISHED',
    owner: 'p0-browser',
    lastUpdated: '2026-07-03T00:00:00Z'
  }
}

function wikiIssue() {
  return {
    id: 'wiki-issue-broken-link-001',
    spaceId: 'ibm-i-modernization',
    pageId: 'wiki-file-p0',
    issueType: 'BROKEN_LINK',
    severity: 'MEDIUM',
    status: 'OPEN',
    evidenceRefs: [
      {
        type: 'WIKI_PAGE',
        id: 'wiki-file-p0',
        label: 'p0-wiki',
        locator: null
      }
    ],
    message: 'Wiki link target does not exist in this Knowledge Space.',
    createdAt: '2026-07-05T00:00:00Z',
    resolvedAt: null
  }
}

function graphView() {
  return {
    spaceId: 'ibm-i-modernization',
    nodes: [
      {
        id: 'node-p0',
        label: 'P0 Browser Evidence',
        type: 'CONCEPT',
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        evidenceCount: 1
      }
    ],
    edges: [],
    counts: { nodes: 1, edges: 0, excluded: 0 }
  }
}

function graphDetail() {
  return {
    node: graphView().nodes[0],
    adjacentNodes: [],
    adjacentEdges: [],
    evidenceReferences: [
      {
        referenceType: 'SOURCE_CHUNK',
        sourceChunkId: 'chunk-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: 'P0 Browser Evidence',
        confidence: 0.93,
        reviewStatus: 'APPROVED'
      },
      {
        referenceType: 'WIKI_PAGE',
        wikiPageId: 'wiki-p0',
        label: 'P0 Browser Evidence',
        section: 'p0-browser-evidence',
        confidence: 0.93,
        reviewStatus: 'PUBLISHED'
      }
    ]
  }
}

function askRun() {
  return {
    runId: 'ask-p0',
    sessionId: 'ask-session-p0',
    sessionTitle: 'P0 browser evidence',
    spaceId: 'ibm-i-modernization',
    question: 'What evidence was published?',
    status: 'SUCCEEDED',
    reviewPolicy: 'APPROVED_ONLY',
    mode: 'mock',
    requestedBy: 'p0-browser',
    answer: 'Mock chat summary for the referenced Atlas evidence.',
    answerConfidence: 0.82,
    answerReviewStatus: 'REVIEW_REQUIRED',
    answerReviewLabel: 'Review required',
    answerReviewReason: null,
    answerReviewedBy: null,
    answerReviewedAt: null,
    answerReusable: false,
    modelRunId: 'model-run-p0',
    safeMessage: 'Trusted ask completed.',
    evidence: [
      {
        evidenceId: 'ask-ev-p0',
        citationId: 'ask-cite-p0',
        sourceChunkId: 'chunk-p0',
        fileItemId: 'file-p0',
        sourceFile: 'productization.md',
        page: 1,
        section: 'P0 Browser Evidence',
        reviewStatus: 'APPROVED',
        confidence: 0.93,
        vectorItemKey: 'ibm-i-modernization/chunk-p0',
        score: 0.91,
        evidenceLabel: 'productization.md page 1',
        sourceLocator: 'page 1 / P0 Browser Evidence / chunk chunk-p0',
        citationStatus: 'ELIGIBLE',
        reviewEligible: true,
        excludedReason: null,
        createdAt: '2026-07-03T00:00:00Z'
      }
    ],
    createdAt: '2026-07-03T00:00:00Z',
    completedAt: '2026-07-03T00:00:01Z'
  }
}

function askQualityMetrics() {
  return {
    runId: 'ask-p0',
    spaceId: 'ibm-i-modernization',
    status: 'SUCCEEDED',
    evidenceCoverage: {
      evidenceCount: 1,
      citedEvidenceCount: 1,
      coverageRatio: 1,
      missingEvidence: false
    },
    citationHealth: {
      evidenceCount: 1,
      healthyCitationCount: 1,
      uncitedEvidenceCount: 0,
      healthRatio: 1,
      unhealthy: false
    },
    confidence: {
      averageEvidenceConfidence: 0.93,
      answerConfidence: 0.82,
      band: 'MEDIUM'
    },
    reviewEligibility: {
      reviewEligible: true,
      status: 'ELIGIBLE',
      reasons: []
    },
    noEvidenceRefusal: false,
    safeDiagnostics: []
  }
}

function askSessions() {
  return [
    {
      sessionId: 'ask-session-p0',
      spaceId: 'ibm-i-modernization',
      title: 'P0 browser evidence',
      createdBy: 'p0-browser',
      runCount: 1,
      latestStatus: 'SUCCEEDED',
      latestAnswerReviewStatus: 'REVIEW_REQUIRED',
      createdAt: '2026-07-03T00:00:00Z',
      updatedAt: '2026-07-03T00:00:01Z'
    }
  ]
}

function askSessionDetail() {
  return {
    ...askSessions()[0],
    runs: [askRun()]
  }
}

function modelAdapters(deepSeekConfigured = false) {
  const adapters = [
    {
      adapterKey: 'mock-model',
      modelKey: 'deepseek-flash',
      displayName: 'DeepSeek Flash',
      providerFamily: 'built-in mock',
      modelType: 'CHAT',
      supportedOperations: ['CHAT'],
      defaultModel: true,
      status: 'AVAILABLE',
      contextLimit: 8192,
      maskedConfigSummary: {
        credential: 'mock',
        endpoint: 'not_configured',
        externalNetwork: 'disabled'
      },
      secretStatuses: [
        secretStatus('built-in mock', 'model-adapter', 'credential', 'NOT_REQUIRED', 'mock')
      ]
    }
  ]
  if (deepSeekConfigured) {
    return [
      {
        adapterKey: 'deepseek',
        modelKey: 'deepseek-chat',
        displayName: 'DeepSeek Chat',
        providerFamily: 'deepseek',
        modelType: 'CHAT',
        supportedOperations: ['CHAT'],
        defaultModel: true,
        status: 'AVAILABLE',
        contextLimit: 64000,
        maskedConfigSummary: {
          credential: 'configured',
          endpoint: 'configured',
          externalNetwork: 'enabled'
        },
        secretStatuses: [
          secretStatus('deepseek', 'model-configuration', 'credential', 'CONFIGURED', 'runtime'),
          secretStatus('deepseek', 'model-configuration', 'endpoint', 'CONFIGURED', 'runtime')
        ]
      },
      ...adapters
    ]
  }
  return adapters
}

function modelConfiguration(credentialStatus = 'MISSING') {
  return {
    adapterKey: 'deepseek',
    provider: 'deepseek',
    modelKey: 'deepseek-chat',
    credentialStatus,
    endpointStatus: 'CONFIGURED',
    mode: credentialStatus === 'CONFIGURED' ? 'runtime' : 'missing',
    maskedConfigSummary: {
      provider: 'deepseek',
      credential: credentialStatus.toLowerCase().replace('_', '-'),
      endpoint: 'configured',
      externalNetwork: credentialStatus === 'MISSING' ? 'disabled' : 'enabled'
    },
    secretStatuses: [
      secretStatus(
        'deepseek',
        'model-configuration',
        'credential',
        credentialStatus === 'ENV_CONFIGURED' ? 'ENV_CONFIGURED' : credentialStatus,
        credentialStatus === 'MISSING' ? 'none' : 'runtime'
      ),
      secretStatus('deepseek', 'model-configuration', 'endpoint', 'CONFIGURED', 'runtime')
    ]
  }
}

function secretStatus(
  provider: string,
  scope: string,
  key: string,
  status: string,
  source: string
) {
  return {
    reference: {
      provider,
      scope,
      key,
      displayName: key === 'credential' ? 'Credential' : 'Endpoint'
    },
    status,
    source,
    maskedLabel: status === 'MISSING' ? 'Missing' : 'Configured',
    replaceable: true,
    removable: status !== 'MISSING'
  }
}

async function flushAsync() {
  for (let index = 0; index < 6; index++) {
    await new Promise(resolve => setTimeout(resolve, 0))
  }
}

import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase H real Vue settings administers models and adapter panels safely', async ({ page }) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  await page.goto('/')

  await page.getByRole('button', { name: /全部设置/ }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('常规设置')
  await expect(page.getByTestId('vue-general-language')).toHaveValue('zh-CN')
  await expect(page.getByTestId('vue-general-theme')).toHaveValue('light')
  await expect(page.getByTestId('vue-general-interface-font')).toHaveValue('system')
  await expect(page.getByTestId('vue-general-code-font')).toHaveValue('system-mono')
  await page.getByTestId('vue-general-font-size-large').click()
  await expect(page.getByTestId('vue-general-font-size-large')).toHaveClass(/active/)
  await page.getByTestId('vue-general-memory').click()
  await expect(page.getByTestId('vue-general-memory')).toHaveAttribute('aria-checked', 'false')
  await expect(page.locator('.atlas-general-boundary')).toContainText('不执行生产 RBAC')

  await page.getByRole('button', { name: '用户信息' }).click()
  await expect(page.getByTestId('vue-user-info-panel')).toContainText('用户信息')
  await expect(page.getByTestId('vue-user-info-panel')).toContainText('用户 ID')
  await expect(page.getByTestId('vue-user-info-id')).toContainText('atlas-demo-user-001')
  await expect(page.getByTestId('vue-user-info-name')).toContainText('leo')
  await expect(page.getByTestId('vue-user-info-email')).toContainText('leo@example.com')
  await expect(page.getByTestId('vue-user-info-registered')).toContainText('2026/06/23 13:19')

  await page.getByRole('dialog').getByRole('button', { name: '空间信息', exact: true }).click()
  await expect(page.getByTestId('vue-space-info-panel')).toContainText('空间信息')
  await expect(page.getByTestId('vue-space-info-id')).toContainText('ibm-i-modernization')
  await expect(page.getByTestId('vue-space-info-name')).toContainText('IBM i Modernization')
  await expect(page.getByTestId('vue-space-info-status')).toContainText('REVIEW_REQUIRED')
  await expect(page.getByTestId('vue-space-info-storageQuota')).toContainText('10 GB')
  await expect(page.getByTestId('vue-space-info-storageUsed')).toContainText('81.83 MB')
  await page.getByTestId('vue-space-info-edit-name').click()
  await page.getByTestId('vue-space-info-name-input').fill('IBM i Modernization Workspace')
  await page.getByTestId('vue-space-info-save').click()
  await expect(page.getByTestId('vue-space-info-save-status')).toContainText('空间信息已更新')
  await expect(page.getByTestId('vue-space-info-name')).toContainText(
    'IBM i Modernization Workspace'
  )

  await page.getByRole('dialog').getByRole('button', { name: '成员管理', exact: true }).click()
  await expect(page.getByTestId('vue-member-manager')).toContainText('待接受的邀请')
  await expect(page.getByTestId('vue-member-manager')).toContainText('暂无待接受的邀请')
  await expect(page.getByTestId('vue-member-manager')).toContainText('空间成员')
  await expect(page.getByTestId('vue-member-manager')).toContainText('Atlas Admin')
  await page.getByTestId('vue-member-search').fill('admin@example.com')
  await expect(page.getByTestId('vue-member-manager')).toContainText('Atlas Admin')
  await page.getByLabel('Atlas Admin 角色').selectOption('reviewer')
  await expect(page.getByTestId('vue-member-status')).toContainText('审核者')
  await page.getByTestId('vue-member-invite').click()
  await expect(page.getByTestId('vue-member-status')).toContainText('不发送邮件')

  await page.getByRole('button', { name: '注册配置' }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('注册策略')
  await expect(page.locator('.atlas-settings-placeholder')).toContainText('不提交真实公司域名')

  await page.getByRole('button', { name: 'API 信息' }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('API 信息')
  await expect(page.getByTestId('vue-api-info-panel')).toContainText(
    '查看和管理 Atlas API 调用信息'
  )
  await expect(page.getByTestId('vue-api-key-value')).toHaveValue(/•{32}/)
  await page.getByTestId('vue-api-key-reveal').click()
  await expect(page.getByTestId('vue-api-key-value')).toHaveValue(/•{32}/)
  await expect(page.getByTestId('vue-api-info-status')).toContainText('API Key 保持隐藏')
  await page.getByTestId('vue-api-key-copy').click()
  await expect(page.getByTestId('vue-api-info-status')).toContainText('API Key 已复制')
  await page.getByTestId('vue-api-key-refresh').click()
  await expect(page.getByTestId('vue-api-key-value')).toHaveValue(/•{32}/)
  await expect(page.getByTestId('vue-api-info-status')).toContainText('API Key 已刷新')
  await page.getByTestId('vue-api-base-copy').click()
  await expect(page.getByTestId('vue-api-info-status')).toContainText('API 地址已复制')
  await page.getByTestId('vue-api-doc-link').click()
  await expect(page.getByTestId('vue-api-info-status')).toContainText('API 文档入口已准备')

  await page.getByRole('button', { name: '消息管理' }).click()
  await expect(page.getByTestId('vue-message-management')).toContainText('消息管理')
  await expect(page.getByTestId('vue-message-index-toggle')).toHaveAttribute(
    'aria-checked',
    'false'
  )
  await expect(page.getByTestId('vue-message-management')).toContainText('消息索引未配置')
  await page.getByTestId('vue-message-index-toggle').click()
  await expect(page.getByTestId('vue-message-index-toggle')).toHaveAttribute('aria-checked', 'true')
  await expect(page.getByTestId('vue-message-management')).toContainText('text-embedding-v4')
  await expect(page.getByTestId('vue-message-index-stats')).toContainText('已索引消息')

  await page.getByRole('dialog').getByRole('button', { name: '模型管理', exact: true }).click()
  await expect(page.getByTestId('vue-model-manager')).toContainText('模型配置')
  await page.getByTestId('vue-model-card-deepseek-flash').click()
  await expect(page.getByTestId('vue-model-editor')).toContainText('编辑模型')
  await page.getByTestId('vue-model-display-name').fill('Phase H Chat Model')
  await page.getByTestId('vue-key-replace').click()
  await page.getByTestId('vue-key-input').fill('phase-h-key-not-persisted')
  await page.getByTestId('vue-key-confirm').click()
  await page.getByTestId('vue-test-model').click()
  await expect(page.getByTestId('vue-model-test-status')).toContainText(
    'Mock connection passed with masked credentials'
  )
  await page.getByRole('button', { name: '取消' }).click()
  await expect(page.getByTestId('vue-model-manager')).not.toContainText('Phase H Chat Model')

  await page.getByTestId('vue-model-card-deepseek-flash').click()
  await page.getByTestId('vue-model-display-name').fill('Phase H Saved Model')
  await page.getByTestId('vue-save-model').click()
  await expect(page.getByTestId('vue-model-manager')).toContainText('Phase H Saved Model')
  await expect(page.locator('body')).not.toContainText('phase-h-key-not-persisted')

  await page.screenshot({
    path: '../docs/00-context/evidence/phase-h-settings-administration.png',
    fullPage: true
  })
})

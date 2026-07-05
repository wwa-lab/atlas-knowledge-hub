import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase H real Vue settings administers models and adapter panels safely', async ({ page }) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  await page.goto('/')

  await page.getByRole('button', { name: /全部设置/ }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('常规设置')
  await expect(page.locator('.atlas-settings-placeholder')).toContainText('不执行生产 RBAC')

  await page.getByRole('button', { name: '注册配置' }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('注册策略')
  await expect(page.locator('.atlas-settings-placeholder')).toContainText('不提交真实公司域名')

  await page.getByRole('button', { name: 'API 信息' }).click()
  await expect(page.getByTestId('vue-admin-panel')).toContainText('API 信息')
  await expect(page.locator('.atlas-settings-placeholder')).toContainText('不显示 token')

  await page.getByRole('dialog').getByRole('button', { name: '模型管理', exact: true }).click()
  await expect(page.getByTestId('vue-model-manager')).toContainText('模型配置')
  await page.getByTestId('vue-add-model').click()
  await page.getByTestId('vue-add-chat').click()
  await expect(page.getByTestId('vue-model-editor')).toContainText('编辑模型')
  await page.getByTestId('vue-model-display-name').fill('Phase H Chat Model')
  await page.getByTestId('vue-key-replace').click()
  await page.getByTestId('vue-key-input').fill('phase-h-key-not-persisted')
  await page.getByTestId('vue-key-confirm').click()
  await page.getByTestId('vue-test-model').click()
  await expect(page.getByTestId('vue-model-test-status')).toContainText('Mock connection passed')
  await page.getByRole('button', { name: '取消' }).click()
  await expect(page.getByTestId('vue-model-manager')).not.toContainText('Phase H Chat Model')

  await page.getByTestId('vue-add-model').click()
  await page.getByTestId('vue-add-chat').click()
  await page.getByTestId('vue-model-display-name').fill('Phase H Saved Model')
  await page.getByTestId('vue-save-model').click()
  await expect(page.getByTestId('vue-model-manager')).toContainText('Phase H Saved Model')
  await expect(page.locator('body')).not.toContainText('phase-h-key-not-persisted')

  await page.screenshot({
    path: '../docs/00-context/evidence/phase-h-settings-administration.png',
    fullPage: true
  })
})

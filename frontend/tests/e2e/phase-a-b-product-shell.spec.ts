import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase A and B real Vue product path opens home, chat, settings, and space detail', async ({
  page
}) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  await page.goto('/')

  await expect(page.getByTestId('vue-product-page')).toBeVisible()
  await expect(page.locator('iframe.product-frame')).toHaveCount(0)
  await expect(page.getByRole('heading', { name: '知识库' })).toBeVisible()
  await expect(page.getByTestId('vue-space-card-ibm-i-modernization')).toContainText(
    'IBM i Modernization'
  )
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-a-product-home.png',
    fullPage: true
  })

  await page.getByRole('button', { name: /对话/ }).click()
  await expect(page.getByTestId('vue-global-chat')).toContainText('知识库(2)')
  await page.getByRole('button', { name: /Legacy Spec Factory/ }).click()
  await expect(page.getByTestId('vue-global-chat')).toContainText('知识库(3)')

  await page.getByRole('button', { name: /模型管理/ }).click()
  await expect(page.getByTestId('vue-model-manager')).toContainText('模型配置')
  await page.getByRole('button', { name: '×' }).click()

  await page.getByRole('button', { name: /知识库/ }).first().click()
  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-space-detail')).toContainText('IBM i Modernization Index')

  const spaceTabs = page.getByRole('navigation', { name: 'Knowledge Space tabs' })
  for (const tabName of ['文档', '处理中心', 'Wiki', '图谱']) {
    await spaceTabs.getByRole('button', { name: tabName, exact: true }).click()
    await expect(spaceTabs.getByRole('button', { name: tabName, exact: true })).toHaveClass(
      /active/
    )
  }

  await expect(page.getByTestId('vue-space-detail')).toContainText('source trace')
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-b-space-detail.png',
    fullPage: true
  })
})

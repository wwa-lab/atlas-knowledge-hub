import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('P0 shell exposes API-driven workspace and keeps prototype out of the primary path', async ({
  page
}) => {
  await mockP0Api(page)
  await page.goto('/')

  await expect(page.getByRole('heading', { name: '知识库' })).toBeVisible()
  await expect(page.getByTestId('vue-api-space-status')).toContainText('API-backed metadata')
  await expect(page.getByTestId('vue-space-card-ibm-i-modernization')).toContainText(
    'IBM i Modernization'
  )
  await expect(page.locator('iframe')).toHaveCount(0)

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-space-head')).toContainText('API-backed Space')
  await page.getByRole('button', { name: '文档' }).click()
  await expect(page.getByTestId('vue-api-create-batch')).toBeEnabled()
  await page.getByRole('button', { name: '处理中心' }).click()
  await expect(page.getByTestId('vue-api-approve-file')).toBeDisabled()
})

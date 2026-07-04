import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('P0 shell exposes API-driven workspace and disables unconnected prototype actions', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'Knowledge Workspace' })).toBeVisible()
  await expect(page.getByTestId('space-list')).toContainText('IBM i Modernization')
  await expect(page.getByTestId('space-detail')).toContainText('Platform Team')
  await expect(page.locator('iframe')).toHaveCount(0)

  await expect(page.getByTestId('coming-soon')).toHaveCount(3)
  await expect(page.getByText('Production file upload coming soon')).toBeDisabled()
  await expect(page.getByText('Production auth and member admin coming soon')).toBeDisabled()
  await expect(page.getByText('Real provider setup coming soon')).toBeDisabled()

  await expect(page.getByTestId('create-sample-batch')).toBeEnabled()
  await expect(page.getByTestId('approve-file')).toBeDisabled()
  await expect(page.getByTestId('publish-file')).toBeDisabled()
})

import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('metadata-only sample upload creates a real API batch and source trace view', async ({ page }) => {
  await mockP0Api(page)
  await page.goto('/')

  await expect(page.getByTestId('coming-soon').first()).toBeDisabled()
  await expect(page.getByTestId('coming-soon')).toContainText([
    'Production file upload coming soon',
    'Production auth and member admin coming soon',
    'Real provider setup coming soon'
  ])

  await page.getByTestId('create-sample-batch').click()
  await expect(page.getByTestId('batch-list')).toContainText('P0 Browser Batch')
  await expect(page.getByTestId('file-list')).toContainText('samples/p0/productization.md')
  await expect(page.getByTestId('chunk-list')).toContainText('productization.md')
  await expect(page.getByTestId('chunk-list')).toContainText('confidence 0.93')
})

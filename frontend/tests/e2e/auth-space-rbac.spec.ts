import { expect, test } from '@playwright/test'
import { knowledgeManagerUser, mockP0Api, ownerUser, viewerUser } from './p0-api-mock'

test('viewer role sees disabled writes and manipulated writes receive safe forbidden', async ({
  page
}) => {
  await mockP0Api(page, { currentUser: viewerUser() })

  await page.goto('/')

  await expect(page.getByTestId('vue-create-space-open')).toBeDisabled()

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档', exact: true }).click()

  await expect(page.getByTestId('vue-api-create-batch')).toBeDisabled()
  await expect(page.getByTestId('vue-api-upload-documents')).toBeDisabled()

  const response = await page.evaluate(async () => {
    const result = await fetch('/api/spaces/ibm-i-modernization/batches', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Atlas-User': 'mock-viewer'
      },
      body: JSON.stringify({
        name: 'Manipulated viewer write',
        sourceKind: 'folder',
        owner: 'viewer',
        files: []
      })
    })
    return { status: result.status, body: await result.json() }
  })

  expect(response.status).toBe(403)
  expect(response.body.error.code).toBe('FORBIDDEN')
  expect(JSON.stringify(response.body)).not.toContain('sourcePath')
})

test('knowledge manager can run knowledge workflow but not space administration', async ({
  page
}) => {
  await mockP0Api(page, { currentUser: knowledgeManagerUser() })

  await page.goto('/')

  await expect(page.getByTestId('vue-create-space-open')).toBeDisabled()

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档', exact: true }).click()

  await expect(page.getByTestId('vue-api-create-batch')).toBeEnabled()
  await page.getByTestId('vue-api-create-batch').click()
  await page.getByRole('button', { name: '处理中心', exact: true }).click()
  await expect(page.getByTestId('vue-api-approve-file')).toBeEnabled()
})

test('space owner can administer spaces and create content batches', async ({ page }) => {
  await mockP0Api(page, { currentUser: ownerUser() })

  await page.goto('/')

  await expect(page.getByTestId('vue-create-space-open')).toBeEnabled()

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档', exact: true }).click()
  await expect(page.getByTestId('vue-api-create-batch')).toBeEnabled()
})

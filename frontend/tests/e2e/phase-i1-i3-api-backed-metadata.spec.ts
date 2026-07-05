import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase I1-I3 real Vue product surfaces use Atlas API metadata and review queues', async ({
  page
}) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  const apiRequests: string[] = []
  page.on('request', request => {
    const url = new URL(request.url())
    if (url.pathname.startsWith('/api/')) {
      apiRequests.push(`${request.method()} ${url.pathname}`)
    }
  })

  await page.goto('/')
  await expect(page.getByTestId('vue-api-space-status')).toContainText('API-backed metadata')
  await expect(page.getByTestId('vue-space-card-ibm-i-modernization')).toContainText('API')

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await expect(page.getByTestId('vue-space-head')).toContainText('API-backed Space')

  await page.getByRole('button', { name: '文档' }).click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('Batches 0')
  await page.getByTestId('vue-api-create-batch').click()
  await expect(page.getByTestId('vue-api-metadata')).toContainText('P0 Browser Batch')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('samples/p0/productization.md')
  await expect(page.getByTestId('vue-api-metadata')).toContainText('chunk-p0')

  await page.getByRole('button', { name: '处理中心' }).click()
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('API review queues')
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('READY TO PUBLISH')
  await expect(page.getByTestId('vue-api-review-queues')).toContainText('LOW CONFIDENCE')

  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/spaces']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/spaces/ibm-i-modernization']))
  expect(apiRequests).toEqual(
    expect.arrayContaining(['GET /api/spaces/ibm-i-modernization/batches'])
  )
  expect(apiRequests).toEqual(expect.arrayContaining(['POST /api/spaces/ibm-i-modernization/batches']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/batches/batch-p0/files']))
  expect(apiRequests).toEqual(expect.arrayContaining(['GET /api/files/file-p0/chunks']))
  expect(apiRequests).toEqual(
    expect.arrayContaining(['GET /api/spaces/ibm-i-modernization/review-queues'])
  )

  await page.screenshot({
    path: '../docs/00-context/evidence/phase-i1-i3-api-backed-metadata.png',
    fullPage: true
  })
})

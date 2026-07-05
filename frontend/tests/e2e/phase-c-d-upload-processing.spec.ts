import { expect, test } from '@playwright/test'
import { mockP0Api } from './p0-api-mock'

test('Phase C and D Vue product path creates mock batch and explains processing gates', async ({
  page
}) => {
  await mockP0Api(page)
  await page.setViewportSize({ width: 1440, height: 960 })
  await page.goto('/')

  await page.getByTestId('vue-space-card-ibm-i-modernization').click()
  await page.getByRole('button', { name: '文档' }).click()

  await page.getByTestId('vue-upload-folder').click()
  await expect(page.getByTestId('vue-upload-review')).toContainText('Upload Review')
  await expect(page.getByTestId('vue-inventory-row')).toHaveCount(7)
  await expect(page.getByTestId('vue-upload-review')).toContainText('UNSUPPORTED')

  await page.getByTestId('vue-create-batch').click()
  await expect(page.getByTestId('vue-batch-summary')).toContainText('Batch created')
  await expect(page.getByTestId('vue-batch-summary')).toContainText('PDF_CONVERT_FAILED')
  await expect(page.getByTestId('vue-batch-summary')).toContainText('OCR_REQUIRED')
  await expect(page.getByTestId('vue-batch-summary')).toContainText('LOW_CONFIDENCE')
  await expect(page.getByTestId('vue-batch-summary')).toContainText('PUBLISHED')

  await page.getByTestId('vue-view-report').click()
  await expect(page.getByTestId('vue-batch-report')).toContainText('Batch Report')
  await expect(page.getByTestId('vue-batch-report')).toContainText('source_trace:')
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-c-upload-batch.png',
    fullPage: true
  })

  await page.getByRole('button', { name: '处理中心' }).click()
  await expect(page.getByTestId('vue-processing-center')).toContainText('parse failures')
  await expect(page.getByTestId('vue-processing-center')).toContainText('OCR required')
  await expect(page.getByTestId('vue-processing-issue')).toContainText([
    '解析失败',
    '需要 OCR',
    '低置信度',
    '缺少 source_trace',
    'LLM 生成需审核',
    '待发布'
  ])
  await expect(page.getByTestId('vue-processing-issue').first()).toContainText(
    'blocks Wiki / Graph / Ask'
  )
  await page.screenshot({
    path: '../docs/00-context/evidence/phase-d-processing-center.png',
    fullPage: true
  })
})

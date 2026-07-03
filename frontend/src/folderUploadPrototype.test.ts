import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { JSDOM } from 'jsdom'
import type { FolderUploadProvider } from './types'

function loadPrototypeProvider() {
  const currentDir = dirname(fileURLToPath(import.meta.url))
  const html = readFileSync(resolve(currentDir, '../public/atlas-prototype.html'), 'utf8')
  const dom = new JSDOM(html, {
    runScripts: 'dangerously',
    url: 'http://atlas.local/'
  })
  const provider = (
    dom.window as unknown as {
      __atlasFolderUploadProvider: FolderUploadProvider
    }
  ).__atlasFolderUploadProvider

  return { dom, provider }
}

describe('folder upload prototype provider', () => {
  it('creates deterministic inventory, derived metrics, and populated report sections', async () => {
    const { dom, provider } = loadPrototypeProvider()

    const inventory = await provider.createInventory('folder')
    expect(inventory.files.some(file => file.supported)).toBe(true)
    expect(inventory.files.some(file => !file.supported)).toBe(true)

    const batch = await provider.createBatch(inventory)
    expect(batch.metrics.totalFiles).toBe(batch.fileItems.length)
    expect(batch.metrics.unsupported).toBe(
      batch.fileItems.filter(file => file.status === 'UNSUPPORTED').length
    )
    expect(batch.metrics.failed).toBe(
      batch.fileItems.filter(file => ['PDF_CONVERT_FAILED', 'FAILED'].includes(file.status)).length
    )

    const report = await provider.buildReport(batch)
    expect(report.inventory.length).toBe(batch.fileItems.length)
    expect(report.unsupported.length).toBeGreaterThan(0)
    expect(report.conversionFailures.length).toBeGreaterThan(0)
    expect(report.lowConfidence.length).toBeGreaterThan(0)
    expect(report.reviewRequired.length).toBeGreaterThan(0)

    dom.window.close()
  })
})

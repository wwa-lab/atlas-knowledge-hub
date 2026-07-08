import { describe, expect, it, vi } from 'vitest'
import { mockFolderInventory, useProductUploadWorkflow } from './useProductUploadWorkflow'

describe('useProductUploadWorkflow', () => {
  it('keeps seeded upload metrics and report sections deterministic', () => {
    const workflow = useProductUploadWorkflow()

    expect(workflow.visibleProductBatchFiles.value).toHaveLength(7)
    expect(workflow.productBatchMetrics.value).toMatchObject({
      total: 7,
      supported: 6,
      unsupported: 1,
      failed: 1,
      ocr: 1,
      lowConfidence: 1,
      reviewRequired: 4,
      approved: 1,
      published: 1
    })
    expect(workflow.productProcessingIssues.value).toContainEqual(
      expect.objectContaining({
        id: 'missing-trace',
        count: 2,
        status: 'blocks trusted Ask'
      })
    )
    expect(workflow.reportSections.value.map(section => section.label)).toEqual([
      'Inventory',
      'Unsupported',
      'Conversion failures',
      'OCR required',
      'Low confidence',
      'Review required'
    ])
  })

  it('opens, copies, and creates a mock upload batch without mutating seed data', () => {
    const showDocumentsTab = vi.fn()
    const workflow = useProductUploadWorkflow({ showDocumentsTab })

    workflow.openProductReport()
    workflow.openMockUpload('zip')

    expect(showDocumentsTab).toHaveBeenCalledTimes(1)
    expect(workflow.isProductReportOpen.value).toBe(false)
    expect(workflow.uploadSession.value?.packageName).toBe('ibm-i-modernization-evidence.zip')
    expect(workflow.uploadSession.value?.files).toHaveLength(mockFolderInventory.length)
    workflow.uploadSession.value!.files[0].path = 'changed.md'
    expect(mockFolderInventory[0].path).toBe('discovery/BRD_Methodology.pdf')

    workflow.createProductBatch()

    expect(workflow.uploadSession.value).toBeNull()
    expect(workflow.activeProductBatchFiles.value[0].path).toBe('changed.md')
    expect(workflow.visibleProductBatchFiles.value[0].path).toBe('changed.md')
    workflow.openProductReport()
    expect(workflow.isProductReportOpen.value).toBe(true)
    workflow.closeProductReport()
    expect(workflow.isProductReportOpen.value).toBe(false)
  })
})

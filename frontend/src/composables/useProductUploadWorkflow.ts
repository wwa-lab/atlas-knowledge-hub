import { computed, ref } from 'vue'
import type { MockInventoryFile, MockUploadKind, MockUploadSession } from '@/domain/viewModels'

export interface ProductUploadWorkflowOptions {
  showDocumentsTab?: () => void
}

export const mockFolderInventory: MockInventoryFile[] = [
  {
    id: 'mock-brd',
    path: 'discovery/BRD_Methodology.pdf',
    type: 'PDF',
    size: '4.8 MB',
    supported: true,
    status: 'REVIEW_REQUIRED',
    confidence: 0.82,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'BRD_Methodology.pdf / page 12 / chunk brd-012'
  },
  {
    id: 'mock-rpg',
    path: 'analysis/RPG_Scan_Result.xlsx',
    type: 'XLSX',
    size: '2.1 MB',
    supported: true,
    status: 'LOW_CONFIDENCE',
    confidence: 0.67,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'RPG_Scan_Result.xlsx / sheet Programs / row 42'
  },
  {
    id: 'mock-nightly',
    path: 'jobs/Nightly_Batch.docx',
    type: 'DOCX',
    size: '920 KB',
    supported: true,
    status: 'PDF_CONVERT_FAILED',
    confidence: 0.58,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'Nightly_Batch.docx / section 3',
    reason: 'Office conversion failed safely in mock status.'
  },
  {
    id: 'mock-ocr',
    path: 'screenshots/green-screen-flow.png',
    type: 'PNG',
    size: '1.4 MB',
    supported: true,
    status: 'OCR_REQUIRED',
    confidence: 0.61,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'green-screen-flow.png / image region 2',
    reason: 'Image-heavy source needs OCR before publication.'
  },
  {
    id: 'mock-approved',
    path: 'approved/current-state-summary.md',
    type: 'MD',
    size: '48 KB',
    supported: true,
    status: 'APPROVED',
    confidence: 0.93,
    reviewStatus: 'APPROVED',
    sourceTrace: 'current-state-summary.md / section architecture'
  },
  {
    id: 'mock-published',
    path: 'published/modernization-index.md',
    type: 'MD',
    size: '64 KB',
    supported: true,
    status: 'PUBLISHED',
    confidence: 0.96,
    reviewStatus: 'PUBLISHED',
    sourceTrace: 'modernization-index.md / section index'
  },
  {
    id: 'mock-unsupported',
    path: 'raw/archive/old-export.exe',
    type: 'EXE',
    size: '12 MB',
    supported: false,
    status: 'UNSUPPORTED',
    confidence: 0,
    reviewStatus: 'REVIEW_REQUIRED',
    sourceTrace: 'not generated',
    reason: 'Executable files are excluded from the mock parser pipeline.'
  }
]

export function useProductUploadWorkflow(options: ProductUploadWorkflowOptions = {}) {
  const uploadSession = ref<MockUploadSession | null>(null)
  const activeProductBatchFiles = ref<MockInventoryFile[]>([])
  const isProductReportOpen = ref(false)

  const visibleProductBatchFiles = computed(() =>
    activeProductBatchFiles.value.length > 0 ? activeProductBatchFiles.value : mockFolderInventory
  )

  const productBatchMetrics = computed(() => {
    const files = visibleProductBatchFiles.value
    return {
      total: files.length,
      supported: files.filter(file => file.supported).length,
      unsupported: files.filter(file => file.status === 'UNSUPPORTED').length,
      failed: files.filter(file => file.status === 'PDF_CONVERT_FAILED').length,
      ocr: files.filter(file => file.status === 'OCR_REQUIRED').length,
      lowConfidence: files.filter(file => file.status === 'LOW_CONFIDENCE').length,
      reviewRequired: files.filter(
        file => file.reviewStatus === 'REVIEW_REQUIRED' && file.supported
      ).length,
      approved: files.filter(file => file.status === 'APPROVED').length,
      published: files.filter(file => file.status === 'PUBLISHED').length
    }
  })

  const productProcessingIssues = computed(() => [
    {
      id: 'parse-failures',
      label: '解析失败',
      type: 'PDF_CONVERT_FAILED',
      count: productBatchMetrics.value.failed,
      status: 'blocks Wiki / Graph / Ask',
      action: '重试转换',
      source: 'converter stage'
    },
    {
      id: 'ocr-required',
      label: '需要 OCR',
      type: 'OCR_REQUIRED',
      count: productBatchMetrics.value.ocr,
      status: 'excluded until OCR',
      action: '加入 OCR 队列',
      source: 'image-heavy sources'
    },
    {
      id: 'low-confidence',
      label: '低置信度',
      type: 'LOW_CONFIDENCE',
      count: productBatchMetrics.value.lowConfidence,
      status: 'SME review required',
      action: '打开审核',
      source: 'parser confidence'
    },
    {
      id: 'missing-trace',
      label: '缺少 source_trace',
      type: 'MISSING_SOURCE_TRACE',
      count: 2,
      status: 'blocks trusted Ask',
      action: '修复溯源',
      source: 'trace validator'
    },
    {
      id: 'llm-review',
      label: 'LLM 生成需审核',
      type: 'LLM_GENERATED',
      count: productBatchMetrics.value.reviewRequired,
      status: 'review required',
      action: '优先处理',
      source: 'normalization'
    },
    {
      id: 'ready-to-publish',
      label: '待发布',
      type: 'READY_TO_PUBLISH',
      count: productBatchMetrics.value.approved,
      status: 'eligible',
      action: '发布 Wiki',
      source: 'SME approved'
    }
  ])

  const reportSections = computed(() => [
    { label: 'Inventory', files: visibleProductBatchFiles.value },
    { label: 'Unsupported', files: visibleProductBatchFiles.value.filter(file => !file.supported) },
    {
      label: 'Conversion failures',
      files: visibleProductBatchFiles.value.filter(file => file.status === 'PDF_CONVERT_FAILED')
    },
    {
      label: 'OCR required',
      files: visibleProductBatchFiles.value.filter(file => file.status === 'OCR_REQUIRED')
    },
    {
      label: 'Low confidence',
      files: visibleProductBatchFiles.value.filter(file => file.status === 'LOW_CONFIDENCE')
    },
    {
      label: 'Review required',
      files: visibleProductBatchFiles.value.filter(
        file => file.reviewStatus === 'REVIEW_REQUIRED' && file.supported
      )
    }
  ])

  function openMockUpload(kind: MockUploadKind) {
    uploadSession.value = {
      kind,
      packageName:
        kind === 'folder' ? 'IBM i discovery package' : 'ibm-i-modernization-evidence.zip',
      files: mockFolderInventory.map(file => ({ ...file }))
    }
    isProductReportOpen.value = false
    options.showDocumentsTab?.()
  }

  function cancelMockUpload() {
    uploadSession.value = null
  }

  function createProductBatch() {
    const session = uploadSession.value
    if (!session || session.files.every(file => !file.supported)) {
      return
    }
    activeProductBatchFiles.value = session.files.map(file => ({ ...file }))
    uploadSession.value = null
    isProductReportOpen.value = false
  }

  function openProductReport() {
    isProductReportOpen.value = true
  }

  function closeProductReport() {
    isProductReportOpen.value = false
  }

  return {
    uploadSession,
    activeProductBatchFiles,
    isProductReportOpen,
    visibleProductBatchFiles,
    productBatchMetrics,
    productProcessingIssues,
    reportSections,
    openMockUpload,
    cancelMockUpload,
    createProductBatch,
    openProductReport,
    closeProductReport
  }
}

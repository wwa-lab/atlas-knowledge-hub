<script setup lang="ts">
import { ref } from 'vue'
import { mockFileStatusLabel } from '@/domain/viewModels'
import type {
  ApiBatchMetricSummary,
  ManualUrlDraft,
  MockUploadSession,
  ProductBatchMetrics,
  ProductReportSection,
  ProductSpaceCard
} from '@/domain/viewModels'
import type {
  ApiBatch,
  ApiFileItem,
  ApiManualUrlSource,
  ApiSourceChunk,
  ApiSpace,
  ManualUrlFetchIntent
} from '@/types'

const props = defineProps<{
  apiBatchMetrics: ApiBatchMetricSummary
  productBatchMetrics: ProductBatchMetrics
  apiBackedProductSpace: ApiSpace | null
  selectedProductSpace: ProductSpaceCard
  isCreatingBatch: boolean
  canWriteContent: boolean
  manualUrlSources: ApiManualUrlSource[]
  manualUrlDraft: ManualUrlDraft
  isCreatingManualUrl: boolean
  manualUrlError: string
  workflowMessage: string
  workflowError: string
  batches: ApiBatch[]
  files: ApiFileItem[]
  chunks: ApiSourceChunk[]
  uploadSession: MockUploadSession | null
  activeProductBatchFiles: unknown[]
  visibleProductBatchFiles: ProductReportSection['files']
  isProductReportOpen: boolean
  reportSections: ProductReportSection[]
}>()

const emit = defineEmits<{
  documentUpload: [event: { target: unknown }]
  createApiBatch: []
  updateManualUrlDraft: [draft: ManualUrlDraft]
  submitManualUrlSource: []
  selectBatch: [batchId: string]
  selectFile: [fileId: string]
  cancelMockUpload: []
  createProductBatch: []
  openReport: []
  closeReport: []
}>()

const documentUploadInput = ref<{ click: () => void } | null>(null)

function updateManualUrlDraft<K extends keyof ManualUrlDraft>(key: K, value: ManualUrlDraft[K]) {
  emit('updateManualUrlDraft', {
    ...props.manualUrlDraft,
    [key]: value
  })
}

function openDocumentUpload() {
  documentUploadInput.value?.click()
}

function forwardDocumentUpload(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: unknown }).target
      : null
  emit('documentUpload', { target })
}

function readInputValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}

function readManualUrlFetchIntent(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? (target.value as ManualUrlFetchIntent) : 'FETCH_LATER'
}
</script>

<template>
  <div class="atlas-doc-layout">
    <aside>
      <h3>批次状态</h3>
      <button>API Batches {{ apiBatchMetrics.batchCount }}</button>
      <button>API Files {{ apiBatchMetrics.fileCount }}</button>
      <button>API Chunks {{ apiBatchMetrics.chunkCount }}</button>
      <button>Inventory {{ productBatchMetrics.total }}</button>
      <button>Supported {{ productBatchMetrics.supported }}</button>
      <button>Unsupported {{ productBatchMetrics.unsupported }}</button>
      <button>Review Required {{ productBatchMetrics.reviewRequired }}</button>
      <button data-testid="vue-view-report" type="button" @click="emit('openReport')">
        查看报告
      </button>
    </aside>
    <div class="atlas-upload-workflow" data-testid="vue-upload-workflow">
      <section class="atlas-api-metadata" data-testid="vue-api-metadata">
        <header>
          <div>
            <h2>API-backed metadata</h2>
            <p>
              {{ apiBackedProductSpace?.name ?? selectedProductSpace.name }} · upload, parse, review
              and downstream refresh use Atlas API data.
            </p>
          </div>
          <input
            ref="documentUploadInput"
            data-testid="vue-api-upload-input"
            type="file"
            accept=".pdf,.zip,application/pdf,application/zip"
            multiple
            hidden
            @change="forwardDocumentUpload"
          />
          <button
            data-testid="vue-api-upload-documents"
            type="button"
            :disabled="isCreatingBatch || !apiBackedProductSpace || !canWriteContent"
            @click="openDocumentUpload"
          >
            {{ isCreatingBatch ? 'Uploading...' : 'Upload PDF / ZIP' }}
          </button>
          <button
            class="secondary"
            data-testid="vue-api-create-batch"
            type="button"
            :disabled="isCreatingBatch || !apiBackedProductSpace || !canWriteContent"
            @click="emit('createApiBatch')"
          >
            Create sample batch
          </button>
          <button class="coming-soon-button" data-testid="coming-soon" type="button" disabled>
            Office / OCR coming soon
          </button>
        </header>
        <div class="atlas-metric-strip">
          <span>Batches {{ apiBatchMetrics.batchCount }}</span>
          <span>Files {{ apiBatchMetrics.fileCount }}</span>
          <span>Chunks {{ apiBatchMetrics.chunkCount }}</span>
          <span>Markdown {{ apiBatchMetrics.markdownGenerated }}</span>
          <span>Review required {{ apiBatchMetrics.reviewRequired }}</span>
          <span>Manual URLs {{ manualUrlSources.length }}</span>
        </div>
        <form
          class="manual-url-ingest"
          data-testid="vue-manual-url-ingest"
          @submit.prevent="emit('submitManualUrlSource')"
        >
          <label>
            <span>Manual URL source</span>
            <input
              :value="manualUrlDraft.url"
              data-testid="vue-manual-url-input"
              type="url"
              autocomplete="off"
              @input="updateManualUrlDraft('url', readInputValue($event))"
            />
          </label>
          <label>
            <span>Title</span>
            <input
              :value="manualUrlDraft.title"
              data-testid="vue-manual-url-title"
              type="text"
              @input="updateManualUrlDraft('title', readInputValue($event))"
            />
          </label>
          <label>
            <span>Fetch intent</span>
            <select
              :value="manualUrlDraft.fetchIntent"
              data-testid="vue-manual-url-intent"
              @change="updateManualUrlDraft('fetchIntent', readManualUrlFetchIntent($event))"
            >
              <option value="METADATA_ONLY">Metadata only</option>
              <option value="FETCH_LATER">Fetch later</option>
            </select>
          </label>
          <button
            data-testid="vue-manual-url-submit"
            type="submit"
            :disabled="isCreatingManualUrl || !canWriteContent"
          >
            {{ isCreatingManualUrl ? 'Registering...' : 'Register URL' }}
          </button>
          <p v-if="manualUrlError" class="atlas-inline-warning">{{ manualUrlError }}</p>
        </form>
        <p v-if="workflowMessage">{{ workflowMessage }}</p>
        <p v-if="workflowError" class="atlas-inline-warning">{{ workflowError }}</p>
        <div class="atlas-api-grid">
          <article data-testid="vue-manual-url-status">
            <strong>Manual URL sources</strong>
            <p v-if="manualUrlSources.length === 0">No manual URL sources yet.</p>
            <div v-for="source in manualUrlSources" :key="source.id" class="manual-url-card">
              <span>{{ source.displayUrl }}</span>
              <small>
                {{ source.ingestStatus }} · {{ source.reviewStatus }} · {{ source.fetchPolicy }} ·
                confidence {{ source.confidence.toFixed(2) }}
              </small>
              <small>{{ source.eligibilityStatus }} · {{ source.sourceTrace }}</small>
            </div>
          </article>
          <article>
            <strong>Batches</strong>
            <p v-if="batches.length === 0">No API batches yet.</p>
            <button
              v-for="batch in batches"
              :key="batch.id"
              type="button"
              @click="emit('selectBatch', batch.id)"
            >
              {{ batch.name }} · {{ batch.sourceKind }} · {{ batch.metrics.totalFiles }} files
            </button>
          </article>
          <article>
            <strong>Files</strong>
            <p v-if="files.length === 0">No API files yet.</p>
            <button
              v-for="file in files"
              :key="file.id"
              type="button"
              @click="emit('selectFile', file.id)"
            >
              {{ file.sourcePath }} · {{ file.status }} · {{ file.reviewStatus }}
            </button>
          </article>
          <article>
            <strong>Source chunks</strong>
            <p v-if="chunks.length === 0">No API chunks yet.</p>
            <ul>
              <li v-for="chunk in chunks" :key="chunk.id">
                {{ chunk.id }} · {{ chunk.sourceFile }} · {{ chunk.section ?? 'section n/a' }} ·
                confidence {{ chunk.confidence ?? 'n/a' }} · {{ chunk.reviewStatus }}
              </li>
            </ul>
          </article>
        </div>
      </section>

      <section v-if="uploadSession" class="atlas-upload-review" data-testid="vue-upload-review">
        <header>
          <div>
            <h2>Upload Review · {{ uploadSession.kind === 'folder' ? 'Folder' : 'ZIP' }}</h2>
            <p>{{ uploadSession.packageName }} · detected {{ uploadSession.files.length }} files</p>
          </div>
          <button type="button" @click="emit('cancelMockUpload')">Cancel</button>
        </header>
        <div class="atlas-inventory-list">
          <article
            v-for="file in uploadSession.files"
            :key="file.id"
            class="atlas-inventory-row"
            data-testid="vue-inventory-row"
          >
            <strong>{{ file.path }}</strong>
            <span>{{ file.type }} · {{ file.size }}</span>
            <span :class="['atlas-status-badge', file.status.toLowerCase().replaceAll('_', '-')]">
              {{ mockFileStatusLabel(file.status) }}
            </span>
            <span>confidence {{ file.confidence.toFixed(2) }} · {{ file.reviewStatus }}</span>
            <small v-if="file.reason">{{ file.reason }}</small>
          </article>
        </div>
        <p v-if="uploadSession.files.every(file => !file.supported)" class="atlas-inline-warning">
          No supported files to process.
        </p>
        <button
          data-testid="vue-create-batch"
          type="button"
          :disabled="uploadSession.files.every(file => !file.supported)"
          @click="emit('createProductBatch')"
        >
          Create Batch
        </button>
      </section>

      <section class="atlas-batch-summary" data-testid="vue-batch-summary">
        <header>
          <div>
            <h2>文件树与解析状态</h2>
            <p>Mock batch keeps source_trace, confidence, and review status visible.</p>
          </div>
          <span>{{ activeProductBatchFiles.length > 0 ? 'Batch created' : 'Seeded preview' }}</span>
        </header>
        <div class="atlas-metric-strip">
          <span>Total {{ productBatchMetrics.total }}</span>
          <span>Failed {{ productBatchMetrics.failed }}</span>
          <span>OCR {{ productBatchMetrics.ocr }}</span>
          <span>Low confidence {{ productBatchMetrics.lowConfidence }}</span>
          <span>Published {{ productBatchMetrics.published }}</span>
        </div>
        <table>
          <tbody>
            <tr v-for="file in visibleProductBatchFiles" :key="file.id" data-testid="vue-file-row">
              <td>{{ file.path }}</td>
              <td>{{ mockFileStatusLabel(file.status) }}</td>
              <td>{{ file.confidence.toFixed(2) }}</td>
              <td>{{ file.sourceTrace }}</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section v-if="isProductReportOpen" class="atlas-report-panel" data-testid="vue-batch-report">
        <header>
          <h2>Batch Report</h2>
          <button type="button" @click="emit('closeReport')">Close</button>
        </header>
        <div class="atlas-report-grid">
          <article v-for="section in reportSections" :key="section.label">
            <strong>{{ section.label }} · {{ section.files.length }}</strong>
            <p v-if="section.files.length === 0">No items.</p>
            <ul>
              <li v-for="file in section.files" :key="file.id">
                {{ file.path }} · {{ mockFileStatusLabel(file.status) }} · confidence
                {{ file.confidence.toFixed(2) }} · {{ file.reviewStatus }}
                <br />
                source_trace: {{ file.sourceTrace }}
              </li>
            </ul>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

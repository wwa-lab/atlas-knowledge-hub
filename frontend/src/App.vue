<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import GlobalChatView from '@/components/chat/GlobalChatView.vue'
import ProductHomeView from '@/components/home/ProductHomeView.vue'
import ProductShell from '@/components/layout/ProductShell.vue'
import P0WorkbenchView from '@/components/p0/P0WorkbenchView.vue'
import SettingsOrchestrator from '@/components/settings/SettingsOrchestrator.vue'
import SpaceConnectorsTab from '@/components/space/SpaceConnectorsTab.vue'
import SpaceDetailView from '@/components/space/SpaceDetailView.vue'
import SpaceDocumentsTab from '@/components/space/SpaceDocumentsTab.vue'
import SpaceGraphTab from '@/components/space/SpaceGraphTab.vue'
import SpaceReviewTab from '@/components/space/SpaceReviewTab.vue'
import SpaceWikiTab from '@/components/space/SpaceWikiTab.vue'
import { safeError } from '@/composables/apiErrors'
import { useAsk } from '@/composables/useAsk'
import { useBatches } from '@/composables/useBatches'
import { useGlobalChat } from '@/composables/useGlobalChat'
import { useGraph } from '@/composables/useGraph'
import { useProductUploadWorkflow } from '@/composables/useProductUploadWorkflow'
import { useReviewQueue } from '@/composables/useReviewQueue'
import { useSettings } from '@/composables/useSettings'
import { useSpaces } from '@/composables/useSpaces'
import { useWikiPages } from '@/composables/useWikiPages'
import type { ProductView, SpaceTab } from '@/domain/viewModels'

const prototypeSrc = '/atlas-prototype.html'
const activeExperience = ref<'atlas' | 'prototype' | 'p0'>('atlas')
const productView = ref<ProductView>('home')
const activeSpaceTab = ref<SpaceTab>('wiki')

const spacesDomain = useSpaces()
const {
  spaces,
  selectedSpace,
  selectedSpaceId,
  selectedProductSpaceId,
  isLoadingSpaces,
  isLoadingSpace,
  isCreateSpaceOpen,
  isCreatingSpace,
  spacesError,
  createSpaceError,
  createSpaceStatus,
  createSpaceDraft,
  canManageSpaces,
  canWriteContent,
  canOperateKnowledge,
  canManageMembers,
  canReadGovernance,
  selectedProductApiSpace,
  apiBackedProductSpace,
  productSpaceCards,
  selectedProductSpace,
  loadCurrentUser,
  loadSpaces,
  loadSelectedSpace,
  selectSpaceIds,
  openCreateSpacePanel,
  closeCreateSpacePanel,
  createProductSpace: createSpaceRecord,
  updateSelectedSpaceInfo
} = spacesDomain

const graphDomain = useGraph({ selectedSpaceId: () => selectedSpaceId.value })
const {
  graph,
  selectedEdge,
  isLoadingGraph,
  graphError,
  graphState,
  searchText,
  nodeTypeFilter,
  edgeTypeFilter,
  reviewStatusFilter,
  evidenceOnly,
  hoveredNodeId,
  productGraphSearch,
  activeNode,
  activeSelectionLabel,
  evidenceReferences,
  visibleNodes,
  visibleEdges,
  nodeTypeOptions,
  edgeTypeOptions,
  reviewStatusOptions,
  canvasNodes,
  canvasNodeById,
  visibleProductGraphNodes,
  visibleProductGraphEdges,
  selectedProductGraphNode,
  selectedProductGraphEvidence,
  loadGraph,
  selectNode,
  selectEdge,
  edgeNodeLabel,
  selectProductGraphNode
} = graphDomain

let refreshReviewState = (_spaceId: string) => Promise.resolve()
let refreshWikiState = (_spaceId: string) => Promise.resolve()
const batchesDomain = useBatches({
  selectedSpaceId: () => selectedSpaceId.value,
  canWriteContent: () => canWriteContent.value,
  refreshReviewState: spaceId => refreshReviewState(spaceId),
  refreshWikiState: spaceId => refreshWikiState(spaceId)
})
const {
  batches,
  selectedBatchId,
  files,
  selectedFileId,
  chunks,
  manualUrlSources,
  connectorDefinitions,
  connectorSyncRun,
  connectorSyncItems,
  selectedConnectorItemId,
  manualUrlDraft,
  isCreatingBatch,
  isCreatingManualUrl,
  isLoadingConnectors,
  isStartingConnectorSync,
  workflowMessage,
  workflowError,
  manualUrlError,
  connectorError,
  selectedBatch,
  selectedFile,
  selectedChunkIds,
  apiBatchMetrics,
  manualUrlReviewRequiredCount,
  selectedConnectorItem,
  loadBatchContext,
  selectBatch,
  selectFile,
  createBatchFromBrowser,
  handleDocumentUpload,
  submitManualUrlSource,
  loadConnectorDefinitions,
  startMockConnectorSync,
  refreshWorkflow,
  setWorkflowMessage,
  setWorkflowError
} = batchesDomain

const reviewQueue = useReviewQueue({
  selectedFile: () => selectedFile.value,
  selectedChunkIds: () => selectedChunkIds.value,
  selectedBatchId: () => selectedBatchId.value,
  canOperateKnowledge: () => canOperateKnowledge.value,
  refreshWorkflow,
  setWorkflowMessage,
  setWorkflowError
})
const {
  reviewQueues,
  deadLetterEntries,
  selectedDeadLetterId,
  isReviewing,
  isLoadingDeadLetters,
  isRetryingDeadLetter,
  isAcknowledgingDeadLetter,
  deadLetterError,
  canApprove,
  readyQueueCount,
  blockedQueueCount,
  reviewQueueCards,
  apiReadyToPublishCount,
  apiBlockedReviewCount,
  apiProcessingIssues: reviewProcessingIssues,
  selectedDeadLetterEntry,
  loadReviewQueues,
  approveSelectedFile,
  loadDeadLetters,
  retrySelectedDeadLetter,
  acknowledgeSelectedDeadLetter
} = reviewQueue

const wikiDomain = useWikiPages({
  selectedSpaceId: () => selectedSpaceId.value,
  selectedBatch: () => selectedBatch.value,
  selectedBatchId: () => selectedBatchId.value,
  selectedFile: () => selectedFile.value,
  selectedFileId: () => selectedFileId.value,
  chunks: () => chunks.value,
  canOperateKnowledge: () => canOperateKnowledge.value,
  refreshWorkflow,
  loadGraph,
  currentGraphQuery: () => searchText.value,
  setWorkflowMessage,
  setWorkflowError
})
const {
  wikiPages,
  isPublishing,
  isRefreshingEvidence,
  downstreamReady,
  canPublish,
  apiWikiIssueCards,
  visibleProductWikiPages,
  selectedProductWikiPage,
  selectedProductWikiPageIssues,
  loadWikiState,
  publishSelectedFile,
  refreshDownstreamEvidence,
  selectProductWikiPage
} = wikiDomain
refreshReviewState = loadReviewQueues
refreshWikiState = loadWikiState

const settingsDomain = useSettings({
  selectedProductSpace: () => selectedProductSpace.value,
  selectedProductApiSpace: () => selectedProductApiSpace.value,
  selectedSpaceId: () => selectedSpaceId.value,
  canReadGovernance: () => canReadGovernance.value,
  updateSelectedSpaceInfo
})
const { visibleModels, openSettings, closeSettings, loadAuditEvents, loadModelCapabilities } =
  settingsDomain

const askDomain = useAsk({
  selectedSpaceId: () => selectedSpaceId.value,
  selectedFileId: () => selectedFileId.value,
  canOperateKnowledge: () => canOperateKnowledge.value
})
const {
  askRun,
  askSessions,
  selectedAskSession,
  askQuestion,
  productAskQuestion,
  productAskMode,
  isAsking,
  askError,
  askQualityError,
  askSessionError,
  canAsk,
  productAskAnswer,
  askQualityChips,
  submitAsk,
  submitProductAsk
} = askDomain

const { selectedChatSpaces, toggleChatSpace } = useGlobalChat()
const {
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
} = useProductUploadWorkflow({
  showDocumentsTab: () => {
    activeSpaceTab.value = 'docs'
  }
})

const apiProcessingIssues = computed(() => [
  ...reviewProcessingIssues.value,
  ...apiWikiIssueCards.value
])

onMounted(() => {
  void initialize()
})

async function initialize() {
  await Promise.all([
    loadCurrentUser(),
    loadModelCapabilities(),
    loadConnectorDefinitions(),
    loadDeadLetters()
  ])
  const initialSpaceId = await loadSpaces()
  await selectSpace(initialSpaceId)
}

async function selectSpace(spaceId: string) {
  selectSpaceIds(spaceId)
  try {
    await Promise.all([
      loadSelectedSpace(spaceId),
      loadBatchContext(spaceId),
      loadReviewQueues(spaceId),
      loadWikiState(spaceId),
      canReadGovernance.value ? loadAuditEvents(spaceId) : Promise.resolve(),
      loadGraph()
    ])
  } catch (error) {
    setWorkflowError(safeError(error, 'Unable to load selected space workflow.'))
  }
}

function showProductHome() {
  productView.value = 'home'
  closeSettings()
}

function showProductChat() {
  productView.value = 'chat'
  closeSettings()
}

function openProductSpace(spaceId: string) {
  selectedProductSpaceId.value = spaceId
  productView.value = 'space'
  activeSpaceTab.value = 'wiki'
  closeSettings()
  if (spaces.value.some(space => space.id === spaceId)) {
    void selectSpace(spaceId)
  }
}

async function createProductSpace() {
  const created = await createSpaceRecord()
  if (created) {
    await selectSpace(created.id)
  }
}
</script>

<template>
  <ProductShell
    v-if="activeExperience === 'atlas'"
    :product-view="productView"
    :can-manage-members="canManageMembers"
    @show-home="showProductHome"
    @show-chat="showProductChat"
    @open-settings="openSettings"
  >
    <template v-if="productView === 'home'">
      <ProductHomeView
        :product-space-cards="productSpaceCards"
        :api-space-count="spaces.length"
        :can-manage-spaces="canManageSpaces"
        :is-create-space-open="isCreateSpaceOpen"
        :is-creating-space="isCreatingSpace"
        :create-space-draft="createSpaceDraft"
        :create-space-status="createSpaceStatus"
        :create-space-error="createSpaceError"
        @open-create-space="openCreateSpacePanel"
        @close-create-space="closeCreateSpacePanel"
        @create-space="createProductSpace"
        @open-space="openProductSpace"
        @update-create-space-draft="draft => (createSpaceDraft = draft)"
      />
    </template>

    <template v-else-if="productView === 'chat'">
      <GlobalChatView
        :product-space-cards="productSpaceCards"
        :selected-chat-spaces="selectedChatSpaces"
        :product-ask-question="productAskQuestion"
        :product-ask-mode="productAskMode"
        :visible-models="visibleModels"
        :product-ask-answer="productAskAnswer"
        :ask-quality-chips="askQualityChips"
        :ask-quality-error="askQualityError"
        @toggle-chat-space="toggleChatSpace"
        @update-product-ask-question="question => (productAskQuestion = question)"
        @update-product-ask-mode="mode => (productAskMode = mode)"
        @submit-product-ask="submitProductAsk"
      />
    </template>

    <template v-else>
      <SpaceDetailView
        :selected-product-space="selectedProductSpace"
        :active-space-tab="activeSpaceTab"
        :workflow-error="workflowError"
        @show-home="showProductHome"
        @open-mock-upload="openMockUpload"
        @update-active-tab="tab => (activeSpaceTab = tab)"
      >
        <SpaceDocumentsTab
          v-if="activeSpaceTab === 'docs'"
          :api-batch-metrics="apiBatchMetrics"
          :product-batch-metrics="productBatchMetrics"
          :api-backed-product-space="apiBackedProductSpace"
          :selected-product-space="selectedProductSpace"
          :is-creating-batch="isCreatingBatch"
          :can-write-content="canWriteContent"
          :manual-url-sources="manualUrlSources"
          :manual-url-draft="manualUrlDraft"
          :is-creating-manual-url="isCreatingManualUrl"
          :manual-url-error="manualUrlError"
          :workflow-message="workflowMessage"
          :workflow-error="workflowError"
          :batches="batches"
          :files="files"
          :chunks="chunks"
          :upload-session="uploadSession"
          :active-product-batch-files="activeProductBatchFiles"
          :visible-product-batch-files="visibleProductBatchFiles"
          :is-product-report-open="isProductReportOpen"
          :report-sections="reportSections"
          @document-upload="handleDocumentUpload"
          @create-api-batch="createBatchFromBrowser"
          @update-manual-url-draft="draft => (manualUrlDraft = draft)"
          @submit-manual-url-source="submitManualUrlSource"
          @select-batch="selectBatch"
          @select-file="selectFile"
          @cancel-mock-upload="cancelMockUpload"
          @create-product-batch="createProductBatch"
          @open-report="openProductReport"
          @close-report="closeProductReport"
        />
        <SpaceConnectorsTab
          v-else-if="activeSpaceTab === 'connectors'"
          :connector-definitions="connectorDefinitions"
          :is-loading-connectors="isLoadingConnectors"
          :is-starting-connector-sync="isStartingConnectorSync"
          :selected-space-id="selectedSpaceId"
          :connector-sync-run="connectorSyncRun"
          :connector-error="connectorError"
          :connector-sync-items="connectorSyncItems"
          :selected-connector-item="selectedConnectorItem"
          @start-connector-sync="startMockConnectorSync"
          @select-connector-item="itemId => (selectedConnectorItemId = itemId)"
        />
        <SpaceReviewTab
          v-else-if="activeSpaceTab === 'review'"
          :api-blocked-review-count="apiBlockedReviewCount"
          :api-ready-to-publish-count="apiReadyToPublishCount"
          :manual-url-review-required-count="manualUrlReviewRequiredCount"
          :product-batch-metrics="productBatchMetrics"
          :review-queue-cards="reviewQueueCards"
          :api-processing-issues="apiProcessingIssues"
          :can-approve="canApprove"
          :is-reviewing="isReviewing"
          :dead-letter-entries="deadLetterEntries"
          :is-loading-dead-letters="isLoadingDeadLetters"
          :dead-letter-error="deadLetterError"
          :selected-dead-letter-entry="selectedDeadLetterEntry"
          :is-retrying-dead-letter="isRetryingDeadLetter"
          :is-acknowledging-dead-letter="isAcknowledgingDeadLetter"
          :product-processing-issues="productProcessingIssues"
          @approve-selected-file="approveSelectedFile"
          @load-dead-letters="loadDeadLetters"
          @select-dead-letter-entry="entryId => (selectedDeadLetterId = entryId)"
          @retry-selected-dead-letter="retrySelectedDeadLetter"
          @acknowledge-selected-dead-letter="acknowledgeSelectedDeadLetter"
        />
        <SpaceWikiTab
          v-else-if="activeSpaceTab === 'wiki'"
          :visible-product-wiki-pages="visibleProductWikiPages"
          :selected-product-wiki-page="selectedProductWikiPage"
          :selected-product-wiki-page-issues="selectedProductWikiPageIssues"
          :can-publish="canPublish"
          :is-publishing="isPublishing"
          @select-wiki-page="selectProductWikiPage"
          @publish-selected-file="publishSelectedFile"
        />
        <SpaceGraphTab
          v-else
          :product-graph-search="productGraphSearch"
          :visible-product-graph-nodes="visibleProductGraphNodes"
          :visible-product-graph-edges="visibleProductGraphEdges"
          :selected-product-graph-node="selectedProductGraphNode"
          :selected-product-graph-evidence="selectedProductGraphEvidence"
          @update-product-graph-search="query => (productGraphSearch = query)"
          @select-graph-node="selectProductGraphNode"
        />
      </SpaceDetailView>
    </template>

    <SettingsOrchestrator
      :settings="settingsDomain"
      :selected-product-space="selectedProductSpace"
      :can-read-governance="canReadGovernance"
      :can-manage-members="canManageMembers"
    />

    <button class="prototype-entry" type="button" @click="activeExperience = 'prototype'">
      Prototype iframe
    </button>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </ProductShell>

  <main
    v-if="activeExperience === 'prototype'"
    class="product-experience"
    aria-label="Atlas logged-in product workspace"
  >
    <iframe
      class="product-frame"
      title="Atlas Knowledge Hub logged-in product experience"
      :src="prototypeSrc"
    ></iframe>
    <button class="workbench-entry" type="button" @click="activeExperience = 'p0'">
      Full-stack P0 workbench
    </button>
  </main>

  <P0WorkbenchView
    v-if="activeExperience === 'p0'"
    v-model:ask-question="askQuestion"
    v-model:search-text="searchText"
    v-model:node-type-filter="nodeTypeFilter"
    v-model:edge-type-filter="edgeTypeFilter"
    v-model:review-status-filter="reviewStatusFilter"
    v-model:evidence-only="evidenceOnly"
    v-model:hovered-node-id="hoveredNodeId"
    :is-loading-spaces="isLoadingSpaces"
    :spaces-error="spacesError"
    :spaces="spaces"
    :selected-space-id="selectedSpaceId"
    :selected-space="selectedSpace"
    :wiki-pages="wikiPages"
    :blocked-queue-count="blockedQueueCount"
    :is-loading-space="isLoadingSpace"
    :workflow-error="workflowError"
    :workflow-message="workflowMessage"
    :is-creating-batch="isCreatingBatch"
    :can-write-content="canWriteContent"
    :batches="batches"
    :selected-batch-id="selectedBatchId"
    :files="files"
    :selected-file-id="selectedFileId"
    :chunks="chunks"
    :ready-queue-count="readyQueueCount"
    :review-queues="reviewQueues"
    :can-approve="canApprove"
    :is-reviewing="isReviewing"
    :selected-file="selectedFile"
    :can-publish="canPublish"
    :is-publishing="isPublishing"
    :is-refreshing-evidence="isRefreshingEvidence"
    :can-operate-knowledge="canOperateKnowledge"
    :downstream-ready="downstreamReady"
    :ask-run="askRun"
    :can-ask="canAsk"
    :is-asking="isAsking"
    :ask-error="askError"
    :ask-quality-chips="askQualityChips"
    :ask-quality-error="askQualityError"
    :ask-session-error="askSessionError"
    :selected-ask-session="selectedAskSession"
    :ask-sessions="askSessions"
    :graph="graph"
    :graph-state="graphState"
    :is-loading-graph="isLoadingGraph"
    :graph-error="graphError"
    :node-type-options="nodeTypeOptions"
    :edge-type-options="edgeTypeOptions"
    :review-status-options="reviewStatusOptions"
    :visible-edges="visibleEdges"
    :canvas-nodes="canvasNodes"
    :canvas-node-by-id="canvasNodeById"
    :selected-edge="selectedEdge"
    :active-node="activeNode"
    :visible-nodes="visibleNodes"
    :active-selection-label="activeSelectionLabel"
    :evidence-references="evidenceReferences"
    :edge-node-label="edgeNodeLabel"
    @show-prototype="activeExperience = 'prototype'"
    @select-space="selectSpace"
    @create-batch-from-browser="createBatchFromBrowser"
    @select-batch="selectBatch"
    @select-file="selectFile"
    @approve-selected-file="approveSelectedFile"
    @publish-selected-file="publishSelectedFile"
    @refresh-downstream-evidence="refreshDownstreamEvidence"
    @submit-ask="submitAsk"
    @select-node="selectNode"
    @select-edge="selectEdge"
  />
</template>

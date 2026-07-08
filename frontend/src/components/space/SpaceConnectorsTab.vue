<script setup lang="ts">
import type { ApiConnectorDefinition, ApiConnectorSyncItem, ApiConnectorSyncRun } from '@/types'

defineProps<{
  connectorDefinitions: ApiConnectorDefinition[]
  isLoadingConnectors: boolean
  isStartingConnectorSync: boolean
  selectedSpaceId: string
  connectorSyncRun: ApiConnectorSyncRun | null
  connectorError: string
  connectorSyncItems: ApiConnectorSyncItem[]
  selectedConnectorItem: ApiConnectorSyncItem | null
}>()

const emit = defineEmits<{
  startConnectorSync: []
  selectConnectorItem: [itemId: string]
}>()
</script>

<template>
  <div class="atlas-doc-layout" data-testid="vue-connector-sync">
    <aside>
      <h3>Connector Registry</h3>
      <button v-for="definition in connectorDefinitions" :key="definition.id" type="button">
        {{ definition.name }} · {{ definition.status }}
      </button>
      <button v-if="connectorDefinitions.length === 0" type="button" disabled>
        {{ isLoadingConnectors ? 'Loading connectors' : 'No connectors available' }}
      </button>
      <button
        data-testid="vue-start-connector-sync"
        type="button"
        :disabled="isStartingConnectorSync || connectorDefinitions.length === 0 || !selectedSpaceId"
        @click="emit('startConnectorSync')"
      >
        {{ isStartingConnectorSync ? 'Syncing...' : 'Start mock sync' }}
      </button>
    </aside>
    <div class="atlas-upload-workflow">
      <section class="atlas-api-metadata">
        <header>
          <div>
            <h2>Connector Sync v0</h2>
            <p>
              Local fixture connector only. Output artifacts remain review-required and do not
              become trusted Wiki, Ask, or Graph knowledge.
            </p>
          </div>
          <span>{{ connectorSyncRun?.status ?? 'No run yet' }}</span>
        </header>
        <p v-if="connectorError" class="atlas-inline-warning">{{ connectorError }}</p>
        <div class="atlas-metric-strip" data-testid="vue-connector-run-status">
          <span>Items {{ connectorSyncRun?.itemCount ?? 0 }}</span>
          <span>Review {{ connectorSyncRun?.reviewRequiredCount ?? 0 }}</span>
          <span>Failed {{ connectorSyncRun?.failedCount ?? 0 }}</span>
          <span>{{ connectorSyncRun?.safeMessage ?? 'Awaiting local fixture sync' }}</span>
        </div>
        <table>
          <tbody>
            <tr
              v-for="item in connectorSyncItems"
              :key="item.id"
              data-testid="vue-connector-item"
              @click="emit('selectConnectorItem', item.id)"
            >
              <td>{{ item.title }}</td>
              <td>{{ item.itemStatus }}</td>
              <td>{{ item.sourceReference }}</td>
              <td>{{ item.safeErrorCategory }}</td>
            </tr>
          </tbody>
        </table>
        <p v-if="connectorSyncItems.length === 0" class="atlas-inline-warning">
          No connector sync items yet.
        </p>
      </section>
      <section
        v-if="selectedConnectorItem"
        class="atlas-batch-summary"
        data-testid="vue-connector-trace"
      >
        <header>
          <div>
            <h2>{{ selectedConnectorItem.title }}</h2>
            <p>
              {{ selectedConnectorItem.itemStatus }} · confidence
              {{ selectedConnectorItem.confidence ?? 'n/a' }} · review eligible
              {{ selectedConnectorItem.reviewEligible ? 'yes' : 'no' }}
            </p>
          </div>
          <span>review-required handoff</span>
        </header>
        <p>
          source_trace:
          {{
            Object.entries(selectedConnectorItem.sourceTrace)
              .map(([key, value]) => `${key}=${value}`)
              .join(' · ')
          }}
        </p>
        <p>
          provenance:
          {{
            Object.entries(selectedConnectorItem.provenance)
              .map(([key, value]) => `${key}=${value}`)
              .join(' · ')
          }}
        </p>
        <ul>
          <li v-for="artifact in selectedConnectorItem.outputArtifacts" :key="artifact.id">
            {{ artifact.artifactType }} · {{ artifact.reviewStatus }} ·
            {{ artifact.targetPath }}
          </li>
        </ul>
        <p v-if="selectedConnectorItem.safeErrorMessage" class="atlas-inline-warning">
          {{ selectedConnectorItem.safeErrorMessage }}
        </p>
      </section>
    </div>
  </div>
</template>

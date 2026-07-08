<script setup lang="ts">
import { productNodeStyle } from '@/domain/viewModels'
import type { ProductGraphEdge, ProductGraphNode } from '@/domain/viewModels'

defineProps<{
  productGraphSearch: string
  visibleProductGraphNodes: ProductGraphNode[]
  visibleProductGraphEdges: ProductGraphEdge[]
  selectedProductGraphNode: ProductGraphNode
  selectedProductGraphEvidence: string[]
}>()

const emit = defineEmits<{
  updateProductGraphSearch: [query: string]
  selectGraphNode: [nodeId: string]
}>()

function readInputValue(event: unknown) {
  const target =
    event && typeof event === 'object' && 'target' in event
      ? (event as { target?: { value?: unknown } }).target
      : null
  return typeof target?.value === 'string' ? target.value : ''
}
</script>

<template>
  <div class="atlas-graph-layout" data-testid="vue-product-graph">
    <section>
      <header class="atlas-product-graph-head">
        <div>
          <h2>Knowledge Graph</h2>
          <p>Graph belongs to this Knowledge Space and keeps evidence visible.</p>
        </div>
        <input
          :value="productGraphSearch"
          data-testid="vue-graph-search"
          placeholder="Search node, type, or review state"
          @input="emit('updateProductGraphSearch', readInputValue($event))"
        />
      </header>
      <div class="atlas-graph-canvas">
        <button
          v-for="node in visibleProductGraphNodes"
          :key="node.id"
          :class="[
            'atlas-product-node',
            node.reviewStatus.toLowerCase().replaceAll('_', '-'),
            { active: selectedProductGraphNode.id === node.id }
          ]"
          :style="productNodeStyle(node)"
          type="button"
          data-testid="vue-graph-node"
          @click="emit('selectGraphNode', node.id)"
        >
          <strong>{{ node.label }}</strong>
          <span>{{ node.type }}</span>
        </button>
        <span v-for="edge in visibleProductGraphEdges" :key="edge.id" class="atlas-product-edge">
          {{ edge.label }} · confidence {{ edge.confidence.toFixed(2) }}
        </span>
      </div>
      <div class="atlas-graph-legend">
        <span>Wiki Page</span>
        <span>Entity</span>
        <span>Concept</span>
        <span>Document</span>
        <span>Review Required</span>
      </div>
    </section>
    <aside data-testid="vue-graph-detail">
      <h3>{{ selectedProductGraphNode.label }}</h3>
      <p>{{ selectedProductGraphNode.detail }}</p>
      <dl>
        <dt>Type</dt>
        <dd>{{ selectedProductGraphNode.type }}</dd>
        <dt>Review</dt>
        <dd>{{ selectedProductGraphNode.reviewStatus }}</dd>
        <dt>Confidence</dt>
        <dd>{{ selectedProductGraphNode.confidence.toFixed(2) }}</dd>
      </dl>
      <strong>Evidence / source trace</strong>
      <ul>
        <li v-for="evidence in selectedProductGraphEvidence" :key="evidence">
          {{ evidence }}
        </li>
      </ul>
      <p class="atlas-inline-warning">
        Review Required nodes express trust boundaries and are excluded from trusted Ask.
      </p>
    </aside>
  </div>
</template>
